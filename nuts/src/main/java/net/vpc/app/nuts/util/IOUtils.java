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
        public void write(int b) {
        }

        @Override
        public void write(byte[] b) {
        }

        @Override
        public void write(byte[] b, int off, int len) {
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    };

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

    public static File resolvePath(String path, File baseFolder,String workspaceRoot) {
        if(StringUtils.isEmpty(workspaceRoot)){
            workspaceRoot=NutsConstants.DEFAULT_WORKSPACE_ROOT;
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
                return resolvePath(workspaceRoot + "/" + path.substring(2), null,workspaceRoot);
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
}
