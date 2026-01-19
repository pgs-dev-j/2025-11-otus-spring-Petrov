package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JdbcBookRepository implements BookRepository {

    private final GenreRepository genreRepository;

    private final NamedParameterJdbcOperations jdbcTemplate;

    @Override
    public Optional<Book> findById(long id) {
        String sql = """
               select b.id, b.title,
                      a.id as author_id, a.full_name as full_name,
                      g.id as genre_id, g.name as genre_name
               from books b
               join authors a on a.id = b.author_id
               left join books_genres bg on b.id = bg.book_id
               left join genres g on g.id = bg.genre_id
               where b.id = :id
               """;

        MapSqlParameterSource parameters = new MapSqlParameterSource("id", id);

        Book book = jdbcTemplate.query(sql, parameters, new BookResultSetExtractor());

        return Optional.ofNullable(book);
    }

    @Override
    public List<Book> findAll() {
        var genres = genreRepository.findAll();
        var books = getAllBooksWithoutGenres();
        var relations = getAllGenreRelations();
        mergeBooksInfo(books, genres, relations);
        return books;
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            return insert(book);
        }
        return update(book);
    }

    @Override
    public void deleteById(long id) {
        String sql = "delete from books where id = :id";
        MapSqlParameterSource parameters = new MapSqlParameterSource("id", id);
        jdbcTemplate.update(sql, parameters);
    }

    private List<Book> getAllBooksWithoutGenres() {
        String sql = """
               select b.id, b.title,
                      a.id as author_id, a.full_name as full_name
               from books b
               join authors a on a.id = b.author_id
               """;

        return jdbcTemplate.query(sql, new BookRowMapper());
    }

    private List<BookGenreRelation> getAllGenreRelations() {
        String sql = "select book_id, genre_id from books_genres";

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new BookGenreRelation(rs.getLong("book_id"), rs.getLong("genre_id"))
        );
    }

    private void mergeBooksInfo(List<Book> booksWithoutGenres, List<Genre> genres,
                                List<BookGenreRelation> relations) {
        Map<Long, Genre> genreMap = genres.stream().collect(Collectors.toMap(Genre::getId, g -> g));

        Map<Long, List<Long>> bookGenresMap = relations.stream()
                .collect(Collectors.groupingBy(
                        BookGenreRelation::bookId,
                        Collectors.mapping(BookGenreRelation::genreId, Collectors.toList())
                ));

        booksWithoutGenres.forEach(book -> {
            List<Genre> bookGenres = bookGenresMap.getOrDefault(book.getId(), List.of())
                    .stream()
                    .map(genreMap::get)
                    .toList();
            book.setGenres(bookGenres);
        });
    }

    private Book insert(Book book) {
        var keyHolder = new GeneratedKeyHolder();

        String sql = "insert into books (title, author_id) values (:title, :author_id)";
        MapSqlParameterSource parameterSource = new MapSqlParameterSource(
                Map.of(
                        "title", book.getTitle(),
                        "author_id", book.getAuthor().getId()
                )
        );

        jdbcTemplate.update(sql, parameterSource, keyHolder);

        //noinspection DataFlowIssue
        book.setId(keyHolder.getKeyAs(Long.class));
        batchInsertGenresRelationsFor(book);
        return book;
    }

    private Book update(Book book) {
        String sql = """
                update books
                set title = :title, author_id = :author_id
                where id = :id
                """;

        MapSqlParameterSource parameterSource = new MapSqlParameterSource(
                Map.of(
                        "id", book.getId(),
                        "title", book.getTitle(),
                        "author_id", book.getAuthor().getId()
                )
        );

        int updated = jdbcTemplate.update(sql, parameterSource);
        if (updated == 0) {
            throw new EntityNotFoundException("Book with id %d not found".formatted(book.getId()));
        }

        removeGenresRelationsFor(book);
        batchInsertGenresRelationsFor(book);

        return book;
    }

    private void batchInsertGenresRelationsFor(Book book) {
        String sql = "insert into books_genres (book_id, genre_id) values (:book_id, :genre_id)";

        MapSqlParameterSource[] batchParams = book.getGenres().stream()
                .map(genre -> new MapSqlParameterSource(
                        Map.of(
                                "book_id", book.getId(),
                                "genre_id", genre.getId()
                        )
                ))
                .toArray(MapSqlParameterSource[]::new);

        jdbcTemplate.batchUpdate(sql, batchParams);
    }

    private void removeGenresRelationsFor(Book book) {
        String sql = "delete from books_genres where book_id = :book_id";
        MapSqlParameterSource parameterSource = new MapSqlParameterSource("book_id", book.getId());
        jdbcTemplate.update(sql, parameterSource);
    }

    private static class BookRowMapper implements RowMapper<Book> {

        @Override
        public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
            long id = rs.getLong("id");
            String title = rs.getString("title");

            long authorId = rs.getLong("author_id");
            String fullName = rs.getString("full_name");
            Author author = new Author(authorId, fullName);

            return new Book(id, title, author, new ArrayList<>());
        }
    }

    // Использовать для findById
    @SuppressWarnings("ClassCanBeRecord")
    @RequiredArgsConstructor
    private static class BookResultSetExtractor implements ResultSetExtractor<Book> {

        @Override
        public Book extractData(ResultSet rs) throws SQLException, DataAccessException {
            Book book = null;
            List<Genre> genres = new ArrayList<>();

            while (rs.next()) {
                if (book == null) {
                    long bookId = rs.getLong("id");
                    String title = rs.getString("title");

                    long authorId = rs.getLong("author_id");
                    String authorName = rs.getString("full_name");
                    Author author = new Author(authorId, authorName);

                    book = new Book(bookId, title, author, genres);
                }

                long genreId = rs.getLong("genre_id");
                if (!rs.wasNull()) {
                    String genreName = rs.getString("genre_name");
                    genres.add(new Genre(genreId, genreName));
                }
            }

            return book;
        }
    }

    private record BookGenreRelation(long bookId, long genreId) {
    }
}
