package net.thevpc.nuts.toolbox.tomcat.util;

import java.io.File;

public class _FileUtils {
    public static String getFileName(String name) {
        name=name.replace(File.separatorChar,'/');
        int i = name.lastIndexOf('/');
        if(i>=0){
            name=name.substring(i+1);
        }
        return name;
    }
}
