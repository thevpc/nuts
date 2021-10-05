/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.io.InterruptException;
import net.thevpc.nuts.runtime.bundles.io.Interruptible;
import net.thevpc.nuts.runtime.bundles.io.NutsStreamOrPath;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.io.progress.DefaultNutsProgressEvent;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.logging.Level;

/**
 * @author thevpc
 */
public class DefaultNutsIOCopyAction implements NutsIOCopyAction {

    private NutsLogger LOG;

    private NutsIOCopyValidator checker;
    private boolean skipRoot = false;
    private boolean safe = true;
    private boolean logProgress = false;
    private NutsStreamOrPath source;
    private NutsStreamOrPath target;
    private NutsSession session;
    private NutsProgressFactory progressMonitorFactory;
    private boolean interruptible;
    private boolean interrupted;
    private boolean recursive;
    private boolean mkdirs;
    private Interruptible interruptibleInstance;
    private NutsWorkspace ws;

    public DefaultNutsIOCopyAction(NutsWorkspace ws) {
        this.ws = ws;
    }

    private static Path transformPath(Path f, Path sourceBase, Path targetBase) {
        String fs = f.toString();
        String bs = sourceBase.toString();
        if (fs.startsWith(bs)) {
            String relative = fs.substring(bs.length());
            if (!relative.startsWith(File.separator)) {
                relative = File.separator + relative;
            }
            String x = targetBase + relative;
            return Paths.get(x);
        }
        throw new RuntimeException("Invalid path " + f);
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = this.ws.log().setSession(session).of(DefaultNutsIOCopyAction.class);
        }
        return LOG;
    }

    protected void checkSession() {
        NutsWorkspaceUtils.checkSession(ws, session);
    }

    @Override
    public Object getSource() {
        return source;
    }

