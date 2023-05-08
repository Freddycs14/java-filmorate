package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreDbStorageTest {
    private final GenreDbStorage genreStorage;

    @Test
    public void shouldGetAllGenre() {
        List<Genre> genreList = genreStorage.getAllGenre();
        assertEquals(6, genreList.size(), "Список жанров не соответствует ожидаемому");
    }

    @Test
    public void shouldGetMpaById() {
        Genre genreComedy = genreStorage.getGenreById(1);
        assertEquals("Комедия", genreComedy.getName(), "Название жанра не соответствует ожидаемому");
    }
}
