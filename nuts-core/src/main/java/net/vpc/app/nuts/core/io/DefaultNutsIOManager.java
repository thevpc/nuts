package net.vpc.app.nuts.core.io;

import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.io.NullInputStream;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.terminals.AbstractSystemTerminalAdapter;
import net.vpc.app.nuts.core.util.io.NullOutputStream;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.core.DefaultNutsHashCommand;
import net.vpc.app.nuts.core.DefaultNutsWorkspaceEvent;

import net.vpc.app.nuts.core.terminals.DefaultNutsSessionTerminal;
import net.vpc.app.nuts.core.app.DefaultNutsApplicationContext;
import net.vpc.app.nuts.core.terminals.DefaultNutsSystemTerminalBase;
import net.vpc.app.nuts.core.terminals.DefaultSystemTerminal;
import net.vpc.app.nuts.core.terminals.UnmodifiableTerminal;
import net.vpc.app.nuts.core.util.common.CorePlatformUtils;

public class DefaultNutsIOManager implements NutsIOManager {

    private static final Logger LOG = Logger.getLogger(DefaultNutsIOManager.class.getName());
    private NutsWorkspace ws;
    private NutsTerminalFormat terminalMetrics = new DefaultNutsTerminalFormat();
    private final Function<String, String> pathExpansionConverter = new Function<String, String>() {
        @Override
        public String apply(String from) {
            switch (from) {
                case "home.config":
                    return ws.config().getHomeLocation(NutsStoreLocation.CONFIG).toString();
                case "home.programs":
                    return ws.config().getHomeLocation(NutsStoreLocation.PROGRAMS).toString();
                case "home.lib":
                    return ws.config().getHomeLocation(NutsStoreLocation.LIB).toString();
                case "home.temp":
                    return ws.config().getHomeLocation(NutsStoreLocation.TEMP).toString();
                case "home.var":
                    return ws.config().getHomeLocation(NutsStoreLocation.VAR).toString();
                case "home.cache":
                    return ws.config().getHomeLocation(NutsStoreLocation.CACHE).toString();
                case "home.log":
                    return ws.config().getHomeLocation(NutsStoreLocation.LOG).toString();
                case "home.run":
                    return ws.config().getHomeLocation(NutsStoreLocation.RUN).toString();
                case "workspace":
                    return ws.config().getContext(NutsBootContextType.RUNTIME).getWorkspace();
                case "user.home":
                    return System.getProperty("user.home");
                case "config":
                    return ws.config().getContext(NutsBootContextType.RUNTIME).getStoreLocation(NutsStoreLocation.CONFIG);
                case "lib":
                    return ws.config().getContext(NutsBootContextType.RUNTIME).getStoreLocation(NutsStoreLocation.LIB);
                case "programs":
                    return ws.config().getContext(NutsBootContextType.RUNTIME).getStoreLocation(NutsStoreLocation.PROGRAMS);
                case "cache":
                    return ws.config().getContext(NutsBootContextType.RUNTIME).getStoreLocation(NutsStoreLocation.CACHE);
                case "run":
                    return ws.config().getContext(NutsBootContextType.RUNTIME).getStoreLocation(NutsStoreLocation.RUN);
                case "temp":
                    return ws.config().getContext(NutsBootContextType.RUNTIME).getStoreLocation(NutsStoreLocation.TEMP);
                case "log":
                    return ws.config().getContext(NutsBootContextType.RUNTIME).getStoreLocation(NutsStoreLocation.LOG);
                case "var":
                    return ws.config().getContext(NutsBootContextType.RUNTIME).getStoreLocation(NutsStoreLocation.VAR);
                case "nuts.boot.version":
                    return ws.config().getContext(NutsBootContextType.RUNTIME).getApiId().getVersion().toString();
                case "nuts.boot.id":
                    return ws.config().getContext(NutsBootContextType.RUNTIME).getApiId().toString();
                case "nuts.workspace-boot.version":
                    return ws.config().getContext(NutsBootContextType.BOOT).getApiId().getVersion().toString();
                case "nuts.workspace-boot.id":
                    return ws.config().getContext(NutsBootContextType.BOOT).getApiId().toString();
                case "nuts.workspace-runtime.version":
                    return ws.config().getContext(NutsBootContextType.BOOT).getRuntimeId().getVersion().toString();
                case "nuts.workspace-runtime.id":
                    return ws.config().getContext(NutsBootContextType.BOOT).getRuntimeId().toString();
                case "nuts.workspace-location":
                    return ws.config().getContext(NutsBootContextType.RUNTIME).getWorkspace();
            }
            String v = System.getProperty(from);
            if (v != null) {
                return v;
            }
            return "${" + from + "}";
        }
    };
    private NutsSessionTerminal terminal;
    private NutsSystemTerminal systemTerminal;

