/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Level;

import static net.vpc.app.nuts.DefaultNutsBootWorkspace.log;

/**
 * IO Utils helper Created by vpc on 1/15/17.
 */
final class NutsIOUtils {

    private NutsIOUtils() {
    }

    public static File createFile(String path) {
        return new File(getAbsolutePath(path));
    }

    public static File createFile(File parent, String path) {
        return new File(parent, path);
    }

    public static File createFile(String parent, String path) {
        return new File(getAbsolutePath(parent), path);
    }

    public static String getAbsolutePath(String path) {
        try {
            return getAbsoluteFile(new File(path)).getCanonicalPath();
        } catch (IOException e) {
            return getAbsoluteFile(new File(path)).getAbsolutePath();
        }
    }

    public static File getAbsoluteFile(File path) {
        if (path.isAbsolute()) {
            return path;
        }
        try {
            return path.getCanonicalFile();
        } catch (IOException e) {
            return path.getAbsoluteFile();
        }
    }

    public static String readStringFromURL(URL requestURL) throws IOException {
        try (Scanner scanner = new Scanner(requestURL.openStream(),
                StandardCharsets.UTF_8.toString())) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    public static String readStringFromFile(File file) throws IOException {
        try (Scanner scanner = new Scanner(new FileInputStream(file),
                StandardCharsets.UTF_8.toString())) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    public static void copy(InputStream from, File to, boolean mkdirs, boolean closeInput) throws IOException {
        try {
            File parentFile = to.getParentFile();
            if (mkdirs && parentFile != null) {
                parentFile.mkdirs();
            }
            File temp = new File(to.getPath() + "~");
            try {
                Files.copy(from, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Files.move(temp.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } finally {
                temp.delete();
            }
        } finally {
            if (closeInput) {
                from.close();
            }
        }
    }

    public static boolean isAbsolutePath(String location) {
        return new File(location).isAbsolute();
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

    public static String expandPath(String path) {
        if (path.equals("~") || path.equals("~/") || path.equals("~\\") || path.equals("~\\")) {
            return System.getProperty("user.home");
        }
        if (path.startsWith("~/") || path.startsWith("~\\")) {
            path = System.getProperty("user.home") + path.substring(1);
        }
        return path;
    }

    public static File resolvePath(String path, File baseFolder, String nutsHome) {
        if (NutsStringUtils.isEmpty(nutsHome)) {
            nutsHome = NutsConstants.DEFAULT_NUTS_HOME;
        }
        if (path != null && path.length() > 0) {
            String firstItem = "";
            if ('\\' == File.separatorChar) {
                String[] split = path.split("([/\\\\])");
                if (split.length > 0) {
                    firstItem = split[0];
                }
            } else {
                String[] split = path.split("(/|" + File.separatorChar + ")");
                if (split.length > 0) {
                    firstItem = split[0];
                }
            }
            if (firstItem.equals("~~")) {
                return resolvePath(nutsHome + "/" + path.substring(2), null, nutsHome);
            } else if (firstItem.equals("~")) {
                return new File(System.getProperty("user.home"), path.substring(1));
            } else if (isAbsolutePath(path)) {
                return new File(path);
            } else if (baseFolder != null) {
                return NutsIOUtils.createFile(baseFolder, path);
            } else {
                return NutsIOUtils.createFile(path);
            }
        }
        return null;
    }

    public static boolean storeProperties(Properties p, File file) {
        Writer writer = null;
        try {
            try {
                p.store(writer = new FileWriter(file), null);
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
            return true;
        } catch (IOException e) {
            log.log(Level.SEVERE, "Unable to store {0}", file);
        }
        return false;
    }

    public static Properties loadFileProperties(File file) {
        Properties props = new Properties();
        InputStream inputStream = null;
        try {
            try {
                if (file != null && file.isFile()) {
                    inputStream = new FileInputStream(file);
                    props.load(inputStream);
                }
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return props;
    }

    public static Properties loadURLProperties(String url) {
        try {
            if (url != null) {
                return loadURLProperties(new URL(url));
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return new Properties();
    }

    public static Properties loadFileProperties(String file) {
        try {
            if (file != null) {
                return loadFileProperties(new File(file));
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return new Properties();
    }

    public static File urlToFile(String url) {
        if (url != null) {
            URL u = null;
            try {
                u = new URL(url);
            } catch (Exception ex) {
                //
            }
            if (u != null) {
                if ("file".equals(u.getProtocol())) {
                    try {
                        return new File(u.toURI());
                    } catch (Exception ex) {
                        return new File(u.getPath());
                    }
                }
            }
        }
        return null;
    }

    public static Properties loadURLProperties(URL url) {
        long startTime=System.currentTimeMillis();
        Properties props = new Properties();
        InputStream inputStream = null;
        try {
            try {
                if (url != null) {
                    inputStream = url.openStream();
                    props.load(inputStream);
                    long time = System.currentTimeMillis() - startTime;
                    if(time>0) {
                        log.log(Level.CONFIG, "[SUCCESS] Loading props file from  {0} (time {1})", new Object[]{(url == null) ? "<null>" : url.toString(), time + "ms"});
                    }else{
                        log.log(Level.CONFIG, "[SUCCESS] Loading props file from  {0}", new Object[]{(url == null) ? "<null>" : url.toString()});
                    }
                }
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (Exception e) {
            long time = System.currentTimeMillis() - startTime;
            if(time>0) {
                log.log(Level.CONFIG, "[ERROR  ] Loading props file from  {0} (time {1})", new Object[]{(url == null) ? "<null>" : url.toString(), time + "ms"});
            }else{
                log.log(Level.CONFIG, "[ERROR  ] Loading props file from  {0}", new Object[]{(url == null) ? "<null>" : url.toString()});
            }
            //e.printStackTrace();
        }
        return props;
    }

    public static boolean isRemoteURL(String url) {
        if (url == null) {
            return false;
        }
        url = url.toLowerCase();
        return (url.startsWith("http://") || url.startsWith("https://"));
    }
}
