package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User createUser(User user) {
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public User deleteUser(User user) {
        return userStorage.deleteUser(user);
    }

    public List<User> getListUsers() {
        return userStorage.getListUsers();
    }

    public User getUserById(int id) {
        return userStorage.getUserById(id);
    }

    public User addFriend(int userId, int friendId) {
        checkUsers(userId);
        checkUsers(friendId);
        User user = userStorage.getUserById(userId);
        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
        if (user.getFriends().contains(friendId)) {
            throw new ValidationException("Нельзя добавить пользователя в друзья дважды");
        }
        user.getFriends().add(friendId);
        User friend = userStorage.getUserById(friendId);
        if (friend.getFriends() == null) {
            friend.setFriends(new HashSet<>());
        }
        if (friend.getFriends().contains(userId)) {
            throw new ValidationException("Нельзя добавить пользователя в друзья дважды");
        }
        friend.getFriends().add(userId);
        log.info("Пользователь с id: {} добавил в друзья пользователя с id: {}", userId, friendId);
        return userStorage.getUserById(userId);
    }

    public User deleteFriend(int userId, int friendId) {
        checkUsers(userId);
        checkUsers(friendId);
        User user = userStorage.getUserById(userId);
        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
        if (user.getFriends().contains(friendId)) {
            user.getFriends().remove(friendId);
        } else {
            throw new ValidationException("Пользователей нет с вписках друзей у друг друга");
        }

        User friend = userStorage.getUserById(friendId);
        if (friend.getFriends() == null) {
            friend.setFriends(new HashSet<>());
        }
        if (friend.getFriends().contains(userId)) {
            friend.getFriends().remove(userId);
        } else {
            throw new ValidationException("Пользователей нет в списке друзей у друг друга");
        }
        log.info("Пользователь с id: {} удалён из списка друзей пользователя с id: {}", friendId, userId);
        return userStorage.getUserById(userId);
    }

    public List<User> getUserFriends(int userId) {
        checkUsers(userId);
        Set<Integer> userFriends = userStorage.getUserById(userId).getFriends();
        if (userFriends == null) {
            return new ArrayList<>();
        }
        return userFriends.stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> getListCommonFriends(int firstUserId, int secondUserId) {
        checkUsers(firstUserId);
        checkUsers(secondUserId);
        Set<Integer> firstUserFriends = userStorage.getUserById(firstUserId).getFriends();
        if (firstUserFriends == null) {
            return new ArrayList<>();
        }
        Set<Integer> secondUserFriends = userStorage.getUserById(secondUserId).getFriends();
        if (secondUserFriends == null) {
            return new ArrayList<>();
        }
        return firstUserFriends.stream()
                .filter(secondUserFriends::contains)
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public void checkUsers(int userId) {
        if (!userStorage.getListUsers().contains(userStorage.getUserById(userId))) {
            throw new UserNotFoundException("Пользователь c id: " + userId + "не найден");
        }
    }

}