    public DefaultNutsIOManager(NutsWorkspace workspace) {
        this.ws = workspace;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public NutsMonitorCommand monitor() {
        return new DefaultNutsMonitorCommand(ws);
    }

    @Override
    public String expandPath(Path path) {
        return expandPath(path.toString());
    }

    @Override
    public String expandPath(String path) {
        return expandPath(path, ws.config().getWorkspaceLocation().toString());
    }

    @Override
    public String expandPath(String path, String baseFolder) {
        if (path != null && path.length() > 0) {
            path = CoreStringUtils.replaceDollarPlaceHolders(path, pathExpansionConverter);
            if (CoreIOUtils.isURL(path)) {
                return path;
            }
            Path ppath = path(path);
//            if (path.startsWith("file:") || path.startsWith("http://") || path.startsWith("https://")) {
//                return path;
//            }
            if (path.startsWith("~")) {
                if (path.equals("~~")) {
                    Path nutsHome = ws.config().getHomeLocation(NutsStoreLocation.CONFIG);
                    return nutsHome.normalize().toString();
                } else if (path.startsWith("~~") && path.length() > 2 && (path.charAt(2) == '/' || path.charAt(2) == '\\')) {
                    Path nutsHome = ws.config().getHomeLocation(NutsStoreLocation.CONFIG);
                    return nutsHome.resolve(path.substring(3)).normalize().toString();
                } else if (path.equals("~")) {
                    return (System.getProperty("user.home"));
                } else if (path.startsWith("~") && path.length() > 1 && (path.charAt(1) == '/' || path.charAt(1) == '\\')) {
                    return System.getProperty("user.home") + File.separator + path.substring(2);
                } else if (baseFolder != null) {
                    if (CoreIOUtils.isURL(baseFolder)) {
                        return baseFolder + "/" + path;
                    }
                    return path(baseFolder).resolve(path).toAbsolutePath().normalize().toString();
                } else {
                    if (CoreIOUtils.isURL(path)) {
                        return path;
                    }
                    return ppath.toAbsolutePath().normalize().toString();
                }
            } else if (ppath.isAbsolute()) {
                return ppath.normalize().toString();
            } else if (baseFolder != null) {
                if (CoreIOUtils.isURL(baseFolder)) {
                    return baseFolder + "/" + path;
                }
                return path(baseFolder).resolve(path).toAbsolutePath().normalize().toString();
            } else {
                return ppath.toAbsolutePath().normalize().toString();
            }
        }
        if (CoreIOUtils.isURL(baseFolder)) {
            return baseFolder;
        }
        return path(baseFolder).toAbsolutePath().normalize().toString();
    }

    @Override
    public String getResourceString(String resource, Class cls, String defaultValue) {
        String help = null;
        try {
            InputStream s = null;
            try {
                s = cls.getResourceAsStream(resource);
                if (s != null) {
                    help = CoreIOUtils.loadString(s, true);
                }
            } finally {
                if (s != null) {
                    s.close();
                }
            }
        } catch (IOException e) {
            Logger.getLogger(Nuts.class.getName()).log(Level.SEVERE, "Unable to load text from " + resource, e);
        }
        if (help == null) {
            help = defaultValue;//"no help found";
        }
        help = CoreStringUtils.replaceDollarPlaceHolders(help, pathExpansionConverter);
        return help;
    }

    @Override
    public InputStream nullInputStream() {
        return NullInputStream.INSTANCE;
    }

    @Override
    public PrintStream nullPrintStream() {
        return createPrintStream(NullOutputStream.INSTANCE, NutsTerminalMode.INHERITED);
    }

    @Override
    public PrintStream createPrintStream(Path out) {
        if (out == null) {
            return null;
        }
        try {
            return new PrintStream(Files.newOutputStream(out));
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public PrintStream createPrintStream(File out) {
        if (out == null) {
            return null;
        }
        try {
            return new PrintStream(out);
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public PrintStream createPrintStream(OutputStream out, NutsTerminalMode mode) {
        if (mode == null) {
            mode = NutsTerminalMode.INHERITED;
        }
        if (mode == NutsTerminalMode.FORMATTED) {
            if (ws.config().options().getTerminalMode() == NutsTerminalMode.FILTERED) {
                //if nuts started with --no-color modifier, will disable FORMATTED terminal mode
                mode = NutsTerminalMode.FILTERED;
            }
        }
        switch (mode) {
            case FORMATTED: {
                if (out instanceof NutsFormattedPrintStream) {
                    return ((PrintStream) out);
                }
                if (out instanceof NutsFormatFilteredPrintStream) {
                    return createPrintStream(((NutsFormatFilteredPrintStream) out).getUnformattedInstance(), mode);
                }
                //return new NutsDefaultFormattedPrintStream(out);
                HashMap<String, Object> m = new HashMap<>();
                m.put("workspace", this);
                m.put("out", out);
                return (PrintStream) ws.extensions().createSupported(NutsFormattedPrintStream.class,
                        m,
                        new Class[]{OutputStream.class}, new Object[]{out});
            }
            case FILTERED: {
                if (out instanceof NutsFormatFilteredPrintStream) {
                    return ((PrintStream) out);
                }
                if (out instanceof NutsFormattedPrintStream) {
                    return createPrintStream(((NutsFormattedPrintStream) out).getUnformattedInstance(), mode);
                }
                //return new NutsDefaultFormattedPrintStream(out);
                HashMap<String, Object> m = new HashMap<>();
                m.put("workspace", this);
                m.put("out", out);
                return (PrintStream) ws.extensions().createSupported(NutsFormatFilteredPrintStream.class,
                        m,
                        new Class[]{OutputStream.class}, new Object[]{out});
            }
            case INHERITED: {
                if (out instanceof PrintStream) {
                    return (PrintStream) out;
                }
                return new PrintStream(out);
            }
        }
        throw new NutsUnsupportedArgumentException(ws, "Unsupported NutsTerminalMode " + mode);
    }

    @Override
    public NutsSessionTerminal createTerminal() {
        return createTerminal(null);
    }

    @Override
    public NutsSessionTerminal createTerminal(NutsTerminalBase parent) {
        if (parent == null) {
            parent = new AbstractSystemTerminalAdapter() {
                @Override
                public NutsSystemTerminalBase getParent() {
                    return ws.io().getSystemTerminal();
                }
            };
        }
        NutsSessionTerminalBase termb = ws.extensions().createSupported(NutsSessionTerminalBase.class, null);
        if (termb == null) {
            throw new NutsExtensionMissingException(ws, NutsSessionTerminal.class, "Terminal");
        }
        try {
            NutsSessionTerminal term = null;
            if (termb instanceof NutsSessionTerminal) {
                term = (NutsSessionTerminal) termb;
                term.install(ws);
                term.setParent(parent);
            } else {
                term = new DefaultNutsSessionTerminal();
                term.install(ws);
                term.setParent(termb);
            }
            return term;
        } catch (Exception anyException) {
            final NutsSessionTerminal c = new DefaultNutsSessionTerminal();
            c.install(ws);
            c.setParent(parent);
            return c;
        }
    }

    @Override
    public Path createTempFile(String name) {
        return createTempFile(name, null);
    }

    @Override
    public Path createTempFolder(String name) {
        return createTempFolder(name, null);
    }

    @Override
    public Path createTempFolder(String name, NutsRepository repository) {
        File folder = null;
        if (repository == null) {
            folder = ws.config().getStoreLocation(NutsStoreLocation.TEMP).toFile();
        } else {
            folder = repository.config().getStoreLocation(NutsStoreLocation.TEMP).toFile();
        }
        final File temp;
        if (CoreStringUtils.isBlank(name)) {
            name = "temp-";
        } else if (name.length() < 3) {
            name += "-temp-";
        }
        try {
            temp = File.createTempFile(name, Long.toString(System.nanoTime()), folder);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        if (!(temp.delete())) {
            throw new UncheckedIOException(new IOException("Could not delete temp file: " + temp.getAbsolutePath()));
        }

        if (!(temp.mkdir())) {
            throw new UncheckedIOException(new IOException("Could not create temp directory: " + temp.getAbsolutePath()));
        }

        return (temp.toPath());
    }

    @Override
    public Path createTempFile(String name, NutsRepository repository) {
        File folder = null;
        if (repository == null) {
            folder = ws.config().getStoreLocation(NutsStoreLocation.TEMP).toFile();
        } else {
            folder = repository.config().getStoreLocation(NutsStoreLocation.TEMP).toFile();
        }
        folder.mkdirs();
        String prefix = "temp-";
        String ext = null;
        if (!CoreStringUtils.isBlank(name)) {
            ext = CoreIOUtils.getFileExtension(name);
            prefix = name;
            if (prefix.length() < 3) {
                prefix = prefix + "-temp-";
            }
            if (!ext.isEmpty()) {
                ext = "." + ext;
                if (ext.length() < 3) {
                    ext = ".tmp" + ext;
                }
            } else {
                ext = "-nuts";
            }
        }
        try {
            return File.createTempFile(prefix, "-nuts" + (ext != null ? ("." + ext) : ""), folder).toPath();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

    @Override
    public NutsHashCommand hash() {
        return new DefaultNutsHashCommand(ws);
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }

    @Override
    public NutsPathCopyAction copy() {
        return new DefaultNutsIOCopyAction(this);
    }

    @Override
    public Path path(String first, String... more) {
        if ((first == null || first.isEmpty()) && more.length == 0) {
            return null;
        }
        return Paths.get(first, more);
    }

    @Override
    public NutsApplicationContext createApplicationContext(String[] args, Class appClass, String storeId, long startTimeMillis) {
        return new DefaultNutsApplicationContext(ws, args, appClass, storeId, startTimeMillis);
    }

    @Override
    public NutsTerminalFormat getTerminalFormat() {
        return terminalMetrics;
    }

    @Override
    public NutsIOManager setSystemTerminal(NutsSystemTerminalBase term) {
        if (term == null) {
            throw new NutsExtensionMissingException(getWorkspace(), NutsSystemTerminalBase.class, "SystemTerminalBase");
        }
        NutsSystemTerminal syst;
        if ((term instanceof NutsSystemTerminal)) {
            syst = (NutsSystemTerminal) term;
        } else {
            try {
                syst = new DefaultSystemTerminal(term);
                syst.install(getWorkspace());
            } catch (Exception ex) {
                syst = new DefaultSystemTerminal(new DefaultNutsSystemTerminalBase());
                syst.install(getWorkspace());

            }
        }
        if (this.systemTerminal != null) {
            this.systemTerminal.uninstall();
        }
        NutsSystemTerminal old = this.systemTerminal;
        this.systemTerminal = syst;

        if (old != this.systemTerminal) {
            NutsWorkspaceEvent event = null;
            for (NutsWorkspaceListener workspaceListener : getWorkspace().getWorkspaceListeners()) {
                if (event == null) {
                    event = new DefaultNutsWorkspaceEvent(ws.createSession(), null, "systemTerminal", old, this.systemTerminal);
                }
                workspaceListener.onUpdateProperty(event);
            }
        }
        return this;
    }

    @Override
    public NutsSystemTerminal getSystemTerminal() {
        return systemTerminal;
    }

    @Override
    public NutsSessionTerminal getTerminal() {
        return terminal;
    }

    @Override
    public NutsIOManager setTerminal(NutsSessionTerminal terminal) {
        if (terminal == null) {
            terminal = createTerminal();
        }
        if (!(terminal instanceof UnmodifiableTerminal)) {
            terminal = new UnmodifiableTerminal(terminal);
        }
        this.terminal = terminal;
        return this;
    }

    @Override
    public NutsTerminalFormat terminalFormat() {
        return getTerminalFormat();
    }

    @Override
    public NutsSystemTerminal systemTerminal() {
        return getSystemTerminal();
    }

    @Override
    public NutsSessionTerminal terminal() {
        return getTerminal();
    }

    @Override
    public NutsExecutionEntry[] parseExecutionEntries(File file) {
        return parseExecutionEntries(file.toPath());
    }

    @Override
    public NutsExecutionEntry[] parseExecutionEntries(Path file) {
        if (file.getFileName().toString().toLowerCase().endsWith(".jar")) {
            try {
                try (InputStream in = Files.newInputStream(file)) {
                    return parseExecutionEntries(in, "java", file.toAbsolutePath().normalize().toString());
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        } else if (file.getFileName().toString().toLowerCase().endsWith(".class")) {
            try {
                try (InputStream in = Files.newInputStream(file)) {
                    return parseExecutionEntries(in, "class", file.toAbsolutePath().normalize().toString());
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        } else {
            return new NutsExecutionEntry[0];
        }
    }

    @Override
    public NutsExecutionEntry[] parseExecutionEntries(InputStream inputStream, String type, String sourceName) {
        if ("java".equals(type)) {
            return CorePlatformUtils.parseJarExecutionEntries(inputStream, sourceName);
        } else if ("class".equals(type)) {
            NutsExecutionEntry u = CorePlatformUtils.parseClassExecutionEntry(inputStream, sourceName);
            return u == null ? new NutsExecutionEntry[0] : new NutsExecutionEntry[]{u};
        }
        return new NutsExecutionEntry[0];
    }
}
