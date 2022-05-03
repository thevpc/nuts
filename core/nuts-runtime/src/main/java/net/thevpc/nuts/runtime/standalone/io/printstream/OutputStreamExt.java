package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.io.*;

import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamExt extends OutputStream implements NutsOutputTarget {

    private OutputStream base;
    private DefaultNutsOutputTargetMetadata md;

    public OutputStreamExt(OutputStream base, NutsOutputTargetMetadata md0) {
        this.base = base;
        if (base instanceof NutsOutputTarget) {
            md = new DefaultNutsOutputTargetMetadata(((NutsOutputTarget) base).getOutputMetaData());
        } else {
            md = new DefaultNutsOutputTargetMetadata();
        }


        if (md0 == null) {
            if (base instanceof NutsOutputTarget) {
                md = new DefaultNutsOutputTargetMetadata(((NutsOutputTarget) base).getOutputMetaData());
            } else {
                md = new DefaultNutsOutputTargetMetadata();
            }
        } else {
            md = new DefaultNutsOutputTargetMetadata(md0);
            if (base instanceof NutsOutputTarget) {
                NutsOutputTargetMetadata md2 = ((NutsOutputTarget) base).getOutputMetaData();
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
    public NutsOutputTargetMetadata getOutputMetaData() {
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
