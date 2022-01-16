package net.thevpc.nuts.toolbox.nsh.bundles;

import net.thevpc.nuts.NutsPath;
import net.thevpc.nuts.NutsSession;

import java.io.*;
import java.net.URL;

public class _IOUtils {
    public static final int DEFAULT_BUFFER_SIZE = 1024;

    public static String loadString(URL url) throws IOException {
        return new String(loadByteArray(url));
    }

    public static String loadString(InputStream r) throws IOException {
        return new String(loadByteArray(r));
    }

    public static byte[] loadByteArray(URL url) throws IOException {
        InputStream r = null;
        try {
            return loadByteArray(r = url.openStream());
        } finally {
            if (r != null) {
                r.close();
            }
        }
    }

    public static byte[] loadByteArray(InputStream r) throws IOException {
        ByteArrayOutputStream out = null;

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

    }

    /**
     * copy le flux d'entree dans le lux de sortie
     *
     * @param in  entree
     * @param out sortie
     * @throws IOException when IO error
     */
    public static void copy(InputStream in, OutputStream out) throws IOException {
        copy(in, out, DEFAULT_BUFFER_SIZE);
    }

    /**
     * copy le flux d'entree dans le lux de sortie
     *
     * @param in         input
     * @param out        output
     * @param bufferSize bufferSize
     * @throws IOException when IO error
     */
    public static void copy(InputStream in, OutputStream out, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        int len;

        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }

    }

    /**
     * copy le flux d'entree dans le lux de sortie
     *
     * @param in  entree
     * @param out sortie
     * @throws IOException when IO error
     */
    public static void copy(NutsPath in, OutputStream out) throws IOException {
        copy(in, out, DEFAULT_BUFFER_SIZE);
    }

    /**
     * copy stream
     *
     * @param in         input
     * @param out        output
     * @param bufferSize bufferSize
     * @throws IOException when IO error
     */
    public static void copy(NutsPath in, OutputStream out, int bufferSize) throws IOException {
        InputStream fis = null;

        try {
            fis = in.getInputStream();
            copy(fis, out, bufferSize);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }

    }

    public static String getAbsoluteFile2(String path, String cwd, NutsSession session) {
        NutsPath np = NutsPath.of(path, session);
        if (np.isAbsolute()) {
            return path;
        }
        if (cwd == null) {
            cwd = System.getProperty("user.dir");
        }
        switch (path) {
            case "~":
                return System.getProperty("user.home");
            default: {
                if (path.startsWith("~/") || path.startsWith("~\\")) {
                    return NutsPath.ofUserHome(session).resolve(path.substring(2)).normalize().toAbsolute().toString();
                }
                return np.toAbsolute(cwd).normalize().toAbsolute().toString();
            }
        }
    }
}
