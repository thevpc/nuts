package net.vpc.app.nuts.core;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsWorkspaceConfigManager;
import net.vpc.app.nuts.NutsWorkspaceVersionFormat;

import java.util.*;
import net.vpc.app.nuts.NutsTerminal;
import net.vpc.app.nuts.core.util.CoreStringUtils;
import net.vpc.app.nuts.core.util.bundledlibs.io.ByteArrayPrintStream;

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
                if (!CoreStringUtils.isBlank(o1)) {
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
        ByteArrayPrintStream out = new ByteArrayPrintStream();
        format(out);
        return out.toString();
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
    public void format(PrintStream out) {
        PrintWriter p = new PrintWriter(out);
        format(p);
        p.flush();
    }

    @Override
    public void format(Writer w) {
        PrintWriter out = (w instanceof PrintWriter) ? ((PrintWriter) w) : new PrintWriter(w);
        NutsWorkspaceConfigManager configManager = ws.config();
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
            props.put("os-version", ws.config().getPlatformOs().getVersion().toString());
            for (String extraKey : extraKeys) {
                props.put(extraKey, extraProperties.getProperty(extraKey));
            }
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
                String key = e.getKey();
                String value = e.getValue();
                out.printf(CoreStringUtils.alignLeft(key, len - key.length() + ws.parser().escapeText(key).length()) + " : [[%s]]", value);
            }
        }
    }

}
