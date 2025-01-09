package net.thevpc.nuts.util;

import java.io.BufferedReader;
import java.io.LineNumberReader;
import java.io.Reader;

public class NCharReader extends LineNumberReader {

    public NCharReader(Reader reader) {
        super(reader);
    }

    public String read(int count) {
        char[] c = new char[count];
        int v = read(c);
        return new String(c, 0, v);
    }

    public boolean canRead() {
        return peek(1).length() > 0;
    }

    public boolean canReadByCount(int count) {
        return peek(count).length() > count;
    }

    public int read(char[] buffer) {
        return read(buffer, 0, buffer.length);
    }

    public int read(char[] buffer, int offset, int count) {
        int read = 0;
        while (read < count) {
            int v = super.read(buffer, offset, count);
            if (v <= 0) {
                break;
            }
            offset += v;
            read -= v;
        }
        return read;
    }

    public boolean read(String text) {
        if(text==null||text.isEmpty()){
            return true;
        }
        String s = peek(text.length());
        if(s.equals(text)){
            skip(text.length());
            return true;
        }
        return false;
    }

    public boolean peek(String text) {
        if(text==null||text.isEmpty()){
            return true;
        }
        String s = peek(text.length());
        return s.equals(text);
    }

    public char readChar() {
        int c = read();
        if(c<0){
            throw new IllegalArgumentException("EOF");
        }
        return (char)c;
    }

    public int peek() {
        mark(1);
        int s = read();
        if(s>=0) {
            reset();
        }
        return s;
    }

    public String peek(int count) {
        if (count <= 0) {
            return "";
        }
        mark(count);
        String s = read(count);
        reset();
        return s;
    }
}
