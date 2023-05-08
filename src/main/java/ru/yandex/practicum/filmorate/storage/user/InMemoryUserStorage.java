package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

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
        if (users.containsKey(id)) {
            User oldUser = users.get(id);
            Set<Integer> friends = oldUser.getFriends();
            if (user.getName() == null || user.getName().isBlank()) {
                user = User.builder()
                        .email(user.getEmail())
                        .login(user.getLogin())
                        .name(user.getLogin())
                        .birthday(user.getBirthday())
                        .build();
            }
            user.setId(id);
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

    @Override
    public User addFriend(int userId, int friendId) {
        checkUsers(userId);
        checkUsers(friendId);
        if (users.get(userId).getFriends().contains(friendId)) {
            throw new ValidationException("Нельзя добавить пользователя в друзья дважды");
        }
        addToFriends(userId, friendId);
        if (users.get(friendId).getFriends().contains(userId)) {
            throw new ValidationException("Нельзя добавить пользователя в друзья дважды");
        }
        addToFriends(friendId, userId);
        log.info("Пользователь с id: {} добавил в друзья пользователя с id: {}", userId, friendId);
        return users.get(userId);
    }

    @Override
    public User deleteFriend(int userId, int friendId) {
        checkUsers(userId);
        checkUsers(friendId);
        removeFromFriends(userId, friendId);

        removeFromFriends(friendId, userId);
        log.info("Пользователь с id: {} удалён из списка друзей пользователя с id: {}", friendId, userId);
        return users.get(userId);
    }

    @Override
    public List<User> getUserFriends(int userId) {
        checkUsers(userId);
        Set<Integer> userFriends = users.get(userId).getFriends();
        return userFriends.stream()
                .map(users::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getListCommonFriends(int firstUserId, int secondUserId) {
        checkUsers(firstUserId);
        checkUsers(secondUserId);
        Set<Integer> firstUserFriends = users.get(firstUserId).getFriends();
        Set<Integer> secondUserFriends = users.get(secondUserId).getFriends();
        return firstUserFriends.stream()
                .filter(secondUserFriends::contains)
                .map(users::get)
                .collect(Collectors.toList());
    }

    private void checkUsers(int userId) {
        if (!users.containsKey(userId)) {
            throw new UserNotFoundException("Пользователь c id: " + userId + "не найден");
        }
    }

    private void validateLogin(User user) {
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не должен содержать пробелы");
        }
    }

    private int getNextId() {
        return nextId++;
    }

    private void addToFriends(int userId, int friendId) {
        users.get(userId).getFriends().add(friendId);
    }

    private void removeFromFriends(int userId, int friendId) {
        users.get(userId).getFriends().remove(friendId);
    }
}
