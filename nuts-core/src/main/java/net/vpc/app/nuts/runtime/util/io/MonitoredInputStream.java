/**
 * ====================================================================
 * vpc-common-io : common reusable library for
 * input/output
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <br>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <br>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime.util.io;

import net.vpc.app.nuts.NutsProgressMonitor;
import net.vpc.app.nuts.NutsSession;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author vpc
 */
public class MonitoredInputStream extends InputStream implements InputStreamMetadataAware ,Interruptible{

    private final InputStream base;
    private long count;
    private long lastCount;
    private long startTime;
    private long lastTime;
    private final long length;
    private final NutsProgressMonitor monitor;
    private final Object source;
    private final String sourceName;
    private boolean completed = false;
    private boolean interrupted = false;
    private NutsSession session;

    public MonitoredInputStream(InputStream base, Object source, String sourceName, long length, NutsProgressMonitor monitor, NutsSession session) {
        this.base = base;
        this.session = session;
        if (monitor == null) {
            throw new NullPointerException();
        }
        this.monitor = monitor;
        this.source = source;
        this.sourceName = sourceName;
        this.length = length;
    }

    @Override
    public void interrupt() throws InterruptException {
        interrupted=true;
    }

    @Override
    public int read() throws IOException {
        if(interrupted){
            throw new IOException(new InterruptException("Interrupted"));
        }
        try {
            onBeforeRead();
            int r = this.base.read();
            if (r != -1) {
                onAfterRead(1);
            } else {
                onComplete(null);
            }
            return r;
        } catch (IOException ex) {
            onComplete(ex);
            throw ex;
        }
    }

    @Override
    public boolean markSupported() {
        return base.markSupported();
    }

    @Override
    public synchronized void reset() throws IOException {
        try {
            base.reset();
        } catch (IOException ex) {
            onComplete(ex);
            throw ex;
        }
    }

    @Override
    public synchronized void mark(int readlimit) {
        base.mark(readlimit);
    }

    @Override
    public void close() throws IOException {
        onComplete(null);
        base.close();
    }

    @Override
    public int available() throws IOException {
        try {
            if(interrupted){
                throw new IOException(new InterruptException("Interrupted"));
            }
            return base.available();
        } catch (IOException ex) {
            onComplete(ex);
            throw ex;
        }
    }

    @Override
    public long skip(long n) throws IOException {
        try {
            if(interrupted){
                throw new IOException(new InterruptException("Interrupted"));
            }
            onBeforeRead();
            long r = base.skip(n);
            onAfterRead(r);
            return r;
        } catch (IOException ex) {
            onComplete(ex);
            throw ex;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        try {
            if(interrupted){
                throw new IOException(new InterruptException("Interrupted"));
            }
            onBeforeRead();
            int r = base.read(b, off, len);
            onAfterRead(r);
            return r;
        } catch (IOException ex) {
            onComplete(ex);
            throw ex;
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        try {
            if(interrupted){
                throw new IOException(new InterruptException("Interrupted"));
            }
            onBeforeRead();
            int r = base.read(b);
            onAfterRead(r);
            return r;
        } catch (IOException ex) {
            onComplete(ex);
            throw ex;
        }
    }

    private void onBeforeRead() {
        if (!completed) {
            if (startTime == 0) {
                long now = System.currentTimeMillis();
                this.startTime = now;
                this.lastTime = now;
                this.lastCount = 0;
                this.count = 0;
                monitor.onStart(new DefaultNutsProgressEvent(source, sourceName, 0, 0, 0, 0, length, null,session,length<0));
            }
        }
    }

    private void onAfterRead(long count) {
        if (!completed) {
            long now = System.currentTimeMillis();
            this.count += count;
            if (monitor.onProgress(new DefaultNutsProgressEvent(source, sourceName, this.count, now - startTime, this.count - lastCount, now - lastTime, length, null,session,length<0))) {
                this.lastCount = this.count;
                this.lastTime = now;
            }
        }
    }

    private void onComplete(IOException ex) {
        if (!completed) {
            completed = true;
            long now = System.currentTimeMillis();
            monitor.onComplete(new DefaultNutsProgressEvent(source, sourceName, this.count, now - startTime, this.count - lastCount, now - lastTime, length, ex,session,length<0));
        }
    }

    @Override
    public InputStreamMetadata getMetaData() {
        return new FixedInputStreamMetadata(String.valueOf(sourceName), length);
    }

    @Override
    public String toString() {
        return String.valueOf(sourceName);
    }
}
