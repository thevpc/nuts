/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.io.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.format.NFormat;
import net.thevpc.nuts.format.NFormattable;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.time.NProgressEvent;
import net.thevpc.nuts.time.NProgressListener;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author thevpc
 */
public class InputStreamExt extends InputStream implements NInterruptible<InputStream>, NFormattable, NContentMetadataProvider {

    private InputStream base;
    private NContentMetadata md;
    private NWorkspace workspace;

    //
    private Runnable onClose;

    //
    private Long length;
    private final NProgressListener monitor;
    private final Object source;
    private NMsg sourceName;
    private long count;
    private long lastCount;
    private long startTime;
    private long lastTime;
    private boolean completed = false;

    //
    private boolean interrupted;
    private boolean closeBase;

    public InputStreamExt(InputStream base,
                          NContentMetadata md0,
                          boolean closeBase, Runnable onClose,
                          NProgressListener monitor,
                          Object source,
                          NMsg sourceName,
                          Long length,
                          NWorkspace workspace) {
        this.base = base;
        this.closeBase = closeBase;
        this.workspace = workspace;
        this.onClose = onClose;
        this.md = CoreIOUtils.createContentMetadata(md0, base);
        this.monitor = monitor;
        this.source = source;

        if (length == null || length < 0) {
            Long len = this.md.getContentLength().orElse(null);
            if (len != null) {
                long l = len;
                if (l >= 0) {
                    length = l;
                }
            }
        }
        this.length = length;
        if (sourceName == null) {
            NMsg m2 = this.md.getMessage().orElse(null);
            if (m2 != null) {
                sourceName = m2;
            }
        }
        if (sourceName == null) {
            String m2 = this.md.getName().orElse(null);
            if (m2 != null) {
                sourceName = NMsg.ofPlain(m2);
            }
        }
        this.sourceName = sourceName;
    }

    @Override
    public InputStream base() {
        return this;
    }

    @Override
    public void interrupt() throws NInterruptException {
        this.interrupted = true;
        if (base instanceof NInterruptible) {
            ((NInterruptible) base).interrupt();
        }
    }

    @Override
    public int read() throws IOException {
        checkInterrupted();
        if (monitor != null) {
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
        } else {
            return base.read();
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        checkInterrupted();
        if (monitor != null) {
            try {
                onBeforeRead();
                int r = base.read(b);
                onAfterRead(r);
                return r;
            } catch (IOException ex) {
                onComplete(ex);
                throw ex;
            }
        } else {
            return base.read(b);
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        checkInterrupted();
        if (monitor != null) {
            try {
                onBeforeRead();
                int r = base.read(b, off, len);
                onAfterRead(r);
                return r;
            } catch (IOException ex) {
                onComplete(ex);
                throw ex;
            }
        } else {
            return base.read(b, off, len);
        }
    }

    @Override
    public long skip(long n) throws IOException {
        checkInterrupted();
        if (monitor != null) {
            try {
                onBeforeRead();
                long r = base.skip(n);
                onAfterRead(r);
                return r;
            } catch (IOException ex) {
                onComplete(ex);
                throw ex;
            }
        }else{
            return base.skip(n);
        }
    }

    @Override
    public int available() throws IOException {
        checkInterrupted();
        if(monitor!=null) {
            try {
                return base.available();
            } catch (IOException ex) {
                onComplete(ex);
                throw ex;
            }
        }else{
            return base.available();
        }
    }

    @Override
    public void close() {
        if(monitor!=null) {
            onComplete(null);
        }
        if(closeBase) {
            try {
                base.close();
            } catch (IOException e) {
                throw new NIOException(NMsg.ofPlain("error closing base stream"), e);
            }
        }
        if (onClose != null) {
            onClose.run();
        }
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
            if(monitor!=null) {
                onComplete(ex);
            }
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
                monitor.onProgress(NProgressEvent.ofStart(source, sourceName,
                        length==null?-1:length
                ));
            }
        }
    }

    private void onAfterRead(long count) {
        if (!completed) {
            long now = System.nanoTime();
            this.count += count;
            if (monitor.onProgress(NProgressEvent.ofProgress(source, sourceName,
                    this.count, now - startTime, null,
                    this.count - lastCount, now - lastTime,
                    length==null?-1:length,
                    null))) {
                this.lastCount = this.count;
                this.lastTime = now;
            }
        }
    }

    private void onComplete(IOException ex) {
        if (!completed) {
            completed = true;
            long now = System.nanoTime();
            monitor.onProgress(NProgressEvent.ofComplete(source, sourceName,
                    this.count, now - startTime, null,
                    this.count - lastCount, now - lastTime,
                    length==null?-1:length,
                    ex));
        }
    }

    private void checkInterrupted() {
        if (interrupted) {
            throw new NIOException(NMsg.ofPlain("stream is interrupted"));
        }
    }

    @Override
    public NContentMetadata getMetaData() {
        return md;
    }


    @Override
    public NFormat formatter() {
        return NFormat.of(new NContentMetadataProviderFormatSPI(this, sourceName,"input-stream"));
    }

    @Override
    public String toString() {
        NPlainPrintStream out = new NPlainPrintStream();
        NOptional<NMsg> m = getMetaData().getMessage();
        if (m.isPresent()) {
            out.print(m.get());
        } else if (sourceName != null) {
            out.print(sourceName, NTextStyle.path());
        } else {
            out.print(getClass().getSimpleName(), NTextStyle.path());
        }
        return out.toString();
    }

}
