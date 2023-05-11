package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.extractor.GenreResultSetExtractor;

import static ru.yandex.practicum.filmorate.constants.Constant.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreResultSetExtractor extractor;

    @Override
    public Map<Film, List<Genre>> getGenreAllFilms() {
        return jdbcTemplate.query(GET_GENRE_ALL_FILM, extractor);
    }

    @Override
    public List<Genre> getAllGenre() {
        return jdbcTemplate.query(GET_ALL_GENRE, (rs, rowNum) -> mapRow(rs));
    }

    @Override
    public Genre getGenreById(int id) {
        SqlRowSet genreRows = jdbcTemplate.queryForRowSet(GET_GENRE_BY_ID, id);
        if (genreRows.next()) {
            return jdbcTemplate.queryForObject(GET_GENRE_BY_ID, (rs, rowNum) -> mapRow(rs), id);
        } else {
            log.warn("Жанр с идентификатором {} не найден", id);
            throw new GenreNotFoundException("Жанра с id = " + id + " нет в списке");
        }
    }

    @Override
    public List<Genre> getGenreFilmId(int id) {
        return jdbcTemplate.query(GET_GENRE_FROM_FILM, (rs, rowNum) -> mapRow(rs), id);
    }

    private Genre mapRow(ResultSet rs) throws SQLException {
        return Genre.builder()
                .id(rs.getInt("genre_id"))
                .name(rs.getString("name"))
                .build();
    }
}
