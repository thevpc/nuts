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
package net.vpc.app.nuts.util;

import net.vpc.app.nuts.*;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/15/17.
 */
public class IOUtils {
    public static final OutputStream NULL_OUTPUT_STREAM = new OutputStream() {
        @Override
        public void write(int b) throws IOException {
        }

        @Override
        public void write(byte[] b) throws IOException {
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
        }
    };
    private static final Logger log = Logger.getLogger(IOUtils.class.getName());

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

    public static String getFileExtension(File f) {
        return getFileExtension(f.getName());
    }

    public static String getFileExtension(String n) {
        int i = n.lastIndexOf('.');
        if (i >= 0) {
            return n.substring(i + 1);
        }
        return "";
    }

    public static String readStreamAsString(InputStream stream, boolean close) throws IOException {
        return new String(readStreamAsBytes(stream, close));
    }

    public static byte[] readStreamAsBytes(InputStream stream, boolean close) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        copy(stream, os, close, true);
        return os.toByteArray();
    }

    public static void copy(File from, File to, boolean mkdirs) throws IOException {
        copy(new FileInputStream(from), to, mkdirs, true);
    }

    public static void copy(String from, File to, boolean mkdirs) throws IOException {
        if (from == null) {
            from = "";
        }
        copy(new ByteArrayInputStream(from.getBytes()), to, mkdirs, true);
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

    public static File resolvePath(String path, File baseFolder) {
        if (path != null && path.length() > 0) {
            String firstItem = "";
            if ('\\' == File.separatorChar) {
                String[] split = path.split("(/|\\\\)");
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
                return resolvePath(NutsConstants.DEFAULT_WORKSPACE_ROOT + "/" + path.substring(2), null);
            } else if (firstItem.equals("~")) {
                return new File(System.getProperty("user.home"), path.substring(1));
            } else if (isAbsolutePath(path)) {
                return new File(path);
            } else if (baseFolder != null) {
                return IOUtils.createFile(baseFolder, path);
            } else {
                return IOUtils.createFile(path);
            }
        }
        return null;
    }

    public static String readPassword(String prompt, InputStream in, PrintStream out) {
        Console cons = null;
        char[] passwd = null;
        if (in == null) {
            in = System.in;
        }
        if (out == null) {
            out = System.out;
        }
        if (in == System.in && ((cons = System.console()) != null)) {
            if ((passwd = cons.readPassword("[%s]", prompt)) != null) {
                String pwd = new String(passwd);
                java.util.Arrays.fill(passwd, ' ');
                return pwd;
            } else {
                return null;
            }
        } else {
            out.print(prompt);
            out.flush();
            Scanner s = new Scanner(in);
            return s.nextLine();
        }

    }

    public static File createTempFile(NutsDescriptor descriptor, File directory) throws IOException {
        String prefix = "temp-";
        String ext = null;
        if (descriptor != null) {
            ext = StringUtils.trim(descriptor.getExt());
            prefix = StringUtils.trim(descriptor.getId().getGroup()) + "-" + StringUtils.trim(descriptor.getId().getName()) + "-" + StringUtils.trim(descriptor.getId().getVersion().getValue());
            if (prefix.length() < 3) {
                prefix = prefix + "tmp";
            }
            if (!ext.isEmpty()) {
                ext = "." + ext;
                if (ext.length() < 3) {
                    ext = ".tmp" + ext;
                }
            } else {
                ext = "-nuts";
            }
        }
        return File.createTempFile(prefix, "-nuts" + (ext != null ? ("." + ext) : ""), directory);
    }

    public static File createTempFile(NutsDescriptor descriptor) throws IOException {
        return createTempFile(descriptor, null);
    }

    public static Properties loadProperties(URL url) {
        Properties props = new Properties();
        InputStream inputStream = null;
        try {
            try {
                if (url != null) {
                    inputStream = url.openStream();
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

}
