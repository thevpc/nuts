package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.io.ByteArrayPrintStream;

/**
 *
 * type: Command Class
 *
 * @author vpc
 */
public class DefaultNutsWorkspaceInfoFormat implements NutsWorkspaceInfoFormat {

    private final NutsWorkspace ws;
    private final Properties extraProperties = new Properties();
    private boolean minimal = false;
    private boolean showRepositories = false;
    private NutsOutputFormat outputFormat = null;
    boolean fancy = false;

    public DefaultNutsWorkspaceInfoFormat(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsWorkspaceInfoFormat showRepositories() {
        showRepositories(true);
        return this;
    }

    @Override
    public NutsWorkspaceInfoFormat showRepositories(boolean enable) {
        return setShowRepositories(enable);
    }

    @Override
    public NutsWorkspaceInfoFormat setShowRepositories(boolean enable) {
        this.showRepositories = true;
        return this;
    }

    @Override
    public boolean isShowRepositories() {
        return showRepositories;
    }

    @Override
    public NutsWorkspaceInfoFormat addProperty(String key, String value) {
        extraProperties.setProperty(key, value);
        return this;
    }

    @Override
    public NutsWorkspaceInfoFormat addProperties(Properties p) {
        if (p != null) {
            extraProperties.putAll(p);
        }
        return this;
    }

    @Override
    public NutsWorkspaceInfoFormat parseOptions(String... args) {
        NutsCommandLine cmd = new NutsCommandLine(args);
        NutsCommandArg a;
        while ((a = cmd.next()) != null) {
            switch (a.strKey()) {
                case "--min": {
                    this.setMinimal(a.getBooleanValue());
                    break;
                }
                case "--show-repos": {
                    this.setShowRepositories(a.getBooleanValue());
                    break;
                }
                case "--fancy": {
                    this.setFancy(a.getBooleanValue());
                    break;
                }
                case "--trace-format": {
                    this.setOutputFormat(NutsOutputFormat.valueOf(cmd.getValueFor(a).getString().toUpperCase()));
                    break;
                }
                case "--json": {
                    this.setOutputFormat(NutsOutputFormat.JSON);
                    break;
                }
                case "--props": {
                    this.setOutputFormat(NutsOutputFormat.PROPS);
                    break;
                }
                case "--plain": {
                    this.setOutputFormat(NutsOutputFormat.PLAIN);
                    break;
                }
                case "--add": {
                    NutsCommandArg r = cmd.getValueFor(a);
                    extraProperties.put(r.getKey().getString(), r.getValue().getString());
                    break;
                }
                default: {
                    throw new NutsIllegalArgumentException("Unsupported argument " + a);
                }
            }
        }
        return this;
    }

    @Override
    public boolean isFancy() {
        return fancy;
    }

    @Override
    public NutsWorkspaceInfoFormat setFancy(boolean fancy) {
        this.fancy = fancy;
        return this;
    }

    @Override
    public boolean isMinimal() {
        return minimal;
    }

    @Override
    public NutsWorkspaceInfoFormat setMinimal(boolean minimal) {
        this.minimal = minimal;
        return this;
    }

    @Override
    public NutsOutputFormat getOutputFormat() {
        return outputFormat;
    }

    @Override
    public NutsWorkspaceInfoFormat outputFormat(NutsOutputFormat outputFormat) {
        return setOutputFormat(outputFormat);
    }

    @Override
    public NutsWorkspaceInfoFormat setOutputFormat(NutsOutputFormat outputFormat) {
        this.outputFormat = outputFormat;
        return this;
    }

    @Override
    public void print() {
        print(ws.getTerminal());
    }

    @Override
    public void println() {
        println(ws.getTerminal());
    }

    @Override
    public void print(NutsTerminal terminal) {
        print(terminal.out());
    }

    @Override
    public void println(NutsTerminal terminal) {
        println(terminal.out());
    }

    @Override
    public void print(File file) {
        print(file.toPath());
    }

    @Override
    public void println(File file) {
        println(file.toPath());
    }

    @Override
    public void print(Path path) {
        try (Writer w = Files.newBufferedWriter(path)) {
            print(w);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public void println(Path path) {
        try (Writer w = Files.newBufferedWriter(path)) {
            println(w);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public String toString() {
        return format();
    }

    @Override
    public String format() {
        ByteArrayPrintStream out = new ByteArrayPrintStream();
        PrintWriter w = new PrintWriter(out);
        format0(w);
        w.flush();
        return out.toString();
    }

    @Override
    public void print(PrintStream out) {
        PrintWriter p = new PrintWriter(out);
        print(p);
        p.flush();
    }

    @Override
    public void println(PrintStream out) {
        PrintWriter p = new PrintWriter(out);
        println(p);
        p.flush();
    }

    @Override
    public void print(Writer w) {
        format0(w);
    }

    @Override
    public void println(Writer w) {
        try {
            format0(w);
            w.write("\n");
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void format0(Writer w) {
        NutsOutputFormat t = outputFormat;
        if (t == null) {
            t = NutsOutputFormat.PLAIN;
        }
        switch (t) {
            case PLAIN:
                printPlain(w);
                return;
            case JSON:
                printJson(w);
                return;
            case PROPS:
                printProps(w);
                return;
        }
        throw new NutsUnsupportedArgumentException("Unsupported format type " + t);
    }

    public void printProps(Writer w) {
        Map<String, String> p = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : buildWorkspaceMap(isShowRepositories(), true).entrySet()) {
            p.put(e.getKey(), e.getValue() == null ? "" : String.valueOf(e.getValue()));
        }
        CoreIOUtils.storeProperties(p, w);
    }

    public void printJson(Writer w) {
        ws.io().json().pretty().write(buildWorkspaceMap(isShowRepositories(), false), w);
    }

    private static String key(String prefix, String key) {
        if (prefix == null) {
            return key;
        }
        return prefix + "." + key;
    }

//    @Override
    private LinkedHashMap<String, Object> buildWorkspaceMap(boolean deep, boolean exploded) {
        String prefix = null;
        LinkedHashMap<String, Object> props = new LinkedHashMap<>();
        NutsWorkspaceConfigManager configManager = ws.config();
        NutsBootContext rtcontext = configManager.getContext(NutsBootContextType.RUNTIME);
        if (isMinimal()) {
            props.put("nuts-api", rtcontext.getApiId().toString());
            props.put("nuts-runtime", rtcontext.getRuntimeId().toString());
        } else {
            Set<String> extraKeys = new TreeSet<>();
            if (extraProperties != null) {
                extraKeys = new TreeSet(extraProperties.keySet());
            }

            props.put("nuts-api-version", rtcontext.getApiId().getVersion().toString());
            props.put("nuts-api-id", rtcontext.getApiId().toString());
            props.put("nuts-runtime-id", rtcontext.getRuntimeId().toString());
            URL[] cl = configManager.getBootClassWorldURLs();
            List<String> runtimeClassPath = new ArrayList<>();
            if (cl != null) {
                for (URL url : cl) {
                    if (url != null) {
                        String s = url.toString();
                        try {
                            s = Paths.get(url.toURI()).toFile().getPath();
                        } catch (URISyntaxException ex) {
                            s = s.replace(":", "\\:");
                        }
                        runtimeClassPath.add(s);
                    }
                }
            }

            props.put("nuts-runtime-path", CoreStringUtils.join(";", runtimeClassPath));
            props.put("nuts-workspace", configManager.getWorkspaceLocation().toString());
            props.put("nuts-workspace-id", configManager.getUuid());
            props.put("nuts-secure", String.valueOf(ws.security().isSecure()));
            props.put("nuts-store-layout", String.valueOf(configManager.getStoreLocationLayout()));
            props.put("nuts-store-strategy", String.valueOf(configManager.getStoreLocationStrategy()));
            props.put("nuts-repo-store-strategy", String.valueOf(configManager.getRepositoryStoreLocationStrategy()));
            props.put("nuts-option-open-mode", String.valueOf(configManager.getOptions().getOpenMode() == null ? NutsWorkspaceOpenMode.OPEN_OR_CREATE : configManager.getOptions().getOpenMode()));
            props.put("nuts-option-perf", String.valueOf(configManager.getOptions().isPerf()));
            props.put("nuts-option-recover", configManager.getOptions().getInitMode() == null ? "" : configManager.getOptions().getInitMode().name().toLowerCase());
            props.put("nuts-option-read-only", String.valueOf(configManager.getOptions().isReadOnly()));
            props.put("nuts-option-skip-companions", String.valueOf(configManager.getOptions().isSkipInstallCompanions()));
            for (NutsStoreLocation folderType : NutsStoreLocation.values()) {
                props.put("nuts-workspace-" + folderType.name().toLowerCase(), configManager.getStoreLocation(folderType).toString());
            }
            props.put("java-version", System.getProperty("java.version"));
            props.put("java-executable", CoreIOUtils.resolveJavaCommand(null));
            props.put("java-classpath", System.getProperty("java.class.path"));
            props.put("java-library-path", System.getProperty("java.library.path"));
            props.put("os-name", ws.config().getPlatformOs().toString());
            props.put("os-family", ws.config().getPlatformOsFamily().name().toLowerCase());
            if (ws.config().getPlatformOsDist() != null) {
                props.put("os-dist", ws.config().getPlatformOsDist().toString());
            }
            props.put("os-arch", ws.config().getPlatformArch().toString());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            props.put("user-name", System.getProperty("user.name"));
            props.put("user-home", System.getProperty("user.home"));
            props.put("user-dir", System.getProperty("user.dir"));
            props.put("creation-started", dateFormat.format(ws.config().getCreationStartTimeMillis()));
            props.put("creation-finished", dateFormat.format(ws.config().getCreationFinishTimeMillis()));
            props.put("creation-within", CoreCommonUtils.formatPeriodMilli(ws.config().getCreationTimeMillis()).trim());
            props.put("repositories-count", String.valueOf(ws.config().getRepositories().length));
            for (String extraKey : extraKeys) {
                props.put(extraKey, extraProperties.getProperty(extraKey));
            }
            if (deep) {
                Map<String, Object> repositories = new LinkedHashMap<>();
                for (NutsRepository repository : ws.config().getRepositories()) {
                    if (exploded) {
                        repositories.put(repository.config().name(), buildRepoRepoMap(repository, deep, exploded, key(prefix, "repos." + repository.config().name())));
                    } else {
                        repositories.putAll(buildRepoRepoMap(repository, deep, false, key(prefix, "repos." + repository.config().name())));
                    }
                }
                if (exploded) {
                    props.put("repos", repositories);
                }
            }
        }
        return props;
    }

    private void printPlain(Writer w) {
        PrintWriter out = (w instanceof PrintWriter) ? ((PrintWriter) w) : new PrintWriter(w);

        NutsWorkspaceConfigManager configManager = ws.config();
        if (isMinimal()) {
            out.printf("%s/%s", configManager.getContext(NutsBootContextType.RUNTIME).getApiId().getVersion(), configManager.getContext(NutsBootContextType.RUNTIME).getRuntimeId().getVersion());
        } else {
            Map<String, Object> props = buildWorkspaceMap(false, false);
            printMap(out, "", props);
            if (isShowRepositories()) {
                for (NutsRepository repository : ws.config().getRepositories()) {
                    out.append("\n");
                    printRepo(out, fancy, "", repository);
                }
            }
        }
        out.flush();
    }

    private Map<String, Object> buildRepoRepoMap(NutsRepository repo, boolean deep, boolean exploded, String prefix) {
        LinkedHashMap<String, Object> props = new LinkedHashMap<>();
        props.put(key(prefix, "name"), String.valueOf(repo.config().getName()));
        props.put(key(prefix, "global-name"), repo.config().getGlobalName());
        props.put(key(prefix, "uuid"), String.valueOf(repo.config().getUuid()));
        props.put(key(prefix, "type"), repo.config().getType());
        props.put(key(prefix, "speed"), String.valueOf(repo.config().getSpeed()));
        props.put(key(prefix, "enabled"), String.valueOf(repo.config().isEnabled()));
        props.put(key(prefix, "index-enabled"), String.valueOf(repo.config().isIndexEnabled()));
        props.put(key(prefix, "index-subscribed"), String.valueOf(repo.config().isIndexSubscribed()));
        props.put(key(prefix, "location"), repo.config().getLocation(false));
        if (repo.config().getLocation(false) != null) {
            props.put(key(prefix, "location-expanded"), repo.config().getLocation(true));
        }
        props.put(key(prefix, "deploy-order"), String.valueOf(repo.config().getDeployOrder()));
        props.put(key(prefix, "store-location-strategy"), String.valueOf(repo.config().getStoreLocationStrategy()));
        props.put(key(prefix, "store-location"), String.valueOf(repo.config().getStoreLocation()));
        for (NutsStoreLocation value : NutsStoreLocation.values()) {
            props.put(key(prefix, "store-location-" + value.name().toLowerCase()), String.valueOf(repo.config().getStoreLocation(value)));
        }
        props.put(key(prefix, "supported-mirroring"), String.valueOf(repo.config().isSupportedMirroring()));
        if (repo.config().isSupportedMirroring()) {
            props.put(key(prefix, "mirrors-count"), String.valueOf((!repo.config().isSupportedMirroring()) ? 0 : repo.config().getMirrors().length));
        }
        if (deep) {
            if (repo.config().isSupportedMirroring()) {
                Map<String, Object> mirrors = new LinkedHashMap<>();
                for (NutsRepository mirror : repo.config().getMirrors()) {
                    if (exploded) {
                        mirrors.put(mirror.config().name(), buildRepoRepoMap(mirror, deep, true, null));
                    } else {
                        props.putAll(
                                buildRepoRepoMap(mirror, deep, false, key(prefix, "mirrors." + mirror.config().name()))
                        );
                    }
                }
                if (exploded) {
                    props.put("mirrors", mirrors);
                }
            }
        }
        return props;
    }

    private void printRepo(PrintWriter out, boolean fancy, String prefix, NutsRepository repo) {
        out.printf(prefix + "**REPOSITORY :** " + repo.config().getName() + "\n");
        prefix += "   ";
        Map<String, Object> props = buildRepoRepoMap(repo, false, false, null);
        printMap(out, prefix, props);
        if (repo.config().isSupportedMirroring()) {
            for (NutsRepository mirror : repo.config().getMirrors()) {
                out.append("\n");
                printRepo(out, fancy, prefix, mirror);
            }
        }
    }

    private void printMap(PrintWriter out, String prefix, Map<String, Object> props) {
        int len = 23;
        for (String extraKey : props.keySet()) {
            int x = ws.parser().escapeText(extraKey).length();
            if (x > len) {
                len = x;
            }
        }
        boolean first = true;
        for (Map.Entry<String, Object> e : props.entrySet()) {
            if (first) {
                first = false;
            } else {
                out.print("\n");
            }
            printKeyValue(out, fancy, prefix, len, e.getKey(), "" + e.getValue());
        }
    }

    private void printKeyValue(PrintWriter out, boolean fancy, String prefix, int len, String key, String value) {
        boolean requireFancy = false;
        String fancySep = ":";
        if (key.equals("nuts-runtime-path")) {
            requireFancy = true;
            fancySep = ":";
        }
        if (key.equals("nuts-boot-runtime-path")) {
            requireFancy = true;
            fancySep = ":";
        }
        if (key.equals("java.class.path")) {
            requireFancy = true;
            fancySep = File.pathSeparator;
        }
        if (key.equals("java.library.path")) {
            requireFancy = true;
            fancySep = File.pathSeparator;
        }
        printKeyValue(out, fancy && requireFancy, prefix, len, fancySep, key, value);
    }

    private void printKeyValue(PrintWriter out, boolean fancy, String prefix, int len, String fancySep, String key, String value) {
        if (prefix == null) {
            prefix = "";
        }
        if (fancy) {
            String space = prefix + CoreStringUtils.alignLeft("", len + 3) + "[[%s]]";
            String[] split = value.split(fancySep);
            if (split.length == 0) {
                out.print(prefix + CoreStringUtils.alignLeft(key, len - key.length() + ws.parser().escapeText(key).length()) + " : ");
            } else {
                for (int i = 0; i < split.length; i++) {
                    String s = split[i];
                    if (i == 0) {

                    } else {
                        out.print("\n");
                        out.printf(space, s);
                    }
                }
            }
        } else {
            out.printf(prefix + CoreStringUtils.alignLeft(key, len - key.length() + ws.parser().escapeText(key).length()) + " : [[%s]]", value);
        }
    }
}
