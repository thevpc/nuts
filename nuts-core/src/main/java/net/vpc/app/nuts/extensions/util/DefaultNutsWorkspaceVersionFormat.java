package net.vpc.app.nuts.extensions.util;

import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsWorkspaceConfigManager;
import net.vpc.app.nuts.NutsWorkspaceVersionFormat;
import net.vpc.common.io.FileUtils;
import net.vpc.common.strings.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

public class DefaultNutsWorkspaceVersionFormat implements NutsWorkspaceVersionFormat {
    private NutsWorkspace ws;
    private Set<String> options = new HashSet<>();
    private Properties extraProperties = new Properties();

    public DefaultNutsWorkspaceVersionFormat(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsWorkspaceVersionFormat addProperty(String key, String value) {
        extraProperties.setProperty(key, value);
        return this;
    }

    @Override
    public NutsWorkspaceVersionFormat addProperties(Properties p) {
        if (p != null) {
            extraProperties.putAll(p);
        }
        return this;
    }

    @Override
    public NutsWorkspaceVersionFormat addOption(String o) {
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
    public String toString() {
        return format();
    }

    @Override
    public String format() {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(b);
        NutsWorkspaceConfigManager configManager = ws.getConfigManager();
        if (options.contains("min")) {
            out.printf("%s", configManager.getBootAPI().getVersion());
        } else {
            boolean fancy = false;
            if (options.contains("fancy")) {
                fancy = true;
            }
            Set<String> extraKeys = new TreeSet<>();
            if (extraProperties != null) {
                extraKeys = new TreeSet(extraProperties.keySet());
            }
            int len = 21;
            for (String extraKey : extraKeys) {
                int x = ws.escapeText(extraKey).length();
                if (x > len) {
                    len = x;
                }
            }
            LinkedHashMap<String, String> props = new LinkedHashMap<>();
            props.put("nuts-version", configManager.getBootAPI().getVersion().toString());
            props.put("nuts-boot-api", configManager.getBootAPI().toString());
            props.put("nuts-boot-runtime", configManager.getBootRuntime().toString());
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

            props.put("nuts-boot-runtime-path", StringUtils.join(":", runtimeClassPath));
            props.put("nuts-home", configManager.getHomeLocation());
            props.put("nuts-workspace", configManager.getWorkspaceLocation());
            props.put("java-version", System.getProperty("java.version"));
            props.put("java-executable", System.getProperty("java.home") + FileUtils.getNativePath("/bin/java"));
            props.put("java.class.path", System.getProperty("java.class.path"));
            props.put("java.library.path", System.getProperty("java.library.path"));
            props.put("os.name", ws.getPlatformOs().toString());
            props.put("os.dist", ws.getPlatformOsDist().toString());
            props.put("os.arch", ws.getPlatformArch().toString());
            for (String extraKey : extraKeys) {
                props.put(extraKey, extraProperties.getProperty(extraKey));
            }
            boolean first = false;
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
                if (key.equals("java.class.path")) {
                    requireFancy = true;
                    fancySep = File.pathSeparator;
                }
                if (key.equals("java.library.path")) {
                    requireFancy = true;
                    fancySep = File.pathSeparator;
                }
                if (fancy && requireFancy) {
                    out.printf(StringUtils.formatLeft(key, len - key.length() + ws.escapeText(key).length()) + " : ");
                    String space = "\n" + StringUtils.formatLeft("", len + 7) + "[[%s]]";
                    for (String s : e.getValue().split(fancySep)) {
                        out.printf(space, s);
                    }
                } else {
                    out.printf(StringUtils.formatLeft(key, len - key.length() + ws.escapeText(key).length()) + " : [[%s]]", e.getValue());
                }
            }
        }
        out.flush();
        return new String(b.toByteArray());
    }
}
