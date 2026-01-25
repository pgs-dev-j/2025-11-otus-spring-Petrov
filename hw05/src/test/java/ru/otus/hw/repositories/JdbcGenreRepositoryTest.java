package ru.otus.hw.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(JdbcGenreRepository.class)
public class JdbcGenreRepositoryTest {

    @Autowired
    private JdbcGenreRepository repository;

    @Test
    void shouldReturnCorrectGenresList() {
        var expectedGenres = IntStream.range(1, 7).boxed()
                .map(id -> new Genre(id, "Genre_" + id))
                .toList();

        var actualGenres = repository.findAll();

        assertThat(actualGenres).containsExactlyElementsOf(expectedGenres);
    }

    @Test
    void shouldReturnCorrectGenresByIds() {
        var ids = Set.of(1L, 3L, 5L);
        var expectedGenres = List.of(
                new Genre(1, "Genre_1"),
                new Genre(3, "Genre_3"),
                new Genre(5, "Genre_5")
        );

        var actualGenres = repository.findAllByIds(ids);

        assertThat(actualGenres).containsExactlyInAnyOrderElementsOf(expectedGenres);
    }

    @Test
    void shouldReturnEmptyListForNonExistentIds() {
        var ids = Set.of(999L, 1000L);

        var actualGenres = repository.findAllByIds(ids);

        assertThat(actualGenres).isEmpty();
    }
}
