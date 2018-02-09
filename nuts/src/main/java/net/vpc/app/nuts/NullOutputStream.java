package net.vpc.app.nuts;

import java.io.OutputStream;

public final class NullOutputStream extends OutputStream {
    public static final OutputStream INSTANCE = new NullOutputStream();

    private  NullOutputStream() {
    }

    @Override
    public void write(int b) {
    }

    @Override
    public void write(byte[] b) {
    }

    @Override
    public void write(byte[] b, int off, int len) {
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }
}
