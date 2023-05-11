package ru.yandex.practicum.filmorate.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.*;

@Service("FilmDbService")
public class FilmDbService implements FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final GenreStorage genreStorage;

    @Autowired
    public FilmDbService(@Qualifier("FilmDbStorage") FilmStorage filmStorage,
                         @Qualifier("UserDbService") UserService userService,
                         GenreStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.genreStorage = genreStorage;
    }

    @Override
    public Film createFilm(Film film) {
        return filmStorage.createFilm(film);
    }

    @Override
    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    @Override
    public List<Film> getListFilms() {
        List<Film> films = filmStorage.getListFilms();
        Map<Film, List<Genre>> data = genreStorage.getGenreAllFilms();
        for (Film filmFromList : films) {
            if (data.containsKey(filmFromList)) {
                Set<Genre> genres = new HashSet<>();
                genres.addAll(data.get(filmFromList));
                filmFromList.setGenres(genres);
            }
        }
        return films;
    }

    @Override
    public Film getFilmById(int id) {
        Film film = filmStorage.getFilmById(id);
        Map<Film, List<Genre>> data = genreStorage.getGenreAllFilms();
        if (data.containsKey(film)) {
            Set<Genre> genres = new HashSet<>();
            genres.addAll(data.get(film));
            film.setGenres(genres);
        }
        return film;
    }

    @Override
    public Film addLike(int filmId, int userId) {
        return filmStorage.addLike(filmId, userId);
    }

    @Override
    public Film deleteLike(int filmId, int userId) {
        return filmStorage.deleteLike(filmId, userId);
    }

    @Override
    public List<Integer> getLikesByFilm(int filmId) {
        return filmStorage.getLikesByFilm(filmId);
    }

    @Override
    public List<Film> getTopFilms(int count) {
        List<Film> films = filmStorage.getTopFilms(count);
        Map<Film, List<Genre>> data = genreStorage.getGenreAllFilms();
        for (Film filmFromList : films) {
            if (data.containsKey(filmFromList)) {
                Set<Genre> genres = new HashSet<>();
                genres.addAll(data.get(filmFromList));
                filmFromList.setGenres(genres);
            }
        }
        return films;
    }

    @Override
    public void addGenreToFilm(int filmId, int genreId) {
        filmStorage.addGenreToFilm(filmId, genreId);
    }

    @Override
    public void deleteGenreFromFilm(int filmId) {
        filmStorage.deleteGenreFromFilm(filmId);
    }

    @Override
    public Film deleteFilm(Film film) {
        return filmStorage.deleteFilm(film);
    }
}
