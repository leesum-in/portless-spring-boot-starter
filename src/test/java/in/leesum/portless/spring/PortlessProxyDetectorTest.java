package in.leesum.portless.spring;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PortlessProxyDetectorTest {

    @TempDir
    Path tempDir;

    private RecordingProcessExecutor recorder;
    private PortlessProxyDetector detector;
    private FakePortlessProxy fakeProxy;

    @BeforeEach
    void setUp() {
        recorder = new RecordingProcessExecutor(0, "");
        detector = new PortlessProxyDetector(tempDir, recorder);
    }

    @AfterEach
    void tearDown() {
        if (fakeProxy != null) {
            fakeProxy.close();
        }
    }

    @Test
    void readProxyPort_returnsPort_whenFileExists() throws IOException {
        Files.writeString(tempDir.resolve("proxy.port"), "1355\n");
        assertThat(detector.readProxyPort()).isEqualTo(1355);
    }

    @Test
    void readProxyPort_returnsNegative_whenFileMissing() {
        assertThat(detector.readProxyPort()).isEqualTo(-1);
    }

    @Test
    void readProxyPort_returnsNegative_whenFileContainsInvalidContent() throws IOException {
        Files.writeString(tempDir.resolve("proxy.port"), "not-a-number");
        assertThat(detector.readProxyPort()).isEqualTo(-1);
    }

    @Test
    void isProxyRunning_returnsFalse_whenNoProxyPort() {
        assertThat(detector.isProxyRunning()).isFalse();
    }

    @Test
    void isProxyRunning_returnsFalse_whenProxyNotListening() throws IOException {
        Files.writeString(tempDir.resolve("proxy.port"), "59999");
        assertThat(detector.isProxyRunning()).isFalse();
    }

    @Test
    void isProxyRunning_returnsTrue_whenPortlessHeaderPresent() throws IOException {
        fakeProxy = FakePortlessProxy.start(true);
        assertThat(detector.isProxyRunning(fakeProxy.getPort())).isTrue();
    }

    @Test
    void isProxyRunning_returnsFalse_whenPortlessHeaderMissing() throws IOException {
        fakeProxy = FakePortlessProxy.start(false);
        assertThat(detector.isProxyRunning(fakeProxy.getPort())).isFalse();
    }

    @Test
    void detect_returnsTrue_whenProxyAlreadyRunning() throws IOException {
        fakeProxy = FakePortlessProxy.start(true);
        fakeProxy.writePortFile(tempDir);

        assertThat(detector.detect(null)).isTrue();
        assertThat(recorder.commands).isEmpty();
    }

    @Test
    void startProxy_executesWithoutTld() {
        detector.startProxy(null);

        assertThat(recorder.commands).hasSize(1);
        assertThat(recorder.commands.get(0)).containsExactly("portless", "proxy", "start");
    }

    @Test
    void startProxy_executesWithTld() {
        detector.startProxy("test");

        assertThat(recorder.commands).hasSize(1);
        assertThat(recorder.commands.get(0)).containsExactly(
                "portless", "proxy", "start", "--https", "--tld", "test");
    }

    @Test
    void startProxy_returnsFalse_whenCliFails() {
        var failRecorder = new RecordingProcessExecutor(1, "error");
        var failDetector = new PortlessProxyDetector(tempDir, failRecorder);

        assertThat(failDetector.startProxy(null)).isFalse();
    }

    @Test
    void detect_returnsFalse_whenProxyNotRunningAndStartFails() {
        var failRecorder = new RecordingProcessExecutor(1, "error");
        var failDetector = new PortlessProxyDetector(tempDir, failRecorder);

        assertThat(failDetector.detect(null)).isFalse();
    }

    @Test
    void resolveStateDir_usesOverride_whenProvided() {
        Path result = PortlessProxyDetector.resolveStateDir("/custom/path");
        assertThat(result).isEqualTo(Path.of("/custom/path"));
    }

    @Test
    void resolveStateDir_ignoresBlankOverride() {
        Path result = PortlessProxyDetector.resolveStateDir("   ");
        assertThat(result).isNotNull();
    }

    @Test
    void resolveStateDir_returnsFallback_whenNoOverrideAndNoHome() {
        Path result = PortlessProxyDetector.resolveStateDir(null);
        assertThat(result).isNotNull();
    }

    @Test
    void getStateDir_returnsConfiguredDir() {
        assertThat(detector.getStateDir()).isEqualTo(tempDir);
    }
}
