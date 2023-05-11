package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

public interface FilmService {
    Film createFilm(Film film);

    Film updateFilm(Film film);

    void addGenreToFilm(int filmId, int genreId);

    void deleteGenreFromFilm(int filmId);

    Film deleteFilm(Film film);

    List<Film> getListFilms();

    Film getFilmById(int id);

    Film addLike(int filmId, int userId);

    Film deleteLike(int filmId, int userId);

    List<Integer> getLikesByFilm(int filmId);

    List<Film> getTopFilms(int count);
}
