package net.thevpc.nuts.util;

import java.io.*;

public class NIOUtils {

    public static boolean isValidFileNameChar(char c) {
        switch (c) {
            case '"':
            case '\'':
            case '`':
            case '?':
            case '*':
            case ':':
            case '%':
            case '|':
            case '<':
            case '>':
            case '/':
            case '\\':
            case '{':
            case '}':
            case '[':
            case ']':
            case '(':
            case ')':
            case '$': {
                return false;
            }
            default: {
                if (c < 32) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * return normalized file name by replacing any special character with a space and trimming the result
     *
     * @param name fine name to normalize
     * @return normalized string without accents
     */
    public static String normalizeFileName(String name) {
        char[] chars = NStringUtils.normalizeString(name).toCharArray();
        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
                case '°': {
                    chars[i] = 'o';
                    break;
                }
                case '"':
                case '\'':
                case '`':
                case '?':
                case '*':
                case ':':
                case '%':
                case '|':
                case '<':
                case '>':
                case '/':
                case '\\':
                case '{':
                case '}':
                case '[':
                case ']':
                case '(':
                case ')':
                case '$': {
                    chars[i] = ' ';
                    break;
                }
                default: {
                    if (chars[i] < 32) {
                        chars[i] = ' ';
                    }
                }
            }
        }
        return new String(chars).trim();
    }


    public static byte[] readBytes(InputStream from) {
        return readBytes(from, -1);
    }

    public static byte[] readBytes(InputStream from, int bufferSize) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(from, out, bufferSize);
        return out.toByteArray();
    }

    public static long copy(InputStream from, OutputStream to) {
        return copy(from, to, -1);
    }

    public static long copy(InputStream from, OutputStream to, int bufferSize) {
        byte[] bytes = new byte[bufferSize < 0 ? 10240 : bufferSize];
        int count;
        long all = 0;
        try {
            while ((count = from.read(bytes)) > 0) {
                to.write(bytes, 0, count);
                all += count;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return all;
    }
}
