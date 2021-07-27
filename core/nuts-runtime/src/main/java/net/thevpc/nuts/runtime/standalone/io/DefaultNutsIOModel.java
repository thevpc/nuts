package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.io.NullInputStream;
import net.thevpc.nuts.runtime.bundles.parsers.StringPlaceHolderParser;
import net.thevpc.nuts.runtime.core.NutsSupplierBase;
import net.thevpc.nuts.runtime.core.format.text.SimpleWriterOutputStream;
import net.thevpc.nuts.runtime.core.io.ClassLoaderPath;
import net.thevpc.nuts.runtime.core.io.FilePath;
import net.thevpc.nuts.runtime.core.io.NutsPathFromSPI;
import net.thevpc.nuts.runtime.core.io.URLPath;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNutsBootModel;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsPathSPI;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
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
    private List<NutsPathFactory> pathFactories = new ArrayList<>();

    public DefaultNutsIOModel(NutsWorkspace workspace, DefaultNutsBootModel bootModel) {
        this.ws = workspace;
        this.pathExpansionConverter = new NutsWorkspaceVarExpansionFunction(ws);
        this.bootModel = bootModel;
        this.stdout = bootModel.stdout();
        this.stderr = bootModel.stderr();
        this.stdin = bootModel.stdin();
        this.nullOut = new NutsPrintStreamNull(bootModel.bootSession());
        addPathFactory(new FilePathFactory());
        addPathFactory(new ClasspathNutsPathFactory());
        addPathFactory(new URLPathFactory());
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
            return ((NutsPrintStreamAdapter) out).getBaseNutsPrintStream().convertMode(expectedMode);
        }
        SimpleWriterOutputStream w = new SimpleWriterOutputStream(out, session);
        return createPrintStream(w, expectedMode, session);
    }

    public NutsPrintStream createPrintStream(OutputStream out, NutsTerminalMode expectedMode, NutsSession session) {
        if (out == null) {
            return null;
        }
        NutsWorkspaceOptions woptions = ws.env().setSession(session).getBootOptions();
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
            return ((NutsPrintStreamAdapter) out).getBaseNutsPrintStream().convertMode(expectedMode);
        }
        return
                new NutsPrintStreamRaw(out, null, null, session, new NutsPrintStreamBase.Bindings())
                        .convertMode(expectedMode)
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

    public void addPathFactory(NutsPathFactory f) {
        if (f != null && !pathFactories.contains(f)) {
            pathFactories.add(f);
        }
    }

    public void removePathFactory(NutsPathFactory f) {
        pathFactories.remove(f);
    }

    public NutsPath resolve(String path,NutsSession session,ClassLoader classLoader) {
        if(classLoader==null){
            classLoader=Thread.currentThread().getContextClassLoader();
        }
        //
        ClassLoader finalClassLoader = classLoader;
        NutsSupplier<NutsPathSPI> z = Arrays.stream(getPathFactories())
                .map(x -> {
                    try {
                        return x.createPath(path, session, finalClassLoader);
                    } catch (Exception ex) {
                        //
                    }
                    return null;
                })
                .filter(x -> x != null && x.level() > 0)
                .max(Comparator.comparingInt(NutsSupplier::level))
                .orElse(null);
        NutsPathSPI s= z == null ? null : z.create();
        if(s!=null){
            if( s instanceof NutsPath){
                return (NutsPath) s;
            }
            return new NutsPathFromSPI(s);
        }
        return null;
    }

    public NutsPathFactory[] getPathFactories() {
        return pathFactories.toArray(new NutsPathFactory[0]);
    }

    private class URLPathFactory implements NutsPathFactory {
        @Override
        public NutsSupplier<NutsPathSPI> createPath(String path, NutsSession session, ClassLoader classLoader) {
            NutsWorkspaceUtils.checkSession(getWorkspace(), session);
            try {
                URL url=new URL(path);
                return new NutsSupplierBase<NutsPathSPI>(2) {
                    @Override
                    public NutsPathSPI create() {
                        return new URLPath(url, session);
                    }
                };
            }catch (Exception ex){
                //ignore
            }
            return null;
        }
    }

    private class ClasspathNutsPathFactory implements NutsPathFactory {
        @Override
        public NutsSupplier<NutsPathSPI> createPath(String path, NutsSession session, ClassLoader classLoader) {
            NutsWorkspaceUtils.checkSession(getWorkspace(), session);
            try {
                if(path.startsWith("classpath:")) {
                    return new NutsSupplierBase<NutsPathSPI>(2) {
                        @Override
                        public NutsPathSPI create() {
                            return new ClassLoaderPath(path, classLoader, session);
                        }
                    };
                }
            }catch (Exception ex){
                //ignore
            }
            return null;
        }
    }

    private class FilePathFactory implements NutsPathFactory {
        @Override
        public NutsSupplier<NutsPathSPI> createPath(String path, NutsSession session, ClassLoader classLoader) {
            NutsWorkspaceUtils.checkSession(getWorkspace(), session);
            try {
                Path value = Paths.get(path);
                return new NutsSupplierBase<NutsPathSPI>(1) {
                    @Override
                    public NutsPathSPI create() {
                        return new FilePath(value, session);
                    }
                };
            }catch (Exception ex){
                //ignore
            }
            return null;
        }
    }
}
