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
import net.vpc.app.nuts.NutsBootContext;
import net.vpc.app.nuts.NutsBootContextType;
import net.vpc.app.nuts.NutsResultFormatType;
import net.vpc.app.nuts.NutsTerminal;
import net.vpc.app.nuts.NutsUnsupportedArgumentException;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.io.ByteArrayPrintStream;

public class DefaultNutsWorkspaceVersionFormat implements NutsWorkspaceVersionFormat {

    private final NutsWorkspace ws;
    private final Properties extraProperties = new Properties();
    private NutsResultFormatType formatType = null;
    private boolean minimal = false;
    private boolean pretty = true;

    public DefaultNutsWorkspaceVersionFormat(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsWorkspaceVersionFormat parseOptions(String[] args) {
        for (String arg : args) {
            switch (arg) {
                case "--min": {
                    this.setMinimal(true);
                    break;
                }
                case "--json": {
                    this.setFormatType(NutsResultFormatType.JSON);
                    break;
                }
                case "--props": {
                    this.setFormatType(NutsResultFormatType.PROPS);
                    break;
                }
                case "--plain": {
                    this.setFormatType(NutsResultFormatType.PLAIN);
                    break;
                }
                default: {
                    if (arg.startsWith("--add:")) {
                        String kv = arg.substring("--add:".length());
                        int i = kv.indexOf('=');
                        if (i >= 0) {
                            extraProperties.put(kv.substring(0, i), kv.substring(i + 1));
                        } else {
                            extraProperties.put(kv, "");
                        }
                    } else {
                        //ignore!
                    }
                }
            }
        }
        return this;
    }

    @Override
    public boolean isPretty() {
        return pretty;
    }

    @Override
    public NutsWorkspaceVersionFormat setPretty(boolean pretty) {
        this.pretty = pretty;
        return this;
    }

    @Override
    public boolean isMinimal() {
        return minimal;
    }

    @Override
    public NutsWorkspaceVersionFormat setMinimal(boolean minimal) {
        this.minimal = minimal;
        return this;
    }

    @Override
    public NutsResultFormatType getFormatType() {
        return formatType;
    }

    @Override
    public NutsWorkspaceVersionFormat setFormatType(NutsResultFormatType formatType) {
        this.formatType = formatType;
        return this;
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
    public String toString() {
        return format();
    }

    @Override
    public String format() {
        ByteArrayPrintStream out = new ByteArrayPrintStream();
        print(out);
        return out.toString();
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
    public void println(Writer w) {
        print(w);
        try {
            w.write("\n");
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public void print(Writer out) {
        NutsResultFormatType t = formatType;
        if (t == null) {
            t = NutsResultFormatType.PLAIN;
        }
        switch (t) {
            case PLAIN:
                printPlain(out);
                return;
            case PROPS:
                printProps(out);
                return;
            case JSON:
                printJson(out);
                return;
        }
        throw new NutsUnsupportedArgumentException("Unsupported format Type " + t);
    }

    public Map<String, String> buildProps() {
        LinkedHashMap<String, String> props = new LinkedHashMap<>();
        NutsWorkspaceConfigManager configManager = ws.config();
        NutsBootContext rtcontext = configManager.getContext(NutsBootContextType.RUNTIME);
        if (isMinimal()) {
            props.put("nuts-api-version", rtcontext.getApiId().getVersion().toString());
            props.put("nuts-runtime-version", rtcontext.getRuntimeId().getVersion().toString());
            return props;
        }
        Set<String> extraKeys = new TreeSet<>();
        if (extraProperties != null) {
            extraKeys = new TreeSet(extraProperties.keySet());
        }
        props.put("nuts-api-version", rtcontext.getApiId().getVersion().toString());
        props.put("nuts-runtime-version", rtcontext.getRuntimeId().getVersion().toString());
        props.put("java-version", System.getProperty("java.version"));
        props.put("os-version", ws.config().getPlatformOs().getVersion().toString());
        for (String extraKey : extraKeys) {
            props.put(extraKey, extraProperties.getProperty(extraKey));
        }
        return props;
    }

    public void printProps(Writer w) {
        Properties p = new Properties();
        p.putAll(buildProps());
        try {
            p.store(w, null);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void printJson(Writer w) {
        ws.io().writeJson(buildProps(), w, true);
    }

    public void printPlain(Writer w) {
        PrintWriter out = (w instanceof PrintWriter) ? ((PrintWriter) w) : new PrintWriter(w);
        NutsWorkspaceConfigManager configManager = ws.config();
        if (isMinimal()) {
            NutsBootContext rtcontext = configManager.getContext(NutsBootContextType.RUNTIME);
            out.printf("%s/%s", rtcontext.getApiId().getVersion(), rtcontext.getRuntimeId().getVersion());
        } else {
            int len = 23;
            Map<String, String> props = buildProps();
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
