package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.parsers.StringPlaceHolderParser;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.bundles.io.NullInputStream;
import net.thevpc.nuts.runtime.bundles.io.NullOutputStream;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.regex.Pattern;

public class DefaultNutsIOManager implements NutsIOManager {

    public static final Pattern ANCHOR_PATTERN = Pattern.compile("[#]+(?<n>.+)[:]?[#]+");
    //    private final NutsLogger LOG;
    private NutsWorkspace ws;
    private final Function<String, String> pathExpansionConverter;
    private NutsTerminalManager term;
    private InputStream bootStdin = null;
    private PrintStream bootStdout = null;
    private PrintStream bootStderr = null;

    private InputStream currentStdin = null;
    private PrintStream currentStdout = null;
    private PrintStream currentStderr = null;

    public DefaultNutsIOManager(NutsWorkspace workspace) {
        this.ws = workspace;
        pathExpansionConverter=new NutsWorkspaceVarExpansionFunction(ws);
//        LOG = ws.log().of(DefaultNutsIOManager.class);
        term = new DefaultNutsTerminalManager(ws);
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

    @Override
    public InputStream nullInputStream() {
        return NullInputStream.INSTANCE;
    }

    @Override
    public PrintStream nullPrintStream() {
        return new PrintStream(NullOutputStream.INSTANCE);
        //return createPrintStream(NullOutputStream.INSTANCE, NutsTerminalMode.FILTERED, session);
    }

    @Override
    public PrintStream createPrintStream(OutputStream out, NutsTerminalMode expectedMode, NutsSession session) {
        if (out == null) {
            return null;
        }NutsTerminalMode expectedMode0=ws.config().options().getTerminalMode();
        if (expectedMode0 == null) {
            if(ws.config().options().isBot()){
                expectedMode0 = NutsTerminalMode.FILTERED;
            }else{
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
        return CoreIOUtils.toPrintStream(CoreIOUtils.convertOutputStream(out, expectedMode, ws), session);
    }

    @Override
    public NutsTempAction tmp() {
        return new DefaultTempAction(ws);
    }

    @Override
    public NutsTerminalManager term() {
        return term;
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
    public NutsInputAction input() {
        return new DefaultNutsInputAction(ws);
    }

    @Override
    public NutsOutputAction output() {
        return new DefaultNutsOutputAction(ws);
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
