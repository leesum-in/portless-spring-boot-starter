package in.leesum.portless.spring;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PortlessProcessExecutorTest {

    private final PortlessProcessExecutor executor = new PortlessProcessExecutor();

    @Test
    void execute_returnsOutputAndZeroExitCode() throws IOException {
        PortlessProcessExecutor.Result result = executor.execute(List.of("echo", "hello"));

        assertThat(result.exitCode()).isEqualTo(0);
        assertThat(result.output()).isEqualTo("hello");
    }

    @Test
    void execute_returnsNonZeroExitCode() throws IOException {
        PortlessProcessExecutor.Result result = executor.execute(List.of("sh", "-c", "exit 42"));

        assertThat(result.exitCode()).isEqualTo(42);
    }

    @Test
    void execute_capturesStderrInOutput() throws IOException {
        PortlessProcessExecutor.Result result = executor.execute(
                List.of("sh", "-c", "echo error >&2"));

        assertThat(result.output()).isEqualTo("error");
    }

    @Test
    void execute_throwsOnInvalidCommand() {
        assertThatThrownBy(() -> executor.execute(List.of("nonexistent-command-xyz")))
                .isInstanceOf(IOException.class);
    }
}
