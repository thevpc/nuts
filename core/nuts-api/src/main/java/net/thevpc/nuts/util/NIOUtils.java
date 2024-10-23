package net.thevpc.nuts.util;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

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
    public static void copy(InputStream in, Path file) {
        Path p = file.getParent();
        if (p != null) {
            p.toFile().mkdirs();
        }
        try (OutputStream out = Files.newOutputStream(file)) {
            copy(in, out);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static void copy(Reader in, Path file) {
        Path p = file.getParent();
        if (p != null) {
            p.toFile().mkdirs();
        }
        try (Writer out = Files.newBufferedWriter(file)) {
            copy(in, out);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
    public static void copy(Reader in, Writer out) {
        char[] buffer = new char[4096 * 2];
        int c = 0;
        try {
            while ((c = in.read(buffer)) > 0) {
                out.write(buffer, 0, c);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static byte[] readBytes(URL url) {
        try(InputStream in=url.openStream()){
            return readBytes(in);
        }catch (IOException ex){
            throw new UncheckedIOException(ex);
        }
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
