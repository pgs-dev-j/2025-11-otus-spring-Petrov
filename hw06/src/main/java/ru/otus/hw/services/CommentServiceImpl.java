package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.CommentRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Comment> findById(long id) {
        return commentRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> findByBookId(long bookId) {
        return commentRepository.findByBookId(bookId);
    }

    @Override
    @Transactional
    public Comment save(Comment comment) {
        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        commentRepository.deleteById(id);
    }
}