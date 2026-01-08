package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final LocalizedIOService ioService;

    private final QuestionDao questionDao;

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printFormattedLineLocalized("TestService.answer.the.questions");
        var questions = questionDao.findAll();
        var testResult = new TestResult(student);

        for (var question: questions) {
            ioService.printFormattedLineLocalized("TestService.question.format", question.text());
            var answers = question.answers();
            for (int i = 0; i < answers.size(); i++) {
                ioService.printFormattedLineLocalized("TestService.answer.format", i + 1, answers.get(i).text());
            }

            int userAnswerIndex = ioService.readIntForRangeWithPromptLocalized(
                    1,
                    answers.size(),
                    "TestService.enter.answer.prompt",
                    "TestService.invalid.input.error"
            ) - 1;

            boolean isCorrect = answers.get(userAnswerIndex).isCorrect();
            testResult.applyAnswer(question, isCorrect);
        }
        return testResult;
    }
}
