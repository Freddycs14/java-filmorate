package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

public class FilmControllerTests {
    InMemoryFilmStorage filmStorage = new InMemoryFilmStorage();
    InMemoryUserStorage userStorage = new InMemoryUserStorage();
    FilmService filmService = new FilmService(filmStorage, userStorage);
    Film film1 = Film.builder()
            .name("The Green Mile")
            .description("description")
            .releaseDate(LocalDate.of(1999, 12, 6))
            .duration(189)
            .build();
    Film film2 = Film.builder()
            .name("Schindler's List")
            .description("description")
            .releaseDate(LocalDate.of(1993, 11, 30))
            .duration(195)
            .build();

    @Test
    public void getAllFilms() {
        filmService.createFilm(film1);
        filmService.createFilm(film2);
        assertEquals(2, filmService.getListFilms().size(), "Неверное количество фильмов в списке");
    }

    @Test
    public void shouldCreateFilmWithIncorrectDate() {
        Film film = Film.builder()
                .name("The Green Mile")
                .description("description")
                .releaseDate(LocalDate.of(1700, 12, 6))
                .duration(189)
                .build();
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmService.createFilm(film));
        assertEquals("Дата релиза не должна быть раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    public void shouldUpdateFilm() {
        filmService.createFilm(film1);
        int id = film1.getId();
        film2.setId(id);
        filmService.updateFilm(film2);
        assertEquals(1, filmService.getListFilms().size(), "Неверное количество фильмов в списке");
        assertEquals(filmService.getFilmById(id), film2, "Фильмы не совпадают");
    }

    @Test
    public void shouldUpdateFilmWithIncorrectId() {
        filmService.createFilm(film1);
        film2.setId(100);
        FilmNotFoundException exception = assertThrows(
                FilmNotFoundException.class,
                () -> filmService.updateFilm(film2));
        assertEquals("Фильма нет в списке", exception.getMessage());
    }
}
