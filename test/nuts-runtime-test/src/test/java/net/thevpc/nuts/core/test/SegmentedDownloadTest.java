package net.thevpc.nuts.core.test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.runtime.standalone.xtra.cp.NSegmentedHttpDownload;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Segmented (multi-connection, http range) downloads of a single resource,
 * against a local server supporting ranges, ignoring them, changing the
 * resource mid-download or dropping connections.
 */
public class SegmentedDownloadTest {

    private static final Pattern RANGE = Pattern.compile("bytes=(\\d+)-(\\d+)");
    private static final int SIZE = 1024 * 1024 + 12345;
    private static final byte[] DATA = new byte[SIZE];
    private static HttpServer server;
    private static final AtomicInteger rangeRequests = new AtomicInteger();
    private static final AtomicInteger fullRequests = new AtomicInteger();
    private static final AtomicInteger changingRequests = new AtomicInteger();
    private static final Set<Long> dropped = ConcurrentHashMap.newKeySet();

    @BeforeAll
    static void init() throws Exception {
        TestUtils.openNewMinTestWorkspace();
        new Random(42).nextBytes(DATA);
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/ranged", x -> serve(x, true, "\"v1\"", false));
        server.createContext("/no-range", x -> serve(x, false, "\"v1\"", false));
        // a new version is published after the first GET
        server.createContext("/changing", x -> serve(x, true,
                "HEAD".equals(x.getRequestMethod()) || changingRequests.getAndIncrement() == 0 ? "\"v1\"" : "\"v2\"", false));
        // every chunk but the first fails once, mid-body
        server.createContext("/flaky", x -> serve(x, true, "\"v1\"", true));
        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(8));
        server.start();
    }

    @AfterAll
    static void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private static void serve(HttpExchange x, boolean ranges, String etag, boolean flaky) throws IOException {
        try {
            x.getResponseHeaders().set("ETag", etag);
            if (ranges) {
                x.getResponseHeaders().set("Accept-Ranges", "bytes");
            }
            if ("HEAD".equals(x.getRequestMethod())) {
                // HttpServer drops the length passed for HEAD : set it explicitly
                x.getResponseHeaders().set("Content-Length", String.valueOf(SIZE));
                x.sendResponseHeaders(200, -1);
                return;
            }
            String range = x.getRequestHeaders().getFirst("Range");
            String ifRange = x.getRequestHeaders().getFirst("If-Range");
            Matcher m = range == null ? null : RANGE.matcher(range);
            if (ranges && m != null && m.matches() && (ifRange == null || ifRange.equals(etag))) {
                rangeRequests.incrementAndGet();
                long start = Long.parseLong(m.group(1));
                long end = Math.min(SIZE - 1, Long.parseLong(m.group(2)));
                int len = (int) (end - start + 1);
                x.getResponseHeaders().set("Content-Range", "bytes " + start + "-" + end + "/" + SIZE);
                x.sendResponseHeaders(206, len);
                try (OutputStream o = x.getResponseBody()) {
                    if (flaky && start > 0 && start % 65536 == 0 && dropped.add(start)) {
                        o.write(DATA, (int) start, len / 2);
                        o.flush();
                        // abort the exchange : the client sees a truncated body
                        throw new IOException("dropped");
                    }
                    o.write(DATA, (int) start, len);
                }
            } else {
                fullRequests.incrementAndGet();
                x.sendResponseHeaders(200, SIZE);
                try (OutputStream o = x.getResponseBody()) {
                    o.write(DATA);
                }
            }
        } finally {
            x.close();
        }
    }

    private static URL url(String path) throws Exception {
        return new URL("http://127.0.0.1:" + server.getAddress().getPort() + path);
    }

    private static NSegmentedHttpDownload.Options options() {
        NSegmentedHttpDownload.Options o = new NSegmentedHttpDownload.Options();
        o.chunkSize = 64 * 1024;
        o.minSize = 0;
        o.connections = 4;
        o.connectTimeoutMs = 5000;
        o.readTimeoutMs = 5000;
        return o;
    }

    private static NCp segmentedCp() {
        return NCp.of().segmentedChunkSize(64 * 1024).segmentedMinSize(128 * 1024);
    }

    private static class CountingProgress implements NSegmentedHttpDownload.Progress {
        final AtomicInteger starts = new AtomicInteger();
        final AtomicInteger completes = new AtomicInteger();
        final AtomicLong lastDone = new AtomicLong();

        @Override
        public void onStart(long length) {
            starts.incrementAndGet();
        }

        @Override
        public void onProgress(long done, long length) {
            lastDone.set(done);
        }

        @Override
        public void onComplete(long done, long length, Throwable error) {
            completes.incrementAndGet();
            lastDone.set(done);
        }
    }

    private static Path tempFile() throws IOException {
        Path p = Files.createTempFile("segmented-", ".bin");
        p.toFile().deleteOnExit();
        return p;
    }

    @Test
    public void rangesSupported() throws Exception {
        Path target = tempFile();
        int before = rangeRequests.get();
        CountingProgress progress = new CountingProgress();
        NSegmentedHttpDownload.Result r = NSegmentedHttpDownload.download(url("/ranged"), target, options(), progress);
        Assertions.assertEquals(NSegmentedHttpDownload.Result.SEGMENTED, r);
        Assertions.assertArrayEquals(DATA, Files.readAllBytes(target));
        // ceil(SIZE / 64k) range requests, the first one being the probe
        Assertions.assertEquals((SIZE + 65535) / 65536, rangeRequests.get() - before);
        Assertions.assertEquals(1, progress.starts.get());
        Assertions.assertEquals(1, progress.completes.get());
        Assertions.assertEquals(SIZE, progress.lastDone.get());
    }

    @Test
    public void rangesIgnoredIsReadSequentially() throws Exception {
        Path target = tempFile();
        int before = fullRequests.get();
        NSegmentedHttpDownload.Result r = NSegmentedHttpDownload.download(url("/no-range"), target, options(), new CountingProgress());
        Assertions.assertEquals(NSegmentedHttpDownload.Result.SEQUENTIAL, r);
        Assertions.assertArrayEquals(DATA, Files.readAllBytes(target));
        // the probe response is the download : no second request
        Assertions.assertEquals(1, fullRequests.get() - before);
    }

    @Test
    public void resourceChangingFallsBack() throws Exception {
        changingRequests.set(0);
        Path target = tempFile();
        NSegmentedHttpDownload.Result r = NSegmentedHttpDownload.download(url("/changing"), target, options(), new CountingProgress());
        Assertions.assertEquals(NSegmentedHttpDownload.Result.FALLBACK, r);
    }

    @Test
    public void droppedChunksAreRetried() throws Exception {
        dropped.clear();
        Path target = tempFile();
        NSegmentedHttpDownload.Result r = NSegmentedHttpDownload.download(url("/flaky"), target, options(), new CountingProgress());
        Assertions.assertEquals(NSegmentedHttpDownload.Result.SEGMENTED, r);
        Assertions.assertArrayEquals(DATA, Files.readAllBytes(target));
        Assertions.assertFalse(dropped.isEmpty());
    }

    @Test
    public void ncpUsesSegmentedDownload() throws Exception {
        Path target = tempFile();
        Files.delete(target);
        int before = rangeRequests.get();
        segmentedCp().from(NPath.of(url("/ranged").toString())).to(NPath.of(target)).options(NPathOption.SAFE).run();
        Assertions.assertArrayEquals(DATA, Files.readAllBytes(target));
        Assertions.assertTrue(rangeRequests.get() - before > 1);
    }

    @Test
    public void ncpFallsBackWhenResourceChanges() throws Exception {
        changingRequests.set(0);
        Path target = tempFile();
        Files.delete(target);
        int before = fullRequests.get();
        segmentedCp().from(NPath.of(url("/changing").toString())).to(NPath.of(target)).options(NPathOption.SAFE).run();
        Assertions.assertArrayEquals(DATA, Files.readAllBytes(target));
        Assertions.assertTrue(fullRequests.get() - before >= 1);
    }

    @Test
    public void ncpSegmentedDisabled() throws Exception {
        Path target = tempFile();
        Files.delete(target);
        int before = rangeRequests.get();
        segmentedCp().segmented(false)
                .from(NPath.of(url("/ranged").toString())).to(NPath.of(target)).options(NPathOption.SAFE).run();
        Assertions.assertArrayEquals(DATA, Files.readAllBytes(target));
        Assertions.assertEquals(before, rangeRequests.get());
    }

    @Test
    public void ncpBelowMinSizeIsNotSegmented() throws Exception {
        Path target = tempFile();
        Files.delete(target);
        int before = rangeRequests.get();
        segmentedCp().segmentedMinSize(SIZE + 1L)
                .from(NPath.of(url("/ranged").toString())).to(NPath.of(target)).options(NPathOption.SAFE).run();
        Assertions.assertArrayEquals(DATA, Files.readAllBytes(target));
        Assertions.assertEquals(before, rangeRequests.get());
    }
}
