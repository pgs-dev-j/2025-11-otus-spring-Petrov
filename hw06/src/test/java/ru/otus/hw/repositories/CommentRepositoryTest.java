package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе JPA для работы с комментариями")
@DataJpaTest
@Import(CommentRepositoryImpl.class)
class CommentRepositoryTest {

    @Autowired
    private CommentRepositoryImpl commentRepository;

    @Autowired
    private TestEntityManager em;

    @DisplayName("должен загружать комментарий по id")
    @Test
    void shouldReturnCorrectCommentById() {
        Author author = em.persist(new Author(0, "Test Author"));
        Genre genre = em.persist(new Genre(0, "Test Genre"));
        Book book = em.persist(new Book(0, "Test Book", author, List.of(genre)));
        Comment expectedComment = em.persistAndFlush(new Comment(0, "Test Comment", book));

        var actualComment = commentRepository.findById(expectedComment.getId());

        assertThat(actualComment)
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .ignoringFields("book.author", "book.genres")
                .isEqualTo(expectedComment);
    }

    @DisplayName("должен загружать комментарии по id книги")
    @Test
    void shouldReturnCorrectCommentsByBookId() {
        Author author = em.persist(new Author(0, "Test Author"));
        Genre genre = em.persist(new Genre(0, "Test Genre"));
        Book book = em.persist(new Book(0, "Test Book", author, List.of(genre)));

        em.persist(new Comment(0, "Comment 1", book));
        em.persist(new Comment(0, "Comment 2", book));
        em.persist(new Comment(0, "Comment 3", book));
        em.flush();

        var actualComments = commentRepository.findByBookId(book.getId());

        assertThat(actualComments)
                .hasSize(3)
                .extracting(Comment::getText)
                .containsExactlyInAnyOrder("Comment 1", "Comment 2", "Comment 3");
    }

    @DisplayName("должен сохранять новый комментарий")
    @Test
    void shouldSaveNewComment() {
        Author author = em.persist(new Author(0, "Test Author"));
        Genre genre = em.persist(new Genre(0, "Test Genre"));
        Book book = em.persist(new Book(0, "Test Book", author, List.of(genre)));
        em.flush();

        var newComment = new Comment(0, "New Comment", book);

        var savedComment = commentRepository.save(newComment);
        assertThat(savedComment).isNotNull()
                .matches(comment -> comment.getId() > 0)
                .extracting(Comment::getText)
                .isEqualTo("New Comment");

        var foundComment = em.find(Comment.class, savedComment.getId());
        assertThat(foundComment).isNotNull()
                .extracting(Comment::getText)
                .isEqualTo("New Comment");
    }

    @DisplayName("должен обновлять существующий комментарий")
    @Test
    void shouldUpdateExistingComment() {
        Author author = em.persist(new Author(0, "Test Author"));
        Genre genre = em.persist(new Genre(0, "Test Genre"));
        Book book = em.persist(new Book(0, "Test Book", author, List.of(genre)));
        Comment comment = em.persistAndFlush(new Comment(0, "Original Text", book));

        em.clear();

        var updatedComment = new Comment(comment.getId(), "Updated Text", book);

        var savedComment = commentRepository.save(updatedComment);
        assertThat(savedComment)
                .extracting(Comment::getText)
                .isEqualTo("Updated Text");

        var foundComment = em.find(Comment.class, comment.getId());
        assertThat(foundComment)
                .extracting(Comment::getText)
                .isEqualTo("Updated Text");
    }

    @DisplayName("должен удалять комментарий по id")
    @Test
    void shouldDeleteComment() {
        Author author = em.persist(new Author(0, "Test Author"));
        Genre genre = em.persist(new Genre(0, "Test Genre"));
        Book book = em.persist(new Book(0, "Test Book", author, List.of(genre)));
        Comment comment = em.persistAndFlush(new Comment(0, "To Delete", book));

        assertThat(em.find(Comment.class, comment.getId())).isNotNull();

        commentRepository.deleteById(comment.getId());
        em.flush();

        assertThat(em.find(Comment.class, comment.getId())).isNull();
    }
}