package net.vpc.toolbox.tomcat.util;

import net.vpc.app.nuts.JsonSerializer;
import net.vpc.app.nuts.NutsWorkspace;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Map;

public class TomcatUtils {
    public static boolean isEmpty(String string) {
        return string == null || string.trim().isEmpty();
    }

//    public static void main(String[] args) {
//        TomcatServerConfig c=new TomcatServerConfig();
//        TomcatServerAppConfig m = new TomcatServerAppConfig();
//        c.getApps().put("hi", m);
//        m.setDomain("ok");
//        System.out.println(getPropertyValue(c,"apps.hi.domain"));
//    }
    public static Object getPropertyValue(Object obj, String propExpr) {
        int x=propExpr.indexOf('.');
        if(x<0){
            if(obj instanceof Map){
                for (Object o : ((Map) obj).keySet()) {
                    String k=String.valueOf(o);
                    if(k.equals(propExpr)){
                        return ((Map) obj).get(o);
                    }
                }
                return null;
            }
            propExpr = Character.toUpperCase(propExpr.charAt(0)) + propExpr.substring(1);
            Method m = null;
            try {
                m = obj.getClass().getDeclaredMethod("get" + propExpr);
                return m.invoke(obj);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }else {
            Object o = getPropertyValue(obj, propExpr.substring(0, x));
            return getPropertyValue(o, propExpr.substring(x + 1));
        }
    }

    public static String toValidFileName(String name, String defaultName) {
        String r = trim(name);
        if (r.isEmpty()) {
            return trim(defaultName);
        }
        return r
                .replace('/', '_')
                .replace('*', '_')
                .replace('?', '_')
                .replace('.', '_')
                .replace('\\', '_');
    }

    public static String trim(String appName) {
        return appName == null ? "" : appName.trim();
    }

    public static void writeJson(PrintStream out, Object config, NutsWorkspace ws) {
        JsonSerializer jsonSerializer = ws.getExtensionManager().createJsonSerializer();
        PrintWriter w = new PrintWriter(out);
        jsonSerializer.write(config, new PrintWriter(out), true);
        w.flush();
    }

    public static boolean isPositiveInt(String s) {
        if(s==null){
            return false;
        }
        s=s.trim();
        if(s.length()==0){
            return false;
        }
        for (char c : s.toCharArray()) {
            if(!Character.isDigit(c)){
                return false;
            }
        }
        return true;
    }
}
