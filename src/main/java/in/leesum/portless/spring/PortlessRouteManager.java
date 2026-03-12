package in.leesum.portless.spring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PortlessRouteManager {

    private static final Logger LOG = Logger.getLogger(PortlessRouteManager.class.getName());

    private final PortlessProcessExecutor executor;

    public PortlessRouteManager() {
        this(new PortlessProcessExecutor());
    }

    PortlessRouteManager(PortlessProcessExecutor executor) {
        this.executor = executor;
    }

    public void addRoute(String name, int port, boolean force) throws IOException {
        List<String> cmd = new ArrayList<>();
        cmd.add("portless");
        cmd.add("alias");
        if (force) {
            cmd.add("--force");
        }
        cmd.add(name);
        cmd.add(String.valueOf(port));

        PortlessProcessExecutor.Result result = executor.execute(cmd);
        if (result.exitCode() != 0) {
            throw new IOException("portless alias failed (exit " + result.exitCode() + "): " + result.output());
        }
    }

    public void removeRoute(String name) throws IOException {
        List<String> cmd = List.of("portless", "alias", "--remove", name);

        PortlessProcessExecutor.Result result = executor.execute(cmd);
        if (result.exitCode() != 0) {
            LOG.log(Level.WARNING, "portless alias --remove failed (exit {0}): {1}",
                    new Object[]{result.exitCode(), result.output()});
        }
    }
}
