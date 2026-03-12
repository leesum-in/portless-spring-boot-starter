package in.leesum.portless.spring;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PortlessEnvironmentPostProcessorTest {

    private final PortlessEnvironmentPostProcessor processor = new PortlessEnvironmentPostProcessor();
    private FakePortlessProxy fakeProxy;

    @AfterEach
    void tearDown() {
        if (fakeProxy != null) {
            fakeProxy.close();
        }
    }

    @Test
    void skipsWhenDisabled() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("portless.enabled", "false");

        processor.postProcessEnvironment(env, new SpringApplication());

        assertThat(env.getPropertySources().contains("portless")).isFalse();
    }

    @Test
    void skipsWhenNoName() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("portless.state-dir", "/nonexistent/path");

        processor.postProcessEnvironment(env, new SpringApplication());

        assertThat(env.getProperty(PortlessEnvironmentPostProcessor.INTERNAL_NAME_KEY)).isNull();
    }

    @Test
    void findAvailablePort_returnsPortInRange() {
        int port = PortlessEnvironmentPostProcessor.findAvailablePort(8000, 8999);
        assertThat(port).isBetween(8000, 8999);
    }

    @Test
    void findAvailablePort_returnsNegativeOne_whenNoPortAvailable() {
        int port = -1;
        try (var ss = new java.net.ServerSocket()) {
            ss.setReuseAddress(true);
            ss.bind(new InetSocketAddress("127.0.0.1", 0));
            int occupiedPort = ss.getLocalPort();
            port = PortlessEnvironmentPostProcessor.findAvailablePort(occupiedPort, occupiedPort);
        } catch (IOException ignored) {
        }
        assertThat(port).isEqualTo(-1);
    }

    @Test
    void isPortAvailable_returnsTrueForUnusedPort() {
        assertThat(PortlessEnvironmentPostProcessor.isPortAvailable(49876)).isTrue();
    }

    @Test
    void skipsWhenProxyNotDetected() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("portless.name", "myapp");
        env.setProperty("portless.state-dir", "/nonexistent/path");

        processor.postProcessEnvironment(env, new SpringApplication());

        assertThat(env.getProperty(PortlessEnvironmentPostProcessor.INTERNAL_NAME_KEY)).isNull();
    }

    @Test
    void injectsProperties_whenProxyDetected(@TempDir Path tempDir) throws IOException {
        fakeProxy = FakePortlessProxy.start(true);
        fakeProxy.writePortFile(tempDir);

        MockEnvironment env = new MockEnvironment();
        env.setProperty("portless.state-dir", tempDir.toString());
        env.setProperty("portless.name", "testapp");

        processor.postProcessEnvironment(env, new SpringApplication());

        assertThat(env.getProperty("server.port")).isNotNull();
        assertThat(env.getProperty("server.address")).isEqualTo("127.0.0.1");
        assertThat(env.getProperty(PortlessEnvironmentPostProcessor.INTERNAL_NAME_KEY)).isEqualTo("testapp");
        int assignedPort = Integer.parseInt(env.getProperty("server.port"));
        assertThat(assignedPort).isBetween(8000, 8999);
    }

    @Test
    void usesSpringApplicationName_whenPortlessNameNotSet(@TempDir Path tempDir) throws IOException {
        fakeProxy = FakePortlessProxy.start(true);
        fakeProxy.writePortFile(tempDir);

        MockEnvironment env = new MockEnvironment();
        env.setProperty("portless.state-dir", tempDir.toString());
        env.setProperty("spring.application.name", "fallbackapp");

        processor.postProcessEnvironment(env, new SpringApplication());

        assertThat(env.getProperty(PortlessEnvironmentPostProcessor.INTERNAL_NAME_KEY)).isEqualTo("fallbackapp");
    }

    @Test
    void skipsWhenNoNameEvenIfProxyDetected(@TempDir Path tempDir) throws IOException {
        fakeProxy = FakePortlessProxy.start(true);
        fakeProxy.writePortFile(tempDir);

        MockEnvironment env = new MockEnvironment();
        env.setProperty("portless.state-dir", tempDir.toString());

        processor.postProcessEnvironment(env, new SpringApplication());

        assertThat(env.getProperty(PortlessEnvironmentPostProcessor.INTERNAL_NAME_KEY)).isNull();
    }

    @Test
    void usesCustomPortRange(@TempDir Path tempDir) throws IOException {
        fakeProxy = FakePortlessProxy.start(true);
        fakeProxy.writePortFile(tempDir);

        MockEnvironment env = new MockEnvironment();
        env.setProperty("portless.state-dir", tempDir.toString());
        env.setProperty("portless.name", "testapp");
        env.setProperty("portless.min-port", "9000");
        env.setProperty("portless.max-port", "9099");

        processor.postProcessEnvironment(env, new SpringApplication());

        int assignedPort = Integer.parseInt(env.getProperty("server.port"));
        assertThat(assignedPort).isBetween(9000, 9099);
    }

    @Test
    void includesTldInHostname(@TempDir Path tempDir) throws IOException {
        fakeProxy = FakePortlessProxy.start(true);
        fakeProxy.writePortFile(tempDir);

        MockEnvironment env = new MockEnvironment();
        env.setProperty("portless.state-dir", tempDir.toString());
        env.setProperty("portless.name", "testapp");
        env.setProperty("portless.tld", "test");

        processor.postProcessEnvironment(env, new SpringApplication());

        assertThat(env.getProperty(PortlessEnvironmentPostProcessor.INTERNAL_NAME_KEY)).isEqualTo("testapp");
        assertThat(env.getProperty("server.port")).isNotNull();
    }
}
