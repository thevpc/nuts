package net.thevpc.nuts.io;

import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.util.NUnexpectedException;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.text.NMsg;
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

/**
 * NIOUtils class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NIOUtils {

    public static final int DEFAULT_BUFFER_SIZE = 10 * 4 * 1024;

    /**
     * Normalize path.
     *
     * @param names names
     * @return normalize path result
     */
    public static String normalizePath(String names) {
        String string = String.join("/", normalizePathNames(NStringUtils.split(names, "/\\", false, true)));
        if (names != null && (names.startsWith("/") || names.startsWith("\\"))) {
            return "/" + string;
        }
        return string;
    }

    /**
     * Normalize path names.
     *
     * @param names names
     * @return normalize path names result
     */
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

    /**
     * Strip parent.
     *
     * @param child child
     * @param parent parent
     * @return strip parent result
     */
    public static String stripParent(String child, String parent) {
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

    /**
     * Relativize.
     *
     * @param first first
     * @param second second
     * @return relativize result
     */
    public static String relativize(String first, String second) {
        if (first == null || second == null) return null;
        if (first.equals(second)) return "";

        // Normalize separators and split into segments
        String[] startParts = first.split("[/\\\\]+");
        String[] targetParts = second.split("[/\\\\]+");

        // 1. Find common prefix length
        int commonIndex = 0;
        while (commonIndex < startParts.length && commonIndex < targetParts.length
                && startParts[commonIndex].equals(targetParts[commonIndex])) {
            commonIndex++;
        }

        StringBuilder result = new StringBuilder();

        // 2. For every segment remaining in 'first', we must go "up" (..)
        // We skip empty strings caused by leading slashes
        for (int i = commonIndex; i < startParts.length; i++) {
            if (!startParts[i].isEmpty()) {
                result.append("..");
                result.append("/");
            }
        }

        // 3. For every segment remaining in 'second', we go "down"
        for (int i = commonIndex; i < targetParts.length; i++) {
            if (!targetParts[i].isEmpty()) {
                result.append(targetParts[i]);
                if (i < targetParts.length - 1) {
                    result.append("/");
                }
            }
        }

        String path = result.toString();
        // Clean up trailing slash if present
        return (path.endsWith("/") && path.length() > 1) ? path.substring(0, path.length() - 1) : path;
    }

    /**
     * Checks if is valid file name char.
     *
     * @param c c
     * @return is valid file name char result
     */
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
     * space and stripping the result
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
        return NStringUtils.strip(new String(chars));
    }

    /**
     * copy input to output
     *
     * @param in  entree
     * @param out sortie
     */
    public static long copy(Reader in, Writer out) {
        /**
         * Copy.
         *
         * @param in in
         * @param out out
         * @param DEFAULT_BUFFER_SIZE default_buffer_size
         * @return copy result
         */
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
        /**
         * Copy.
         *
         * @param in in
         * @param out out
         * @param DEFAULT_BUFFER_SIZE default_buffer_size
         * @return copy result
         */
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
            /**
             * Nio exception.
             *
             * @param ex ex
             * @return nio exception result
             */
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
            /**
             * Nio exception.
             *
             * @param ex ex
             * @return nio exception result
             */
            throw new NIOException(ex);
        }
        return count;
    }

    /**
     * Load string.
     *
     * @param is is
     * @param close close
     * @return load string result
     */
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
            /**
             * Nio exception.
             *
             * @param ex ex
             * @return nio exception result
             */
            throw new NIOException(ex);
        }
    }

    /**
     * Load string.
     *
     * @param is is
     * @param close close
     * @return load string result
     */
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
            /**
             * Nio exception.
             *
             * @param ex ex
             * @return nio exception result
             */
            throw new NIOException(ex);
        }
    }

    /**
     * Load char array.
     *
     * @param r r
     * @return load char array result
     */
    public static char[] loadCharArray(Reader r) {
        CharArrayWriter out = null;

        try {
            out = new CharArrayWriter();
          /**
           * Copy.
           *
           * @param r r
           * @param out out
           */
            copy(r, out);
            out.flush();
            return out.toCharArray();
        } finally {
            if (out != null) {
                out.close();
            }
        }

    }

    /**
     * Load byte array.
     *
     * @param r r
     * @return load byte array result
     */
    public static byte[] loadByteArray(InputStream r) {
        ByteArrayOutputStream out = null;

        try {
            try {
                out = new ByteArrayOutputStream();
              /**
               * Copy.
               *
               * @param r r
               * @param out out
               */
                copy(r, out);
                out.flush();
                return out.toByteArray();
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        } catch (IOException ex) {
            /**
             * Nio exception.
             *
             * @param ex ex
             * @return nio exception result
             */
            throw new NIOException(ex);
        }
    }

    /**
     * Load byte array.
     *
     * @param r r
     * @param close close
     * @return load byte array result
     */
    public static byte[] loadByteArray(InputStream r, boolean close) {
        ByteArrayOutputStream out = null;

        try {
            try {
                out = new ByteArrayOutputStream();
              /**
               * Copy.
               *
               * @param r r
               * @param out out
               */
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
            /**
             * Nio exception.
             *
             * @param ex ex
             * @return nio exception result
             */
            throw new NIOException(ex);
        }
    }

    /**
     * Load byte array.
     *
     * @param stream stream
     * @param maxSize max size
     * @param close close
     * @return load byte array result
     */
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
                  /**
                   * Copy.
                   *
                   * @param stream stream
                   * @param os os
                   * @param close close
                   * @param true true
                   */
                    copy(stream, os, close, true);
                    return os.toByteArray();
                }
            } finally {
                if (close) {
                    stream.close();
                }
            }
        } catch (IOException ex) {
            /**
             * Nio exception.
             *
             * @param ex ex
             * @return nio exception result
             */
            throw new NIOException(ex);
        }
    }

    /**
     * Copy.
     *
     * @param from from
     * @param to to
     * @param closeInput close input
     * @param closeOutput close output
     * @return copy result
     */
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
            /**
             * Nio exception.
             *
             * @param ex ex
             * @return nio exception result
             */
            throw new NIOException(ex);
        }
    }

    //    public static void delete(File file) {
