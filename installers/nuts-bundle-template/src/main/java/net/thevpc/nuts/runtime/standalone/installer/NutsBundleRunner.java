package net.thevpc.nuts.runtime.standalone.installer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class NutsBundleRunner {
    public static void main(String[] args) {
        new NutsBundleRunner().run(args);
    }

    private boolean run(String[] args) {
        Map<String, String> env = readKeyVarFile("nuts-bundle-vars.config", false);
        Map<String, String> info = readKeyVarFile("nuts-bundle-info.config", false);
        for (Map.Entry<String, String> e : info.entrySet()) {
            if (!env.containsKey(e.getKey())) {
                env.put(e.getKey(), e.getValue());
            }
        }
        boolean verbose = false;
        String appVersion = info.get("version");
        if (appVersion == null || appVersion.trim().isEmpty()) {
            appVersion = "1.0";
        }
        String appTitle = info.get("title");
        if (appTitle == null || appTitle.trim().isEmpty()) {
            appTitle = "NutsBundleRunner";
        }
        String appDescription = info.get("description");
        String layout = null;
        for (String arg : args) {
            String k = null;
            String v = null;
            if (arg.startsWith("-")) {
                String[] kv = splitKeyValue(arg);
                k = kv[0];
                v = kv[1];
            } else {
                k = arg;
                v = null;
            }
            switch (k) {
                case "--version": {
                    System.out.println(appTitle + " v" + appVersion);
                    if (appDescription != null && !appDescription.trim().isEmpty()) {
                        System.out.println();
                        System.out.println(appDescription);
                    }
                    return true;
                }
                case "--target": {
                    if (v != null && v.trim().length() > 0) {
                        v = v.trim();
                        env.put("target", v);
                    }
                    break;
                }
                case "--verbose": {
                    if (v != null && v.trim().length() > 0) {
                        verbose = Boolean.parseBoolean(v);
                    } else {
                        verbose = true;
                    }
                    break;
                }
                case "--layout": {
                    if (v == null) {
                        System.err.println("missing option value : " + k);
                        return false;
                    }
                    layout = v;
                    break;
                }
                case "--help": {
                    System.out.println(appTitle + " v" + appVersion);
                    if (appDescription != null && !appDescription.trim().isEmpty()) {
                        System.out.println();
                        System.out.println(appDescription);
                    }
                    if (isResourceAvailable("nuts-bundle.help")) {
                        System.out.println();
                        try {
                            try (BufferedReader br = new BufferedReader(new InputStreamReader(createInputStream("nuts-bundle.help")))) {
                                String line;
                                while ((line = br.readLine()) != null) {
                                    System.out.println(replaceDollarString(line, env));
                                }
                            }
                        } catch (IOException ex) {
                            throw new UncheckedIOException(ex);
                        }
                        return true;
                    } else {
                        System.out.println("Options : ");
                        System.out.println("  --help");
                        System.out.println("      show help and exit");
                        System.out.println("  --version");
                        System.out.println("      show version exit");
                        System.out.println("  --verbose");
                        System.out.println("      verbose mode");
                        System.out.println("  ---<var>=<value>");
                        System.out.println("      define a new var named");
                        return true;
                    }
                }
                default: {
                    if (k.startsWith("-")) {
                        if (k.startsWith("---")) {
                            env.put(k.substring(3), v == null ? "true" : v);
                        } else {
                            System.err.println("unsupported option : " + k);
                            return false;
                        }
                    } else {
                        env.put("target", v);
                    }
                }
            }
        }
        String filesPath = "nuts-bundle-files" + ((layout == null || layout.isEmpty()) ? "" : ("." + layout)) + ".config";
        if (!isResourceAvailable(filesPath)) {
            if ((layout == null || layout.isEmpty())) {
                System.err.println("missing files file : " + filesPath);
                return false;
            } else {
                System.err.println("invalid layout " + layout + " . missing files file : " + filesPath);
                return false;
            }
        }
        List<String[]> config = readArgsFileEntries(filesPath, true);
        if (config.isEmpty()) {
            System.err.println("empty config file");
            return false;
        }
        for (String[] r : config) {
            if (r.length > 0) {
                switch (r[0].toLowerCase()) {
                    case "copy": {
                        if (r.length == 3) {
                            String k = r[1];
                            String v = replaceDollarString(r[2], env);
                            copyFile(k, v, verbose);
                        } else {
                            throw new IllegalArgumentException("expected copy from to");
                        }
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("unsupported command " + r[0]);
                    }
                }
            }
        }
        return true;
    }

    private static String replaceDollarString(String text, Map<String, String> m) {
        return replaceDollarString(text, m, true, 1000);
    }

    private static String replaceDollarString(String text, Map<String, String> m, boolean err, int max) {
        char[] t = (text == null ? new char[0] : text.toCharArray());
        int p = 0;
        int length = t.length;
        StringBuilder sb = new StringBuilder(length);
        StringBuilder n = new StringBuilder(length);
        StringBuilder img = new StringBuilder(length);
        while (p < length) {
            char c = t[p];
            if (c == '$') {
                img.setLength(0);
                img.append(c);
                if (p + 1 < length && t[p + 1] == '{') {
                    img.append(t[p + 1]);
                    p += 2;
                    n.setLength(0);
                    while (p < length) {
                        c = t[p];
                        if (c != '}') {
                            img.append(c);
                            n.append(c);
                            p++;
                        } else {
                            img.append(c);
                            break;
                        }
                    }
                    sb.append(getProp(n.toString(), img.toString(), m, err, max - 1));
                } else if (p + 1 < length && _isValidMessageVar(t[p + 1])) {
                    p++;
                    n.setLength(0);
                    while (p < length) {
                        c = t[p];
                        if (_isValidMessageVar(c)) {
                            img.append(c);
                            n.append(c);
                            p++;
                        } else {
                            p--;
                            break;
                        }
                    }
                    sb.append(getProp(n.toString(), img.toString(), m, err, max - 1));
                } else {
                    sb.append(c);
                }
            } else {
                sb.append(c);
            }
            p++;
        }
        return sb.toString();
    }

    private static String getProp(String n, String image, Map<String, String> m, boolean err, int max) {
        String x = m.get(n);
        if (x == null) {
            try {
                x = System.getProperty(n);
            } catch (Exception e) {
                //
            }
        }
        if (x == null) {
            try {
                x = System.getenv(n);
            } catch (Exception e) {
                //
            }
        }
        if (x == null) {
            if (err) {
                throw new IllegalArgumentException("var not found " + n);
            } else {
                x = image;
            }
        } else {
            if (x.indexOf('$') >= 0) {
                x = replaceDollarString(x, m, false, max);
            }
        }
        return x;
    }

    static boolean _isValidMessageVar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_';
    }


    private String[] splitKeyValue(String item) {
        int i = item.indexOf('=');
        if (i >= 0) {
            return new String[]{
                    item.substring(0, i),
                    item.substring(i + 1),
            };
        } else {
            return new String[]{
                    item,
                    null,
            };
        }
    }

    private boolean isResourceAvailable(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        String fromFullPath = "/META-INF" + path;
        return getClass().getResource(fromFullPath) != null;
    }

    private InputStream createInputStream(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        String fromFullPath = "/META-INF" + path;
        InputStream in = getClass().getResourceAsStream(fromFullPath);
        if (in == null) {
            throw new UncheckedIOException(new IOException(fromFullPath + " not found"));
        }
        return in;
    }

    private Map<String, String> readKeyVarFile(String pp, boolean required) {
        Map<String, String> m = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : readKeyVarFileEntries(pp, required)) {
            m.put(e.getKey(), e.getValue());
        }
        return m;
    }

    private List<String[]> readArgsFileEntries(String pp, boolean required) {
        if (!required) {
            if (!isResourceAvailable(pp)) {
                return new ArrayList<>();
            }
        }
        List<String[]> result = new ArrayList<>();
        try {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(createInputStream(pp)))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        if (line.charAt(0) != '#') {
                            String[] args = parseDefaultList(line);
                            if (args.length > 0) {
                                result.add(args);
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return result;
    }

    private List<Map.Entry<String, String>> readKeyVarFileEntries(String pp, boolean required) {
        if (!required) {
            if (!isResourceAvailable(pp)) {
                return new ArrayList<>();
            }
        }
        List<Map.Entry<String, String>> result = new ArrayList<>();
        try {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(createInputStream(pp)))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        if (line.charAt(0) != '#') {
                            int x = line.indexOf('=');
                            if (x > 0) {
                                String from = line.substring(0, x).trim();
                                String to = line.substring(x + 1).trim();
                                result.add(new AbstractMap.SimpleEntry<>(from, to));
                            } else {
                                result.add(new AbstractMap.SimpleEntry<>(line, ""));
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return result;
    }

    private void copyFile(String from, String to, boolean verbose) {
        Path toPath = Paths.get(to);
        if (Files.isDirectory(toPath)) {
            throw new UncheckedIOException(new IOException(to + " is already a directory"));
        }
        String fromFullPath = "/bundle";
        try {
            if (!from.startsWith("/")) {
                from = "/" + from;
            }
            fromFullPath = fromFullPath + from;
            Path p = toPath.getParent();
            if (p != null) {
                p.toFile().mkdirs();
            }
            if (verbose) {
                System.err.println("copy " + fromFullPath + " to " + toPath);
            }
            try (InputStream in = createInputStream(fromFullPath)) {
                try (OutputStream os = Files.newOutputStream(toPath)) {
                    byte[] buffer = new byte[2048];
                    int count;
                    while ((count = in.read(buffer)) > 0) {
                        os.write(buffer, 0, count);
                    }
                }
            }
        } catch (IOException ex) {
            System.err.println("unable to copy /META-INF" + fromFullPath + " to " + toPath + " : " + ex.toString());
            throw new UncheckedIOException(ex);
        }
    }

    public static String[] parseDefaultList(String commandLineString) {
        if (commandLineString == null) {
            return new String[0];
        }
        List<String> args = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        final int START = 0;
        final int IN_WORD = 1;
        final int IN_QUOTED_WORD = 2;
        final int IN_DBQUOTED_WORD = 3;
        int status = START;
        char[] charArray = commandLineString.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            switch (status) {
                case START: {
                    switch (c) {
                        case ' ':
                        case '\t': {
                            //ignore
                            break;
                        }
                        case '\r':
                        case '\n': //support multiline commands
                        {
                            //ignore
                            break;
                        }
                        case '\'': {
                            status = IN_QUOTED_WORD;
                            //ignore
                            break;
                        }
                        case '"': {
                            status = IN_DBQUOTED_WORD;
                            //ignore
                            break;
                        }
                        case '\\': {
                            status = IN_WORD;
                            i++;
                            sb.append(charArray[i]);
                            break;
                        }
                        default: {
                            sb.append(c);
                            status = IN_WORD;
                            break;
                        }
                    }
                    break;
                }
                case IN_WORD: {
                    switch (c) {
                        case ' ': {
                            args.add(sb.toString());
                            sb.delete(0, sb.length());
                            status = START;
                            break;
                        }
                        case '\'':
                        case '"': {
                            throw new IllegalArgumentException("illegal char " + c);
                        }
                        case '\\': {
                            i++;
                            sb.append(charArray[i]);
                            break;
                        }
                        default: {
                            sb.append(c);
                            break;
                        }
                    }
                    break;
                }
                case IN_QUOTED_WORD: {
                    switch (c) {
                        case '\'': {
                            args.add(sb.toString());
                            sb.delete(0, sb.length());
                            status = START;
                            //ignore
                            break;
                        }
                        default: {
                            sb.append(c);
                            //ignore
                            break;
                        }
                    }
                    break;
                }
                case IN_DBQUOTED_WORD: {
                    switch (c) {
                        case '"': {
                            args.add(sb.toString());
                            sb.delete(0, sb.length());
                            status = START;
                            //ignore
                            break;
                        }
                        case '\\': {
                            i = readEscapedArg(charArray, i + 1, sb);
                            //ignore
                            break;
                        }
                        default: {
                            sb.append(c);
                            //ignore
                            break;
                        }
                    }
                }
            }
        }
        switch (status) {
            case START: {
                break;
            }
            case IN_WORD: {
                args.add(sb.toString());
                sb.delete(0, sb.length());
                break;
            }
            case IN_QUOTED_WORD: {
                throw new IllegalArgumentException("expected quote");
            }
        }
        return args.toArray(new String[0]);
    }

    private static int readEscapedArg(char[] charArray, int i, StringBuilder sb) {
        char c = charArray[i];
        switch (c) {
            case '\\':
            case ';':
            case '\"':
            case '\'':
            case '$':
            case ' ':
            case '<':
            case '>':
            case '(':
            case ')':
            case '~':
            case '&':
            case '|': {
                sb.append(c);
                break;
            }
            default: {
                sb.append('\\').append(c);
                break;
            }
        }
        return i;
    }

}
