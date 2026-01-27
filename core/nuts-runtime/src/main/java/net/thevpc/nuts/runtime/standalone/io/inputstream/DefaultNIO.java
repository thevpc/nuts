package net.thevpc.nuts.runtime.standalone.io.inputstream;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.app.NApp;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.concurrent.NScoredCallable;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNBootModel;
import net.thevpc.nuts.runtime.standalone.io.path.*;
import net.thevpc.nuts.runtime.standalone.io.path.spi.FilePath;
import net.thevpc.nuts.runtime.standalone.io.path.spi.URLPath;
import net.thevpc.nuts.runtime.standalone.io.printstream.*;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNWorkspaceConfigModel;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.io.NIOUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

@NComponentScope(NScopeType.WORKSPACE)
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNIO implements NIO {
    public DefaultNWorkspaceConfigModel cmodel;
    public DefaultNBootModel bootModel;

    public DefaultNIO() {
        this.cmodel = NWorkspaceExt.of().getConfigModel();
        bootModel = NWorkspaceExt.of().getModel().bootModel;
    }

    @Override
    public InputStream stdin() {
        return getBootModel().getSystemTerminal().in();
    }

    private DefaultNBootModel getBootModel() {
        return NWorkspaceExt.of().getModel().bootModel;
    }


    public OutputStream unwrapOutputStream(OutputStream out) {
        while (out != null) {
            if (out instanceof OutputStreamDelegate) {
                out = ((OutputStreamDelegate) out).getDelegateOutputStream();
            } else {
                break;
            }
        }
        return out;
    }

    public InputStream unwrapInputStream(InputStream in) {
        while (in != null) {
            if (in instanceof InputStreamDelegate) {
                in = ((InputStreamDelegate) in).getDelegateInputStream();
            } else {
                break;
            }
        }
        return in;
    }

    @Override
    public boolean isStdin(InputStream in) {
        InputStream sysin = unwrapInputStream(getBootModel().getSystemTerminal().in());
        in = unwrapInputStream(in);
        return in == sysin;
    }

    @Override
    public boolean isStdout(OutputStream out) {
        OutputStream sysout = unwrapOutputStream(getBootModel().getSystemTerminal().out().asOutputStream());
        out = unwrapOutputStream(out);
        return out == sysout;
    }

    @Override
    public boolean isStderr(OutputStream err) {
        OutputStream syserr = unwrapOutputStream(getBootModel().getSystemTerminal().err().asOutputStream());
        err = unwrapOutputStream(err);
        return err == syserr;
    }

    @Override
    public boolean isStdout(NPrintStream out) {
        if (out == null) {
            return false;
        }
        NSystemTerminal st = getBootModel().getSystemTerminal();
        if (out == st.out()) {
            return true;
        }
        if (out instanceof NPrintStreamRendered) {
            return isStdout(((NPrintStreamRendered) out).getBase());
        }
        return out instanceof NPrintStreamSystem;
    }

    @Override
    public boolean isStderr(NPrintStream out) {
        if (out == null) {
            return false;
        }
        NSystemTerminal st = getBootModel().getSystemTerminal();
        if (out == st.err()) {
            return true;
        }
        if (out instanceof NPrintStreamRendered) {
            return isStderr(((NPrintStreamRendered) out).getBase());
        }
        return out instanceof NPrintStreamSystem;
    }

    @Override
    public NPrintStream stdout() {
        return getBootModel().getSystemTerminal().out();
    }

    @Override
    public NPrintStream stderr() {
        return getBootModel().getSystemTerminal().err();
    }

    private DefaultNWorkspaceConfigModel getConfigModel() {
        return NWorkspaceExt.of().getConfigModel();
    }


//    @Override
//    public NSessionTerminal createTerminal() {
//        return cmodel.createTerminal(session);
//    }
//
//    @Override
//    public NSessionTerminal createTerminal(InputStream in, NPrintStream out, NPrintStream err) {
//        return cmodel.createTerminal(in, out, err, session);
//    }
//
//    @Override
//    public NSessionTerminal createTerminal(NSessionTerminal terminal) {
//        if (terminal == null) {
//            return createTerminal();
//        }
//        if (terminal instanceof DefaultNSessionTerminalFromSystem) {
//            DefaultNSessionTerminalFromSystem t = (DefaultNSessionTerminalFromSystem) terminal;
//            return new DefaultNSessionTerminalFromSystem(session, t);
//        }
//        if (terminal instanceof DefaultNSessionTerminalFromSession) {
//            DefaultNSessionTerminalFromSession t = (DefaultNSessionTerminalFromSession) terminal;
//            return new DefaultNSessionTerminalFromSession(session, t);
//        }
//        return new DefaultNSessionTerminalFromSession(session, terminal);
//    }
//
//    @Override
//    public NSessionTerminal createInMemoryTerminal() {
//        return createInMemoryTerminal(false);
//    }
//
//    @Override
//    public NSessionTerminal createInMemoryTerminal(boolean mergeErr) {
//        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
//        NMemoryPrintStream out = NMemoryPrintStream.of();
//        NMemoryPrintStream err = mergeErr ? out : NMemoryPrintStream.of();
//        return createTerminal(in, out, err);
//    }

    @Override
    public NSystemTerminal getSystemTerminal() {
        return NWorkspaceExt.of().getModel().bootModel.getSystemTerminal();
    }

    @Override
    public NIO setSystemTerminal(NSystemTerminalBase terminal) {
        NWorkspaceExt.of().getModel().bootModel.setSystemTerminal(terminal);
        return this;
    }

    @Override
    public NTerminal getDefaultTerminal() {
        return cmodel.getTerminal();
    }

    @Override
    public NIO setDefaultTerminal(NTerminal terminal) {
        cmodel.setTerminal(terminal);
        return this;
    }


    @Override
    public NIO addPathFactory(NPathFactorySPI pathFactory) {
        getConfigModel().addPathFactory(pathFactory);
        return this;
    }

    @Override
    public NIO removePathFactory(NPathFactorySPI pathFactory) {
        getConfigModel().removePathFactory(pathFactory);
        return this;
    }



    @Override
    public String probeContentType(Path path) {
        return probeContentType(path == null ? null : NPath.of(path));
    }

    @Override
    public String probeContentType(File path) {
        return probeContentType(path == null ? null : NPath.of(path));
    }

    @Override
    public String probeContentType(URL path) {
        return probeContentType(path == null ? null : NPath.of(path));
    }

    @Override
    public String probeContentType(NPath path) {
        List<NContentTypeResolver> allSupported = NExtensions.of()
                .createAllSupported(NContentTypeResolver.class, path);
        NScoredCallable<String> best = NScorable.<NScoredCallable<String>>query()
                .fromStream(allSupported.stream().map(x -> x.probeContentType(path)))
                .getBest().orNull();
        if (best == null) {
            return null;
        }
        return best.call();
    }

    @Override
    public List<String> findExtensionsByContentType(String contentType) {
        List<NContentTypeResolver> allSupported = NExtensions.of()
                .createAllSupported(NContentTypeResolver.class, null);
        LinkedHashSet<String> all = new LinkedHashSet<>();
        for (NContentTypeResolver r : allSupported) {
            List<String> s = r.findExtensionsByContentType(contentType);
            if (s != null) {
                all.addAll(s.stream().filter(x -> !NBlankable.isBlank(x)).collect(Collectors.toList()));
            }
        }
        return new ArrayList<>(all);
    }

    @Override
    public List<String> findContentTypesByExtension(String extension) {
        List<NContentTypeResolver> allSupported = NExtensions.of()
                .createAllSupported(NContentTypeResolver.class, null);
        LinkedHashSet<String> all = new LinkedHashSet<>();
        for (NContentTypeResolver r : allSupported) {
            List<String> s = r.findContentTypesByExtension(extension);
            if (s != null) {
                all.addAll(s.stream().filter(NBlankable::isBlank).collect(Collectors.toList()));
            }
        }
        return new ArrayList<>(all);
    }

    @Override
    public String probeContentType(InputStream stream) {
        byte[] buffer = NIOUtils.readBestEffort(4096, stream);
        return probeContentType(buffer);
    }

    @Override
    public String probeContentType(byte[] bytes) {
        List<NContentTypeResolver> allSupported = NExtensions.of()
                .createAllSupported(NContentTypeResolver.class, bytes);
        NScoredCallable<String> best = NScorable.<NScoredCallable<String>>query()
                .fromStream(allSupported.stream().map(x -> x.probeContentType(bytes)))
                .getBest().orNull();
        if (best == null) {
            return null;
        }
        return best.call();
    }

    @Override
    public String probeCharset(URL path) {
        return probeCharset(path == null ? null : NPath.of(path));
    }

    @Override
    public String probeCharset(File path) {
        return probeCharset(path == null ? null : NPath.of(path));
    }

    @Override
    public String probeCharset(Path path) {
        return probeCharset(path == null ? null : NPath.of(path));
    }

    @Override
    public String probeCharset(NPath path) {
        List<NCharsetResolver> allSupported = NExtensions.of()
                .createAllSupported(NCharsetResolver.class, path);
        NScoredCallable<String> best = NScorable.<NScoredCallable<String>>query()
                .fromStream(allSupported.stream().map(x -> x.probeCharset(path)))
                .getBest().orNull();
        if (best == null) {
            return null;
        }
        return best.call();
    }

    @Override
    public String probeCharset(InputStream stream) {
        byte[] buffer = NIOUtils.readBestEffort(4096 * 10, stream);
        return probeCharset(buffer);
    }

    @Override
    public String probeCharset(byte[] bytes) {
        List<NCharsetResolver> allSupported = NExtensions.of()
                .createAllSupported(NCharsetResolver.class, bytes);
        NScoredCallable<String> best = NScorable.<NScoredCallable<String>>query()
                .fromStream(allSupported.stream().map(x -> x.probeCharset(bytes)))
                .getBest().orNull();
        if (best == null) {
            return null;
        }
        return best.call();
    }

    @Override
    public NTempOutputStream ofTempOutputStream() {
        return new NTempOutputStreamImpl();
    }


}
