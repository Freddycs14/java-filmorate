package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTests {
    InMemoryUserStorage userStorage = new InMemoryUserStorage();
    UserService userService = new UserService(userStorage);

    User user1 = User.builder()
            .email("user1@gmail.com")
            .login("user1")
            .name("Tom")
            .birthday(LocalDate.of(1956, 7, 9))
            .build();
    User user2 = User.builder()
            .email("user2@gmail.com")
            .login("user2")
            .name("Liam")
            .birthday(LocalDate.of(1952, 8, 7))
            .build();

    @Test
    public void getAllUsers() {
        userService.createUser(user1);
        userService.createUser(user2);
        assertEquals(2, userService.getListUsers().size(), "Неверное количество пользователей");
    }

    @Test
    public void shouldCreateUserWhoseLoginHasSpaces() {
        User user = User.builder()
                .email("user@gmail.com")
                .login("new user")
                .name("Tom")
                .birthday(LocalDate.of(1956, 7, 9))
                .build();
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.createUser(user));
        assertEquals("Логин не должен содержать пробелы", exception.getMessage());
    }

    @Test
    public void shouldCreateUserWithoutName() {
        User user = User.builder()
                .email("user@gmail.com")
                .login("user")
                .name("  ")
                .birthday(LocalDate.of(1956, 7, 9))
                .build();
        userService.createUser(user);
        assertEquals(1, userService.getListUsers().size(), "Неверное количество пользователей");
        assertEquals(userService.getUserById(1).getName(), user.getLogin(), "Имя и логин не совпадают");
    }

    @Test
    public void shouldUpdateUser() {
        userService.createUser(user1);
        int id = user1.getId();
        user2.setId(id);
        userService.updateUser(user2);
        assertEquals(1, userService.getListUsers().size(), "Неверное количество пользователей");
        assertEquals(userService.getUserById(id), user2, "Пользователи не совпадают");
    }

    @Test
    public void shouldUpdateUserWithIncorrectId() {
        userService.createUser(user1);
        user2.setId(100);
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.updateUser(user2));
        assertEquals("Пользователь не найден", exception.getMessage());
    }
}
