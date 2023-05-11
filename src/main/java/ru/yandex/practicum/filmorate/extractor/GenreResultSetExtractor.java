package ru.yandex.practicum.filmorate.extractor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Component
public class GenreResultSetExtractor implements ResultSetExtractor <Map<Film, List<Genre>>> {

    @Override
    public Map<Film, List<Genre>> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<Film, List<Genre>> data = new LinkedHashMap<>();
        while (rs.next()) {
            Film film = Film.builder()
                    .id(rs.getInt("film_id"))
                    .name(rs.getString("name"))
                    .description(rs.getString("description"))
                    .releaseDate(rs.getDate("release_date").toLocalDate())
                    .duration(rs.getInt("duration"))
                    .mpa(Mpa.builder()
                            .id(rs.getInt("mpa_id"))
                            .name(rs.getString("mpa_name"))
                            .build())
                    .build();
            if (data.containsKey(film)) {
                data.get(film).add(Genre.builder()
                                        .id(rs.getInt("genre_id"))
                                        .name(rs.getString("genre_name"))
                                        .build());
            } else {
                data.put(film, new ArrayList<>());
                if (rs.getInt("genre_id") != 0) {
                    data.get(film).add(Genre.builder()
                                            .id(rs.getInt("genre_id"))
                                            .name(rs.getString("genre_name"))
                                            .build());
                }
            }
        }
        return data;
    }
}