//        delete(null, file);
//    }
    /**
     * Delete.
     *
     * @param file file
     */
    public static void delete(File file) {
      /**
       * Delete.
       *
       * @param file.toPath() file.to path()
       */
        delete(file.toPath());
    }

    /**
     * Delete.
     *
     * @param file file
     */
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
            /**
             * Nio exception.
             *
             * @param ex ex
             * @return nio exception result
             */
            throw new NIOException(ex);
        }
    }

    /**
     * Returns the path name parts.
     *
     * @param name name
     * @param type type
     * @return get path name parts result
     */
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
        /**
         * N unexpected exception.
         *
         * @param type) type)
         * @return n unexpected exception result
         */
        throw new NUnexpectedException(NMsg.ofC("%s not supported", type));
    }

    /**
     * Returns the file extension.
     *
     * @param s s
     * @return get file extension result
     */
    public static String getFileExtension(Path s) {
        if (s == null) {
            return "";
        }
        /**
         * Returns the file extension.
         *
         * @param s.getFileName().toString() s.get file name().to string()
         * @return get file extension result
         */
        return getFileExtension(s.getFileName().toString());
    }

    /**
     * Returns the file extension.
     *
     * @param s s
     * @return get file extension result
     */
    public static String getFileExtension(File s) {
        if (s == null) {
            return "";
        }
        /**
         * Returns the file extension.
         *
         * @param s.getName() s.get name()
         * @return get file extension result
         */
        return getFileExtension(s.getName());
    }

    /**
     * Returns the file extension.
     *
     * @param s s
     * @return get file extension result
     */
    public static String getFileExtension(String s) {
        if (s == null) {
            return "";
        }
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

    /**
     * Returns the file extension.
     *
     * @param s s
     * @param longest longest
     * @param includeDot include dot
     * @return get file extension result
     */
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

    /**
     * Checks if is url.
     *
     * @param url url
     * @return is url result
     */
    public static boolean isURL(String url) {
        try {
            URI.create(url).toURL();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Chars to bytes.
     *
     * @param chars chars
     * @return chars to bytes result
     */
    public static byte[] charsToBytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
        // clear sensitive data
        Arrays.fill(byteBuffer.array(), (byte) 0);

        return bytes;
    }

    /**
     * Bytes to chars.
     *
     * @param bytes bytes
     * @return bytes to chars result
     */
    public static char[] bytesToChars(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        CharBuffer charBuffer = StandardCharsets.UTF_8.decode(byteBuffer);
        char[] chars = Arrays.copyOfRange(charBuffer.array(),
                charBuffer.position(), charBuffer.limit());
        // clear sensitive data
        Arrays.fill(charBuffer.array(), '\0');
        return chars;
    }

    /**
     * Checks if is absolute path.
     *
     * @param location location
     * @return is absolute path result
     */
    public static boolean isAbsolutePath(String location) {
        return new File(location).isAbsolute();
    }

    /**
     * Returns the absolute path.
     *
     * @param path path
     * @return get absolute path result
     */
    public static String getAbsolutePath(String path) {
        return new File(path).toPath().toAbsolutePath().normalize().toString();
    }

    /**
     * Copy folder.
     *
     * @param src src
     * @param dest dest
     */
    public static void copyFolder(Path src, Path dest) {
        try {
            Files.walk(src)
                    .forEach(source -> copy(source, dest.resolve(src.relativize(source))));
        } catch (IOException e) {
            /**
             * Nio exception.
             *
             * @param e e
             * @return nio exception result
             */
            throw new NIOException(e);
        }
    }

    /**
     * Copy.
     *
     * @param source source
     * @param dest dest
     * @return copy result
     */
    private static void copy(Path source, Path dest) {
        try {
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            /**
             * Nio exception.
             *
             * @param e e
             * @return nio exception result
             */
            throw new NIOException(e);
        }
    }

    /**
     * Compare content.
     *
     * @param file1 file1
     * @param file2 file2
     * @return compare content result
     */
    public static boolean compareContent(Path file1, Path file2) {
        if (Files.isRegularFile(file1) && Files.isRegularFile(file2)) {
            try {
                if (Files.size(file1) == Files.size(file2)) {
                  /**
                   * Try.
                   *
                   * @param Files.newInputStream(file1) files.new input stream(file1)
                   */
                    try (InputStream in1 = Files.newInputStream(file1)) {
                      /**
                       * Try.
                       *
                       * @param Files.newInputStream(file1) files.new input stream(file1)
                       */
                        try (InputStream in2 = Files.newInputStream(file1)) {
                            /**
                             * Compare content.
                             *
                             * @param in1 in1
                             * @param in2 in2
                             * @return compare content result
                             */
                            return compareContent(in1, in2);
                        }
                    }
                }
            } catch (IOException e) {
                /**
                 * Nio exception.
                 *
                 * @param e e
                 * @return nio exception result
                 */
                throw new NIOException(e);
            }
        }
        return false;
    }

    /**
     * Compare content.
     *
     * @param in1 in1
     * @param in2 in2
     * @return compare content result
     */
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

    /**
     * Read best effort.
     *
     * @param len len
     * @param in in
     * @return read best effort result
     */
    public static byte[] readBestEffort(int len, InputStream in) {
        if (len < 0) {
            /**
             * Index out of bounds exception.
             *
             * @return index out of bounds exception result
             */
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

    /**
     * Read best effort.
     *
     * @param b b
     * @param off off
     * @param len len
     * @param in in
     * @return read best effort result
     */
    public static int readBestEffort(byte[] b, int off, int len, InputStream in) {
        if (len < 0) {
            /**
             * Index out of bounds exception.
             *
             * @return index out of bounds exception result
             */
            throw new IndexOutOfBoundsException();
        }
        int n = 0;
        while (n < len) {
            int count = 0;
            try {
                count = in.read(b, off + n, len - n);
            } catch (IOException e) {
                /**
                 * Nio exception.
                 *
                 * @param e e
                 * @return nio exception result
                 */
                throw new NIOException(e);
            }
            if (count < 0) {
                break;
            }
            n += count;
        }
        return n;
    }

    /**
     * Read bytes.
     *
     * @param file file
     * @return read bytes result
     */
    public static byte[] readBytes(File file) {
      /**
       * Try.
       *
       * @param FileInputStream(file) file input stream(file)
       */
        try (InputStream in = new FileInputStream(file)) {
            /**
             * Read bytes.
             *
             * @param in in
             * @return read bytes result
             */
            return readBytes(in);
        } catch (IOException ex) {
            /**
             * Unchecked io exception.
             *
             * @param ex ex
             * @return unchecked io exception result
             */
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Read bytes.
     *
     * @param file file
     * @return read bytes result
     */
    public static byte[] readBytes(Path file) {
      /**
       * Try.
       *
       * @param Files.newInputStream(file) files.new input stream(file)
       */
        try (InputStream in = Files.newInputStream(file)) {
            /**
             * Read bytes.
             *
             * @param in in
             * @return read bytes result
             */
            return readBytes(in);
        } catch (IOException ex) {
            /**
             * Unchecked io exception.
             *
             * @param ex ex
             * @return unchecked io exception result
             */
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Read bytes.
     *
     * @param url url
     * @return read bytes result
     */
    public static byte[] readBytes(URL url) {
      /**
       * Try.
       *
       * @param url.openStream() url.open stream()
       */
        try (InputStream in = url.openStream()) {
            /**
             * Read bytes.
             *
             * @param in in
             * @return read bytes result
             */
            return readBytes(in);
        } catch (IOException ex) {
            /**
             * Unchecked io exception.
             *
             * @param ex ex
             * @return unchecked io exception result
             */
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Read string.
     *
     * @param from from
     * @return read string result
     */
    public static String readString(InputStream from) {
        return new String(readBytes(from));
    }

    /**
     * Read string.
     *
     * @param from from
     * @return read string result
     */
    public static String readString(Reader from) {
        return new String(readChars(from));
    }

    /**
     * Read bytes.
     *
     * @param from from
     * @return read bytes result
     */
    public static byte[] readBytes(InputStream from) {
        /**
         * Read bytes.
         *
         * @param from from
         * @param -1 -1
         * @return read bytes result
         */
        return readBytes(from, -1);
    }

    /**
     * Read bytes.
     *
     * @param from from
     * @param bufferSize buffer size
     * @return read bytes result
     */
    public static byte[] readBytes(InputStream from, int bufferSize) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
      /**
       * Copy.
       *
       * @param from from
       * @param out out
       * @param bufferSize buffer size
       */
        copy(from, out, bufferSize);
        return out.toByteArray();
    }

    /**
     * Read chars.
     *
     * @param from from
     * @return read chars result
     */
    public static char[] readChars(Reader from) {
        /**
         * Read chars.
         *
         * @param from from
         * @param -1 -1
         * @return read chars result
         */
        return readChars(from, -1);
    }

    /**
     * Read chars.
     *
     * @param from from
     * @param bufferSize buffer size
     * @return read chars result
     */
    public static char[] readChars(Reader from, int bufferSize) {
        CharArrayWriter out = new CharArrayWriter();
      /**
       * Copy.
       *
       * @param from from
       * @param out out
       * @param bufferSize buffer size
       */
        copy(from, out, bufferSize);
        return out.toCharArray();
    }

    /**
     * Read string.
     *
     * @param file file
     * @return read string result
     */
    public static String readString(File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException ex) {
            /**
             * Unchecked io exception.
             *
             * @param ex ex
             * @return unchecked io exception result
             */
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Read string.
     *
     * @param file file
     * @return read string result
     */
    public static String readString(Path file) {
        try {
            return new String(Files.readAllBytes(file));
        } catch (IOException ex) {
            /**
             * Unchecked io exception.
             *
             * @param ex ex
             * @return unchecked io exception result
             */
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Copy.
     *
     * @param in in
     * @param file file
     */
    public static void copy(InputStream in, Path file) {
        Path p = file.getParent();
        if (p != null) {
            p.toFile().mkdirs();
        }
      /**
       * Try.
       *
       * @param Files.newOutputStream(file) files.new output stream(file)
       */
        try (OutputStream out = Files.newOutputStream(file)) {
          /**
           * Copy.
           *
           * @param in in
           * @param out out
           */
            copy(in, out);
        } catch (IOException ex) {
            /**
             * Unchecked io exception.
             *
             * @param ex ex
             * @return unchecked io exception result
             */
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Copy.
     *
     * @param in in
     * @param file file
     */
    public static void copy(Reader in, Path file) {
        Path p = file.getParent();
        if (p != null) {
            p.toFile().mkdirs();
        }
      /**
       * Try.
       *
       * @param Files.newBufferedWriter(file) files.new buffered writer(file)
       */
        try (Writer out = Files.newBufferedWriter(file)) {
          /**
           * Copy.
           *
           * @param in in
           * @param out out
           */
            copy(in, out);
        } catch (IOException ex) {
            /**
             * Unchecked io exception.
             *
             * @param ex ex
             * @return unchecked io exception result
             */
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Expand file.
     *
     * @param path path
     * @return expand file result
     */
    public static File expandFile(String path) {
        final String p = expandPath(path);
        if (p == null) {
            return null;
        }
        return new File(p);
    }

    /**
     * path expansion replaces ~ with ${user.home} property value
     *
     * @param path to expand
     * @return expanded path
     */
    public static String expandPath(String path) {
        if (path == null) {
            return path;
        }
        if (path.equals("~")) {
            return System.getProperty("user.home");
        }
        if (path.startsWith("~") && path.length() > 1 && (path.charAt(1) == '/' || path.charAt(1) == '\\')) {
            return System.getProperty("user.home") + path.substring(1);
        }
        return path;
    }

}
