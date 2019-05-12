package net.vpc.app.nuts.core.util.cmdline.format;

import java.util.Arrays;

public class FormatUtils {
    public static String escapeString(String name) {
        if (name == null) {
            name = "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\"");
        for (char c : name.toCharArray()) {
            switch (c) {
                case '\"':
                case '\\': {
                    sb.append('\\').append(c);
                    break;
                }
                case '\t': {
                    sb.append('\\').append('t');
                    break;
                }
                case '\n': {
                    sb.append('\\').append('n');
                    break;
                }
                case '\r': {
                    sb.append('\\').append('r');
                    break;
                }
                case '\f': {
                    sb.append('\\').append('f');
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        sb.append("\"");
        return sb.toString();
    }
    public static boolean isEmpty(String string) {
        return string == null || string.trim().isEmpty();
    }
    public static String alignLeft(String s, int width) {
        StringBuilder sb = new StringBuilder();
        if (s != null) {
            sb.append(s);
            int x = width - sb.length();
            if (x > 0) {
                sb.append(fillString(' ', x));
            }
        }
        return sb.toString();
    }

    public static String alignRight(String s, int width) {
        StringBuilder sb = new StringBuilder();
        if (s != null) {
            sb.append(s);
            int x = width - sb.length();
            if (x > 0) {
                sb.insert(0, fillString(' ', x));
            }
        }
        return sb.toString();
    }

    public static String fillString(char x, int width) {
        char[] cc = new char[width];
        Arrays.fill(cc, x);
        return new String(cc);
    }

    public static String fillString(String pattern, int width) {
        if(pattern==null || pattern.length()==0){
            throw new IllegalArgumentException("Empty Pattern");
        }
        char[] cc = new char[width];
        int len = pattern.length();
        for (int i = 0; i < cc.length; i++) {
            cc[i]=pattern.charAt(i% len);
        }
        return new String(cc);
    }

    private static String repeat(char c, int count) {
        char[] a = new char[count];
        Arrays.fill(a, 0, count, c);
        return new String(a);
    }

    private static String repeatV(char c, int count) {
        char[] a = new char[2 * count - 1];
        for (int i = 0; i < count; i += 2) {
            a[i] = c;
            if (i + 1 < a.length) {
                a[i + 1] = '\n';
            }
        }
        return new String(a);
    }

}
