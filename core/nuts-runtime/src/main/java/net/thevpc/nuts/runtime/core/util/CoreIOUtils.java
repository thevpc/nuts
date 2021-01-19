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
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.core.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.http.SimpleHttpClient;
import net.thevpc.nuts.runtime.core.io.NutsFormattedPrintStream;
import net.thevpc.nuts.runtime.core.format.text.*;
import net.thevpc.nuts.runtime.core.terminals.NutsTerminalModeOp;
import net.thevpc.nuts.runtime.standalone.index.CacheDB;
import net.thevpc.nuts.runtime.standalone.io.*;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.DefaultNutsDescriptorContentParserContext;
import net.thevpc.nuts.NutsLogVerb;
import net.thevpc.nuts.runtime.bundles.nanodb.NanoDB;
import net.thevpc.nuts.runtime.bundles.io.*;
import net.thevpc.nuts.runtime.bundles.nanodb.NanoDBTableFile;
import net.thevpc.nuts.spi.NutsDescriptorContentParserComponent;
import net.thevpc.nuts.spi.NutsDescriptorContentParserContext;
import net.thevpc.nuts.spi.NutsTransportComponent;
import net.thevpc.nuts.spi.NutsTransportConnection;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;

/**
 * Created by vpc on 5/16/17.
 */
public class CoreIOUtils {

    public static final int DEFAULT_BUFFER_SIZE = 1024;
    //    private static final Logger LOG = Logger.getLogger(CoreIOUtils.class.getName());
    public static final DirectoryStream.Filter<Path> DIR_FILTER = new DirectoryStream.Filter<Path>() {
        @Override
        public boolean accept(Path pathname) {
            try {
                return Files.isDirectory(pathname);
            } catch (Exception e) {
                //ignore
                return false;
            }
        }
    };
    private static final char[] HEX_ARR = "0123456789ABCDEF".toCharArray();
    public static String newLineString = null;

    public static PrintWriter toPrintWriter(Writer writer, NutsSession session) {
        if (writer == null) {
            return null;
        }
        if (writer instanceof ExtendedFormatAware) {
            if (writer instanceof PrintWriter) {
                return (PrintWriter) writer;
            }
        }
        ExtendedFormatAwarePrintWriter s = new ExtendedFormatAwarePrintWriter(writer);
        NutsWorkspaceUtils.setSession(s,session);
        return s;
    }

    public static PrintWriter toPrintWriter(OutputStream writer, NutsSession session) {
        if (writer == null) {
            return null;
        }
        ExtendedFormatAwarePrintWriter s = new ExtendedFormatAwarePrintWriter(writer);
        NutsWorkspaceUtils.setSession(s,session);
        return s;
    }

    public static PrintStream toPrintStream(Writer writer, NutsSession session) {
        if (writer == null) {
            return null;
        }
        SimpleWriterOutputStream s = new SimpleWriterOutputStream(writer,session);
        NutsWorkspaceUtils.setSession(s,session);
        return toPrintStream(s, session);
    }

    public static PrintStream toPrintStream(OutputStream os, NutsSession session) {
        if (os == null) {
            return null;
        }
        if (os instanceof PrintStream) {
            PrintStream y = (PrintStream) os;
            NutsWorkspaceUtils.setSession(y,session);
            return y;
        }
        PrintStreamExt s = new PrintStreamExt(os, false);
        NutsWorkspaceUtils.setSession(s,session);
        return s;
    }

    public static OutputStream convertOutputStream(OutputStream out, NutsTerminalMode expected, NutsWorkspace ws) {
        ExtendedFormatAware a = convertOutputStreamToExtendedFormatAware(out, expected, ws);
        return (OutputStream) a;
    }

    public static ExtendedFormatAware convertOutputStreamToExtendedFormatAware(OutputStream out, NutsTerminalMode expected, NutsWorkspace ws) {
        if (out == null) {
            return null;
        }
        ExtendedFormatAware aw = null;
        if (out instanceof ExtendedFormatAware) {
            aw = (ExtendedFormatAware) out;
        } else {
            aw = new RawOutputStream(out,ws);
        }
        switch (expected) {
            case INHERITED: {
                return aw.convert(NutsTerminalModeOp.NOP);
            }
            case FORMATTED: {
                return aw.convert(NutsTerminalModeOp.FORMAT);
            }
            case FILTERED: {
                return aw.convert(NutsTerminalModeOp.FILTER);
            }
            default: {
                throw new IllegalArgumentException("unsupported terminal mode " + expected);
            }
        }
    }

    public static void clearMonitor(PrintStream out, NutsWorkspace ws) {
        NutsTerminalMode terminalMode = ws.config().options().getTerminalMode();
        if (ws.io().term().isFormatted(out)
                &&
                (
                        terminalMode == null
                                || terminalMode == NutsTerminalMode.FORMATTED
                )
        ) {
            FPrintCommands.runMoveLineStart(out);
//                    out.print("                                                                      ");
//                    out.print("`move-line-start`");
            out.flush();
        }
    }

    public static NutsURLHeader getURLHeader(URL url) {
        return new DefaultNutsURLHeader(new SimpleHttpClient(url));
    }

    public static String getPlatformOsFamily() {
        String property = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        if (property.startsWith("linux")) {
            return "linux";
        }
        if (property.startsWith("win")) {
            return "windows";
        }
        if (property.startsWith("mac")) {
            return "mac";
        }
        if (property.startsWith("sunos")) {
            return "unix";
        }
        if (property.startsWith("freebsd")) {
            return "unix";
        }
        return "unknown";
    }

    public static URL[] toURL(String[] all) {
        List<URL> urls = new ArrayList<>();
        if (all != null) {
            for (String s : all) {
                if (!CoreStringUtils.isBlank(s)) {
                    try {
                        URL u = new URL(s);
                        urls.add(u);
                    } catch (MalformedURLException e) {
                        //
                        try {
                            urls.add(new File(s).toURI().toURL());
                        } catch (IOException ex) {
                            throw new UncheckedIOException(ex);
                        }
                    }
                }
            }
        }
        return urls.toArray(new URL[0]);
    }

