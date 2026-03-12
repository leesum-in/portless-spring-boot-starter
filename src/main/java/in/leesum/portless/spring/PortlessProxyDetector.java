package in.leesum.portless.spring;

import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PortlessProxyDetector {

    private static final Logger LOG = Logger.getLogger(PortlessProxyDetector.class.getName());
    private static final int CONNECT_TIMEOUT_MS = 500;

    private final Path stateDir;
    private final PortlessProcessExecutor executor;

    public PortlessProxyDetector(Path stateDir) {
        this(stateDir, new PortlessProcessExecutor());
    }

    PortlessProxyDetector(Path stateDir, PortlessProcessExecutor executor) {
        this.stateDir = stateDir;
        this.executor = executor;
    }

    public static Path resolveStateDir(String stateDirOverride) {
        if (StringUtils.hasText(stateDirOverride)) {
            return Paths.get(stateDirOverride);
        }

        String envDir = System.getenv("PORTLESS_STATE_DIR");
        if (StringUtils.hasText(envDir)) {
            return Paths.get(envDir);
        }

        Path homeDir = Paths.get(System.getProperty("user.home"), ".portless");
        if (Files.exists(homeDir.resolve("proxy.port"))) {
            return homeDir;
        }

        return Paths.get("/tmp/portless");
    }

    public int readProxyPort() {
        try {
            String content = Files.readString(stateDir.resolve("proxy.port")).trim();
            return Integer.parseInt(content);
        } catch (IOException | NumberFormatException e) {
            return -1;
        }
    }

    public boolean isProxyRunning() {
        int proxyPort = readProxyPort();
        if (proxyPort <= 0) {
            return false;
        }
        return isProxyRunning(proxyPort);
    }

    public boolean isProxyRunning(int proxyPort) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) URI.create(
                    "http://127.0.0.1:" + proxyPort + "/").toURL().openConnection();
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(CONNECT_TIMEOUT_MS);
            conn.connect();
            return "1".equals(conn.getHeaderField("x-portless"));
        } catch (IOException e) {
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public boolean startProxy(String tld) {
        try {
            List<String> cmd = new ArrayList<>();
            cmd.add("portless");
            cmd.add("proxy");
            cmd.add("start");
            if (tld != null) {
                cmd.add("--https");
                cmd.add("--tld");
                cmd.add(tld);
            }
            PortlessProcessExecutor.Result result = executor.execute(cmd);
            if (result.exitCode() == 0) {
                LOG.info("Portless: proxy started automatically" + (tld != null ? " (tld=" + tld + ")" : ""));
                return true;
            }
            LOG.warning("Portless: failed to start proxy: " + result.output());
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public Path getStateDir() {
        return stateDir;
    }

    public boolean detect(String tld) {
        int proxyPort = readProxyPort();
        if (proxyPort > 0 && isProxyRunning(proxyPort)) {
            return true;
        }
        if (startProxy(tld)) {
            return readProxyPort() > 0;
        }
        return false;
    }
}
