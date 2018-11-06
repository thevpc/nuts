package net.vpc.toolbox.tomcat.util;

import java.lang.reflect.Method;

public class TomcatUtils {
    public static boolean isEmpty(String string) {
        return string == null || string.trim().isEmpty();
    }
    public static Object getPropertyValue(Object obj, String propName) {
        propName = Character.toUpperCase(propName.charAt(0)) + propName.substring(1);
        Method m = null;
        try {
            m = obj.getClass().getDeclaredMethod("get" + propName);
            return m.invoke(obj);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
