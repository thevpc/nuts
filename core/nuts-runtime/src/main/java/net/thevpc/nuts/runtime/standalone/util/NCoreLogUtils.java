package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.io.NPath;

import java.net.URL;

public class NCoreLogUtils {

    /**
     * returns the compressed form of the given path to be used in progress messages.
     * null is returned as is.
     */
    public static NPath forProgress(NPath p) {
        if (p == null) {
            return null;
        }
        NPath c = p.toCompressedForm();
        return c == null ? p : c;
    }

    public static String forProgressUrl(URL p) {
        if (p == null) {
            return null;
        }
        return forProgressPathString(p.toString());
    }

    public static String forProgressPathString(String p) {
        if (p == null || p.trim().isEmpty()) {
            return p;
        }
        NPath path = NPath.of(p);
        if (path == null) {
            return p;
        }
        return forProgress(path).toString();
    }
}
