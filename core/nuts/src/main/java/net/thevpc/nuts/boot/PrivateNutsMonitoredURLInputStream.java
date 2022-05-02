package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsBootException;
import net.thevpc.nuts.util.NutsLoggerVerb;
import net.thevpc.nuts.NutsMessage;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;

public class PrivateNutsMonitoredURLInputStream extends FilterInputStream {
    public static final int M = 1024 * 1024;
    private final long startTime;
    private final long contentLength;
    private final PrivateNutsBootLog log;
    private final URL url;
    long endTime;
    long lastSec = -1;
    long readCount = 0;
    boolean preDestroyed = false;

    private PrivateNutsMonitoredURLInputStream(InputStream in, URL url, long startTime, long contentLength, PrivateNutsBootLog log) {
        super(in);
        this.startTime = startTime;
        this.url = url;
        this.contentLength = contentLength;
        this.log = log;
    }

    public static PrivateNutsMonitoredURLInputStream of(URL url, PrivateNutsBootLog log) {
        if (log != null) {
            log.log(Level.FINE, NutsLoggerVerb.START, NutsMessage.jstyle("download {0}", url));
        }
        long startTime = System.currentTimeMillis();
        URLConnection c = null;
        try {
            c = url.openConnection();
        } catch (IOException ex) {
            if (log != null) {
                log.log(Level.FINE, NutsLoggerVerb.FAIL, NutsMessage.jstyle("failed to download {0}", url));
            }
            throw new NutsBootException(NutsMessage.cstyle("url not accessible %s", url), ex);
        }
        long contentLength = c.getContentLengthLong();
        try {
            return new PrivateNutsMonitoredURLInputStream(c.getInputStream(), url, startTime, contentLength, log);
        } catch (IOException ex) {
            if (log != null) {
                log.log(Level.FINE, NutsLoggerVerb.FAIL, NutsMessage.jstyle("failed to download {0}", url));
            }
            throw new NutsBootException(NutsMessage.cstyle("url not accessible %s", url), ex);
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
            endTime = System.currentTimeMillis();
            doLog(true);
            if(contentLength>=0) {
                if (readCount != contentLength) {
                    log.log(Level.FINE, NutsLoggerVerb.FAIL, NutsMessage.jstyle("failed to downloaded {0}. stream closed unexpectedly", url));
                    throw new NutsBootException(NutsMessage.jstyle("failed to downloaded {0}. stream closed unexpectedly", url));
                }
            }
            if (log != null) {
                log.log(Level.FINE, NutsLoggerVerb.SUCCESS, NutsMessage.jstyle("successfully downloaded {0}", url));
            }
        }
    }

    private void doLog(boolean force) {
        long sec = (System.currentTimeMillis() - startTime) / 1000;
        if (sec != lastSec || force) {
            lastSec = sec;
        }else{
            return;
        }
        if (sec == 0) {
            if (contentLength <= 0) {
                String v = formatSize(readCount)+"/s";
                if (log != null) {
                    log.log(Level.FINE, NutsLoggerVerb.READ, NutsMessage.cstyle("%-8s %s/s", v, url));
                }
            } else {
                float f = (float) (((double) readCount / (double) contentLength) * 100);
                String v = formatSize(readCount)+"/s";
                if (log != null) {
                    log.log(Level.FINE, NutsLoggerVerb.READ, NutsMessage.cstyle("%.2f%% %-8s %s", f, v, url));
                }
            }
        } else {
            if (contentLength <= 0) {
                String v = formatSize(readCount / sec)+"/s";
                if (log != null) {
                    log.log(Level.FINE, NutsLoggerVerb.READ, NutsMessage.cstyle("%-8s %s", v, url));
                }
            } else {
                float f = (float) (((double) readCount / (double) contentLength) * 100);
                String v = formatSize(readCount / sec)+"/s";
                if (log != null) {
                    log.log(Level.FINE, NutsLoggerVerb.READ, NutsMessage.cstyle("%.2f%% %-8s %s", f, v, url));
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
        return s+"B";
    }
}
