package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.MpaNotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import static ru.yandex.practicum.filmorate.constants.Constant.*;

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
        jdbcTemplate.update(INSERT_MPA, mpa.getId(), mpa.getName());
        log.info("Рейтинг {} добавлен в таблицу", mpa);
        return mpa;
    }

    @Override
    public List<Mpa> getAllMpa() {
        List<Mpa> mpa = jdbcTemplate.query(GET_ALL_MPA, (rs, rowNum) -> mapRow(rs));
        log.info("Список рейтингов получен");
        if (mpa.isEmpty()) {
            log.info("Список рейтингов пустой");
        }
        return mpa;
    }

    @Override
    public Mpa getMpaById(int id) {
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet(GET_MPA_BY_ID, id);
        if (mpaRows.next()) {
            return jdbcTemplate.queryForObject(GET_MPA_BY_ID, (rs, rowNum) -> mapRow(rs), id);
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
