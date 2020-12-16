package net.thevpc.nuts.runtime.standalone.util.common;

public class StringReaderExt {
    String content;
    int pos = 0;

    public StringReaderExt(String content) {
        this.content = content == null ? "" : content;
    }

    public char peekChar() {
        return content.charAt(pos);
    }

    public char peekChar(int i) {
        return content.charAt(pos + i);
    }

    public boolean isAvailable(int count) {
        return pos + count < content.length();
    }

    public char nextChar() {
        char c = content.charAt(pos);
        pos++;
        return c;
    }

    public String peekChars(int max) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < max; i++) {
            if (hasNext()) {
                sb.append(peekChar(i));
            } else {
                break;
            }
        }
        return sb.toString();
    }

    public String nextChars(int max) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < max; i++) {
            if (hasNext()) {
                sb.append(nextChar());
            } else {
                break;
            }
        }
        return sb.toString();
    }

    public boolean hasNext(int count) {
        return content.length() - (pos + count) > 0;
    }

    public boolean hasNext() {
        return content.length() - pos > 0;
    }

    public boolean peekChars(String s) {
        return peekChars(s.length()).equals(s);
    }

}
