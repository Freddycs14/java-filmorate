package ru.yandex.practicum.filmorate.controller;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.Film;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private static final LocalDate LIMIT_DATE = LocalDate.of(1895, Month.DECEMBER, 28);
    private int nextId = 1;

    @GetMapping("/films")
    public List<Film> getListFilms() {
        return new ArrayList<Film>(films.values());
    }

    @PostMapping("/films")
    public Film createFilm(@Valid @RequestBody Film film) {
        validateDate(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Добавление фильма");
        return film;
    }

    @PutMapping("/films")
    public Film updateFilm(@Valid @RequestBody Film film) {
        validateDate(film);
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
        } else {
            throw new ValidationException("Фильма нет в списке");
        }
        log.info("Обновление фильма");
        return film;
    }

    public int getNextId() {
        return nextId++;
    }

    private void validateDate(Film film) {
        if (film.getReleaseDate().isBefore(LIMIT_DATE)) {
            throw new ValidationException("Дата релиза не должна быть раньше 28 декабря 1895 года");
        }
    }

    public Map<Integer, Film> getFilms() {
        return films;
    }
}