    public static URL[] toURL(File[] all) {
        List<URL> urls = new ArrayList<>();
        if (all != null) {
            for (File s : all) {
                if (s != null) {
                    try {
                        urls.add(s.toURI().toURL());
                    } catch (MalformedURLException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }
        }
        return urls.toArray(new URL[0]);
    }

    public static File toFile(String url) {
        if (CoreStringUtils.isBlank(url)) {
            return null;
        }
        URL u;
        try {
            u = new URL(url);
            return toFile(u);
        } catch (MalformedURLException e) {
            //
            return new File(url);
        }
    }

    public static File toFile(URL url) {
        if (url == null) {
            return null;
        }
        if ("file".equals(url.getProtocol())) {
            try {
                return Paths.get(url.toURI()).toFile();
            } catch (URISyntaxException e) {
                //
            }
        }
        return null;
    }

    public static boolean setExecutable(Path path) {
        if (Files.exists(path) && !Files.isExecutable(path)) {
            PosixFileAttributeView p = Files.getFileAttributeView(path, PosixFileAttributeView.class);
            if (p != null) {
                try {
                    Set<PosixFilePermission> old = new HashSet<>(p.readAttributes().permissions());
                    old.add(PosixFilePermission.OWNER_EXECUTE);
                    Files.setPosixFilePermissions(path, old);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
                return true;
            }
        }
        return false;
    }

    //    public static Path pathOf(String... s) {
//        return Paths.get(s[0], Arrays.copyOfRange(s, 1, s.length));
//    }
//    public static Path pathOf(String s) {
//        if (CoreStringUtils.isEmpty(s)) {
//            return null;
//        }
//        return Paths.get(s);
//    }
//    public static InputStream toInputStreamSource(Object anyObject) {
//        try {
//            if (anyObject instanceof Path) {
//                // IOUtils.toInputStreamSource does not support Path
//                anyObject = ((Path) anyObject).toFile();
//            }
//            return IOUtils.toInputStreamSource(anyObject, null, null, null);
//        } catch (IOException ex) {
//            throw new UncheckedIOException(ex);
//        }
//    }
    public static boolean mkdirs(Path p) {
        if (p != null) {
            try {
                Files.createDirectories(p);
                return true;
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        return false;
    }

    public static Path toPathFile(String s,NutsWorkspace ws) {
        if (CoreStringUtils.isBlank(s)) {
            return null;
        }
        if (s.startsWith("file:")) {
            try {
                URI uri = new URL(s).toURI();
                if (uri.getAuthority() != null && uri.getAuthority().length() > 0) {
                    // Hack for UNC Path
                    uri = (new URL("file://" + s.substring("file:".length()))).toURI();
                }

                return Paths.get(uri);
            } catch (URISyntaxException ex) {
                throw new NutsParseException(ws, "not a file Path : " + s);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        if (s.startsWith("http://")
                || s.startsWith("https://")
                || s.startsWith("ftp://")
                || s.startsWith("jar:")
                || s.startsWith("zip:")
                || s.startsWith("ssh:")) {
            throw new NutsParseException(ws, "not a file Path");
        }
        if (isURL(s)) {
            throw new NutsParseException(ws, "not a file Path");
        }
        return Paths.get(s);
    }

    public static boolean isPathFile(String s) {
        if (CoreStringUtils.isBlank(s)) {
            return false;
        }
        if (s.startsWith("file:")) {
            return true;
        }
        if (s.startsWith("http://")
                || s.startsWith("https://")
                || s.startsWith("ftp://")
                || s.startsWith("jar:")
                || s.startsWith("zip:")
                || s.startsWith("ssh:")) {
            return false;
        }
        if (isURL(s)) {
            return false;
        }
        return true;
    }

    public static boolean isPathLocal(String s) {
        if (CoreStringUtils.isBlank(s)) {
            return false;
        }
        if (s.startsWith("file:")
                || s.startsWith("jar:")
                || s.startsWith("zip:")) {
            return true;
        }
        if (s.startsWith("http://")
                || s.startsWith("https://")
                || s.startsWith("ftp://")
                || s.startsWith("ssh://")) {
            return false;
        }
        if (isURL(s)) {
            return false;
        }
        return true;
    }

    public static boolean isPathHttp(String s) {
        if (CoreStringUtils.isBlank(s)) {
            return false;
        }
        if (s.startsWith("http://")
                || s.startsWith("https://")) {
            return true;
        }
        return false;
    }

    public static boolean isPathURL(String s) {
        if (CoreStringUtils.isBlank(s)) {
            return false;
        }
        if (CoreStringUtils.isBlank(s)
                || s.startsWith("file:")
                || s.startsWith("jar:")
                || s.startsWith("zip:")) {
            return true;
        }
        if (s.startsWith("http://")
                || s.startsWith("https://")
                || s.startsWith("ftp://")
                || s.startsWith("ssh://")) {
            return true;
        }
        if (isURL(s)) {
            return true;
        }
        return false;
    }

    public static String syspath(String s) {
        return s.replace('/', File.separatorChar);
    }

    public static String resolveRepositoryPath(NutsAddRepositoryOptions options, Path rootFolder, NutsSession session) {
        NutsWorkspace ws=session.getWorkspace();
        String loc = options.getLocation();
        String goodName = options.getName();
        if (CoreStringUtils.isBlank(goodName)) {
            goodName = options.getConfig().getName();
        }
        if (CoreStringUtils.isBlank(goodName)) {
            goodName = options.getName();
        }
        if (CoreStringUtils.isBlank(goodName)) {
            if (options.isTemporary()) {
                goodName = "temp-" + UUID.randomUUID().toString();
            } else {
                goodName = "repo-" + UUID.randomUUID().toString();
            }
        }
        if (CoreStringUtils.isBlank(loc)) {
            if (options.isTemporary()) {
                if (CoreStringUtils.isBlank(goodName)) {
                    goodName = "temp";
                }
                if (goodName.length() < 3) {
                    goodName = goodName + "-repo";
                }
                loc = ws.io().tmp()
                        .setSession(session)
                        .createTempFolder(goodName + "-");
            } else {
                if (CoreStringUtils.isBlank(loc)) {
                    if (CoreStringUtils.isBlank(goodName)) {
                        goodName = CoreNutsUtils.randomColorName() + "-repo";
                    }
                    loc = goodName;
                }
            }
        }
        return ws.io().expandPath(loc, rootFolder.toString());
    }

    public static String trimSlashes(String repositoryIdPath) {
        StringBuilder sb = new StringBuilder(repositoryIdPath);
        boolean updated = true;
        while (updated) {
            updated = false;
            if (sb.length() > 0) {
                if (sb.charAt(0) == '/' || sb.charAt(0) == '\\') {
                    sb.delete(0, 1);
                    updated = true;
                } else if (sb.charAt(sb.length() - 1) == '/' || sb.charAt(sb.length() - 1) == '\\') {
                    sb.delete(sb.length() - 1, sb.length());
                    updated = true;
                }
            }
        }
        return sb.toString();
    }

    public static PrintStream resolveOut(NutsSession session) {
        return (session.getTerminal() == null) ? session.getWorkspace().io().nullPrintStream() : session.getTerminal().out();
    }

    /**
     * @param localPath localPath
     * @param parseOptions may include --all-mains to force lookup of all main
     *                     classes if available
     * @param session session
     * @return descriptor
     */
    public static NutsDescriptor resolveNutsDescriptorFromFileContent(NutsInput localPath, String[] parseOptions, NutsSession session) {
        if (parseOptions == null) {
            parseOptions = new String[0];
        }
        NutsWorkspace ws = session.getWorkspace();
        if (localPath != null) {
            List<NutsDescriptorContentParserComponent> allParsers = ws.extensions().createAllSupported(NutsDescriptorContentParserComponent.class,  null, session);
            if (allParsers.size() > 0) {
                String fileExtension = CoreIOUtils.getFileExtension(localPath.getName());
                NutsDescriptorContentParserContext ctx = new DefaultNutsDescriptorContentParserContext(session, localPath, fileExtension, null, parseOptions);
                for (NutsDescriptorContentParserComponent parser : allParsers) {
                    NutsDescriptor desc = null;
                    try {
                        desc = parser.parse(ctx);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (desc != null) {
                        return desc;
                    }
                }
            }
        }
        return null;
    }

    public static String getPath(NutsId id, String ext, char sep) {
        StringBuilder sb = new StringBuilder();
        sb.append(id.getGroupId().replace('.', sep));
        sb.append(sep);
        sb.append(id.getArtifactId());
        sb.append(sep);
        sb.append(id.getVersion().toString());
        sb.append(sep);
        String name = id.getArtifactId() + "-" + id.getVersion().getValue();
        sb.append(name);
        sb.append(ext);
        return sb.toString();
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
    public static long copy(java.io.InputStream in, OutputStream out) {
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
    public static long copy(java.io.InputStream in, OutputStream out, int bufferSize) {
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
            throw new UncheckedIOException(ex);
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
            throw new UncheckedIOException(ex);
        }
    }

    public static String loadString(java.io.InputStream is, boolean close) {
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
            throw new UncheckedIOException(ex);
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
            throw new UncheckedIOException(ex);
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

    public static byte[] loadByteArray(java.io.InputStream r) {
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
            throw new UncheckedIOException(ex);
        }
    }

    public static byte[] loadByteArray(java.io.InputStream r, boolean close) {
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
            throw new UncheckedIOException(ex);
        }
    }

    public static byte[] loadByteArray(java.io.InputStream stream, int maxSize, boolean close) {
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
            throw new UncheckedIOException(ex);
        }
    }

    public static long copy(java.io.InputStream from, OutputStream to, boolean closeInput, boolean closeOutput) {
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
            throw new UncheckedIOException(ex);
        }
    }

    public static java.io.InputStream monitor(URL from, NutsProgressMonitor monitor, NutsSession session) {
            return monitor(
                    NutsWorkspaceUtils.of(session.getWorkspace()).openURL(from),
                    from, getURLName(from), CoreIOUtils.getURLHeader(from).getContentLength(), monitor, session);
    }

    public static java.io.InputStream monitor(java.io.InputStream from, Object source, String sourceName, long length, NutsProgressMonitor monitor, NutsSession session) {
        return new MonitoredInputStream(from, source, sourceName, length, monitor, session);
    }

    public static java.io.InputStream monitor(java.io.InputStream from, Object source, NutsProgressMonitor monitor, NutsSession session) {
        String sourceName = null;
        long length = -1;
        if (from instanceof InputStreamMetadataAware) {
            final InputStreamMetadataAware m = (InputStreamMetadataAware) from;
            sourceName = m.getMetaData().getName();
            length = m.getMetaData().getLength();
        }
        return new MonitoredInputStream(from, source, sourceName, length, monitor, session);
    }

//    public static void delete(File file) {
//        delete(null, file);
//    }

    public static void delete(NutsSession session, File file) {
        delete(session, file.toPath());
    }
//
//    public static void delete(Path file) {
//        delete(null, file);
//    }

    public static void delete(NutsSession session, Path file) {
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
        NutsLogger LOG = session == null ? null : session.getWorkspace().log().of(CoreIOUtils.class);
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
                            LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.WARNING).log("delete file " + file);
                        }
                        deleted[0]++;
                    } catch (IOException e) {
                        if (LOG != null) {
                            LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.WARNING).log("failed deleting file : " + file);
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
                            LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.WARNING).log("delete folder " + dir);
                        }
                        deleted[1]++;
                    } catch (IOException e) {
                        if (LOG != null) {
                            LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.WARNING).log("failed deleting folder: " + dir);
                        }
                        deleted[2]++;
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static String getNativePath(String path) {
        return path.replace('/', File.separatorChar);
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
    public static String getFileExtension(String s,boolean longest, boolean includeDot) {
        int i = longest?s.indexOf('.'):s.lastIndexOf('.');
        if (i == 0) {
            return includeDot?s:s.substring(1);
        } else if (i > 0) {
            if (i < (s.length() - 1)) {
                return s.substring(includeDot?i:(i + 1));
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

    public static String getURLName(URL url) {
        return getURLName(url.getFile());
    }

    public static String getURLName(String path) {
        String name;
        int index = path.lastIndexOf('/');
        if (index < 0) {
            name = path;
        } else {
            name = path.substring(index + 1);
        }
        index = name.indexOf('?');
        if (index >= 0) {
            name = name.substring(0, index);
        }
        name = name.trim();
        return name;
    }
//
//    public static CoreInput createInputSource(InputStream source) {
//        if (source == null) {
//            return null;
//        }
//        String name = String.valueOf(source);
//        return new InputStream(name, source);
//    }

    public static CoreInput createInputSource(java.io.InputStream source, String name, String typeName,NutsWorkspace ws) {
        if (source == null) {
            return null;
        }
        if (name == null) {
            name = String.valueOf(source);
        }
        return new InputStream(name, source, typeName,ws);
    }

    public static CoreInput createInputSource(Path source, String name, String typeName, NutsWorkspace ws) {
        if (source == null) {
            return null;
        }
        if (name == null) {
            name = source.getFileName().toString();
        }
        return new PathInput(name, source, typeName,ws);
    }

    public static CoreInput createInputSource(File source, String name, String typeName, NutsWorkspace ws) {
        if (source == null) {
            return null;
        }
        return createInputSource(source.toPath(), name, typeName,ws);
    }

    public static CoreInput createInputSource(byte[] source, String name, String typeName, NutsWorkspace ws) {
        if (source == null) {
            return null;
        }
        if (name == null) {
            name = String.valueOf(source);
        }
        return new ByteArrayInput(name, source, typeName,ws);
    }

    public static CoreInput createInputSource(String source, String name, String typeName,NutsWorkspace ws) {
        if (source == null) {
            return null;
        }
        if (isPathFile(source)) {
            return createInputSource(toPathFile(source,ws), name, typeName,ws);
        }
        URL baseURL = null;
        try {
            baseURL = new URL(source);
        } catch (Exception ex) {
            //
        }
        if (baseURL != null) {
            if (name == null) {
                name = source;
            }
            return new URLInput(name, baseURL, typeName,ws);
        }

        throw new NutsUnsupportedArgumentException(ws, "Unsupported source : " + source);
    }

    public static boolean isValidInputStreamSource(Class type) {
        return URL.class.isAssignableFrom(type)
                || File.class.isAssignableFrom(type)
                || Path.class.isAssignableFrom(type)
                || byte[].class.isAssignableFrom(type)
                || java.io.InputStream.class.isAssignableFrom(type)
                || String.class.isAssignableFrom(type)
                || CoreInput.class.isAssignableFrom(type);
    }

//    public static CoreInput createInputSource(Object source) {
//        if (source == null) {
//            return null;
//        } else if (source instanceof CoreInput) {
//            return (CoreInput) source;
//        } else if (source instanceof InputStream) {
//            return createInputSource((InputStream) source);
//        } else if (source instanceof Path) {
//            return createInputSource((Path) source);
//        } else if (source instanceof File) {
//            return createInputSource((File) source);
//        } else if (source instanceof URL) {
//            return createInputSource((URL) source);
//        } else if (source instanceof byte[]) {
//            return createInputSource(new ByteArrayInputStream((byte[]) source));
//        } else if (source instanceof String) {
//            return createInputSource((String) source);
//        } else {
//            throw new NutsUnsupportedArgumentException(null, "Unsupported type " + source.getClass().getName());
//        }
//    }


    public static NutsTransportConnection getHttpClientFacade(NutsSession session, String url) {
        //        System.out.println("getHttpClientFacade "+url);
        NutsTransportComponent best = session.getWorkspace().extensions().createSupported(NutsTransportComponent.class, url, session);
        if (best == null) {
            best = DefaultHttpTransportComponent.INSTANCE;
        }
        return best.open(url);
    }

    public static String urlEncodeString(String s) {
        if (s == null || s.trim().length() == 0) {
            return "";
        }
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Path resolveLocalPathFromURL(URL url) {
        try {
            return new File(url.toURI()).toPath();
        } catch (URISyntaxException e) {
            return new File(url.getPath()).toPath();
        }
    }

    public static URL resolveURLFromResource(Class cls, String urlPath,NutsWorkspace ws) {
        if (!urlPath.startsWith("/")) {
            throw new NutsIllegalArgumentException(ws, "unable to resolve url from " + urlPath);
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
                    throw new UncheckedIOException(ex2);
                }
            }
        } else {
            String encoded = encodePath(urlPath,ws);
            String url_tostring = url.toString();
            if (url_tostring.endsWith(encoded)) {
                try {
                    return new URL(url_tostring.substring(0, url_tostring.length() - encoded.length()));
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
            throw new NutsIllegalArgumentException(ws, "unable to resolve url from " + urlPath);
        }
    }

    private static String encodePath(String path,NutsWorkspace ws) {
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
                    throw new NutsIllegalArgumentException(ws, "unable to encode " + t, ex);
                }
            }
        }
        return encoded.toString();
    }

    public static File resolveLocalFileFromResource(Class cls, String url,NutsWorkspace ws) {
        return resolveLocalFileFromURL(resolveURLFromResource(cls, url,ws));
    }

    public static File resolveLocalFileFromURL(URL url) {
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            return new File(url.getPath());
        }
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 255;
            hexChars[j * 2] = HEX_ARR[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARR[v & 15];
        }
        return new String(hexChars);
    }

    public static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte aByte : bytes) {
            sb.append(toHex(aByte >> 4));
            sb.append(toHex(aByte));
        }
        return sb.toString();
    }

    public static char[] toHexChars(byte[] bytes) {
        char[] sb = new char[bytes.length * 2];
        int x = 0;
        for (byte aByte : bytes) {
            sb[x++] = toHex(aByte >> 4);
            sb[x++] = toHex(aByte);
        }
        return sb;
    }

    private static char toHex(int nibble) {
        return HEX_ARR[nibble & 15];
    }

    public static byte[] evalMD5(String input) {
        try {
            byte[] bytesOfMessage = input.getBytes("UTF-8");
            return evalMD5(bytesOfMessage);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String evalMD5Hex(Path path) {
        return toHexString(evalMD5(path));
    }

    public static byte[] evalMD5(Path path) {
        try (java.io.InputStream is = new BufferedInputStream(Files.newInputStream(path))) {
            return evalMD5(is);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static String evalMD5Hex(java.io.InputStream input) {
        return toHexString(evalMD5(input));
    }

    public static byte[] evalHash(java.io.InputStream input, String algo) {

        try {
            MessageDigest md;
            md = MessageDigest.getInstance(algo);
            byte[] buffer = new byte[8192];
            int len = 0;
            try {
                len = input.read(buffer);
                while (len != -1) {
                    md.update(buffer, 0, len);
                    len = input.read(buffer);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new UncheckedIOException(new IOException(e));
        }
    }

    public static byte[] evalMD5(java.io.InputStream input) {

        try {
            MessageDigest md;
            md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int len = 0;
            try {
                len = input.read(buffer);
                while (len != -1) {
                    md.update(buffer, 0, len);
                    len = input.read(buffer);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new UncheckedIOException(new IOException(e));
        }
    }

    public static byte[] evalMD5(byte[] bytesOfMessage) {
        try {
            MessageDigest md;
            md = MessageDigest.getInstance("MD5");
            return md.digest(bytesOfMessage);
        } catch (NoSuchAlgorithmException e) {
            throw new UncheckedIOException(new IOException(e));
        }
    }

    public static String evalSHA1Hex(Path file) {
        try {
            return evalSHA1Hex(Files.newInputStream(file), true);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String evalSHA1(File file) {
        try {
            return evalSHA1Hex(new FileInputStream(file), true);
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static byte[] charsToBytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
        // clear sensitive data
        Arrays.fill(byteBuffer.array(), (byte) 0);

        return bytes;
    }

    public static char[] bytesToChars(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        CharBuffer charBuffer = Charset.forName("UTF-8").decode(byteBuffer);
        char[] chars = Arrays.copyOfRange(charBuffer.array(),
                charBuffer.position(), charBuffer.limit());
        // clear sensitive data
        Arrays.fill(charBuffer.array(), '\0');
        return chars;
    }

    public static char[] evalSHA1(char[] input) {
        byte[] bytes = charsToBytes(input);
        char[] r = evalSHA1HexChars(new ByteArrayInputStream(bytes), true);
        Arrays.fill(bytes, (byte) 0);
        return r;
    }

    public static String evalSHA1(String input) {
        return evalSHA1Hex(new ByteArrayInputStream(input.getBytes()), true);
    }

    public static String evalSHA1Hex(java.io.InputStream input, boolean closeStream) {
        return toHexString(evalSHA1(input, closeStream));
    }

    public static char[] evalSHA1HexChars(java.io.InputStream input, boolean closeStream) {
        return toHexChars(evalSHA1(input, closeStream));
    }

    public static byte[] evalSHA1(java.io.InputStream input, boolean closeStream) {
        try {
            MessageDigest sha1 = null;
            try {
                sha1 = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException ex) {
                throw new UncheckedIOException(new IOException(ex));
            }
            byte[] buffer = new byte[8192];
            int len = 0;
            try {
                len = input.read(buffer);
                while (len != -1) {
                    sha1.update(buffer, 0, len);
                    len = input.read(buffer);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return sha1.digest();
        } finally {
            if (closeStream) {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                }
            }
        }
    }

    public static class CachedURL{
        String url;
        String path;
        String sha1;
        long lastModified;
        long size;
    }

    public static NutsInput getCachedUrlWithSHA1(NutsWorkspace ws, String path, String sourceTypeName, boolean ignoreSha1NotFound,NutsSession session) {
        final Path cacheBasePath = Paths.get(ws.locations().getStoreLocation(ws.getRuntimeId(), NutsStoreLocation.CACHE));
        final Path urlContent = cacheBasePath.resolve("urls-content");
        String sha1=null;
        try {
            ByteArrayOutputStream t = new ByteArrayOutputStream();
            ws.io().copy()
                    .setSession(session)
                    .from(path + ".sha1").to(t).run();
            sha1 = t.toString().trim();
        }catch (NutsIOException ex){
            if(!ignoreSha1NotFound){
                throw ex;
            }
        }
        NanoDB cachedDB = CacheDB.of(ws);
        NanoDBTableFile<CachedURL> cacheTable = cachedDB.createTable(
                cachedDB.createBeanDefinition(
                        CachedURL.class,false, "url"
                ),true
        );

//        final PersistentMap<String, String> cu=getCachedUrls(ws);
        CachedURL old=cacheTable.findByIndex("path", path).findFirst().orElse(null);
//        String cachedSha1 = cu.get("sha1://" + path);
//        String oldLastModified =cu.get("lastModified://" + path);
//        String oldSize =cu.get("length://" + path);
        if(sha1!=null) {
            if (old!=null && sha1.equalsIgnoreCase(old.sha1)) {
                String cachedID =old.path;
                if (cachedID != null) {
                    Path p = urlContent.resolve(cachedID);
                    if (Files.exists(p)) {
                        return ws.io().input().of(p);
                    }
                }
            }
        }
        try {
            NutsURLHeader header = null;
            long size = -1;
            long lastModified = -1;
            NutsTransportConnection f = CoreIOUtils.getHttpClientFacade(session, path);
            try {

                header = f.getURLHeader();
                size = header.getContentLength();
                Instant lastModifiedInstant = header.getLastModified();
                if(lastModifiedInstant!=null){
                    lastModified = lastModifiedInstant.toEpochMilli();
                }
            } catch (Exception ex) {
                //ignore error
            }

            //when sha1 was not resolved check size and last modification
            if(sha1==null) {
                if (old!=null && old.lastModified!= -1 && old.lastModified ==lastModified) {
                    if (old!=null && old.size==size) {
                        String cachedID = old.path;
                        if (cachedID != null) {
                            Path p = urlContent.resolve(cachedID);
                            if (Files.exists(p)) {
                                return ws.io().input().of(p);
                            }
                        }
                    }
                }
            }

            final String s = UUID.randomUUID().toString();
            final Path outPath = urlContent.resolve(s + "~");
            Files.createDirectories(urlContent);
            final OutputStream p = Files.newOutputStream(outPath);

            long finalLastModified = lastModified;
            InputStreamTee ist = new InputStreamTee(f.open(), p, () -> {
                if (Files.exists(outPath)) {
                    CachedURL ccu=new CachedURL();
                    ccu.url=path;
                    ccu.path=s;
                    ccu.sha1=evalSHA1Hex(outPath);
                    long newSize = -1;
                    try {
                        newSize = Files.size(outPath);
                    }catch (Exception ex){
                        //
                    }
                    ccu.size= newSize;
                    ccu.lastModified= finalLastModified;
                    Path newLocalPath = urlContent.resolve(s);
                    try {
                        Files.move(outPath, newLocalPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                    cacheTable.add(ccu);
                    cacheTable.flush();
                }
            });
            return ws.io().input()
                    .setName(path)
                    .setTypeName(sourceTypeName)
                    .of(new InputStreamMetadataAwareImpl(ist, new FixedInputStreamMetadata(path, size)));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }



//    public static PersistentMap<String, String> getCachedUrls(NutsWorkspace ws) {
//        final String k = PersistentMap.class.getName() + ":getCachedUrls";
//        PersistentMap<String, String> m = (PersistentMap<String, String>) ws.env().getProperty(k);
//        if (m == null) {
//            m = new DefaultPersistentMap<String, String>(String.class, String.class,
//                    Paths.get(
//                    ws.locations().getStoreLocation(
//                    ws.getRuntimeId(),
//                    NutsStoreLocation.CACHE)).resolve("urls-db").toFile(),ws);
//            ws.env().setProperty(k, m,new NutsUpdateOptions(ws.createSession()));
//        }
//        return m;
//    }

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
            throw new UncheckedIOException(ex);
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
                        buffer.append(toHex((c >> 12) & 0xF));
                        buffer.append(toHex((c >> 8) & 0xF));
                        buffer.append(toHex((c >> 4) & 0xF));
                        buffer.append(toHex(c & 0xF));
                    } else {
                        buffer.append(c);
                    }
                }
            }
        }
        return buffer.toString();
    }

    public static NutsInput toPathInputSource(NutsInput is, List<Path> tempPaths, NutsSession session) {
        NutsWorkspace ws=session.getWorkspace();
        if (is.getSource() instanceof Path) {
            //okkay
            return is;
        } else if (is.getSource() instanceof File) {
            return ws.io().input().setMultiRead(true).of(((File) is.getSource()).toPath());
        } else {
            Path temp = Paths.get(ws.io().tmp()
                    .setSession(session)
                    .createTempFile(is.getName()
                    ));
            ws.io().copy().setSafe(false).from(is).to(temp).setSession(session).run();
            tempPaths.add(temp);
            return ws.io().input().setMultiRead(true).of(temp);
        }
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
            throw new UncheckedIOException(e);
        }
    }

    private static void copy(Path source, Path dest) {
        try {
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException(CoreStringUtils.exceptionToString(e), e);
        }
    }

    public static Path toPath(Object lockedObject) {
        if (lockedObject instanceof Path) {
            return (Path) lockedObject;
        } else if (lockedObject instanceof File) {
            return ((File) lockedObject).toPath();
        } else if (lockedObject instanceof String) {
            return Paths.get((String) lockedObject);
        }
        return null;
    }

    public static NutsProgressFactory createLogProgressMonitorFactory(MonitorType mt) {
        switch (mt) {
            case STREAM:
                new DefaultNutsInputStreamProgressFactory();
            case DEFAULT:
                new DefaultNutsProgressFactory();
        }
        return new DefaultNutsProgressFactory();
    }

    public static NutsProgressMonitor createProgressMonitor(MonitorType mt, Object source, Object sourceOrigin, NutsSession session, boolean logProgress, NutsProgressFactory progressFactory) {
        NutsProgressMonitor m0 = null;
        NutsProgressMonitor m1 = null;
        if (logProgress) {
            m0 = createLogProgressMonitorFactory(mt).create(source, sourceOrigin, session);
        }
        if (progressFactory != null) {
            m1 = progressFactory.create(source, sourceOrigin, session);
        }
        if (m1 == null) {
            return m0;
        }
        if (m0 == null) {
            return m1;
        }
        return new NutsProgressMonitorList(new NutsProgressMonitor[]{m0, m1});
    }

    public static Path toPath(String path) {
        return CoreStringUtils.isBlank(path) ? null : Paths.get(path);
    }

    public static String compressUrl(String path) {
        if (path.startsWith("http://")
                || path.startsWith("https://")) {
            URL u = null;
            try {
                u = new URL(path);
            } catch (MalformedURLException e) {
                return path;
            }
            // pre-compute length of StringBuffer
            int len = u.getProtocol().length() + 1;
            if (u.getAuthority() != null && u.getAuthority().length() > 0) {
                len += 2 + u.getAuthority().length();
            }
            if (u.getPath() != null) {
                len += u.getPath().length();
            }
            if (u.getQuery() != null) {
                len += 1 + u.getQuery().length();
            }
            if (u.getRef() != null) {
                len += 1 + u.getRef().length();
            }

            StringBuffer result = new StringBuffer(len);
            result.append(u.getProtocol());
            result.append(":");
            if (u.getAuthority() != null && u.getAuthority().length() > 0) {
                result.append("//");
                result.append(u.getAuthority());
            }
            if (u.getPath() != null) {
                result.append(compressPath(u.getPath(), 0, 2));
            }
            if (u.getQuery() != null) {
                result.append('?');
                result.append("...");
//                result.append(u.getQuery());
            }
            if (u.getRef() != null) {
                result.append("#");
                result.append("...");
//                result.append(u.getRef());
            }
            return result.toString();

        } else {
            return compressPath(path);
        }
    }

    public static String compressPath(String path) {
        return compressPath(path, 2, 2);
    }

    public static String compressPath(String path, int left, int right) {
        String p = System.getProperty("user.home");
        if (path.startsWith(p + File.separator)) {
            path = "~" + path.substring(p.length());
        }
        List<String> a = new ArrayList<>(Arrays.asList(path.split("[\\\\/]")));
        int min = left + right + 1;
        if (a.size() > 0 && a.get(0).equals("")) {
            left += 1;
            min += 1;
        }
        if (a.size() > min) {
            a.add(left, "...");
            int len = a.size() - right - left - 1;
            for (int i = 0; i < len; i++) {
                a.remove(left + 1);
            }
        }
        return String.join("/", a);
    }

    public static java.io.InputStream interruptible(java.io.InputStream in) {
        if (in == null) {
            return in;
        }
        if (in instanceof Interruptible) {
            return in;
        }
        return new InputStreamExt(in, null);
    }

    //    public static OutputStream resolveBaseOutputStream(OutputStream out) {
//        if (out == null) {
//            return null;
//        }
//        if (out instanceof NutsOutputStreamExt) {
//            NutsOutputStreamExt a = (NutsOutputStreamExt) out;
//            return a.baseOutputStream();
//        }
//        if (out instanceof NutsFormattedPrintStream) {
//            return ((NutsFormattedPrintStream) out).getUnformattedInstance();
//        }
//        return out;
//    }
//    public static NutsTerminalMode resolveTerminalMode(OutputStream out) {
//        if (out == null) {
//            return NutsTerminalMode.INHERITED;
//        }
//        if (out instanceof NutsOutputStreamExt) {
//            NutsOutputStreamExt a = (NutsOutputStreamExt) out;
//            return a.getMode();
//        }
//        if (out instanceof NutsFormattedPrintStream) {
//            return NutsTerminalMode.FORMATTED;
//        }
//        return NutsTerminalMode.INHERITED;
//    }
    public static NutsTerminalModeOp resolveNutsTerminalModeOp(OutputStream out) {
        if (out == null) {
            return NutsTerminalModeOp.NOP;
        }
        if (out instanceof ExtendedFormatAware) {
            ExtendedFormatAware a = (ExtendedFormatAware) out;
            return a.getModeOp();
        }
        if (out instanceof NutsFormattedPrintStream) {
            return NutsTerminalModeOp.FORMAT;
        }
        return NutsTerminalModeOp.NOP;
    }

    //    public static PrintStream createFormattedPrintStream(OutputStream out, NutsWorkspace ws) {
//        PrintStream supported = (PrintStream) ws.setExtension(true).createSupported(
//                NutsFormattedPrintStream.class,
//                new DefaultNutsSupportLevelContext<>(ws, out),
//                new Class[]{OutputStream.class}, new Object[]{out});
//        if (supported == null) {
//            throw new NutsExtensionNotFoundException(ws, NutsFormattedPrintStream.class, "FormattedPrintStream");
//        }
//        NutsWorkspaceUtils.of(ws).setWorkspace(supported);
//        return supported;
//    }
//
//    public static PrintStream createFilteredPrintStream(OutputStream out, NutsWorkspace ws) {
//        return new NutsPrintStreamFiltered(out);
//    }
//    public static PrintStream convertPrintStream(OutputStream out, NutsTerminalMode m, NutsWorkspace ws) {
//        if (out == null) {
//            return null;
//        }
//        if (out instanceof NutsPrintStreamExt) {
//            NutsPrintStreamExt a = (NutsPrintStreamExt) out;
//            NutsTerminalMode am = a.getMode();
//            switch (m) {
//                case FORMATTED: {
//                    switch (am) {
//                        case FORMATTED: {
//                            return (PrintStream) a;
//                        }
//                        case FILTERED: {
//                            return a.basePrintStream();
//                        }
//                        case INHERITED: {
//                            return new NutsPrintStreamFormattedUnixAnsi(out);
//                        }
//                        default: {
//                            throw new NutsUnsupportedEnumException(ws, am);
//                        }
//                    }
//                }
//                case FILTERED: {
//                    switch (am) {
//                        case FORMATTED: {
//                            return (PrintStream) a;
//                        }
//                        case FILTERED: {
//                            return a.basePrintStream();
//                        }
//                        case INHERITED: {
//                            return new NutsPrintStreamFormattedUnixAnsi(out);
//                        }
//                        default: {
//                            throw new NutsUnsupportedEnumException(ws, am);
//                        }
//                    }
//                }
//            }
//        }
//        if (out instanceof PrintStream) {
//            while (out != null) {
//                if (out instanceof NutsPrintStreamExt) {
//                    PrintStream p = ((NutsPrintStreamExt) out).basePrintStream();
//                    if (p == null || p == out) {
//                        return (PrintStream) out;
//                    }
//                    out = p;
//                } else {
//                    return (PrintStream) out;
//                }
//            }
//        } else {
//            return CoreIOUtils.toPrintStream(out);
//        }
//        return CoreIOUtils.toPrintStream(out);
//    }
    //    public static PrintStream compress(PrintStream out) {
//        if (out == null) {
//            return null;
//        }
//        if (out instanceof NutsPrintStreamExt) {
//            NutsPrintStreamExt a = (NutsPrintStreamExt) out;
//            PrintStream out2 = a.basePrintStream();
//            if (out2 instanceof NutsPrintStreamExt) {
//                NutsPrintStreamExt b = (NutsPrintStreamExt) out2;
//                switch (a.getMode()) {
//                    case FILTERED: {
//                        switch (b.getMode()) {
//                            case FORMATTED: {
//                                return compress(b.basePrintStream());
//                            }
//                            case FILTERED: {
//                                return compress(out2);
//                            }
//                            case INHERITED: {
//                                return out;
//                            }
//                            default: {
//                                throw new IllegalArgumentException("Unexpected " + b.getMode());
//                            }
//                        }
//                    }
//                    case FORMATTED: {
//                        switch (b.getMode()) {
//                            case FORMATTED: {
//                                return compress(out2);
//                            }
//                            case FILTERED: {
//                                return compress(b.basePrintStream());
//                            }
//                            case INHERITED: {
//                                return out;
//                            }
//                            default: {
//                                throw new IllegalArgumentException("Unexpected " + b.getMode());
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        if (out instanceof PrintStream) {
//            while (out != null) {
//                if (out instanceof NutsPrintStreamExt) {
//                    PrintStream p = ((NutsPrintStreamExt) out).basePrintStream();
//                    if (p == null || p == out) {
//                        return (PrintStream) out;
//                    }
//                    out = p;
//                } else {
//                    return (PrintStream) out;
//                }
//            }
//        } else {
//            return CoreIOUtils.toPrintStream(out);
//        }
//        return CoreIOUtils.toPrintStream(out);
//    }
    public static PrintStream out(NutsWorkspace ws) {
        DefaultNutsIOManager io = (DefaultNutsIOManager) ws.io();
        PrintStream out = io.getCurrentStdout();
        return out == null ? System.out : out;
    }

    public static PrintStream err(NutsWorkspace ws) {
        DefaultNutsIOManager io = (DefaultNutsIOManager) ws.io();
        PrintStream err = io.getCurrentStderr();
        return err == null ? System.err : err;
    }

    public static java.io.InputStream in(NutsWorkspace ws) {
        DefaultNutsIOManager io = (DefaultNutsIOManager) ws.io();
        java.io.InputStream in = io.getCurrentStdin();
        return in == null ? System.in : in;
    }

    public static boolean isObsoletePath(NutsSession session, Path path) {
        try {
            return isObsoleteInstant(session, Files.getLastModifiedTime(path).toInstant());
        } catch (IOException e) {
            return true;
        }
    }

    public static boolean isObsoleteInstant(NutsSession session, Instant instant) {
        if (session.getExpireTime() != null) {
            if (instant == null || instant.isBefore(session.getExpireTime())) {
                return true;
            }
        }
        return false;
    }

    public enum MonitorType {
        STREAM,
        DEFAULT,
    }

    public static class ProcessExecHelper implements IProcessExecHelper {

        ProcessBuilder2 pb;
        NutsWorkspace ws;
        PrintStream out;

        public ProcessExecHelper(ProcessBuilder2 pb, NutsWorkspace ws, PrintStream out) {
            this.pb = pb;
            this.ws = ws;
            this.out = out;
        }

        public void dryExec() {
            if (ws.io().term().isFormatted(out)) {
                out.print("[dry] ==[exec]== ");
                out.println(pb.getFormattedCommandString(ws));
            } else {
                out.print("[dry] exec ");
                out.printf("%s%n", pb.getCommandString());
            }
        }

        public int exec() {
            try {
                clearMonitor(out, ws);
                ProcessBuilder2 p = pb.start();
                return p.waitFor().getResult();
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        public Future<Integer> execAsync() {
            try {
                clearMonitor(out, ws);
                ProcessBuilder2 p = pb.start();
                return new FutureTask<Integer>(() -> p.waitFor().getResult());
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    public static class DefaultMultiReadItem implements MultiInput {

        private CoreInput base;
        private byte[] content;
        private String typeName;

        public DefaultMultiReadItem(CoreInput base, String typeName) {
            this.base = base;
            content = CoreIOUtils.loadByteArray(base.open());
            this.typeName = typeName;
        }

        @Override
        public String getName() {
            return base.getName();
        }

        @Override
        public String getTypeName() {
            return typeName;
        }

        @Override
        public void close() {
            base.close();
        }

        @Override
        public java.io.InputStream open() {
            return new NamedByteArrayInputStream(content, base.getName());
        }

        @Override
        public long length() {
            return content.length;
        }

        @Override
        public Object getSource() {
            return base.getSource();
        }

        @Override
        public boolean isPath() {
            return base.isPath();
        }

        @Override
        public Path getPath() {
            return base.getPath();
        }

        @Override
        public boolean isURL() {
            return base.isURL();
        }

        @Override
        public URL getURL() {
            return base.getURL();
        }

        @Override
        public void copyTo(Path path) {
            base.copyTo(path);
        }

        @Override
        public MultiInput multi() {
            return this;
        }

        @Override
        public String toString() {
            return base.toString();
        }

        @Override
        public String getContentType() {
            return base.getContentType();
        }

        @Override
        public String getContentEncoding() {
            return base.getContentEncoding();
        }

        @Override
        public Instant getLastModified() {
            return base.getLastModified();
        }
    }

    public static abstract class AbstractMultiReadItem
            extends AbstractItem
            implements MultiInput {

        public AbstractMultiReadItem(String name, Object value, boolean path, boolean url, String typeName,NutsWorkspace ws) {
            super(name, value, path, url, typeName,ws);
        }

    }

    public static abstract class AbstractItem implements CoreInput {

        Object value;
        boolean path;
        boolean url;
        String name;
        String typeName;
        NutsWorkspace ws;

        public AbstractItem(String name, Object value, boolean path, boolean url, String typeName,NutsWorkspace ws) {
            this.name = name;
            this.value = value;
            this.path = path;
            this.url = url;
            this.typeName = typeName;
            this.ws = ws;
        }

        protected NutsIOException createOpenError(Exception ex) {
            String n = getTypeName();
            if (n == null) {
                n = getName();
            }
            String s = toString();
            if(s.equals(n)){
                return new NutsIOException(ws, n+" not found", ex);
            }
            return new NutsIOException(ws, n+" not found : " +toString(), ex);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getTypeName() {
            return typeName;
        }

        @Override
        public void close() {
        }

        @Override
        public abstract java.io.InputStream open();

        @Override
        public Object getSource() {
            return value;
        }

        public boolean isPath() {
            return path;
        }

        @Override
        public Path getPath() {
            throw new NutsUnsupportedOperationException(ws);
        }

        public boolean isURL() {
            return url;
        }

        @Override
        public URL getURL() {
            throw new NutsUnsupportedOperationException(ws);
        }

        @Override
        public void copyTo(Path path) {
            try {
                Files.copy(open(), path, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        @Override
        public MultiInput multi() {
            if (this instanceof MultiInput) {
                return (MultiInput) this;
            }
            return (MultiInput) new DefaultMultiReadItem(this, getTypeName());
        }

    }

    private static class ByteArrayInput extends AbstractMultiReadItem {

        public ByteArrayInput(String name, byte[] value, String typeName,NutsWorkspace ws) {
            super(name, value, false, false, typeName,ws);
        }

        @Override
        public java.io.InputStream open() {
            byte[] bytes = (byte[]) this.getSource();
            return new InputStreamMetadataAwareImpl(new NamedByteArrayInputStream(bytes, name), new FixedInputStreamMetadata(name, bytes.length));
        }

        @Override
        public long length() {
            byte[] bytes = (byte[]) this.getSource();
            return bytes.length;
        }

        @Override
        public String toString() {
            return "bytes(" + ((byte[]) this.getSource()).length + ")";
        }

        @Override
        public String getContentType() {
            return null;
        }

        @Override
        public String getContentEncoding() {
            return null;
        }

        @Override
        public Instant getLastModified() {
            return null;
        }
    }

    public static class URLInput extends AbstractItem {

        private NutsURLHeader cachedNutsURLHeader = null;

        public URLInput(String name, URL value, String typeName,NutsWorkspace ws) {
            super(name, value, false, true, typeName,ws);
        }

        @Override
        public java.io.InputStream open() {
            try {
                URL u = getURL();
                if (CoreIOUtils.isPathHttp(u.toString())) {
                    try {
                        NutsURLHeader uh = getURLHeader();
                        return new InputStreamMetadataAwareImpl(
                                NutsWorkspaceUtils.of(ws).openURL(u),
                                new FixedInputStreamMetadata(u.toString(), uh == null ? -1 : uh.getContentLength()));
                    } catch (Exception ex) {
                        //ignore
                    }
                }
                return NutsWorkspaceUtils.of(ws).openURL(u);
            } catch (Exception ex) {
                throw createOpenError(ex);
            }
        }

        @Override
        public String getContentType() {
            NutsURLHeader r=null;
            try {
                r = getURLHeader();
            }catch (Exception ex){
                //
            }
            if(r!=null){
                return r.getContentType();
            }
            return null;
        }

        @Override
        public String getContentEncoding() {
            NutsURLHeader r=null;
            try {
                r = getURLHeader();
            }catch (Exception ex){
                //
            }
            if(r!=null){
                return r.getContentEncoding();
            }
            return null;
        }

        @Override
        public Instant getLastModified() {
            NutsURLHeader r=null;
            try {
                r = getURLHeader();
            }catch (Exception ex){
                //
            }
            if(r!=null){
                return r.getLastModified();
            }
            return null;
        }

        @Override
        public URL getURL() {
            return (URL) getSource();
        }

        protected NutsURLHeader getURLHeader() {
            if (cachedNutsURLHeader == null) {
                URL u = getURL();
                if (CoreIOUtils.isPathHttp(u.toString())) {
                    try {
                        NutsTransportConnection hf = DefaultHttpTransportComponent.INSTANCE.open(u.toString());
                        cachedNutsURLHeader = hf.getURLHeader();
                    } catch (Exception ex) {
                        //ignore
                    }
                }
            }
            return cachedNutsURLHeader;
        }

        @Override
        public long length() {
            URL u = getURL();
            if (CoreIOUtils.isPathHttp(u.toString())) {
                try {
                    NutsURLHeader uh = getURLHeader();
                    return uh == null ? -1 : uh.getContentLength();
                } catch (Exception ex) {
                    //ignore
                }
            }
            File file = toFile(u);
            if (file != null) {
                return file.length();
            }
            return -1;
        }

        @Override
        public String toString() {
            return getURL().toString();
        }

    }

    private static class PathInput extends AbstractMultiReadItem {

        public PathInput(String name, Path value, String typeName,NutsWorkspace ws) {
            super(name, value, true, true, typeName,ws);
        }

        @Override
        public java.io.InputStream open() {
            try {
                Path p = getPath();
                return new InputStreamMetadataAwareImpl(Files.newInputStream(p), new FixedInputStreamMetadata(p.toString(),
                        Files.size(p)));
            } catch (IOException ex) {
                throw createOpenError(ex);
            }
        }

        @Override
        public String getContentType() {
            return null;
        }

        @Override
        public String getContentEncoding() {
            return null;
        }

        @Override
        public Instant getLastModified() {
            FileTime r = null;
            try {
                r = Files.getLastModifiedTime(getPath());
                if(r!=null){
                    return r.toInstant();
                }
            } catch (IOException e) {
                //
            }
            return null;
        }

        @Override
        public Path getPath() {
            return (Path) getSource();
        }

        @Override
        public URL getURL() {
            try {
                return getPath().toUri().toURL();
            } catch (MalformedURLException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        @Override
        public void copyTo(Path path) {
            if (!Files.isRegularFile(getPath())) {
                throw createOpenError(new FileNotFoundException(getPath().toString()));
            }
            try {
                Files.copy(getPath(), path, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        @Override
        public long length() {
            try {
                return Files.size(getPath());
            } catch (IOException e) {
                return -1;
            }
        }

        @Override
        public String toString() {
            return getPath().toString();
        }

    }

    private static class InputStream extends AbstractItem {

        public InputStream(String name, java.io.InputStream value, String typeName,NutsWorkspace ws) {
            super(name, value, false, false, typeName,ws);
        }

        @Override
        public java.io.InputStream open() {
            return (java.io.InputStream) getSource();
        }

        @Override
        public void copyTo(Path path) {
            try (java.io.InputStream o = open()) {
                Files.copy(o, path, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        @Override
        public long length() {
            return -1;
        }

        @Override
        public String toString() {
            return "InputStream(" + getSource() + ")";
        }

        @Override
        public String getContentType() {
            return null;
        }

        @Override
        public String getContentEncoding() {
            return null;
        }

        @Override
        public Instant getLastModified() {
            return null;
        }
    }

}
