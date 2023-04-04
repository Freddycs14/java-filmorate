package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private static final LocalDate LIMIT_DATE = LocalDate.of(1895, Month.DECEMBER, 28);
    private int nextId = 1;

    @Override
    public Film createFilm(Film film) {
        validateDate(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Добавление фильма");
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        validateDate(film);
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
        } else {
            throw new FilmNotFoundException("Фильма нет в списке");
        }
        log.info("Обновление фильма");
        return film;
    }

    @Override
    public Film deleteFilm(Film film) {
        if (films.containsKey(film.getId())) {
            films.remove(film.getId());
            log.info("Фильм удалён");
            return film;
        } else {
            throw new FilmNotFoundException("Фильма нет в списке");
        }
    }

    @Override
    public List<Film> getListFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getFilmById(int id) {
        if (films.containsKey(id)) {
            return films.get(id);
        } else {
            throw new FilmNotFoundException("Фильма с id = " + id + " нет в списке");
        }
    }

    public int getNextId() {
        return nextId++;
    }

    private void validateDate(Film film) {
        if (film.getReleaseDate().isBefore(LIMIT_DATE)) {
            throw new ValidationException("Дата релиза не должна быть раньше 28 декабря 1895 года");
        }
    }
}
