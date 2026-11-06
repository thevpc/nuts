package net.thevpc.nuts.runtime.standalone.xtra.cp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Downloads a single http(s) resource over several connections, each one
 * fetching a byte range ({@code Range: bytes=a-b}) written in place into a
 * pre-allocated file.
 * <br>
 * The first chunk request doubles as the capability probe : a {@code 206}
 * with a consistent {@code Content-Range} enables segmenting, a {@code 200}
 * (ranges ignored) is consumed as a plain sequential download, anything else
 * hands over to the caller ({@link Result#FALLBACK}). Every further chunk is
 * conditional ({@code If-Range}) on the probe {@code ETag} (or
 * {@code Last-Modified}) so that a resource changing mid-download is detected
 * rather than silently mixed; that, as well as expired signed urls and
 * servers that stop honouring ranges, also ends in {@link Result#FALLBACK}.
 * <br>
 * Transient network failures are retried per chunk, resuming from the last
 * byte received. Worker threads only use {@code java.net} and the file
 * channel (no workspace or session access) : everything workspace dependent
 * (timeouts, user agent) is captured in {@link Options} by the caller, and
 * progress is reported back on the caller thread.
 */
public final class NSegmentedHttpDownload {

    private static final Pattern CONTENT_RANGE = Pattern.compile("bytes\\s+(\\d+)-(\\d+)/(\\d+|\\*)");
    private static final int BUFFER_SIZE = 64 * 1024;
    private static final long PROGRESS_PERIOD_MS = 200;
    private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);

    public enum Result {
        /**
         * downloaded over several ranged connections
         */
        SEGMENTED,
        /**
         * the server ignored ranges and the full body was read sequentially
         */
        SEQUENTIAL,
        /**
         * nothing usable was downloaded, the caller must use a plain download
         */
        FALLBACK
    }

    public interface Progress {
        /**
         * called once, on the caller thread, when bytes start being received
         */
        void onStart(long length);

        /**
         * called periodically, on the caller thread
         */
        void onProgress(long done, long length);

        /**
         * called once if {@link #onStart(long)} was called, on the caller thread
         */
        void onComplete(long done, long length, Throwable error);
    }

    public static class Options {
        /**
         * max parallel connections; below 2 disables segmenting
         */
        public int connections = 4;
        /**
         * size of each range request
         */
        public long chunkSize = 4L * 1024 * 1024;
        /**
         * resources smaller than this are never segmented
         */
        public long minSize = 8L * 1024 * 1024;
        public int connectTimeoutMs = 30_000;
        public int readTimeoutMs = 30_000;
        /**
         * retries of a single chunk on transient network failures
         */
        public int chunkRetries = 3;
        public String userAgent;
    }

    private NSegmentedHttpDownload() {
    }

    /**
     * true if {@code url} with known {@code length} is worth a segmented
     * download under {@code options}
     */
    public static boolean isCandidate(URL url, long length, Options options) {
        if (url == null || options == null || options.connections < 2 || length < 0) {
            return false;
        }
        String p = url.getProtocol();
        if (!"http".equals(p) && !"https".equals(p)) {
            return false;
        }
        if (url.getUserInfo() != null) {
            // credentials are handled by the regular http client only
            return false;
        }
        return length >= Math.max(options.minSize, 2 * Math.max(1, options.chunkSize));
    }

    /**
     * downloads {@code url} into {@code target} (created or truncated).
     *
     * @return how the download was performed; on {@link Result#FALLBACK} the
     * content of {@code target} is undefined
     * @throws IOException when the download failed after retries
     */
    public static Result download(URL url, Path target, Options options, Progress progress) throws IOException {
        long chunkSize = Math.max(BUFFER_SIZE, options.chunkSize);
        HttpURLConnection probe = open(url, options);
        probe.setRequestProperty("Range", "bytes=0-" + (chunkSize - 1));
        int code;
        try {
            code = probe.getResponseCode();
        } catch (IOException ex) {
            probe.disconnect();
            throw ex;
        }
        if (code == HttpURLConnection.HTTP_OK) {
            return sequential(probe, target, progress);
        }
        long[] range = code == HttpURLConnection.HTTP_PARTIAL ? parseContentRange(probe.getHeaderField("Content-Range")) : null;
        if (range == null || range[0] != 0 || range[2] < 0) {
            probe.disconnect();
            return Result.FALLBACK;
        }
        long length = range[2];
        // redirects (mirrors, signed urls) are resolved once
        URL finalUrl = probe.getURL();
        String etag = probe.getHeaderField("ETag");
        String validator = (etag != null && !etag.startsWith("W/")) ? etag : probe.getHeaderField("Last-Modified");

        // the probe carries the first chunk (as served, possibly shorter or longer than asked)
        List<long[]> chunks = new ArrayList<>();
        chunks.add(new long[]{0, range[1]});
        for (long s = range[1] + 1; s < length; s += chunkSize) {
            chunks.add(new long[]{s, Math.min(length, s + chunkSize) - 1});
        }

        Files.deleteIfExists(target);
        try (RandomAccessFile raf = new RandomAccessFile(target.toFile(), "rw")) {
            raf.setLength(length);
        }
        AtomicLong done = new AtomicLong();
        AtomicBoolean cancelled = new AtomicBoolean();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        ConcurrentLinkedQueue<long[]> queue = new ConcurrentLinkedQueue<>(chunks.subList(1, chunks.size()));
        int workers = Math.min(options.connections, chunks.size());
        ExecutorService pool = Executors.newFixedThreadPool(workers, newThreadFactory());
        Throwable error = null;
        progress.onStart(length);
        try (FileChannel channel = FileChannel.open(target, StandardOpenOption.WRITE)) {
            Chunk first = new Chunk(chunks.get(0)[0], chunks.get(0)[1]);
            ChunkDownloader d = new ChunkDownloader(finalUrl, validator, etag, length, options, channel, done, cancelled);
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < workers; i++) {
                boolean withProbe = i == 0;
                futures.add(pool.submit(() -> {
                    try {
                        if (withProbe) {
                            d.download(first, probe);
                        }
                        long[] c;
                        while (!cancelled.get() && (c = queue.poll()) != null) {
                            d.download(new Chunk(c[0], c[1]), null);
                        }
                    } catch (Throwable ex) {
                        if (failure.compareAndSet(null, ex)) {
                            cancelled.set(true);
                        }
                    }
                    return null;
                }));
            }
            for (Future<?> f : futures) {
                while (true) {
                    try {
                        f.get(PROGRESS_PERIOD_MS, TimeUnit.MILLISECONDS);
                        break;
                    } catch (TimeoutException ex) {
                        progress.onProgress(done.get(), length);
                    } catch (ExecutionException ex) {
                        failure.compareAndSet(null, ex.getCause());
                        break;
                    } catch (InterruptedException ex) {
                        cancelled.set(true);
                        Thread.currentThread().interrupt();
                        throw new InterruptedIOException("download interrupted : " + url);
                    }
                }
            }
            Throwable t = failure.get();
            if (t instanceof FallbackException) {
                error = t;
                return Result.FALLBACK;
            }
            if (t != null) {
                error = t;
                if (t instanceof IOException) {
                    throw (IOException) t;
                }
                throw new IOException(t);
            }
            if (done.get() != length) {
                error = new IOException("incomplete download : " + done.get() + "/" + length + " bytes : " + url);
                throw (IOException) error;
            }
            return Result.SEGMENTED;
        } catch (IOException | RuntimeException | Error ex) {
            if (error == null) {
                error = ex;
            }
            throw ex;
        } finally {
            cancelled.set(true);
            pool.shutdownNow();
            probe.disconnect();
            progress.onComplete(done.get(), length, error);
        }
    }

    private static Result sequential(HttpURLConnection c, Path target, Progress progress) throws IOException {
        long length = c.getContentLengthLong();
        long done = 0;
        Throwable error = null;
        progress.onStart(length);
        long last = System.currentTimeMillis();
        try (InputStream in = c.getInputStream();
             FileChannel channel = FileChannel.open(target, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int n;
            while ((n = in.read(buffer)) > 0) {
                ByteBuffer bb = ByteBuffer.wrap(buffer, 0, n);
                while (bb.hasRemaining()) {
                    channel.write(bb);
                }
                done += n;
                long now = System.currentTimeMillis();
                if (now - last >= PROGRESS_PERIOD_MS) {
                    last = now;
                    progress.onProgress(done, length);
                }
            }
            if (length >= 0 && done != length) {
                throw new MiddleTransferException("incomplete download : " + done + "/" + length + " bytes : " + c.getURL());
            }
            return Result.SEQUENTIAL;
        } catch (IOException | RuntimeException ex) {
            error = ex;
            throw ex;
        } finally {
            c.disconnect();
            progress.onComplete(done, length, error);
        }
    }

    private static HttpURLConnection open(URL url, Options options) throws IOException {
        URLConnection uc = url.openConnection();
        if (!(uc instanceof HttpURLConnection)) {
            throw new IOException("not an http url : " + url);
        }
        HttpURLConnection c = (HttpURLConnection) uc;
        c.setConnectTimeout(Math.max(0, options.connectTimeoutMs));
        c.setReadTimeout(Math.max(0, options.readTimeoutMs));
        c.setInstanceFollowRedirects(true);
        // byte ranges are meaningless on a transparently compressed body
        c.setRequestProperty("Accept-Encoding", "identity");
        if (options.userAgent != null) {
            c.setRequestProperty("User-Agent", options.userAgent);
        }
        return c;
    }

    /**
     * @return {start, end, total} (total -1 when unknown) or null
     */
    static long[] parseContentRange(String value) {
        if (value == null) {
            return null;
        }
        Matcher m = CONTENT_RANGE.matcher(value.trim());
        if (!m.matches()) {
            return null;
        }
        try {
            long start = Long.parseLong(m.group(1));
            long end = Long.parseLong(m.group(2));
            long total = "*".equals(m.group(3)) ? -1 : Long.parseLong(m.group(3));
            if (end < start || (total >= 0 && end >= total)) {
                return null;
            }
            return new long[]{start, end, total};
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * parses sizes such as {@code 1048576}, {@code 512k}, {@code 8m}, {@code 1g}
     */
    public static long parseSize(String value, long defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        String s = value.trim().toLowerCase();
        if (s.endsWith("b")) {
            s = s.substring(0, s.length() - 1);
        }
        long factor = 1;
        if (!s.isEmpty()) {
            switch (s.charAt(s.length() - 1)) {
                case 'k':
                    factor = 1024L;
                    break;
                case 'm':
                    factor = 1024L * 1024;
                    break;
                case 'g':
                    factor = 1024L * 1024 * 1024;
                    break;
            }
            if (factor != 1) {
                s = s.substring(0, s.length() - 1);
            }
        }
        try {
            return Long.parseLong(s.trim()) * factor;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private static ThreadFactory newThreadFactory() {
        String prefix = "nuts-download-" + POOL_NUMBER.getAndIncrement() + "-";
        AtomicInteger n = new AtomicInteger(1);
        return r -> {
            Thread t = new Thread(r, prefix + n.getAndIncrement());
            t.setDaemon(true);
            return t;
        };
    }

    private static class Chunk {
        final long start;
        final long end;
        /**
         * next byte to write, kept across retries
         */
        long pos;

        Chunk(long start, long end) {
            this.start = start;
            this.end = end;
            this.pos = start;
        }
    }

    /**
     * the resource cannot (any longer) be downloaded by ranges
     */
    static class FallbackException extends IOException {
        FallbackException(String message) {
            super(message);
        }
    }

    private static class ChunkDownloader {
        final URL url;
        final String validator;
        final String etag;
        final long length;
        final Options options;
        final FileChannel channel;
        final AtomicLong done;
        final AtomicBoolean cancelled;

        ChunkDownloader(URL url, String validator, String etag, long length, Options options, FileChannel channel, AtomicLong done, AtomicBoolean cancelled) {
            this.url = url;
            this.validator = validator;
            this.etag = etag;
            this.length = length;
            this.options = options;
            this.channel = channel;
            this.done = done;
            this.cancelled = cancelled;
        }

        void download(Chunk chunk, HttpURLConnection opened) throws IOException {
            int attempt = 0;
            while (true) {
                HttpURLConnection c = opened;
                opened = null;
                try {
                    if (c == null) {
                        c = request(chunk.pos, chunk.end);
                    }
                    transfer(c, chunk);
                    if (chunk.pos > chunk.end) {
                        return;
                    }
                    throw new MiddleTransferException("connection closed at " + chunk.pos + " in range " + chunk.start + "-" + chunk.end);
                } catch (FallbackException ex) {
                    throw ex;
                } catch (IOException ex) {
                    // socket failures, timeouts, premature EOF, 5xx : retry from the last received byte
                    if (cancelled.get() || ++attempt > options.chunkRetries) {
                        throw ex;
                    }
                } finally {
                    if (c != null) {
                        c.disconnect();
                    }
                }
            }
        }

        HttpURLConnection request(long from, long to) throws IOException {
            HttpURLConnection c = open(url, options);
            c.setRequestProperty("Range", "bytes=" + from + "-" + to);
            if (validator != null) {
                c.setRequestProperty("If-Range", validator);
            }
            int code = c.getResponseCode();
            if (code == HttpURLConnection.HTTP_PARTIAL) {
                long[] r = parseContentRange(c.getHeaderField("Content-Range"));
                if (r == null || r[0] != from || r[1] > to || (r[2] >= 0 && r[2] != length)) {
                    c.disconnect();
                    throw new FallbackException("unexpected Content-Range " + c.getHeaderField("Content-Range") + " for bytes=" + from + "-" + to);
                }
                String e = c.getHeaderField("ETag");
                if (etag != null && e != null && !etag.equals(e)) {
                    c.disconnect();
                    throw new FallbackException("resource changed while downloading (ETag " + etag + " -> " + e + ")");
                }
                return c;
            }
            c.disconnect();
            if (code >= 500) {
                throw new ServerException("http " + code + " for bytes=" + from + "-" + to);
            }
            // 200 : range ignored or resource changed (If-Range); 403/410 : signed url expired; ...
            throw new FallbackException("http " + code + " for bytes=" + from + "-" + to);
        }

        /**
         * writes the response body at {@code chunk.pos}, advancing it
         */
        void transfer(HttpURLConnection c, Chunk chunk) throws IOException {
            byte[] buffer = new byte[BUFFER_SIZE];
            try (InputStream in = c.getInputStream()) {
                while (chunk.pos <= chunk.end) {
                    if (cancelled.get()) {
                        throw new InterruptedIOException("cancelled");
                    }
                    int max = (int) Math.min(buffer.length, chunk.end - chunk.pos + 1);
                    int n = in.read(buffer, 0, max);
                    if (n < 0) {
                        break;
                    }
                    ByteBuffer bb = ByteBuffer.wrap(buffer, 0, n);
                    long p = chunk.pos;
                    while (bb.hasRemaining()) {
                        p += channel.write(bb, p);
                    }
                    chunk.pos += n;
                    done.addAndGet(n);
                }
            }
        }
    }

    /**
     * 5xx answer to a range request, worth a retry
     */
    private static class ServerException extends IOException {
        ServerException(String message) {
            super(message);
        }
    }
}
