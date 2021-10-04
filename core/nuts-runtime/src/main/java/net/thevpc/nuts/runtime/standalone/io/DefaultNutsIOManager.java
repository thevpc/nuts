package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.io.FilePath;
import net.thevpc.nuts.runtime.core.io.URLPath;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DefaultNutsIOManager implements NutsIOManager {

    private DefaultNutsIOModel model;
    private NutsSession session;

    public DefaultNutsIOManager(DefaultNutsIOModel model) {
        this.model = model;
    }

    public DefaultNutsIOModel getModel() {
        return model;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<Object> criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public NutsPath path(String path) {
        checkSession();
        return path(path,null);
    }

    @Override
    public NutsPath path(File path) {
        checkSession();
        if (path == null) {
            return null;
        }
        return new FilePath(path.toPath(),getSession());
    }

    @Override
    public NutsPath path(Path path) {
        checkSession();
        if (path == null) {
            return null;
        }
        return new FilePath(path,getSession());
    }

    @Override
    public NutsPath path(URL path) {
        checkSession();
        if (path == null) {
            return null;
        }
        return new URLPath(path,getSession());
    }

    @Override
    public NutsPath path(String path, ClassLoader classLoader) {
        checkSession();
        if (path == null || path.trim().isEmpty()) {
            return null;
        }
        NutsPath p = model.resolve(path, getSession(), classLoader);
        if (p == null) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("unable to resolve path from %s", path));
        }
        return p;
    }

    @Override
    public InputStream nullInputStream() {
        checkSession();
        return model.nullInputStream();
    }

    @Override
    public NutsPrintStream nullPrintStream() {
        checkSession();
        return model.nullPrintStream();
    }

    @Override
    public NutsPrintStream createPrintStream(OutputStream out, NutsTerminalMode expectedMode) {
        checkSession();
        return model.createPrintStream(out, expectedMode, session);
    }

    @Override
    public NutsPrintStream createPrintStream(OutputStream out) {
        checkSession();
        return new NutsPrintStreamRaw(out, null, null, session, new NutsPrintStreamBase.Bindings());
    }

    @Override
    public NutsPrintStream createPrintStream(Writer out) {
        checkSession();
        return model.createPrintStream(out, NutsTerminalMode.INHERITED, session);
    }

    @Override
    public NutsMemoryPrintStream createMemoryPrintStream() {
        checkSession();
        return new NutsByteArrayPrintStream(getSession());
    }

    @Override
    public NutsTempAction tmp() {
        checkSession();
        return model.tmp().setSession(session);
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
        return model.copy().setSession(session);
    }

    @Override
    public NutsIOProcessAction ps() {
        return model.ps().setSession(session);
    }

    @Override
    public NutsIOCompressAction compress() {
        return model.compress().setSession(session);
    }

    @Override
    public NutsIOUncompressAction uncompress() {
        return model.uncompress().setSession(session);
    }

    @Override
    public NutsIODeleteAction delete() {
        return model.delete().setSession(session);
    }

    @Override
    public NutsMonitorAction monitor() {
        return model.monitor().setSession(session);
    }

    @Override
    public NutsIOHashAction hash() {
        return model.hash().setSession(session);
    }

//    @Override
//    public NutsInputAction input() {
//        return model.input().setSession(session);
//    }
//
//    @Override
//    public NutsOutputAction output() {
//        return model.output().setSession(session);
//    }

    public NutsSession getSession() {
        return session;
    }

    public NutsIOManager setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NutsPrintStream stdout() {
        return model.stdout();
    }

    @Override
    public NutsPrintStream stderr() {
        return model.stderr();
    }

    @Override
    public InputStream stdin() {
        return model.stdin();
    }

    @Override
    public boolean isStandardOutputStream(NutsPrintStream out) {
        if (out == null) {
            return false;
        }
        if (out == model.stdout()) {
            return true;
        }
        if (out == model.bootModel.stdout()) {
            return true;
        }
        if (out instanceof NutsPrintStreamRendered) {
            return isStandardOutputStream(((NutsPrintStreamRendered) out).getBase());
        }
        return false;
    }

    @Override
    public boolean isStandardErrorStream(NutsPrintStream out) {
        if (out == null) {
            return false;
        }
        if (out == model.stderr()) {
            return true;
        }
        if (out == model.bootModel.stderr()) {
            return true;
        }
        if (out instanceof NutsPrintStreamRendered) {
            return isStandardOutputStream(((NutsPrintStreamRendered) out).getBase());
        }
        return false;
    }

    @Override
    public boolean isStandardInputStream(InputStream in) {
        return in == model.stdin();
    }

    @Override
    public NutsIOManager addPathFactory(NutsPathFactory pathFactory) {
        model.addPathFactory(pathFactory);
        return this;
    }

    @Override
    public NutsIOManager removePathFactory(NutsPathFactory pathFactory) {
        model.removePathFactory(pathFactory);
        return this;
    }

    private void checkSession() {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), getSession());
    }
}
