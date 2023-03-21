package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

public class FilmControllerTests extends FilmController {
    FilmController filmController = new FilmController();
    Film film1 = Film.builder()
            .name("The Green Mile")
            .description("description")
            .releaseDate(LocalDate.of(1999,12,6))
            .duration(189)
            .build();
    Film film2 = Film.builder()
            .name("Schindler's List")
            .description("description")
            .releaseDate(LocalDate.of(1993,11,30))
            .duration(195)
            .build();

    @BeforeEach
    public void beforeEach() {
        filmController.getFilms().clear();
    }

    @Test
    public void getAllFilms() {
        filmController.createFilm(film1);
        filmController.createFilm(film2);
        assertEquals(2, filmController.getListFilms().size(), "Неверное количество фильмов в списке");
    }

    @Test
    public void shouldCreateFilmWithIncorrectDate() {
        Film film = Film.builder()
                .name("The Green Mile")
                .description("description")
                .releaseDate(LocalDate.of(1700,12,6))
                .duration(189)
                .build();
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.createFilm(film));
        assertEquals("Дата релиза не должна быть раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    public void shouldUpdateFilm() {
        filmController.createFilm(film1);
        int id = film1.getId();
        film2.setId(id);
        filmController.updateFilm(film2);
        assertEquals(1, filmController.getListFilms().size(), "Неверное количество фильмов в списке");
        assertEquals(filmController.getFilms().get(id), film2, "Фильмы не совпадают");
    }

    @Test
    public void shouldUpdateFilmWithIncorrectId() {
        filmController.createFilm(film1);
        film2.setId(100);
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.updateFilm(film2));
        assertEquals("Фильма нет в списке", exception.getMessage());
    }



}
