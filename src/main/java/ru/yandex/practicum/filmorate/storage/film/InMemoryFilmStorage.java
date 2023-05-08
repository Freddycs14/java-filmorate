package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Integer.compare;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private static final LocalDate LIMIT_DATE = LocalDate.of(1895, Month.DECEMBER, 28);
    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    @Override
    public Film createFilm(Film film) {
        validateDate(film);
        film.setId(getNextId());
        //film.setLikes(new HashSet<>());
        films.put(film.getId(), film);
        log.info("Добавление фильма");
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        validateDate(film);
        int id = film.getId();
        if (films.containsKey(id)) {
            Film oldFilm = films.get(id);
            Set<Integer> likes = oldFilm.getLikes();
            //film.setLikes(likes);
            films.put(id, film);
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

    @Override
    public Film addLike(int filmId, int userId) {
        if (films.get(filmId).getLikes().contains(userId)) {
            throw new ValidationException("Пользователь с id: " + userId + " уже поставил лайк этому фильму");
        }
        putLike(filmId,userId);
        log.info("Пользователь с id: {} поставил лайк фильму с id: {}", userId, filmId);
        return films.get(filmId);
    }

    @Override
    public Film deleteLike(int filmId, int userId) {
        removeLike(filmId,userId);
        log.info("Пользователь с id: {} удалил лайк фильму с id: {}", userId, filmId);
        return films.get(filmId);
    }

    @Override
    public List<Film> getTopFilms(int count) {
        log.info("Выводим список популярных фильмов");
        List<Film> topFilms = getListFilms();
        return topFilms.stream()
                .sorted((o1, o2) -> {
                    int comp = compare(o1.getLikes().size(), o2.getLikes().size());
                    return -1 * comp;
                }).limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public void addGenreToFilm(int filmId, int genreId) {
        System.out.println("Заглушка");
    }

    @Override
    public void deleteGenreFromFilm(int filmId, int genreId) {
        System.out.println("Заглушка");
    }

    @Override
    public List<Integer> getLikesByFilm(int filmId) {
        System.out.println("Заглушка");
        List<Integer> likes = new ArrayList<>();
        return likes;
    }

    private int getNextId() {
        return nextId++;
    }

    private void validateDate(Film film) {
        if (film.getReleaseDate().isBefore(LIMIT_DATE)) {
            throw new ValidationException("Дата релиза не должна быть раньше 28 декабря 1895 года");
        }
    }

    private void putLike(int filmId, int userId) {
        films.get(filmId).getLikes().add(userId);
    }

    private void removeLike(int filmId, int userId) {
        films.get(filmId).getLikes().remove(userId);
    }
}
