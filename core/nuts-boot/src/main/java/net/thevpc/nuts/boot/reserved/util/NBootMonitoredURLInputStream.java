package net.thevpc.nuts.boot.reserved.util;

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
    private NBootChronometer chronometer;
    private final long contentLength;
    private final NBootLog log;
    private final URL url;
    long lastSec = -1;
    long readCount = 0;
    boolean preDestroyed = false;

    private NBootMonitoredURLInputStream(InputStream in, URL url, NBootChronometer chronometer, long contentLength, NBootLog log) {
        super(in);
        this.chronometer = chronometer;
        this.url = url;
        this.contentLength = contentLength;
        this.log = log;
    }

    public static NBootMonitoredURLInputStream of(URL url, NBootLog log) {
        if (log != null) {
            log.with().level(Level.FINE).verbStart().log(NBootMsg.ofC("download %s", url));
        }
        NBootChronometer chronometer = NBootChronometer.startNow();
        URLConnection c = null;
        try {
            c = url.openConnection();
        } catch (IOException ex) {
            if (log != null) {
                log.with().level(Level.FINE).verbFail().log(NBootMsg.ofC("failed to download %s", url));
            }
            throw new UncheckedIOException("url not accessible " + url, ex);
        }
        long contentLength = c.getContentLengthLong();
        try {
            return new NBootMonitoredURLInputStream(c.getInputStream(), url, chronometer, contentLength, log);
        } catch (IOException ex) {
            if (log != null) {
                log.with().level(Level.FINE).verbFail().log(NBootMsg.ofC("failed to download %s", url));
            }
            throw new UncheckedIOException("url not accessible " + url, ex);
        }
    }

    @Override
    public int read() throws IOException {
        int r = super.read();
        if (r >= 0) {
            readCount++;
        } else {
            preDestroy();
        }
        return r;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int t = super.read(b, off, len);
        if (t > 0) {
            readCount += t;
            doLog(false);
        } else {
            preDestroy();
        }
        return t;
    }

    @Override
    public long skip(long n) throws IOException {
        long t = super.skip(n);
        if (t > 0) {
            readCount += t;
            doLog(false);
        } else {
            preDestroy();
        }
        return t;
    }

    @Override
    public void close() throws IOException {
        preDestroy();
        super.close();
    }

    private void preDestroy() {
        if (!preDestroyed) {
            preDestroyed = true;
            chronometer.stop();
            doLog(true);
            if (contentLength >= 0) {
                if (readCount != contentLength) {
                    log.with().level(Level.FINE).verbFail().log(NBootMsg.ofC("failed to downloaded %s. stream closed unexpectedly", url));
                    throw new NBootException(NBootMsg.ofC("failed to downloaded %s. stream closed unexpectedly", url));
                }
            }
            if (log != null) {
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
