-- changeset pgs:001-create-authors-table
CREATE TABLE authors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255)
);

-- changeset pgs:001-create-genres-table
CREATE TABLE genres (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255)
);

-- changeset pgs:001-create-books-table
CREATE TABLE books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    author_id BIGINT NOT NULL,
    CONSTRAINT fk_books_author FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE CASCADE
);

-- changeset pgs:001-create-books-genres-table
CREATE TABLE books_genres (
    book_id BIGINT NOT NULL,
    genre_id BIGINT NOT NULL,
    CONSTRAINT fk_books_genres_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    CONSTRAINT fk_books_genres_genre FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE
);

-- changeset pgs:001-create-comments-table
CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    text VARCHAR(255),
    book_id BIGINT NOT NULL,
    CONSTRAINT fk_comments_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);