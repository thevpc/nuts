package net.thevpc.nuts.boot.internal.util;

import net.thevpc.nuts.boot.NBootException;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;

public class NBootMonitoredURLInputStream extends FilterInputStream {
    public static final int M = 1024 * 1024;
    /**
     * timeout used to establish the connection (milliseconds)
     */
    public static final int DEFAULT_CONNECT_TIMEOUT = 3000;
    /**
     * timeout used while waiting for data (milliseconds). A download may
     * legitimately pause longer than a connection is allowed to be established,
     * so the two timeouts must not share the same (short) value.
     */
    public static final int DEFAULT_READ_TIMEOUT = 30000;
    private NBootChronometer chronometer;
    private final long contentLength;
    private final URL url;
    long lastSec = -1;
    long readCount = 0;
    boolean finished = false;

    private NBootMonitoredURLInputStream(InputStream in, URL url, NBootChronometer chronometer, long contentLength) {
        super(in);
        this.chronometer = chronometer;
        this.url = url;
        this.contentLength = contentLength;
    }

    public static NBootMonitoredURLInputStream of(URL url) {
        NBootLog log = NBootContext.log();
        if (log != null) {
            log.with().level(Level.FINE).verbStart().log(NBootMsg.ofC("download %s", url));
        }
        NBootChronometer chronometer = NBootChronometer.startNow();
        URLConnection c = null;
        try {
            c = url.openConnection();
        } catch (IOException ex) {
            if (log != null) {
                log.with().level(Level.FINE).verbFail().log(NBootMsg.ofC("failed to download %s : open connection leads to %s", url, ex));
            }
            throw new UncheckedIOException("url not accessible " + url, ex);
        }
        Integer bootConnectionTimout = null;
        Integer bootReadTimout = null;
        if (NBootContext.context() != null) {
            bootConnectionTimout = NBootContext.context().connectionTimout;
            bootReadTimout = NBootContext.context().connectionReadTimeout;
        }
        if (bootConnectionTimout == null) {
            bootConnectionTimout = DEFAULT_CONNECT_TIMEOUT;
        }
        if (bootReadTimout == null) {
            bootReadTimout = DEFAULT_READ_TIMEOUT;
        }
        bootConnectionTimout = Math.max(0, bootConnectionTimout);
        bootReadTimout = Math.max(0, bootReadTimout);
        c.setConnectTimeout(bootConnectionTimout);
        c.setReadTimeout(bootReadTimout);
        long contentLength = c.getContentLengthLong();
        try {
            return new NBootMonitoredURLInputStream(c.getInputStream(), url, chronometer, contentLength);
        } catch (IOException ex) {
            if (log != null) {
                log.with().level(Level.FINE).verbFail().log(NBootMsg.ofC("failed to download %s : %s", url, ex));
            }
            throw new UncheckedIOException("url not accessible " + url, ex);
        }
    }

    public long getContentLength() {
        return contentLength;
    }

    public long getReadCount() {
        return readCount;
    }

    /**
     * @return true when the whole announced content has been read (or the
     * content length is unknown).
     */
    public boolean isComplete() {
        return contentLength < 0 || readCount == contentLength;
    }

    @Override
    public int read() throws IOException {
        int r = super.read();
        if (r >= 0) {
            readCount++;
        } else {
            completed();
        }
        return r;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int t = super.read(b, off, len);
        if (t > 0) {
            readCount += t;
            doLog(false);
        } else if (t < 0) {
            completed();
        }
        return t;
    }

    @Override
    public long skip(long n) throws IOException {
        long t = super.skip(n);
        if (t > 0) {
            readCount += t;
            doLog(false);
        }
        return t;
    }

    @Override
    public void close() throws IOException {
        try {
            finished(true);
        } finally {
            super.close();
        }
    }

    private void completed() {
        finished(false);
    }

    private void finished(boolean closing) {
        if (!finished) {
            finished = true;
            chronometer.stop();
            doLog(true);
            NBootLog log = NBootContext.log();
            if (contentLength >= 0 && readCount != contentLength) {
                if (log != null) {
                    log.with().level(Level.FINE).verbFail().log(NBootMsg.ofC("failed to downloaded %s. stream closed unexpectedly", url));
                }
                // closing a stream before its end is a legitimate operation (the
                // caller may only need a prefix). Only a read that reaches EOF
                // earlier than announced is an actual download failure.
                if (!closing) {
                    throw new NBootException(NBootMsg.ofC("failed to downloaded %s. stream closed unexpectedly", url));
                }
            } else if (log != null) {
                log.with().level(Level.FINE).verbSuccess().log(NBootMsg.ofC("successfully downloaded %s", url));
            }
        }
    }

    private void doLog(boolean force) {
        long sec = chronometer.getDuration().getTimeAsSeconds();
        if (sec != lastSec || force) {
            lastSec = sec;
        } else {
            return;
        }
        NBootLog log = NBootContext.log();
        if (sec == 0) {
            if (contentLength <= 0) {
                String v = formatSize(readCount) + "/s";
                if (log != null) {
                    log.with().level(Level.FINE).verbRead().log(NBootMsg.ofC("%-8s %s/s", v, url));
                }
            } else {
                float f = (float) (((double) readCount / (double) contentLength) * 100);
                String v = formatSize(readCount) + "/s";
                if (log != null) {
                    log.with().level(Level.FINE).verbRead().log(NBootMsg.ofC("%.2f%% %-8s %s", f, v, url));
                }
            }
        } else {
            if (contentLength <= 0) {
                String v = formatSize(readCount / sec) + "/s";
                if (log != null) {
                    log.with().level(Level.FINE).verbRead().log(NBootMsg.ofC("%-8s %s", v, url));
                }
            } else {
                float f = (float) (((double) readCount / (double) contentLength) * 100);
                String v = formatSize(readCount / sec) + "/s";
                if (log != null) {
                    log.with().level(Level.FINE).verbRead().log(NBootMsg.ofC("%.2f%% %-8s %s", f, v, url));
                }
            }
        }
    }

    private String formatSize(long s) {
        if (s > M) {
            long mega = s / (M);
            s %= M;
            if (s == 0) {
                return mega + "M";
            }
            String x = String.valueOf((int) ((((double) s) / M) * 100.0));
            if (x.length() < 2) {
                return mega + ".0" + x + "M";
            }
            return mega + "." + x + "M";
        }
        if (s > 1024) {
            long kilo = s / 1024;
            s %= 1024;
            if (s == 0) {
                return kilo + "K";
            }
            String x = String.valueOf((int) ((((double) s) / 1024) * 100.0));
            if (x.length() < 2) {
                return kilo + ".0" + x + "K";
            }
            return kilo + "." + x + "K";
        }
        return s + "B";
    }
}
