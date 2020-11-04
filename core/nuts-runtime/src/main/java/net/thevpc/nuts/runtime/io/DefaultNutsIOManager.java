package net.thevpc.nuts.runtime.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.util.io.CoreIOUtils;
import net.thevpc.nuts.runtime.util.io.NullInputStream;
import net.thevpc.nuts.runtime.util.io.NullOutputStream;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringTokenizer;
import java.util.function.Function;

public class DefaultNutsIOManager implements NutsIOManager {

    //    private final NutsLogger LOG;
    private NutsWorkspace ws;
    private NutsTerminalManager term;
    private DefaultTempManager tmp;
    private final Function<String, String> pathExpansionConverter = new Function<String, String>() {
        @Override
        public String apply(String from) {
            switch (from) {
                case "home.config":
                    return ws.locations().getHomeLocation(NutsStoreLocation.CONFIG).toString();
                case "home.apps":
                    return ws.locations().getHomeLocation(NutsStoreLocation.APPS).toString();
                case "home.lib":
                    return ws.locations().getHomeLocation(NutsStoreLocation.LIB).toString();
                case "home.temp":
                    return ws.locations().getHomeLocation(NutsStoreLocation.TEMP).toString();
                case "home.var":
                    return ws.locations().getHomeLocation(NutsStoreLocation.VAR).toString();
                case "home.cache":
                    return ws.locations().getHomeLocation(NutsStoreLocation.CACHE).toString();
                case "home.log":
                    return ws.locations().getHomeLocation(NutsStoreLocation.LOG).toString();
                case "home.run":
                    return ws.locations().getHomeLocation(NutsStoreLocation.RUN).toString();
                case "workspace":
                    return ws.locations().getWorkspaceLocation().toString();
                case "user.home":
                    return System.getProperty("user.home");
                case "config":
                    return ws.locations().getStoreLocation(NutsStoreLocation.CONFIG).toString();
                case "lib":
                    return ws.locations().getStoreLocation(NutsStoreLocation.LIB).toString();
                case "apps":
                    return ws.locations().getStoreLocation(NutsStoreLocation.APPS).toString();
                case "cache":
                    return ws.locations().getStoreLocation(NutsStoreLocation.CACHE).toString();
                case "run":
                    return ws.locations().getStoreLocation(NutsStoreLocation.RUN).toString();
                case "temp":
                    return ws.locations().getStoreLocation(NutsStoreLocation.TEMP).toString();
                case "log":
                    return ws.locations().getStoreLocation(NutsStoreLocation.LOG).toString();
                case "var":
                    return ws.locations().getStoreLocation(NutsStoreLocation.VAR).toString();
                case "nuts.boot.version":
                    return ws.getApiVersion();
                case "nuts.boot.id":
                    return ws.getApiId().toString();
                case "nuts.workspace-boot.version":
                    return Nuts.getVersion();
                case "nuts.workspace-boot.id":
                    return NutsConstants.Ids.NUTS_API + "#" + Nuts.getVersion();
                case "nuts.workspace-runtime.version": {
                    String rt = ws.config().getOptions().getRuntimeId();
                    return rt == null ? ws.getRuntimeId().getVersion().toString() : rt.contains("#")
                            ? rt.substring(rt.indexOf("#") + 1)
                            : rt;
                }
                case "nuts.workspace-runtime.id": {
                    String rt = ws.config().getOptions().getRuntimeId();
                    return rt == null ? ws.getRuntimeId().getVersion().toString() : rt.contains("#")
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
    private InputStream bootStdin = null;
    private PrintStream bootStdout = null;
    private PrintStream bootStderr = null;

    private InputStream currentStdin = null;
    private PrintStream currentStdout = null;
    private PrintStream currentStderr = null;

    public DefaultNutsIOManager(NutsWorkspace workspace) {
        this.ws = workspace;
//        LOG = ws.log().of(DefaultNutsIOManager.class);
        term=new DefaultNutsTerminalManager(ws);
        tmp=new DefaultTempManager(ws);
    }

    @Override
    public NutsTempManager tmp() {
        return tmp;
    }


    @Override
    public NutsTerminalManager term() {
        return term;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<Object> criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public String expandPath(String path) {
        return expandPath(path, ws.locations().getWorkspaceLocation().toString());
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
                    Path nutsHome = ws.locations().getHomeLocation(NutsStoreLocation.CONFIG);
                    return nutsHome.normalize().toString();
                } else if (path.startsWith("~~") && path.length() > 2 && (path.charAt(2) == '/' || path.charAt(2) == '\\')) {
                    Path nutsHome = ws.locations().getHomeLocation(NutsStoreLocation.CONFIG);
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
    public String loadFormattedString(Reader is, ClassLoader classLoader) {
        return loadHelp(is, classLoader, true, 36, true);
    }

    @Override
    public String loadFormattedString(String resourcePath, ClassLoader classLoader, String defaultValue) {
        return loadHelp(resourcePath, classLoader, false, true, defaultValue);
    }

    @Override
    public InputStream nullInputStream() {
        return NullInputStream.INSTANCE;
    }

    @Override
    public PrintStream nullPrintStream() {
        return createPrintStream(NullOutputStream.INSTANCE, NutsTerminalMode.FILTERED);
    }

    @Override
    public PrintStream createPrintStream(OutputStream out, NutsTerminalMode expectedMode) {
        if (out == null) {
            return null;
        }
        if (expectedMode == null) {
            expectedMode = ws.config().options().getTerminalMode();
        }
        if (expectedMode == NutsTerminalMode.FORMATTED) {
            if (ws.config().options().getTerminalMode() == NutsTerminalMode.FILTERED) {
                //if nuts started with --no-color modifier, will disable FORMATTED terminal mode each time
                expectedMode = NutsTerminalMode.FILTERED;
            }
        }
        return CoreIOUtils.toPrintStream(CoreIOUtils.convertOutputStream(out, expectedMode, ws), ws);
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
    public NutsIOCopyAction copy() {
        return new DefaultNutsIOCopyAction(this);
    }

    @Override
    public NutsIOProcessAction ps() {
        return new DefaultNutsIOProcessAction(ws);
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
    public NutsIODeleteAction delete() {
        return new DefaultNutsIODeleteAction(ws);
    }

    @Override
    public NutsMonitorAction monitor() {
        return new DefaultNutsMonitorAction(ws);
    }

    @Override
    public NutsIOHashAction hash() {
        return new DefaultNutsIOHashAction(ws);
    }


    @Override
    public NutsInputManager input() {
        return new DefaultNutsInputManager(ws);
    }

    @Override
    public NutsOutputManager output() {
        return new DefaultNutsOutputManager(ws);
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
                return "@@Not Found resource " + term().getTerminalFormat().escapeText(urlPath) + "@@";
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
                        sb.append(loadHelp(e, classLoader, err, depth - 1, false, "@@NOT FOUND\\<" + term().getTerminalFormat().escapeText(e) + "\\>@@"));
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

    public NutsWorkspace getWorkspace() {
        return ws;
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

//    @Override
//    public NutsIOManager executorService(ExecutorService executor) {
//        if (executor == null) {
//            throw new IllegalArgumentException("Unable to set null executor");
//        }
//        return this;
//    }

    public InputStream getBootStdin(boolean nonnull) {
        if (bootStdin != null) {
            return bootStdin;
        }
        return nonnull ? System.in : null;
    }

    public PrintStream getBootStdout(boolean nonnull) {
        if (bootStdout != null) {
            return bootStdout;
        }
        return nonnull ? System.out : null;
    }

    public PrintStream getBootStderr(boolean nonnull) {
        if (bootStderr != null) {
            return bootStderr;
        }
        return nonnull ? System.err : null;
    }

    public InputStream getCurrentStdin() {
        return currentStdin;
    }

    public void setCurrentStdin(InputStream currentStdin) {
        this.currentStdin = currentStdin;
    }

    public PrintStream getCurrentStdout() {
        return currentStdout;
    }

    public void setCurrentStdout(PrintStream currentStdout) {
        this.currentStdout = currentStdout;
    }

    public PrintStream getCurrentStderr() {
        return currentStderr;
    }

    public void setCurrentStderr(PrintStream currentStderr) {
        this.currentStderr = currentStderr;
    }

}
