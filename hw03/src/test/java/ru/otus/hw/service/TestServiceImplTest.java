package ru.otus.hw.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
class TestServiceImplTest {

    @Autowired
    private TestService testService;

    @MockitoBean
    private QuestionDao questionDao;

    @MockitoBean
    private LocalizedIOService ioService;

    private final Student student = new Student("John", "Doe");

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

        when(ioService.readIntForRangeWithPromptLocalized(
                eq(1), eq(2), anyString(), anyString()))
                .thenReturn(1);

        when(ioService.readIntForRangeWithPromptLocalized(
                eq(1), eq(3), anyString(), anyString()))
                .thenReturn(2);

        TestResult result = testService.executeTestFor(student);

        verify(ioService).printLine("");
        verify(ioService).printFormattedLineLocalized("TestService.answer.the.questions");

        verify(ioService).printFormattedLineLocalized("TestService.question.format", "Question 1?");
        verify(ioService).printFormattedLineLocalized("TestService.answer.format", 1, "Correct");
        verify(ioService).printFormattedLineLocalized("TestService.answer.format", 2, "Wrong");
        verify(ioService).readIntForRangeWithPromptLocalized(1, 2, "TestService.enter.answer.prompt", "TestService.invalid.input.error");

        verify(ioService).printFormattedLineLocalized("TestService.question.format", "Question 2?");
        verify(ioService).printFormattedLineLocalized("TestService.answer.format", 1, "Wrong");
        verify(ioService).printFormattedLineLocalized("TestService.answer.format", 2, "Correct");
        verify(ioService).printFormattedLineLocalized("TestService.answer.format", 3, "Maybe");
        verify(ioService).readIntForRangeWithPromptLocalized(1, 3, "TestService.enter.answer.prompt", "TestService.invalid.input.error");

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

        when(ioService.readIntForRangeWithPromptLocalized(eq(1), eq(2), anyString(), anyString()))
                .thenReturn(2);

        TestResult result = testService.executeTestFor(student);

        assertThat(result.getRightAnswersCount()).isEqualTo(0);
    }
}
