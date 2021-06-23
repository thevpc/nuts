package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.NutsPath;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class NutsCompressedPath extends NutsPathBase {
    private NutsPath base;
    private String compressedForm;

    public NutsCompressedPath(NutsPathBase base) {
        super(base.getSession());
        this.base = base;
        this.compressedForm = compressUrl(base.toString());
    }

    @Override
    public Path toFilePath() {
        return base.toFilePath();
    }

    public static String compressUrl(String path) {
        if (path.startsWith("http://")
                || path.startsWith("https://")) {
            URL u = null;
            try {
                u = new URL(path);
            } catch (MalformedURLException e) {
                return path;
            }
            // pre-compute length of StringBuffer
            int len = u.getProtocol().length() + 1;
            if (u.getAuthority() != null && u.getAuthority().length() > 0) {
                len += 2 + u.getAuthority().length();
            }
            if (u.getPath() != null) {
                len += u.getPath().length();
            }
            if (u.getQuery() != null) {
                len += 1 + u.getQuery().length();
            }
            if (u.getRef() != null) {
                len += 1 + u.getRef().length();
            }

            StringBuffer result = new StringBuffer(len);
            result.append(u.getProtocol());
            result.append(":");
            if (u.getAuthority() != null && u.getAuthority().length() > 0) {
                result.append("//");
                result.append(u.getAuthority());
            }
            if (u.getPath() != null) {
                result.append(compressPath(u.getPath(), 0, 2));
            }
            if (u.getQuery() != null) {
                result.append('?');
                result.append("...");
//                result.append(u.getQuery());
            }
            if (u.getRef() != null) {
                result.append("#");
                result.append("...");
//                result.append(u.getRef());
            }
            return result.toString();

        } else {
            return compressPath(path);
        }
    }

    public static String compressPath(String path) {
        return compressPath(path, 2, 2);
    }

    public static String compressPath(String path, int left, int right) {
        String p = System.getProperty("user.home");
        if (path.startsWith("http://") || path.startsWith("https://")) {
            int interr = path.indexOf('?');
            if (interr > 0) {
                path = path.substring(0, interr) + "?...";
            }
        }
        if (path.startsWith(p + File.separator)) {
            path = "~" + path.substring(p.length());
        } else if (path.startsWith("http://")) {
            int x = path.indexOf('/', "http://".length() + 1);
            if (x <= 0) {
                return path;
            }
            return path.substring(0, x) + compressPath(path.substring(x), 0, right);
        } else if (path.startsWith("https://")) {
            int x = path.indexOf('/', "https://".length() + 1);
            if (x <= 0) {
                return path;
            }
            return path.substring(0, x) + compressPath(path.substring(x), 0, right);
        }
        List<String> a = new ArrayList<>(Arrays.asList(path.split("[\\\\/]")));
        int min = left + right + 1;
        if (a.size() > 0 && a.get(0).equals("")) {
            left += 1;
            min += 1;
        }
        if (a.size() > min) {
            a.add(left, "...");
            int len = a.size() - right - left - 1;
            for (int i = 0; i < len; i++) {
                a.remove(left + 1);
            }
        }
        return String.join("/", a);
    }

    @Override
    public String name() {
        return base.name();
    }

    @Override
    public String location() {
        return base.location();
    }

    @Override
    public NutsPath compressedForm() {
        return this;
    }

    @Override
    public URL toURL() {
        return base.toURL();
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, compressedForm);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsCompressedPath that = (NutsCompressedPath) o;
        return base.equals(that.base) && compressedForm.equals(that.compressedForm);
    }

    @Override
    public String toString() {
        return String.valueOf(compressedForm);
    }

    @Override
    public InputStream inputStream() {
        return base.inputStream();
    }

    @Override
    public OutputStream outputStream() {
        return base.outputStream();
    }
}
