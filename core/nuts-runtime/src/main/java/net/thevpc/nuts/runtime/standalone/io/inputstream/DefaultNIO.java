package net.thevpc.nuts.runtime.standalone.io.inputstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNBootModel;
import net.thevpc.nuts.runtime.standalone.io.path.NPathFromSPI;
import net.thevpc.nuts.runtime.standalone.io.path.spi.FilePath;
import net.thevpc.nuts.runtime.standalone.io.path.spi.URLPath;
import net.thevpc.nuts.runtime.standalone.io.printstream.*;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNWorkspaceConfigModel;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.io.NIOUtils;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringBuilder;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultNIO implements NIO {
    public DefaultNWorkspaceConfigModel cmodel;
    public DefaultNBootModel bootModel;
    public NWorkspace workspace;

    public DefaultNIO(NWorkspace workspace) {
        this.workspace = workspace;
        this.cmodel = NWorkspaceExt.of().getConfigModel();
        bootModel = NWorkspaceExt.of().getModel().bootModel;
    }

    @Override
    public boolean isStdin(InputStream in) {
        return in == stdin();
    }

    @Override
    public InputStream stdin() {
        return getBootModel().getSystemTerminal().in();
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    private DefaultNBootModel getBootModel() {
        return NWorkspaceExt.of().getModel().bootModel;
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
        return NWorkspaceExt.of(workspace).getConfigModel();
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
    public NPath createPath(String path) {
        return createPath(path, null);
    }

    @Override
    public NPath createPath(File path) {
        if (path == null) {
            return null;
        }
        return createPath(new FilePath(path.toPath(), workspace));
    }

    @Override
    public NPath createPath(Path path) {
        if (path == null) {
            return null;
        }
        return createPath(new FilePath(path, workspace));
    }

    @Override
    public NPath createPath(URL path) {
        if (path == null) {
            return null;
        }
        return createPath(new URLPath(path, workspace));
    }

    @Override
    public NPath createPath(String path, ClassLoader classLoader) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }
        NPath p = getConfigModel().resolve(path, classLoader);
        if (p == null) {
            throw new NIllegalArgumentException(NMsg.ofC("unable to resolve path from %s", path));
        }
        return p;
    }

    @Override
    public NPath createPath(NPathSPI path) {
        if (path == null) {
            return null;
        }
        return new NPathFromSPI(path);
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

    public NPath ofTempFile(String name) {
        return createAnyTempFile(name, false, null);
    }

    @Override
    public NPath ofTempFolder(String name) {
        return createAnyTempFile(name, true, null);
    }

    @Override
    public NPath ofTempFile() {
        return createAnyTempFile(null, false, null);
    }

    @Override
    public NPath ofTempFolder() {
        return createAnyTempFile(null, true, null);
    }


    public NPath ofTempRepositoryFile(String name, NRepository repository) {
        return createAnyTempFile(name, false, resolveRootPath(repository));
    }

    @Override
    public NPath ofTempRepositoryFolder(String name, NRepository repository) {
        return createAnyTempFile(name, true, resolveRootPath(repository));
    }

    @Override
    public NPath ofTempRepositoryFile(NRepository repository) {
        return createAnyTempFile(null, false, resolveRootPath(repository));
    }

    @Override
    public NPath ofTempRepositoryFolder(NRepository repository) {
        return createAnyTempFile(null, true, resolveRootPath(repository));
    }


    @Override
    public NPath ofTempIdFile(String name, NId repository) {
        return createAnyTempFile(name, false, resolveRootPath(repository));
    }

    @Override
    public NPath ofTempIdFolder(String name, NId repository) {
        return createAnyTempFile(name, true, resolveRootPath(repository));
    }

    @Override
    public NPath ofTempIdFile(NId repository) {
        return createAnyTempFile(null, false, resolveRootPath(repository));
    }

    @Override
    public NPath ofTempIdFolder(NId repository) {
        return createAnyTempFile(null, true, resolveRootPath(repository));
    }

    private NPath resolveRootPath(NRepository repositoryId) {
        if (repositoryId == null) {
            return NWorkspace.of().getStoreLocation(NStoreType.TEMP);
        } else {
            return repositoryId.config().getStoreLocation(NStoreType.TEMP);
        }
    }

    private NPath resolveRootPath(NId nId) {
        if (nId == null) {
            return NWorkspace.of().getStoreLocation(NStoreType.TEMP);
        } else {
            return NWorkspace.of().getStoreLocation(nId, NStoreType.TEMP);
        }
    }

    public NPath createAnyTempFile(String name, boolean folder, NPath rootFolder) {
        NSession session = workspace.currentSession();
        if (rootFolder == null) {
            rootFolder = NWorkspace.of().getStoreLocation(NStoreType.TEMP);
        }
        NId appId = NApp.of().getId().orElseGet(() -> session.getWorkspace().getRuntimeId());
        if (appId != null) {
            rootFolder = rootFolder.resolve(NConstants.Folders.ID).resolve(NWorkspace.of().getDefaultIdBasedir(appId));
        }
        if (name == null) {
            name = "";
        }
        rootFolder.mkdirs();
        NStringBuilder ext = new NStringBuilder(NIOUtils.getFileExtension(name, false, true));
        NStringBuilder prefix = new NStringBuilder((ext.length() > 0) ? name.substring(0, name.length() - ext.length()) : name);
        if (ext.isEmpty() && prefix.isEmpty()) {
            prefix.append("nuts-");
            if (!folder) {
                ext.append(".tmp");
            }
        } else if (ext.isEmpty()) {
            if (!folder) {
                ext.append("-tmp");
            }
        } else if (prefix.isEmpty()) {
            prefix.append(ext);
            ext.clear();
            ext.append("-tmp");
        }
        if (!prefix.endsWith("-")) {
            prefix.append('-');
        }
        if (prefix.length() < 3) {
            if (prefix.length() < 3) {
                prefix.append('A');
                if (prefix.length() < 3) {
                    prefix.append('B');
                }
            }
        }

        if (folder) {
            for (int i = 0; i < 15; i++) {
                File temp = null;
                try {
                    temp = File.createTempFile(prefix.toString(), ext.toString(), rootFolder.toFile().get());
                    if (temp.delete() && temp.mkdir()) {
                        return NPath.of(temp.toPath())
                                .setUserTemporary(true);
                    }
                } catch (IOException ex) {
                    //
                }
            }
            throw new NIOException(NMsg.ofC("could not create temp directory: %s*%s", rootFolder + File.separator + prefix, ext));
        } else {
            try {
                return NPath.of(File.createTempFile(prefix.toString(), ext.toString(), rootFolder.toFile().get()).toPath())
                        .setUserTemporary(true);
            } catch (IOException e) {
                throw new NIOException(e);
            }
        }
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
                .createComponents(NContentTypeResolver.class, path);
        NCallableSupport<String> best = null;
        for (NContentTypeResolver r : allSupported) {
            NCallableSupport<String> s = r.probeContentType(path);
            if (s != null && s.isValid()) {
                if (best == null || s.getSupportLevel() > best.getSupportLevel()) {
                    best = s;
                }
            }
        }
        if (best == null) {
            return null;
        }
        return best.call();
    }

    @Override
    public List<String> findExtensionsByContentType(String contentType) {
        List<NContentTypeResolver> allSupported = NExtensions.of()
                .createComponents(NContentTypeResolver.class, null);
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
                .createComponents(NContentTypeResolver.class, null);
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
                .createComponents(NContentTypeResolver.class, bytes);
        NCallableSupport<String> best = null;
        for (NContentTypeResolver r : allSupported) {
            NCallableSupport<String> s = r.probeContentType(bytes);
            if (s != null && s.isValid()) {
                if (best == null || s.getSupportLevel() > best.getSupportLevel()) {
                    best = s;
                }
            }
        }
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
                .createComponents(NCharsetResolver.class, path);
        NCallableSupport<String> best = null;
        for (NCharsetResolver r : allSupported) {
            NCallableSupport<String> s = r.probeCharset(path);
            if (s != null && s.isValid()) {
                if (best == null || s.getSupportLevel() > best.getSupportLevel()) {
                    best = s;
                }
            }
        }
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
                .createComponents(NCharsetResolver.class, bytes);
        NCallableSupport<String> best = null;
        for (NCharsetResolver r : allSupported) {
            NCallableSupport<String> s = r.probeCharset(bytes);
            if (s != null && s.isValid()) {
                if (best == null || s.getSupportLevel() > best.getSupportLevel()) {
                    best = s;
                }
            }
        }
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
