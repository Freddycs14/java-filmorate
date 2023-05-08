package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface GenreStorage {
    List<Genre> getAllGenre();

    Genre getGenreById(int id);

    List<Genre> getGenreFilmId(int id);
}
