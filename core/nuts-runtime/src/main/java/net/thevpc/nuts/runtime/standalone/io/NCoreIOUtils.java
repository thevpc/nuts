package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.NBootOptions;
import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.time.NChronometer;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NHex;
import net.thevpc.nuts.util.NIOUtils;
import net.thevpc.nuts.util.NMsg;

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
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Level;

public class NCoreIOUtils {
    public static String getURLDigest(URL url, NLog bLog) {
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
            return NHex.fromBytes(digest);
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
            return NHex.fromBytes(digest);
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

    public static boolean isFileAccessible(Path path, Instant expireTime, NLog bLog) {
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
                bLog.with().level(Level.FINEST).verb(NLogVerb.FAIL).log(NMsg.ofC("unable to get LastModifiedTime for file : %s", path.toString(), ex0.toString()));
            }
        }
        return proceed;
    }

    public static InputStream resolveInputStream(String url, NLog bLog) {
        InputStream in = null;
        try {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                URL url1 = CoreIOUtils.urlOf(url);
                try {
                    in = openStream(url1, bLog);
                } catch (Exception ex) {
                    //do not need to log error
                    return null;
                }
            } else if (url.startsWith("file:")) {
                URL url1 = CoreIOUtils.urlOf(url);
                File file = toFile(url1);
                if (file == null) {
                    // was not able to resolve to File
                    try {
                        in = openStream(url1, bLog);
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
            bLog.with().level(Level.FINE).verb(NLogVerb.FAIL).error(e).log(NMsg.ofC("unable to resolveInputStream %s", url));
        }
        return in;
    }

    public static String expandPath(String path, String base, Function<String, String> pathExpansionConverter) {
        path = NMsg.ofV(path.trim(), pathExpansionConverter).toString();
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

    public static String getStoreLocationPath(NBootOptions bOptions, NStoreType value) {
        Map<NStoreType, String> storeLocations = bOptions.getStoreLocations().orNull();
        if (storeLocations != null) {
            return storeLocations.get(value);
        }
        return null;
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

    public static String getAbsolutePath(String path) {
        return new File(path).toPath().toAbsolutePath().normalize().toString();
    }

    public static InputStream openStream(URL url, NLog bLog) {
        return NReservedMonitoredURLInputStream.of(url, bLog);
    }

    public static byte[] loadStream(InputStream stream, NLog bLog) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        NIOUtils.copy(stream, bos, true, true);
        return bos.toByteArray();
    }

    public static ByteArrayInputStream preloadStream(InputStream stream, NLog bLog) throws IOException {
        return new ByteArrayInputStream(loadStream(stream, bLog));
    }

    public static Properties loadURLProperties(Path path, NLog bLog) {
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

    public static Properties loadURLProperties(URL url, File cacheFile, boolean useCache, NLog bLog) {
        NChronometer chrono = NChronometer.startNow();
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
                        NDuration time = chrono.getDuration();
                        bLog.with().level(Level.CONFIG).verb(NLogVerb.SUCCESS).log(NMsg.ofC("load cached file from  %s" + ((!time.isZero()) ? " (time %s)" : ""), cacheFile.getPath(), chrono));
                        return props;
                    } catch (IOException ex) {
                        bLog.with().level(Level.CONFIG).verb(NLogVerb.FAIL).log(NMsg.ofC("invalid cache. Ignored %s : %s", cacheFile.getPath(), ex.toString()));
                    } finally {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Exception ex) {
                                if (bLog != null) {
                                    bLog.with().level(Level.FINE).verb(NLogVerb.FAIL).error(ex).log(NMsg.ofPlain("unable to close stream"));
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
                                NDuration time = chrono.getDuration();
                                if (cachedRecovered) {
                                    bLog.with().level(Level.CONFIG).verb(NLogVerb.CACHE).log(NMsg.ofC("recover cached prp file %s (from %s)" + ((!time.isZero()) ? " (time %s)" : ""), cacheFile.getPath(), urlString, time));
                                } else {
                                    bLog.with().level(Level.CONFIG).verb(NLogVerb.CACHE).log(NMsg.ofC("cache prp file %s (from %s)" + ((!time.isZero()) ? " (time %s)" : ""), cacheFile.getPath(), urlString, time));
                                }
                                return props;
                            }
                        }
                        NDuration time = chrono.getDuration();
                        bLog.with().level(Level.CONFIG).verb(NLogVerb.SUCCESS).log(NMsg.ofC("load props file from  %s" + ((!time.isZero()) ? " (time %s)" : ""), urlString, time));
                    } else {
                        NDuration time = chrono.getDuration();
                        bLog.with().level(Level.CONFIG).verb(NLogVerb.FAIL).log(NMsg.ofC("load props file from  %s" + ((!time.isZero()) ? " (time %s)" : ""), urlString, time));
                    }
                }
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (Exception e) {
            NDuration time = chrono.getDuration();
            bLog.with().level(Level.CONFIG).verb(NLogVerb.FAIL).log(NMsg.ofC("load props file from  %s" + ((!time.isZero()) ? " (time %s)" : ""), String.valueOf(url),
                    time));
        }
        return props;
    }

    public static boolean isURL(String url) {
        if (url != null) {
            try {
                CoreIOUtils.urlOf(url);
                return true;
            } catch (Exception e) {
                //
            }
        }
        return false;
    }

    public static File toFile(String url) {
        if (NBlankable.isBlank(url)) {
            return null;
        }
        URL u = null;
        try {
            u = CoreIOUtils.urlOf(url);
            return toFile(u);
        } catch (Exception e) {
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

    public static void copy(File ff, File to, NLog bLog) throws IOException {
        if (to.getParentFile() != null) {
            to.getParentFile().mkdirs();
        }
        if (ff == null || !ff.exists()) {
            bLog.with().level(Level.CONFIG).verb(NLogVerb.FAIL).log(NMsg.ofC("not found %s", ff));
            throw new FileNotFoundException(ff == null ? "" : ff.getPath());
        }
        try {
            Files.copy(ff.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            bLog.with().level(Level.CONFIG).verb(NLogVerb.FAIL).log(NMsg.ofC("error copying %s to %s : %s", ff, to, ex.toString()));
            throw ex;
        }
    }

    public static void copy(URL url, File to, NLog bLog) throws IOException {
        try {
            InputStream in = openStream(url, bLog);
            if (in == null) {
                throw new IOException("empty Stream " + url);
            }
            if (to.getParentFile() != null) {
                if (!to.getParentFile().isDirectory()) {
                    boolean mkdirs = to.getParentFile().mkdirs();
                    if (!mkdirs) {
                        bLog.with().level(Level.CONFIG).verb(NLogVerb.FAIL).log(NMsg.ofC("error creating folder %s", url));
                    }
                }
            }
            ReadableByteChannel rbc = Channels.newChannel(in);
            FileOutputStream fos = new FileOutputStream(to);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (FileNotFoundException | UncheckedIOException ex) {
            bLog.with().level(Level.CONFIG).verb(NLogVerb.FAIL).log(NMsg.ofC("not found %s", url));
            throw ex;
        } catch (IOException ex) {
            bLog.with().level(Level.CONFIG).verb(NLogVerb.FAIL).log(NMsg.ofC("error copying %s to %s : %s", url, to, ex.toString()));
            throw ex;
        }
    }


}
