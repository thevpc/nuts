package net.thevpc.nuts.toolbox.nsh.jshell.parser;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Iterator;

public class StrReader {
    Reader reader;
    private boolean uniformNewLine = true;
    private StringBuilder buffer = new StringBuilder();

    public void pushBackChar(char c) {
        buffer.insert(0, c);
    }

//    public Reader nextItem() {
//
//    }

    public int getChar(int pos) {
        if (pos >= buffer.length()) {
            while (true) {
                if (fillLine() <= 0) {
                    break;
                }
                if (pos < buffer.length()) {
                    break;
                }
            }
        }
        if (pos >= buffer.length()) {
            return -1;
        }
        return buffer.charAt(pos);
    }

    public boolean readString(String str) {
        if (isString(str, 0)) {
            readAll(str.length());
            return true;
        }
        return false;
    }

    public boolean isString(String str, int pos) {
        for (int i = 0; i < str.length(); i++) {
            if (!isChar(str.charAt(i), pos + i)) {
                return false;
            }
        }
        return true;
    }


    public boolean isChar(char c, int pos) {
        return getChar(pos) == c;
    }


    public boolean isWordChar(int c) {
        if (c == -1) {
            return false;
        }
        switch (c) {
            case '(':
            case ')':
            case '{':
            case '}':
            case '\\':
            case '$':
            case '"':
            case '\'':
            case '`':
            case '<':
            case '>':
            case '|':
            case '&':
            case '=':
            case ':':
            case '+':
            case '*':
            case '?':
            case '[':
            case ']':
            case '#':
            case ';':
                {
                return false;
            }
            default: {
                if (c <= 32) {
                    return false;
                }
            }
        }
        return true;
    }

    public Reader toReader() {
        return new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                return StrReader.this.read(cbuf, off, len);
            }

            @Override
            public void close() throws IOException {
                StrReader.this.close();
            }
        };
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        int x = 0;
        for (int i = 0; i < len; i++) {
            int c = read();
            if (c == -1) {
                return i;
            }
            cbuf[off + i] = (char) c;
        }
        return len;
    }

    public Iterable<String> lines() {
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    String line;

                    @Override
                    public boolean hasNext() {
                        line = readLine();
                        return line != null;
                    }

                    @Override
                    public String next() {
                        return line;
                    }
                };
            }
        };
    }

    public String readLine() {
        StringBuilder sb = new StringBuilder();
        while (true) {
            int r = read();
            if (r < 0) {
                break;
            }
            sb.append((char) r);
        }
        if (sb.length() == 0) {
            return null;
        }
        return sb.toString();
    }

    public int peekChar() {
        if (buffer.length() == 0) {
            fillLine();
        }
        if (buffer.length() > 0) {
            return buffer.charAt(0);
        }
        return -1;
    }

    public int read() {
        if (buffer.length() == 0) {
            fillLine();
        }
        if (buffer.length() > 0) {
            char c = buffer.charAt(0);
            buffer.deleteCharAt(0);
            return c;
        }
        return -1;
    }

    private int read0() {
        try {
            return reader.read();
        } catch (IOException e) {
            return -1;
        }
    }


    private int fillLine() {
        int count = 0;
        while (true) {
            int e = read0();
            if (e == -1) {
                break;
            }
            count++;
            if (e == '\\') {
                int e2 = read0();
                if (e2 == -1) {
                    buffer.append('\\');
                    break;
                } else if (e2 == '\n') {
                    count++;
                    break; //new line
                } else if (e2 == '\r') {
                    count++;
                    e2 = read0();
                    if (e2 == '\n') {
                        count++;
                        break; //new line
                    } else if (e2 == -1) {
                        break; //end of file
                    } else {
                        count++;
                        buffer.append((char) e2);
                        break; //new line
                    }
                } else {
                    count++;
                    buffer.append('\\');
                    buffer.append((char) e2);
                }
            } else if (e == '\r') {
                int e2 = read0();
                if (e2 == -1) {
                    if (uniformNewLine) {
                        buffer.append('\n');
                    } else {
                        buffer.append("\r");
                    }
                    break; //new line
                } else if (e2 == '\n') {
                    count++;
                    if (uniformNewLine) {
                        buffer.append('\n');
                    } else {
                        buffer.append("\r\n");
                    }
                    break; //new line
                } else {
                    count++;
                    buffer.append((char) e2);
                }
            } else {
                count++;
                buffer.append((char) e);
            }
        }
        return count;
    }

    public void close() {
        try {
            reader.close();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public String readAll(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int r = read();
            if (r < 0) {
                throw new IllegalArgumentException("Invalid");
            }
            sb.append((char) r);
        }
        return sb.toString();
    }
}
