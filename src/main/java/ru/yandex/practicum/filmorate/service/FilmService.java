package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    private final GenreDbStorage genreStorage;

    @Autowired
    public FilmService(@Qualifier("FilmDbStorage") FilmStorage filmStorage,
                       @Qualifier("UserDbStorage") UserStorage userStorage,
                       GenreDbStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
    }

    public Film createFilm(Film film) {
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public void addGenreToFilm(int filmId, int genreId) {
        filmStorage.addGenreToFilm(filmId, genreId);
    }

    public void deleteGenreFromFilm(int filmId, int genreId) {
        filmStorage.deleteGenreFromFilm(filmId, genreId);
    }

    public Film deleteFilm(Film film) {
        return filmStorage.deleteFilm(film);
    }

    public List<Film> getListFilms() {
        return filmStorage.getListFilms().stream()
                .peek(film -> genreStorage.getGenreFilmId(film.getId())
                        .forEach(film::addGenre))
                .collect(Collectors.toList());
    }

    public Film getFilmById(int id) {
        Film film = filmStorage.getFilmById(id);
        genreStorage.getGenreFilmId(film.getId())
                .forEach(film::addGenre);
        return film;
    }

    public Film addLike(int filmId, int userId) {
        return filmStorage.addLike(filmId, userId);
    }

    public Film deleteLike(int filmId, int userId) {
        return filmStorage.deleteLike(filmId, userId);
    }

    public List<Integer> getLikesByFilm(int filmId) {
        return filmStorage.getLikesByFilm(filmId);
    }

    public List<Film> getTopFilms(int count) {
        return filmStorage.getTopFilms(count).stream()
                .peek(film -> genreStorage.getGenreFilmId(film.getId())
                        .forEach(film::addGenre))
                .collect(Collectors.toList());
    }
}
