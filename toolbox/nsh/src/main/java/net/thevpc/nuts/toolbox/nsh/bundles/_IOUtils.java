package net.thevpc.nuts.toolbox.nsh.bundles;

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
     * @param in entree
     * @param out sortie
     * @throws IOException when IO error
     */
    public static void copy(InputStream in, OutputStream out) throws IOException {
        copy(in, out, DEFAULT_BUFFER_SIZE);
    }

    /**
     * copy le flux d'entree dans le lux de sortie
     *
     * @param in input
     * @param out output
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
     * @param in entree
     * @param out sortie
     * @throws IOException when IO error
     */
    public static void copy(File in, OutputStream out) throws IOException {
        copy(in, out, DEFAULT_BUFFER_SIZE);
    }

    /**
     * copy stream
     *
     * @param in input
     * @param out output
     * @param bufferSize bufferSize
     * @throws IOException when IO error
     */
    public static void copy(File in, OutputStream out, int bufferSize) throws IOException {
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(in);
            copy(fis, out, bufferSize);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }

    }

    public static String getAbsoluteFile2(String path, String cwd) {
        if (new File(path).isAbsolute()) {
            return path;
        }
        if (cwd == null) {
            cwd = System.getProperty("user.dir");
        }
        switch (path){
            case "~" : return System.getProperty("user.home");
            case "." : {
                File file = new File(cwd);
                try {
                    return file.getCanonicalPath();
                }catch (IOException ex){
                    return file.getAbsolutePath();
                }
            }
            case ".." : {
                File file = new File(cwd, "..");
                try {
                    return file.getCanonicalPath();
                }catch (IOException ex){
                    return file.getAbsolutePath();
                }
            }
        }
        int j=-1;
        char[] chars = path.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if(chars[i]=='/' || chars[i]=='\\'){
                j=i;
                break;
            }
        }
        if(j>0) {
            switch (path.substring(0,j)) {
                case "~":
                    String e = path.substring(j + 1);
                    if(e.isEmpty()){
                        return System.getProperty("user.home");
                    }
                    File file = new File(System.getProperty("user.home"), e);
                    try {
                        return file.getCanonicalPath();
                    }catch (IOException ex){
                        return file.getAbsolutePath();
                    }
            }
        }
        File file = new File(cwd, path);
        try {
            return file.getCanonicalPath();
        }catch (IOException ex){
            return file.getAbsolutePath();
        }
    }
}
