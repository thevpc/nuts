package net.vpc.app.nuts.runtime.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class NutsLogFormatHelper {
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final Map<Level, String> logLevelCache = new HashMap<>();
    private static final Map<String, String> logVerbCache = new HashMap<>();
    private static final Map<String, String> classNameCache = new HashMap<>();

    public static String logLevel(Level l) {
        String v = logLevelCache.get(l);
        if (v == null) {
            StringBuilder sb = new StringBuilder(l.getName());
            ensureSize(sb, 6);
            v = sb.toString();
            logLevelCache.put(l, v);
        }
        return v;
    }

    public static String logVerb(String l) {
        if(l==null){
            l="";
        }
        String v = logVerbCache.get(l);
        if (v == null) {
            StringBuilder sb = new StringBuilder(7/*9*/);
//            sb.append("[");
            sb.append(l);
            ensureSize(sb, 7/*8*/);
//            sb.append("]");
            v = sb.toString();
            logVerbCache.put(l, v);
        }
        return v;
    }

    public static void ensureSize(StringBuilder sb, int size) {
        sb.ensureCapacity(size);
        int length = size - sb.length();
        while (length > 0) {
            if (length >= 16) {
                sb.append("                ");
                length -= 16;
            } else if (length >= 8) {
                sb.append("        ");
                length -= 8;
            } else if (length >= 4) {
                sb.append("    ");
                length -= 4;
            } else if (length >= 2) {
                sb.append("  ");
                length -= 2;
            } else {
                sb.append(' ');
                length--;
            }
        }
    }

    public static String formatClassName(String className) {
        if (className == null) {
            return "";
        }
        String v = classNameCache.get(className);
        if (v == null) {
            int maxSize = 47;
            StringBuilder sb = new StringBuilder(maxSize);
            int pos = 0, end;
            while ((end = className.indexOf('.', pos)) >= 0) {
                sb.append(className.charAt(pos)).append('.');
                pos = end + 1;
            }
            sb.append(className.substring(pos));
            ensureSize(sb, maxSize);
            v = sb.toString();
            classNameCache.put(className, v);
        }
        return v;
    }

    public static String stacktrace(Throwable th){
        try {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                th.printStackTrace(pw);
            }
            return sw.toString();
        } catch (Exception ex) {
            // ignore
        }
        return "";
    }
}
