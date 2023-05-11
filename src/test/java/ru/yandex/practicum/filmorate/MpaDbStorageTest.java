package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MpaDbStorageTest {
    private final MpaDbStorage mpaStorage;

    @Test
    public void shouldGetAllMpa() {
        List<Mpa> mpaList = mpaStorage.getAllMpa();
        assertEquals(5, mpaList.size(), "Список рейтингов не соответствует ожидаемому");
    }

    @Test
    public void shouldAddMpa() {
        List<Mpa> mpaLists = mpaStorage.getAllMpa();
        assertEquals(5, mpaLists.size(), "Список рейтингов не соответствует ожидаемому");
        Mpa newMpa = Mpa.builder().name("NC-30").build();
        mpaStorage.addMpa(newMpa);
        mpaLists = mpaStorage.getAllMpa();
        assertEquals(6, mpaLists.size(), "Список рейтингов не соответствует ожидаемому");
    }

    @Test
    public void shouldGetMpaById() {
        Mpa mpaG = mpaStorage.getMpaById(1);
        assertEquals("G", mpaG.getName(), "Название рейтинга не соответствует ожидаемому");
    }
}
