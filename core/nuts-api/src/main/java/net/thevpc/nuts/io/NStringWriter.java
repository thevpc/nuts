package net.thevpc.nuts.io;

public interface NStringWriter {
    void write(char text);

    void write(String text);

    void write(char[] text, int offset, int len);
}
