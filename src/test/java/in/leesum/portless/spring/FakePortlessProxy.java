package in.leesum.portless.spring;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

class FakePortlessProxy implements AutoCloseable {

    private final HttpServer server;

    private FakePortlessProxy(HttpServer server) {
        this.server = server;
    }

    static FakePortlessProxy start(boolean includeHeader) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            if (includeHeader) {
                exchange.getResponseHeaders().add("x-portless", "1");
            }
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.start();
        return new FakePortlessProxy(server);
    }

    int getPort() {
        return server.getAddress().getPort();
    }

    void writePortFile(Path stateDir) throws IOException {
        Files.writeString(stateDir.resolve("proxy.port"), String.valueOf(getPort()));
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
