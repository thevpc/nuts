package net.vpc.app.nuts.runtime.io;

import net.vpc.app.nuts.core.io.NutsFormattedPrintStream;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;
import net.vpc.app.nuts.runtime.util.io.NullInputStream;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.terminals.AbstractSystemTerminalAdapter;
import net.vpc.app.nuts.runtime.util.io.NullOutputStream;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringTokenizer;
import java.util.function.Function;

import net.vpc.app.nuts.runtime.DefaultNutsSupportLevelContext;
import net.vpc.app.nuts.runtime.DefaultNutsWorkspaceEvent;

import net.vpc.app.nuts.runtime.terminals.DefaultNutsSessionTerminal;
import net.vpc.app.nuts.runtime.app.DefaultNutsApplicationContext;
import net.vpc.app.nuts.runtime.terminals.DefaultNutsSystemTerminalBase;
import net.vpc.app.nuts.runtime.terminals.DefaultSystemTerminal;
import net.vpc.app.nuts.runtime.terminals.UnmodifiableTerminal;
import net.vpc.app.nuts.NutsUnsupportedEnumException;
import net.vpc.app.nuts.core.io.NutsPrintStreamExt;

public class DefaultNutsIOManager implements NutsIOManager {

    //    private final NutsLogger LOG;
    private NutsWorkspace ws;
    private NutsTerminalFormat terminalMetrics = new DefaultNutsTerminalFormat();
    private final Function<String, String> pathExpansionConverter = new Function<String, String>() {
        @Override
        public String apply(String from) {
            switch (from) {
                case "home.config":
                    return ws.config().getHomeLocation(NutsStoreLocation.CONFIG).toString();
                case "home.apps":
                    return ws.config().getHomeLocation(NutsStoreLocation.APPS).toString();
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
                    return ws.config().getWorkspaceLocation().toString();
                case "user.home":
                    return System.getProperty("user.home");
                case "config":
                    return ws.config().getStoreLocation(NutsStoreLocation.CONFIG).toString();
                case "lib":
                    return ws.config().getStoreLocation(NutsStoreLocation.LIB).toString();
                case "apps":
                    return ws.config().getStoreLocation(NutsStoreLocation.APPS).toString();
                case "cache":
                    return ws.config().getStoreLocation(NutsStoreLocation.CACHE).toString();
                case "run":
                    return ws.config().getStoreLocation(NutsStoreLocation.RUN).toString();
                case "temp":
                    return ws.config().getStoreLocation(NutsStoreLocation.TEMP).toString();
                case "log":
                    return ws.config().getStoreLocation(NutsStoreLocation.LOG).toString();
                case "var":
                    return ws.config().getStoreLocation(NutsStoreLocation.VAR).toString();
                case "nuts.boot.version":
                    return ws.config().getApiVersion();
                case "nuts.boot.id":
                    return ws.config().getApiId().toString();
                case "nuts.workspace-boot.version":
                    return Nuts.getVersion();
                case "nuts.workspace-boot.id":
                    return NutsConstants.Ids.NUTS_API + "#" + Nuts.getVersion();
                case "nuts.workspace-runtime.version": {
                    String rt = ws.config().getOptions().getRuntimeId();
                    return rt == null ? ws.config().getRuntimeId().getVersion().toString() : rt.contains("#")
                            ? rt.substring(rt.indexOf("#") + 1)
                            : rt;
                }
                case "nuts.workspace-runtime.id": {
                    String rt = ws.config().getOptions().getRuntimeId();
                    return rt == null ? ws.config().getRuntimeId().getVersion().toString() : rt.contains("#")
                            ? rt
                            : (NutsConstants.Ids.NUTS_RUNTIME + "#" + rt);
                }
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
    private WorkspaceSystemTerminalAdapter workspaceSystemTerminalAdapter;

    public DefaultNutsIOManager(NutsWorkspace workspace) {
        this.ws = workspace;
//        LOG = ws.log().of(DefaultNutsIOManager.class);
        workspaceSystemTerminalAdapter = new WorkspaceSystemTerminalAdapter(ws);
        NutsWorkspaceUtils.of(ws).setWorkspace(terminalMetrics);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<Object> criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public NutsMonitorAction monitor() {
        return new DefaultNutsMonitorAction(ws);
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
            Path ppath = Paths.get(path);
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
                    return Paths.get(baseFolder).resolve(path).toAbsolutePath().normalize().toString();
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
                return Paths.get(baseFolder).resolve(path).toAbsolutePath().normalize().toString();
            } else {
                return ppath.toAbsolutePath().normalize().toString();
            }
        }
        if (CoreIOUtils.isURL(baseFolder)) {
            return baseFolder;
        }
        return Paths.get(baseFolder).toAbsolutePath().normalize().toString();
    }

    @Override
    public String loadFormattedString(String resourcePath, ClassLoader classLoader, String defaultValue) {
        return loadHelp(resourcePath, classLoader, false, true, defaultValue);
    }

    @Override
    public String loadFormattedString(Reader is, ClassLoader classLoader) {
        return loadHelp(is, classLoader, true, 36, true);
    }

    private String loadHelp(String urlPath, ClassLoader clazz, boolean err, boolean vars, String defaultValue) {
        return loadHelp(urlPath, clazz, err, 36, vars, defaultValue);
    }

    private String loadHelp(String urlPath, ClassLoader classLoader, boolean err, int depth, boolean vars, String defaultValue) {
        if (depth <= 0) {
            throw new IllegalArgumentException("Unable to load " + urlPath + ". Too many recursions");
        }
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        if (urlPath.startsWith("/")) {
            urlPath = urlPath.substring(1);
        }
        URL resource = classLoader.getResource(urlPath);
        if (resource == null) {
            if (err) {
                return "@@Not Found resource " + terminalFormat().escapeText(urlPath) + "@@";
            }
            if (defaultValue == null) {
                return null;
            }
            try (Reader is = new StringReader(defaultValue)) {
                return loadHelp(is, classLoader, err, depth, vars);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        try (Reader is = new InputStreamReader(resource.openStream())) {
            return loadHelp(is, classLoader, true, depth, vars);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private String loadHelp(Reader is, ClassLoader classLoader, boolean err, int depth, boolean vars) {
        return processHelp(CoreIOUtils.loadString(is, true), classLoader, err, depth, vars);
    }

    private String processHelp(String s, ClassLoader classLoader, boolean err, int depth, boolean vars) {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        StringBuilder sb = new StringBuilder();
        if (s != null) {
            StringTokenizer st = new StringTokenizer(s, "\n\r", true);
            while (st.hasMoreElements()) {
                String e = st.nextToken();
                if (e.length() > 0) {
                    if (e.charAt(0) == '\n' || e.charAt(0) == '\r') {
                        sb.append(e);
                    } else if (e.startsWith("#!include<") && e.trim().endsWith(">")) {
                        e = e.trim();
                        e = e.substring("#!include<".length(), e.length() - 1);
                        sb.append(loadHelp(e, classLoader, err, depth - 1, false, "@@NOT FOUND\\<" + terminalFormat().escapeText(e) + "\\>@@"));
                    } else {
                        sb.append(e);
                    }
                }
            }
        }
        String help = sb.toString();
        if (vars) {
            help = CoreStringUtils.replaceDollarPlaceHolders(help, pathExpansionConverter);
        }
        return help;
    }

    @Override
    public InputStream nullInputStream() {
        return NullInputStream.INSTANCE;
    }

    @Override
    public PrintStream nullPrintStream() {
        return createPrintStream(NullOutputStream.INSTANCE, NutsTerminalMode.FILTERED);
    }

//    @Override
//    public PrintStream createPrintStream(Path out) {
//        if (out == null) {
//            return null;
//        }
//        try {
//            return new PrintStream(Files.newOutputStream(out));
//        } catch (IOException ex) {
//            throw new IllegalArgumentException(ex);
//        }
//    }
//
//    @Override
//    public PrintStream createPrintStream(File out) {
//        if (out == null) {
//            return null;
//        }
//        try {
//            return new PrintStream(out);
//        } catch (IOException ex) {
//            throw new IllegalArgumentException(ex);
//        }
//    }

    @Override
    public PrintStream createPrintStream(OutputStream out, NutsTerminalMode mode) {
        if (mode == null) {
            mode = NutsTerminalMode.INHERITED;
        }
        if (mode == NutsTerminalMode.FORMATTED) {
            if (ws.config().options().getTerminalMode() == NutsTerminalMode.FILTERED) {
                //if nuts started with --no-color modifier, will disable FORMATTED terminal mode each time
                mode = NutsTerminalMode.FILTERED;
            }
        }

        if (out == null) {
            return null;
        }
        if (out instanceof NutsPrintStreamExt) {
            NutsPrintStreamExt a = (NutsPrintStreamExt) out;
            NutsTerminalMode am = a.getMode();
            switch (mode) {
                case FORMATTED: {
                    switch (am) {
                        case FORMATTED: {
                            return (PrintStream) a;
                        }
                        case FILTERED: {
                            return a.basePrintStream();
                        }
                        case INHERITED: {
                            PrintStream supported = (PrintStream) ws.extensions().createSupported(
                                    NutsFormattedPrintStream.class,
                                    new DefaultNutsSupportLevelContext<>(ws, out),
                                    new Class[]{OutputStream.class}, new Object[]{out});
                            if (supported == null) {
                                throw new NutsExtensionNotFoundException(ws, NutsFormattedPrintStream.class, "FormattedPrintStream");
                            }
                            NutsWorkspaceUtils.of(ws).setWorkspace(supported);
                            return supported;
                        }
                        default: {
                            throw new NutsUnsupportedEnumException(ws, am);
                        }
                    }
                }
                case FILTERED: {
                    switch (am) {
                        case FORMATTED: {
                            return a.basePrintStream();
                        }
                        case FILTERED: {
                            return (PrintStream) a;
                        }
                        case INHERITED: {
                            return (PrintStream) a;
                        }
                        default: {
                            throw new NutsUnsupportedEnumException(ws, am);
                        }
                    }
                }
                default: {
                    throw new NutsUnsupportedEnumException(ws, mode);
                }
            }
        } else if (out instanceof NutsFormattedPrintStream) {
            NutsFormattedPrintStream a = (NutsFormattedPrintStream) out;
            switch (mode) {
                case FORMATTED: {
                    return (PrintStream) a;
                }
                case FILTERED: {
                    return a.getUnformattedInstance();
                }
                default: {
                    throw new NutsUnsupportedEnumException(ws, mode);
                }
            }
        } else {
            switch (mode) {
                case FORMATTED: {
                    PrintStream supported = (PrintStream) ws.extensions().createSupported(
                            NutsFormattedPrintStream.class,
                            new DefaultNutsSupportLevelContext<>(ws, out),
                            new Class[]{OutputStream.class}, new Object[]{out});
                    if (supported == null) {
                        throw new NutsExtensionNotFoundException(ws, NutsFormattedPrintStream.class, "FormattedPrintStream");
                    }
                    NutsWorkspaceUtils.of(ws).setWorkspace(supported);
                    return supported;
                }
                case FILTERED: {
                    return (PrintStream) out;
                }
                default: {
                    throw new NutsUnsupportedEnumException(ws, mode);
                }
            }
        }
    }

    @Override
    public NutsSessionTerminal createTerminal() {
        return createTerminal(null);
    }

    @Override
    public NutsSessionTerminal createTerminal(NutsTerminalBase parent) {
        if (parent == null) {
            parent = workspaceSystemTerminalAdapter;
        }
        NutsSessionTerminalBase termb = ws.extensions().createSupported(NutsSessionTerminalBase.class, null);
        if (termb == null) {
            throw new NutsExtensionNotFoundException(ws, NutsSessionTerminal.class, "SessionTerminalBase");
        }
        NutsWorkspaceUtils.of(ws).setWorkspace(termb);
        try {
            NutsSessionTerminal term = null;
            if (termb instanceof NutsSessionTerminal) {
                term = (NutsSessionTerminal) termb;
                NutsWorkspaceUtils.of(ws).setWorkspace(term);
                term.setParent(parent);
            } else {
                term = new DefaultNutsSessionTerminal();
                NutsWorkspaceUtils.of(ws).setWorkspace(term);
                term.setParent(termb);
            }
            return term;
        } catch (Exception anyException) {
            final NutsSessionTerminal c = new DefaultNutsSessionTerminal();
            NutsWorkspaceUtils.of(ws).setWorkspace(c);
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
        folder.mkdirs();
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
    public NutsIOHashAction hash() {
        return new DefaultNutsIOHashAction(ws);
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }

    @Override
    public NutsIOCopyAction copy() {
        return new DefaultNutsIOCopyAction(this);
    }

    @Override
    public NutsIOProcessAction ps() {
        return new DefaultNutsIOProcessAction(ws);
    }

    @Override
    public NutsIODeleteAction delete() {
        return new DefaultNutsIODeleteAction(ws);
    }

    @Override
    public NutsIOCompressAction compress() {
        return new DefaultNutsIOCompressAction(this);
    }

    @Override
    public NutsIOUncompressAction uncompress() {
        return new DefaultNutsIOUncompressAction(this);
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
    public NutsIOManager setSystemTerminal(NutsSystemTerminalBase terminal) {
        //TODO : should pass session in method
        return setSystemTerminal(terminal,null);
    }
    public NutsIOManager setSystemTerminal(NutsSystemTerminalBase terminal,NutsSession session) {
        if (terminal == null) {
            throw new NutsExtensionNotFoundException(getWorkspace(), NutsSystemTerminalBase.class, "SystemTerminalBase");
        }
        NutsSystemTerminal syst;
        if ((terminal instanceof NutsSystemTerminal)) {
            syst = (NutsSystemTerminal) terminal;
        } else {
            try {
                syst = new DefaultSystemTerminal(terminal);
                NutsWorkspaceUtils.of(ws).setWorkspace(syst);
            } catch (Exception ex) {
                syst = new DefaultSystemTerminal(new DefaultNutsSystemTerminalBase());
                NutsWorkspaceUtils.of(ws).setWorkspace(syst);

            }
        }
        if (this.systemTerminal != null) {
            NutsWorkspaceUtils.unsetWorkspace(this.systemTerminal);
        }
        NutsSystemTerminal old = this.systemTerminal;
        this.systemTerminal = syst;

        if (old != this.systemTerminal) {
            NutsWorkspaceEvent event = null;
            if(session!=null) {
                for (NutsWorkspaceListener workspaceListener : getWorkspace().getWorkspaceListeners()) {
                    if (event == null) {
                        event = new DefaultNutsWorkspaceEvent(session, null, "systemTerminal", old, this.systemTerminal);
                    }
                    workspaceListener.onUpdateProperty(event);
                }
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
            return NutsWorkspaceUtils.of(ws).parseJarExecutionEntries(inputStream, sourceName);
        } else if ("class".equals(type)) {
            NutsExecutionEntry u = NutsWorkspaceUtils.of(ws).parseClassExecutionEntry(inputStream, sourceName);
            return u == null ? new NutsExecutionEntry[0] : new NutsExecutionEntry[]{u};
        }
        return new NutsExecutionEntry[0];
    }

    private static class WorkspaceSystemTerminalAdapter extends AbstractSystemTerminalAdapter {

        private NutsWorkspace workspace;

        public WorkspaceSystemTerminalAdapter(NutsWorkspace workspace) {
            this.workspace = workspace;
        }

        @Override
        public NutsSystemTerminalBase getParent() {
            return workspace.io().getSystemTerminal();
        }
    }

    public DefaultNutsIOLockAction lock() {
        return new DefaultNutsIOLockAction(ws);
    }

//    @Override
//    public void invokeLocked(Runnable r, Runnable rollback, NutsLock lock) {
//        lock.acquire();
//        try {
//            r.run();
//        } catch (Exception ex) {
//            rollback.run();
//        }
//        if (Files.exists(lockPath)) {
//            throw new NutsLockBarrierException(ws, lockedObject, lockPath);
//        }
//        try {
//            Files.newOutputStream(lockPath).close();
//        } catch (IOException ex) {
//            throw new NutsLockException(ws, "Unable to acquire lock " + lockedObject, lockPath, ex);
//        }
//    }

}
