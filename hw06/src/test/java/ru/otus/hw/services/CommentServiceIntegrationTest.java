package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("Интеграционный тест для CommentService")
@SpringBootTest
class CommentServiceIntegrationTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private BookService bookService;

    @DisplayName("должен загружать комментарий с книгой без LazyInitializationException")
    @Test
    void shouldLoadCommentWithBookWithoutLazyException() {
        var optionalComment = commentService.findById(1L);

        assertThat(optionalComment).isPresent();

        Comment comment = optionalComment.get();

        assertThatCode(() -> {
            Book book = comment.getBook();
            assertThat(book).isNotNull();
            var bookTitle = book.getTitle();
            assertThat(bookTitle).isNotBlank();
        }).doesNotThrowAnyException();
    }

    @DisplayName("должен загружать комментарии по bookId с книгой без LazyInitializationException")
    @Test
    void shouldLoadCommentsByBookIdWithBookWithoutLazyException() {
        var comments = commentService.findByBookId(1L);

        assertThat(comments).isNotEmpty();

        comments.forEach(comment -> {
            assertThatCode(() -> {
                Book book = comment.getBook();
                assertThat(book).isNotNull();
                var bookTitle = book.getTitle();
                assertThat(bookTitle).isNotBlank();
            }).doesNotThrowAnyException();
        });
    }

    @DisplayName("должен сохранять новый комментарий с корректной связью к книге")
    @Test
    @Transactional
    void shouldSaveNewCommentWithCorrectBookRelation() {
        var book = bookService.findById(1L).orElseThrow();

        var newComment = new Comment(0, "New Test Comment", book);
        var savedComment = commentService.save(newComment);

        assertThat(savedComment).isNotNull();
        assertThat(savedComment.getId()).isGreaterThan(0);

        assertThatCode(() -> {
            assertThat(savedComment.getBook().getId()).isEqualTo(1L);
            assertThat(savedComment.getBook().getTitle()).isEqualTo("BookTitle_1");
        }).doesNotThrowAnyException();
    }

    @DisplayName("должен обновлять комментарий с сохранением связи к книге")
    @Test
    @Transactional
    void shouldUpdateCommentWithBookRelation() {
        var existingComment = commentService.findById(1L).orElseThrow();

        var updatedComment = new Comment(existingComment.getId(), "Updated Comment Text", existingComment.getBook());
        var savedComment = commentService.save(updatedComment);

        assertThat(savedComment).isNotNull();
        assertThat(savedComment.getText()).isEqualTo("Updated Comment Text");

        assertThatCode(() -> {
            assertThat(savedComment.getBook()).isNotNull();
            assertThat(savedComment.getBook().getId()).isEqualTo(existingComment.getBook().getId());
        }).doesNotThrowAnyException();
    }

    @DisplayName("должен удалять комментарий")
    @Test
    @Transactional
    void shouldDeleteComment() {
        var book = bookService.findById(1L).orElseThrow();
        var newComment = new Comment(0, "Comment to delete", book);
        var savedComment = commentService.save(newComment);

        long commentId = savedComment.getId();

        assertThat(commentService.findById(commentId)).isPresent();

        commentService.deleteById(commentId);

        assertThat(commentService.findById(commentId)).isEmpty();
    }
}