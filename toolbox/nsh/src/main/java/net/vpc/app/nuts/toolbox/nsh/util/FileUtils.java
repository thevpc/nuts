package net.vpc.app.nuts.toolbox.nsh.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class FileUtils {
    public static InputStream getInputStream(String path,String keyFilePath, String keyPassword) throws IOException {
        FilePath p=new FilePath(path);
        switch (p.getProtocol()){
            case "file": return new FileInputStream(p.getPath());
            case "ssh": return new SShConnection(path,keyFilePath,keyPassword).getInputStream(path,true);
            case "url": return new URL(p.getPath()).openStream();
        }
        throw new IOException("Unsupported protocol "+path);
    }
}
