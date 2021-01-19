package net.thevpc.nuts.runtime.bundles.nanodb;

import java.io.InputStream;

public interface NanoDBOutputStream extends AutoCloseable {
    void writeLob(String name, InputStream in);

    long getPosition();

    void writeBoolean(boolean val);

    public void writeByte(int val);

    public void writeShort(int val);

    public void writeChar(int val);

    public void writeInt(int val);

    public void writeLong(long val);

    public void writeFloat(float val);

    public void writeDouble(double val);

    public void writeBytes(String str);

    public void writeChars(String str);

    public void writeUTF(String str);

    @Override
    void close();

    void flush();

//        void writeObject(Object obj) ;
}
