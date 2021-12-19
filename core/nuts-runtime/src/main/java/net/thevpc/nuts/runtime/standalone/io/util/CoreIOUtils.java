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
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.io.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.descriptor.DefaultNutsDescriptorContentParserContext;
import net.thevpc.nuts.runtime.standalone.executor.system.PipeRunnable;
import net.thevpc.nuts.runtime.standalone.io.printstream.NutsFormattedPrintStream;
import net.thevpc.nuts.runtime.standalone.io.progress.*;
import net.thevpc.nuts.runtime.standalone.io.terminal.NutsTerminalModeOp;
import net.thevpc.nuts.runtime.standalone.repository.index.CacheDB;
import net.thevpc.nuts.runtime.standalone.text.ExtendedFormatAware;
import net.thevpc.nuts.runtime.standalone.text.ExtendedFormatAwarePrintWriter;
import net.thevpc.nuts.runtime.standalone.text.RawOutputStream;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.DoWhenExist;
import net.thevpc.nuts.runtime.standalone.util.DoWhenNotExists;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util.PathInfo;
import net.thevpc.nuts.runtime.standalone.xtra.download.DefaultHttpTransportComponent;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDB;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDBTableFile;
import net.thevpc.nuts.spi.*;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;

/**
 * Created by vpc on 5/16/17.
 */
public class CoreIOUtils {

    public static final int DEFAULT_BUFFER_SIZE = 1024;
    public static final String MIME_TYPE_SHA1 = "text/sha-1";
    public static String newLineString = null;

