package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTests extends UserController {

    UserController userController = new UserController();
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

    @BeforeEach
    public void beforeEach() {
        userController.getUsers().clear();
    }

    @Test
    public void getAllUsers() {
        userController.createUser(user1);
        userController.createUser(user2);
        assertEquals(2, userController.getListUsers().size(), "Неверное количество пользователей");
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
                () -> userController.createUser(user));
        assertEquals("Логин не должен содержать пробелы", exception.getMessage());
    }

    @Test
    public void shouldCreateUserWithoutName () {
        User user = User.builder()
                .email("user@gmail.com")
                .login("user")
                .name("  ")
                .birthday(LocalDate.of(1956, 7, 9))
                .build();
        userController.createUser(user);
        assertEquals(1, userController.getListUsers().size(),"Неверное количество пользователей");
        assertEquals(userController.getUsers().get(1).getName(), user.getLogin(), "Имя и логин не совпадают");
    }

    @Test
    public void shouldUpdateUser() {
        userController.createUser(user1);
        int id = user1.getId();
        user2.setId(id);
        userController.updateUser(user2);
        assertEquals(1, userController.getListUsers().size(), "Неверное количество пользователей");
        assertEquals(userController.getUsers().get(id), user2, "Пользователи не совпадают");
    }

    @Test
    public void shouldUpdateUserWithIncorrectId() {
        userController.createUser(user1);
        user2.setId(100);
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.updateUser(user2));
        assertEquals("Пользователь не найден", exception.getMessage());

    }

}
