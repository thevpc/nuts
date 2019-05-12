package net.vpc.app.nuts;

import java.lang.reflect.Method;
import java.util.Map;

public class NutsAppUtils {
    public static Object getPropertyValue(Object obj, String propExpr) {
        int x = propExpr.indexOf('.');
        if (x < 0) {
            if (obj instanceof Map) {
                for (Object o : ((Map) obj).keySet()) {
                    String k = String.valueOf(o);
                    if (k.equals(propExpr)) {
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
        } else {
            Object o = getPropertyValue(obj, propExpr.substring(0, x));
            return getPropertyValue(o, propExpr.substring(x + 1));
        }
    }
}
