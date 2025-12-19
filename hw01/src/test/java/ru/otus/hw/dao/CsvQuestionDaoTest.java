package ru.otus.hw.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

public class CsvQuestionDaoTest {

    private static final String EXISTING_FILE = "questions.csv";
    private static final String NON_EXISTENT_FILE = "no-such-file.csv";

    @Mock
    private TestFileNameProvider fileNameProvider;

    private CsvQuestionDao dao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dao = new CsvQuestionDao(fileNameProvider);
    }

    @Test
    void shouldLoadQuestionsFromExistingFile() {
        when(fileNameProvider.getTestFileName()).thenReturn(EXISTING_FILE);

        List<Question> questions = dao.findAll();

        assertThat(questions)
                .isNotNull()
                .isNotEmpty()
                .hasSize(3)
                .allMatch(q -> q.text() != null && !q.text().isBlank())
                .allMatch(q -> q.answers() != null && !q.answers().isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenFileNotFound() {
        when(fileNameProvider.getTestFileName()).thenReturn(NON_EXISTENT_FILE);

        assertThatThrownBy(dao::findAll)
                .isInstanceOf(QuestionReadException.class)
                .hasMessageContaining("File not found")
                .hasMessageContaining(NON_EXISTENT_FILE);
    }
}