//    public DefaultNutsIOCopyAction setSource(Object source) {
//        this.source = _input().of(source);
//        return this;
//    }

    @Override
    public NutsIOCopyAction setSource(NutsPath source) {
        this.source = source==null?null:NutsStreamOrPath.of(source);
        return this;
    }

    @Override
    public NutsIOCopyAction setSource(InputStream source) {
        this.source = source==null?null:NutsStreamOrPath.of(source);
        return this;
    }

    @Override
    public NutsIOCopyAction setSource(File source) {
        this.source = source==null?null:NutsStreamOrPath.of(session.io().path(source));
        return this;
    }

    @Override
    public NutsIOCopyAction setSource(Path source) {
        this.source = source==null?null:NutsStreamOrPath.of(session.io().path(source));
        return this;
    }

    @Override
    public NutsIOCopyAction setSource(URL source) {
        this.source = source==null?null:NutsStreamOrPath.of(session.io().path(source));
        return this;
    }

    @Override
    public NutsIOCopyAction setSource(String source) {
        this.source = source==null?null:NutsStreamOrPath.of(session.io().path(source));
        return this;
    }

    @Override
    public NutsIOCopyAction from(NutsPath source) {
        this.source = source==null?null:NutsStreamOrPath.of(source);
        return this;
    }

    @Override
    public NutsIOCopyAction from(String source) {
        this.source = source==null?null:NutsStreamOrPath.of(session.io().path(source));
        return this;
    }

    @Override
    public NutsIOCopyAction from(InputStream source) {
        return setSource(source);
    }

    @Override
    public NutsIOCopyAction from(File source) {
        return setSource(source);
    }

    @Override
    public NutsIOCopyAction from(Path source) {
        return setSource(source);
    }

    @Override
    public NutsIOCopyAction from(URL source) {
        return setSource(source);
    }

    @Override
    public Object getTarget() {
        return target;
    }

    @Override
    public NutsIOCopyAction setTarget(OutputStream target) {
        this.target = target==null?null:NutsStreamOrPath.of(target);
        return this;
    }

    @Override
    public NutsIOCopyAction setTarget(NutsPrintStream target) {
        this.target = target==null?null:NutsStreamOrPath.of(target);
        return this;
    }

    @Override
    public NutsIOCopyAction to(NutsPrintStream target) {
        this.target = target==null?null:NutsStreamOrPath.of(target);
        return this;
    }

    @Override
    public NutsIOCopyAction setTarget(NutsPath target) {
        this.target = target==null?null:NutsStreamOrPath.of(target);
        return this;
    }

    @Override
    public NutsIOCopyAction setTarget(Path target) {
        this.target = target==null?null:NutsStreamOrPath.of(session.io().path(target));
        return this;
    }

    @Override
    public NutsIOCopyAction setTarget(String target) {
        this.target = target==null?null:NutsStreamOrPath.of(session.io().path(target));
        return this;
    }

    @Override
    public NutsIOCopyAction setTarget(File target) {
        this.target = target==null?null:NutsStreamOrPath.of(session.io().path(target));
        return this;
    }

    @Override
    public NutsIOCopyAction to(NutsPath target) {
        this.target = target==null?null:NutsStreamOrPath.of(target);
        return this;
    }

    @Override
    public NutsIOCopyAction to(OutputStream target) {
        return setTarget(target);
    }

    @Override
    public NutsIOCopyAction to(String target) {
        this.target = target==null?null:NutsStreamOrPath.of(session.io().path(target));
        return this;
    }

    @Override
    public NutsIOCopyAction to(Path target) {
        return setTarget(target);
    }

    @Override
    public NutsIOCopyAction to(File target) {
        return setTarget(target);
    }

    @Override
    public NutsIOCopyValidator getValidator() {
        return checker;
    }

    @Override
    public DefaultNutsIOCopyAction setValidator(NutsIOCopyValidator checker) {
        this.checker = checker;
        return this;
    }

    @Override
    public boolean isRecursive() {
        return recursive;
    }

    @Override
    public NutsIOCopyAction setRecursive(boolean recursive) {
        this.recursive = recursive;
        return this;
    }

    @Override
    public boolean isMkdirs() {
        return mkdirs;
    }

    @Override
    public NutsIOCopyAction setMkdirs(boolean mkdirs) {
        this.mkdirs = mkdirs;
        return this;
    }

    @Override
    public boolean isSafe() {
        return safe;
    }

    @Override
    public DefaultNutsIOCopyAction setSafe(boolean value) {
        this.safe = value;
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsIOCopyAction setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public byte[] getByteArrayResult() {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        to(b);
        setSafe(false);
        run();
        return b.toByteArray();
    }

    @Override
    public NutsIOCopyAction run() {
        checkSession();
        NutsStreamOrPath _source = source;
        if (_source == null) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.formatted("missing source"));
        }
        if (target == null) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.formatted("missing target"));
        }
        if (_source.isPath() && _source.getPath().isDirectory()) {
            // this is a directory!!!
            if (!target.isPath()) {
                throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("unsupported copy of directory to %s",target));
            }
            Path fromPath=_source.getPath().toFile();
            Path toPath = target.getPath().toFile();
            CopyData cd = new CopyData();
            if (isLogProgress() || getProgressMonitorFactory() != null) {
                prepareCopyFolder(fromPath, cd);
                copyFolderWithMonitor(fromPath, toPath, cd);
            } else {
                copyFolderNoMonitor(fromPath, toPath, cd);
            }
            return this;
        }
        copyStream();
        return this;
    }

    @Override
    public boolean isLogProgress() {
        return logProgress;
    }

    @Override
    public DefaultNutsIOCopyAction setLogProgress(boolean value) {
        this.logProgress = value;
        return this;
    }

    /**
     * return progress factory responsible of creating progress monitor
     *
     * @return progress factory responsible of creating progress monitor
     * @since 0.5.8
     */
    @Override
    public NutsProgressFactory getProgressMonitorFactory() {
        return progressMonitorFactory;
    }

    /**
     * set progress factory responsible of creating progress monitor
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NutsIOCopyAction setProgressMonitorFactory(NutsProgressFactory value) {
        this.progressMonitorFactory = value;
        return this;
    }

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NutsIOCopyAction setProgressMonitor(NutsProgressMonitor value) {
        this.progressMonitorFactory = value == null ? null : new SingletonNutsInputStreamProgressFactory(value);
        return this;
    }

    @Override
    public NutsIOCopyAction setSkipRoot(boolean skipRoot) {
        this.skipRoot = skipRoot;
        return this;
    }

    @Override
    public boolean isSkipRoot() {
        return skipRoot;
    }

    public boolean isInterruptible() {
        return interruptible;
    }

    public NutsIOCopyAction setInterruptible(boolean interruptible) {
        this.interruptible = interruptible;
        return this;
    }

    public NutsIOCopyAction interrupt() {
        if (interruptibleInstance != null) {
            interruptibleInstance.interrupt();
        }
        this.interrupted = true;
        return this;
    }

    private void checkInterrupted() {
        if (interrupted) {
            throw new UncheckedIOException(new IOException(new InterruptException()));
        }
    }

    private void prepareCopyFolder(Path d, CopyData f) {
        try {
            Files.walkFileTree(d, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    checkInterrupted();
                    f.folders++;
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    checkInterrupted();
                    f.files++;
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    checkInterrupted();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    checkInterrupted();
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException exc) {
            throw new UncheckedIOException(exc);
        }
    }

    private void copyFolderWithMonitor(Path srcBase, Path targetBase, CopyData f) {
        checkSession();
        NutsSession session = getSession();
        long start = System.currentTimeMillis();
        NutsProgressMonitor m = CoreIOUtils.createProgressMonitor(CoreIOUtils.MonitorType.DEFAULT, srcBase, srcBase, session, isLogProgress(), getProgressMonitorFactory());
        NutsText srcBaseMessage = session.text().toText(srcBase);
        m.onStart(new DefaultNutsProgressEvent(srcBase,
                srcBaseMessage
                , 0, 0, 0, 0, f.files + f.folders, null, session, false));
        try {
            NutsSession finalSession = session;
            Files.walkFileTree(srcBase, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    checkInterrupted();
                    f.doneFolders++;
                    CoreIOUtils.mkdirs(transformPath(dir, srcBase, targetBase),session);
                    m.onProgress(new DefaultNutsProgressEvent(srcBase, srcBaseMessage, f.doneFiles + f.doneFolders, System.currentTimeMillis() - start, 0, 0, f.files + f.folders, null, finalSession, false));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    checkInterrupted();
                    f.doneFiles++;
                    copy(file, transformPath(file, srcBase, targetBase));
                    m.onProgress(new DefaultNutsProgressEvent(srcBase, srcBaseMessage, f.doneFiles + f.doneFolders, System.currentTimeMillis() - start, 0, 0, f.files + f.folders, null, finalSession, false));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    checkInterrupted();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    checkInterrupted();
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException exc) {
            throw new UncheckedIOException(exc);
        } finally {
            m.onComplete(new DefaultNutsProgressEvent(srcBase, srcBaseMessage, f.files + f.folders, System.currentTimeMillis() - start, 0, 0, f.files + f.folders, null, session, false));
        }
    }

    public Path copy(Path source, Path target, CopyOption... options) throws IOException {
        if (interruptible) {
            try (InputStream in = CoreIOUtils.interruptible(Files.newInputStream(source))) {
                interruptibleInstance = (Interruptible) in;
                try (OutputStream out = Files.newOutputStream(target)) {
                    transferTo(in, out);
                }
            }
            return target;
        }
        return Files.copy(source, target, options);
    }

    public long copy(InputStream in, Path target, CopyOption... options)
            throws IOException {
        if (interruptible) {
            in = CoreIOUtils.interruptible(in);
            interruptibleInstance = (Interruptible) in;
            try (OutputStream out = Files.newOutputStream(target)) {
                return transferTo(in, out);
            }
        }
        return Files.copy(in, target, options);
    }

    public long copy(InputStream in, OutputStream out, CopyOption... options)
            throws IOException {
        if (interruptible) {
            in = CoreIOUtils.interruptible(in);
            interruptibleInstance = (Interruptible) in;
            return transferTo(in, out);
        }
        return CoreIOUtils.copy(in, out);
    }

    public long copy(Path source, OutputStream out) throws IOException {
        if (interruptible) {
            try (InputStream in = CoreIOUtils.interruptible(Files.newInputStream(source))) {
                interruptibleInstance = (Interruptible) in;
                return transferTo(in, out);
            }
        }
        return Files.copy(source, out);
    }

    private long transferTo(InputStream in, OutputStream out) throws IOException {
        int DEFAULT_BUFFER_SIZE = 8192;
        Objects.requireNonNull(out, "out");
        long transferred = 0;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int read;
        while ((read = in.read(buffer, 0, DEFAULT_BUFFER_SIZE)) >= 0) {
            checkInterrupted();
            out.write(buffer, 0, read);
            transferred += read;
        }
        return transferred;
    }

    private void copyFolderNoMonitor(Path srcBase, Path targetBase, CopyData f) {
        try {
            Files.walkFileTree(srcBase, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    checkInterrupted();
                    f.doneFolders++;
                    CoreIOUtils.mkdirs(transformPath(dir, srcBase, targetBase),session);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    checkInterrupted();
                    f.doneFiles++;
                    copy(file, transformPath(file, srcBase, targetBase));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    checkInterrupted();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    checkInterrupted();
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException exc) {
            throw new UncheckedIOException(exc);
        }
    }

    private void copyStream() {
        checkSession();
        NutsStreamOrPath _source = source;
        boolean _target_isPath = target.isPath() && target.getPath().isFile();
        if (checker != null && !_target_isPath && !safe) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.formatted("unsupported validation if neither safeCopy is armed nor path is defined"));
        }
        if (isLogProgress() || getProgressMonitorFactory() != null) {
            NutsMonitorAction monitor = getSession().io().monitor();
            if(_source.isInputStream()){
                monitor.setSource(_source.getInputStream());
            }else{
                monitor.setSource(_source.getPath());
            }
            _source = NutsStreamOrPath.of(monitor
                    .setProgressFactory(getProgressMonitorFactory())
                    .setLogProgress(isLogProgress())
                    .create());
        }
        _LOGOP(session).level(Level.FINEST).verb(NutsLogVerb.START).log(NutsMessage.jstyle("copy {0} to {1}",
                _source==null?null:_source.getValue(),
                target==null?null:target.getValue()));
        try {
            if (safe) {
                Path temp = null;
                if (_target_isPath) {
                    Path to = target.getPath().toFile();
                    CoreIOUtils.mkdirs(to.getParent(),session);
                    temp = to.resolveSibling(to.getFileName() + "~");
                } else {
                    temp = Paths.get(getSession().io().tmp()
                            .setSession(session)
                            .createTempFile("temp~")
                    );
                }
                try {
                    if (_source.isPath() && _source.getPath().isFile()) {
                        copy(_source.getPath().toFile(), temp, StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        try (InputStream ins = _source.getInputStream()) {
                            copy(ins, temp, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                    _validate(temp);
                    if (_target_isPath) {
                        try {
                            Files.move(temp, target.getPath().toFile(), StandardCopyOption.REPLACE_EXISTING);
                        }catch (FileSystemException e){
                            // happens when the file is used by another process
                            // in that case try to check if the file needs to be copied
                            //if not, return safely!
                            if(CoreIOUtils.compareContent(temp,target.getPath().toFile())){
                                //cannot write the file (used by another process), but no pbm because does not need to
                                return;
                            }throw e;
                        }
                        temp = null;
                    } else {
                        try (OutputStream ops = target.getOutputStream()) {
                            copy(temp, ops);
                        }
                    }
                } finally {
                    if (temp != null && Files.exists(temp)) {
                        Files.delete(temp);
                    }
                }
            } else {
                if (_target_isPath) {
                    Path to = target.getPath().toFile();
                    CoreIOUtils.mkdirs(to.getParent(),session);
                    if (_source.isPath() && _source.getPath().isFile()) {
                        copy(_source.getPath().toFile(), to, StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        try (InputStream ins = _source.getInputStream()) {
                            copy(ins, to, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                    _validate(to);
                } else {
                    ByteArrayOutputStream bos = null;
                    if (checker != null) {
                        bos = new ByteArrayOutputStream();
                        if (_source.isPath() && _source.getPath().isFile()) {
                            copy(_source.getPath().toFile(), bos);
                        } else {
                            try (InputStream ins = _source.getInputStream()) {
                                copy(ins, bos);
                            }
                        }
                        try (OutputStream ops = target.getOutputStream()) {
                            copy(new ByteArrayInputStream(bos.toByteArray()), ops);
                        }
                        _validate(bos.toByteArray());
                    } else {
                        if (_source.isPath() && _source.getPath().isFile()) {
                            try (OutputStream ops = target.getOutputStream()) {
                                copy(_source.getPath().toFile(), ops);
                            }
                        } else {
                            try (InputStream ins = _source.getInputStream()) {
                                try (OutputStream ops = target.getOutputStream()) {
                                    copy(ins, ops);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            _LOGOP(session).level(Level.CONFIG).verb(NutsLogVerb.FAIL)
                    .log(NutsMessage.jstyle("error copying {0} to {1} : {2}", _source.getValue(),
                            target.getValue(), ex));
            throw new UncheckedIOException(ex);
        }
    }

    private void _validate(Path temp) {
        if (checker != null) {
            try (InputStream in = Files.newInputStream(temp)) {
                checker.validate(in);
            } catch (NutsIOCopyValidationException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new NutsIOCopyValidationException(session, NutsMessage.cstyle("validate file %s failed",temp), ex);
            }
        }
    }

    private void _validate(byte[] temp) {
        if (checker != null) {
            try (InputStream in = new ByteArrayInputStream(temp)) {
                checker.validate(in);
            } catch (NutsIOCopyValidationException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new NutsIOCopyValidationException(session, NutsMessage.cstyle("validate file failed"), ex);
            }
        }
    }

    private static class CopyData {

        long files;
        long folders;
        long doneFiles;
        long doneFolders;
    }
}
