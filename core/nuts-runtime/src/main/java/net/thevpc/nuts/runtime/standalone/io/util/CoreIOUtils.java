/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]  
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License"); 
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.io.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.runtime.standalone.io.terminal.NTerminalModeOp;
import net.thevpc.nuts.runtime.standalone.repository.index.CacheDB;
import net.thevpc.nuts.runtime.standalone.text.ExtendedFormatAware;
import net.thevpc.nuts.runtime.standalone.text.ExtendedFormatAwarePrintWriter;
import net.thevpc.nuts.runtime.standalone.text.RawOutputStream;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.DoWhenExist;
import net.thevpc.nuts.runtime.standalone.util.DoWhenNotExists;
import net.thevpc.nuts.util.NCoreCollectionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util.PathInfo;
import net.thevpc.nuts.runtime.standalone.xtra.digest.NDigestUtils;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDB;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDBTableFile;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.util.NStream;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * Created by vpc on 5/16/17.
 */
public class CoreIOUtils {

    public static final int DEFAULT_BUFFER_SIZE = 1024;
    public static final String MIME_TYPE_SHA1 = "text/sha-1";
    public static String newLineString = null;

    @Deprecated
    public static PrintWriter toPrintWriter(Writer writer, NSystemTerminalBase term) {
        if (writer == null) {
            return null;
        }
        if (writer instanceof ExtendedFormatAware) {
            if (writer instanceof PrintWriter) {
                return (PrintWriter) writer;
            }
        }
        ExtendedFormatAwarePrintWriter s = new ExtendedFormatAwarePrintWriter(writer, term, NWorkspace.of().get());
        return s;
    }

    public static boolean isValidConfLine(String line) {
        String l = NStringUtils.trimLeftToNull(line);
        if (l == null) {
            return false;
        }
        if (l.charAt(0) == '#') {
            return false;
        }
        return true;
    }

    public static Stream<String> confLines(InputStream stream) {
        return lines(stream).filter(CoreIOUtils::isValidConfLine);
    }

    public static Stream<String> confLines(Reader stream) {
        return lines(stream).filter(CoreIOUtils::isValidConfLine);
    }

    public static Stream<String> lines(InputStream stream) {
        return lines(new InputStreamReader(stream));
    }

    public static Stream<String> lines(Reader reader) {
        return NCoreCollectionUtils.finiteStream(new Supplier<String>() {
            private BufferedReader r = new BufferedReader(reader);

            public String get() {
                try {
                    return r.readLine();
                } catch (IOException e) {
                    throw new NIOException(e);
                }
            }
        });
    }

    @Deprecated
    public static PrintWriter toPrintWriter(OutputStream writer, NSystemTerminalBase term) {
        if (writer == null) {
            return null;
        }
        ExtendedFormatAwarePrintWriter s = new ExtendedFormatAwarePrintWriter(writer, term, NWorkspace.of().get());
        return s;
    }

    @Deprecated
    public static OutputStream convertOutputStream(OutputStream out, NTerminalMode expected, NSystemTerminalBase term) {
        ExtendedFormatAware a = convertOutputStreamToExtendedFormatAware(out, expected, term);
        return (OutputStream) a;
    }

    @Deprecated
    public static ExtendedFormatAware convertOutputStreamToExtendedFormatAware(OutputStream out, NTerminalMode expected, NSystemTerminalBase term) {
        if (out == null) {
            return null;
        }
        ExtendedFormatAware aw = null;
        if (out instanceof ExtendedFormatAware) {
            aw = (ExtendedFormatAware) out;
        } else {
            aw = new RawOutputStream(out, term, NWorkspace.of().get());
        }
        switch (expected) {
            case INHERITED: {
                return aw.convert(NTerminalModeOp.NOP);
            }
            case FORMATTED: {
                return aw.convert(NTerminalModeOp.FORMAT);
            }
            case FILTERED: {
                return aw.convert(NTerminalModeOp.FILTER);
            }
            default: {
                throw new NIllegalArgumentException(NMsg.ofC("unsupported terminal mode %s", expected));
            }
        }
    }

