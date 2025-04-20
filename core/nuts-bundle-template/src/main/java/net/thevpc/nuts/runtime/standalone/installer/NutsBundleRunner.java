package net.thevpc.nuts.runtime.standalone.installer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NutsBundleRunner {
    boolean verbose = false;
    String layout = null;
    String appVersion;
    String appTitle;
    Set<String> osFamilies;
    Map<String, String> env;
    Map<String, String> info;

    public static void main(String[] args) {
        new NutsBundleRunner().run(args);
    }

    private boolean run(String[] args) {
        env = readKeyVarFile("nuts-bundle-vars.config", false);
        info = readKeyVarFile("nuts-bundle-info.config", false);
        for (Map.Entry<String, String> e : info.entrySet()) {
            if (!env.containsKey(e.getKey())) {
                env.put(e.getKey(), e.getValue());
            }
        }
        appVersion = info.get("version");
        if (appVersion == null || appVersion.trim().isEmpty()) {
            appVersion = "1.0";
        }
        appTitle = info.get("title");
        if (appTitle == null || appTitle.trim().isEmpty()) {
            appTitle = "NutsBundleRunner";
        }
        String appDescription = info.get("description");
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
                    doLogHelp(appTitle + " v" + appVersion);
                    if (appDescription != null && !appDescription.trim().isEmpty()) {
                        doLogHelp("");
                        doLogHelp(appDescription);
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
                        doLogError("missing option value : " + k);
                        return false;
                    }
                    layout = v;
                    break;
                }
                case "--help": {
                    doLogHelp(appTitle + " v" + appVersion);
                    if (appDescription != null && !appDescription.trim().isEmpty()) {
                        doLogHelp("");
                        doLogHelp(appDescription);
                    }
                    if (isResourceAvailable("nuts-bundle.help")) {
                        doLogHelp("");
                        try {
                            try (BufferedReader br = new BufferedReader(new InputStreamReader(createInputStream("nuts-bundle.help")))) {
                                String line;
                                while ((line = br.readLine()) != null) {
                                    doLogHelp(replaceDollarString(line));
                                }
                            }
                        } catch (IOException ex) {
                            throw new UncheckedIOException(ex);
                        }
                        return true;
                    } else {
                        doLogHelp("Options : ");
                        doLogHelp("  --help");
                        doLogHelp("      show help and exit");
                        doLogHelp("  --version");
                        doLogHelp("      show version exit");
                        doLogHelp("  --verbose");
                        doLogHelp("      verbose mode");
                        doLogHelp("  ---<var>=<value>");
                        doLogHelp("      define a new var named");
                        return true;
                    }
                }
                default: {
                    if (k.startsWith("-")) {
                        if (k.startsWith("---")) {
                            env.put(k.substring(3), v == null ? "true" : v);
                        } else {
                            doLogError("unsupported option : " + k);
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
                doLogError("missing files file : " + filesPath);
                return false;
            } else {
                doLogError("invalid layout " + layout + " . missing files file : " + filesPath);
                return false;
            }
        }
        List<String[]> config = readArgsFileEntries(filesPath, true);
        if (config.isEmpty()) {
            doLogError("empty config file");
            return false;
        }
        for (String[] r : config) {
            if (r.length > 0) {
                try {
                    String[] subCmd = Arrays.copyOfRange(r, 1, r.length);
                    switch (r[0].toLowerCase()) {
                        case "install": {
                            if (!cmdInstall(subCmd)) {
                                return false;
                            }
                            break;
                        }
                        case "set-executable": {
                            if (!cmdSetExecutable(subCmd)) {
                                return false;
                            }
                            break;
                        }
                        default: {
                            doLogError("unsupported command " + r[0] + " in " + commandToString(r));
                            return false;
                        }
                    }
                } catch (RuntimeException ex) {
                    doLogError("command failed : " + commandToString(r));
                    doLogError(ex.toString());
                    return false;
                }
            }
        }
        return true;
    }


    private boolean cmdInstall(String[] r) {
        String commandName = "install";
        String from = null;
        String to = null;
        Path outputPath = null;
        boolean executable = false;
        Set<String> acceptableOses = new HashSet<>();
        for (String s : r) {
            if (s.startsWith("-")) {
                if (readOptionAcceptableOS(s, acceptableOses)) {
                    //ok
                } else if (s.equals("--executable")) {
                    executable = true;
                } else {
                    doLogError("Unsupported option : " + s + " in " + commandToString(commandName, r));
                    return false;
                }
            } else if (from == null) {
                from = s;
            } else if (to == null) {
                to = s;
            } else {
                doLogError("Unsupported argument : " + s + " in " + commandToString(commandName, r));
                return false;
            }
        }
        if (from == null || to == null) {
            doLogError("expected install <from> <to>" + " in " + commandToString(commandName, r));
            return false;
        }
        if (!isAcceptabeOs(acceptableOses)) {
            doDebug("skipped command for incompatible OS " + acceptableOses + " (Current is " + getOsFamilies() + " : " + System.getProperty("os.name") + "). command was :" + commandToString(commandName, r));
            return true;
        }
        to = replaceDollarString(to, env);
        Path toPath = Paths.get(to);
        if (Files.isDirectory(toPath)) {
            doLogError(to + " is already a directory" + " in " + commandToString(commandName, r));
            return false;
        }
        String fromFullPath = "/bundle";
        try {
            if (!from.startsWith("/")) {
                from = "/" + from;
            }
            fromFullPath = fromFullPath + from;
            outputPath = toPath.getParent();
            if (outputPath != null) {
                outputPath.toFile().mkdirs();
            }
            doDebug("install " + fromFullPath + " to " + toPath);
            try (InputStream in = createInputStream(fromFullPath)) {
                try (OutputStream os = Files.newOutputStream(toPath)) {
                    copyStream(in, os);
                }
            }
        } catch (Exception ex) {
            doLogError("unable to copy /META-INF" + fromFullPath + " to " + toPath + " : " + ex.toString() + " in " + commandToString(commandName, r));
            return false;
        }
        try {
            if (outputPath != null && executable) {
                toPath.toFile().setExecutable(true);
            }
        } catch (Exception ex) {
            doLogError("unable to make " + toPath + " executable : " + ex.toString() + " in " + commandToString(commandName, r));
            return false;
        }
        return true;
    }


    private boolean cmdSetExecutable(String[] r) {
        String commandName = "set-executable";
        List<String> toMakeExecutables = new ArrayList<>();
        Set<String> acceptableOses = new HashSet<>();
        for (String s : r) {
            if (s.startsWith("-")) {
                if (readOptionAcceptableOS(s, acceptableOses)) {
                    //ok
                } else {
                    doLogError("Unsupported option : " + s + " in " + commandToString(commandName, r));
                    return false;
                }
            } else {
                toMakeExecutables.add(s);
            }
        }
        if (toMakeExecutables.isEmpty()) {
            doLogError("missing paths. Expected set-executable <path>..." + " in " + commandToString(commandName, r));
            return false;
        }
        if (!isAcceptabeOs(acceptableOses)) {
            doDebug("skipped command for incompatible OS " + acceptableOses + " (Current is " + getOsFamilies() + " : " + System.getProperty("os.name") + "). command was :" + commandToString(commandName, r));
            return true;
        }
        for (String p : toMakeExecutables) {
            p = replaceDollarString(p, env);
            for (File file : expandFilesByGlob(p)) {
                file.setExecutable(true);
                doDebug("set-executable " + file.getAbsolutePath());
            }
        }
        return true;
    }


    /// ////////////////////////////////////////////////////
    /// INSTANCE UTILITIES
    /// ////////////////////////////////////////////////////

    private void doLogHelp(String msg) {
        System.out.println(msg);
    }

    private void doLogTrace(String msg) {
        System.out.println("[INFO ] " + msg);
    }

    private void doLogError(String msg) {
        System.err.println("[ERROR] " + msg);
    }

    private void doLogWarning(String msg) {
        System.err.println("[WARN ] " + msg);
    }

    private void doDebug(String msg) {
        if (verbose) {
            System.err.println("[DEBUG] " + msg);
        }
    }

    private String replaceDollarString(String text) {
        return replaceDollarString(text, env);
    }

    public boolean isAcceptabeOs(Set<String> requestedOsFamily) {
        if (requestedOsFamily.isEmpty()) {
            return true;
        }
        Set<String> currentOsFamilies = getOsFamilies();
        for (String currentOsFamily : currentOsFamilies) {
            if (requestedOsFamily.contains(currentOsFamily)) {
                return true;
            }
        }
        return false;
    }

    public Set<String> getOsFamilies() {
        if (osFamilies == null) {
            doDebug("resolve OS Name " + System.getProperty("os.name"));
            String property = System.getProperty("os.name").toLowerCase();
            if (property.startsWith("linux")) {
                osFamilies = new LinkedHashSet<>(Arrays.asList("linux", "posix"));
            } else if (property.startsWith("win")) {
                osFamilies = new LinkedHashSet<>(Arrays.asList("windows"));
            } else if (property.startsWith("mac")) {
                osFamilies = new LinkedHashSet<>(Arrays.asList("macos"));
            } else if (property.startsWith("sunos") || property.startsWith("solaris")) {
                osFamilies = new LinkedHashSet<>(Arrays.asList("sunos", "posix", "unix"));
            } else if (property.startsWith("zos")) {
                osFamilies = new LinkedHashSet<>(Arrays.asList("zos", "posix", "unix"));
            } else if (property.startsWith("freebsd")) {
                osFamilies = new LinkedHashSet<>(Arrays.asList("freebsd", "posix", "unix"));
            } else if (property.startsWith("openbsd")) {
                osFamilies = new LinkedHashSet<>(Arrays.asList("openbsd", "posix", "unix"));
            } else if (property.startsWith("netbsd")) {
                osFamilies = new LinkedHashSet<>(Arrays.asList("netbsd", "posix", "unix"));
            } else if (property.startsWith("aix")) {
                osFamilies = new LinkedHashSet<>(Arrays.asList("aix", "posix", "unix"));
            } else if (property.startsWith("hpux")) {
                osFamilies = new LinkedHashSet<>(Arrays.asList("hpux", "posix", "unix"));
            } else if (property.startsWith("os400") && property.length() <= 5 || !Character.isDigit(property.charAt(5))) {
                osFamilies = new LinkedHashSet<>(Arrays.asList("os400", "unix"));
            } else {
                osFamilies = new LinkedHashSet<>(Arrays.asList("unknown"));
            }
            doDebug("resolve OS Family as " + osFamilies);
        }
        return osFamilies;
    }

    /// ////////////////////////////////////////////////////
    /// UTILITIES
    /// ////////////////////////////////////////////////////

    private void copyStream(InputStream in, OutputStream os) {
        byte[] buffer = new byte[2048];
        int count;
        try {
            while ((count = in.read(buffer)) > 0) {
                os.write(buffer, 0, count);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static boolean readOptionAcceptableOS(String options, Set<String> acceptableOses) {
        if (options.equals("--windows")) {
            acceptableOses.add("windows");
            return true;
        } else if (options.equals("--linux")) {
            acceptableOses.add("linux");
            return true;
        } else if (options.equals("--posix")) {
            acceptableOses.add("posix");
            return true;
        } else if (options.equals("--macos")) {
            acceptableOses.add("macos");
            return true;
        } else if (options.equals("--unix")) {
            acceptableOses.add("unix");
            return true;
        } else if (options.equals("--unknown")) {
            acceptableOses.add("unknown");
            return true;
        }
        return false;
    }

    private static String commandToString(String name, String[] r) {
        List<String> all = new ArrayList<>();
        all.add(name);
        all.addAll(Arrays.asList(r));
        return commandToString(all.toArray(new String[0]));
    }

    private static String commandToString(String[] r) {
        return Arrays.stream(r).map(x -> x).collect(Collectors.joining(" "));
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

    private String[] splitLastIndexOfAny(String item, String[] sep) {
        int x = -1;
        String goodSep = null;
        for (int i = 0; i < sep.length; i++) {
            int v = item.lastIndexOf(sep[i]);
            if (v >= 0 && v > x) {
                x = v;
                goodSep = sep[i];
            }
        }
        if (x >= 0) {
            return new String[]{
                    item.substring(0, x),
                    item.substring(x + goodSep.length()),
            };
        }
        return new String[]{
                item.substring(0, x),
                null,
        };
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
                            try {
                                String[] args = parseDefaultList(line);
                                if (args.length > 0) {
                                    result.add(args);
                                }
                            } catch (RuntimeException e) {
                                doLogError("invalid command : " + line);
                                doLogError("                : " + e);
                                throw e;
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            doLogError("invalid file : " + pp);
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


    public List<File> expandFilesByGlob(String text) {
        boolean glob = text.contains("*") || text.contains("?");
        if (!glob) {
            return Arrays.asList(new File(text));
        }
        String[] sp = splitLastIndexOfAny(text, new String[]{"/", "\\"});
        String fileName;
        String parentPath = ".";
        if (sp[1] == null) {
            fileName = text;
        } else {
            parentPath = sp[0];
            fileName = sp[1];
        }
        Pattern p = compileGlob(fileName);
        File[] result = new File(parentPath).listFiles((dir, name) -> p.matcher(name).matches());
        if (result == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(result);
    }

    public Pattern compileGlob(String text) {
        StringBuilder sb = new StringBuilder();
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
                case '.':
                case '{':
                case '}':
                case '<':
                case '>':
                case '[':
                case ']':
                case '^':
                case '$': {
                    sb.append('\\');
                    sb.append(c);
                    break;
                }
                case '*': {
                    sb.append(".*");
                    break;
                }
                case '?': {
                    sb.append(".");
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        return Pattern.compile(sb.toString());
    }
}
