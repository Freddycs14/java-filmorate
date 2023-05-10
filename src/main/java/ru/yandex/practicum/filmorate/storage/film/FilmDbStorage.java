package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import static ru.yandex.practicum.filmorate.constants.Constant.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Repository("FilmDbStorage")
@Component
@RequiredArgsConstructor
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film createFilm(Film film) {
        validateMpa(film.getMpa().getId());
        validateDate(film);
        for (Genre genre : film.getGenres()) {
            validateGenre(genre.getId());
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_FILM, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setLong(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);
        film.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        film.getGenres().forEach(genre -> addGenreToFilm(film.getId(), genre.getId()));
        log.info("Фильм {} сохранен", film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        validateDate(film);
        if (jdbcTemplate.update(UPDATE_FILM, film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()) > 0) {
            jdbcTemplate.update(DELETE_FILM_GENRE, film.getId());
            film.getGenres().forEach(genre -> addGenreToFilm(film.getId(), genre.getId()));
            log.info("Фильм {} обновлен в таблице", film);
            return film;
        }
        log.warn("Фильм с id {} не найден", film.getId());
        throw new FilmNotFoundException(String.format("Фильм с id %d не найден", film.getId()));
    }

    @Override
    public Film deleteFilm(Film film) {
        jdbcTemplate.update(DELETE_FILM, film.getId());
        log.info("Фильм {} удалён", film);
        return film;
    }

    @Override
    public List<Film> getListFilms() {
        //return jdbcTemplate.query(GET_LIST_FILMS, (rs, rowNum) -> mapRow(rs));
        List<Film> films = jdbcTemplate.query(GET_LIST_FILMS, (rs, rowNum) -> mapRow(rs));
        for (Film film : films) {
            if (film.getGenres() != null) {
                for (Genre genre : film.getGenres()) {
                    jdbcTemplate.update(INSERT_FILM_GENRE, film.getId(), genre.getId());
                }
            }
            if (film.getGenres().size() == 0) {
                log.error("Список фильмов пуст");
            }
        }
        log.info("Получен список всех фильмов");
        return films;
    }

    @Override
    public Film getFilmById(int id) {
        try {
            return jdbcTemplate.queryForObject(GET_FILM_BY_ID, (rs, rowNum) -> mapRow(rs), id);
        } catch (DataRetrievalFailureException e) {
            log.warn("Фильм с id {} не найден", id);
            throw new FilmNotFoundException(String.format("Фильм с id %d не найден", id));
        }
    }

    @Override
    public void addGenreToFilm(int filmId, int genreId) {
        jdbcTemplate.update(INSERT_FILM_GENRE, filmId, genreId);
    }

    @Override
    public void deleteGenreFromFilm(int filmId, int genreId) {
        jdbcTemplate.update(DELETE_FILM_GENRE_FROM_FILM, filmId, genreId);
    }

    @Override
    public Film addLike(int filmId, int userId) {
        validationLike(filmId, userId);
        try {
            jdbcTemplate.update(INSERT_FILM_LIKES, userId, filmId);
            log.info("Пользователь с идентификатором {} поставил лайк фильму с идентификатором {}", userId, filmId);
            return getFilmById(filmId);
        } catch (DataAccessException exception) {
            log.error("Пользователь id = {} уже поставил лайк фильму id = {}", userId, filmId);
            throw new ValidationException("Пользователь с уже поставил лайк этому фильму");
        }
    }

    @Override
    public Film deleteLike(int filmId, int userId) {
        validationLike(filmId, userId);
        jdbcTemplate.update(DELETE_FILM_LIKES, filmId, userId);
        log.info("Пользователь c идентификатором {} убрал лайк с фильма с идентификатором{}", userId, filmId);
        return getFilmById(filmId);
    }

    @Override
    public List<Integer> getLikesByFilm(int filmId) {
        return jdbcTemplate.query(GET_LIKES_BY_FILM, (rs, rowNum) -> rs.getInt("user_id"), filmId);
    }

    @Override
    public List<Film> getTopFilms(int count) {
        return jdbcTemplate.query(GET_TOP_FILMS, (rs, rowNum) -> mapRow(rs), count);
    }

    private Film mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("film_id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
        int duration = rs.getInt("duration");
        int mpaId = rs.getInt("mpa_id");
        String mpaName = rs.getString("mpa_name");

        Mpa mpa = Mpa.builder()
                .id(mpaId)
                .name(mpaName)
                .build();

        return Film.builder()
                .id(id)
                .name(name)
                .description(description)
                .releaseDate(releaseDate)
                .duration(duration)
                .mpa(mpa)
                .build();
    }

    private void validateDate(Film film) {
        if (film.getReleaseDate().isBefore(LIMIT_DATE)) {
            throw new ValidationException("Дата релиза не должна быть раньше 28 декабря 1895 года");
        }
    }

    private void validateMpa(int mpaId) {
        if (mpaId <= 0 || mpaId > 5) {
            log.error("Неорректно переданны данные по Mpa");
            throw new MpaNotFoundException("Неорректно переданы данные по Mpa");
        }
    }

    private void validateGenre(int genreId) {
        if (genreId <= 0 || genreId > 6) {
            log.error("Неорректно переданны данные по жанру");
            throw new GenreNotFoundException("Неорректно переданы данные по жанру");
        }
    }

    private void validationLike(int filmId, int userId) {
        if (filmId <= 0) {
            log.error("Передан неверный id фильма. Лайк не добавлен");
            throw new FilmNotFoundException("Фильм не найден");
        }
        if (userId <= 0) {
            log.error("Передан неверный id пользователя. Лайк не добавлен");
            throw new UserNotFoundException("Пользователь не найден");
        }
    }
}
