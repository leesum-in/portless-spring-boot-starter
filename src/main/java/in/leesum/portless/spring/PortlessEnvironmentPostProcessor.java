package in.leesum.portless.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

public class PortlessEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final Logger LOG = Logger.getLogger(PortlessEnvironmentPostProcessor.class.getName());
    static final String PROPERTY_SOURCE_NAME = "portless";
    static final String INTERNAL_NAME_KEY = "portless._internal.name";
    private static final int RANDOM_ATTEMPTS = 50;

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if ("false".equalsIgnoreCase(environment.getProperty("portless.enabled"))) {
            LOG.fine("Portless disabled via property");
            return;
        }

        String stateDir = environment.getProperty("portless.state-dir");
        Path resolvedStateDir = PortlessProxyDetector.resolveStateDir(stateDir);
        PortlessProxyDetector detector = new PortlessProxyDetector(resolvedStateDir);

        String tld = environment.getProperty("portless.tld");

        if (!detector.detect(tld)) {
            LOG.fine("Portless proxy not available, skipping");
            return;
        }

        String name = resolveName(environment);
        if (name == null) {
            LOG.warning("Portless: 'portless.name' is not set and spring.application.name is not available. Skipping.");
            return;
        }

        int minPort = environment.getProperty("portless.min-port", Integer.class, PortlessProperties.DEFAULT_MIN_PORT);
        int maxPort = environment.getProperty("portless.max-port", Integer.class, PortlessProperties.DEFAULT_MAX_PORT);

        int port = findAvailablePort(minPort, maxPort);
        if (port == -1) {
            LOG.warning("Portless: could not find available port in range " + minPort + "-" + maxPort);
            return;
        }

        Map<String, Object> props = new HashMap<>();
        props.put("server.port", port);
        props.put("server.address", "127.0.0.1");
        props.put(INTERNAL_NAME_KEY, name);
        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, props));

        String hostname = name + "." + (StringUtils.hasText(tld) ? tld : "localhost");
        String scheme = StringUtils.hasText(tld) ? "https" : "http";
        LOG.info("Portless: assigned port " + port + " for " + scheme + "://" + hostname);
    }

    private String resolveName(ConfigurableEnvironment environment) {
        String name = environment.getProperty("portless.name");
        if (StringUtils.hasText(name)) {
            return name;
        }
        String appName = environment.getProperty("spring.application.name");
        if (StringUtils.hasText(appName)) {
            return appName;
        }
        return null;
    }

    static int findAvailablePort(int minPort, int maxPort) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < RANDOM_ATTEMPTS; i++) {
            int port = random.nextInt(minPort, maxPort + 1);
            if (isPortAvailable(port)) {
                return port;
            }
        }
        for (int port = minPort; port <= maxPort; port++) {
            if (isPortAvailable(port)) {
                return port;
            }
        }
        return -1;
    }

    static boolean isPortAvailable(int port) {
        try (ServerSocket ss = new ServerSocket()) {
            ss.setReuseAddress(true);
            ss.bind(new InetSocketAddress("127.0.0.1", port));
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
