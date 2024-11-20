package net.thevpc.nuts.toolbox.ntomcat.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPs;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class TomcatUtils {

    public static String toValidFileName(String name, String defaultName) {
        String r = NStringUtils.trim(name);
        if (r.isEmpty()) {
            return NStringUtils.trim(defaultName);
        }
        return r
                .replace('/', '_')
                .replace('*', '_')
                .replace('?', '_')
                .replace('\\', '_');
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
        if (value == null) {
            value = "";
        }
        int dot = value.indexOf('/');
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
                || o instanceof Collection) {
            return String.valueOf(o);
        }
        if (o.getClass().isArray()) {
            return Arrays.toString((Object[]) o);
        }
        return "\"" + o.toString().replace("\"", "\\\"") + "\"";
    }

    public static boolean deleteDir(Path src) {
        if (Files.isDirectory(src)) {
            try {
                Files.walk(src)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return true;
        }
        return false;
    }

//    public static boolean copyDir(Path src, Path dest, boolean force) {
//        boolean[] ref = new boolean[1];
//        try {
//            if (!Files.exists(dest)) {
//                ref[0] = true;
//                Files.createDirectories(dest);
//            }
//            Files.walk(src)
//                    .forEach(source -> {
//                        try {
//                            Path to = dest.resolve(src.relativize(source));
//                            if (force || !Files.exists(to)) {
//                                ref[0] = true;
//                                Files.copy(source, to,
//                                        StandardCopyOption.REPLACE_EXISTING);
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    });
//        } catch (IOException e) {
//            throw new UncheckedIOException(e);
//        }
//        return ref[0];
//    }

    public static String getFolderCatalinaHomeVersion(NPath h) {
        if(h!=null) {
            NPath file = h.resolve("RELEASE-NOTES");
            if (file.exists()) {
                try (BufferedReader r = file.getBufferedReader()) {
                    String line = null;
                    while ((line = r.readLine()) != null) {
                        line = line.trim();
                        if (line.startsWith("Apache Tomcat Version")) {
                            String v = line.substring("Apache Tomcat Version".length()).trim();
                            if (!NBlankable.isBlank(v)) {
                                return v;
                            }
                        }
                    }
                } catch (Exception ex) {
                    //
                }
            }
        }
        return null;
    }

    public static RunningTomcat[] getRunningInstances(NSession session) {
        return NPs.of().type("java").getResultList()
                .stream().filter((p) -> p.getName().equals("org.apache.catalina.startup.Bootstrap"))
                .map(x -> new RunningTomcat(x, session)).toArray(RunningTomcat[]::new);
    }
}
