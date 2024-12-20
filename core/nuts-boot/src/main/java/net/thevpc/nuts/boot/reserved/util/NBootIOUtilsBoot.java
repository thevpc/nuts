/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
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
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.boot.reserved.util;

import net.thevpc.nuts.boot.NBootException;
import net.thevpc.nuts.boot.NBootOptionsInfo;
import net.thevpc.nuts.boot.NBootCancelException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

public class NBootIOUtilsBoot {

    public static final String DELETE_FOLDERS_HEADER = "ATTENTION ! You are about to delete nuts workspace files.";

    public static String getAbsolutePath(String path) {
        return new File(path).toPath().toAbsolutePath().normalize().toString();
    }

    public static String readStringFromFile(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }

    public static InputStream openStream(URL url, NBootLog bLog) {
        return NBootMonitoredURLInputStream.of(url, bLog);
    }

    public static byte[] loadStream(InputStream stream, NBootLog bLog) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        copy(stream, bos, true, true);
        return bos.toByteArray();
    }

    public static ByteArrayInputStream preloadStream(InputStream stream, NBootLog bLog) throws IOException {
        return new ByteArrayInputStream(loadStream(stream, bLog));
    }

    public static Properties loadURLProperties(Path path, NBootLog bLog) {
        Properties props = new Properties();
        if (Files.isRegularFile(path)) {
            try (InputStream is = Files.newInputStream(path)) {
                props.load(is);
            } catch (IOException ex) {
                return new Properties();
            }
        }
        return props;
    }

    public static Properties loadURLProperties(URL url, File cacheFile, boolean useCache, NBootLog bLog) {
        NBootChronometer chrono = NBootChronometer.startNow();
        Properties props = new Properties();
        InputStream inputStream = null;
        File urlFile = toFile(url);
        try {
            if (useCache) {
                if (cacheFile != null && cacheFile.isFile()) {
                    try {
                        inputStream = new FileInputStream(cacheFile);
                        props.load(inputStream);
                        chrono.stop();
                        NBootDuration time = chrono.getDuration();
                        bLog.with().level(Level.CONFIG).verbSuccess().log(NBootMsg.ofC("load cached file from  %s" + ((!time.isZero()) ? " (time %s)" : ""), cacheFile.getPath(), chrono));
                        return props;
                    } catch (IOException ex) {
                        bLog.with().level(Level.CONFIG).verbFail().log(NBootMsg.ofC("invalid cache. Ignored %s : %s", cacheFile.getPath(), ex.toString()));
                    } finally {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Exception ex) {
                                if (bLog != null) {
                                    bLog.with().level(Level.FINE).verbFail().error(ex).log(NBootMsg.ofPlain("unable to close stream"));
                                }
                                //
                            }
                        }
                    }
                }
            }
            inputStream = null;
            try {
                if (url != null) {
                    String urlString = url.toString();
                    inputStream = openStream(url, bLog);
                    if (inputStream != null) {
                        props.load(inputStream);
                        if (cacheFile != null) {
                            boolean copy = true;
                            //do not override self!
                            if (urlFile != null) {
                                if (getAbsolutePath(urlFile.getPath()).equals(getAbsolutePath(cacheFile.getPath()))) {
                                    copy = false;
                                }
                            }
                            if (copy) {
                                File pp = cacheFile.getParentFile();
                                if (pp != null) {
                                    pp.mkdirs();
                                }
                                boolean cachedRecovered = cacheFile.isFile();
                                if (urlFile != null) {
                                    copy(urlFile, cacheFile, bLog);
                                } else {
                                    copy(url, cacheFile, bLog);
                                }
                                NBootDuration time = chrono.getDuration();
                                if (cachedRecovered) {
                                    bLog.with().level(Level.CONFIG).verbCache().log(NBootMsg.ofC("recover cached prp file %s (from %s)" + ((!time.isZero()) ? " (time %s)" : ""), cacheFile.getPath(), urlString, time));
                                } else {
                                    bLog.with().level(Level.CONFIG).verbCache().log(NBootMsg.ofC("cache prp file %s (from %s)" + ((!time.isZero()) ? " (time %s)" : ""), cacheFile.getPath(), urlString, time));
                                }
                                return props;
                            }
                        }
                        NBootDuration time = chrono.getDuration();
                        bLog.with().level(Level.CONFIG).verbSuccess().log(NBootMsg.ofC("load props file from  %s" + ((!time.isZero()) ? " (time %s)" : ""), urlString, time));
                    } else {
                        NBootDuration time = chrono.getDuration();
                        bLog.with().level(Level.CONFIG).verbFail().log(NBootMsg.ofC("load props file from  %s" + ((!time.isZero()) ? " (time %s)" : ""), urlString, time));
                    }
                }
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (Exception e) {
            NBootDuration time = chrono.getDuration();
            bLog.with().level(Level.CONFIG).verbFail().log(NBootMsg.ofC("load props file from  %s" + ((!time.isZero()) ? " (time %s)" : ""), String.valueOf(url),
                    time));
        }
        return props;
    }

    public static boolean isURL(String url) {
        if (url != null) {
            try {
                new URL(url);
                return true;
            } catch (MalformedURLException e) {
                //
            }
        }
        return false;
    }

    public static String getNativePath(String s) {
        return s.replace('/', File.separatorChar);
    }

    public static File toFile(String url) {
        if (NBootStringUtils.isBlank(url)) {
            return null;
        }
        URL u = null;
        try {
            u = new URL(url);
            return toFile(u);
        } catch (MalformedURLException e) {
            //
            return new File(url);
        }
    }

    public static URL toURL(String url) {
        if (NBootStringUtils.isBlank(url)) {
            return null;
        }
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            return null;
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

    public static long copy(InputStream from, OutputStream to, boolean closeInput, boolean closeOutput) throws IOException {
        byte[] bytes = new byte[10240];
        int count;
        long all = 0;
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
    }

    public static void copy(File ff, File to, NBootLog bLog) throws IOException {
        if(ff.equals(to)){
            return;
        }
        if (to.getParentFile() != null) {
            to.getParentFile().mkdirs();
        }
        if (ff == null || !ff.exists()) {
            bLog.with().level(Level.CONFIG).verbFail().log(NBootMsg.ofC("not found %s", ff));
            throw new FileNotFoundException(ff == null ? "" : ff.getPath());
        }
        try {
            Files.copy(ff.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            bLog.with().level(Level.CONFIG).verbFail().log(NBootMsg.ofC("error copying %s to %s : %s", ff, to, ex.toString()));
            throw ex;
        }
    }

    public static void copy(String path, File to, NBootLog bLog) throws IOException {
        if (NBootStringUtils.isBlank(path)) {
            throw new IOException("empty path " + path);
        }
        File file = toFile(path);
        if(file!=null){
            copy(file,to,bLog);
        }else{
            URL u = toURL(path);
            if(u!=null){
                copy(u,to,bLog);
            }else{
                throw new IOException("neither file nor URL : " + path);
            }
        }
    }
    public static void copy(URL url, File to, NBootLog bLog) throws IOException {
        try {
            InputStream in = openStream(url, bLog);
            if (in == null) {
                throw new IOException("empty Stream " + url);
            }
            if (to.getParentFile() != null) {
                if (!to.getParentFile().isDirectory()) {
                    boolean mkdirs = to.getParentFile().mkdirs();
                    if (!mkdirs) {
                        bLog.with().level(Level.CONFIG).verbFail().log(NBootMsg.ofC("error creating folder %s", url));
                    }
                }
            }
            ReadableByteChannel rbc = Channels.newChannel(in);
            FileOutputStream fos = new FileOutputStream(to);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (FileNotFoundException | UncheckedIOException ex) {
            bLog.with().level(Level.CONFIG).verbFail().log(NBootMsg.ofC("not found %s", url));
            throw ex;
        } catch (IOException ex) {
            bLog.with().level(Level.CONFIG).verbFail().log(NBootMsg.ofC("error copying %s to %s : %s", url, to, ex.toString()));
            throw ex;
        }
    }

    public static File createFile(String parent, String child) {
        String userHome = System.getProperty("user.home");
        if (child.startsWith("~/") || child.startsWith("~\\")) {
            child = new File(userHome, child.substring(2)).getPath();
        }
        if ((child.startsWith("/") || child.startsWith("\\") || new File(child).isAbsolute())) {
            return new File(child);
        }
        if (parent != null) {
            if (parent.startsWith("~/")) {
                parent = new File(userHome, parent.substring(2)).getPath();
            }
        } else {
            parent = ".";
        }
        return new File(parent, child);
    }

    public static String expandPath(String path, String base, Function<String, String> pathExpansionConverter) {
        path = NBootMsg.ofV(path.trim(), pathExpansionConverter).toString();
        if (isURL(path)) {
            return path;
        }
        if (path.startsWith("~/") || path.startsWith("~\\")) {
            path = System.getProperty("user.home") + path.substring(1);
        }
        if (base == null) {
            base = System.getProperty("user.dir");
        }
        if (new File(path).isAbsolute()) {
            return path;
        }
        return base + File.separator + path;
    }

    public static boolean isFileAccessible(Path path, Instant expireTime, NBootLog bLog) {
        boolean proceed = Files.isRegularFile(path);
        if (proceed) {
            try {
                if (expireTime != null) {
                    FileTime lastModifiedTime = Files.getLastModifiedTime(path);
                    if (lastModifiedTime.toInstant().compareTo(expireTime) < 0) {
                        return false;
                    }
                }
            } catch (Exception ex0) {
                bLog.with().level(Level.FINEST).verbFail().log(NBootMsg.ofC("unable to get LastModifiedTime for file : %s", path.toString(), ex0.toString()));
            }
        }
        return proceed;
    }

    public static String getURLDigest(URL url, NBootLog bLog) {
        if (url != null) {
            File ff = toFile(url);
            if (ff != null) {
                return getFileOrDirectoryDigest(ff.toPath());
            }
            InputStream is = null;
            try {
                is = openStream(url, bLog);
                if (is != null) {
                    return getStreamDigest(is);
                }
            } catch (Exception e) {
                //
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        //
                    }
                }
            }
        }
        return null;
    }

    public static String getFileOrDirectoryDigest(Path p) {
        if (Files.isDirectory(p)) {
            return getDirectoryDigest(p);
        } else if (Files.isRegularFile(p)) {
            try (InputStream is = Files.newInputStream(p)) {
                return getStreamDigest(is);
            } catch (IOException ex) {
                return null;
            }
        }
        return null;
    }

    private static String getDirectoryDigest(Path p) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            Files.walkFileTree(p, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    incrementalUpdateFileDigest(new ByteArrayInputStream(dir.toString().getBytes()), md);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    incrementalUpdateFileDigest(new ByteArrayInputStream(file.toString().getBytes()), md);
                    incrementalUpdateFileDigest(Files.newInputStream(file), md);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
            byte[] digest = md.digest();
            return NBootStringUtils.toHexString(digest);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getStreamDigest(InputStream is) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return getStreamDigest(is, md, 2048);
        } catch (Exception e) {
            return null;
        }
    }

    private static String getStreamDigest(InputStream is, MessageDigest md, int byteArraySize) {
        try {
            md.reset();
            byte[] bytes = new byte[byteArraySize];
            int numBytes;
            while ((numBytes = is.read(bytes)) != -1) {
                md.update(bytes, 0, numBytes);
            }
            byte[] digest = md.digest();
            return NBootStringUtils.toHexString(digest);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static void incrementalUpdateFileDigest(InputStream is, MessageDigest md) {
        try {
            byte[] bytes = new byte[4096];
            int numBytes;
            while ((numBytes = is.read(bytes)) != -1) {
                md.update(bytes, 0, numBytes);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static InputStream resolveInputStream(String url, NBootLog bLog) {
        InputStream in = null;
        try {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                URL url1 = new URL(url);
                try {
                    in = NBootIOUtilsBoot.openStream(url1, bLog);
                } catch (Exception ex) {
                    //do not need to log error
                    return null;
                }
            } else if (url.startsWith("file:")) {
                URL url1 = new URL(url);
                File file = NBootIOUtilsBoot.toFile(url1);
                if (file == null) {
                    // was not able to resolve to File
                    try {
                        in = NBootIOUtilsBoot.openStream(url1, bLog);
                    } catch (Exception ex) {
                        //do not need to log error
                        return null;
                    }
                } else if (file.isFile()) {
                    in = Files.newInputStream(file.toPath());
                } else {
                    return null;
                }
            } else {
                File file = new File(url);
                if (file.isFile()) {
                    in = Files.newInputStream(file.toPath());
                } else {
                    return null;
                }
            }
        } catch (IOException e) {
            bLog.with().level(Level.FINE).verbFail().error(e).log(NBootMsg.ofC("unable to resolveInputStream %s", url));
        }
        return in;
    }

    public static long deleteAndConfirmAll(Path[] folders, boolean force, NBootDeleteFilesContextBoot refForceAll,
                                           String header, NBootLog bLog, NBootOptionsInfo bOptions, Supplier<String> readline) {
        long count = 0;
        boolean headerWritten = false;
        if (folders != null) {
            for (Path child : folders) {
                if (Files.exists(child)) {
                    if (!headerWritten) {
                        headerWritten = true;
                        if (!force && !refForceAll.isForce()) {
                            if (header != null) {
                                if (!NBootUtils.firstNonNull(bOptions.getBot(), false)) {
                                    bLog.with().level(Level.WARNING).verbWarning().log(NBootMsg.ofC("%s", header));
                                }
                            }
                        }
                    }
                    count += deleteAndConfirm(child, force, refForceAll, bLog, bOptions, readline);
                }
            }
        }
        return count;
    }

    private static long deleteAndConfirm(Path directory, boolean force, NBootDeleteFilesContextBoot refForceAll,
                                         NBootLog bLog, NBootOptionsInfo bOptions, Supplier<String> readline) {
        if (Files.exists(directory)) {
            if (!force && !refForceAll.isForce() && refForceAll.accept(directory)) {
                String line = null;
                if (!NBootUtils.firstNonNull(bOptions.getBot(), false)) {
                    if (NBootUtils.sameEnum(NBootUtils.firstNonNull(bOptions.getConfirm(), "ASK"), "YES")) {
                        line = "y";
                    } else {
                        throw new NBootException(NBootMsg.ofPlain("failed to delete files in --bot mode without auto confirmation"));
                    }
                } else {
                    if (NBootUtils.firstNonNull(bOptions.getGui(), false)) {
                        line = NBootUtils.inputString(
                                NBootMsg.ofC("do you confirm deleting %s [y/n/c/a] (default 'n') ?", directory).toString(),
                                null, readline, bLog
                        );
                    } else {
                        String cc = NBootUtils.firstNonNull(bOptions.getConfirm(), "ASK");
                        switch (cc) {
                            case "YES": {
                                line = "y";
                                break;
                            }
                            case "NO": {
                                line = "n";
                                break;
                            }
                            case "ERROR": {
                                throw new NBootException(NBootMsg.ofPlain("error response"));
                            }
                            case "ASK": {
                                // Level.OFF is to force logging in all cases
                                bLog.with().level(Level.OFF).verbWarning().log(NBootMsg.ofC("do you confirm deleting %s [y/n/c/a] (default 'n') ? : ", directory));
                                line = readline.get();
                            }
                        }
                    }
                }
                if ("a".equalsIgnoreCase(line) || "all".equalsIgnoreCase(line)) {
                    refForceAll.setForce(true);
                } else if ("c".equalsIgnoreCase(line)) {
                    throw new NBootCancelException();
                } else if (!NBootUtils.firstNonNull(NBootUtils.parseBoolean(line), false)) {
                    refForceAll.ignore(directory);
                    return 0;
                }
            }
            long[] count = new long[1];
            try {
                Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        count[0]++;
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                            throws IOException {
                        count[0]++;
                        boolean deleted = false;
                        for (int i = 0; i < 2; i++) {
                            try {
                                Files.delete(dir);
                                deleted = true;
                                break;
                            } catch (DirectoryNotEmptyException e) {
                                // sometimes, on Windows OS, the Filesystem hasn't yet finished deleting
                                // the children (asynchronous)
                                //try three times and then exit!
                            }
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                        if (!deleted) {
                            //do not catch, last time the exception is thrown
                            Files.delete(dir);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
                count[0]++;
                bLog.with().level(Level.FINEST).verbWarning().log(NBootMsg.ofC("delete folder : %s (%s files/folders deleted)", directory, count[0]));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            return count[0];
        }
        return 0;
    }

    public static String formatURL(URL url) {
        if (url == null) {
            return "<EMPTY>";
        }
        File f = toFile(url);
        if (f != null) {
            return f.getPath();
        }
        return url.toString();
    }

    public static String getStoreLocationPath(NBootOptionsInfo bOptions, String storeType) {
        Map<String, String> storeLocations = bOptions.getStoreLocations();
        if (storeLocations != null) {
            return storeLocations.get(NBootUtils.enumName(storeType));
        }
        return null;
    }

    /**
     * @param includeRoot true if include root
     * @param locations   of type NutsStoreLocation, Path of File
     * @param readline
     */
    public static long deleteStoreLocations(NBootOptionsInfo lastBootOptions, NBootOptionsInfo o, boolean includeRoot,
                                            NBootLog bLog, Object[] locations, Supplier<String> readline) {
        if (lastBootOptions == null) {
            return 0;
        }
        String confirm = NBootUtils.enumName(NBootUtils.firstNonNull(o.getConfirm(), "ASK"));
        if (NBootUtils.sameEnum(confirm, "ASK")
                && !NBootUtils.sameEnum(NBootUtils.enumId(NBootUtils.firstNonNull(o.getOutputFormat(), "PLAIN")), "PLAIN")) {
            throw new NBootException(
                    NBootMsg.ofPlain("unable to switch to interactive mode for non plain text output format. "
                            + "You need to provide default response (-y|-n) for resetting/recovering workspace. "
                            + "You was asked to confirm deleting folders as part as recover/reset option."), 255);
        }
        bLog.with().level(Level.FINEST).verbWarning().log(NBootMsg.ofC("delete workspace location(s) at : %s",
                lastBootOptions.getWorkspace()
        ));
        boolean force = false;
        switch (confirm) {
            case "ASK": {
                break;
            }
            case "YES": {
                force = true;
                break;
            }
            case "NO":
            case "ERROR": {
                bLog.with().level(Level.WARNING).verbWarning().log(NBootMsg.ofPlain("reset cancelled (applied '--no' argument)"));
                throw new NBootCancelException();
            }
        }
        List<Path> folders = new ArrayList<>();
        if (includeRoot) {
            folders.add(Paths.get(lastBootOptions.getWorkspace()));
        }
        for (Object ovalue : locations) {
            if (ovalue != null) {
                if (ovalue instanceof String) {
                    String p = getStoreLocationPath(lastBootOptions, (String) ovalue);
                    if (p != null) {
                        folders.add(Paths.get(p));
                    }
                } else if (ovalue instanceof Path) {
                    folders.add(((Path) ovalue));
                } else if (ovalue instanceof File) {
                    folders.add(((File) ovalue).toPath());
                } else {
                    throw new NBootException(NBootMsg.ofC("unsupported path type : %s", ovalue));
                }
            }
        }
        NBootOptionsInfo optionsCopy = o.copy();
        if (NBootUtils.firstNonNull(optionsCopy.getBot(), false) || !NBootUtils.isGraphicalDesktopEnvironment()) {
            optionsCopy.setGui(false);
        }
        return deleteAndConfirmAll(folders.toArray(new Path[0]), force, DELETE_FOLDERS_HEADER, bLog, optionsCopy, readline);
    }

    public static long deleteAndConfirmAll(Path[] folders, boolean force, String header,
                                           NBootLog bLog, NBootOptionsInfo bOptions, Supplier<String> readline) {
        return deleteAndConfirmAll(folders, force, new NBootDeleteFilesContextBootImpl(), header, bLog, bOptions, readline);
    }
}
