package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepositoryImpl;
import ru.otus.hw.repositories.BookRepositoryImpl;
import ru.otus.hw.repositories.GenreRepositoryImpl;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Интеграционный тест для BookService")
@DataJpaTest
@Import({BookServiceImpl.class, BookRepositoryImpl.class,
        AuthorRepositoryImpl.class, GenreRepositoryImpl.class})
@Transactional(propagation = Propagation.NEVER)
class BookServiceIntegrationTest {

    @Autowired
    private BookService bookService;

    @DisplayName("должен загружать книгу с автором и жанрами без LazyInitializationException")
    @Test
    void shouldLoadBookWithAuthorAndGenresWithoutLazyException() {
        var optionalBook = bookService.findById(1L);

        var expected = new Book(1L, "BookTitle_1",
                new Author(1L, "Author_1"),
                List.of(new Genre(1L, "Genre_1"), new Genre(2L, "Genre_2")));

        assertThat(optionalBook)
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @DisplayName("должен загружать все книги с авторами и жанрами без LazyInitializationException")
    @Test
    void shouldLoadAllBooksWithAuthorAndGenresWithoutLazyException() {
        var expected = List.of(
                new Book(1L, "BookTitle_1", new Author(1L, "Author_1"),
                        List.of(new Genre(1L, "Genre_1"), new Genre(2L, "Genre_2"))),
                new Book(2L, "BookTitle_2", new Author(2L, "Author_2"),
                        List.of(new Genre(3L, "Genre_3"), new Genre(4L, "Genre_4"))),
                new Book(3L, "BookTitle_3", new Author(3L, "Author_3"),
                        List.of(new Genre(5L, "Genre_5"), new Genre(6L, "Genre_6")))
        );

        assertThat(bookService.findAll())
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @DisplayName("должен сохранять книгу с корректными связями")
    @Test
    @Transactional
    void shouldInsertBookWithCorrectRelations() {
        var expected = new Book(0, "New Book",
                new Author(1L, "Author_1"),
                List.of(new Genre(1L, "Genre_1"), new Genre(2L, "Genre_2")));

        var savedBook = bookService.insert("New Book", 1L, Set.of(1L, 2L));

        assertThat(savedBook)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expected);
    }

    @DisplayName("должен обновлять книгу с корректными связями")
    @Test
    @Transactional
    void shouldUpdateBookWithCorrectRelations() {
        var expected = new Book(1L, "Updated Title",
                new Author(2L, "Author_2"),
                List.of(new Genre(3L, "Genre_3"), new Genre(4L, "Genre_4")));

        var updatedBook = bookService.update(1L, "Updated Title", 2L, Set.of(3L, 4L));

        assertThat(updatedBook)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }
}