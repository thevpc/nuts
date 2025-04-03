/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
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


import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.runtime.standalone.web.DefaultNWebCli;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.terminal.NTerminalModeOp;
import net.thevpc.nuts.runtime.standalone.text.ExtendedFormatAware;
import net.thevpc.nuts.runtime.standalone.text.ExtendedFormatAwarePrintWriter;
import net.thevpc.nuts.runtime.standalone.text.RawOutputStream;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.runtime.standalone.util.DoWhenExist;
import net.thevpc.nuts.runtime.standalone.util.DoWhenNotExists;
import net.thevpc.nuts.util.NCoreCollectionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util.PathInfo;
import net.thevpc.nuts.runtime.standalone.xtra.digest.NDigestUtils;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDB;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDBTableStore;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.util.NStream;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Created by vpc on 5/16/17.
 */
public class CoreIOUtils {

    public static final String MIME_TYPE_SHA1 = "text/sha-1";
    public static String newLineString = null;

    public static final URL urlOf(String any){
        try {
            return URI.create(any).toURL();
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }

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
        ExtendedFormatAwarePrintWriter s = new ExtendedFormatAwarePrintWriter(writer, term, NWorkspace.get().get());
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
        ExtendedFormatAwarePrintWriter s = new ExtendedFormatAwarePrintWriter(writer, term, NWorkspace.get().get());
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
            aw = new RawOutputStream(out, term);
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
        NSession session = NSession.get().get();
        return (session.getTerminal() == null) ? NPrintStream.NULL
                : session.getTerminal().out();
    }

    //
//    public static void delete(Path file) {
//        delete(null, file);
//    }


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
                return CoreIOUtils.urlOf(jarFile);
            } catch (Exception ex) {
                // Probably no protocol in original jar URL, like "jar:C:/mypath/myjar.jar".
                // This usually indicates that the jar file resides in the file system.
                if (!jarFile.startsWith("/")) {
                    jarFile = "/" + jarFile;
                }
                try {
                    return CoreIOUtils.urlOf("file:" + jarFile);
                } catch (Exception ex2) {
                    throw new NIOException(ex2);
                }
            }
        } else {
            String encoded = encodePath(urlPath);
            String url_tostring = url.toString();
            if (url_tostring.endsWith(encoded)) {
                try {
                    return CoreIOUtils.urlOf(url_tostring.substring(0, url_tostring.length() - encoded.length()));
                } catch (Exception ex) {
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

    public static InputStream getCachedUrlWithSHA1(String path, String sourceTypeName, boolean ignoreSha1NotFound) {
        NWorkspace workspace=NWorkspace.get().get();
        final NPath cacheBasePath = NWorkspace.of().getStoreLocation(workspace.getRuntimeId(), NStoreType.CACHE);
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
        NanoDB cachedDB = NWorkspaceExt.of().store().cacheDB();
        NanoDBTableStore<CachedURL> cacheTable
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
        long size = header.contentLength();
        Instant lastModifiedInstant = header.lastModifiedInstant();
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
                        long newSize = outPath.contentLength();
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
                        size, header.contentType(), header.getCharset(), sourceTypeName
                )).createInputStream()
                ;

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
            String ext = pp.getNameParts(NPathExtensionType.SHORT).getExtension();
            if (ext.isEmpty()) {
                String ct = NIO.of().probeContentType(temp);
                if (ct != null) {
                    List<String> e = NIO.of().findExtensionsByContentType(ct);
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

    public static boolean isObsoletePath(Path path) {
        try {
            return isObsoleteInstant(Files.getLastModifiedTime(path).toInstant());
        } catch (IOException e) {
            return true;
        }
    }

    public static boolean isObsoletePath(NPath path) {
        try {
            Instant i = path.lastModifiedInstant();
            if (i == null) {
                return false;
            }
            return isObsoleteInstant(i);
        } catch (Exception e) {
            return true;
        }
    }

    public static boolean isObsoleteInstant(Instant instant) {
        NSession session=NSession.get().get();
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
            if (replacement == null || replacement.isEmpty()) {
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

    public static PathInfo.Status tryWriteStatus(byte[] content, NPath out, String rememberMeKey) {
        return tryWrite(content, out, DoWhenExist.IGNORE, DoWhenNotExists.IGNORE, rememberMeKey);
    }

    public static PathInfo.Status tryWrite(byte[] content, NPath out, String rememberMeKey) {
        return tryWrite(content, out, DoWhenExist.ASK, DoWhenNotExists.CREATE, rememberMeKey);
    }

    public static PathInfo.Status tryWrite(byte[] content, NPath out, /*boolean doNotWrite*/ DoWhenExist doWhenExist, DoWhenNotExists doWhenNotExist, String rememberMeKey) {
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
        NSession session=NSession.get().get();
        if (old == null) {
            switch (doWhenNotExist) {
                case IGNORE: {
                    return PathInfo.Status.DISCARDED;
                }
                case CREATE: {
                    out.mkParentDirs();
                    out.writeBytes(content);
                    if (session.isPlainTrace()) {
                        NOut.resetLine().println(NMsg.ofC("create file %s", out));
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
                            NOut.resetLine().println(NMsg.ofC("create file %s", out));
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
                        NOut.resetLine().println(NMsg.ofC("update file %s", out));
                    }
                    return PathInfo.Status.OVERRIDDEN;
                }
                case ASK: {
                    if (NAsk.of()
                            .setDefaultValue(true)
                            .setRememberMeKey(rememberMeKey==null?null:("Override."+rememberMeKey))
                            .forBoolean(NMsg.ofC("override %s ?",
                                    NText.ofStyled(
                                            betterPath(out.toString()), NTextStyle.path()
                                    ))
                            ).getBooleanValue()) {
                        out.writeBytes(content);
                        if (session.isPlainTrace()) {
                            NOut.resetLine().println(NMsg.ofC("update file %s", out));
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
        return NStream.ofIterator(
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
        NSession session=NSession.get().get();
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
            in = DefaultNWebCli.prepareGlobalOpenStream(u);
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
            NSession session=NSession.get().get();
            in = NExecInput.ofStream(session.in());
        }
        if (in.getType() == NRedirectType.INHERIT) {
            NSession session=NSession.get().get();
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
        NSession session=NSession.get().get();
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
            NMemoryPrintStream out = NPrintStream.ofMem(NTerminalMode.FILTERED);
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
