package net.thevpc.nuts.toolbox.fileversion;

import java.io.File;
import java.io.IOException;

class _IOUtils {
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
