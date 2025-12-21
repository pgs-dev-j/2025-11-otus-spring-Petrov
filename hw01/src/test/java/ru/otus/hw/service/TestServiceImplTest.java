package ru.otus.hw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.List;

import static org.mockito.Mockito.*;

class TestServiceImplTest {

    private IOService ioService;
    private QuestionDao questionDao;
    private TestService testService;

    @BeforeEach
    void setUp() {
        ioService = mock(IOService.class);
        questionDao = mock(QuestionDao.class);
        testService = new TestServiceImpl(ioService, questionDao);
    }

    @Test
    void shouldCorrectlyPrintAnyQuestionsWithNumberedAnswers() {
        List<Question> anyQuestions = List.of(
                new Question("Any question 1?",
                        List.of(new Answer("Answer A", true), new Answer("Answer B", false))),
                new Question("Another question?",
                        List.of(new Answer("Yes", false), new Answer("No", true), new Answer("Maybe", false)))
        );

        when(questionDao.findAll()).thenReturn(anyQuestions);

        testService.executeTest();

        verify(ioService, times(3)).printLine("");

        verify(ioService).printFormattedLine("Please answer the questions below%n");

        verify(ioService).printFormattedLine("Question: %s", "Any question 1?");
        verify(ioService).printFormattedLine("  %d. %s", 1, "Answer A");
        verify(ioService).printFormattedLine("  %d. %s", 2, "Answer B");

        verify(ioService).printFormattedLine("Question: %s", "Another question?");
        verify(ioService).printFormattedLine("  %d. %s", 1, "Yes");
        verify(ioService).printFormattedLine("  %d. %s", 2, "No");
        verify(ioService).printFormattedLine("  %d. %s", 3, "Maybe");

        verifyNoMoreInteractions(ioService);
    }
}
