DROP TABLE IF EXISTS FILMS, USERS, GENRE, MPA, FILM_GENRE, FILM_LIKES, FRIENDS;

create table IF NOT EXISTS GENRE
(
    GENRE_ID INTEGER auto_increment,
    NAME     CHARACTER VARYING(50) not null,
    constraint GENRE_PK
        primary key (GENRE_ID)
);

create table IF NOT EXISTS MPA
(
    MPA_ID INTEGER auto_increment,
    NAME CHARACTER VARYING(50) not null,
    constraint MPA_PK
        primary key (MPA_ID)
);

create table IF NOT EXISTS FILMS
(
    FILM_ID      INTEGER auto_increment,
    NAME         CHARACTER VARYING(150) not null,
    DESCRIPTION  CHARACTER VARYING(255) not null,
    RELEASE_DATE DATE                   not null,
    DURATION     INTEGER                not null,
    MPA_ID       INTEGER                not null,
    constraint FILMS_PK
        primary key (FILM_ID),
    constraint FILMS_FK
        foreign key (MPA_ID) references MPA
);

create table IF NOT EXISTS FILM_GENRE
(
    FILM_ID  INTEGER not null,
    GENRE_ID INTEGER not null,
    constraint FILM_GENRE_PK
        primary key (FILM_ID, GENRE_ID),
    constraint FILM_GENRE_FK
        foreign key (FILM_ID) references FILMS
            on delete cascade,
    constraint FILM_GENRE_FK_1
        foreign key (GENRE_ID) references GENRE
            on delete cascade
);

create table IF NOT EXISTS USERS
(
    USER_ID  INTEGER auto_increment,
    EMAIL    CHARACTER VARYING(255) not null,
    LOGIN    CHARACTER VARYING(50)  not null,
    NAME     CHARACTER VARYING(50)  not null,
    BIRTHDAY DATE                   not null,
    constraint USERS_PK
        primary key (USER_ID)
);

create table IF NOT EXISTS FILM_LIKES
(
    USER_ID INTEGER not null,
    FILM_ID INTEGER not null,
    constraint FILM_LIKES_PK
        primary key (USER_ID, FILM_ID),
    constraint FILM_LIKES_FK
        foreign key (FILM_ID) references FILMS
            on delete cascade,
    constraint FILM_LIKES_FK_1
        foreign key (USER_ID) references USERS
            on delete cascade
);

create table IF NOT EXISTS FRIENDS
(
    USER_ID   INTEGER not null,
    FRIEND_ID INTEGER not null,
    STATUS    BOOLEAN not null,
    constraint FRIENDS_PK
        primary key (USER_ID, FRIEND_ID),
    constraint FRIENDS_FK
        foreign key (USER_ID) references USERS
            on delete cascade,
    constraint FRIENDS_FK_1
        foreign key (FRIEND_ID) references USERS
            on delete cascade
);

