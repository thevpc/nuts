package net.thevpc.nuts.io;

import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.util.NUnexpectedException;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NIOUtils {

    public static final int DEFAULT_BUFFER_SIZE = 10 * 4 * 1024;

    public static String normalizePath(String names) {
        String string = String.join("/", normalizePathNames(NStringUtils.split(names, "/\\", false, true)));
        if (names != null && (names.startsWith("/") || names.startsWith("\\"))) {
            return "/" + string;
        }
        return string;
    }

    public static List<String> normalizePathNames(List<String> names) {
        List<String> newNames = new ArrayList<>();
        for (String item : names) {
            switch (item) {
                case ".": {
                    break;
                }
                case "..": {
                    if (newNames.size() > 0) {
                        newNames.remove(newNames.size() - 1);
                    }
                    break;
                }
                default: {
                    newNames.add(item);
                }
            }
        }
        return newNames;
    }

    public static String toRelativePath(String child, String parent) {
        if (child.startsWith(parent)) {
            child = child.substring(parent.length());
            if (child.startsWith("/") || child.startsWith("\\")) {
                child = child.substring(1);
            }
            if (child.isEmpty()) {
                return "/";
            }
            return child;
        }
        return null;
    }

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
     * return normalized file name by replacing any special character with a
     * space and trimming the result
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

    /**
     * copy input to output
     *
     * @param in  entree
     * @param out sortie
     */
    public static long copy(Reader in, Writer out) {
        return copy(in, out, DEFAULT_BUFFER_SIZE);
    }

    /**
     * copy input to output
     *
     * @param in  entree
     * @param out sortie
     * @return size copied
     */
    public static long copy(InputStream in, OutputStream out) {
        return copy(in, out, DEFAULT_BUFFER_SIZE);
    }

    /**
     * copy input stream to output stream using the buffer size in bytes
     *
     * @param in         entree
     * @param out        sortie
     * @param bufferSize bufferSize
     * @return size copied
     */
    public static long copy(InputStream in, OutputStream out, int bufferSize) {
        if (bufferSize <= 0) {
            bufferSize = DEFAULT_BUFFER_SIZE;
        }
        byte[] buffer = new byte[bufferSize];
        int len;
        long count = 0;
        try {
            while ((len = in.read(buffer)) > 0) {
                count += len;
                out.write(buffer, 0, len);
            }
            return len;
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    /**
     * copy input stream to output stream using the buffer size in bytes
     *
     * @param in         entree
     * @param out        sortie
     * @param bufferSize bufferSize
     */
    public static long copy(Reader in, Writer out, int bufferSize) {
        if (bufferSize <= 0) {
            bufferSize = DEFAULT_BUFFER_SIZE;
        }
        char[] buffer = new char[bufferSize];
        int len;
        long count = 0;
        try {
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
                count += len;
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
        return count;
    }

    public static String loadString(InputStream is, boolean close) {
        try {
            try {
                byte[] bytes = loadByteArray(is);
                return new String(bytes);
            } finally {
                if (is != null && close) {
                    is.close();
                }
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    public static String loadString(Reader is, boolean close) {
        try {
            try {
                char[] bytes = loadCharArray(is);
                return new String(bytes);
            } finally {
                if (is != null && close) {
                    is.close();
                }
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    public static char[] loadCharArray(Reader r) {
        CharArrayWriter out = null;

        try {
            out = new CharArrayWriter();
            copy(r, out);
            out.flush();
            return out.toCharArray();
        } finally {
            if (out != null) {
                out.close();
            }
        }

    }

    public static byte[] loadByteArray(InputStream r) {
        ByteArrayOutputStream out = null;

        try {
            try {
                out = new ByteArrayOutputStream();
                copy(r, out);
                out.flush();
                return out.toByteArray();
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    public static byte[] loadByteArray(InputStream r, boolean close) {
        ByteArrayOutputStream out = null;

        try {
            try {
                out = new ByteArrayOutputStream();
                copy(r, out);
                out.flush();
                return out.toByteArray();
            } finally {
                if (out != null) {
                    out.close();
                }
                if (r != null && close) {
                    r.close();
                }
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    public static byte[] loadByteArray(InputStream stream, int maxSize, boolean close) {
        try {
            try {
                if (maxSize > 0) {
                    ByteArrayOutputStream to = new ByteArrayOutputStream();
                    byte[] bytes = new byte[Math.max(maxSize, 10240)];
                    int count;
                    int all = 0;
                    while ((count = stream.read(bytes)) > 0) {
                        if (all + count < maxSize) {
                            to.write(bytes, 0, count);
                            all += count;
                        } else {
                            int count2 = maxSize - all;
                            to.write(bytes, 0, count2);
                            all += count2;
                            break;
                        }
                    }
                    return to.toByteArray();
                } else {
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    copy(stream, os, close, true);
                    return os.toByteArray();
                }
            } finally {
                if (close) {
                    stream.close();
                }
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    public static long copy(InputStream from, OutputStream to, boolean closeInput, boolean closeOutput) {
        byte[] bytes = new byte[1024];//
        int count;
        long all = 0;
        try {
            try {
                try {
                    while ((count = from.read(bytes)) > 0) {
                        to.write(bytes, 0, count);
                        all += count;
                    }
                    return all;
                } finally {
                    if (closeInput) {
                        from.close();
                    }
                }
            } finally {
                if (closeOutput) {
                    to.close();
                }
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    //    public static void delete(File file) {
//        delete(null, file);
//    }
    public static void delete(File file) {
        delete(file.toPath());
    }

    public static void delete(Path file) {
        if (!Files.exists(file)) {
            return;
        }
        if (Files.isRegularFile(file)) {
            try {
                Files.delete(file);
            } catch (IOException e) {
                return;
            }
        }
        final int[] deleted = new int[]{0, 0, 0};
        NSession session = NSession.get().orNull();
        NLog LOG = session == null ? null : NLog.of(NIOUtils.class);
        try {
            Files.walkFileTree(file, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        Files.delete(file);
                        if (LOG != null) {
                            LOG.log(
                                    NMsg.ofC("delete file %s", file).asFineAlert());
                        }
                        deleted[0]++;
                    } catch (IOException e) {
                        if (LOG != null) {
                            LOG
                                    .log(NMsg.ofC("failed deleting file : %s", file)
                                            .asFineAlert()
                                    );
                        }
                        deleted[2]++;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    try {
                        Files.delete(dir);
                        if (LOG != null) {
                            LOG.log(NMsg.ofC("delete folder %s", dir)
                                    .asFineAlert());
                        }
                        deleted[1]++;
                    } catch (IOException e) {
                        if (LOG != null) {
                            LOG.log(NMsg.ofC("failed deleting folder: %s", dir)
                                    .asFineAlert()
                            );
                        }
                        deleted[2]++;
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    public static NPathNameParts getPathNameParts(String name, NPathExtensionType type) {
        if (type == null || type == NPathExtensionType.SMART) {
            type = NPathExtensionType.LONG;
        }
        switch (type) {
            case LONG: {
                String n = name == null ? "" : name;
                int i = n.indexOf('.');
                if (i < 0) {
                    return new NPathNameParts(n, "", "", NPathExtensionType.LONG);
                }
                return new NPathNameParts(n.substring(0, i), n.substring(i + 1), n.substring(i), NPathExtensionType.LONG);
            }
            case SHORT: {
                String n = name == null ? "" : name;
                int i = n.lastIndexOf('.');
                if (i < 0) {
                    return new NPathNameParts(n, "", "", NPathExtensionType.SHORT);
                }
                return new NPathNameParts(n.substring(0, i), n.substring(i + 1), n.substring(i), NPathExtensionType.SHORT);
            }
        }
        throw new NUnexpectedException(NMsg.ofC("%s not supported", type));
    }

    public static String getFileExtension(String s) {
        int i = s.lastIndexOf('.');
        if (i == 0) {
            return s.substring(1);
        } else if (i > 0) {
            if (i < (s.length() - 1)) {
                return s.substring(i + 1);
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    public static String getFileExtension(String s, boolean longest, boolean includeDot) {
        int i = longest ? s.indexOf('.') : s.lastIndexOf('.');
        if (i == 0) {
            return includeDot ? s : s.substring(1);
        } else if (i > 0) {
            if (i < (s.length() - 1)) {
                return s.substring(includeDot ? i : (i + 1));
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    public static boolean isURL(String url) {
        try {
            URI.create(url).toURL();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static byte[] charsToBytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
        // clear sensitive data
        Arrays.fill(byteBuffer.array(), (byte) 0);

        return bytes;
    }

    public static char[] bytesToChars(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        CharBuffer charBuffer = StandardCharsets.UTF_8.decode(byteBuffer);
        char[] chars = Arrays.copyOfRange(charBuffer.array(),
                charBuffer.position(), charBuffer.limit());
        // clear sensitive data
        Arrays.fill(charBuffer.array(), '\0');
        return chars;
    }

    public static boolean isAbsolutePath(String location) {
        return new File(location).isAbsolute();
    }

    public static String getAbsolutePath(String path) {
        return new File(path).toPath().toAbsolutePath().normalize().toString();
    }

    public static void copyFolder(Path src, Path dest) {
        try {
            Files.walk(src)
                    .forEach(source -> copy(source, dest.resolve(src.relativize(source))));
        } catch (IOException e) {
            throw new NIOException(e);
        }
    }

    private static void copy(Path source, Path dest) {
        try {
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new NIOException(e);
        }
    }

    public static boolean compareContent(Path file1, Path file2) {
        if (Files.isRegularFile(file1) && Files.isRegularFile(file2)) {
            try {
                if (Files.size(file1) == Files.size(file2)) {
                    try (InputStream in1 = Files.newInputStream(file1)) {
                        try (InputStream in2 = Files.newInputStream(file1)) {
                            return compareContent(in1, in2);
                        }
                    }
                }
            } catch (IOException e) {
                throw new NIOException(e);
            }
        }
        return false;
    }

    public static boolean compareContent(InputStream in1, InputStream in2) {
        int max = 2048;
        byte[] b1 = new byte[max];
        byte[] b2 = new byte[max];
        while (true) {
            int c1 = readBestEffort(b1, 0, b1.length, in1);
            int c2 = readBestEffort(b2, 0, b2.length, in2);
            if (c1 != c2) {
                return false;
            }
            if (c1 == 0) {
                return true;
            }
            if (!Arrays_equals(b1, 0, c1, b2, 0, c1)) {
                return false;
            }
            if (c1 < max) {
                return true;
            }
        }
    }

    private static boolean Arrays_equals(byte[] a, int aFromIndex, int aToIndex,
                                         byte[] b, int bFromIndex, int bToIndex) {
        //method added in JDK 9
        int aLength = aToIndex - aFromIndex;
        int bLength = bToIndex - bFromIndex;
        if (aLength != bLength) {
            return false;
        }
        for (int i = 0; i < aLength; i++) {
            if (a[aFromIndex + i] != b[bFromIndex + i]) {
                return false;
            }
        }

        return true;
    }

    public static byte[] readBestEffort(int len, InputStream in) {
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return new byte[0];
        }
        byte[] buf = new byte[len];
        int count = readBestEffort(buf, 0, len, in);
        if (count == len) {
            return buf;
        }
        byte[] buf2 = new byte[count];
        System.arraycopy(buf, 0, buf2, 0, count);
        return buf2;
    }

    public static int readBestEffort(byte[] b, int off, int len, InputStream in) {
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }
        int n = 0;
        while (n < len) {
            int count = 0;
            try {
                count = in.read(b, off + n, len - n);
            } catch (IOException e) {
                throw new NIOException(e);
            }
            if (count < 0) {
                break;
            }
            n += count;
        }
        return n;
    }

    public static byte[] readBytes(File file) {
        try (InputStream in = new FileInputStream(file)) {
            return readBytes(in);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static byte[] readBytes(Path file) {
        try (InputStream in = Files.newInputStream(file)) {
            return readBytes(in);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static byte[] readBytes(URL url) {
        try (InputStream in = url.openStream()) {
            return readBytes(in);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static String readString(InputStream from) {
        return new String(readBytes(from));
    }

    public static String readString(Reader from) {
        return new String(readChars(from));
    }

    public static byte[] readBytes(InputStream from) {
        return readBytes(from, -1);
    }

    public static byte[] readBytes(InputStream from, int bufferSize) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(from, out, bufferSize);
        return out.toByteArray();
    }

    public static char[] readChars(Reader from) {
        return readChars(from, -1);
    }

    public static char[] readChars(Reader from, int bufferSize) {
        CharArrayWriter out = new CharArrayWriter();
        copy(from, out, bufferSize);
        return out.toCharArray();
    }

    public static String readString(File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static String readString(Path file) {
        try {
            return new String(Files.readAllBytes(file));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
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

}
