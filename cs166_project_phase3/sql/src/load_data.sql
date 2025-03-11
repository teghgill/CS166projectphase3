/* Replace the location to where you saved the data files*/
COPY Users
FROM '/class/classes/tgill018/CS166projectphase3/cs166_project_phase3/data/users.csv'
WITH DELIMITER ',' CSV HEADER;

COPY Items
FROM '/class/classes/tgill018/CS166projectphase3/cs166_project_phase3/data/items.csv'
WITH DELIMITER ',' CSV HEADER;

COPY Store
FROM '/class/classes/tgill018/CS166projectphase3/cs166_project_phase3/data/store.csv'
WITH DELIMITER ',' CSV HEADER;

COPY FoodOrder
FROM '/class/classes/tgill018/CS166projectphase3/cs166_project_phase3/data/foodorder.csv'
WITH DELIMITER ',' CSV HEADER;

COPY ItemsInOrder
FROM '/class/classes/tgill018/CS166projectphase3/cs166_project_phase3/data/itemsinorder.csv'
WITH DELIMITER ',' CSV HEADER;
