package in.leesum.portless.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.server.context.WebServerInitializedEvent;
import org.springframework.boot.web.server.WebServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PortlessLifecycleManagerTest {

    private RecordingProcessExecutor recorder;
    private PortlessRouteManager routeManager;
    private PortlessLifecycleManager lifecycleManager;

    @BeforeEach
    void setUp() {
        recorder = new RecordingProcessExecutor(0, "");
        routeManager = new PortlessRouteManager(recorder);
        lifecycleManager = new PortlessLifecycleManager(routeManager, "myapp", false);
    }

    @Test
    void registersRouteOnWebServerInitialized() {
        lifecycleManager.onWebServerInitialized(mockEvent(4213));

        assertThat(lifecycleManager.isRegistered()).isTrue();
        assertThat(recorder.commands).hasSize(1);
        assertThat(recorder.commands.get(0)).containsExactly("portless", "alias", "myapp", "4213");
    }

    @Test
    void unregistersRouteOnDestroy() {
        lifecycleManager.onWebServerInitialized(mockEvent(4213));
        lifecycleManager.destroy();

        assertThat(lifecycleManager.isRegistered()).isFalse();
        assertThat(recorder.commands).hasSize(2);
        assertThat(recorder.commands.get(1)).containsExactly("portless", "alias", "--remove", "myapp");
    }

    @Test
    void destroyDoesNothingWhenNotRegistered() {
        lifecycleManager.destroy();

        assertThat(lifecycleManager.isRegistered()).isFalse();
        assertThat(recorder.commands).isEmpty();
    }

    @Test
    void registersWithForce() {
        var forceManager = new PortlessLifecycleManager(routeManager, "myapp", true);
        forceManager.onWebServerInitialized(mockEvent(4500));

        assertThat(forceManager.isRegistered()).isTrue();
        assertThat(recorder.commands.get(0)).containsExactly("portless", "alias", "--force", "myapp", "4500");
    }

    @Test
    void doesNotRegister_whenCliFails() {
        var failingRecorder = new RecordingProcessExecutor(1, "Route already exists");
        var failingManager = new PortlessRouteManager(failingRecorder);
        var manager = new PortlessLifecycleManager(failingManager, "myapp", false);

        manager.onWebServerInitialized(mockEvent(4213));

        assertThat(manager.isRegistered()).isFalse();
    }

    @Test
    void getName_returnsConfiguredName() {
        assertThat(lifecycleManager.getName()).isEqualTo("myapp");
    }

    private WebServerInitializedEvent mockEvent(int port) {
        WebServerInitializedEvent event = mock(WebServerInitializedEvent.class);
        WebServer webServer = mock(WebServer.class);
        when(event.getWebServer()).thenReturn(webServer);
        when(webServer.getPort()).thenReturn(port);
        return event;
    }
}
