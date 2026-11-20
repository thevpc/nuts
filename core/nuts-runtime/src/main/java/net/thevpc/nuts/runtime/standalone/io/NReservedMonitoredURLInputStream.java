package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.runtime.standalone.io.util.NSecretRedactor;
import net.thevpc.nuts.runtime.standalone.xtra.web.DefaultNHttpClient;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.mon.NChronometer;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.text.NMsg;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;

public class NReservedMonitoredURLInputStream extends FilterInputStream {
    public static final int M = 1024 * 1024;
    private NChronometer chronometer;
    private final long contentLength;
    private final NLog log;
    private final URL url;
    private final String displayUrl;
    long lastSec = -1;
    long readCount = 0;
    boolean preDestroyed = false;

    private NReservedMonitoredURLInputStream(InputStream in, URL url, NChronometer chronometer, long contentLength, NLog log) {
        super(in);
        this.chronometer = chronometer;
        this.url = url;
        this.displayUrl = NSecretRedactor.redact(url == null ? null : url.toString());
        this.contentLength = contentLength;
        this.log = log;
    }

    public static NReservedMonitoredURLInputStream of(URL url, NLog log) {
        String displayUrl = NSecretRedactor.redact(url == null ? null : url.toString());
        if (log != null) {
            log.log(NMsg.ofC("download %s", displayUrl).asFinest().withIntent(NMsgIntent.START));
        }
        NChronometer chronometer = NChronometer.of();
        URLConnection c = null;
        try {
            c = url.openConnection();
        } catch (IOException ex) {
            if (log != null) {
                log.log(NMsg.ofC("failed to download %s", displayUrl).asFinestFail());
            }
            throw new UncheckedIOException("url not accessible " + displayUrl, ex);
        }
        DefaultNHttpClient.prepareGlobalConnection(c);
        long contentLength = c.getContentLengthLong();
        try {
            return new NReservedMonitoredURLInputStream(c.getInputStream(), url, chronometer, contentLength, log);
        } catch (IOException ex) {
            if (log != null) {
                log.log(NMsg.ofC("failed to download %s", displayUrl));
            }
            throw new UncheckedIOException("url not accessible " + displayUrl, ex);
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
        try {
            preDestroy();
        } finally {
            // closing must always release the underlying connection, even if
            // recording the download outcome fails
            super.close();
        }
    }

    private void preDestroy() {
        if (!preDestroyed) {
            preDestroyed = true;
            chronometer.stop();
            doLog(true);
            if (log != null) {
                if (contentLength >= 0 && readCount != contentLength) {
                    log.log(NMsg.ofC("failed to downloaded %s. stream closed unexpectedly", displayUrl).asFineFail());
                } else {
                    log.log(NMsg.ofC("successfully downloaded %s", displayUrl).asFine().withIntent(NMsgIntent.SUCCESS));
                }
            }
        }
    }

    private void doLog(boolean force) {
        long sec = chronometer.duration().toSeconds();
        if (sec != lastSec || force) {
            lastSec = sec;
        } else {
            return;
        }
        if (sec == 0) {
            if (contentLength <= 0) {
                String v = formatSize(readCount) + "/s";
                if (log != null) {
                    log.log(NMsg.ofC("%-8s %s/s", v, displayUrl).withLevel(Level.FINE).withIntent(NMsgIntent.READ));
                }
            } else {
                float f = (float) (((double) readCount / (double) contentLength) * 100);
                String v = formatSize(readCount) + "/s";
                if (log != null) {
                    log.log(NMsg.ofC("%.2f%% %-8s %s", f, v, displayUrl).withLevel(Level.FINE).withIntent(NMsgIntent.READ));
                }
            }
        } else {
            if (contentLength <= 0) {
                String v = formatSize(readCount / sec) + "/s";
                if (log != null) {
                    log.log(NMsg.ofC("%-8s %s", v, displayUrl).withLevel(Level.FINE).withIntent(NMsgIntent.READ));
                }
            } else {
                float f = (float) (((double) readCount / (double) contentLength) * 100);
                String v = formatSize(readCount / sec) + "/s";
                if (log != null) {
                    log.log(NMsg.ofC("%.2f%% %-8s %s", f, v, displayUrl).withLevel(Level.FINE).withIntent(NMsgIntent.READ));
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
