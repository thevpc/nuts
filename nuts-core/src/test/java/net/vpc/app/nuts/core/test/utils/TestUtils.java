/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.test.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import net.vpc.app.nuts.Nuts;

/**
 *
 * @author vpc
 */
public class TestUtils {

    public static final String LINUX_CONFIG = new File(System.getProperty("user.home") + "/.nuts").getPath();
    public static final String LINUX_CACHE = new File(System.getProperty("user.home") + "/.cache/nuts").getPath();
    public static final String LINUX_TEMP = new File(System.getProperty("java.io.tmpdir") + "/" + System.getProperty("user.name") + "/nuts").getPath();
    public static final String[] NUTS_STD_FOLDERS = {LINUX_CONFIG, LINUX_CACHE, LINUX_TEMP};
    public static final String NUTS_VERSION = Nuts.getVersion();
    public static final String NDI_VERSION = NUTS_VERSION + ".0";

    public static FileSystemStash STASH = new FileSystemStash();

    public static Set<String> createNamesSet(String... names) {
        return new HashSet<String>(Arrays.asList(names));
    }

    public static int count(File d, FileFilter f) {
        return list(d, f).length;
    }

    public static Set<String> listNamesSet(File d, FileFilter f) {
        return Arrays.stream(list(d, f)).map(x -> x.getName()).collect(Collectors.toSet());
    }

    public static File[] list(File d, FileFilter f) {
        if (!d.isDirectory()) {
            return new File[0];
        }
        return d.listFiles(f);
    }

    public static void setSystemProperties(Map<String, String> params) {
        if (params != null) {
            final Properties p = System.getProperties();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                p.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public static void unsetNutsSystemProperties() {
        final Properties props = System.getProperties();
        for (Object k : props.keySet()) {
            String ks = String.valueOf(k);
            if (ks.startsWith("nuts.")) {
                System.out.println("## removed " + ks + "=" + props.getProperty(ks));
                props.remove(ks);
            } else if (ks.startsWith("nuts_")) {
                System.out.println("## removed " + ks + "=" + props.getProperty(ks));
                props.remove(ks);
            }
        }
    }

    public static void unsetSystemProperties(Map<String, String> params) {
        if (params != null) {
            final Properties p = System.getProperties();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                p.remove(entry.getKey());
            }
        }
    }

    public static String getCallerSimpleClasMethod(int index) {
        StackTraceElement i = getCallerStackTraceElement0(3+index);
        String cn=i.getClassName();
        String m=i.getClassName();
        return cn+"."+m;
    }
    
    public static String getCallerMethodId() {
        return getCallerMethodId(1);
    }
    
    public static String getCallerMethodId(int index) {
        StackTraceElement i = getCallerStackTraceElement0(3+index);
        String cn=i.getClassName();
        if(cn.indexOf('.')>0){
            cn=cn.substring(cn.lastIndexOf('.')+1);
        }
        String m=i.getMethodName();
        return cn+"_"+m;
    }
    
    public static String getCallerMethodName() {
        return getCallerMethodId(1);
    }
    
    public static String getCallerMethodName(int index) {
        return getCallerStackTraceElement0(3+index).getMethodName();
    }
    
    public static String getCallerClassSimpleName() {
        return getCallerClassSimpleName(1);
    }
    
    public static String getCallerClassSimpleName(int index) {
        StackTraceElement i = getCallerStackTraceElement0(3+index);
        String cn=i.getClassName();
        if(cn.indexOf('.')>0){
            cn=cn.substring(cn.lastIndexOf('.')+1);
        }
        return cn;
    }
    
    public static StackTraceElement getCallerStackTraceElement(int index) {
        return getCallerStackTraceElement0(3 + index);
    }

    public static StackTraceElement getCallerStackTraceElement() {
        return getCallerStackTraceElement0(3);
    }

    public static StackTraceElement getCallerStackTraceElement0(int index) {
        StackTraceElement[] s = Thread.currentThread().getStackTrace();
        return s[index];
    }

}
