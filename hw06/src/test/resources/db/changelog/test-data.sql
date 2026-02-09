-- changeset pgs:test-001-insert-authors
INSERT INTO authors (full_name) VALUES ('Author_1');
INSERT INTO authors (full_name) VALUES ('Author_2');
INSERT INTO authors (full_name) VALUES ('Author_3');

-- changeset pgs:test-001-insert-genres
INSERT INTO genres (name) VALUES ('Genre_1');
INSERT INTO genres (name) VALUES ('Genre_2');
INSERT INTO genres (name) VALUES ('Genre_3');
INSERT INTO genres (name) VALUES ('Genre_4');
INSERT INTO genres (name) VALUES ('Genre_5');
INSERT INTO genres (name) VALUES ('Genre_6');

-- changeset pgs:test-001-insert-books
INSERT INTO books (title, author_id) VALUES ('BookTitle_1', 1);
INSERT INTO books (title, author_id) VALUES ('BookTitle_2', 2);
INSERT INTO books (title, author_id) VALUES ('BookTitle_3', 3);

-- changeset pgs:test-001-insert-books-genres
INSERT INTO books_genres (book_id, genre_id) VALUES (1, 1);
INSERT INTO books_genres (book_id, genre_id) VALUES (1, 2);
INSERT INTO books_genres (book_id, genre_id) VALUES (2, 3);
INSERT INTO books_genres (book_id, genre_id) VALUES (2, 4);
INSERT INTO books_genres (book_id, genre_id) VALUES (3, 5);
INSERT INTO books_genres (book_id, genre_id) VALUES (3, 6);

-- changeset pgs:test-001-insert-comments
INSERT INTO comments (text, book_id) VALUES ('Comment_1 for Book_1', 1);
INSERT INTO comments (text, book_id) VALUES ('Comment_2 for Book_1', 1);
INSERT INTO comments (text, book_id) VALUES ('Comment_3 for Book_1', 1);
INSERT INTO comments (text, book_id) VALUES ('Comment_1 for Book_2', 2);
INSERT INTO comments (text, book_id) VALUES ('Comment_2 for Book_2', 2);