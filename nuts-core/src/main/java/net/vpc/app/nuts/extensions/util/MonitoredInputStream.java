/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.util;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author vpc
 */
public class MonitoredInputStream extends InputStream {

    private final InputStream base;
    private long count;
    private long lastCount;
    private long startTime;
    private long lastTime;
    private final long length;
    private final InputStreamMonitor monitor;
    private final Object source;
    private final String sourceName;

    public MonitoredInputStream(InputStream base, Object source, String sourceName, long length, InputStreamMonitor monitor) {
        this.base = base;
        if (monitor == null) {
            throw new NullPointerException();
        }
        this.monitor = monitor;
        this.source = source;
        this.sourceName = sourceName;
        this.length = length;
    }

    @Override
    public int read() throws IOException {
        int r = this.base.read();
        if (r != 0) {
            onRead(1);
        }
        return r;
    }

    @Override
    public boolean markSupported() {
        return base.markSupported();
    }

    @Override
    public synchronized void reset() throws IOException {
        base.reset();
    }

    @Override
    public synchronized void mark(int readlimit) {
        base.mark(readlimit);
    }

    @Override
    public void close() throws IOException {
        base.close();
    }

    @Override
    public int available() throws IOException {
        return base.available();
    }

    @Override
    public long skip(long n) throws IOException {
        long r = base.skip(n);
        onRead(r);
        return r;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int r = base.read(b, off, len);
        onRead(r);
        return r;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int r = base.read(b);
        onRead(r);
        return r;
    }

    private void onRead(long count) {
        long now = System.nanoTime();
        if (startTime == 0) {
            this.startTime = now;
            this.lastTime = now;
            this.lastCount = now;
            this.lastCount = 0;
            this.count = count;
        } else {
            this.count += count;
            if (monitor.onProgress(new InputStreamEvent(source, sourceName, this.count, now - startTime, this.count - lastCount, now - lastTime, length))) {
                this.lastCount = this.count;
                this.lastTime = now;
            }
        }
    }

}
