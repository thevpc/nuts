package net.thevpc.nuts.runtime.standalone.io.outputstream;

public interface OutputHelper {
    void write(byte[] b, int offset, int len);

    void flush();
}
