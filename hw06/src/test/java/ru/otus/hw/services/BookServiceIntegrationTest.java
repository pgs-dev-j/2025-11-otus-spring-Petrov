package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("Интеграционный тест для BookService")
@SpringBootTest
class BookServiceIntegrationTest {

    @Autowired
    private BookService bookService;

    @DisplayName("должен загружать книгу с автором и жанрами без LazyInitializationException")
    @Test
    void shouldLoadBookWithAuthorAndGenresWithoutLazyException() {
        var optionalBook = bookService.findById(1L);

        assertThat(optionalBook).isPresent();

        Book book = optionalBook.get();

        assertThatCode(() -> {
            var authorName = book.getAuthor().getFullName();
            assertThat(authorName).isNotBlank();
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            var genres = book.getGenres();
            assertThat(genres).isNotEmpty();
            var firstGenreName = genres.get(0).getName();
            assertThat(firstGenreName).isNotBlank();
        }).doesNotThrowAnyException();
    }

    @DisplayName("должен загружать все книги с авторами и жанрами без LazyInitializationException")
    @Test
    void shouldLoadAllBooksWithAuthorAndGenresWithoutLazyException() {
        var books = bookService.findAll();

        assertThat(books).isNotEmpty();

        books.forEach(book -> {
            assertThatCode(() -> {
                var authorName = book.getAuthor().getFullName();
                assertThat(authorName).isNotBlank();
            }).doesNotThrowAnyException();

            assertThatCode(() -> {
                var genres = book.getGenres();
                assertThat(genres).isNotEmpty();
                var firstGenreName = genres.get(0).getName();
                assertThat(firstGenreName).isNotBlank();
            }).doesNotThrowAnyException();
        });
    }

    @DisplayName("должен сохранять книгу с корректными связями")
    @Test
    @Transactional
    void shouldInsertBookWithCorrectRelations() {
        var savedBook = bookService.insert("New Book", 1L, java.util.Set.of(1L, 2L));

        assertThat(savedBook).isNotNull();
        assertThat(savedBook.getId()).isGreaterThan(0);

        assertThatCode(() -> {
            assertThat(savedBook.getAuthor().getFullName()).isEqualTo("Author_1");
            assertThat(savedBook.getGenres()).hasSize(2);
        }).doesNotThrowAnyException();
    }

    @DisplayName("должен обновлять книгу с корректными связями")
    @Test
    @Transactional
    void shouldUpdateBookWithCorrectRelations() {
        var updatedBook = bookService.update(1L, "Updated Title", 2L, java.util.Set.of(3L, 4L));

        assertThat(updatedBook).isNotNull();

        assertThatCode(() -> {
            assertThat(updatedBook.getAuthor().getFullName()).isEqualTo("Author_2");
            assertThat(updatedBook.getGenres()).hasSize(2);
        }).doesNotThrowAnyException();
    }
}