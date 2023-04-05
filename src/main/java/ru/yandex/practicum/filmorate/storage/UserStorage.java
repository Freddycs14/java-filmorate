package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    User createUser(User user);

    User updateUser(User user);

    User deleteUser(User user);

    List<User> getListUsers();

    User getUserById(int id);

    User addFriend(int userId, int friendId);

    User deleteFriend(int userId, int friendId);

    List<User> getUserFriends(int userId);

    List<User> getListCommonFriends(int firstUserId, int secondUserId);
}
