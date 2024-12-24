package net.thevpc.nuts.io;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.CharBuffer;

public class NullWriter extends Writer {
    public static final Writer INSTANCE=new NullWriter();

    private NullWriter() {
    }

    @Override
    public void write(char[] chars, int i, int i1) throws IOException {
        
    }

    @Override
    public void write(int c) throws IOException {

    }

    @Override
    public void write(char[] cbuf) throws IOException {

    }

    @Override
    public void write(String str) throws IOException {

    }

    @Override
    public void write(String str, int off, int len) throws IOException {

    }

    @Override
    public Writer append(CharSequence csq) throws IOException {
        return this;
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        return this;
    }

    @Override
    public Writer append(char c) throws IOException {
        return this;
    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public void close() throws IOException {

    }
}