    public static String resolveRepositoryPath(NAddRepositoryOptions options, Path rootFolder) {
        String loc = options.getLocation();
        String goodName = options.getName();
        if (NBlankable.isBlank(goodName)) {
            goodName = options.getConfig().getName();
        }
        if (NBlankable.isBlank(goodName)) {
            goodName = options.getName();
        }
        if (NBlankable.isBlank(goodName)) {
            if (options.isTemporary()) {
                goodName = "temp-" + UUID.randomUUID();
            } else {
                goodName = "repo-" + UUID.randomUUID();
            }
        }
        if (NBlankable.isBlank(loc)) {
            if (options.isTemporary()) {
                if (NBlankable.isBlank(goodName)) {
                    goodName = "temp";
                }
                if (goodName.length() < 3) {
                    goodName = goodName + "-repo";
                }
                loc = NPath
                        .ofTempFolder(goodName + "-").toString();
            } else {
                if (NBlankable.isBlank(loc)) {
                    if (NBlankable.isBlank(goodName)) {
                        goodName = CoreNUtils.randomColorName() + "-repo";
                    }
                    loc = goodName;
                }
            }
        }
        return NPath.of(loc).toAbsolute(rootFolder.toString())
                .toString();
    }

//    public static String trimSlashes(String repositoryIdPath) {
//        StringBuilder sb = new StringBuilder(repositoryIdPath);
//        boolean updated = true;
//        while (updated) {
//            updated = false;
//            if (sb.length() > 0) {
//                if (sb.charAt(0) == '/' || sb.charAt(0) == '\\') {
//                    sb.delete(0, 1);
//                    updated = true;
//                } else if (sb.charAt(sb.length() - 1) == '/' || sb.charAt(sb.length() - 1) == '\\') {
//                    sb.delete(sb.length() - 1, sb.length());
//                    updated = true;
//                }
//            }
//        }
//        return sb.toString();
//    }

    public static NPrintStream resolveOut() {
        NSession session = NSession.of().get();
        return (session.getTerminal() == null) ? NPrintStream.NULL
                : session.getTerminal().out();
    }

