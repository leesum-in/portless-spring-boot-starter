package in.leesum.portless.spring;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PortlessRouteManagerTest {

    @Test
    void addRoute_executesCorrectCommand() throws IOException {
        var recorder = new RecordingProcessExecutor(0, "");
        var manager = new PortlessRouteManager(recorder);

        manager.addRoute("myapp", 4213, false);

        assertThat(recorder.commands).hasSize(1);
        assertThat(recorder.commands.get(0)).containsExactly("portless", "alias", "myapp", "4213");
    }

    @Test
    void addRoute_withForce_includesForceFlag() throws IOException {
        var recorder = new RecordingProcessExecutor(0, "");
        var manager = new PortlessRouteManager(recorder);

        manager.addRoute("myapp", 4213, true);

        assertThat(recorder.commands.get(0)).containsExactly("portless", "alias", "--force", "myapp", "4213");
    }

    @Test
    void addRoute_throwsOnNonZeroExit() {
        var recorder = new RecordingProcessExecutor(1, "Route already exists");
        var manager = new PortlessRouteManager(recorder);

        assertThatThrownBy(() -> manager.addRoute("myapp", 4213, false))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("portless alias failed")
                .hasMessageContaining("Route already exists");
    }

    @Test
    void removeRoute_executesCorrectCommand() throws IOException {
        var recorder = new RecordingProcessExecutor(0, "");
        var manager = new PortlessRouteManager(recorder);

        manager.removeRoute("myapp");

        assertThat(recorder.commands).hasSize(1);
        assertThat(recorder.commands.get(0)).containsExactly("portless", "alias", "--remove", "myapp");
    }

    @Test
    void removeRoute_doesNotThrowOnNonZeroExit() {
        var recorder = new RecordingProcessExecutor(1, "Route not found");
        var manager = new PortlessRouteManager(recorder);

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> manager.removeRoute("myapp"));
    }
}
