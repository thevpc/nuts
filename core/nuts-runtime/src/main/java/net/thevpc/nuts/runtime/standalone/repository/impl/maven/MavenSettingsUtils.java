package net.thevpc.nuts.runtime.standalone.repository.impl.maven;

import net.thevpc.nuts.internal.NApiUtilsRPI;

import java.util.Arrays;
import java.util.List;

/**
 * Helpers shared by the runtime and boot {@code settings.xml} loaders so both
 * sides apply the same interpretation of a settings document.
 */
public class MavenSettingsUtils {

    private MavenSettingsUtils() {
    }

    /**
     * Expands a path as declared in {@code settings.xml}. Maven accepts
     * {@code ~} for the user home and a small set of system properties, neither
     * of which is interpreted by the JDK.
     *
     * @param path raw path as found in the settings file
     * @return the expanded path, or the input when it is blank
     */
    public static String expandPath(String path) {
        if (path == null) {
            return null;
        }
        String p = path.trim();
        if (p.isEmpty()) {
            return p;
        }
        p = p.replace('\\', NApiUtilsRPI.getNativePath("/").equals("\\") ? '\\' : '/');
        String home = System.getProperty("user.home");
        if (p.equals("~") || p.startsWith("~/") || p.startsWith("~" + NApiUtilsRPI.getNativePath("/"))) {
            p = home + p.substring(1);
        }
        p = replaceProperty(p, "user.home", home);
        p = replaceProperty(p, "basedir", home);
        return p;
    }

    private static String replaceProperty(String path, String key, String value) {
        if (value == null) {
            return path;
        }
        return path
                .replace("${" + key + "}", value)
                .replace("$" + key, value);
    }

    /**
     * Whether a {@code <mirrorOf>} expression claims to mirror Maven Central.
     * Only the forms that nuts can reason about without a full repository graph
     * are recognised: the literal {@code central} and the catch-all {@code *}.
     *
     * @param mirrorOf raw {@code <mirrorOf>} value
     * @return true if central is covered by the expression
     */
    public static boolean mirrorsCentral(String mirrorOf) {
        if (mirrorOf == null) {
            return false;
        }
        String expr = mirrorOf.trim();
        if (expr.isEmpty() || "external:*".equals(expr)) {
            return false;
        }
        List<String> patterns = Arrays.asList(expr.split(","));
        for (String pattern : patterns) {
            String p = pattern.trim();
            if (p.isEmpty()) {
                continue;
            }
            if ("*".equals(p) || "central".equals(p)) {
                return true;
            }
        }
        return false;
    }
}
