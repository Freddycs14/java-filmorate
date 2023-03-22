package ru.yandex.practicum.filmorate.controller;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.User;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @GetMapping("/users")
    public List<User> getListUsers() {
        return new ArrayList<User>(users.values());
    }

    @PostMapping("/users")
    public User createUser(@Valid @RequestBody User user) {
        validateLogin(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user = User.builder()
                    .email(user.getEmail())
                    .login(user.getLogin())
                    .name(user.getLogin())
                    .birthday(user.getBirthday())
                    .build();
        }
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Добавление пользователя");
        return user;
    }

    @PutMapping("/users")
    public User updateUser(@Valid @RequestBody User user) {
        validateLogin(user);
        int id = user.getId();
        if (user.getName() == null || user.getName().isBlank()) {
            user = User.builder()
                    .email(user.getEmail())
                    .login(user.getLogin())
                    .name(user.getLogin())
                    .birthday(user.getBirthday())
                    .build();
        }
        user.setId(id);
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
        } else {
            throw new ValidationException("Пользователь не найден");
        }
        log.info("Обновление пользователя");
        return user;
    }

    private void validateLogin(User user) {
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не должен содержать пробелы");
        }
    }

    public int getNextId() {
        return nextId++;
    }

    public Map<Integer, User> getUsers() {
        return users;
    }
}
