package ru.otus.hw.shell;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.service.TestRunnerService;

@ShellComponent
@RequiredArgsConstructor
public class ApplicationCommands {

    private final TestRunnerService testRunnerService;

    @ShellMethod(value = "Start the student testing session", key = {"test", "start", "s"})
    public String start() {
        testRunnerService.run();
        return "OK";
    }
}
