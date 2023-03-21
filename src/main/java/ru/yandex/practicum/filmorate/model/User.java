package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;

@Data
public class User {
    private int id;
    @Email
    @NotBlank
    private final String email;
    @NotBlank
    private final String login;
    private final String name;
    @PastOrPresent
    @NotNull
    private final LocalDate birthday;
}
