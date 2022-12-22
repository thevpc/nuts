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
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.util.InterruptException;
import net.thevpc.nuts.runtime.standalone.io.util.Interruptible;
import net.thevpc.nuts.spi.NutsFormatSPI;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.util.NutsProgressEvent;
import net.thevpc.nuts.util.NutsProgressListener;
import net.thevpc.nuts.io.NutsPlainPrintStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author thevpc
 */
public class MonitoredInputStream extends InputStream implements NutsInputSource, Interruptible {

    private final InputStream base;
    private final long length;
    private final NutsProgressListener monitor;
    private final Object source;
    private final NutsMessage sourceName;
    private long count;
    private long lastCount;
    private long startTime;
    private long lastTime;
    private boolean completed = false;
    private boolean interrupted = false;
    private final NutsSession session;
    private DefaultNutsInputSourceMetadata md;

    public MonitoredInputStream(InputStream base, Object source, NutsMessage sourceName, long length, NutsProgressListener monitor, NutsSession session) {
        this.base = (InputStream) NutsIO.of(session).createInputSource(base);
        this.session = session;
        if (monitor == null) {
            throw new NullPointerException();
        }
        this.monitor = monitor;
        this.source = source;
        this.sourceName = sourceName;
        this.length = length;
        this.md = new DefaultNutsInputSourceMetadata(((NutsInputSource) base).getInputMetaData());
    }

    @Override
    public void interrupt() throws InterruptException {
        interrupted = true;
    }

    @Override
    public int read() throws IOException {
        if (interrupted) {
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
    public int read(byte[] b) throws IOException {
        try {
            if (interrupted) {
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

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        try {
            if (interrupted) {
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
    public long skip(long n) throws IOException {
        try {
            if (interrupted) {
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
    public int available() throws IOException {
        try {
            if (interrupted) {
                throw new IOException(new InterruptException("Interrupted"));
            }
            return base.available();
        } catch (IOException ex) {
            onComplete(ex);
            throw ex;
        }
    }

    @Override
    public void close() throws IOException {
        onComplete(null);
        base.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        base.mark(readlimit);
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
    public boolean markSupported() {
        return base.markSupported();
    }

    private void onBeforeRead() {
        if (!completed) {
            if (startTime == 0) {
                long now = System.nanoTime();
                this.startTime = now;
                this.lastTime = now;
                this.lastCount = 0;
                this.count = 0;
                monitor.onProgress(NutsProgressEvent.ofStart(source, sourceName, length, session));
            }
        }
    }

    private void onAfterRead(long count) {
        if (!completed) {
            long now = System.nanoTime();
            this.count += count;
            if (monitor.onProgress(NutsProgressEvent.ofProgress(source, sourceName,
                    this.count, now - startTime, null,
                    this.count - lastCount, now - lastTime,
                    length, null, session))) {
                this.lastCount = this.count;
                this.lastTime = now;
            }
        }
    }

    private void onComplete(IOException ex) {
        if (!completed) {
            completed = true;
            long now = System.nanoTime();
            monitor.onProgress(NutsProgressEvent.ofComplete(source, sourceName,
                    this.count, now - startTime, null,
                    this.count - lastCount, now - lastTime,
                    length, ex, session));
        }
    }

    public NutsInputSourceMetadata getInputMetaData() {
        return md;
    }

    @Override
    public InputStream getInputStream() {
        return this;
    }

    public boolean isMultiRead() {
        return false;
    }

    @Override
    public NutsFormat formatter(NutsSession session) {
        return NutsFormat.of(session!=null?session:this.session, new NutsFormatSPI() {
            @Override
            public String getName() {
                return "input-stream";
            }

            @Override
            public void print(NutsPrintStream out) {
                NutsOptional<NutsMessage> m = getInputMetaData().getMessage();
                if (m.isPresent()) {
                    out.print(m.get());
                } else if (sourceName != null) {
                    out.append(sourceName, NutsTextStyle.path());
                } else {
                    out.append(getClass().getSimpleName(), NutsTextStyle.path());
                }
            }

            @Override
            public boolean configureFirst(NutsCommandLine commandLine) {
                return false;
            }
        });
    }

    @Override
    public String toString() {
        NutsPlainPrintStream out = new NutsPlainPrintStream();
        NutsOptional<NutsMessage> m = getInputMetaData().getMessage();
        if (m.isPresent()) {
            out.print(m.get());
        } else if (sourceName != null) {
            out.append(sourceName, NutsTextStyle.path());
        } else {
            out.append(getClass().getSimpleName(), NutsTextStyle.path());
        }
        return out.toString();
    }

}
