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
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepositoryImpl;
import ru.otus.hw.repositories.BookRepositoryImpl;
import ru.otus.hw.repositories.CommentRepositoryImpl;
import ru.otus.hw.repositories.GenreRepositoryImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Интеграционный тест для CommentService")
@DataJpaTest
@Import({CommentServiceImpl.class, CommentRepositoryImpl.class,
        BookServiceImpl.class, BookRepositoryImpl.class,
        AuthorRepositoryImpl.class, GenreRepositoryImpl.class})
class CommentServiceIntegrationTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private BookService bookService;

    @DisplayName("должен загружать комментарий с книгой без LazyInitializationException")
    @Test
    @Transactional(propagation = Propagation.NEVER)
    void shouldLoadCommentWithBookWithoutLazyException() {
        var expectedBook = new Book(1L, "BookTitle_1",
                new Author(1L, "Author_1"),
                List.of(new Genre(1L, "Genre_1"), new Genre(2L, "Genre_2")));
        var expected = new Comment(1L, "Comment_1 for Book_1", expectedBook);

        assertThat(commentService.findById(1L))
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @DisplayName("должен загружать комментарии по bookId с книгой без LazyInitializationException")
    @Test
    @Transactional(propagation = Propagation.NEVER)
    void shouldLoadCommentsByBookIdWithBookWithoutLazyException() {
        var comments = commentService.findByBookId(1L);

        assertThat(comments).isNotEmpty();

        comments.forEach(comment ->
                assertThat(comment.getBook()).isNotNull()
        );
    }

    @DisplayName("должен сохранять новый комментарий с корректной связью к книге")
    @Test
    @Transactional
    void shouldSaveNewCommentWithCorrectBookRelation() {
        var book = bookService.findById(1L).orElseThrow();
        var newComment = new Comment(0, "New Test Comment", book);

        var savedComment = commentService.save(newComment);

        assertThat(savedComment)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(newComment);
    }

    @DisplayName("должен обновлять комментарий с сохранением связи к книге")
    @Test
    @Transactional
    void shouldUpdateCommentWithBookRelation() {
        var existingComment = commentService.findById(1L).orElseThrow();
        var updatedComment = new Comment(existingComment.getId(), "Updated Text", existingComment.getBook());

        assertThat(commentService.save(updatedComment))
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(updatedComment);
    }

    @DisplayName("должен удалять комментарий")
    @Test
    @Transactional
    void shouldDeleteComment() {
        var book = bookService.findById(1L).orElseThrow();
        var saved = commentService.save(new Comment(0, "To delete", book));

        assertThat(commentService.findById(saved.getId())).isPresent();
        commentService.deleteById(saved.getId());
        assertThat(commentService.findById(saved.getId())).isEmpty();
    }
}