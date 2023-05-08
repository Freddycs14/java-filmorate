package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmorateApplicationTests {

    private final GenreDbStorage genreStorage;
    private final UserService userService;
    private final FilmService filmService;
    private final JdbcTemplate jdbcTemplate;

    Film film1 = Film.builder()
            .name("The Green Mile")
            .description("description")
            .releaseDate(LocalDate.of(1999, 12, 6))
            .duration(189)
            .mpa(Mpa.builder()
                    .id(1).build())
            .build();

    Film film2 = Film.builder()
            .name("Schindler's List")
            .description("description")
            .releaseDate(LocalDate.of(1993, 11, 30))
            .duration(195)
            .mpa(Mpa.builder()
                    .id(2).build())
            .build();
    Film film3 = Film.builder()
            .name("Friends")
            .description("description")
            .releaseDate(LocalDate.of(1994, 11, 30))
            .duration(10000)
            .mpa(Mpa.builder()
                    .id(1).build())
            .build();

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
    User user3 = User.builder()
            .email("user3@gmail.com")
            .login("user3")
            .name("Leo")
            .birthday(LocalDate.of(1964, 8, 7))
            .build();

    @AfterEach
    public void cleanDb() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate,
                "users", "films", "friends", "film_genre", "film_likes");
        jdbcTemplate.update("ALTER TABLE USERS ALTER COLUMN user_id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE FILMS ALTER COLUMN film_id RESTART WITH 1");
    }

    @Test
    public void shouldCreateFilm() {
        Film addedFilm = filmService.createFilm(film1);
        assertThat(addedFilm)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 1);
    }

    @Test
    public void shouldUpdateFilm() {
        filmService.createFilm(film1);
        Film updateFilm = Film.builder().id(1).name("You Don't Mess with the Zohan").
                description("description").releaseDate(LocalDate.of(2008, 6, 6))
                .mpa(Mpa.builder().id(3).build()).build();
        Film updatedFilm = filmService.updateFilm(updateFilm);
        assertThat(updatedFilm)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "You Don't Mess with the Zohan");

        FilmNotFoundException e = assertThrows(
                FilmNotFoundException.class,
                () -> filmService.updateFilm(Film.builder().id(-1).name("You Don't Mess with the Zohan").
                        description("description").releaseDate(LocalDate.of(2008, 6, 6))
                        .mpa(Mpa.builder().id(3).build()).build())
        );
        assertEquals("Фильм с id -1 не найден", e.getMessage());

        e = assertThrows(
                FilmNotFoundException.class,
                () -> filmService.updateFilm(Film.builder().id(999).name("You Don't Mess with the Zohan").
                        description("description").releaseDate(LocalDate.of(2008, 6, 6))
                        .mpa(Mpa.builder().id(3).build()).build())
        );
        assertEquals("Фильм с id 999 не найден", e.getMessage());
    }

    @Test
    public void shouldDeleteFilm() {
        filmService.createFilm(film1);
        filmService.deleteFilm(filmService.getFilmById(1));
        List<Film> films = filmService.getListFilms();
        assertThat(films)
                .isNotNull()
                .isEqualTo(Collections.emptyList());
    }

    @Test
    public void shouldGetListFilms() {
        List<Film> films = filmService.getListFilms();
        assertThat(films)
                .isNotNull()
                .isEqualTo(Collections.emptyList());
        filmService.createFilm(film1);
        films = filmService.getListFilms();
        assertNotNull(films, "Cписок фильма не пустой");
        assertEquals(films.size(), 1, "Количество фильмов в списке не верное");
        assertEquals(films.get(0).getId(), 1, "Значение id фильма в списке не совпадает");
    }

    @Test
    public void shouldGetFilmById() {
        Film addedFilm = filmService.createFilm(film1);
        Film foundFilm = filmService.getFilmById(addedFilm.getId());
        assertThat(foundFilm)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "The Green Mile");

        FilmNotFoundException e = assertThrows(FilmNotFoundException.class, () -> filmService.getFilmById(-1));
        assertEquals("Фильм с id -1 не найден", e.getMessage());

        e = assertThrows(FilmNotFoundException.class, () -> filmService.getFilmById(999));
        assertEquals("Фильм с id 999 не найден", e.getMessage());
    }

    @Test
    public void testAddGenreToFilm() {
        filmService.createFilm(film1);
        filmService.addGenreToFilm(1, 1);

        List<Genre> genres = genreStorage.getGenreFilmId(1);
        assertNotNull(genres, "Список жанров фильма не пустой");
        assertEquals(genres.size(), 1, "Количество жанров в фильме не верное");
        assertEquals(genres.get(0).getId(), 1, "Значение id жанра в списке не совпадает");
    }

    @Test
    public void testDeleteGenreFromFilm() {
        filmService.createFilm(film1);
        filmService.addGenreToFilm(1, 1);
        List<Genre> genres = genreStorage.getGenreFilmId(1);
        assertNotNull(genres, "Список жанров фильма не пустой");
        assertEquals(genres.size(), 1, "Количество жанров в фильме не верное");
        assertEquals(genres.get(0).getId(), 1, "Значение id жанра в списке не совпадает");
        filmService.deleteGenreFromFilm(1, 1);
        genres = genreStorage.getGenreFilmId(1);
        assertThat(genres)
                .isNotNull()
                .isEqualTo(Collections.emptyList());
    }

    @Test
    public void shouldCreateUser() {
        User addedUser = userService.createUser(user1);
        assertThat(addedUser)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 1);
    }

    @Test
    public void shouldUpdateUser() {
        userService.createUser(user1);
        User newUser = User.builder().id(1).email("user2@gmail.com").login("user2").name("Adam")
                .birthday(LocalDate.of(1952, 8, 7)).build();
        User updatedUser = userService.updateUser(newUser);
        assertThat(updatedUser)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "Adam");

        UserNotFoundException e = assertThrows(
                UserNotFoundException.class,
                () -> userService.updateUser(User.builder().id(-1).email("user2@gmail.com").login("user2").name("Adam")
                        .birthday(LocalDate.of(1952, 8, 7)).build())
        );
        assertEquals("Пользователь не найден", e.getMessage());

        e = assertThrows(
                UserNotFoundException.class,
                () -> userService.updateUser(User.builder().id(999).email("user2@gmail.com").login("user2").name("Adam")
                        .birthday(LocalDate.of(1952, 8, 7)).build())
        );
        assertEquals("Пользователь не найден", e.getMessage());
    }

    @Test
    public void shouldDeleteUser() {
        userService.createUser(user1);
        userService.deleteUser(userService.getUserById(1));
        List<User> users = userService.getListUsers();
        assertThat(users)
                .isNotNull()
                .isEqualTo(Collections.emptyList());
    }

    @Test
    public void shouldGetListUsers() {
        List<User> users = userService.getListUsers();
        assertThat(users)
                .isNotNull()
                .isEqualTo(Collections.emptyList());
        userService.createUser(user1);
        users = userService.getListUsers();
        assertNotNull(users, "Cписок пользователей не пустой");
        assertEquals(users.size(), 1, "Количество пользователей в списке не верное");
        assertEquals(users.get(0).getId(), 1, "Значение id пользователя в списке не совпадает");
    }

    @Test
    public void shouldGetUserById() {
        User addedUser = userService.createUser(user1);
        User foundUser = userService.getUserById(addedUser.getId());
        assertThat(foundUser)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "Tom");
        UserNotFoundException e = assertThrows(UserNotFoundException.class, () -> userService.getUserById(-1));
        assertEquals("Пользователь с id -1 не найден", e.getMessage());

        e = assertThrows(UserNotFoundException.class, () -> userService.getUserById(999));
        assertEquals("Пользователь с id 999 не найден", e.getMessage());
    }

    @Test
    public void testAddFriend() {
        userService.createUser(user1);
        User friend = userService.createUser(user2);
        userService.addFriend(1, 2);
        List<User> friends = userService.getUserFriends(1);
        assertNotNull(friends, "Список друзей пустой");
        assertEquals(friends.size(), 1, "Количество друзей в списке не верное");
        assertEquals(friends.get(0), friend, "Пользователи в списках не совпадают");
    }

    @Test
    public void testDeleteFriend() {
        userService.createUser(user1);
        User friend = userService.createUser(user2);
        userService.addFriend(1, 2);
        List<User> friends = userService.getUserFriends(1);
        assertNotNull(friends, "Список друзей пустой");
        assertEquals(friends.size(), 1, "Количество друзей в списке не верное");
        assertEquals(friends.get(0), friend, "Пользователи в списках не совпадают");

        userService.deleteFriend(1, 2);
        friends = userService.getUserFriends(1);
        assertThat(friends)
                .isNotNull()
                .isEqualTo(Collections.emptyList());
    }

    @Test
    public void shouldGetUserFriends() {
        userService.createUser(user1);
        User friend = userService.createUser(user2);
        List<User> friends = userService.getUserFriends(1);
        assertThat(friends)
                .isNotNull()
                .isEqualTo(Collections.emptyList());
        userService.addFriend(1, 2);
        friends = userService.getUserFriends(1);
        assertNotNull(friends, "Список друзей пустой");
        assertEquals(friends.size(), 1, "Количество друзей в списке не верное");
        assertEquals(friends.get(0), friend, "Пользователи в списках не совпадают");
    }

    @Test
    public void shouldGetListCommonFriends() {
        userService.createUser(user1);
        userService.createUser(user2);
        User commonFriend = userService.createUser(user3);
        List<User> commonFriends = userService.getListCommonFriends(1, 2);
        assertThat(commonFriends)
                .isNotNull()
                .isEqualTo(Collections.emptyList());
        userService.addFriend(1, 2);
        userService.addFriend(1, 3);
        userService.addFriend(2, 1);
        userService.addFriend(2, 3);
        commonFriends = userService.getListCommonFriends(1, 2);
        assertNotNull(commonFriends, "Список друзей пустой");
        assertEquals(commonFriends.size(), 1, "Количество общих друзей в списке не верное");
        assertEquals(commonFriends.get(0), commonFriend, "Пользователи в списках не совпадают");
    }

    @Test
    public void shouldAddLike() {
        filmService.createFilm(film1);
        userService.createUser(user1);
        filmService.addLike(1, 1);
        List<Integer> likes = filmService.getLikesByFilm(1);
        assertNotNull(likes, "Список лайков пустой");
        assertEquals(likes.size(), 1, "Количество лайков в списке не верное");
        assertEquals(likes.get(0), 1, "Лайки от пользователей не совпадают");
    }

    @Test
    public void shouldDeleteLike() {
        filmService.createFilm(film1);
        userService.createUser(user1);
        filmService.addLike(1, 1);
        List<Integer> likes = filmService.getLikesByFilm(1);
        assertNotNull(likes, "Список лайков пустой");
        assertEquals(likes.size(), 1, "Количество лайков в списке не верное");
        assertEquals(likes.get(0), 1, "Лайки от пользователей не совпадают");
        filmService.deleteLike(1, 1);
        likes = filmService.getLikesByFilm(1);
        assertThat(likes)
                .isNotNull()
                .isEqualTo(Collections.emptyList());
    }

    @Test
    public void shouldGetLikesByFilm() {
        filmService.createFilm(film1);
        List<Integer> likes = filmService.getLikesByFilm(1);
        assertThat(likes)
                .isNotNull()
                .isEqualTo(Collections.emptyList());
    }

    @Test
    public void shouldGetTopFilms() {
        Film firstFilm = filmService.createFilm(film1);
        Film secondFilm = filmService.createFilm(film2);
        Film thirdFilm = filmService.createFilm(film3);
        userService.createUser(user1);
        userService.createUser(user2);
        filmService.addLike(2, 1);
        filmService.addLike(2, 2);
        filmService.addLike(1, 1);
        List<Film> topFilms = filmService.getTopFilms(3);
        assertNotNull(topFilms, "Список популярных фильмов пустой");
        assertEquals(topFilms.size(), 3, "Количество фильмов в списке не верное");
        assertEquals(topFilms.get(0).getName(), secondFilm.getName(), "Фильмы не совпадают");
        assertEquals(topFilms.get(1).getName(), firstFilm.getName(), "Фильмы не совпадают");
        assertEquals(topFilms.get(2).getName(), thirdFilm.getName(), "Фильмы не совпадают");
    }


}
