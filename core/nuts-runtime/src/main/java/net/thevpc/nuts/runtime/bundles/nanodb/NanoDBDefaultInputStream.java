package net.thevpc.nuts.runtime.bundles.nanodb;

import java.io.*;

class NanoDBDefaultInputStream implements NanoDBInputStream {
    private DataInputStream in;

    public NanoDBDefaultInputStream(InputStream in) {
        this.in =(in instanceof DataInputStream)?(DataInputStream) in:new DataInputStream(in);
    }

    @Override
    public InputStream readLob(String name) {
        throw new UnsupportedOperationException("unsupported operation: readLob");
    }

    @Override
    public int readInt() {
        try {
            return in.readInt();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public long readLong() {
        try {
            return in.readLong();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public float readFloat() {
        try {
            return in.readFloat();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public double readDouble() {
        try {
            return in.readDouble();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public String readUTF() {
        try {
            return in.readUTF();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public void close() {
        try {
            in.close();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public byte readByte() {
        try {
            return in.readByte();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

//    @Override
//    public Object readObject() {
//        try {
//            return is.readObject();
//        } catch (IOException ex) {
//            throw new UncheckedIOException(ex);
//        } catch (ClassNotFoundException e) {
//            throw new UncheckedIOException(new IOException(e));
//        }
//    }
}
