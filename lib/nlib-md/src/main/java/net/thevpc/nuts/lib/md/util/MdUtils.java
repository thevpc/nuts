package net.thevpc.nuts.lib.md.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MdUtils {
    public static String times(char c, int x) {
        char[] s = new char[x];
        Arrays.fill(s, c);
        return new String(s);
    }

    public static String toRelativePath(String p, String workingDir) {
        Path to = Paths.get(p).normalize().toAbsolutePath();
        Path wd = Paths.get(workingDir);
        int i = -1;
        for (int j = 0; j < Math.min(to.getNameCount(), wd.getNameCount()); j++) {
            if (to.getName(j).getFileName().toString().equals(wd.getName(j).getFileName().toString())) {
                i = j;
            } else {
                break;
            }
        }
        if (i < 0) {
            return p;
        }
        List<String> ppp = new ArrayList<>();
        for (int j = i + 1; j < wd.getNameCount(); j++) {
            ppp.add("..");
        }
        for (int j = i + 1; j < to.getNameCount(); j++) {
            ppp.add(to.getName(j).getFileName().toString());
        }
        return String.join("/", ppp);
    }

    public static boolean isRelativePath(String p) {
        if (p.startsWith("file:") || p.startsWith("http:") || p.startsWith("https:")) {
            return false;
        }
        if (Paths.get(p).isAbsolute()) {
            return false;
        }
        return true;
    }

    public static String times(String c, int x) {
        int len = c.length();
        char[] src = c.toCharArray();
        char[] dest = new char[x * len];
        int p = 0;
        for (int i = 0; i < x; i++) {
            System.arraycopy(src, 0, dest, p, len);
            p += len;
        }
        return new String(dest);
    }
}
