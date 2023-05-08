package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Genre> getAllGenre() {
        String sqlQuery = "SELECT * FROM genre ORDER BY genre_id";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> mapRow(rs));
    }

    @Override
    public Genre getGenreById(int id) {
        String sqlQuery = "SELECT * FROM genre WHERE genre_id = ?";
        SqlRowSet genreRows = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (genreRows.next()) {
            return jdbcTemplate.queryForObject(sqlQuery, (rs, rowNum) -> mapRow(rs), id);
        } else {
            log.warn("Жанр с идентификатором {} не найден", id);
            throw new GenreNotFoundException("Жанра с id = " + id + " нет в списке");
        }
    }

    @Override
    public List<Genre> getGenreFilmId(int id) {
        String sqlQuery = "SELECT g.* FROM film_genre AS fg JOIN genre AS g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id = ? ORDER BY g.genre_id";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> mapRow(rs), id);
    }

    private Genre mapRow(ResultSet rs) throws SQLException {
        return Genre.builder()
                .id(rs.getInt("genre_id"))
                .name(rs.getString("name"))
                .build();
    }
}
