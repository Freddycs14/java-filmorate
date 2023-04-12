# java-filmorate
Template repository for Filmorate project.
![Untitled](https://user-images.githubusercontent.com/118081787/231573383-8a4563c5-6f49-4f85-8806-184485ee22f1.png)
Ссылка на диаграмму:
https://dbdiagram.io/d/6435bd658615191cfa8d2046

Примеры запросов:
1. Получение списка всех фильмов:

    SELECT *

    FROM film;



3. Получение пользователя по id:

   SELECT *

   FROM user

   WHERE user_id = 1;


3. Получение списка топ-5 фильмов по популярности:

   SELECT f.name

   FROM film AS f

   RIGHT JOIN film_likes AS fl ON f.film_id = fl.film_id

   GROUP BY fl.film_id

   ORDER BY SUM(user_id) DESC
   
   LIMIT 5;
 
