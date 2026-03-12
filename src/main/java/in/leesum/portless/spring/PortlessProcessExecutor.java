package in.leesum.portless.spring;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

class PortlessProcessExecutor {

    private static final long TIMEOUT_SECONDS = 10;

    record Result(int exitCode, String output) {}

    Result execute(List<String> command) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(command)
                .redirectErrorStream(true);
        Process process = pb.start();
        String output;
        try (var is = process.getInputStream()) {
            output = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
        try {
            if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new IOException("portless command timed out: " + command);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            throw new IOException("Interrupted while running portless command", e);
        }
        return new Result(process.exitValue(), output.trim());
    }
}
