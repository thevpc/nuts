package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.common.io.ByteArrayPrintStream;
import net.vpc.common.strings.StringUtils;
import net.vpc.common.util.Chronometer;

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

public class DefaultNutsWorkspaceInfoFormat implements NutsWorkspaceInfoFormat {

    private NutsWorkspace ws;
    private Set<String> options = new HashSet<>();
    private Properties extraProperties = new Properties();

    public DefaultNutsWorkspaceInfoFormat(NutsWorkspace ws) {
        this.ws = ws;
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
    public NutsWorkspaceInfoFormat addOption(String o) {
        if (o != null) {
            for (String o1 : Arrays.asList(o.split(","))) {
                if (!StringUtils.isEmpty(o1)) {
                    options.add(o1);
                }
            }
        }
        return this;
    }

    @Override
    public NutsWorkspaceInfoFormat addOptions(String... o) {
        for (String option : options) {
            addOption(option);
        }
        return this;
    }

    @Override
    public void format() {
        format(ws.getTerminal());
    }

    @Override
    public void format(NutsTerminal terminal) {
        format(terminal.getOut());
    }

    @Override
    public void format(File file) {
        format(file.toPath());
    }

    @Override
    public void format(Path path) {
        try (Writer w = Files.newBufferedWriter(path)) {
            format(w);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public String toString() {
        ByteArrayPrintStream out = new ByteArrayPrintStream();
        PrintWriter w = new PrintWriter(out);
        format0(w);
        w.flush();
        return out.toString();
    }

    @Override
    public void format(PrintStream out) {
        PrintWriter p = new PrintWriter(out);
        format(p);
        p.flush();
    }

    @Override
    public void format(Writer w) {
        format0(w);
    }

//    @Override
    private void format0(Writer w) {
        PrintWriter out = (w instanceof PrintWriter) ? ((PrintWriter) w) : new PrintWriter(w);

        NutsWorkspaceConfigManager configManager = ws.config();
        if (options.contains("min")) {
            out.printf("%s/%s", configManager.getRunningContext().getApiId().getVersion(), configManager.getRunningContext().getRuntimeId().getVersion());
        } else {
            boolean fancy = false;
            if (options.contains("fancy")) {
                fancy = true;
            }
            Set<String> extraKeys = new TreeSet<>();
            if (extraProperties != null) {
                extraKeys = new TreeSet(extraProperties.keySet());
            }

            LinkedHashMap<String, String> props = new LinkedHashMap<>();
            props.put("nuts-version", configManager.getRunningContext().getApiId().getVersion().toString());
            props.put("nuts-api", configManager.getRunningContext().getApiId().toString());
            props.put("nuts-runtime", configManager.getRunningContext().getRuntimeId().toString());
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

            props.put("nuts-runtime-path", StringUtils.join(":", runtimeClassPath));
            props.put("nuts-workspace", configManager.getWorkspaceLocation().toString());
            props.put("nuts-workspace-id", configManager.getUuid());
            props.put("nuts-secure", String.valueOf(configManager.isSecure()));
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
            props.put("java-executable", CoreNutsUtils.resolveJavaCommand(null));
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
            props.put("creation-within", Chronometer.formatPeriodMilli(ws.config().getCreationTimeMillis()).trim());
            props.put("repositories-count", String.valueOf(ws.config().getRepositories().length));
            for (String extraKey : extraKeys) {
                props.put(extraKey, extraProperties.getProperty(extraKey));
            }
            printMap(out, fancy, "", props);
            for (NutsRepository repository : ws.config().getRepositories()) {
                out.append("\n");
                printRepo(out, fancy, "", repository);
            }
        }
        out.flush();
    }

    private void printRepo(PrintWriter out, boolean fancy, String prefix, NutsRepository repo) {
        out.printf(prefix + "REPOSITORY : " + repo.getName() + "\n");
        prefix += "   ";
        LinkedHashMap<String, String> props = new LinkedHashMap<>();
        props.put("name", String.valueOf(repo.config().getName()));
        props.put("global-name", repo.config().getGlobalName());
        props.put("uuid", String.valueOf(repo.config().getUuid()));
        props.put("type", repo.config().getType());
        props.put("speed", String.valueOf(repo.config().getSpeed()));
        props.put("enabled", String.valueOf(repo.config().isEnabled()));
        props.put("index-enabled", String.valueOf(repo.config().isIndexEnabled()));
        props.put("index-subscribed", String.valueOf(repo.config().isIndexSubscribed()));
        props.put("location", repo.config().getLocation(false));
        if (repo.config().getLocation(false) != null) {
            props.put("location-expanded", repo.config().getLocation(true));
        }
        props.put("deploy-order", String.valueOf(repo.config().getDeployOrder()));
        props.put("store-location-strategy", String.valueOf(repo.config().getStoreLocationStrategy()));
        props.put("store-location", String.valueOf(repo.config().getStoreLocation()));
        for (NutsStoreLocation value : NutsStoreLocation.values()) {
            props.put("store-location-" + value.name().toLowerCase(), String.valueOf(repo.config().getStoreLocation(value)));
        }
        props.put("supported-mirroring", String.valueOf(repo.config().isSupportedMirroring()));
        if (repo.config().isSupportedMirroring()) {
            props.put("mirrors-count", String.valueOf((!repo.config().isSupportedMirroring()) ? 0 : repo.config().getMirrors().length));
        }
        printMap(out, fancy, prefix, props);
        if (repo.config().isSupportedMirroring()) {
            for (NutsRepository mirror : repo.config().getMirrors()) {
                out.append("\n");
                printRepo(out, fancy, prefix, mirror);
            }
        }
    }

    private void printMap(PrintWriter out, boolean fancy, String prefix, Map<String, String> props) {
        int len = 23;
        for (String extraKey : props.keySet()) {
            int x = ws.parser().escapeText(extraKey).length();
            if (x > len) {
                len = x;
            }
        }
        boolean first = true;
        for (Map.Entry<String, String> e : props.entrySet()) {
            if (first) {
                first = false;
            } else {
                out.print("\n");
            }
            printKeyValue(out, fancy, prefix, len, e.getKey(), e.getValue());
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
            String space = prefix + StringUtils.formatLeft("", len + 3) + "[[%s]]";
            String[] split = value.split(fancySep);
            if (split.length == 0) {
                out.print(prefix + StringUtils.formatLeft(key, len - key.length() + ws.parser().escapeText(key).length()) + " : ");
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
            out.printf(prefix + StringUtils.formatLeft(key, len - key.length() + ws.parser().escapeText(key).length()) + " : [[%s]]", value);
        }
    }
}
