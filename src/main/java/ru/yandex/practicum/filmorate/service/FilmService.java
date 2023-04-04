package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Integer.compare;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film createFilm(Film film) {
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public Film deleteFilm(Film film) {
        return filmStorage.deleteFilm(film);
    }

    public List<Film> getListFilms() {
        return filmStorage.getListFilms();
    }

    public Film getFilmById(int id) {
        return filmStorage.getFilmById(id);
    }

    public Film addLike(int filmId, int userId) {
        checkFilms(filmId);
        checkUsers(userId);
        Film film = filmStorage.getFilmById(filmId);
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        if (film.getLikes().contains(userId)) {
            throw new ValidationException("Пользователь с id: " + userId + " уже поставил лайк этому фильму");
        }
        film.getLikes().add(userId);
        log.info("Пользователь с id: {} поставил лайк фильму с id: {}", userId, filmId);
        return filmStorage.getFilmById(filmId);
    }

    public Film deleteLike(int filmId, int userId) {
        checkFilms(filmId);
        checkUsers(userId);
        Film film = filmStorage.getFilmById(filmId);
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        if (film.getLikes().contains(userId)) {
            film.getLikes().remove(userId);
        } else {
            throw new ValidationException("Пользователь с id: " + userId + " не ставил лайк этому фильму");
        }
        log.info("Пользователь с id: {} удалил лайк фильму с id: {}", userId, filmId);
        return filmStorage.getFilmById(filmId);
    }

    public List<Film> getTopFilms(int count) {
        log.info("Выводим список популярных фильмов");
        List<Film> topFilms = filmStorage.getListFilms();
        for (Film film : topFilms) {
            if (film.getLikes() == null) {
                film.setLikes(new HashSet<>());
            }
        }
        return topFilms.stream()
                .sorted((o1, o2) -> {
                    int comp = compare(o1.getLikes().size(), o2.getLikes().size());
                    return -1 * comp;
                }).limit(count)
                .collect(Collectors.toList());
    }

    public void checkUsers(int userId) {
        if (!userStorage.getListUsers().contains(userStorage.getUserById(userId))) {
            throw new UserNotFoundException("Пользователь c id: " + userId + "не найден");
        }
    }

    public void checkFilms(int filmId) {
        if (!filmStorage.getListFilms().contains(filmStorage.getFilmById(filmId))) {
            throw new FilmNotFoundException("Фильм c id: " + filmId + "не найден");
        }
    }
}
