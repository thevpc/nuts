package net.thevpc.nuts.reserved;

import net.thevpc.nuts.NutsBootException;
import net.thevpc.nuts.util.NutsChronometer;
import net.thevpc.nuts.util.NutsLoggerVerb;
import net.thevpc.nuts.NutsMessage;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;

public class NutsReservedMonitoredURLInputStream extends FilterInputStream {
    public static final int M = 1024 * 1024;
    private NutsChronometer chronometer;
    private final long contentLength;
    private final NutsReservedBootLog log;
    private final URL url;
    long lastSec = -1;
    long readCount = 0;
    boolean preDestroyed = false;

    private NutsReservedMonitoredURLInputStream(InputStream in, URL url, NutsChronometer chronometer, long contentLength, NutsReservedBootLog log) {
        super(in);
        this.chronometer = chronometer;
        this.url = url;
        this.contentLength = contentLength;
        this.log = log;
    }

    public static NutsReservedMonitoredURLInputStream of(URL url, NutsReservedBootLog log) {
        if (log != null) {
            log.log(Level.FINE, NutsLoggerVerb.START, NutsMessage.ofJstyle("download {0}", url));
        }
        NutsChronometer chronometer = NutsChronometer.startNow();
        URLConnection c = null;
        try {
            c = url.openConnection();
        } catch (IOException ex) {
            if (log != null) {
                log.log(Level.FINE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("failed to download {0}", url));
            }
            throw new NutsBootException(NutsMessage.ofCstyle("url not accessible %s", url), ex);
        }
        long contentLength = c.getContentLengthLong();
        try {
            return new NutsReservedMonitoredURLInputStream(c.getInputStream(), url, chronometer, contentLength, log);
        } catch (IOException ex) {
            if (log != null) {
                log.log(Level.FINE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("failed to download {0}", url));
            }
            throw new NutsBootException(NutsMessage.ofCstyle("url not accessible %s", url), ex);
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
                    log.log(Level.FINE, NutsLoggerVerb.FAIL, NutsMessage.ofJstyle("failed to downloaded {0}. stream closed unexpectedly", url));
                    throw new NutsBootException(NutsMessage.ofJstyle("failed to downloaded {0}. stream closed unexpectedly", url));
                }
            }
            if (log != null) {
                log.log(Level.FINE, NutsLoggerVerb.SUCCESS, NutsMessage.ofJstyle("successfully downloaded {0}", url));
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
                    log.log(Level.FINE, NutsLoggerVerb.READ, NutsMessage.ofCstyle("%-8s %s/s", v, url));
                }
            } else {
                float f = (float) (((double) readCount / (double) contentLength) * 100);
                String v = formatSize(readCount) + "/s";
                if (log != null) {
                    log.log(Level.FINE, NutsLoggerVerb.READ, NutsMessage.ofCstyle("%.2f%% %-8s %s", f, v, url));
                }
            }
        } else {
            if (contentLength <= 0) {
                String v = formatSize(readCount / sec) + "/s";
                if (log != null) {
                    log.log(Level.FINE, NutsLoggerVerb.READ, NutsMessage.ofCstyle("%-8s %s", v, url));
                }
            } else {
                float f = (float) (((double) readCount / (double) contentLength) * 100);
                String v = formatSize(readCount / sec) + "/s";
                if (log != null) {
                    log.log(Level.FINE, NutsLoggerVerb.READ, NutsMessage.ofCstyle("%.2f%% %-8s %s", f, v, url));
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
