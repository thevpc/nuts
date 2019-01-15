package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;
import net.vpc.common.io.ByteArrayPrintStream;
import net.vpc.common.io.FileUtils;
import net.vpc.common.strings.StringUtils;
import net.vpc.common.util.Chronometer;

import java.io.File;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
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
    public String toString() {
        return format();
    }

    @Override
    public String format() {
        ByteArrayPrintStream out = new ByteArrayPrintStream();
        format(out);
        return out.toString();
    }
    
    @Override
    public void format(PrintStream out) {

        NutsWorkspaceConfigManager configManager = ws.getConfigManager();
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
            int len = 23;
            for (String extraKey : extraKeys) {
                int x = ws.getParseManager().escapeText(extraKey).length();
                if (x > len) {
                    len = x;
                }
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
            props.put("nuts-home", configManager.getHomeLocation());
            props.put("nuts-workspace", configManager.getWorkspaceLocation());
            props.put("nuts-read-only", String.valueOf(configManager.isReadOnly()));
            props.put("nuts-secure", String.valueOf(configManager.isSecure()));
            for (NutsStoreFolder folderType : NutsStoreFolder.values()) {
                props.put("nuts-workspace-" + folderType.name().toLowerCase(), configManager.getStoreLocation(folderType));
            }
            props.put("java-version", System.getProperty("java.version"));
            props.put("java-executable", System.getProperty("java.home") + FileUtils.getNativePath("/bin/java"));
            props.put("java-classpath", System.getProperty("java.class.path"));
            props.put("java-library-path", System.getProperty("java.library.path"));
            props.put("os-name", ws.getConfigManager().getPlatformOs().toString());
            if(ws.getConfigManager().getPlatformOsDist()!=null) {
                props.put("os-dist", ws.getConfigManager().getPlatformOsDist().toString());
            }
            props.put("os-arch", ws.getConfigManager().getPlatformArch().toString());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            props.put("creation-started", dateFormat.format(ws.getConfigManager().getCreationStartTimeMillis()));
            props.put("creation-finished", dateFormat.format(ws.getConfigManager().getCreationFinishTimeMillis()));
            props.put("creation-within", Chronometer.formatPeriodMilli(ws.getConfigManager().getCreationTimeMillis()).trim());
            for (String extraKey : extraKeys) {
                props.put(extraKey, extraProperties.getProperty(extraKey));
            }
            for (String extraKey : props.keySet()) {
                int x = ws.getParseManager().escapeText(extraKey).length();
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
                boolean requireFancy = false;
                String fancySep = ":";
                String key = e.getKey();
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
                String value = e.getValue();
                printKeyValue(out, fancy && requireFancy, len, fancySep, key, value);
            }
        }
    }

    private void printKeyValue(PrintStream out, boolean fancy, int len, String fancySep, String key, String value) {
        if (fancy) {
            String space = StringUtils.formatLeft("", len + 3) + "[[%s]]";
            String[] split = value.split(fancySep);
            if (split.length == 0) {
                out.print(StringUtils.formatLeft(key, len - key.length() + ws.getParseManager().escapeText(key).length()) + " : ");
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
            out.printf(StringUtils.formatLeft(key, len - key.length() + ws.getParseManager().escapeText(key).length()) + " : [[%s]]", value);
        }
    }
}

