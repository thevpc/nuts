package net.thevpc.nuts.runtime.core.expr;

public class StringReaderExt {
    String content;
    int pos = 0;

    public StringReaderExt(String content) {
        this.content = content == null ? "" : content;
    }

    public char peekChar() {
        if ((pos < 0) || (pos >= content.length())) {
            throw new StringIndexOutOfBoundsException(pos);
        }
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
            if (hasNext(i)) {
                sb.append(peekChar(i));
            } else {
                break;
            }
        }
        return sb.toString();
    }

    public boolean readString(String s) {
        int max = s.length();
        String n = peekChars(max);
        if(n.equals(s)){
            nextChars(max);
            return true;
        }
        return false;
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

    public interface CharPosPredicate{
        boolean test(char c, int pos);
    }

    public boolean peekChars(int count, CharPosPredicate filter) {
        if(hasNext(count)){
            for (int i = 0; i < count; i++) {
                if(!filter.test(peekChar(i),i)){
                    return false;
                }
            }
            return true;
        }else {
            return false;
        }
    }

    public boolean peekChars(String s) {
        return peekChars(s.length()).equals(s);
    }

    @Override
    public String toString() {
        return content.substring(pos);
    }
}
