/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.xtra.cp;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.runtime.standalone.time.NProgressUtils;
import net.thevpc.nuts.runtime.standalone.time.SingletonNInputStreamProgressFactory;
import net.thevpc.nuts.runtime.standalone.io.util.*;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.time.NProgressEvent;
import net.thevpc.nuts.time.NProgressFactory;
import net.thevpc.nuts.time.NProgressListener;
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
@NComponentScope(NScopeType.PROTOTYPE)
public class DefaultNCp implements NCp {

    private final NWorkspace workspace;
    private NCpValidator checker;
    private boolean skipRoot = false;
    private int maxRepeatCount = 3;
    private NInputSource source;
    private NOutputTarget target;
    private NProgressFactory progressMonitorFactory;
    private boolean interrupted;
    private boolean recursive;
    private boolean mkdirs;
    private NInterruptible interruptibleInstance;
    private Object sourceOrigin;
    private String sourceTypeName;

    private NMsg actionMsg;
    private Set<NPathOption> options = new LinkedHashSet<>();

    public DefaultNCp(NWorkspace workspace) {
        this.workspace = workspace;
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
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    protected NLogOp _LOGOP() {
        return _LOG().with();
    }

    protected NLog _LOG() {
        return NLog.of(DefaultNCp.class);
    }

    @Override
    public NInputSource getSource() {
        return source;
    }

    @Override
    public NCp setSource(NPath source) {
        this.source = source;
        return this;
    }

    @Override
    public NCp setSource(InputStream source) {
        this.source = source == null ? null : NInputSource.of(source);
        return this;
    }

    @Override
    public NCp setSource(File source) {
        this.source = source == null ? null : NPath.of(source);
        return this;
    }

    @Override
    public NCp setSource(Path source) {
        this.source = source == null ? null : NPath.of(source);
        return this;
    }

    @Override
    public NCp setSource(URL source) {
        this.source = source == null ? null : NPath.of(source);
        return this;
    }

    @Override
    public NCp setSource(String source) {
        this.source = source == null ? null : NPath.of(source);
        return this;
    }

    @Override
    public NCp setSource(byte[] source) {
        this.source = source == null ? null : NInputSource.of(source);
        return this;
    }

    @Override
    public NCp from(NInputSource source) {
        this.source = source;
        return this;
    }

    @Override
    public NCp from(NPath source) {
        this.source = source;
        return this;
    }

    @Override
    public NCp from(Reader source) {
        this.source = source == null ? null : new ReaderInputSource(workspace, source);
        return this;
    }

    @Override
    public NCp to(NOutputTarget target) {
        this.target = target;
        return this;
    }

    @Override
    public NCp to(Writer target) {
        this.target = target == null ? null : new WriterOutputTarget(workspace, target);
        return this;
    }

    @Override
    public NCp from(InputStream source) {
        return setSource(source);
    }

    @Override
    public NCp from(File source) {
        return setSource(source);
    }

    @Override
    public NCp from(Path source) {
        return setSource(source);
    }

    @Override
    public NCp from(URL source) {
        return setSource(source);
    }

    @Override
    public NCp from(byte[] source) {
        return setSource(source);
    }

    @Override
    public NOutputTarget getTarget() {
        return target;
    }

    @Override
    public NCp setTarget(OutputStream target) {
        this.target = target == null ? null : NOutputTarget.of(target);
        return this;
    }

    @Override
    public NCp setTarget(NPrintStream target) {
        this.target = target;
        return this;
    }

    @Override
    public NCp setTarget(NPath target) {
        this.target = target;
        return this;
    }

    @Override
    public NCp setTarget(Path target) {
        this.target = target == null ? null : NPath.of(target);
        return this;
    }

    @Override
    public NCp setTarget(File target) {
        this.target = target == null ? null : NPath.of(target);
        return this;
    }

    @Override
    public NCp setTarget(NOutputTarget target) {
        this.target = target;
        return this;
    }

    @Override
    public NCp setSource(NInputSource source) {
        this.source = source;
        return this;
    }

    @Override
    public NCp to(OutputStream target) {
        return setTarget(target);
    }

    @Override
    public NCp to(NPrintStream target) {
        return setTarget(target);
    }

    @Override
    public NCp to(Path target) {
        return setTarget(target);
    }

    @Override
    public NCp to(File target) {
        return setTarget(target);
    }

    @Override
    public NCp to(NPath target) {
        this.target = target;
        return this;
    }

    @Override
    public NCp addOptions(NPathOption... pathOptions) {
        if (pathOptions != null) {
            for (NPathOption o : pathOptions) {
                if (o != null) {
                    options.add(o);
                }
            }
        }
        return this;
    }

    @Override
    public NCp removeOptions(NPathOption... pathOptions) {
        if (pathOptions != null) {
            for (NPathOption o : pathOptions) {
                if (o != null) {
                    options.remove(o);
                }
            }
        }
        return this;
    }

    @Override
    public NCp clearOptions() {
        options.clear();
        return this;
    }

    @Override
    public Set<NPathOption> getOptions() {
        return new LinkedHashSet<>(options);
    }

    @Override
    public NCpValidator getValidator() {
        return checker;
    }

    @Override
    public DefaultNCp setValidator(NCpValidator checker) {
        this.checker = checker;
        return this;
    }

    @Override
    public boolean isRecursive() {
        return recursive;
    }

    @Override
    public NCp setRecursive(boolean recursive) {
        this.recursive = recursive;
        return this;
    }

    @Override
    public boolean isMkdirs() {
        return mkdirs;
    }

    @Override
    public NCp setMkdirs(boolean mkdirs) {
        this.mkdirs = mkdirs;
        return this;
    }

    @Override
    public byte[] getByteArrayResult() {
        NMemoryPrintStream b = NPrintStream.ofMem();
        to(b);
        removeOptions(NPathOption.SAFE);
        run();
        return b.getBytes();
    }

    @Override
    public String getStringResult() {
        return new String(getByteArrayResult());
    }

    @Override
    public NCp run() {
        NAssert.requireNonBlank(source, "source");
        NAssert.requireNonBlank(target, "target");

        NInputSource _source = source;
        if ((_source instanceof NPath) && ((NPath) _source).isDirectory()) {
            // this is a directory!!!
            if (!(target instanceof NPath)) {
                throw new NIllegalArgumentException(NMsg.ofC("unsupported copy of directory to %s", target));
            }
            Path fromPath = ((NPath) _source).toPath().get();
            Path toPath = ((NPath) target).toPath().get();
            CopyData cd = new CopyData();
            if (
                    options.contains(NPathOption.LOG)
                            || options.contains(NPathOption.TRACE)
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
    public NProgressFactory getProgressFactory() {
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
    public NCp setProgressFactory(NProgressFactory value) {
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
    public NCp setProgressMonitor(NProgressListener value) {
        this.progressMonitorFactory = value == null ? null : new SingletonNInputStreamProgressFactory(value);
        return this;
    }

    @Override
    public boolean isSkipRoot() {
        return skipRoot;
    }

    @Override
    public NCp setSkipRoot(boolean skipRoot) {
        this.skipRoot = skipRoot;
        return this;
    }

    public NCp interrupt() {
        if (interruptibleInstance != null) {
            interruptibleInstance.interrupt();
        }
        this.interrupted = true;
        return this;
    }

    public Object getSourceOrigin() {
        return sourceOrigin;
    }

    public NCp setSourceOrigin(Object sourceOrigin) {
        this.sourceOrigin = sourceOrigin;
        return this;
    }

    public NMsg getActionMessage() {
        return actionMsg;
    }

    public DefaultNCp setActionMessage(NMsg actionMsg) {
        this.actionMsg = actionMsg;
        return this;
    }

    public String getSourceTypeName() {
        return sourceTypeName;
    }

    public NCp setSourceTypeName(String sourceTypeName) {
        this.sourceTypeName = sourceTypeName;
        return this;
    }

    private void checkInterrupted() {
        if (interrupted) {
            throw new NInterruptException();
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
            throw new NIOException(exc);
        }
    }

    private void copyFolderWithMonitor(Path srcBase, Path targetBase, CopyData f) {
        long start = System.nanoTime();
        Object origin = getSourceOrigin();
        NProgressListener m = NProgressUtils.createProgressMonitor(
                NProgressUtils.MonitorType.DEFAULT, NPath.of(srcBase), origin, workspace,
                options.contains(NPathOption.LOG),
                options.contains(NPathOption.TRACE),
                getProgressFactory());
        NText srcBaseMessage = NText.of(srcBase);
        m.onProgress(NProgressEvent.ofStart(srcBase,
                NMsg.ofNtf(srcBaseMessage)
                , f.files + f.folders));
        try {
            Files.walkFileTree(srcBase, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    checkInterrupted();
                    f.doneFolders++;
                    NPath.of(transformPath(dir, srcBase, targetBase)).mkdirs();
                    m.onProgress(NProgressEvent.ofProgress(srcBase, NMsg.ofNtf(srcBaseMessage),
                            f.doneFiles + f.doneFolders, System.nanoTime() - start, null,
                            0, 0, f.files + f.folders, null));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    checkInterrupted();
                    f.doneFiles++;
                    copy(file, transformPath(file, srcBase, targetBase), options);
                    m.onProgress(NProgressEvent.ofProgress(srcBase, NMsg.ofNtf(srcBaseMessage),
                            f.doneFiles + f.doneFolders, System.nanoTime() - start,
                            null, 0, 0, f.files + f.folders, null));
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
            throw new NIOException(exc);
        } finally {
            m.onProgress(NProgressEvent.ofComplete(srcBase, NMsg.ofNtf(srcBaseMessage), f.files + f.folders,
                    System.nanoTime() - start, null, 0, 0,
                    f.files + f.folders, null));
        }
    }

    public Path copy(Path source, Path target, Set<NPathOption> options) throws IOException {
        if (options.contains(NPathOption.INTERRUPTIBLE)) {
            if (Files.exists(target)) {
                if (!options.contains(NPathOption.REPLACE_EXISTING)) {
                    return null;
                }
            }
            try (InputStream in = NInputSourceBuilder.of(Files.newInputStream(source)).setInterruptible(true).createInputStream()) {
                interruptibleInstance = (NInterruptible) in;
                try (OutputStream out = Files.newOutputStream(target)) {
                    transferTo(in, out);
                }
            }
            return target;
        }
        return Files.copy(source, target, CoreIOUtils.asCopyOptions(options).toArray(new CopyOption[0]));
    }

    public long copy(InputStream in, Path target, Set<NPathOption> options)
            throws IOException {
        if (options.contains(NPathOption.INTERRUPTIBLE)) {
            in = NInputSourceBuilder.of(in).setInterruptible(true).createInputStream();
            interruptibleInstance = (NInterruptible) in;
            try (OutputStream out = Files.newOutputStream(target)) {
                return transferTo(in, out);
            }
        }
        return Files.copy(in, target, CoreIOUtils.asCopyOptions(options).toArray(new CopyOption[0]));
    }

    public long copy(InputStream in, OutputStream out, Set<NPathOption> options)
            throws IOException {
        if (options.contains(NPathOption.INTERRUPTIBLE)) {
            in = NInputSourceBuilder.of(in).setInterruptible(true).createInputStream();
            interruptibleInstance = (NInterruptible) in;
            return transferTo(in, out);
        }
        return NIOUtils.copy(in, out);
    }
    public long copy(Reader in, Writer out, Set<NPathOption> options)
            throws IOException {
//        if (options.contains(NPathOption.INTERRUPTIBLE)) {
//            in = NInputSourceBuilder.of(in).setInterruptible(true).createInputStream();
//            interruptibleInstance = (NInterruptible) in;
//            return transferTo(in, out);
//        }
        return NIOUtils.copy(in, out);
    }

    public long copy(Path source, OutputStream out) throws IOException {
        if (options.contains(NPathOption.INTERRUPTIBLE)) {
            try (InputStream in = NInputSourceBuilder.of(Files.newInputStream(source)).setInterruptible(true).createInputStream()) {
                interruptibleInstance = (NInterruptible) in;
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
                    NPath.of(transformPath(dir, srcBase, targetBase)).mkdirs();
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
            throw new NIOException(exc);
        }
    }

    private void copyStream() {
        NAssert.requireNonBlank(source, "source");
        NAssert.requireNonBlank(target, "target");
        boolean safe = options.contains(NPathOption.SAFE);
        if (safe) {
            copyStreamSafe(source, target);
        } else {
            copyStreamOnce(source, target);
        }
    }

    private void copyStreamSafe(NInputSource source, NOutputTarget target) {
        if (source.isMultiRead()) {
            copyStreamMulti(source, target);
        } else {
            copyStreamOnce(source, target);
        }
    }

    private void copyStreamMulti(NInputSource source, NOutputTarget target) {
        int repeatCount = 1;
        int maxRepeatCount = this.maxRepeatCount;
        if (maxRepeatCount < 1) {
            maxRepeatCount = 3;
        }
        for (int i = repeatCount; i <= maxRepeatCount; i++) {
            try {
                NLogOp lop = _LOGOP();
                if (i > 1 && lop.isLoggable(Level.FINEST)) {
                    lop.level(Level.FINEST).verb(NLogVerb.START).log(NMsg.ofC("repeat download #%s %s",
                            i,
                            source));
                }
                copyStreamOnce(source, target);
                return;
            } catch (NIOException ex) {
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

    private NPath asValidSourcePath() {
        if (source != null) {
            if (source instanceof NPath) {
                NPath p = (NPath) source;
                if (p.isRegularFile()) {
                    return p;
                }
            }
        }
        return null;
    }

    private NPath asValidTargetPath() {
        if (target != null) {
            if (target instanceof NPath) {
                NPath p = (NPath) target;
                //if (p.isFile()) {
                return p;
                //}
            }
        }
        return null;
    }

    private void copyStreamOnce(NInputSource source, NOutputTarget target) {
        NSession session = workspace.currentSession();
        NAssert.requireNonNull(source, "source");
        NAssert.requireNonNull(target, "target");
        NOutputTarget2 _target2 = new NOutputTarget2(target);
        NInputSource2 _source2 = new NInputSource2(source);
        boolean safe = options.contains(NPathOption.SAFE);
        if (checker != null && _target2.jpath == null && !safe) {
            throw new NIllegalArgumentException(NMsg.ofNtf("unsupported validation if neither safeCopy is armed nor path is defined"));
        }
        NMsg loggedSrc = _source2.source.getMetaData().getMessage().orElse(NMsg.ofPlain("unknown-source"));
        NMsg loggedTarget = target.getMetaData().getMessage().orElse(NMsg.ofPlain("unknown-target"));
        NMsg m = getActionMessage();
        if (m == null) {
            m = NMsg.ofPlain("copy");
        }
        if (options.contains(NPathOption.LOG)) {
            session.getTerminal().printProgress(NMsg.ofC("%-14s %s to %s", m, loggedSrc, loggedTarget));
        }
        if (options.contains(NPathOption.LOG)
                || options.contains(NPathOption.TRACE)
                || getProgressFactory() != null
        ) {
            NInputStreamMonitor monitor = NInputStreamMonitor.of();
            monitor.setSource(_source2.source);
            monitor.setLogProgress(options.contains(NPathOption.LOG));
            monitor.setTraceProgress(options.contains(NPathOption.TRACE));
            monitor.setOrigin(getSourceOrigin());
            monitor.setSourceTypeName(getSourceTypeName());
            _source2.source = NInputSource.of(
                    monitor.setProgressFactory(getProgressFactory())
                            .setLength(_source2.source.getMetaData().getContentLength().orElse(-1L))
                            .setLogProgress(options.contains(NPathOption.LOG))
                            .create());
        }
        NLogOp lop = _LOGOP();
        if (lop.isLoggable(Level.FINEST)) {
            lop.level(Level.FINEST).verb(NLogVerb.START).log(NMsg.ofC("%s %s to %s",
                    m,
                    loggedSrc,
                    loggedTarget));
        }
        try {
            if (safe) {
                Path temp = null;
                if (_target2.jpath != null) {
                    NPath.of(_target2.jpath).mkParentDirs();
                    temp = _target2.jpath.resolveSibling(_target2.jpath.getFileName() + "~");
                } else {
                    temp = NPath.ofTempFile("temp~").toPath().get();
                }
                try {
                    if (_source2.jpath != null) {
                        copy(_source2.jpath, temp, new HashSet<>(Collections.singletonList(NPathOption.REPLACE_EXISTING)));
                    } else {
                        try (InputStream ins = _source2.source.getInputStream()) {
                            copy(ins, temp, new HashSet<>(Collections.singletonList(NPathOption.REPLACE_EXISTING)));
                        }
                    }
                    _validate(temp);
                    if (_target2.jpath != null) {
                        try {
                            Files.move(temp, _target2.jpath, StandardCopyOption.REPLACE_EXISTING);
                        } catch (FileSystemException e) {
                            // happens when the file is used by another process
                            // in that case try to check if the file needs to be copied
                            //if not, return safely!
                            if (NIOUtils.compareContent(temp, _target2.jpath)) {
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
                if (_target2.jpath != null) {
                    Path to = _target2.jpath;
                    NPath.of(to).mkParentDirs();
                    if (_source2.jpath != null) {
                        copy(_source2.jpath, to, new HashSet<>(Collections.singletonList(NPathOption.REPLACE_EXISTING)));
                    } else {
                        try (InputStream ins = _source2.source.getInputStream()) {
                            copy(ins, to, new HashSet<>(Collections.singletonList(NPathOption.REPLACE_EXISTING)));
                        }
                    }
                    _validate(to);
                } else {
                    ByteArrayOutputStream bos = null;
                    if (checker != null) {
                        bos = new ByteArrayOutputStream();
                        if (_source2.jpath != null) {
                            copy(_source2.jpath, bos);
                        } else {
                            try (InputStream ins = _source2.source.getInputStream()) {
                                copy(ins, bos, options);
                            }
                        }
                        try (OutputStream ops = target.getOutputStream()) {
                            copy(new ByteArrayInputStream(bos.toByteArray()), ops, options);
                        }
                        _validate(bos.toByteArray());
                    } else {
                        if (_source2.jpath != null) {
                            try (OutputStream ops = target.getOutputStream()) {
                                copy(_source2.jpath, ops);
                            }
                        } else {
                            try (InputStream ins = _source2.source.getInputStream()) {
                                try (OutputStream ops = target.getOutputStream()) {
                                    copy(ins, ops, options);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            lop.level(Level.CONFIG).verb(NLogVerb.FAIL)
                    .log(NMsg.ofC("error copying %s to %s : %s", _source2.source, target, ex));
            throw new NIOException(ex);
        }
    }

    private void _validate(Path temp) {
        if (checker != null) {
            try (InputStream in = Files.newInputStream(temp)) {
                checker.validate(in);
            } catch (NCpValidatorException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new NCpValidatorException(NMsg.ofC("validate file %s failed", temp), ex);
            }
        }
    }

    private void _validate(byte[] temp) {
        if (checker != null) {
            try (InputStream in = new ByteArrayInputStream(temp)) {
                checker.validate(in);
            } catch (NCpValidatorException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new NCpValidatorException(NMsg.ofPlain("validate file failed"), ex);
            }
        }
    }

    private static class NOutputTarget2 {
        NOutputTarget target;
        NPath path;
        Path jpath;

        public NOutputTarget2(NOutputTarget target) {
            this.target = target;
            this.path = (this.target instanceof NPath) ? (NPath) this.target : null;
            if (this.path != null && this.path.isLocal()) {
                this.jpath = this.path.toPath().orNull();
            }
        }
    }

    private static class NInputSource2 {
        NInputSource source;
        NPath path;
        Path jpath;

        public NInputSource2(NInputSource source) {
            this.source = source;
            this.path = (this.source instanceof NPath) ? (NPath) this.source : null;
            if (this.path != null && this.path.isLocal()) {
                this.jpath = this.path.toPath().orNull();
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
