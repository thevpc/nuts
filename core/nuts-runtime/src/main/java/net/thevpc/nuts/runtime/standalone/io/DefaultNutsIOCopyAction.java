/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.io;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.logging.Level;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.io.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.util.io.DefaultNutsProgressEvent;
import net.thevpc.nuts.runtime.standalone.log.NutsLogVerb;
import net.thevpc.nuts.runtime.standalone.util.io.InterruptException;
import net.thevpc.nuts.runtime.standalone.util.io.Interruptible;

/**
 * @author thevpc
 */
public class DefaultNutsIOCopyAction implements NutsIOCopyAction {

    private final NutsLogger LOG;

    private NutsIOCopyValidator checker;
    private boolean skipRoot = false;
    private boolean safe = true;
    private boolean logProgress = false;
    private NutsInput source;
    private NutsOutput target;
    private DefaultNutsIOManager iom;
    private NutsSession session;
    private NutsProgressFactory progressMonitorFactory;
    private boolean interruptible;
    private boolean interrupted;
    private Interruptible interruptibleInstance;

    public DefaultNutsIOCopyAction(DefaultNutsIOManager iom) {
        this.iom = iom;
        LOG = iom.getWorkspace().log().of(DefaultNutsIOCopyAction.class);
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

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public NutsIOCopyAction setSource(String source) {
        this.source = iom.input().of(source);
        return this;
    }

    @Override
    public NutsIOCopyAction setSource(NutsInput source) {
        this.source = iom.input().of(source);
        return this;
    }

    @Override
    public NutsIOCopyAction setSource(InputStream source) {
        this.source = iom.input().of(source);
        return this;
    }

    @Override
    public NutsIOCopyAction setSource(File source) {
        this.source = iom.input().of(source);
        return this;
    }

    @Override
    public NutsIOCopyAction setSource(Path source) {
        this.source = iom.input().of(source);
        return this;
    }

    @Override
    public NutsIOCopyAction setSource(URL source) {
        this.source = iom.input().of(source);
        return this;
    }

    @Override
    public NutsIOCopyAction setTarget(OutputStream target) {
        this.target = iom.output().of(target);
        return this;
    }

    @Override
    public NutsIOCopyAction setTarget(Path target) {
        this.target = iom.output().of(target);
        return this;
    }

    @Override
    public NutsIOCopyAction setTarget(File target) {
        this.target = iom.output().of(target);
        return this;
    }

    public DefaultNutsIOCopyAction setSource(Object source) {
        this.source = iom.input().of(source);
        return this;
    }

    @Override
    public NutsIOCopyAction from(String source) {
        this.source = iom.input().of(source);
        return this;
    }

    @Override
    public NutsIOCopyAction to(String target) {
        this.target = iom.output().of(target);
        return this;
    }

    @Override
    public NutsIOCopyAction from(Object source) {
        this.source = iom.input().of(source);
        return this;
    }

    @Override
    public NutsIOCopyAction to(Object target) {
        this.target = iom.output().of(target);
        return this;
    }

    @Override
    public Object getTarget() {
        return target;
    }

    //    @Override
    public DefaultNutsIOCopyAction setTarget(Object target) {
        this.target = iom.output().of(target);
        return this;
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
    public boolean isLogProgress() {
        return logProgress;
    }

    @Override
    public DefaultNutsIOCopyAction setLogProgress(boolean value) {
        this.logProgress = value;
        return this;
    }

    @Override
    public NutsIOCopyAction from(NutsInput source) {
        return setSource(source);
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
    public NutsIOCopyAction to(File target) {
        return setTarget(target);
    }

    @Override
    public NutsIOCopyAction to(NutsOutput target) {
        return setTarget(target);
    }

    @Override
    public NutsIOCopyAction setTarget(NutsOutput target) {
        this.target=target;
        return this;
    }

    @Override
    public NutsIOCopyAction to(OutputStream target) {
        return setTarget(target);
    }

    @Override
    public NutsIOCopyAction to(Path target) {
        return setTarget(target);
    }

    @Override
    public NutsIOCopyAction validator(NutsIOCopyValidator validationVerifier) {
        return setValidator(validationVerifier);
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
    public NutsIOCopyAction safe() {
        setSafe(true);
        return this;
    }

    @Override
    public NutsIOCopyAction safe(boolean value) {
        setSafe(value);
        return this;
    }

    @Override
    public NutsIOCopyAction logProgress() {
        setLogProgress(true);
        return this;
    }

    @Override
    public NutsIOCopyAction logProgress(boolean value) {
        setLogProgress(value);
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
        safe(false);
        run();
        return b.toByteArray();
    }

    private void checkInterrupted() {
        if (interrupted) {
            throw new UncheckedIOException(new IOException(new InterruptException()));
        }
    }

    @Override
    public NutsIOCopyAction run() {
        NutsInput _source = source;
        if (_source == null) {
            throw new UnsupportedOperationException("Missing Source");
        }
        if (target == null) {
            throw new UnsupportedOperationException("Missing Target");
        }
        if (_source.isPath()) {
            if (Files.isDirectory(_source.getPath())) {
                // this is a directory!!!
                if (!target.isPath()) {
                    throw new NutsIllegalArgumentException(this.iom.getWorkspace(), "Unsupported copy of directory to " + target);
                }
                Path toPath = target.getPath();
                CopyData cd = new CopyData();
                if (isLogProgress() || getProgressMonitorFactory() != null) {
                    prepareCopyFolder(_source.getPath(), cd);
                    copyFolderWithMonitor(_source.getPath(), toPath, cd);
                } else {
                    copyFolderNoMonitor(_source.getPath(), toPath, cd);
                }
                return this;
            }
        }
        copyStream();
        return this;
    }

    private static class CopyData {

        long files;
        long folders;
        long doneFiles;
        long doneFolders;
    }

    private void prepareCopyFolder(Path d, CopyData f) {
        try {
            Files.walkFileTree(d, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)  {
                    checkInterrupted();
                    f.folders++;
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)  {
                    checkInterrupted();
                    f.files++;
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc)  {
                    checkInterrupted();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc)  {
                    checkInterrupted();
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException exc) {
            throw new UncheckedIOException(exc);
        }
    }

    private void copyFolderWithMonitor(Path srcBase, Path targetBase, CopyData f) {
        NutsSession session = getSession();
        if (session == null) {
            session = iom.getWorkspace().createSession();
        }
        long start = System.currentTimeMillis();
        NutsProgressMonitor m = CoreIOUtils.createProgressMonitor(CoreIOUtils.MonitorType.DEFAULT, srcBase, srcBase, session, isLogProgress(), getProgressMonitorFactory());
        m.onStart(new DefaultNutsProgressEvent(srcBase, srcBase.toString(), 0, 0, 0, 0, f.files + f.folders, null, session, false));
        try {
            NutsSession finalSession = session;
            Files.walkFileTree(srcBase, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    checkInterrupted();
                    f.doneFolders++;
                    Files.createDirectories(transformPath(dir, srcBase, targetBase));
                    m.onProgress(new DefaultNutsProgressEvent(srcBase, srcBase.toString(), f.doneFiles + f.doneFolders, System.currentTimeMillis() - start, 0, 0, f.files + f.folders, null, finalSession, false));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    checkInterrupted();
                    f.doneFiles++;
                    copy(file, transformPath(file, srcBase, targetBase));
                    m.onProgress(new DefaultNutsProgressEvent(srcBase, srcBase.toString(), f.doneFiles + f.doneFolders, System.currentTimeMillis() - start, 0, 0, f.files + f.folders, null, finalSession, false));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc)  {
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
            m.onComplete(new DefaultNutsProgressEvent(srcBase, srcBase.toString(), f.files + f.folders, System.currentTimeMillis() - start, 0, 0, f.files + f.folders, null, session, false));
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

    private void copyFolderNoMonitor(Path srcBase, Path targetBase, CopyData f) {
        try {
            Files.walkFileTree(srcBase, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    checkInterrupted();
                    f.doneFolders++;
                    Files.createDirectories(transformPath(dir, srcBase, targetBase));
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
        NutsInput _source = source;
        boolean _target_isPath = target.isPath();
        if (checker != null && !_target_isPath && !safe) {
            throw new NutsIllegalArgumentException(this.iom.getWorkspace(), "Unsupported validation if neither safeCopy is armed nor path is defined");
        }
        if (isLogProgress() || getProgressMonitorFactory() != null) {
            _source = iom.monitor().source(_source).setSession(session)
                    .progressFactory(getProgressMonitorFactory())
                    .logProgress(isLogProgress())
                    .createSource();
        }
        LOG.with().level(Level.FINEST).verb(NutsLogVerb.START).log("copy {0} to {1}", _source, target);
        try {
            if (safe) {
                Path temp = null;
                if (_target_isPath) {
                    Path to = target.getPath();
                    CoreIOUtils.mkdirs(to.getParent());
                    temp = to.resolveSibling(to.getFileName() + "~");
                } else {
                    temp = iom.tmp().createTempFile("temp~");
                }
                try {
                    if (_source.isPath()) {
                        copy(_source.getPath(), temp, StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        try (InputStream ins = _source.open()) {
                            copy(ins, temp, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                    _validate(temp);
                    if (_target_isPath) {
                        Files.move(temp, target.getPath(), StandardCopyOption.REPLACE_EXISTING);
                        temp = null;
                    } else {
                        try (OutputStream ops = target.open()) {
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
                    Path to = target.getPath();
                    CoreIOUtils.mkdirs(to.getParent());
                    if (_source.isPath()) {
                        copy(_source.getPath(), target.getPath(), StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        try (InputStream ins = _source.open()) {
                            copy(ins, target.getPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                    _validate(target.getPath());
                } else {
                    ByteArrayOutputStream bos = null;
                    if (checker != null) {
                        bos = new ByteArrayOutputStream();
                        if (_source.isPath()) {
                            copy(_source.getPath(), bos);
                        } else {
                            try (InputStream ins = _source.open()) {
                                copy(ins, bos);
                            }
                        }
                        try (OutputStream ops = target.open()) {
                            copy(new ByteArrayInputStream(bos.toByteArray()), ops);
                        }
                        _validate(bos.toByteArray());
                    } else {
                        if (_source.isPath()) {
                            try (OutputStream ops = target.open()) {
                                copy(_source.getPath(), ops);
                            }
                        } else {
                            try (InputStream ins = _source.open()) {
                                try (OutputStream ops = target.open()) {
                                    copy(ins, ops);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            LOG.with().level(Level.CONFIG).verb(NutsLogVerb.FAIL).log("error copying {0} to {1} : {2}", _source.getSource(), target.getSource(), CoreStringUtils.exceptionToString(ex));
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
                throw new NutsIOCopyValidationException(session.getWorkspace(), "Validate file " + temp + " failed", ex);
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
                throw new NutsIOCopyValidationException(session.getWorkspace(), "Validate file failed", ex);
            }
        }
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
     * set progress factory responsible of creating progress monitor
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NutsIOCopyAction progressMonitorFactory(NutsProgressFactory value) {
        return setProgressMonitorFactory(value);
    }

    /**
     * set progress monitor. Will create a singeleton progress monitor factory
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

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NutsIOCopyAction progressMonitor(NutsProgressMonitor value) {
        return setProgressMonitor(value);
    }

    @Override
    public NutsIOCopyAction skipRoot(boolean value) {
        return setSkipRoot(value);
    }

    @Override
    public NutsIOCopyAction skipRoot() {
        return skipRoot(true);
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
}
