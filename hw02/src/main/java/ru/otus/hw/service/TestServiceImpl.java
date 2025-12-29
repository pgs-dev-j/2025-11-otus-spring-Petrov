package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");
        var questions = questionDao.findAll();
        var testResult = new TestResult(student);

        for (var question: questions) {
            ioService.printFormattedLine("Question: %s", question.text());
            var answers = question.answers();
            for (int i = 0; i < answers.size(); i++) {
                ioService.printFormattedLine("  %d. %s", i + 1, answers.get(i).text());
            }

            int userAnswerIndex = ioService.readIntForRangeWithPrompt(
                    1,
                    answers.size(),
                    "Please enter the answer number: ",
                    "Invalid input! Please enter a number from 1 to " + answers.size()
            ) - 1;

            boolean isCorrect = answers.get(userAnswerIndex).isCorrect();
            testResult.applyAnswer(question, isCorrect);
        }
        return testResult;
    }
}
