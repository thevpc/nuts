package net.thevpc.nuts.runtime.standalone.io;

public interface OutputHelper {
    void write(byte[] b, int offset, int len);

    void flush();
}
