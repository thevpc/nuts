package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.io.NullInputStream;
import net.thevpc.nuts.runtime.bundles.parsers.StringPlaceHolderParser;
import net.thevpc.nuts.runtime.core.format.text.SimpleWriterOutputStream;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNutsBootModel;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

public class DefaultNutsIOModel {

    public final NutsPrintStream nullOut;
    private final Function<String, String> pathExpansionConverter;
    public DefaultNutsBootModel bootModel;
    //    private final NutsLogger LOG;
    private NutsWorkspace ws;
    private InputStream stdin = null;
    private NutsPrintStream stdout;
    private NutsPrintStream stderr;

    public DefaultNutsIOModel(NutsWorkspace workspace, DefaultNutsBootModel bootModel) {
        this.ws = workspace;
        this.pathExpansionConverter = new NutsWorkspaceVarExpansionFunction(ws);
        this.bootModel = bootModel;
        this.stdout = bootModel.stdout();
        this.stderr = bootModel.stderr();
        this.stdin = bootModel.stdin();
        this.nullOut = new NutsPrintStreamNull(bootModel.bootSession());
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }

    public int getSupportLevel(NutsSupportLevelContext<Object> criteria) {
        return DefaultNutsIOManager.DEFAULT_SUPPORT;
    }

    public String expandPath(String path) {
        return expandPath(path, ws.locations().getWorkspaceLocation().toString());
    }

    public String expandPath(String path, String baseFolder) {
        if (path != null && path.length() > 0) {
            path = StringPlaceHolderParser.replaceDollarPlaceHolders(path, pathExpansionConverter);
            if (CoreIOUtils.isURL(path)) {
                return path;
            }
            Path ppath = Paths.get(path);
//            if (path.startsWith("file:") || path.startsWith("http://") || path.startsWith("https://")) {
//                return path;
//            }
            if (path.startsWith("~")) {
                if (path.equals("~~")) {
                    Path nutsHome = Paths.get(ws.locations().getHomeLocation(NutsStoreLocation.CONFIG));
                    return nutsHome.normalize().toString();
                } else if (path.startsWith("~~") && path.length() > 2 && (path.charAt(2) == '/' || path.charAt(2) == '\\')) {
                    Path nutsHome = Paths.get(ws.locations().getHomeLocation(NutsStoreLocation.CONFIG));
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

    public InputStream nullInputStream() {
        return NullInputStream.INSTANCE;
    }

    public NutsPrintStream nullPrintStream() {
        return nullOut;
        //return createPrintStream(NullOutputStream.INSTANCE, NutsTerminalMode.FILTERED, session);
    }

    public NutsPrintStream createPrintStream(Writer out, NutsTerminalMode expectedMode, NutsSession session) {
        if (out == null) {
            return null;
        }
        if (out instanceof NutsPrintStreamAdapter) {
            return ((NutsPrintStreamAdapter) out).getBaseNutsPrintStream().convert(expectedMode);
        }
        SimpleWriterOutputStream w = new SimpleWriterOutputStream(out, session);
        return createPrintStream(w, expectedMode, session);
    }

    public NutsPrintStream createPrintStream(OutputStream out, NutsTerminalMode expectedMode, NutsSession session) {
        if (out == null) {
            return null;
        }
        NutsWorkspaceOptions woptions = ws.config().setSession(session).options();
        NutsTerminalMode expectedMode0 = woptions.getTerminalMode();
        if (expectedMode0 == null) {
            if (woptions.isBot()) {
                expectedMode0 = NutsTerminalMode.FILTERED;
            } else {
                expectedMode0 = NutsTerminalMode.FORMATTED;
            }
        }
        if (expectedMode == null) {
            expectedMode = expectedMode0;
        }
        if (expectedMode == NutsTerminalMode.FORMATTED) {
            if (expectedMode0 == NutsTerminalMode.FILTERED) {
                //if nuts started with --no-color modifier, will disable FORMATTED terminal mode each time
                expectedMode = NutsTerminalMode.FILTERED;
            }
        }
        if (out instanceof NutsPrintStreamAdapter) {
            return ((NutsPrintStreamAdapter) out).getBaseNutsPrintStream().convert(expectedMode);
        }
        return
                new NutsPrintStreamRaw(out, null, null, session, new NutsPrintStreamBase.Bindings())
                        .convert(expectedMode)
                ;
    }


    public NutsTempAction tmp() {
        return new DefaultTempAction(ws);
    }

    //
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
//    
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
    public NutsIOCopyAction copy() {
        return new DefaultNutsIOCopyAction(ws);
    }

    public NutsIOProcessAction ps() {
        return new DefaultNutsIOProcessAction(ws);
    }

    public NutsIOCompressAction compress() {
        return new DefaultNutsIOCompressAction(ws);
    }

    public NutsIOUncompressAction uncompress() {
        return new DefaultNutsIOUncompressAction(ws);
    }

    public NutsIODeleteAction delete() {
        return new DefaultNutsIODeleteAction(ws);
    }

    public NutsMonitorAction monitor() {
        return new DefaultNutsMonitorAction(ws);
    }

    public NutsIOHashAction hash() {
        return new DefaultNutsIOHashAction(ws);
    }

    public NutsInputAction input() {
        return new DefaultNutsInputAction(ws);
    }

    public NutsOutputAction output() {
        return new DefaultNutsOutputAction(ws);
    }

//    
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
//    
//    public NutsIOManager executorService(ExecutorService executor) {
//        if (executor == null) {
//            throw new IllegalArgumentException("Unable to set null executor");
//        }
//        return this;
//    }

    public InputStream stdin() {
        return stdin == null ? bootModel.stdin() : stdin;
    }

    public void setStdin(InputStream stdin) {
        this.stdin = stdin;
    }

    public NutsPrintStream stdout() {
        return stdout;
    }

    public void setStdout(NutsPrintStream stdout) {
        this.stdout = stdout == null ?

                bootModel.stdout() : stdout;
    }

    public NutsPrintStream stderr() {
        return stderr;
    }

    public void setStderr(NutsPrintStream stderr) {
        this.stderr = stderr == null ? bootModel.stderr() : stderr;
    }

}
