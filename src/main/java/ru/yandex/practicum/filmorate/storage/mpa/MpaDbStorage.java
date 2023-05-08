package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.MpaNotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Mpa addMpa(Mpa mpa) {
        String sqlQuery = "INSERT INTO mpa VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery, mpa.getId(), mpa.getName());
        log.info("Рейтинг {} добавлен в таблицу", mpa);
        return mpa;
    }

    @Override
    public List<Mpa> getAllMpa() {
        String sqlQuery = "SELECT * FROM mpa";
        List<Mpa> mpa = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> mapRow(rs));
        log.info("Список рейтингов получен");
        if (mpa.isEmpty()) {
            log.info("Список рейтингов пустой");
        }
        return mpa;
    }

    @Override
    public Mpa getMpaById(int id) {
        String sqlQuery = "SELECT * FROM mpa WHERE mpa_id = ?";
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (mpaRows.next()) {
            return jdbcTemplate.queryForObject(sqlQuery, (rs, rowNum) -> mapRow(rs), id);
        } else {
            log.warn("Рейтинг с идентификатором {} не найден", id);
            throw new MpaNotFoundException("Рейтинга с id = " + id + " нет в списке");
        }
    }

    private Mpa mapRow(ResultSet rs) throws SQLException {
        return Mpa.builder()
                .id(rs.getInt("mpa_id"))
                .name(rs.getString("name"))
                .build();
    }
}
