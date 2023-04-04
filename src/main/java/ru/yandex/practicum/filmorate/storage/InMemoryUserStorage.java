package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @Override
    public User createUser(User user) {
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

    @Override
    public User updateUser(User user) {
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
            throw new UserNotFoundException("Пользователь не найден");
        }
        log.info("Обновление пользователя");
        return user;
    }

    @Override
    public User deleteUser(User user) {
        if (users.containsKey(user.getId())) {
            users.remove(user.getId());
            log.info("Пользователь удалён");
            return user;
        } else {
            throw new UserNotFoundException("Пользователя нет в списке");
        }
    }

    @Override
    public List<User> getListUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUserById(int id) {
        if (users.containsKey(id)) {
            return users.get(id);
        } else {
            throw new UserNotFoundException("Пользователя с id = " + id + " нет в списке");
        }
    }

    private void validateLogin(User user) {
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не должен содержать пробелы");
        }
    }

    public int getNextId() {
        return nextId++;
    }
}
