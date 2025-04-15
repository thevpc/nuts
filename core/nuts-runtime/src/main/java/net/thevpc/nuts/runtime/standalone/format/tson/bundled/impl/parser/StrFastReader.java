package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.parser;

import java.util.Arrays;

public class StrFastReader {
    private char[] all;
    private int index;

    public StrFastReader(String s) {
        this.all = s.toCharArray();
    }

    public String peek(int count) {
        int max = Math.min(all.length - index, count);
        if (max <= 0) {
            return "";
        }
        char[] chars = Arrays.copyOfRange(all, index, index + count);
        return new String(chars);
    }

    public String read(int count) {
        int max = Math.min(all.length - index, count);
        if (max <= 0) {
            return "";
        }
        char[] chars = Arrays.copyOfRange(all, index, index + count);
        index += count;
        return new String(chars);
    }

    public char read() {
        char c = all[index];
        index++;
        return c;
    }

    public char peek() {
        return all[index];
    }

    public boolean hasNext() {
        return index < all.length;
    }

    public String readAnyIgnoreCase(String... all) {
        for (String s : all) {
            boolean y = readIgnoreCase(s);
            if(y){
                return s;
            }
        }
        return null;
    }

    public String readAny(String... all) {
        for (String s : all) {
            boolean y = read(s);
            if(y){
                return s;
            }
        }
        return null;
    }

    public String readAny(char... s) {
        if (index >= all.length) {
            return null;
        }
        for (int i = 0; i < s.length; i++) {
            if (s[i] == all[index]) {
                String ss = String.valueOf(all[index]);
                index++;
                return ss;
            }
        }
        return null;
    }

    public String peekAny(char... s) {
        if (index >= all.length) {
            return null;
        }
        for (int i = 0; i < s.length; i++) {
            if (s[i] == all[index]) {
                return String.valueOf(all[index]);
            }
        }
        return null;
    }

    public boolean read(char s) {
        if (index >= all.length) {
            return false;
        }
        if (s == all[index]) {
            index++;
            return true;
        }
        return false;
    }

    public boolean peek(char s) {
        if (index >= all.length) {
            return false;
        }
        if (s == all[index]) {
            return true;
        }
        return false;
    }

    interface CharPredicate {
        boolean test(char c);
    }

    public String readWhile(CharPredicate p) {
        StringBuilder sb = new StringBuilder();
        while (hasNext()) {
            char c = peek();
            if (p.test(c)) {
                sb.append(c);
                read();
            } else {
                break;
            }
        }
        return sb.toString();
    }

    public String readUntil(CharPredicate p) {
        StringBuilder sb = new StringBuilder();
        while (hasNext()) {
            char c = peek();
            if (p.test(c)) {
                break;
            } else {
                read();
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public boolean read(String s) {
        int len = s.length();
        if (index + len - 1 >= all.length) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (s.charAt(i) != all[index + i]) {
                return false;
            }
        }
        index += len;
        return true;
    }

    public boolean readIgnoreCase(String s) {
        int len = s.length();
        if (index + len - 1 >= all.length) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            char c1 = Character.toUpperCase(s.charAt(i));
            char c2 = Character.toUpperCase(all[index + i]);
            if (c1 != c2) {
                return false;
            }
        }
        index += len;
        return true;
    }

    public boolean peek(String s) {
        int len = s.length();
        if (index + len - 1 >= all.length) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (s.charAt(i) != all[index]) {
                return false;
            }
        }
        return true;
    }

    public void unread() {
        index--;
    }

    @Override
    public String toString() {
        if (index < all.length) {
            return new String(all, index, all.length - index);
        }
        return "";
    }
}
