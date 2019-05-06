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
import net.vpc.app.nuts.NutsCommandArg;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsTerminal;
import net.vpc.app.nuts.NutsUnsupportedArgumentException;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.io.ByteArrayPrintStream;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;

/**
 *
 * type: Command Class
 *
 * @author vpc
 */
public class DefaultNutsWorkspaceVersionFormat implements NutsWorkspaceVersionFormat {

    private final NutsWorkspace ws;
    private final Properties extraProperties = new Properties();
    private NutsOutputFormat outputFormat = null;
    private boolean minimal = false;
    private boolean pretty = true;

    public DefaultNutsWorkspaceVersionFormat(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsWorkspaceVersionFormat parseOptions(String[] args) {
        NutsCommandLine cmd = new NutsCommandLine(args);
        NutsCommandArg a;
        while ((a = cmd.next()) != null) {
            switch (a.strKey()) {
                case "--min": {
                    this.setMinimal(a.getBooleanValue());
                    break;
                }
                case "--pretty": {
                    this.setPretty(a.getBooleanValue());
                    break;
                }
                case "--add": {
                    NutsCommandArg r = cmd.getValueFor(a);
                    extraProperties.put(r.getKey().getString(), r.getValue().getString());
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
                default: {
                    throw new NutsIllegalArgumentException("Unsupported argument " + a);
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
    public NutsOutputFormat getOutputFormat() {
        return outputFormat;
    }

    @Override
    public NutsWorkspaceVersionFormat outputFormat(NutsOutputFormat outputFormat) {
        return setOutputFormat(outputFormat);
    }

    @Override
    public NutsWorkspaceVersionFormat setOutputFormat(NutsOutputFormat outputFormat) {
        this.outputFormat = outputFormat;
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
        NutsOutputFormat t = outputFormat;
        if (t == null) {
            t = NutsOutputFormat.PLAIN;
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
        CoreIOUtils.storeProperties(buildProps(), w);
    }

    public void printJson(Writer w) {
        ws.io().json().pretty().write(buildProps(), w);
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