    /**
     * copy input to output
     *
     * @param in  entree
     * @param out sortie
     */
    public static void copy(Reader in, Writer out) {
        copy(in, out, DEFAULT_BUFFER_SIZE);
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
    public static void copy(Reader in, Writer out, int bufferSize) {
        char[] buffer = new char[bufferSize];
        int len;
        try {
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
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
//
//    public static void delete(Path file) {
//        delete(null, file);
//    }

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
        NSession session = NSession.of().orNull();
        NLog LOG = session == null ? null : NLog.of(CoreIOUtils.class);
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
                            LOG.with().level(Level.FINEST).verb(NLogVerb.WARNING).log(
                                    NMsg.ofJ("delete file {0}", file));
                        }
                        deleted[0]++;
                    } catch (IOException e) {
                        if (LOG != null) {
                            LOG.with().level(Level.FINEST).verb(NLogVerb.WARNING)
                                    .log(NMsg.ofJ("failed deleting file : {0}", file)
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
                            LOG.with().level(Level.FINEST).verb(NLogVerb.WARNING)
                                    .log(NMsg.ofJ("delete folder {0}", dir));
                        }
                        deleted[1]++;
                    } catch (IOException e) {
                        if (LOG != null) {
                            LOG.with().level(Level.FINEST).verb(NLogVerb.WARNING)
                                    .log(NMsg.ofJ("failed deleting folder: {0}", dir)
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
//    public static String getAbsoluteFile(File s) {
//        return s.toPath().normalize().toAbsolutePath().toString();
//    }
//    

    public static String buildUrl(String url, String path) {
        if (!url.endsWith("/")) {
            if (path.startsWith("/")) {
                return url + path;
            } else {
                return url + "/" + path;
            }
        } else {
            if (path.startsWith("/")) {
                return url + path.substring(1);
            } else {
                return url + path;
            }
        }
    }

    public static boolean isURL(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException ex) {
            return false;
        }
    }

    public static String urlEncodeString(String s) {
        if (s == null || s.trim().length() == 0) {
            return "";
        }
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new NIOException(e);
        }
    }

    public static Path resolveLocalPathFromURL(URL url) {
        try {
            return new File(url.toURI()).toPath();
        } catch (URISyntaxException e) {
            return new File(url.getPath()).toPath();
        }
    }

    public static URL resolveURLFromResource(Class cls, String urlPath) {
        if (!urlPath.startsWith("/")) {
            throw new NIllegalArgumentException(NMsg.ofC("unable to resolve url from %s", urlPath));
        }
        URL url = cls.getResource(urlPath);
        String urlFile = url.getFile();
        int separatorIndex = urlFile.indexOf("!/");
        if (separatorIndex != -1) {
            String jarFile = urlFile.substring(0, separatorIndex);
            try {
                return new URL(jarFile);
            } catch (MalformedURLException ex) {
                // Probably no protocol in original jar URL, like "jar:C:/mypath/myjar.jar".
                // This usually indicates that the jar file resides in the file system.
                if (!jarFile.startsWith("/")) {
                    jarFile = "/" + jarFile;
                }
                try {
                    return new URL("file:" + jarFile);
                } catch (IOException ex2) {
                    throw new NIOException(ex2);
                }
            }
        } else {
            String encoded = encodePath(urlPath);
            String url_tostring = url.toString();
            if (url_tostring.endsWith(encoded)) {
                try {
                    return new URL(url_tostring.substring(0, url_tostring.length() - encoded.length()));
                } catch (IOException ex) {
                    throw new NIOException(ex);
                }
            }
            throw new NIllegalArgumentException(NMsg.ofC("unable to resolve url from %s", urlPath));
        }
    }

    private static String encodePath(String path) {
        StringTokenizer st = new StringTokenizer(path, "/", true);
        StringBuilder encoded = new StringBuilder();
        while (st.hasMoreTokens()) {
            String t = st.nextToken();
            if (t.equals("/")) {
                encoded.append(t);
            } else {
                try {
                    encoded.append(URLEncoder.encode(t, "UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    throw new NIllegalArgumentException(NMsg.ofC("unable to encode %s", t), ex);
                }
            }
        }
        return encoded.toString();
    }

    public static File resolveLocalFileFromResource(Class cls, String url) {
        return resolveLocalFileFromURL(resolveURLFromResource(cls, url));
    }

    public static File resolveLocalFileFromURL(URL url) {
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            return new File(url.getPath());
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

    public static InputStream getCachedUrlWithSHA1(String path, String sourceTypeName, boolean ignoreSha1NotFound) {
        NWorkspace workspace=NWorkspace.of().get();
        final NPath cacheBasePath = NLocations.of().getStoreLocation(workspace.getRuntimeId(), NStoreType.CACHE);
        final NPath urlContent = cacheBasePath.resolve("urls-content");
        String sha1 = null;
        try {
            ByteArrayOutputStream t = new ByteArrayOutputStream();
            NCp.of()
                    .from(NPath.of(path + ".sha1")).to(t).run();
            sha1 = t.toString().trim();
        } catch (NIOException ex) {
            if (!ignoreSha1NotFound) {
                throw ex;
            }
        }
        NanoDB cachedDB = CacheDB.of();
        NanoDBTableFile<CachedURL> cacheTable
                = cachedDB.tableBuilder(CachedURL.class).setNullable(false)
                .addAllFields()
                .addIndices("url")
                .getOrCreate();

//        final PersistentMap<String, String> cu=getCachedUrls(session);
        CachedURL old = cacheTable.findByIndex("url", path).findFirst().orNull();
//        String cachedSha1 = cu.get("sha1://" + path);
//        String oldLastModified =cu.get("lastModified://" + path);
//        String oldSize =cu.get("length://" + path);
        if (sha1 != null) {
            if (old != null && sha1.equalsIgnoreCase(old.sha1)) {
                String cachedID = old.path;
                if (cachedID != null) {
                    NPath p = urlContent.resolve(cachedID);
                    if (p.exists()) {
                        return p.getInputStream();
                    }
                }
            }
        }

        NPath header = NPath.of(path);
        long size = header.getContentLength();
        Instant lastModifiedInstant = header.getLastModifiedInstant();
        long lastModified = lastModifiedInstant == null ? 0 : lastModifiedInstant.toEpochMilli();

        //when sha1 was not resolved check size and last modification
        if (sha1 == null) {
            if (old != null && old.lastModified != -1 && old.lastModified == lastModified) {
                if (old != null && old.size == size) {
                    String cachedID = old.path;
                    if (cachedID != null) {
                        NPath p = urlContent.resolve(cachedID);
                        if (p.exists()) {
                            return p.getInputStream();
                        }
                    }
                }
            }
        }

        final String s = UUID.randomUUID().toString();
        final NPath outPath = urlContent.resolve(s + "~");
        urlContent.mkdirs();
        OutputStream p = outPath.getOutputStream();
        long finalLastModified = lastModified;
        NIO io = NIO.of();
        InputStream ist = NInputSourceBuilder.of(header.getInputStream())
                .setTee(p)
                .setCloseAction(() -> {
                    if (outPath.exists()) {
                        CachedURL ccu = new CachedURL();
                        ccu.url = path;
                        ccu.path = s;
                        ccu.sha1 = NDigestUtils.evalSHA1Hex(outPath);
                        long newSize = outPath.getContentLength();
                        ccu.size = newSize;
                        ccu.lastModified = finalLastModified;
                        NPath newLocalPath = urlContent.resolve(s);
                        try {
                            Files.move(outPath.toPath().get(), newLocalPath.toPath().get(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                        } catch (IOException ex) {
                            throw new NIOException(ex);
                        }
                        cacheTable.add(ccu);
                        cacheTable.flush();
                    }
                })
                .createInputStream();
        return NInputSourceBuilder.of(ist)
                .setMetadata(new DefaultNContentMetadata(
                        path,
                        NMsg.ofNtf(NText.ofStyledPath(path)),
                        size, header.getContentType(), header.getCharset(), sourceTypeName
                )).createInputStream()
                ;

    }

    public static void storeProperties(Map<String, String> props, OutputStream out, boolean sort) {
        storeProperties(props, new OutputStreamWriter(out), sort);
    }

    public static void storeProperties(Map<String, String> props, Writer w, boolean sort) {
        try {
            Set<String> keys = props.keySet();
            if (sort) {
                keys = new TreeSet<>(keys);
            }
            for (String key : keys) {
                String value = props.get(key);
                w.write(escapePropsString(key, true));
                w.write("=");
                w.write(escapePropsString(value, false));
                w.write("\n");
                w.flush();
            }
            w.flush();
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    /*
     * Converts unicodes to encoded &#92;uxxxx and escapes
     * special characters with a preceding slash.
     * This is a modified method from java.util.Properties because the method
     * is private but we need call it handle special properties files
     */
    public static String escapePropsString(String theString,
                                           boolean escapeSpace) {
        if (theString == null) {
            theString = "";
        }
        char[] chars = theString.toCharArray();
        StringBuilder buffer = new StringBuilder(chars.length);
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
                case '\\': {
                    buffer.append("\\\\");
                    break;
                }
                case ' ': {
                    if (i == 0 || escapeSpace) {
                        buffer.append('\\');
                    }
                    buffer.append(' ');
                    break;
                }
                case '\t': {
                    buffer.append("\\t");
                    break;
                }
                case '\n': {
                    buffer.append("\\n");
                    break;
                }
                case '\r': {
                    buffer.append("\\r");
                    break;
                }
                case '\f': {
                    buffer.append("\\f");
                    break;
                }
                case ':':
                case '#':
                case '!':
                case '=': {
                    buffer.append('\\');
                    buffer.append(c);
                    break;
                }
                default: {
                    if ((c > 61) && (c < 127)) {
                        buffer.append(c);
                    } else if (((c < 0x0020) || (c > 0x007e))) {
                        buffer.append('\\');
                        buffer.append('u');
                        buffer.append(NHex.toHexChar((c >> 12) & 0xF));
                        buffer.append(NHex.toHexChar((c >> 8) & 0xF));
                        buffer.append(NHex.toHexChar((c >> 4) & 0xF));
                        buffer.append(NHex.toHexChar(c & 0xF));
                    } else {
                        buffer.append(c);
                    }
                }
            }
        }
        return buffer.toString();
    }

    public static Path toPathInputSource(NInputSource is, List<Path> tempPaths, boolean enforceExtension) {
        boolean isPath = is instanceof NPath;
        if (isPath) {
            Path sf = ((NPath) is).toPath().orNull();
            if (sf != null) {
                return sf;
            }
        }
        String name = is.getMetaData().getName().orElse("no-name");
        Path temp = NPath.ofTempFile(name).toPath().get();
        NCp a = NCp.of().removeOptions(NPathOption.SAFE);
        if (isPath) {
            a.from(((NPath) is));
        } else {
            a.from(is.getInputStream());
        }
        a.to(temp).run();

        if (enforceExtension) {
            NPath pp = NPath.of(temp);
            String ext = pp.getLastExtension();
            if (ext.isEmpty()) {
                NContentTypes ctt = NContentTypes.of();
                String ct = ctt.probeContentType(temp);
                if (ct != null) {
                    List<String> e = ctt.findExtensionsByContentType(ct);
                    if (!e.isEmpty()) {
                        NPath newFile = NPath.ofTempFile(name + "." + e.get(0));
                        Path newFilePath = newFile.toPath().get();
                        try {
                            Files.move(temp, newFilePath, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException ex) {
                            throw new NIOException(ex);
                        }
                        tempPaths.add(newFilePath);
                        return newFilePath;
                    }
                }
            }
        }
        tempPaths.add(temp);
        return temp;
    }

    public static String getNewLine() {
        if (newLineString == null) {
            synchronized (CoreIOUtils.class) {
                newLineString = System.getProperty("line.separator");
            }
        }
        return newLineString;
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
        } catch (Exception e) {
            throw new RuntimeException(CoreStringUtils.exceptionToString(e), e);
        }
    }

    public static boolean isObsoletePath(Path path) {
        try {
            return isObsoleteInstant(Files.getLastModifiedTime(path).toInstant());
        } catch (IOException e) {
            return true;
        }
    }

    public static boolean isObsoletePath(NPath path) {
        try {
            Instant i = path.getLastModifiedInstant();
            if (i == null) {
                return false;
            }
            return isObsoleteInstant(i);
        } catch (Exception e) {
            return true;
        }
    }

    public static boolean isObsoleteInstant(Instant instant) {
        NSession session=NSession.of().get();
        if (session.getExpireTime().isPresent()) {
            return instant == null || instant.isBefore(session.getExpireTime().orNull());
        }
        return false;
    }

    public static byte[] loadFileContentLenient(Path out) {
        if (Files.isRegularFile(out)) {
            try {
                return Files.readAllBytes(out);
            } catch (Exception ex) {
                //ignore
            }
        }
        return new byte[0];
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

    public static boolean Arrays_equals(byte[] a, int aFromIndex, int aToIndex,
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

    public static boolean compareContent(Path file1, Path file2) {
        if (Files.isRegularFile(file1) && Files.isRegularFile(file2)) {
            try {
                if (Files.size(file1) == Files.size(file2)) {
                    int max = 2048;
                    byte[] b1 = new byte[max];
                    byte[] b2 = new byte[max];
                    try (java.io.InputStream in1 = Files.newInputStream(file1)) {
                        try (java.io.InputStream in2 = Files.newInputStream(file1)) {
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
                    }
                }
            } catch (IOException e) {
                throw new NIOException(e);
            }
        }
        return false;
    }

    public static InputStream createBytesStream(byte[] bytes, NMsg message, String contentType, String encoding, String kind) {
        return NInputSourceBuilder.of(new ByteArrayInputStream(bytes))
                .setMetadata(new DefaultNContentMetadata(
                                message,
                                (long) bytes.length,
                                contentType,
                                encoding, kind
                        )
                ).createInputStream();
    }

    public static String betterPath(String path1) {
        String home = System.getProperty("user.home");
        if (path1.startsWith(home + "/") || path1.startsWith(home + "\\")) {
            return "~" + path1.substring(home.length());
        }
        return path1;
    }

    public static String replaceFilePrefixes(String path, Map<String, String> map) {
        for (Map.Entry<String, String> e : map.entrySet()) {
            String v = replaceFilePrefix(path, e.getKey(), e.getValue());
            if (!v.equals(path)) {
                return v;
            }
        }
        return path;
    }

    public static String replaceFilePrefix(String path, String prefix, String replacement) {
        String path1 = path;
        String fs = File.separator;
        if (!prefix.endsWith(fs)) {
            prefix = prefix + fs;
        }
        if (!path1.endsWith(fs)) {
            path1 = prefix + fs;
        }
        if (path1.equals(prefix)) {
            if (replacement == null) {
                return "";
            }
            return replacement;
        }
        if (path.startsWith(prefix)) {
            if (replacement == null || replacement.equals("")) {
                return path1.substring(prefix.length());
            }
            return replacement + fs + path1.substring(prefix.length());
        }
        return path;
    }

    public static String longestCommonParent(String path1, String path2) {
        int latestSlash = -1;
        final int len = Math.min(path1.length(), path2.length());
        for (int i = 0; i < len; i++) {
            if (path1.charAt(i) != path2.charAt(i)) {
                break;
            } else if (path1.charAt(i) == '/') {
                latestSlash = i;
            }
        }
        if (latestSlash <= 0) {
            return "";
        }
        return path1.substring(0, latestSlash + 1);
    }

    public static PathInfo.Status tryWriteStatus(byte[] content, NPath out) {
        return tryWrite(content, out, DoWhenExist.IGNORE, DoWhenNotExists.IGNORE);
    }

    public static PathInfo.Status tryWrite(byte[] content, NPath out) {
        return tryWrite(content, out, DoWhenExist.ASK, DoWhenNotExists.CREATE);
    }

    public static PathInfo.Status tryWrite(byte[] content, NPath out, /*boolean doNotWrite*/ DoWhenExist doWhenExist, DoWhenNotExists doWhenNotExist) {
        NAssert.requireNonNull(doWhenExist, "doWhenExist");
        NAssert.requireNonNull(doWhenNotExist, "doWhenNotExist");
//        System.err.println("[DEBUG] try write "+out);
        out = out.toAbsolute().normalize();
        byte[] old = null;
        if (out.isRegularFile()) {
            try {
                old = out.readBytes();
            } catch (Exception ex) {
                //ignore
            }
        }
        NSession session=NSession.of().get();
        if (old == null) {
            switch (doWhenNotExist) {
                case IGNORE: {
                    return PathInfo.Status.DISCARDED;
                }
                case CREATE: {
                    out.mkParentDirs();
                    out.writeBytes(content);
                    if (session.isPlainTrace()) {
                        session.out().resetLine().println(NMsg.ofC("create file %s", out));
                    }
                    return PathInfo.Status.CREATED;
                }
                case ASK: {
                    if (NAsk.of()
                            .setDefaultValue(true)
                            .forBoolean(NMsg.ofC("create %s ?",
                                    NText.ofStyled(
                                            betterPath(out.toString()), NTextStyle.path()
                                    ))
                            ).getBooleanValue()) {
                        out.mkParentDirs();
                        out.writeBytes(content);
                        if (session.isPlainTrace()) {
                            session.out().resetLine().println(NMsg.ofC("create file %s", out));
                        }
                        return PathInfo.Status.CREATED;
                    } else {
                        return PathInfo.Status.DISCARDED;
                    }
                }
                default: {
                    throw new NUnsupportedEnumException(doWhenNotExist);
                }
            }
        } else {
            if (Arrays.equals(old, content)) {
                return PathInfo.Status.DISCARDED;
            }
            switch (doWhenExist) {
                case IGNORE: {
                    return PathInfo.Status.DISCARDED;
                }
                case OVERRIDE: {
                    out.writeBytes(content);
                    if (session.isPlainTrace()) {
                        session.out().resetLine().println(NMsg.ofC("update file %s", out));
                    }
                    return PathInfo.Status.OVERRIDDEN;
                }
                case ASK: {
                    if (NAsk.of()
                            .setDefaultValue(true)
                            .forBoolean(NMsg.ofC("override %s ?",
                                    NText.ofStyled(
                                            betterPath(out.toString()), NTextStyle.path()
                                    ))
                            ).getBooleanValue()) {
                        out.writeBytes(content);
                        if (session.isPlainTrace()) {
                            session.out().resetLine().println(NMsg.ofC("update file %s", out));
                        }
                        return PathInfo.Status.OVERRIDDEN;
                    } else {
                        return PathInfo.Status.DISCARDED;
                    }
                }
                default: {
                    throw new NUnsupportedEnumException(doWhenExist);
                }
            }
        }
    }

    public static Set<CopyOption> asCopyOptions(Set<NPathOption> noptions) {
        Set<CopyOption> joptions = new HashSet<>();

        for (NPathOption option : noptions) {
            switch (option) {
                case REPLACE_EXISTING: {
                    joptions.add(StandardCopyOption.REPLACE_EXISTING);
                    break;
                }
                case ATOMIC: {
                    joptions.add(StandardCopyOption.ATOMIC_MOVE);
                    break;
                }
                case COPY_ATTRIBUTES: {
                    joptions.add(StandardCopyOption.COPY_ATTRIBUTES);
                    break;
                }
            }
        }
        return joptions;
    }

    //    public static boolean isFileExistsAndIsWritable(Path file) {
//        if(!Files.exists(file)){
//            return false;
//        }
//        boolean writable;
//        Channel channel = null;
//        try {
//            channel = new RandomAccessFile(file.toFile(), "rw").getChannel();
//            writable = true;
//        } catch(Exception ex) {
//            writable = false;
//        } finally {
//            if(channel!=null) {
//                try {
//                    channel.close();
//                } catch (IOException ex) {
//                    // exception handling
//                }
//            }
//        }
//        return writable;
//    }

    public static NStream<String> safeLines(byte[] bytes) {
        return NStream.of(
                new Iterator<String>() {
                    BufferedReader br;
                    String line;

                    @Override
                    public boolean hasNext() {
                        if (br == null) {
                            br = CoreIOUtils.bufferedReaderOf(bytes);
                        }
                        try {
                            line = null;
                            line = br.readLine();
                        } catch (IOException e) {
                            //
                        }
                        return line != null;
                    }

                    @Override
                    public String next() {
                        return line;
                    }
                }
        );
    }

    public static BufferedReader bufferedReaderOf(byte[] bytes) {
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
    }

    public static NExecOutput validateErr(NExecOutput err) {
        NSession session=NSession.of().get();
        if (err == null) {
            err = NExecOutput.ofStream(session.err());
        }
        if (err.getType() == NRedirectType.INHERIT) {
            if (NIO.of().isStderr(session.err())) {
                err = NExecOutput.ofInherit();
            } else {
                err = NExecOutput.ofStream(session.err());
            }
        } else if (err.getType() == NRedirectType.STREAM) {
            if (NIO.of().isStderr(session.err())) {
                err = NExecOutput.ofStream(session.err());
            }
        }
        return err;
    }

    public static NOptional<InputStream> openStream(URL u) {
        if (u == null) {
            return NOptional.ofNamedEmpty("null url");
        }
        InputStream in = null;
        try {
            in = u.openStream();
        } catch (IOException e) {
            return NOptional.ofNamedEmpty("error stream for " + u);
        }
        if (in == null) {
            return NOptional.ofNamedEmpty("null stream for " + u);
        }
        return NOptional.of(in);
    }


    public static class CachedURL {

        String url;
        String path;
        String sha1;
        long lastModified;
        long size;
    }

    public static DefaultNContentMetadata defaultNutsInputSourceMetadata(InputStream is) {
        Objects.requireNonNull(is);
        if (is instanceof NInputSource) {
            return new DefaultNContentMetadata(((NInputSource) is).getMetaData());
        }
        return new DefaultNContentMetadata();
    }

    public static NExecInput validateIn(NExecInput in) {
        if (in == null) {
            NSession session=NSession.of().get();
            in = NExecInput.ofStream(session.in());
        }
        if (in.getType() == NRedirectType.INHERIT) {
            NSession session=NSession.of().get();
            if (NIO.of().isStdin(session.in())) {
                in = NExecInput.ofInherit();
            } else {
                in = NExecInput.ofStream(session.in());
            }
        } else if (in.getType() == NRedirectType.STREAM) {
            if (NIO.of().isStdin(in.getStream())) {
                in = NExecInput.ofInherit();
            }
        }
        return in;
    }

    public static NExecOutput validateOut(NExecOutput out) {
        NSession session=NSession.of().get();
        if (out == null) {
            out = NExecOutput.ofStream(session.out());
        }
        if (out.getType() == NRedirectType.INHERIT) {
            if (NIO.of().isStdout(session.out())) {
                out = NExecOutput.ofInherit();
            } else {
                out = NExecOutput.ofStream(session.out());
            }
        } else if (out.getType() == NRedirectType.STREAM) {
            if (NIO.of().isStdout(session.out())) {
                out = NExecOutput.ofStream(session.out());
            }
        }
        return out;
    }

    public static String metadataToString(NContentMetadata md, Object caller) {
        NOptional<NMsg> m = md.getMessage();
        if (m.isPresent()) {
            NPlainPrintStream out = new NPlainPrintStream();
            out.print(m.get());
            String s = out.toString();
            if (!NBlankable.isBlank(s)) {
                return s;
            }
        }

        NOptional<String> m2 = md.getName();
        if (m2.isPresent()) {
            String s = m2.get();
            if (!NBlankable.isBlank(s)) {
                return s;
            }
        }
        if (caller != null) {
            return caller.getClass().getSimpleName();
        }
        return "no-name";
    }

    public static NContentMetadata createContentMetadata(Object... any) {
        DefaultNContentMetadata md = null;
        if (any != null) {
            for (Object o : any) {
                NContentMetadata md2 = null;
                if (o instanceof NContentMetadata) {
                    md2 = (NContentMetadata) o;
                } else if (o instanceof NContentMetadataProvider) {
                    md2 = ((NContentMetadataProvider) o).getMetaData();
                }
                if (md2 != null) {
                    if (md == null) {
                        md = new DefaultNContentMetadata(md2);
                    } else {
                        if (md.getContentLength().isNotPresent()) {
                            md.setContentLength(md2.getContentLength().orNull());
                        }
                        if (md.getContentType().isNotPresent()) {
                            md.setContentType(md2.getContentType().orNull());
                        }
                        if (md.getMessage().isNotPresent()) {
                            md.setMessage(md2.getMessage().orNull());
                        }
                        if (md.getName().isNotPresent()) {
                            md.setName(md2.getName().orNull());
                        }
                        if (md.getKind().isNotPresent()) {
                            md.setKind(md2.getKind().orNull());
                        }
                    }
                }
            }
        }
        if (md == null) {
            md = new DefaultNContentMetadata();
        }
        return md;
    }

    public static boolean isHttpUrl(String s) {
        if (s != null) {
            s = s.toLowerCase();
            return s.startsWith("http://") || s.startsWith("https://");
        }
        return false;
    }

    public static boolean isFileProtocol(String s) {
        if (s != null) {
            s = s.toLowerCase();
            return s.startsWith("file://");
        }
        return false;
    }

    public static String concatPath(String ...all) {
        StringBuilder sb=new StringBuilder();
        if(all!=null){
            for (String s : all) {
                if(s!=null){
                    s=s.trim();
                }
                if(s.length()>0) {

                    boolean newSlash = s.length() > 0 && s.charAt(0) == '/';
                    boolean wasSlash = sb.length() > 0 && sb.charAt(sb.length() - 1) == '/';
                    if (sb.length() > 0) {
                        if (!wasSlash && !newSlash) {
                            sb.append("/");
                            sb.append(s);
                        }else{
                            sb.append(s);
                        }
                    } else {
                        sb.append(s);
                    }
                }
            }
        }
        return sb.toString();
    }

    public static String ensureLeadingSlash(String s) {
        if(s.length()>0){
            if(!s.startsWith("/")){
                return "/"+s;
            }
            return s;
        }
        return "/";
    }

}
