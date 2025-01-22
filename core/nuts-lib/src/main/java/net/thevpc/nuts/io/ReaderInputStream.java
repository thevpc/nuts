package net.thevpc.nuts.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class ReaderInputStream extends InputStream {
    private final Reader reader;
    private final String charsetName;
    private byte[] buffer;
    private int index;
    private int end;

    public ReaderInputStream(Reader reader, String charsetName) {
        this.reader = reader;
        this.charsetName = charsetName == null ? "UTF-8" : charsetName;
        this.buffer = null;
        this.index = 0;
        this.end = 0;
    }

    @Override
    public int read() throws IOException {
        if (index >= end) {
            char[] charBuffer = new char[1024];
            int numChars = reader.read(charBuffer);
            if (numChars == -1) {
                return -1;
            }
            buffer = new String(charBuffer, 0, numChars).getBytes(charsetName);
            index = 0;
            end = buffer.length;
        }
        return buffer[index++] & 0xFF;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
