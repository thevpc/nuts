/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.io.util;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.format.NFormat;
import net.thevpc.nuts.format.NFormattable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author thevpc
 */
public class InputStreamTee extends InputStream implements NInterruptible<InputStream>, NFormattable, NContentMetadataProvider {

    private final InputStream in;
    private final OutputStream out;
    private final Runnable onClose;
    private boolean interrupted;
    private NContentMetadata metadata;
    private NWorkspace workspace;

    public InputStreamTee(InputStream in, OutputStream out, Runnable onClose, NContentMetadata metadata, NWorkspace workspace) {
        this.in = in;
        this.out = out;
        this.onClose = onClose;
        this.metadata = CoreIOUtils.createContentMetadata(metadata, in);
        this.workspace = workspace;
    }

    @Override
    public InputStream base() {
        return this;
    }

    @Override
    public NContentMetadata getMetaData() {
        return metadata;
    }

    @Override
    public NFormat formatter() {
        return NFormat.of(new NContentMetadataProviderFormatSPI(this, null, "input-stream-tee"));
    }

    @Override
    public void interrupt() throws NInterruptException {
        this.interrupted = true;
    }

    @Override
    public int read() throws IOException {
        checkInterrupted();
        int x = in.read();
        if (x >= 0) {
            out.write(x);
        }
        return x;
    }

    @Override
    public int read(byte[] b) throws IOException {
        checkInterrupted();
        final int p = in.read(b);
        if (p > 0) {
            out.write(b, 0, p);
        }
        return p;
    }

    @Override
    public long skip(long n) throws IOException {
        long c = 0;
        byte[] bytes = new byte[1024];
        while (c < n) {
            checkInterrupted();
            final int p = in.read(bytes, 0, Math.min((int) (n - c), bytes.length));
            if (p > 0) {
                out.write(bytes, 0, p);
            } else {
                break;
            }
        }
        return c;
    }

    @Override
    public synchronized void mark(int readlimit) {
        checkMark();
        super.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        if (true) {
            return false;
        }
        return in.markSupported();
    }

    @Override
    public synchronized void reset() throws IOException {
        checkMark();
        in.reset();
    }


    @Override
    public void close() throws IOException {
        in.close();
        out.close();
        if (onClose != null) {
            onClose.run();
        }
    }

    @Override
    public int available() throws IOException {
        checkInterrupted();
        return in.available();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        checkInterrupted();
        final int p = in.read(b, off, len);
        if (p > 0) {
            out.write(b, off, p);
        }
        return p;
    }

    private void checkInterrupted() {
        if (interrupted) {
            throw new NIOException(NMsg.ofPlain("stream is interrupted"));
        }
    }

    private void checkMark() {
        throw new NIOException(NMsg.ofPlain("unsupported mark"));
    }

}
