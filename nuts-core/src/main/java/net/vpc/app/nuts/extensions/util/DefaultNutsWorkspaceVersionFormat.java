package net.vpc.app.nuts.extensions.util;

import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsWorkspaceConfigManager;
import net.vpc.app.nuts.NutsWorkspaceVersionFormat;
import net.vpc.common.io.ByteArrayPrintStream;
import net.vpc.common.strings.StringUtils;

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
    public NutsWorkspaceVersionFormat addOptions(String... o) {
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
        NutsWorkspaceConfigManager configManager = ws.getConfigManager();
        if (options.contains("min")) {
            out.printf("%s/%s", configManager.getRunningContext().getApiId().getVersion(), configManager.getRunningContext().getRuntimeId().getVersion());
        } else {
            Set<String> extraKeys = new TreeSet<>();
            if (extraProperties != null) {
                extraKeys = new TreeSet(extraProperties.keySet());
            }
            int len = 23;
            LinkedHashMap<String, String> props = new LinkedHashMap<>();
            props.put("nuts-boot-api", configManager.getRunningContext().getApiId().toString());
            props.put("nuts-boot-runtime", configManager.getRunningContext().getRuntimeId().toString());
            props.put("java-version", System.getProperty("java.version"));
            props.put("os-version", ws.getConfigManager().getPlatformOs().getVersion().toString());
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
                String key = e.getKey();
                String value = e.getValue();
                out.printf(StringUtils.formatLeft(key, len - key.length() + ws.getParseManager().escapeText(key).length()) + " : [[%s]]", value);
            }
        }
        return out.toString();
    }


}
