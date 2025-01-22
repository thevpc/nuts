package net.thevpc.nuts.lib.doc.util;

import net.thevpc.nuts.io.NPath;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;

public class FileProcessorUtils {
    
    public static void mkdirs(NPath parent) {
        if (parent == null) {
            return;
        }
        if (!parent.isDirectory()) {
            parent.mkdirs();
        }
    }

    public static String extractWorkDir(String pathString, String defaultWorkdir) {
        if (pathString.startsWith("http://")
                || pathString.startsWith("https://")) {
            return ".";
        }
        if (pathString.startsWith("file://")) {
            try {
                File ff = Paths.get(new URL(pathString).toURI()).toFile();
                File pf = ff.getParentFile();
                if (pf != null) {
                    return pf.getAbsolutePath();
                }
                return defaultWorkdir;
            } catch (Exception e) {
                //
            }
            return defaultWorkdir;
        }
        File f = null;
        try {
            File d0 = new File(pathString);
            if (d0.isAbsolute()) {
                f = d0.getCanonicalFile().getParentFile();
            } else {
                f = new File(defaultWorkdir + File.separator + pathString).getCanonicalFile();
            }
        } catch (IOException ex) {
            //
        }
        if (f == null) {
            return defaultWorkdir;
        }
        return f.getPath();
    }
    
    public static NPath toRealPath(NPath path, NPath workingDir) {
        return toAbsolutePath(path, workingDir).normalize();
    }
    
    public static NPath toAbsolutePath(NPath path, NPath workingDir) {
        if(!path.isAbsolute()){
            return workingDir.resolve(path).toAbsolute().normalize();
        }
        return path;
    }
    
    public static NPath toAbsolute(NPath pathString, NPath workingDir) {
        if(pathString.isAbsolute()){
            return pathString.normalize();
        }
        if(workingDir==null){
            workingDir=NPath.of(System.getProperty("user.dir"));
        }
        return workingDir.resolve(pathString).toAbsolute().normalize();
    }

    public static String toAbsolute(String pathString, String workingDir) {
        if (!new File(pathString).isAbsolute()) {
            if(workingDir==null){
                workingDir=System.getProperty("user.dir");
            }
            pathString = workingDir + File.separator + pathString;
        }
        return pathString;
    }
    
    private static final char[] HEXARR = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    
    public static String loadString(NPath s) {
        return loadString(s, null);
    }
    
    public static String loadString(NPath s, String encoding) {
        return s.readString(encoding==null?null:Charset.forName(encoding));
    }
    
    public static String loadString(InputStream s, String encoding) {
        try {
            byte[] bytes = loadBytes(s);
            if (encoding == null) {
                return new String(bytes);
            }
            return new String(bytes, encoding);
        } catch (UnsupportedEncodingException ex) {
            throw new UncheckedIOException(ex);
        }
    }
    
    public static String[] splitMimeTypes(String mt) {
        if (mt == null) {
            return new String[0];
        }
        return new String[]{mt};
    }

    public static byte[] loadBytes(InputStream s) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int r;
            while ((r = s.read(buffer)) > 0) {
                bos.write(buffer, 0, r);
            }
            return bos.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
    
    
    
    public static char toHex(int nibble) {
        return HEXARR[(nibble & 0xF)];
    }
    
}
