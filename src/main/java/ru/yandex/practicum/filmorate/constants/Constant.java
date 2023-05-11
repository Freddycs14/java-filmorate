package ru.yandex.practicum.filmorate.constants;

import java.time.LocalDate;
import java.time.Month;

public final class Constant {
    public static final LocalDate LIMIT_DATE = LocalDate.of(1895, Month.DECEMBER, 28);
    public static final String INSERT_FILM = "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    public static final String UPDATE_FILM = "UPDATE films " +
            "SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";
    public static final String DELETE_FILM = "DELETE FROM films WHERE film_id = ?";
    public static final String GET_LIST_FILMS = "SELECT f.film_id, f.name, f.description, f.release_date, " +
            "f.duration, f.mpa_id, m.name AS mpa_name FROM films AS f JOIN mpa AS m ON f.mpa_id = m.mpa_id";
    public static final String GET_FILM_BY_ID = "SELECT f.film_id, f.name, f.description, f.release_date, " +
            "f.duration, f.mpa_id, m.name AS mpa_name FROM films AS f JOIN mpa AS m ON f.mpa_id = m.mpa_id " +
            "WHERE f.film_id = ?";
    public static final String INSERT_FILM_GENRE = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
    public static final String DELETE_FILM_GENRE_FROM_FILM = "DELETE FROM film_genre WHERE film_id = ?";
    public static final String INSERT_FILM_LIKES = "INSERT INTO film_likes (user_id, film_id) VALUES (?, ?)";
    public static final String DELETE_FILM_LIKES = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
    public static final String GET_LIKES_BY_FILM = "SELECT user_id FROM film_likes WHERE film_id = ?";
    public static final String GET_TOP_FILMS = "SELECT f.film_id, f.name, f.description, f.release_date, " +
            "f.duration, f.mpa_id, m.name AS mpa_name FROM films AS f JOIN mpa AS m ON f.mpa_id = m.mpa_id " +
            "LEFT JOIN (SELECT film_id, COUNT(user_id) AS likes_count FROM film_likes GROUP BY film_id " +
            "ORDER BY likes_count) AS popular ON f.film_id = popular.film_id ORDER BY popular.likes_count DESC limit ?";
    public static final String INSERT_USER = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    public static final String UPDATE_USER = "UPDATE users SET email=?, login=?, name=?, birthday=? WHERE user_id=?";
    public static final String DELETE_USER = "DELETE FROM users WHERE user_id = ?";
    public static final String GET_LIST_USERS = "SELECT user_id, email, login, name, birthday FROM users ORDER BY user_id";
    public static final String GET_USER_BY_ID = "SELECT user_id, email, login, name, birthday FROM users where user_id = ?";
    public static final String INSERT_FRIENDS = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, ?)";
    public static final String DELETE_FRIENDS = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
    public static final String GET_USER_FRIENDS = "SELECT friend_id FROM friends WHERE user_id = ?";
    public static final String INSERT_MPA = "INSERT INTO mpa VALUES (?, ?)";
    public static final String GET_ALL_MPA = "SELECT mpa_id, name FROM mpa";
    public static final String GET_MPA_BY_ID = "SELECT mpa_id, name FROM mpa WHERE mpa_id = ?";
    public static final String GET_ALL_GENRE = "SELECT genre_id, name FROM genre ORDER BY genre_id";
    public static final String GET_GENRE_BY_ID = "SELECT genre_id, name FROM genre WHERE genre_id = ?";
    public static final String GET_GENRE_FROM_FILM = "SELECT g.genre_id, g.name FROM film_genre AS fg " +
            "JOIN genre AS g ON fg.genre_id = g.genre_id WHERE fg.film_id = ? ORDER BY g.genre_id";
    public static final String GET_GENRE_ALL_FILM = "SELECT f.film_id, " +
            "f.name, " +
            "f.description, " +
            "f.duration, " +
            "f.release_date, " +
            "f.mpa_id, " +
            "m.name AS mpa_name, " +
            "fg.genre_id, " +
            "g.name AS genre_name " +
            "FROM films AS f " +
            "JOIN mpa AS m ON f.mpa_id = m.mpa_id " +
            "LEFT JOIN film_genre AS fg ON  f.film_id = fg.film_id " +
            "LEFT JOIN genre AS g ON fg.genre_id = g.genre_id ";
}
