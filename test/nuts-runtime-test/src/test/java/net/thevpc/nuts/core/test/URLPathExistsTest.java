package net.thevpc.nuts.core.test;

import com.sun.net.httpserver.HttpServer;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NPath;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@code NPath::exists} on http urls : HEAD first, GET only when the server
 * does not support HEAD, and no second request when the HEAD one failed to
 * connect.
 */
public class URLPathExistsTest {

    private static HttpServer server;
    private static final AtomicInteger gets = new AtomicInteger();

    @BeforeAll
    static void init() throws Exception {
        TestUtils.openNewMinTestWorkspace();
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/ok", x -> reply(x, 200, "ok"));
        server.createContext("/missing", x -> reply(x, 404, "missing"));
        // HEAD not supported, GET works
        server.createContext("/no-head", x -> {
            if ("HEAD".equals(x.getRequestMethod())) {
                x.sendResponseHeaders(405, -1);
                x.close();
            } else {
                reply(x, 200, "ok");
            }
        });
        server.start();
    }

    @AfterAll
    static void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private static void reply(com.sun.net.httpserver.HttpExchange x, int code, String body) throws java.io.IOException {
        if ("GET".equals(x.getRequestMethod())) {
            gets.incrementAndGet();
        }
        byte[] b = body.getBytes(StandardCharsets.UTF_8);
        if ("HEAD".equals(x.getRequestMethod())) {
            x.sendResponseHeaders(code, -1);
        } else {
            x.sendResponseHeaders(code, b.length);
            try (OutputStream o = x.getResponseBody()) {
                o.write(b);
            }
        }
        x.close();
    }

    private static String url(String path) {
        return "http://127.0.0.1:" + server.getAddress().getPort() + path;
    }

    @Test
    public void existingUrlUsesHeadOnly() {
        int before = gets.get();
        Assertions.assertTrue(NPath.of(url("/ok")).exists());
        Assertions.assertEquals(before, gets.get());
    }

    @Test
    public void missingUrlUsesHeadOnly() {
        int before = gets.get();
        Assertions.assertFalse(NPath.of(url("/missing")).exists());
        Assertions.assertEquals(before, gets.get());
    }

    @Test
    public void headNotSupportedFallsBackToGet() {
        int before = gets.get();
        Assertions.assertTrue(NPath.of(url("/no-head")).exists());
        Assertions.assertEquals(before + 1, gets.get());
    }

    @Test
    public void unreachableHostIsNotRetried() throws Exception {
        int port;
        try (ServerSocket s = new ServerSocket(0)) {
            port = s.getLocalPort();
        }
        // nothing listens on that port any more : connection refused
        long start = System.nanoTime();
        Assertions.assertFalse(NPath.of("http://127.0.0.1:" + port + "/x").exists());
        Assertions.assertTrue(System.nanoTime() - start < 5_000_000_000L);
    }
}
