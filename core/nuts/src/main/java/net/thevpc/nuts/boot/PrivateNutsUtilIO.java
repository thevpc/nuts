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
package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsBlankable;
import net.thevpc.nuts.NutsLogVerb;
import net.thevpc.nuts.NutsMessage;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Level;

public class PrivateNutsUtilIO {

    public static String getAbsolutePath(String path) {
        return new File(path).toPath().toAbsolutePath().normalize().toString();
    }

    public static String readStringFromFile(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }

    public static InputStream openStream(URL url, PrivateNutsBootLog bLog) {
        return PrivateNutsMonitoredURLInputStream.of(url,bLog);
    }

    public static byte[] loadStream(InputStream stream, PrivateNutsBootLog bLog) throws IOException{
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        copy(stream,bos,true, true);
        return bos.toByteArray();
    }

    public static ByteArrayInputStream preloadStream(InputStream stream, PrivateNutsBootLog bLog) throws IOException{
        return new ByteArrayInputStream(loadStream(stream,bLog));
    }

    public static Properties loadURLProperties(Path path, PrivateNutsBootLog bLog) {
        Properties props = new Properties();
        if(Files.isRegularFile(path)) {
            try (InputStream is = Files.newInputStream(path)) {
                props.load(is);
            } catch (IOException ex) {
                return new Properties();
            }
        }
        return props;
    }

    public static Properties loadURLProperties(URL url, File cacheFile, boolean useCache, PrivateNutsBootLog bLog) {
        long startTime = System.currentTimeMillis();
        Properties props = new Properties();
        InputStream inputStream = null;
        File urlFile = toFile(url);
        try {
            if (useCache) {
                if (cacheFile != null && cacheFile.isFile()) {
                    try {
                        inputStream = new FileInputStream(cacheFile);
                        props.load(inputStream);
                        long time = System.currentTimeMillis() - startTime;
                        bLog.log(Level.CONFIG, NutsLogVerb.SUCCESS, NutsMessage.jstyle("load cached file from  {0}" + ((time > 0) ? " (time {1})" : ""), cacheFile.getPath(), PrivateNutsUtils.formatPeriodMilli(time)));
                        return props;
                    } catch (IOException ex) {
                        bLog.log(Level.CONFIG, NutsLogVerb.FAIL, NutsMessage.jstyle("invalid cache. Ignored {0} : {1}", cacheFile.getPath(), ex.toString()));
                    } finally {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Exception ex) {
                                if (bLog != null) {
                                    bLog.log(Level.FINE, NutsMessage.jstyle("unable to close stream"), ex);
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
                    inputStream = openStream(url,bLog);
                    if (inputStream != null) {
                        props.load(inputStream);
                        if (cacheFile != null) {
                            boolean copy = true;
                            //dont override self!
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
                                long time = System.currentTimeMillis() - startTime;
                                if (cachedRecovered) {
                                    bLog.log(Level.CONFIG, NutsLogVerb.CACHE, NutsMessage.jstyle("recover cached prp file {0} (from {1})" + ((time > 0) ? " (time {2})" : ""), cacheFile.getPath(), urlString, PrivateNutsUtils.formatPeriodMilli(time)));
                                } else {
                                    bLog.log(Level.CONFIG, NutsLogVerb.CACHE, NutsMessage.jstyle("cache prp file {0} (from {1})" + ((time > 0) ? " (time {2})" : ""), cacheFile.getPath(), urlString, PrivateNutsUtils.formatPeriodMilli(time)));
                                }
                                return props;
                            }
                        }
                        long time = System.currentTimeMillis() - startTime;
                        bLog.log(Level.CONFIG, NutsLogVerb.SUCCESS, NutsMessage.jstyle("load props file from  {0}" + ((time > 0) ? " (time {1})" : ""), urlString, PrivateNutsUtils.formatPeriodMilli(time)));
                    } else {
                        long time = System.currentTimeMillis() - startTime;
                        bLog.log(Level.CONFIG, NutsLogVerb.FAIL, NutsMessage.jstyle("load props file from  {0}" + ((time > 0) ? " (time {1})" : ""), urlString, PrivateNutsUtils.formatPeriodMilli(time)));
                    }
                }
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (Exception e) {
            long time = System.currentTimeMillis() - startTime;
            bLog.log(Level.CONFIG, NutsLogVerb.FAIL, NutsMessage.jstyle("load props file from  {0}" + ((time > 0) ? " (time {1})" : ""), String.valueOf(url),
                    PrivateNutsUtils.formatPeriodMilli(time)));
        }
        return props;
    }

    public static boolean isURL(String url) {
        if(url!=null) {
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
        if (NutsBlankable.isBlank(url)) {
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

    public static void copy(File ff, File to, PrivateNutsBootLog bLog) throws IOException {
        if (to.getParentFile() != null) {
            to.getParentFile().mkdirs();
        }
        if (ff == null || !ff.exists()) {
            bLog.log(Level.CONFIG, NutsLogVerb.FAIL, NutsMessage.jstyle("not found {0}", ff));
            throw new FileNotFoundException(ff == null ? "" : ff.getPath());
        }
        try {
            Files.copy(ff.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            bLog.log(Level.CONFIG, NutsLogVerb.FAIL, NutsMessage.jstyle("error copying {0} to {1} : {2}", ff, to, ex.toString()));
            throw ex;
        }
    }

    public static void copy(URL url, File to, PrivateNutsBootLog bLog) throws IOException {
        try {
            InputStream in = openStream(url,bLog);
            if (in == null) {
                throw new IOException("empty Stream " + url);
            }
            if (to.getParentFile() != null) {
                if (!to.getParentFile().isDirectory()) {
                    boolean mkdirs = to.getParentFile().mkdirs();
                    if (!mkdirs) {
                        bLog.log(Level.CONFIG, NutsLogVerb.FAIL, NutsMessage.jstyle("error creating folder {0}", url));
                    }
                }
            }
            ReadableByteChannel rbc = Channels.newChannel(in);
            FileOutputStream fos = new FileOutputStream(to);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (FileNotFoundException ex) {
            bLog.log(Level.CONFIG, NutsLogVerb.FAIL, NutsMessage.jstyle("not found {0}", url));
            throw ex;
        } catch (IOException ex) {
            bLog.log(Level.CONFIG, NutsLogVerb.FAIL, NutsMessage.jstyle("error copying {0} to {1} : {2}", url, to, ex.toString()));
            throw ex;
        }
    }

    static File createFile(String parent, String child) {
        String userHome = System.getProperty("user.home");
        if (child.startsWith("~/")) {
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

    protected static String expandPath(String path, String base, Function<String, String> pathExpansionConverter) {
        path = PrivateNutsUtils.replaceDollarString(path.trim(), pathExpansionConverter);
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
}