    public static boolean isFreeTcpPort(int port) {
        try (ServerSocket s = new ServerSocket(port)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static int detectRandomFreeTcpPort(int from, int to) {
        Set<Integer> tested = new HashSet<>();
        int interval = to - from;
        if (interval > 0) {
            while (tested.size() < interval) {
                int x = from + (int) (Math.random() * interval);
                if (!tested.contains(x)) {
                    tested.add(x);
                    if (isFreeTcpPort(x)) {
                        return x;
                    }
                }
            }
        }
        return -1;
    }

    @Deprecated
    public static int detectFirstFreeTcpPort(int from, int to) {
        for (int i = from; i < to; i++) {
            if (isFreeTcpPort(i)) {
                return i;
            }
        }
        return -1;
    }

    @Deprecated
    public static PrintWriter toPrintWriter(Writer writer, NutsSystemTerminalBase term, NutsSession session) {
        if (writer == null) {
            return null;
        }
        if (writer instanceof ExtendedFormatAware) {
            if (writer instanceof PrintWriter) {
                return (PrintWriter) writer;
            }
        }
        ExtendedFormatAwarePrintWriter s = new ExtendedFormatAwarePrintWriter(writer, term, session);
        NutsWorkspaceUtils.setSession(s, session);
        return s;
    }

    @Deprecated
    public static PrintWriter toPrintWriter(OutputStream writer, NutsSystemTerminalBase term, NutsSession session) {
        if (writer == null) {
            return null;
        }
        ExtendedFormatAwarePrintWriter s = new ExtendedFormatAwarePrintWriter(writer, term, session);
        NutsWorkspaceUtils.setSession(s, session);
        return s;
    }

    @Deprecated
    public static OutputStream convertOutputStream(OutputStream out, NutsTerminalMode expected, NutsSystemTerminalBase term, NutsSession session) {
        ExtendedFormatAware a = convertOutputStreamToExtendedFormatAware(out, expected, term, session);
        return (OutputStream) a;
    }

    @Deprecated
    public static ExtendedFormatAware convertOutputStreamToExtendedFormatAware(OutputStream out, NutsTerminalMode expected, NutsSystemTerminalBase term, NutsSession session) {
        if (out == null) {
            return null;
        }
        ExtendedFormatAware aw = null;
        if (out instanceof ExtendedFormatAware) {
            aw = (ExtendedFormatAware) out;
        } else {
            aw = new RawOutputStream(out, term, session);
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
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unsupported terminal mode %s", expected));
            }
        }
    }

    //    public static void clearMonitor(NutsPrintStream out, NutsWorkspace ws) {
//        NutsTerminalMode terminalMode = ws.boot().getBootOptions().getTerminalMode();
//        boolean bot = ws.boot().getBootOptions().isBot();
//        if (terminalMode == null) {
//            terminalMode = bot ? NutsTerminalMode.FILTERED : NutsTerminalMode.FORMATTED;
//        }
//        if (terminalMode == NutsTerminalMode.FORMATTED) {
//            out.run(NutsTerminalCommand.MOVE_LINE_START);
//        }
//    }
//    public static NutsURLHeader getURLHeader(URL url) {
//        return new DefaultNutsURLHeader(new SimpleHttpClient(url));
//    }
    public static URL asURL(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        try {
            return new URL(s);
        } catch (MalformedURLException e) {
            //
        }
        return null;
    }

    public static URL[] toURL(String[] all, NutsSession session) {
        List<URL> urls = new ArrayList<>();
        if (all != null) {
            for (String s : all) {
                if (!NutsBlankable.isBlank(s)) {
                    try {
                        URL u = new URL(s);
                        urls.add(u);
                    } catch (MalformedURLException e) {
                        //
                        try {
                            urls.add(new File(s).toURI().toURL());
                        } catch (IOException ex) {
                            throw new NutsIOException(session, ex);
                        }
                    }
                }
            }
        }
        return urls.toArray(new URL[0]);
    }

    public static URL[] toURL(File[] all, NutsSession session) {
        List<URL> urls = new ArrayList<>();
        if (all != null) {
            for (File s : all) {
                if (s != null) {
                    try {
                        urls.add(s.toURI().toURL());
                    } catch (MalformedURLException e) {
                        throw new NutsIOException(session, e);
                    }
                }
            }
        }
        return urls.toArray(new URL[0]);
    }

    public static File toFile(String url) {
        if (NutsBlankable.isBlank(url)) {
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

    public static String urlTrimLastSlash(String url) {
        int x = url.length() - 1;
        if (x == 0) {
            return "";
        }
        while (x > 0) {
            char c = url.charAt(x);
            if (c == '/') {
                x--;
            } else {
                break;
            }
        }
        return url.substring(0, x);
    }

    public static String urlTrimFirstSlash(String url) {
        int len = url.length();
        if (len == 0) {
            return "";
        }
        int x = 0;
        while (x < len) {
            char c = url.charAt(x);
            if (c == '/') {
                x++;
            } else {
                break;
            }
        }
        return url.substring(x);
    }

    public static String getURLParentPath(String ppath) {
        if (ppath == null) {
            return null;
        }
        while (ppath.endsWith("/")) {
            ppath = ppath.substring(0, ppath.length() - 1);
        }
        if (ppath.isEmpty()) {
            return null;
        }
        int i = ppath.lastIndexOf('/');
        if (i <= 0) {
            ppath = "/";
        } else {
            ppath = ppath.substring(0, i + 1);
        }
        return ppath;
    }
//    public static String getURLParent(String url) {
//        int x = url.length() - 1;
//        if (x == 0) {
//            return "";
//        }
//        while (x > 0) {
//            char c = url.charAt(x);
//            if (c == '/') {
//                x--;
//            } else {
//                break;
//            }
//        }
//        while (x > 0) {
//            char c = url.charAt(x);
//            if (c == '/') {
//                break;
//            }
//            x--;
//        }
//        return url.substring(0, x);
//    }

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

    public static boolean setExecutable(Path path, NutsSession session) {
        if (Files.exists(path) && !Files.isExecutable(path)) {
            PosixFileAttributeView p = Files.getFileAttributeView(path, PosixFileAttributeView.class);
            if (p != null) {
                try {
                    Set<PosixFilePermission> old = new HashSet<>(p.readAttributes().permissions());
                    old.add(PosixFilePermission.OWNER_EXECUTE);
                    Files.setPosixFilePermissions(path, old);
                } catch (IOException ex) {
                    throw new NutsIOException(session, ex);
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
    public static boolean mkdirs(Path p, NutsSession session) {
        if (p != null) {
            try {
                if (!Files.isDirectory(p)) {
                    Files.createDirectories(p);
                }
                return true;
            } catch (IOException ex) {
                throw new NutsIOException(session, ex);
            }
        }
        return false;
    }

    public static Path toPathFile(String s, NutsSession session) {
        if (NutsBlankable.isBlank(s)) {
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
                throw new NutsParseException(session, NutsMessage.cstyle("not a file path : %s", s));
            } catch (IOException ex) {
                throw new NutsIOException(session, ex);
            }
        }
        if (s.startsWith("http://")
                || s.startsWith("https://")
                || s.startsWith("ftp://")
                || s.startsWith("jar:")
                || s.startsWith("zip:")
                || s.startsWith("ssh:")) {
            throw new NutsParseException(session, NutsMessage.cstyle("not a file path : %s", s));
        }
        if (isURL(s)) {
            throw new NutsParseException(session, NutsMessage.cstyle("not a file path : %s", s));
        }
        return Paths.get(s);
    }

    public static boolean isPathFile(String s) {
        if (NutsBlankable.isBlank(s)) {
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
        return !isURL(s);
    }

    public static boolean isPathLocal(String s) {
        if (NutsBlankable.isBlank(s)) {
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
        return !isURL(s);
    }

    public static boolean isPathHttp(String s) {
        if (NutsBlankable.isBlank(s)) {
            return false;
        }
        return s.startsWith("http://")
                || s.startsWith("https://");
    }

    public static boolean isPathURL(String s) {
        if (NutsBlankable.isBlank(s)) {
            return false;
        }
        if (NutsBlankable.isBlank(s)
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
        return isURL(s);
    }

    public static String getNativePath(String path) {
        return path.replace('/', File.separatorChar);
    }

    public static String resolveRepositoryPath(NutsAddRepositoryOptions options, Path rootFolder, NutsSession session) {
        NutsWorkspace ws = session.getWorkspace();
        String loc = options.getLocation();
        String goodName = options.getName();
        if (NutsBlankable.isBlank(goodName)) {
            goodName = options.getConfig().getName();
        }
        if (NutsBlankable.isBlank(goodName)) {
            goodName = options.getName();
        }
        if (NutsBlankable.isBlank(goodName)) {
            if (options.isTemporary()) {
                goodName = "temp-" + UUID.randomUUID();
            } else {
                goodName = "repo-" + UUID.randomUUID();
            }
        }
        if (NutsBlankable.isBlank(loc)) {
            if (options.isTemporary()) {
                if (NutsBlankable.isBlank(goodName)) {
                    goodName = "temp";
                }
                if (goodName.length() < 3) {
                    goodName = goodName + "-repo";
                }
                loc = NutsTmp.of(session)
                        .createTempFolder(goodName + "-").toString();
            } else {
                if (NutsBlankable.isBlank(loc)) {
                    if (NutsBlankable.isBlank(goodName)) {
                        goodName = CoreNutsUtils.randomColorName() + "-repo";
                    }
                    loc = goodName;
                }
            }
        }
        return NutsPath.of(loc, session).toAbsolute(rootFolder.toString())
                .toString();
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

    public static NutsPrintStream resolveOut(NutsSession session) {
        return (session.getTerminal() == null) ? NutsPrintStream.ofNull(session)
                : session.getTerminal().out();
    }

    /**
     * @param localPath    localPath
     * @param parseOptions may include --all-mains to force lookup of all main
     *                     classes if available
     * @param session      session
     * @return descriptor
     */
    public static NutsDescriptor resolveNutsDescriptorFromFileContent(Path localPath, String[] parseOptions, NutsSession session) {
        if (parseOptions == null) {
            parseOptions = new String[0];
        }
        if (localPath != null) {
            String fileExtension = CoreIOUtils.getFileExtension(localPath.getFileName().toString());
            NutsDescriptorContentParserContext ctx = new DefaultNutsDescriptorContentParserContext(session, localPath, fileExtension, null, parseOptions);
            List<NutsDescriptorContentParserComponent> allParsers = session.extensions()
                    .setSession(session)
                    .createAllSupported(NutsDescriptorContentParserComponent.class, ctx);
            if (allParsers.size() > 0) {
                for (NutsDescriptorContentParserComponent parser : allParsers) {
                    NutsDescriptor desc = null;
                    try {
                        desc = parser.parse(ctx);
                    } catch (Exception e) {
                        NutsLoggerOp.of(CoreIOUtils.class, session)
                                .level(Level.FINE)
                                .verb(NutsLogVerb.WARNING)
                                .error(e)
                                .log(NutsMessage.cstyle("error parsing %s with %s", localPath, parser.getClass().getSimpleName() + ". Error ignored"));
                        //e.printStackTrace();
                    }
                    if (desc != null) {
                        if (!desc.isBlank()) {
                            return desc;
                        }
                        return checkDescriptor(desc, session);
                    }
                }
            }
        }
        return null;
    }

    private static NutsDescriptor checkDescriptor(NutsDescriptor nutsDescriptor, NutsSession session) {
        NutsId id = nutsDescriptor.getId();
        String groupId = id == null ? null : id.getGroupId();
        String artifactId = id == null ? null : id.getArtifactId();
        NutsVersion version = id == null ? null : id.getVersion();
        if (groupId == null || artifactId == null || NutsBlankable.isBlank(version)) {
            switch (session.getConfirm()) {
                case ASK:
                case ERROR: {
                    if (groupId == null) {
                        groupId = session.getTerminal().ask()
                                .forString(NutsMessage.cstyle("group id"))
                                .setDefaultValue(groupId)
                                .setHintMessage(NutsBlankable.isBlank(groupId) ? null : NutsMessage.plain(groupId))
                                .getValue();
                    }
                    if (artifactId == null) {
                        artifactId = session.getTerminal().ask()
                                .forString(NutsMessage.cstyle("artifact id"))
                                .setDefaultValue(artifactId)
                                .setHintMessage(NutsBlankable.isBlank(artifactId) ? null : NutsMessage.plain(artifactId))
                                .getValue();
                    }
                    if (NutsBlankable.isBlank(version)) {
                        String ov = version == null ? null : version.getValue();
                        String v = session.getTerminal().ask()
                                .forString(NutsMessage.cstyle("version"))
                                .setDefaultValue(ov)
                                .setHintMessage(NutsBlankable.isBlank(ov) ? null : NutsMessage.plain(ov))
                                .getValue();
                        version = NutsVersionParser.of(session)
                                .setAcceptBlank(true)
                                .setAcceptIntervals(true)
                                .setLenient(true).parse(v);
                    }
                    break;
                }
                case NO:
                case YES: {
                    //silently return null
                }
            }
        }
        if (groupId == null || artifactId == null || NutsBlankable.isBlank(version)) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid descriptor id %s:%s#%s", groupId, artifactId, version));
        }
        return nutsDescriptor.builder()
                .setId(NutsIdBuilder.of(session).setGroupId(groupId).setArtifactId(artifactId).setVersion(version).build())
                .build();
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
    public static void copy(Reader in, Writer out, NutsSession session) {
        copy(in, out, DEFAULT_BUFFER_SIZE, session);
    }

    /**
     * copy input to output
     *
     * @param in  entree
     * @param out sortie
     * @return size copied
     */
    public static long copy(java.io.InputStream in, OutputStream out, NutsSession session) {
        return copy(in, out, DEFAULT_BUFFER_SIZE, session);
    }

    /**
     * copy input stream to output stream using the buffer size in bytes
     *
     * @param in         entree
     * @param out        sortie
     * @param bufferSize bufferSize
     * @return size copied
     */
    public static long copy(java.io.InputStream in, OutputStream out, int bufferSize, NutsSession session) {
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
            throw new NutsIOException(session, ex);
        }
    }

    /**
     * copy input stream to output stream using the buffer size in bytes
     *
     * @param in         entree
     * @param out        sortie
     * @param bufferSize bufferSize
     */
    public static void copy(Reader in, Writer out, int bufferSize, NutsSession session) {
        char[] buffer = new char[bufferSize];
        int len;
        try {
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }

    public static String loadString(java.io.InputStream is, boolean close, NutsSession session) {
        try {
            try {
                byte[] bytes = loadByteArray(is, session);
                return new String(bytes);
            } finally {
                if (is != null && close) {
                    is.close();
                }
            }
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }

    public static String loadString(Reader is, boolean close, NutsSession session) {
        try {
            try {
                char[] bytes = loadCharArray(is, session);
                return new String(bytes);
            } finally {
                if (is != null && close) {
                    is.close();
                }
            }
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }

    public static char[] loadCharArray(Reader r, NutsSession session) {
        CharArrayWriter out = null;

        try {
            out = new CharArrayWriter();
            copy(r, out, session);
            out.flush();
            return out.toCharArray();
        } finally {
            if (out != null) {
                out.close();
            }
        }

    }

    public static byte[] loadByteArray(java.io.InputStream r, NutsSession session) {
        ByteArrayOutputStream out = null;

        try {
            try {
                out = new ByteArrayOutputStream();
                copy(r, out, session);
                out.flush();
                return out.toByteArray();
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }

    public static byte[] loadByteArray(java.io.InputStream r, boolean close, NutsSession session) {
        ByteArrayOutputStream out = null;

        try {
            try {
                out = new ByteArrayOutputStream();
                copy(r, out, session);
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
            throw new NutsIOException(session, ex);
        }
    }

    public static byte[] loadByteArray(java.io.InputStream stream, int maxSize, boolean close, NutsSession session) {
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
                    copy(stream, os, close, true, session);
                    return os.toByteArray();
                }
            } finally {
                if (close) {
                    stream.close();
                }
            }
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }

    public static long copy(java.io.InputStream from, OutputStream to, boolean closeInput, boolean closeOutput, NutsSession session) {
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
            throw new NutsIOException(session, ex);
        }
    }

    public static java.io.InputStream monitor(URL from, NutsProgressMonitor monitor, NutsSession session) {
        return monitor(
                NutsWorkspaceUtils.of(session).openURL(from),
                from, NutsTexts.of(session).ofStyled(getURLName(from), NutsTextStyle.path()),
                NutsPath.of(from, session).getContentLength(), monitor, session);
    }

    public static java.io.InputStream monitor(java.io.InputStream from, Object source, NutsString sourceName, long length, NutsProgressMonitor monitor, NutsSession session) {
        return new MonitoredInputStream(from, source, sourceName, length, monitor, session);
    }

    public static java.io.InputStream monitor(java.io.InputStream from, Object source, NutsProgressMonitor monitor, NutsSession session) {
        NutsString sourceName = null;
        long length = -1;
        NutsStreamMetadata m = NutsStreamMetadata.resolve(from);
        if (m != null) {
            sourceName = NutsTexts.of(session).toText(m.getName());
            length = m.getContentLength();
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
        NutsLogger LOG = session == null ? null : NutsLogger.of(CoreIOUtils.class, session);
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
                            LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.WARNING).log(
                                    NutsMessage.jstyle("delete file {0}", file));
                        }
                        deleted[0]++;
                    } catch (IOException e) {
                        if (LOG != null) {
                            LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.WARNING)
                                    .log(NutsMessage.jstyle("failed deleting file : {0}", file)
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
                            LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.WARNING)
                                    .log(NutsMessage.jstyle("delete folder {0}", dir));
                        }
                        deleted[1]++;
                    } catch (IOException e) {
                        if (LOG != null) {
                            LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.WARNING)
                                    .log(NutsMessage.jstyle("failed deleting folder: {0}", dir)
                                    );
                        }
                        deleted[2]++;
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
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

    //    public static CoreInput createInputSource(byte[] source, String name, NutsString formattedString, String typeName, NutsSession session) {
//        if (source == null) {
//            return null;
//        }
//        if (name == null) {
//            if (formattedString != null) {
//                name = formattedString.filteredText();
//            }
//        }
//        if (name == null) {
//            name = String.valueOf(source);
//        }
//        if (formattedString == null) {
//            formattedString = NutsTexts.of(session).forPlain(name);
//        }
//        return new ByteArrayInput(name, formattedString, source, typeName, session);
//    }
    public static boolean isValidInputStreamSource(Class type) {
        return URL.class.isAssignableFrom(type)
                || File.class.isAssignableFrom(type)
                || Path.class.isAssignableFrom(type)
                || byte[].class.isAssignableFrom(type)
                || java.io.InputStream.class.isAssignableFrom(type)
                || String.class.isAssignableFrom(type) //                || CoreInput.class.isAssignableFrom(type)
                ;
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
        NutsTransportComponent best = session.extensions()
                .createSupported(NutsTransportComponent.class, false, url);
        if (best == null) {
            best = DefaultHttpTransportComponent.INSTANCE;
        }
        return best.open(url);
    }

    public static String urlEncodeString(String s, NutsSession session) {
        if (s == null || s.trim().length() == 0) {
            return "";
        }
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new NutsIOException(session, e);
        }
    }

    public static Path resolveLocalPathFromURL(URL url) {
        try {
            return new File(url.toURI()).toPath();
        } catch (URISyntaxException e) {
            return new File(url.getPath()).toPath();
        }
    }

    public static URL resolveURLFromResource(Class cls, String urlPath, NutsSession session) {
        if (!urlPath.startsWith("/")) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unable to resolve url from %s", urlPath));
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
                    throw new NutsIOException(session, ex2);
                }
            }
        } else {
            String encoded = encodePath(urlPath, session);
            String url_tostring = url.toString();
            if (url_tostring.endsWith(encoded)) {
                try {
                    return new URL(url_tostring.substring(0, url_tostring.length() - encoded.length()));
                } catch (IOException ex) {
                    throw new NutsIOException(session, ex);
                }
            }
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unable to resolve url from %s", urlPath));
        }
    }

    private static String encodePath(String path, NutsSession session) {
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
                    throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unable to encode %s", t), ex);
                }
            }
        }
        return encoded.toString();
    }

    public static File resolveLocalFileFromResource(Class cls, String url, NutsSession session) {
        return resolveLocalFileFromURL(resolveURLFromResource(cls, url, session));
    }

    public static File resolveLocalFileFromURL(URL url) {
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            return new File(url.getPath());
        }
    }

    public static byte[] evalMD5(String input, NutsSession session) {
        byte[] bytesOfMessage = input.getBytes(StandardCharsets.UTF_8);
        return evalMD5(bytesOfMessage, session);
    }

    public static String evalMD5Hex(Path path, NutsSession session) {
        return NutsUtilStrings.toHexString(evalMD5(path, session));
    }

    public static byte[] evalMD5(Path path, NutsSession session) {
        try (java.io.InputStream is = new BufferedInputStream(Files.newInputStream(path))) {
            return evalMD5(is, session);
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }

    public static String evalMD5Hex(java.io.InputStream input, NutsSession session) {
        return NutsUtilStrings.toHexString(evalMD5(input, session));
    }

    public static byte[] evalHash(java.io.InputStream input, String algo, NutsSession session) {

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
                throw new NutsIOException(session, e);
            }
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new NutsIOException(session, new IOException(e));
        }
    }

    public static byte[] evalMD5(java.io.InputStream input, NutsSession session) {

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
                throw new NutsIOException(session, e);
            }
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new NutsIOException(session, e);
        }
    }

    public static byte[] evalMD5(byte[] bytesOfMessage, NutsSession session) {
        try {
            MessageDigest md;
            md = MessageDigest.getInstance("MD5");
            return md.digest(bytesOfMessage);
        } catch (NoSuchAlgorithmException e) {
            throw new NutsIOException(session, e);
        }
    }

    public static String evalSHA1Hex(NutsPath file, NutsSession session) {
        try {
            try (InputStream is = file.getInputStream()) {
                return evalSHA1Hex(is, true, session);
            }
        } catch (IOException e) {
            throw new NutsIOException(session, e);
        }
    }

    public static String evalSHA1(File file, NutsSession session) {
        try {
            return evalSHA1Hex(new FileInputStream(file), true, session);
        } catch (FileNotFoundException e) {
            throw new NutsIOException(session, e);
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

    public static char[] evalSHA1(char[] input, NutsSession session) {
        byte[] bytes = charsToBytes(input);
        char[] r = evalSHA1HexChars(new ByteArrayInputStream(bytes), true, session);
        Arrays.fill(bytes, (byte) 0);
        return r;
    }

    public static String evalSHA1(String input, NutsSession session) {
        return evalSHA1Hex(new ByteArrayInputStream(input.getBytes()), true, session);
    }

    public static String evalSHA1Hex(java.io.InputStream input, boolean closeStream, NutsSession session) {
        return NutsUtilStrings.toHexString(evalSHA1(input, closeStream, session));
    }

    public static char[] evalSHA1HexChars(java.io.InputStream input, boolean closeStream, NutsSession session) {
        return NutsUtilStrings.toHexString(evalSHA1(input, closeStream, session)).toCharArray();
    }

    public static byte[] evalSHA1(java.io.InputStream input, boolean closeStream, NutsSession session) {
        try {
            MessageDigest sha1 = null;
            try {
                sha1 = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException ex) {
                throw new NutsIOException(session, ex);
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
                throw new NutsIOException(session, e);
            }
            return sha1.digest();
        } finally {
            if (closeStream) {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException ex) {
                        throw new NutsIOException(session, ex);
                    }
                }
            }
        }
    }

    public static InputStream getCachedUrlWithSHA1(String path, String sourceTypeName, boolean ignoreSha1NotFound, NutsSession session) {
        final NutsPath cacheBasePath = session.locations().getStoreLocation(session.getWorkspace().getRuntimeId(), NutsStoreLocation.CACHE);
        final NutsPath urlContent = cacheBasePath.resolve("urls-content");
        String sha1 = null;
        try {
            ByteArrayOutputStream t = new ByteArrayOutputStream();
            NutsCp.of(session)
                    .from(path + ".sha1").to(t).run();
            sha1 = t.toString().trim();
        } catch (NutsIOException ex) {
            if (!ignoreSha1NotFound) {
                throw ex;
            }
        }
        NanoDB cachedDB = CacheDB.of(session);
        NanoDBTableFile<CachedURL> cacheTable
                = cachedDB.tableBuilder(CachedURL.class, session).setNullable(false)
                .addAllFields()
                .addIndices("url")
                .getOrCreate();

//        final PersistentMap<String, String> cu=getCachedUrls(session);
        CachedURL old = cacheTable.findByIndex("url", path, session).findFirst().orElse(null);
//        String cachedSha1 = cu.get("sha1://" + path);
//        String oldLastModified =cu.get("lastModified://" + path);
//        String oldSize =cu.get("length://" + path);
        if (sha1 != null) {
            if (old != null && sha1.equalsIgnoreCase(old.sha1)) {
                String cachedID = old.path;
                if (cachedID != null) {
                    NutsPath p = urlContent.resolve(cachedID);
                    if (p.exists()) {
                        return p.getInputStream();
                    }
                }
            }
        }

        NutsPath header = null;
        long size = -1;
        long lastModified = -1;
        NutsTransportConnection f = CoreIOUtils.getHttpClientFacade(session, path);
        try {

            header = f.getPath();
            size = header.getContentLength();
            Instant lastModifiedInstant = header.getLastModifiedInstant();
            if (lastModifiedInstant != null) {
                lastModified = lastModifiedInstant.toEpochMilli();
            }
        } catch (Exception ex) {
            //ignore error
        }

        //when sha1 was not resolved check size and last modification
        if (sha1 == null) {
            if (old != null && old.lastModified != -1 && old.lastModified == lastModified) {
                if (old != null && old.size == size) {
                    String cachedID = old.path;
                    if (cachedID != null) {
                        NutsPath p = urlContent.resolve(cachedID);
                        if (p.exists()) {
                            return p.getInputStream();
                        }
                    }
                }
            }
        }

        final String s = UUID.randomUUID().toString();
        final NutsPath outPath = urlContent.resolve(s + "~");
        urlContent.mkdirs();
        OutputStream p = outPath.getOutputStream();
        long finalLastModified = lastModified;
        InputStreamTee ist = new InputStreamTee(f.open(), p, () -> {
            if (outPath.exists()) {
                CachedURL ccu = new CachedURL();
                ccu.url = path;
                ccu.path = s;
                ccu.sha1 = evalSHA1Hex(outPath, session);
                long newSize = outPath.getContentLength();
                ccu.size = newSize;
                ccu.lastModified = finalLastModified;
                NutsPath newLocalPath = urlContent.resolve(s);
                try {
                    Files.move(outPath.toFile(), newLocalPath.toFile(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (IOException ex) {
                    throw new NutsIOException(session, ex);
                }
                cacheTable.add(ccu, session);
                cacheTable.flush(session);
            }
        });
        return InputStreamMetadataAwareImpl.of(ist, new NutsDefaultStreamMetadata(
                        path,
                        NutsTexts.of(session).ofStyled(path, NutsTextStyle.path()),
                        size, NutsPath.of(path, session).getContentType(), sourceTypeName
                )
        );

    }

    public static void storeProperties(Map<String, String> props, OutputStream out, boolean sort, NutsSession session) {
        storeProperties(props, new OutputStreamWriter(out), sort, session);
    }

    public static void storeProperties(Map<String, String> props, Writer w, boolean sort, NutsSession session) {
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
            throw new NutsIOException(session, ex);
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
                        buffer.append(NutsUtilStrings.toHexChar((c >> 12) & 0xF));
                        buffer.append(NutsUtilStrings.toHexChar((c >> 8) & 0xF));
                        buffer.append(NutsUtilStrings.toHexChar((c >> 4) & 0xF));
                        buffer.append(NutsUtilStrings.toHexChar(c & 0xF));
                    } else {
                        buffer.append(c);
                    }
                }
            }
        }
        return buffer.toString();
    }

    public static Path toPathInputSource(NutsStreamOrPath is, List<Path> tempPaths, NutsSession session) {
        if (is.isPath() && is.getPath().isFile()) {
            return is.getPath().toFile();
        }
        Path temp = NutsTmp.of(session)
                .createTempFile(getURLName(is.getName())).toFile();
        NutsCp a = NutsCp.of(session).removeOptions(NutsPathOption.SAFE);
        if (is.isPath()) {
            a.from(is.getPath());
        } else {
            a.from(is.getInputStream());
        }
        a.to(temp).setSession(session).run();
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

    public static void copyFolder(Path src, Path dest, NutsSession session) {
        try {
            Files.walk(src)
                    .forEach(source -> copy(source, dest.resolve(src.relativize(source))));
        } catch (IOException e) {
            throw new NutsIOException(session, e);
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
                return new DefaultNutsInputStreamProgressFactory();
            case DEFAULT:
                return new DefaultNutsProgressFactory();
        }
        return new DefaultNutsProgressFactory();
    }

    public static NutsProgressMonitor createProgressMonitor(MonitorType mt, Object source, Object sourceOrigin, NutsSession session,
                                                            boolean logProgress,
                                                            boolean traceProgress,
                                                            NutsProgressFactory progressFactory) {
        List<NutsProgressMonitor> all = new ArrayList<>();
        if (logProgress) {
            all.add(createLogProgressMonitorFactory(mt).create(source, sourceOrigin, session));
        }
        if (traceProgress) {
            all.add(new TraceNutsProgressMonitor());
        }
        if (progressFactory != null) {
            all.add(progressFactory.create(source, sourceOrigin, session));
        }
        if (all.isEmpty()) {
            return new SilentNutsProgressMonitor();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NutsProgressMonitorList(all.toArray(new NutsProgressMonitor[0]));
    }

    public static Path toPath(String path) {
        return NutsBlankable.isBlank(path) ? null : Paths.get(path);
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

    public static boolean isObsoletePath(NutsSession session, Path path) {
        try {
            return isObsoleteInstant(session, Files.getLastModifiedTime(path).toInstant());
        } catch (IOException e) {
            return true;
        }
    }

    public static boolean isObsoletePath(NutsSession session, NutsPath path) {
        try {
            Instant i = path.getLastModifiedInstant();
            if (i == null) {
                return false;
            }
            return isObsoleteInstant(session, i);
        } catch (Exception e) {
            return true;
        }
    }

    //    public static PrintStream createFormattedPrintStream(OutputStream out, NutsWorkspace session) {
//        PrintStream supported = (PrintStream) session.setExtension(true).createSupported(
//                NutsFormattedPrintStream.class,
//                new DefaultNutsSupportLevelContext<>(session, out),
//                new Class[]{OutputStream.class}, new Object[]{out});
//        if (supported == null) {
//            throw new NutsExtensionNotFoundException(session, NutsFormattedPrintStream.class, "FormattedPrintStream");
//        }
//        NutsWorkspaceUtils.of(session).setWorkspace(supported);
//        return supported;
//    }
//
//    public static PrintStream createFilteredPrintStream(OutputStream out, NutsWorkspace session) {
//        return new NutsPrintStreamFiltered(out);
//    }
//    public static PrintStream convertPrintStream(OutputStream out, NutsTerminalMode m, NutsWorkspace session) {
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
//                            throw new NutsUnsupportedEnumException(session, am);
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
//                            throw new NutsUnsupportedEnumException(session, am);
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
//    public static NutsPrintStream out(NutsWorkspace ws) {
//        DefaultNutsIOManager io = (DefaultNutsIOManager) ws.io();
//        NutsPrintStream out = io.getModel().getCurrentStdout();
//        return out == null ? CoreIOUtils.out : out;
//    }
//
//    public static NutsPrintStream err(NutsWorkspace ws) {
//        DefaultNutsIOManager io = (DefaultNutsIOManager) ws.io();
//        NutsPrintStream err = io.getModel().getCurrentStderr();
//        return err == null ? CoreIOUtils.err : err;
//    }
//    public static java.io.InputStream in(NutsWorkspace ws) {
//        DefaultNutsIOManager io = (DefaultNutsIOManager) ws.io();
//        java.io.InputStream in = io.getModel().stdin();
//        return in == null ? System.in : in;
//    }
    public static boolean isObsoleteInstant(NutsSession session, Instant instant) {
        if (session.getExpireTime() != null) {
            return instant == null || instant.isBefore(session.getExpireTime());
        }
        return false;
    }

    //    public static List<String> headFrom(NutsInput input, int count) {
//        return input.lines().limit(count).collect(Collectors.toList());
//    }
//
//    public static List<String> tailFrom(NutsInput input, int max) {
//        LinkedList<String> lines = new LinkedList<>();
//        BufferedReader br = new BufferedReader(new InputStreamReader(input.open()));
//        String line;
//        try {
//            int count = 0;
//            while ((line = br.readLine()) != null) {
//                lines.add(line);
//                count++;
//                if (count > max) {
//                    lines.remove();
//                }
//            }
//        } catch (IOException e) {
//            throw new NutsIOException(session,e);
//        }
//        return lines;
//    }
//    public static Stream<String> linesFrom(NutsInput input) {
//        BufferedReader br = new BufferedReader(new InputStreamReader(input.open()));
//        Iterator<String> sourceIterator = new Iterator<String>() {
//            String line = null;
//
//            @Override
//            public boolean hasNext() {
//                boolean hasNext = false;
//                try {
//                    try {
//                        line = br.readLine();
//                    } catch (IOException e) {
//                        throw new NutsIOException(session,e);
//                    }
//                    hasNext = line != null;
//                    return hasNext;
//                } finally {
//                    if (!hasNext) {
//                        try {
//                            br.close();
//                        } catch (IOException e) {
//                            throw new NutsIOException(session,e);
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public String next() {
//                return line;
//            }
//        };
//        return StreamSupport.stream(
//                Spliterators.spliteratorUnknownSize(sourceIterator, Spliterator.ORDERED),
//                false);
//    }
    public static String loadFileContentLenientString(Path out) {
        return new String(loadFileContentLenient(out));
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

//    public static PipeRunnable pipeOld(String name, String cmd, String desc, final NonBlockingInputStream in, final OutputStream out, NutsSession session) {
//        PipeRunnable p = new PipeRunnable(name, cmd, desc, in, out, true, session);
//        session.config().executorService().submit(p);
//        return p;
//    }

    public static PipeRunnable pipe(String name, String cmd, String desc, final NonBlockingInputStream in, final OutputStream out, NutsSession session) {
        return new PipeRunnable(name, cmd, desc, in, out, true, session);
    }

    public static Path sysWhich(String commandName) {
        Path[] p = sysWhichAll(commandName);
        if (p.length > 0) {
            return p[0];
        }
        return null;
    }

    public static Path[] sysWhichAll(String commandName) {
        if (commandName == null || commandName.isEmpty()) {
            return new Path[0];
        }
        List<Path> all = new ArrayList<>();
        String p = System.getenv("PATH");
        if (p != null) {
            for (String s : p.split(File.pathSeparator)) {
                try {
                    if (!s.trim().isEmpty()) {
                        Path c = Paths.get(s, commandName);
                        if (Files.isRegularFile(c)) {
                            if (Files.isExecutable(c)) {
                                all.add(c);
                            }
                        }
                    }
                } catch (Exception ex) {
                    //ignore
                }
            }
        }
        return all.toArray(new Path[0]);
    }

    //    public static class DefaultMultiReadItem implements MultiInput {
//
//        private CoreInput base;
//        private byte[] content;
//        private String typeName;
//
//        public DefaultMultiReadItem(CoreInput base, String typeName) {
//            this.base = base;
//            content = CoreIOUtils.loadByteArray(base.open());
//            this.typeName = typeName;
//        }
//
//        @Override
//        public NutsString getFormattedName() {
//            return base.getFormattedName();
//        }
//
//        @Override
//        public String getName() {
//            return base.getName();
//        }
//
//        @Override
//        public String getTypeName() {
//            return typeName;
//        }
//
//        @Override
//        public void close() {
//            base.close();
//        }
//
//        @Override
//        public java.io.InputStream open() {
//            return new NamedByteArrayInputStream(content, base.getName());
//        }
//
//        @Override
//        public long length() {
//            return content.length;
//        }
//
//        @Override
//        public Object getSource() {
//            return base.getSource();
//        }
//
//        @Override
//        public boolean isFile() {
//            return base.isFile();
//        }
//
//        @Override
//        public Path getFilePath() {
//            return base.getFilePath();
//        }
//
//        @Override
//        public boolean isURL() {
//            return base.isURL();
//        }
//
//        @Override
//        public URL getURL() {
//            return base.getURL();
//        }
//
//        @Override
//        public String getContentType() {
//            return base.getContentType();
//        }
//
//        @Override
//        public String getContentEncoding() {
//            return base.getContentEncoding();
//        }
//
//        @Override
//        public Instant getLastModifiedInstant() {
//            return base.getLastModifiedInstant();
//        }
//
//        @Override
//        public Stream<String> lines() {
//            return linesFrom(this);
//        }
//
//        @Override
//        public List<String> head(int count) {
//            return headFrom(this, count);
//        }
//
//        @Override
//        public List<String> tail(int count) {
//            return tailFrom(this, count);
//        }
//
//        @Override
//        public void copyTo(Path path) {
//            base.copyTo(path);
//        }
//
//        @Override
//        public MultiInput multi() {
//            return this;
//        }
//
//        @Override
//        public String toString() {
//            return base.toString();
//        }
//    }
//    public static abstract class AbstractMultiReadItem
//            extends AbstractItem
//            implements MultiInput {
//
//        public AbstractMultiReadItem(String name, NutsString formattedName, Object value, boolean path, boolean url, String typeName, NutsSession session) {
//            super(name, formattedName, value, path, url, typeName, session);
//        }
//
//    }
//    public static abstract class AbstractItem implements CoreInput {
//
//        Object value;
//        boolean path;
//        boolean url;
//        String name;
//        NutsString formattedName;
//        String typeName;
//        NutsSession session;
//
//        public AbstractItem(String name, NutsString formattedName, Object value, boolean path, boolean url, String typeName, NutsSession session) {
//            this.name = name;
//            this.value = value;
//            this.path = path;
//            this.formattedName = formattedName;
//            this.url = url;
//            this.typeName = typeName;
//            this.session = session;
//        }
//
//        @Override
//        public NutsString getFormattedName() {
//            return formattedName;
//        }
//
//        @Override
//        public String getName() {
//            return name;
//        }
//
//        @Override
//        public String getTypeName() {
//            return typeName;
//        }
//
//        @Override
//        public void close() {
//        }
//
//        @Override
//        public abstract java.io.InputStream open();
//
//        @Override
//        public Object getSource() {
//            return value;
//        }
//
//        public boolean isFile() {
//            return path;
//        }
//
//        @Override
//        public Path getFilePath() {
//            throw new NutsUnsupportedOperationException(session,NutsMessage.cstyle("unsupported operation '%s'","getFilePath"));
//        }
//
//        public boolean isURL() {
//            return url;
//        }
//
//        @Override
//        public URL getURL() {
//            throw new NutsUnsupportedOperationException(session,NutsMessage.cstyle("unsupported operation '%s'","getURL"));
//        }
//
//        @Override
//        public Stream<String> lines() {
//            return linesFrom(this);
//        }
//
//        @Override
//        public List<String> head(int count) {
//            return headFrom(this, count);
//        }
//
//        @Override
//        public List<String> tail(int count) {
//            return tailFrom(this, count);
//        }
//
//        protected NutsIOException createOpenError(Exception ex) {
//            String n = getTypeName();
//            if (n == null) {
//                n = getName();
//            }
//            String s = toString();
//            if (s.equals(n)) {
//                return new NutsIOException(session, NutsMessage.cstyle("%s not found", n), ex);
//            }
//            return new NutsIOException(session, NutsMessage.cstyle("%s not found : %s", n, toString()), ex);
//        }
//
//        @Override
//        public void copyTo(Path path) {
//            try {
//                Files.copy(open(), path, StandardCopyOption.REPLACE_EXISTING);
//            } catch (IOException ex) {
//                throw new NutsIOException(session,ex);
//            }
//        }
//
//        @Override
//        public MultiInput multi() {
//            if (this instanceof MultiInput) {
//                return (MultiInput) this;
//            }
//            return (MultiInput) new DefaultMultiReadItem(this, getTypeName());
//        }
//    }
//    private static class ByteArrayInput extends AbstractMultiReadItem {
//
//        public ByteArrayInput(String name, NutsString formattedName, byte[] value, String typeName, NutsSession session) {
//            super(name, formattedName, value, false, false, typeName, session);
//        }
//
//        @Override
//        public java.io.InputStream open() {
//            byte[] bytes = (byte[]) this.getSource();
//            return new InputStreamMetadataAwareImpl(new NamedByteArrayInputStream(bytes, name), new NutsDefaultStreamMetadata(name, bytes.length));
//        }
//
//        @Override
//        public long length() {
//            byte[] bytes = (byte[]) this.getSource();
//            return bytes.length;
//        }
//
//        @Override
//        public String getContentType() {
//            return null;
//        }
//
//        @Override
//        public String getContentEncoding() {
//            return null;
//        }
//
//        @Override
//        public Instant getLastModifiedInstant() {
//            return null;
//        }
//
//        @Override
//        public String toString() {
//            return "bytes://" + ((byte[]) this.getSource()).length;
//        }
//    }
//    public static class URLInput extends AbstractItem {
//
//        private NutsPath cachedNutsURLHeader = null;
//
//        public URLInput(String name, NutsString formattedName, URL value, String typeName, NutsSession session) {
//            super(name, formattedName, value, false, true, typeName, session);
//        }
//
//        @Override
//        public java.io.InputStream open() {
//            try {
//                URL u = getURL();
//                if (CoreIOUtils.isPathHttp(u.toString())) {
//                    try {
//                        NutsPath uh = getPath();
//                        return new InputStreamMetadataAwareImpl(
//                                NutsWorkspaceUtils.of(session).openURL(u),
//                                new NutsDefaultStreamMetadata(u.toString(), uh == null ? -1 : uh.getContentLength()));
//                    } catch (Exception ex) {
//                        //ignore
//                    }
//                }
//                return NutsWorkspaceUtils.of(session).openURL(u);
//            } catch (Exception ex) {
//                throw createOpenError(ex);
//            }
//        }
//
//        @Override
//        public URL getURL() {
//            return (URL) getSource();
//        }
//
//        protected NutsPath getPath() {
//            if (cachedNutsURLHeader == null) {
//                URL u = getURL();
//                if (CoreIOUtils.isPathHttp(u.toString())) {
//                    try {
//                        NutsTransportConnection hf = DefaultHttpTransportComponent.INSTANCE.open(u.toString());
//                        cachedNutsURLHeader = hf.getPath();
//                    } catch (Exception ex) {
//                        //ignore
//                    }
//                }
//            }
//            return cachedNutsURLHeader;
//        }
//
//        @Override
//        public long length() {
//            URL u = getURL();
//            if (CoreIOUtils.isPathHttp(u.toString())) {
//                try {
//                    NutsPath uh = getPath();
//                    return uh == null ? -1 : uh.getContentLength();
//                } catch (Exception ex) {
//                    //ignore
//                }
//            }
//            File file = toFile(u);
//            if (file != null) {
//                return file.length();
//            }
//            return -1;
//        }
//
//        @Override
//        public String getContentType() {
//            NutsPath r = null;
//            try {
//                r = getPath();
//            } catch (Exception ex) {
//                //
//            }
//            if (r != null) {
//                return r.getContentType();
//            }
//            return null;
//        }
//
//        @Override
//        public String getContentEncoding() {
//            NutsPath r = null;
//            try {
//                r = getPath();
//            } catch (Exception ex) {
//                //
//            }
//            if (r != null) {
//                return r.getContentEncoding();
//            }
//            return null;
//        }
//
//        @Override
//        public Instant getLastModifiedInstant() {
//            NutsPath r = null;
//            try {
//                r = getPath();
//            } catch (Exception ex) {
//                //
//            }
//            if (r != null) {
//                return r.getLastModifiedInstant();
//            }
//            return null;
//        }
//
//        @Override
//        public String toString() {
//            return getURL().toString();
//        }
//
//    }
//    private static class PathInput extends AbstractMultiReadItem {
//
//        public PathInput(String name, NutsString formattedName, Path value, String typeName, NutsSession session) {
//            super(name, formattedName, value, true, true, typeName, session);
//        }
//
//        @Override
//        public java.io.InputStream open() {
//            try {
//                Path p = this.getFilePath();
//                return new InputStreamMetadataAwareImpl(Files.newInputStream(p), new NutsDefaultStreamMetadata(p.toString(),
//                        Files.size(p)));
//            } catch (IOException ex) {
//                throw createOpenError(ex);
//            }
//        }
//
//        @Override
//        public Path getFilePath() {
//            return (Path) getSource();
//        }
//
//        @Override
//        public URL getURL() {
//            try {
//                return this.getFilePath().toUri().toURL();
//            } catch (MalformedURLException ex) {
//                throw new NutsIOException(session,ex);
//            }
//        }
//
//        @Override
//        public void copyTo(Path path) {
//            if (!Files.isRegularFile(this.getFilePath())) {
//                throw createOpenError(new FileNotFoundException(this.getFilePath().toString()));
//            }
//            try {
//                Files.copy(this.getFilePath(), path, StandardCopyOption.REPLACE_EXISTING);
//            } catch (IOException ex) {
//                throw new NutsIOException(session,ex);
//            }
//        }
//
//        @Override
//        public long length() {
//            try {
//                return Files.size(this.getFilePath());
//            } catch (IOException e) {
//                return -1;
//            }
//        }
//
//        @Override
//        public String getContentType() {
//            return null;
//        }
//
//        @Override
//        public String getContentEncoding() {
//            return null;
//        }
//
//        @Override
//        public Instant getLastModifiedInstant() {
//            FileTime r = null;
//            try {
//                r = Files.getLastModifiedTime(this.getFilePath());
//                if (r != null) {
//                    return r.toInstant();
//                }
//            } catch (IOException e) {
//                //
//            }
//            return null;
//        }
//
//        @Override
//        public String toString() {
//            return this.getFilePath().toString();
//        }
//
//    }
//    public static class InputStream extends AbstractItem {
//
//        public InputStream(String name, NutsString formattedName, java.io.InputStream value, String typeName, NutsSession session) {
//            super(name, formattedName, value, false, false, typeName, session);
//        }
//
//        @Override
//        public java.io.InputStream open() {
//            return (java.io.InputStream) getSource();
//        }
//
//        @Override
//        public void copyTo(Path path) {
//            try (java.io.InputStream o = open()) {
//                Files.copy(o, path, StandardCopyOption.REPLACE_EXISTING);
//            } catch (IOException ex) {
//                throw new NutsIOException(session,ex);
//            }
//        }
//
//        @Override
//        public long length() {
//            return -1;
//        }
//
//        @Override
//        public String getContentType() {
//            return null;
//        }
//
//        @Override
//        public String getContentEncoding() {
//            return null;
//        }
//
//        @Override
//        public Instant getLastModifiedInstant() {
//            return null;
//        }
//
//        @Override
//        public String toString() {
//            return "input-stream://" + getSource();
//        }
//    }
    public static byte[] readBestEffort(int len, java.io.InputStream in, NutsSession session) {
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return new byte[0];
        }
        byte[] buf = new byte[len];
        int count = readBestEffort(buf, 0, len, in, session);
        if (count == len) {
            return buf;
        }
        byte[] buf2 = new byte[count];
        System.arraycopy(buf, 0, buf2, 0, count);
        return buf2;
    }

    public static int readBestEffort(byte[] b, int off, int len, java.io.InputStream in, NutsSession session) {
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }
        int n = 0;
        while (n < len) {
            int count = 0;
            try {
                count = in.read(b, off + n, len - n);
            } catch (IOException e) {
                throw new NutsIOException(session, e);
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

    public static boolean compareContent(Path file1, Path file2, NutsSession session) {
        if (Files.isRegularFile(file1) && Files.isRegularFile(file2)) {
            try {
                if (Files.size(file1) == Files.size(file2)) {
                    int max = 2048;
                    byte[] b1 = new byte[max];
                    byte[] b2 = new byte[max];
                    try (java.io.InputStream in1 = Files.newInputStream(file1)) {
                        try (java.io.InputStream in2 = Files.newInputStream(file1)) {
                            while (true) {
                                int c1 = readBestEffort(b1, 0, b1.length, in1, session);
                                int c2 = readBestEffort(b2, 0, b2.length, in2, session);
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
                throw new NutsIOException(session, e);
            }
        }
        return false;
    }

    public static InputStream createBytesStream(byte[] bytes, NutsMessage message, String contentType, String kind, NutsSession session) {
        return InputStreamMetadataAwareImpl.of(
                new ByteArrayInputStream(bytes),
                new NutsDefaultStreamMetadata(
                        message,
                        bytes.length,
                        contentType,
                        kind,
                        session
                )
        );
    }

    public static boolean isPathFolder(String p) {
        if (p == null) {
            return false;
        }
        return (p.equals(".") || p.equals("..") || p.endsWith("/") || p.endsWith("\\"));
    }

    public static boolean isPath(String p) {
        if (p == null) {
            return false;
        }
        return (p.equals(".") || p.equals("..") || p.contains("/") || p.contains("\\"));
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

    public static byte[] loadFile(NutsPath out) {
        if (out.isRegularFile()) {
            try {
                return out.readAllBytes();
            } catch (Exception ex) {
                //ignore
            }
        }
        return null;
    }

    public static PathInfo.Status tryWriteStatus(byte[] content, NutsPath out, NutsSession session) {
        return tryWrite(content, out, DoWhenExist.IGNORE, DoWhenNotExists.IGNORE, session);
    }

    public static PathInfo.Status tryWrite(byte[] content, NutsPath out, NutsSession session) {
        return tryWrite(content, out, DoWhenExist.ASK, DoWhenNotExists.CREATE, session);
    }

    public static PathInfo.Status tryWrite(byte[] content, NutsPath out, /*boolean doNotWrite*/ DoWhenExist doWhenExist, DoWhenNotExists doWhenNotExist, NutsSession session) {
        if (doWhenExist == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.plain("missing doWhenExist"));
        }
        if (doWhenNotExist == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.plain("missing doWhenNotExist"));
        }
//        System.err.println("[DEBUG] try write "+out);
        out = out.toAbsolute().normalize();
        byte[] old = loadFile(out);
        if (old == null) {
            switch (doWhenNotExist) {
                case IGNORE: {
                    return PathInfo.Status.DISCARDED;
                }
                case CREATE: {
                    out.mkParentDirs();
                    out.writeBytes(content);
                    if (session.isPlainTrace()) {
                        session.out().resetLine().printf("create file %s%n", out);
                    }
                    return PathInfo.Status.CREATED;
                }
                case ASK: {
                    if (session.getTerminal().ask()
                            .resetLine()
                            .setDefaultValue(true).setSession(session)
                            .forBoolean("create %s ?",
                                    NutsTexts.of(session).ofStyled(
                                            betterPath(out.toString()), NutsTextStyle.path()
                                    )
                            ).getBooleanValue()) {
                        out.mkParentDirs();
                        out.writeBytes(content);
                        if (session.isPlainTrace()) {
                            session.out().resetLine().printf("create file %s%n", out);
                        }
                        return PathInfo.Status.CREATED;
                    } else {
                        return PathInfo.Status.DISCARDED;
                    }
                }
                default: {
                    throw new NutsUnsupportedEnumException(session, doWhenNotExist);
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
                        session.out().resetLine().printf("update file %s%n", out);
                    }
                    return PathInfo.Status.OVERRIDDEN;
                }
                case ASK: {
                    if (session.getTerminal().ask()
                            .resetLine()
                            .setDefaultValue(true).setSession(session)
                            .forBoolean("override %s ?",
                                    NutsTexts.of(session).ofStyled(
                                            betterPath(out.toString()), NutsTextStyle.path()
                                    )
                            ).getBooleanValue()) {
                        out.writeBytes(content);
                        if (session.isPlainTrace()) {
                            session.out().resetLine().printf("update file %s%n", out);
                        }
                        return PathInfo.Status.OVERRIDDEN;
                    } else {
                        return PathInfo.Status.DISCARDED;
                    }
                }
                default: {
                    throw new NutsUnsupportedEnumException(session, doWhenExist);
                }
            }
        }
    }

    public static Set<CopyOption> asCopyOptions(Set<NutsPathOption> noptions) {
        Set<CopyOption> joptions = new HashSet<>();

        for (NutsPathOption option : noptions) {
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

    public static NutsStream<String> safeLines(byte[] bytes, NutsSession session) {
        return NutsStream.of(
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
                }, session
        );
    }

    public static BufferedReader bufferedReaderOf(byte[] bytes) {
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
    }

    public enum MonitorType {
        STREAM,
        DEFAULT,
    }

    public static class CachedURL {

        String url;
        String path;
        String sha1;
        long lastModified;
        long size;
    }
}
