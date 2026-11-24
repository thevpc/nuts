package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.runtime.standalone.io.NCoreIOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * A truncated HTTP response used to leave a corrupt jar in the cache (on old
 * JDKs it even aborted the whole bootstrap). Downloads must be retried instead.
 */
public class DownloadRetryTest {

    private static ServerSocket server;
    private static Thread serverThread;
    private static volatile boolean stopped;
    private static final Map<String, AtomicInteger> calls = new ConcurrentHashMap<>();
    private static byte[] payload;

    @BeforeAll
    static void start() throws Exception {
        TestUtils.openNewMinTestWorkspace();
        payload = new byte[100_000];
        for (int i = 0; i < payload.length; i++) {
            payload[i] = (byte) ('A' + (i % 26));
        }
        server = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
        serverThread = new Thread(() -> {
            while (!stopped) {
                try (Socket socket = server.accept()) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(socket.getInputStream(), StandardCharsets.ISO_8859_1));
                    String firstLine = reader.readLine();
                    String line;
                    while ((line = reader.readLine()) != null && !line.isEmpty()) {
                        // consume request headers
                    }
                    String path = firstLine == null ? "" : firstLine.split(" ")[1];
                    int call = calls.computeIfAbsent(path, k -> new AtomicInteger()).incrementAndGet();
                    boolean truncate = path.contains("always") || call == 1;
                    OutputStream out = socket.getOutputStream();
                    out.write(("HTTP/1.1 200 OK\r\n"
                            + "Content-Type: application/java-archive\r\n"
                            + "Content-Length: " + payload.length + "\r\n"
                            + "\r\n").getBytes(StandardCharsets.US_ASCII));
                    if (truncate) {
                        // announce the full length but close after a small prefix
                        out.write(payload, 0, 10_000);
                        out.flush();
                    } else {
                        out.write(payload);
                        out.flush();
                    }
                } catch (Exception ignored) {
                    // server stops when the socket is closed
                }
            }
        }, "truncating-http-server");
        serverThread.setDaemon(true);
        serverThread.start();
    }

    @AfterAll
    static void stop() throws Exception {
        stopped = true;
        if (server != null) {
            server.close();
        }
    }

    @Test
    public void truncatedDownloadIsRetried() throws Exception {
        Path dir = Files.createTempDirectory("nuts-download-retry");
        Path file = dir.resolve("nuts-runtime.jar");
        try {
            NCoreIOUtils.copy(url("retry.jar"), file.toFile(), NLog.ofNull());
            Assertions.assertEquals(payload.length, Files.size(file));
            Assertions.assertEquals(2, calls.get("/retry.jar").get(),
                    "the truncated response should be retried exactly once");
        } finally {
            deleteTree(dir);
        }
    }

    @Test
    public void brokenDownloadNeverLeavesAPartialFile() throws Exception {
        Path dir = Files.createTempDirectory("nuts-download-broken");
        Path file = dir.resolve("nuts-runtime.jar");
        try {
            Assertions.assertThrows(IOException.class,
                    () -> NCoreIOUtils.copy(url("always-broken.jar"), file.toFile(), NLog.ofNull()));
            Assertions.assertFalse(Files.exists(file), "a failed download must not leave a corrupt jar behind");
            try (Stream<Path> children = Files.list(dir)) {
                Assertions.assertEquals(0, children.count(), "temporary download files must be cleaned up");
            }
            Assertions.assertEquals(3, calls.get("/always-broken.jar").get(),
                    "a permanent truncation should be attempted three times");
        } finally {
            deleteTree(dir);
        }
    }

    private static URL url(String path) throws Exception {
        return new URL("http://127.0.0.1:" + server.getLocalPort() + "/" + path);
    }

    private static void deleteTree(Path dir) throws IOException {
        try (Stream<Path> children = Files.list(dir)) {
            java.util.Iterator<Path> it = children.iterator();
            while (it.hasNext()) {
                Files.deleteIfExists(it.next());
            }
        }
        Files.deleteIfExists(dir);
    }
}
