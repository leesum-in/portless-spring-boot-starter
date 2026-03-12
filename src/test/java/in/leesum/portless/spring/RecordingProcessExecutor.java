package in.leesum.portless.spring;

import java.util.ArrayList;
import java.util.List;

class RecordingProcessExecutor extends PortlessProcessExecutor {

    final List<List<String>> commands = new ArrayList<>();
    private final int exitCode;
    private final String output;

    RecordingProcessExecutor(int exitCode, String output) {
        this.exitCode = exitCode;
        this.output = output;
    }

    @Override
    Result execute(List<String> command) {
        commands.add(List.copyOf(command));
        return new Result(exitCode, output);
    }
}
