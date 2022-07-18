/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.xtra.cp;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.progress.NutsProgressUtils;
import net.thevpc.nuts.runtime.standalone.io.progress.SingletonNutsInputStreamProgressFactory;
import net.thevpc.nuts.runtime.standalone.io.util.*;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsPaths;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.text.NutsText;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.logging.Level;

/**
 * @author thevpc
 */
public class DefaultNutsCp implements NutsCp {

    private final NutsWorkspace ws;
    private NutsLogger LOG;
    private NutsCpValidator checker;
    private boolean skipRoot = false;
    private int maxRepeatCount = 3;
    private NutsInputSource source;
    private NutsOutputTarget target;
    private NutsSession session;
    private NutsProgressFactory progressMonitorFactory;
    private boolean interrupted;
    private boolean recursive;
    private boolean mkdirs;
    private Interruptible interruptibleInstance;
    private Object sourceOrigin;
    private String sourceTypeName;

    private NutsMessage actionMessage;
    private Set<NutsPathOption> options = new LinkedHashSet<>();

    public DefaultNutsCp(NutsSession session) {
        this.session = session;
        this.ws = session.getWorkspace();
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

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = NutsLogger.of(DefaultNutsCp.class, session);
        }
        return LOG;
    }

    protected void checkSession() {
        NutsSessionUtils.checkSession(ws, session);
    }

    @Override
    public NutsInputSource getSource() {
        return source;
    }

    @Override
    public NutsCp setSource(NutsPath source) {
        this.source = source;
        return this;
    }

    @Override
    public NutsCp setSource(InputStream source) {
        checkSession();
        this.source = source == null ? null : NutsIO.of(session).createInputSource(source);
        return this;
    }

    @Override
    public NutsCp setSource(File source) {
        checkSession();
        this.source = source == null ? null : NutsPath.of(source, session);
        return this;
    }

    @Override
    public NutsCp setSource(Path source) {
        this.source = source == null ? null : NutsPath.of(source, session);
        return this;
    }

    @Override
    public NutsCp setSource(URL source) {
        this.source = source == null ? null : NutsPath.of(source, session);
        return this;
    }

    @Override
    public NutsCp setSource(String source) {
        this.source = source == null ? null : NutsPath.of(source, session);
        return this;
    }

    @Override
    public NutsCp setSource(byte[] source) {
        checkSession();
        this.source = source == null ? null : NutsIO.of(session).createInputSource(new ByteArrayInputStream(source));
        return this;
    }

    @Override
    public NutsCp from(NutsInputSource source) {
        this.source = source;
        return this;
    }

    @Override
    public NutsCp from(NutsPath source) {
        this.source = source;
        return this;
    }

    @Override
    public NutsCp to(NutsOutputTarget target) {
        this.target = target;
        return this;
    }

    @Override
    public NutsCp from(InputStream source) {
        return setSource(source);
    }

    @Override
    public NutsCp from(File source) {
        return setSource(source);
    }

    @Override
    public NutsCp from(Path source) {
        return setSource(source);
    }

    @Override
    public NutsCp from(URL source) {
        return setSource(source);
    }

    @Override
    public NutsCp from(byte[] source) {
        return setSource(source);
    }

    @Override
    public NutsOutputTarget getTarget() {
        return target;
    }

    @Override
    public NutsCp setTarget(OutputStream target) {
        checkSession();
        this.target = target == null ? null : NutsIO.of(session).createOutputTarget(target);
        return this;
    }

    @Override
    public NutsCp setTarget(NutsPrintStream target) {
        this.target = target;
        return this;
    }

    @Override
    public NutsCp setTarget(NutsPath target) {
        this.target = target;
        return this;
    }

    @Override
    public NutsCp setTarget(Path target) {
        this.target = target == null ? null : NutsPath.of(target, session);
        return this;
    }

    @Override
    public NutsCp setTarget(File target) {
        this.target = target == null ? null : NutsPath.of(target, session);
        return this;
    }

    @Override
    public NutsCp setTarget(NutsOutputTarget target) {
        this.target = target;
        return this;
    }

    @Override
    public NutsCp setSource(NutsInputSource source) {
        this.source = source;
        return this;
    }

    @Override
    public NutsCp to(OutputStream target) {
        return setTarget(target);
    }

    @Override
    public NutsCp to(NutsPrintStream target) {
        return setTarget(target);
    }

    @Override
    public NutsCp to(Path target) {
        return setTarget(target);
    }

    @Override
    public NutsCp to(File target) {
        return setTarget(target);
    }

    @Override
    public NutsCp to(NutsPath target) {
        this.target = target;
        return this;
    }

    @Override
    public NutsCp addOptions(NutsPathOption... pathOptions) {
        if (pathOptions != null) {
            for (NutsPathOption o : pathOptions) {
                if (o != null) {
                    options.add(o);
                }
            }
        }
        return this;
    }

    @Override
    public NutsCp removeOptions(NutsPathOption... pathOptions) {
        if (pathOptions != null) {
            for (NutsPathOption o : pathOptions) {
                if (o != null) {
                    options.remove(o);
                }
            }
        }
        return this;
    }

    @Override
    public NutsCp clearOptions() {
        options.clear();
        return this;
    }

    @Override
    public Set<NutsPathOption> getOptions() {
        return new LinkedHashSet<>(options);
    }

    @Override
    public NutsCpValidator getValidator() {
        return checker;
    }

    @Override
    public DefaultNutsCp setValidator(NutsCpValidator checker) {
        this.checker = checker;
        return this;
    }

    @Override
    public boolean isRecursive() {
        return recursive;
    }

    @Override
    public NutsCp setRecursive(boolean recursive) {
        this.recursive = recursive;
        return this;
    }

    @Override
    public boolean isMkdirs() {
        return mkdirs;
    }

    @Override
    public NutsCp setMkdirs(boolean mkdirs) {
        this.mkdirs = mkdirs;
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsCp setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(ws, session);
        return this;
    }

    @Override
    public byte[] getByteArrayResult() {
        checkSession();
        NutsMemoryPrintStream b = NutsPrintStream.ofInMemory(session);
        to(b);
        removeOptions(NutsPathOption.SAFE);
        run();
        return b.getBytes();
    }

    @Override
    public String getStringResult() {
        return new String(getByteArrayResult());
    }

    @Override
    public NutsCp run() {
        checkSession();
        NutsUtils.requireNonBlank(source, session, "source");
        NutsUtils.requireNonBlank(target, session, "target");

        NutsInputSource _source = source;
        if ((_source instanceof NutsPath) && ((NutsPath) _source).isDirectory()) {
            // this is a directory!!!
            if (!(target instanceof NutsPath)) {
                throw new NutsIllegalArgumentException(getSession(), NutsMessage.ofCstyle("unsupported copy of directory to %s", target));
            }
            Path fromPath = ((NutsPath) _source).toFile();
            Path toPath = ((NutsPath) target).toFile();
            CopyData cd = new CopyData();
            if (
                    options.contains(NutsPathOption.LOG)
                            || options.contains(NutsPathOption.TRACE)
                            || getProgressFactory() != null
            ) {
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

    /**
     * return progress factory responsible of creating progress monitor
     *
     * @return progress factory responsible of creating progress monitor
     * @since 0.5.8
     */
    @Override
    public NutsProgressFactory getProgressFactory() {
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
    public NutsCp setProgressFactory(NutsProgressFactory value) {
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
    public NutsCp setProgressMonitor(NutsProgressListener value) {
        this.progressMonitorFactory = value == null ? null : new SingletonNutsInputStreamProgressFactory(value);
        return this;
    }

    @Override
    public boolean isSkipRoot() {
        return skipRoot;
    }

    @Override
    public NutsCp setSkipRoot(boolean skipRoot) {
        this.skipRoot = skipRoot;
        return this;
    }

    public NutsCp interrupt() {
        if (interruptibleInstance != null) {
            interruptibleInstance.interrupt();
        }
        this.interrupted = true;
        return this;
    }

    public Object getSourceOrigin() {
        return sourceOrigin;
    }

    public NutsCp setSourceOrigin(Object sourceOrigin) {
        this.sourceOrigin = sourceOrigin;
        return this;
    }

    public NutsMessage getActionMessage() {
        return actionMessage;
    }

    public DefaultNutsCp setActionMessage(NutsMessage actionMessage) {
        this.actionMessage = actionMessage;
        return this;
    }

    public String getSourceTypeName() {
        return sourceTypeName;
    }

    public NutsCp setSourceTypeName(String sourceTypeName) {
        this.sourceTypeName = sourceTypeName;
        return this;
    }

    private void checkInterrupted() {
        if (interrupted) {
            throw new NutsIOException(session, new InterruptException());
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
            throw new NutsIOException(session, exc);
        }
    }

    private void copyFolderWithMonitor(Path srcBase, Path targetBase, CopyData f) {
        checkSession();
        NutsSession session = getSession();
        long start = System.nanoTime();
        Object origin = getSourceOrigin();
        NutsProgressListener m = NutsProgressUtils.createProgressMonitor(
                NutsProgressUtils.MonitorType.DEFAULT, NutsPath.of(srcBase, session), origin, session,
                options.contains(NutsPathOption.LOG),
                options.contains(NutsPathOption.TRACE),
                getProgressFactory());
        NutsText srcBaseMessage = NutsTexts.of(session).ofText(srcBase);
        m.onProgress(NutsProgressEvent.ofStart(srcBase,
                NutsMessage.ofNtf(srcBaseMessage)
                , f.files + f.folders, session));
        try {
            NutsSession finalSession = session;
            Files.walkFileTree(srcBase, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    checkInterrupted();
                    f.doneFolders++;
                    NutsPath.of(transformPath(dir, srcBase, targetBase), session).mkdirs();
                    m.onProgress(NutsProgressEvent.ofProgress(srcBase, NutsMessage.ofNtf(srcBaseMessage),
                            f.doneFiles + f.doneFolders, System.nanoTime() - start, null,
                            0, 0, f.files + f.folders, null, finalSession));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    checkInterrupted();
                    f.doneFiles++;
                    copy(file, transformPath(file, srcBase, targetBase), options);
                    m.onProgress(NutsProgressEvent.ofProgress(srcBase, NutsMessage.ofNtf(srcBaseMessage),
                            f.doneFiles + f.doneFolders, System.nanoTime() - start,
                            null, 0, 0, f.files + f.folders, null, finalSession));
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
            throw new NutsIOException(session, exc);
        } finally {
            m.onProgress(NutsProgressEvent.ofComplete(srcBase, NutsMessage.ofNtf(srcBaseMessage), f.files + f.folders,
                    System.nanoTime() - start, null, 0, 0,
                    f.files + f.folders, null, session));
        }
    }

    public Path copy(Path source, Path target, Set<NutsPathOption> options) throws IOException {
        if (options.contains(NutsPathOption.INTERRUPTIBLE)) {
            if (Files.exists(target)) {
                if (!options.contains(NutsPathOption.REPLACE_EXISTING)) {
                    return null;
                }
            }
            try (InputStream in = CoreIOUtils.toInterruptible(Files.newInputStream(source))) {
                interruptibleInstance = (Interruptible) in;
                try (OutputStream out = Files.newOutputStream(target)) {
                    transferTo(in, out);
                }
            }
            return target;
        }
        return Files.copy(source, target, CoreIOUtils.asCopyOptions(options).toArray(new CopyOption[0]));
    }

    public long copy(InputStream in, Path target, Set<NutsPathOption> options)
            throws IOException {
        if (options.contains(NutsPathOption.INTERRUPTIBLE)) {
            in = CoreIOUtils.toInterruptible(in);
            interruptibleInstance = (Interruptible) in;
            try (OutputStream out = Files.newOutputStream(target)) {
                return transferTo(in, out);
            }
        }
        return Files.copy(in, target, CoreIOUtils.asCopyOptions(options).toArray(new CopyOption[0]));
    }

    public long copy(InputStream in, OutputStream out, Set<NutsPathOption> options)
            throws IOException {
        if (options.contains(NutsPathOption.INTERRUPTIBLE)) {
            in = CoreIOUtils.toInterruptible(in);
            interruptibleInstance = (Interruptible) in;
            return transferTo(in, out);
        }
        return CoreIOUtils.copy(in, out, session);
    }

    public long copy(Path source, OutputStream out) throws IOException {
        if (options.contains(NutsPathOption.INTERRUPTIBLE)) {
            try (InputStream in = CoreIOUtils.toInterruptible(Files.newInputStream(source))) {
                interruptibleInstance = (Interruptible) in;
                try {
                    return transferTo(in, out);
                } catch (IOException ex) {
                    throw new MiddleTransferException(ex);
                }
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
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    checkInterrupted();
                    f.doneFolders++;
                    NutsPath.of(transformPath(dir, srcBase, targetBase), session).mkdirs();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    checkInterrupted();
                    f.doneFiles++;
                    copy(file, transformPath(file, srcBase, targetBase), options);
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
            throw new NutsIOException(session, exc);
        }
    }

    private void copyStream() {
        checkSession();
        NutsUtils.requireNonBlank(source, session, "source");
        NutsUtils.requireNonBlank(target, session, "target");
        boolean safe = options.contains(NutsPathOption.SAFE);
        if (safe) {
            copyStreamSafe(source, target);
        } else {
            copyStreamOnce(source, target);
        }
    }

    private void copyStreamSafe(NutsInputSource source, NutsOutputTarget target) {
        if (source.isMultiRead()) {
            copyStreamMulti(source, target);
        } else {
            copyStreamOnce(source, target);
        }
    }

    private void copyStreamMulti(NutsInputSource source, NutsOutputTarget target) {
        int repeatCount = 1;
        int maxRepeatCount = this.maxRepeatCount;
        if (maxRepeatCount < 1) {
            maxRepeatCount = 3;
        }
        for (int i = repeatCount; i <= maxRepeatCount; i++) {
            try {
                NutsLoggerOp lop = _LOGOP(session);
                if (i > 1 && lop.isLoggable(Level.FINEST)) {
                    lop.level(Level.FINEST).verb(NutsLoggerVerb.START).log(NutsMessage.ofJstyle("repeat download #{0} {1}",
                            i,
                            source));
                }
                copyStreamOnce(source, target);
                return;
            } catch (NutsIOException ex) {
                Throwable cause = ex.getCause();
                if (
                        cause instanceof SocketException
                                || cause instanceof SocketTimeoutException
                                || cause instanceof MiddleTransferException
                ) {
                    //ignore;
                } else {
                    throw ex;
                }
            }
        }
    }

    private NutsPath asValidSourcePath() {
        if (source != null) {
            if (source instanceof NutsPath) {
                NutsPath p = (NutsPath) source;
                if (p.isFile()) {
                    return p;
                }
            }
        }
        return null;
    }

    private NutsPath asValidTargetPath() {
        if (target != null) {
            if (target instanceof NutsPath) {
                NutsPath p = (NutsPath) target;
                if (p.isFile()) {
                    return p;
                }
            }
        }
        return null;
    }

    private void copyStreamOnce(NutsInputSource source, NutsOutputTarget target) {
        NutsInputSource _source = source;

        NutsUtils.requireNonNull(source, getSession(), "source");
        NutsUtils.requireNonNull(target, getSession(), "target");
        NutsPath _target = asValidTargetPath();
        NutsPath _source0 = asValidSourcePath();
        boolean _target_isPath = _target != null;
        boolean safe = options.contains(NutsPathOption.SAFE);
        if (checker != null && !_target_isPath && !safe) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.ofNtf("unsupported validation if neither safeCopy is armed nor path is defined"));
        }
        NutsMessage loggedSrc = _source.getInputMetaData().getMessage().orElse(NutsMessage.ofPlain("unknown-source"));
        NutsMessage loggedTarget = target.getOutputMetaData().getMessage().orElse(NutsMessage.ofPlain("unknown-target"));
        NutsMessage m = getActionMessage();
        if (m == null) {
            m = NutsMessage.ofPlain("copy");
        }
        if (options.contains(NutsPathOption.LOG)) {
            session.getTerminal().printProgress("%-14s %s to %s", m, loggedSrc, loggedTarget);
        }
        if (options.contains(NutsPathOption.LOG)
                || options.contains(NutsPathOption.TRACE)
                || getProgressFactory() != null
        ) {
            NutsInputStreamMonitor monitor = NutsInputStreamMonitor.of(session);
            monitor.setSource(_source);
            monitor.setLogProgress(options.contains(NutsPathOption.LOG));
            monitor.setTraceProgress(options.contains(NutsPathOption.TRACE));
            monitor.setOrigin(getSourceOrigin());
            monitor.setSourceTypeName(getSourceTypeName());
            _source = NutsIO.of(session).createInputSource(
                    monitor.setProgressFactory(getProgressFactory())
                            .setLogProgress(options.contains(NutsPathOption.LOG))
                            .create());
        }
        NutsLoggerOp lop = _LOGOP(session);
        if (lop.isLoggable(Level.FINEST)) {
            lop.level(Level.FINEST).verb(NutsLoggerVerb.START).log(NutsMessage.ofJstyle("{0} {1} to {2}",
                    m,
                    loggedSrc,
                    loggedTarget));
        }
        try {
            if (safe) {
                Path temp = null;
                if (_target_isPath) {
                    Path to = _target.toFile();
                    NutsPath.of(to, session).mkParentDirs();
                    temp = to.resolveSibling(to.getFileName() + "~");
                } else {
                    temp = NutsPaths.of(getSession())
                            .createTempFile("temp~").toFile();
                }
                try {
                    if (_source0 != null) {
                        copy(_source0.toFile(), temp, new HashSet<>(Collections.singletonList(NutsPathOption.REPLACE_EXISTING)));
                    } else {
                        try (InputStream ins = _source.getInputStream()) {
                            copy(ins, temp, new HashSet<>(Collections.singletonList(NutsPathOption.REPLACE_EXISTING)));
                        }
                    }
                    _validate(temp);
                    if (_target_isPath) {
                        try {
                            Files.move(temp, _target.toFile(), StandardCopyOption.REPLACE_EXISTING);
                        } catch (FileSystemException e) {
                            // happens when the file is used by another process
                            // in that case try to check if the file needs to be copied
                            //if not, return safely!
                            if (CoreIOUtils.compareContent(temp, _target.toFile(), session)) {
                                //cannot write the file (used by another process), but no pbm because does not need to
                                return;
                            }
                            throw e;
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
                    Path to = _target.toFile();
                    NutsPath.of(to, session).mkParentDirs();
                    if (_source0 != null) {
                        copy(_source0.toFile(), to, new HashSet<>(Collections.singletonList(NutsPathOption.REPLACE_EXISTING)));
                    } else {
                        try (InputStream ins = _source.getInputStream()) {
                            copy(ins, to, new HashSet<>(Collections.singletonList(NutsPathOption.REPLACE_EXISTING)));
                        }
                    }
                    _validate(to);
                } else {
                    ByteArrayOutputStream bos = null;
                    if (checker != null) {
                        bos = new ByteArrayOutputStream();
                        if (_source0 != null) {
                            copy(_source0.toFile(), bos);
                        } else {
                            try (InputStream ins = _source.getInputStream()) {
                                copy(ins, bos, options);
                            }
                        }
                        try (OutputStream ops = target.getOutputStream()) {
                            copy(new ByteArrayInputStream(bos.toByteArray()), ops, options);
                        }
                        _validate(bos.toByteArray());
                    } else {
                        if (_source0 != null) {
                            try (OutputStream ops = target.getOutputStream()) {
                                copy(_source0.toFile(), ops);
                            }
                        } else {
                            try (InputStream ins = _source.getInputStream()) {
                                try (OutputStream ops = target.getOutputStream()) {
                                    copy(ins, ops, options);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            lop.level(Level.CONFIG).verb(NutsLoggerVerb.FAIL)
                    .log(NutsMessage.ofJstyle("error copying {0} to {1} : {2}", _source,
                            target, ex));
            throw new NutsIOException(session, ex);
        }
    }

    private void _validate(Path temp) {
        if (checker != null) {
            try (InputStream in = Files.newInputStream(temp)) {
                checker.validate(in);
            } catch (NutsCpValidatorException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new NutsCpValidatorException(session, NutsMessage.ofCstyle("validate file %s failed", temp), ex);
            }
        }
    }

    private void _validate(byte[] temp) {
        if (checker != null) {
            try (InputStream in = new ByteArrayInputStream(temp)) {
                checker.validate(in);
            } catch (NutsCpValidatorException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new NutsCpValidatorException(session, NutsMessage.ofPlain("validate file failed"), ex);
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
