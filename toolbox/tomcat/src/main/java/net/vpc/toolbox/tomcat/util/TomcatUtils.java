package net.vpc.toolbox.tomcat.util;

import net.vpc.app.nuts.JsonIO;
import net.vpc.app.nuts.NutsWorkspace;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class TomcatUtils {
    public static boolean isEmpty(String string) {
        return string == null || string.trim().isEmpty();
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
        JsonIO jsonSerializer = ws.getJsonIO();
        PrintWriter w = new PrintWriter(out);
        jsonSerializer.write(config, new PrintWriter(out), true);
        w.flush();
    }

    public static boolean isPositiveInt(String s) {
        if (s == null) {
            return false;
        }
        s = s.trim();
        if (s.length() == 0) {
            return false;
        }
        for (char c : s.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    public static String[] splitInstanceAppPreferInstance(String value) {
        return splitInstanceApp(value, true);
    }
    public static String[] splitInstanceAppPreferApp(String value) {
        return splitInstanceApp(value, false);
    }

    public static String[] splitInstanceApp(String value, boolean preferInstance) {
        int dot = value.indexOf('.');
        if (dot >= 0) {
            return new String[]{value.substring(0, dot), value.substring(0, dot + 1)};
        } else if (preferInstance) {
            return new String[]{value, ""};
        } else {
            return new String[]{"", value};
        }
    }

    public static String toJsonString(Object o) {
        if (o == null) {
            return String.valueOf(o);
        }
        if (o instanceof Boolean
                || o instanceof Number
                || o instanceof Map
                || o instanceof Collection
        ) {
            return String.valueOf(o);
        }
        if (o.getClass().isArray()) {
            return Arrays.toString((Object[]) o);
        }
        return "\"" + o.toString().replace("\"", "\\\"") + "\"";
    }
}
