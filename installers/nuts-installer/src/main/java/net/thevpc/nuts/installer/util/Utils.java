package net.thevpc.nuts.installer.util;

import net.thevpc.nuts.installer.model.InstallData;
import net.thevpc.nuts.installer.InstallerContext;
import net.thevpc.nuts.installer.NutsInstaller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static boolean isBlank(String url) {
        return url==null || url.trim().isEmpty();
    }
    public static String trim(String url) {
        return url==null ?"": url.trim();
    }

    public static Path downloadFile(String url, String prefix, String ext, Path to) {
        try {
            if (to == null) {
                to = Files.createTempFile(prefix, ext);
            } else {
                Path p = to.getParent();
                if (p != null) {
                    Files.createDirectories(p);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < 3; i++) {
            try (OutputStream os = Files.newOutputStream(to)) {
                try (InputStream is = new URL(url).openStream()) {
                    int nRead;
                    byte[] data = new byte[4];

                    while ((nRead = is.read(data, 0, data.length)) != -1) {
                        os.write(data, 0, nRead);
                    }
                }
                return to;
            } catch (IOException e) {
                if (i >= 2) {
                    throw new RuntimeException(e);
                }
            }
        }
        throw new RuntimeException("cannot download "+url);
    }

    public static String downloadFile(String url) {
        for (int i = 0; i < 3; i++) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (InputStream is = new URL(url).openStream()) {
                int nRead;
                byte[] data = new byte[4];

                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    bos.write(data, 0, nRead);
                }
                return bos.toString();
            } catch (IOException e) {
                if (i >= 2) {
                    throw new RuntimeException(e);
                }
            }
        }
        throw new RuntimeException("cannot download "+url);
    }

    public static Function<String, String> getVarsConverter(InstallerContext context) {
        return new Function<String, String>() {
            @Override
            public String apply(String s) {
                switch (s){
                    case "apiVersion": return InstallData.of(context).installVersion.api;
                }
                return null;
            }
        };
    }

    public static String loadString(String url,Function<String, String> convert) {
        URL r = NutsInstaller.class.getResource(url);
        if (r == null) {
            throw new NoSuchElementException("not found " + url);
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (InputStream is = r.openStream()) {
            int nRead;
            byte[] data = new byte[4];

            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            String str = buffer.toString();
            if (convert != null) {
                str = replace(str, convert);
            }
            return str;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String replace(String str, Function<String, String> fct) {
        Pattern p = Pattern.compile("\\$\\{(?<k>[^\\]]*)\\}");
        Matcher matcher = p.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String k = matcher.group("k");
            String v = fct.apply(k);
            if (v == null) {
                v = k;
                //throw new IllegalArgumentException("Unknown variable " + k);
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(v));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static Map<String, String> toMSS(Map<String, Object> vars) {
        Map<String, String> m = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : vars.entrySet()) {
            m.put(e.getKey(), String.valueOf(e.getValue()));
        }
        return m;
    }

    public static String getWorkspaceLocation() {
        if(isWindows()){
            return System.getProperty("user.home") + "\\AppData\\Roaming\\nuts\\config\\";
        }
        String val = trim(System.getenv("XDG_CONFIG_HOME"));
        if (!val.isEmpty()) {
            return val + "/nuts/";
        }
        return System.getProperty("user.home") + "/.config/nuts" + "/";
    }

    public static boolean isWindows() {
        String e=System.getProperty("os.name");
        if (e == null) {
            e = "";
        } else {
            e = e.trim().toLowerCase();
        }
        if (e.startsWith("win")) {
            return true;
        }
        return false;
    }
}
