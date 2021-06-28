package net.thevpc.nuts.toolbox.ntemplate.filetemplate.util;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileProcessorUtils {
    
    public static void mkdirs(Path parent) {
        if (parent == null) {
            return;
        }
        if (!Files.isDirectory(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
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
    
    public static Path toRealPath(Path path, Path workingDir) {
        try {
            return toAbsolutePath(path, workingDir).toRealPath();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
    
    public static Path toAbsolutePath(Path path, Path workingDir) {
        if(!path.isAbsolute()){
            return workingDir.resolve(path).toAbsolutePath().normalize();
        }
        return path;
    }
    
    public static Path toAbsolute(Path pathString, Path workingDir) {
        if(pathString.isAbsolute()){
            return pathString.normalize();
        }
        if(workingDir==null){
            workingDir=Paths.get(System.getProperty("user.dir"));
        }
        return workingDir.resolve(pathString).toAbsolutePath().normalize();
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
    
    public static String loadString(Path s) {
        return loadString(s, null);
    }
    
    public static String loadString(Path s, String encoding) {
        try(InputStream in=Files.newInputStream(s)){
            return loadString(in, encoding);
        }catch(IOException ex){
            throw new UncheckedIOException(ex);
        }
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
