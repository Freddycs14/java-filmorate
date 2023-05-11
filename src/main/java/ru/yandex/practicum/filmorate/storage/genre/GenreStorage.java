package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Map;

public interface GenreStorage {
    List<Genre> getAllGenre();

    Genre getGenreById(int id);

    Map<Film, List<Genre>> getGenreAllFilms();

    List<Genre> getGenreFilmId(int id);
}
