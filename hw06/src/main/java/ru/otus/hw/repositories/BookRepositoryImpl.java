package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;

@Repository
public class BookRepositoryImpl implements BookRepository {

    @PersistenceContext
    private final EntityManager em;

    public BookRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public Optional<Book> findById(long id) {
        var books = em.createQuery(
                        "select distinct b from Book b " +
                                "left join fetch b.author " +
                                "left join fetch b.genres " +
                                "where b.id = :id",
                        Book.class)
                .setParameter("id", id)
                .getResultList();
        return books.isEmpty() ? Optional.empty() : Optional.of(books.get(0));
    }

    @Override
    public List<Book> findAll() {
        return em.createQuery(
                        "select distinct b from Book b " +
                                "left join fetch b.author " +
                                "left join fetch b.genres",
                        Book.class)
                .getResultList();
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            em.persist(book);
            return book;
        } else {
            return em.merge(book);
        }
    }

    @Override
    public void deleteById(long id) {
        Book book = em.find(Book.class, id);
        if (book != null) {
            em.remove(book);
        } else {
            throw new EntityNotFoundException("Book with id %d not found".formatted(id));
        }
    }
}
