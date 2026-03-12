package in.leesum.portless.spring;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.web.server.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PortlessLifecycleManager implements DisposableBean {

    private static final Logger LOG = Logger.getLogger(PortlessLifecycleManager.class.getName());

    private final PortlessRouteManager routeManager;
    private final String name;
    private final boolean force;
    private volatile boolean registered = false;

    public PortlessLifecycleManager(PortlessRouteManager routeManager, String name, boolean force) {
        this.routeManager = routeManager;
        this.name = name;
        this.force = force;
    }

    @EventListener
    public void onWebServerInitialized(WebServerInitializedEvent event) {
        int port = event.getWebServer().getPort();

        try {
            routeManager.addRoute(name, port, force);
            registered = true;
            LOG.log(Level.INFO, "Portless: registered route {0} -> 127.0.0.1:{1}",
                    new Object[]{name, port});
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Portless: failed to register route for {0}",
                    new Object[]{name});
        }
    }

    @Override
    public void destroy() {
        if (!registered) {
            return;
        }
        try {
            routeManager.removeRoute(name);
            registered = false;
            LOG.log(Level.INFO, "Portless: unregistered route {0}", name);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Portless: failed to unregister route for {0}",
                    new Object[]{name});
        }
    }

    boolean isRegistered() {
        return registered;
    }

    String getName() {
        return name;
    }
}
