package net.thevpc.nuts.io;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

public class NullReader extends Reader {
    public static final Reader INSTANCE=new NullReader();

    private NullReader() {
    }

    @Override
    public int read(char[] chars, int i, int i1)  {
        return -1;
    }

    @Override
    public int read(CharBuffer target)  {
        return 0;
    }

    @Override
    public int read()  {
        return -1;
    }

    @Override
    public int read(char[] cbuf)  {
        return 0;
    }

    @Override
    public long skip(long n)  {
        return 0;
    }

    @Override
    public boolean ready()  {
        return false;
    }

    @Override
    public boolean markSupported() {
        return false;
    }


    @Override
    public void close() {

    }
}
