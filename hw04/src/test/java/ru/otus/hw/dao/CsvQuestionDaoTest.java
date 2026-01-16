package ru.otus.hw.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {
        CsvQuestionDao.class,
        TestFileNameProvider.class
})
@ActiveProfiles("test")
public class CsvQuestionDaoTest {

    private static final String EXISTING_FILE = "questions-test.csv";
    private static final String NON_EXISTENT_FILE = "no-such-file.csv";

    @MockitoBean
    private TestFileNameProvider fileNameProvider;

    @Autowired
    private CsvQuestionDao dao;

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
