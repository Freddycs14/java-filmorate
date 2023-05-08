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

import java.sql.*;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Objects;

@Repository("FilmDbStorage")
@Component
@RequiredArgsConstructor
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private static final LocalDate LIMIT_DATE = LocalDate.of(1895, Month.DECEMBER, 28);

    @Override
    public Film createFilm(Film film) {
        validateMpa(film.getMpa().getId());
        validateDate(film);
        for (Genre genre : film.getGenres()) {
            validateGenre(genre.getId());
        }
        String sqlQuery = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
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
        String sqlQueryFilm = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?" +
                " WHERE film_id = ?";
        if (jdbcTemplate.update(sqlQueryFilm, film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()) > 0) {
            String sqlQueryFilmGenre = "DELETE FROM film_genre WHERE film_id = ?";
            jdbcTemplate.update(sqlQueryFilmGenre, film.getId());
            film.getGenres().forEach(genre -> addGenreToFilm(film.getId(), genre.getId()));
            log.info("Фильм {} обновлен в таблице", film);
            return film;
        }
        log.warn("Фильм с id {} не найден", film.getId());
        throw new FilmNotFoundException(String.format("Фильм с id %d не найден", film.getId()));
    }

    @Override
    public Film deleteFilm(Film film) {
        String sqlQuery = "DELETE FROM films WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery, film.getId());
        log.info("Фильм {} удалён", film);
        return film;
    }

    @Override
    public List<Film> getListFilms() {
        String sqlQuery = "SELECT f.*, m.name AS mpa_name FROM films AS f JOIN mpa AS m ON f.mpa_id = m.mpa_id";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> mapRow(rs));
    }

    @Override
    public Film getFilmById(int id) {
        String sqlQuery = "SELECT f.*, m.name AS mpa_name FROM films AS f JOIN mpa AS m ON f.mpa_id = m.mpa_id WHERE f.film_id = ?";
        try {
            return jdbcTemplate.queryForObject(sqlQuery, (rs, rowNum) -> mapRow(rs), id);
        } catch (DataRetrievalFailureException e) {
            log.warn("Фильм с id {} не найден", id);
            throw new FilmNotFoundException(String.format("Фильм с id %d не найден", id));
        }
    }

    @Override
    public void addGenreToFilm(int filmId, int genreId) {
        String sqlQuery = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery, filmId, genreId);
    }

    @Override
    public void deleteGenreFromFilm(int filmId, int genreId) {
        String sqlQuery = "DELETE FROM film_genre WHERE (film_id = ? AND genre_id = ?)";
        jdbcTemplate.update(sqlQuery, filmId, genreId);
    }

    @Override
    public Film addLike(int filmId, int userId) {
        validationLike(filmId, userId);
        try {
            String sqlQuery = "INSERT INTO film_likes (user_id, film_id) VALUES (?, ?)";
            jdbcTemplate.update(sqlQuery, userId, filmId);
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
        String sqlQuery = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sqlQuery, filmId, userId);
        log.info("Пользователь c идентификатором {} убрал лайк с фильма с идентификатором{}", userId, filmId);
        return getFilmById(filmId);
    }

    @Override
    public List<Integer> getLikesByFilm(int filmId) {
        String sqlQuery = "SELECT user_id FROM film_likes WHERE film_id = ?";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> rs.getInt("user_id"), filmId);
    }

    @Override
    public List<Film> getTopFilms(int count) {
        String sqlQuery = "SELECT f.*, m.name AS mpa_name FROM films AS f JOIN mpa AS m ON f.mpa_id = m.mpa_id" +
                " LEFT JOIN (SELECT film_id, COUNT(user_id) AS likes_count FROM film_likes GROUP BY film_id" +
                " ORDER BY likes_count) AS popular ON f.film_id = popular.film_id ORDER BY popular.likes_count DESC limit ?";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> mapRow(rs), count);
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
