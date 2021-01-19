package net.thevpc.nuts.runtime.bundles.nanodb;

import java.io.InputStream;

public interface NanoDBInputStream extends AutoCloseable {
    InputStream readLob(String name);

    int readInt();

    public long readLong();

    float readFloat();

    double readDouble();

    String readUTF();

    void close();

    byte readByte();

//        Object readObject();
}
