package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import static ru.yandex.practicum.filmorate.constants.Constant.*;

import java.sql.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository("UserDbStorage")
@Component
@RequiredArgsConstructor
@Slf4j
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public User createUser(User user) {
        validateLogin(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);
        user.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return user;

    }

    @Override
    public User updateUser(User user) {
        validateLogin(user);
        if (jdbcTemplate.update(UPDATE_USER, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId()) > 0) {
            return user;
        }
        log.warn("Пользователь с id {} не найден", user.getId());
        throw new UserNotFoundException("Пользователь не найден");
    }

    @Override
    public User deleteUser(User user) {
        jdbcTemplate.update(DELETE_USER, user.getId());
        log.info("Пользователь удалён");
        return user;
    }

    @Override
    public List<User> getListUsers() {
        return jdbcTemplate.query(GET_LIST_USERS, (rs, rowNum) -> mapRow(rs));
    }

    @Override
    public User getUserById(int id) {
        try {
            return jdbcTemplate.queryForObject(GET_USER_BY_ID, (rs, rowNum) -> mapRow(rs), id);
        } catch (DataRetrievalFailureException e) {
            log.warn("Пользователь с id {} не найден", id);
            throw new UserNotFoundException(String.format("Пользователь с id %d не найден", id));
        }
    }

    @Override
    public User addFriend(int userId, int friendId) {
        validationFriend(userId, friendId);
        if (getUserById(userId) != null && getUserById(friendId) != null) {
            jdbcTemplate.update(INSERT_FRIENDS, userId, friendId, true);
            log.info("Пользователь {} оставил заявку в друзья пользователю {}", userId, friendId);
            return getUserById(userId);
        } else {
            throw new UserNotFoundException("Пользователь с таким идентификатором не найден");
        }
    }

    @Override
    public User deleteFriend(int userId, int friendId) {
        jdbcTemplate.update(DELETE_FRIENDS, userId, friendId);
        log.info("Пользователь {} удалил заявку в друзья пользователя {}", userId, friendId);
        return getUserById(userId);
    }

    @Override
    public List<User> getUserFriends(int userId) {
        List<Integer> friendId = jdbcTemplate.queryForList(GET_USER_FRIENDS, Integer.class, userId);
        return friendId.stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getListCommonFriends(int firstUserId, int secondUserId) {
        return getUserFriends(firstUserId).stream()
                .filter(getUserFriends(secondUserId)::contains)
                .collect(Collectors.toList());
    }

    private void validateLogin(User user) {
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не должен содержать пробелы");
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return User.builder()
                .id(rs.getInt("user_id"))
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .build();
    }

    private void validationFriend(int userId, int friendId) {
        if (userId <= 0 || friendId <= 0) {
            log.error("Передан неверный id пользователя. Запрос не отправлен");
            throw new UserNotFoundException("Пользователь не найден");
        }
    }
}
