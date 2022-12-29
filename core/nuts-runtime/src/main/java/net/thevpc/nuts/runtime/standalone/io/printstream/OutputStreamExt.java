package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.io.*;

import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamExt extends OutputStream implements NOutputTarget {

    private OutputStream base;
    private DefaultNOutputTargetMetadata md;

    public OutputStreamExt(OutputStream base, NOutputTargetMetadata md0) {
        this.base = base;
        if (base instanceof NOutputTarget) {
            md = new DefaultNOutputTargetMetadata(((NOutputTarget) base).getOutputMetaData());
        } else {
            md = new DefaultNOutputTargetMetadata();
        }


        if (md0 == null) {
            if (base instanceof NOutputTarget) {
                md = new DefaultNOutputTargetMetadata(((NOutputTarget) base).getOutputMetaData());
            } else {
                md = new DefaultNOutputTargetMetadata();
            }
        } else {
            md = new DefaultNOutputTargetMetadata(md0);
            if (base instanceof NOutputTarget) {
                NOutputTargetMetadata md2 = ((NOutputTarget) base).getOutputMetaData();
                if (md.getMessage().isNotPresent()) {
                    md.setMessage(md2.getMessage().orNull());
                }
                if (md.getName().isNotPresent()) {
                    md.setName(md2.getName().orNull());
                }
                if (md.getKind().isNotPresent()) {
                    md.setKind(md2.getKind().orNull());
                }
            }
        }

    }

    @Override
    public OutputStream getOutputStream() {
        return this;
    }

    @Override
    public NOutputTargetMetadata getOutputMetaData() {
        return md;
    }

    @Override
    public void write(int b) throws IOException {
        base.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        base.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        base.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        base.flush();
    }

    @Override
    public void close() throws IOException {
        base.close();
    }
}
