package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.io.NPath;

import java.net.URL;

public class NCoreLogUtils {
    public static NPath forProgress(NPath p){
        if(p!=null){
            p=p.toCompressedForm();
        }
        return NPath.of("");
    }
    public static String forProgressUrl(URL p){
        return "";
    }
    public static String forProgressPathString(String p){
        return "";
    }
}
