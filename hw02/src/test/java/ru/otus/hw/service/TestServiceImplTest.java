package ru.otus.hw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TestServiceImplTest {

    private IOService ioService;
    private QuestionDao questionDao;
    private TestService testService;

    private Student student;

    @BeforeEach
    void setUp() {
        ioService = mock(IOService.class);
        questionDao = mock(QuestionDao.class);
        testService = new TestServiceImpl(ioService, questionDao);

        student = new Student("John", "Doe");
    }

    @Test
    void shouldCorrectlyExecuteTestAndCountAnswers() {
        List<Question> questions = List.of(
                new Question("Question 1?",
                        List.of(
                                new Answer("Correct", true),
                                new Answer("Wrong", false)
                        )),
                new Question("Question 2?",
                        List.of(
                                new Answer("Wrong", false),
                                new Answer("Correct", true),
                                new Answer("Maybe", false)
                        ))
        );

        when(questionDao.findAll()).thenReturn(questions);

        when(ioService.readIntForRangeWithPrompt(
                eq(1), eq(2), anyString(), anyString()))
                .thenReturn(1);

        when(ioService.readIntForRangeWithPrompt(
                eq(1), eq(3), anyString(), anyString()))
                .thenReturn(2);

        TestResult result = testService.executeTestFor(student);

        verify(ioService).printLine("");
        verify(ioService).printFormattedLine("Please answer the questions below%n");

        verify(ioService).printFormattedLine("Question: %s", "Question 1?");
        verify(ioService).printFormattedLine("  %d. %s", 1, "Correct");
        verify(ioService).printFormattedLine("  %d. %s", 2, "Wrong");
        verify(ioService).readIntForRangeWithPrompt(1, 2, "Please enter the answer number: ",
                "Invalid input! Please enter a number from 1 to 2");

        verify(ioService).printFormattedLine("Question: %s", "Question 2?");
        verify(ioService).printFormattedLine("  %d. %s", 1, "Wrong");
        verify(ioService).printFormattedLine("  %d. %s", 2, "Correct");
        verify(ioService).printFormattedLine("  %d. %s", 3, "Maybe");
        verify(ioService).readIntForRangeWithPrompt(1, 3, "Please enter the answer number: ",
                "Invalid input! Please enter a number from 1 to 3");

        assertThat(result.getStudent()).isEqualTo(student);
        assertThat(result.getAnsweredQuestions()).hasSize(2);
        assertThat(result.getRightAnswersCount()).isEqualTo(2);
    }

    @Test
    void shouldCountWrongAnswers() {
        List<Question> questions = List.of(
                new Question("Q?", List.of(new Answer("Yes", true), new Answer("No", false)))
        );
        when(questionDao.findAll()).thenReturn(questions);

        when(ioService.readIntForRangeWithPrompt(eq(1), eq(2), anyString(), anyString()))
                .thenReturn(2);

        TestResult result = testService.executeTestFor(student);

        assertThat(result.getRightAnswersCount()).isEqualTo(0);
    }
}
