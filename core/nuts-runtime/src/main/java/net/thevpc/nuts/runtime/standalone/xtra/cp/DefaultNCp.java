/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.xtra.cp;

import net.thevpc.nuts.concurrent.NInterruptedException;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.text.NI18n;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.runtime.standalone.xtra.time.NProgressUtils;
import net.thevpc.nuts.runtime.standalone.xtra.web.DefaultNHttpClient;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.runtime.standalone.xtra.time.SingletonNInputStreamProgressFactory;
import net.thevpc.nuts.runtime.standalone.io.util.*;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.mon.NChronometer;
import net.thevpc.nuts.mon.NProgressEvent;
import net.thevpc.nuts.mon.NProgressFactory;
import net.thevpc.nuts.mon.NProgressListener;
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
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNCp implements NCp {

    private NCpValidator checker;
    private boolean skipRoot = false;
    private final int maxRepeatCount = 3;
    private Boolean segmented;
    private int segmentedConnections;
    private long segmentedChunkSize;
    private long segmentedMinSize = -1;
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
    private final Set<NPathOption> options = new LinkedHashSet<>();

    public DefaultNCp() {
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
        throw new NIllegalArgumentException(NMsg.ofC("Invalid path %s", f));
    }

    protected NLog _LOG() {
        return NLog.of(DefaultNCp.class);
    }

    @Override
    public NInputSource source() {
        return source;
    }

    @Override
    public NCp source(NPath source) {
        this.source = source;
        return this;
    }

    @Override
    public NCp source(InputStream source) {
        this.source = source == null ? null : NInputSource.of(source);
        return this;
    }

    @Override
    public NCp source(File source) {
        this.source = source == null ? null : NPath.of(source);
        return this;
    }

    @Override
    public NCp source(Path source) {
        this.source = source == null ? null : NPath.of(source);
        return this;
    }

    @Override
    public NCp source(URL source) {
        this.source = source == null ? null : NPath.of(source);
        return this;
    }

    @Override
    public NCp source(String source) {
        this.source = source == null ? null : NPath.of(source);
        return this;
    }

    @Override
    public NCp source(byte[] source) {
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
        this.source = source == null ? null : new ReaderInputSource(NWorkspace.of(), source);
        return this;
    }

    @Override
    public NCp to(NOutputTarget target) {
        this.target = target;
        return this;
    }

    @Override
    public NCp to(Writer target) {
        this.target = target == null ? null : new WriterOutputTarget(target);
        return this;
    }

    @Override
    public NCp from(InputStream source) {
        return source(source);
    }

    @Override
    public NCp from(File source) {
        return source(source);
    }

    @Override
    public NCp from(Path source) {
        return source(source);
    }

    @Override
    public NCp from(URL source) {
        return source(source);
    }

    @Override
    public NCp from(byte[] source) {
        return source(source);
    }

    @Override
    public NOutputTarget target() {
        return target;
    }

    @Override
    public NCp target(OutputStream target) {
        this.target = target == null ? null : NOutputTarget.of(target);
        return this;
    }

    @Override
    public NCp target(NPrintStream target) {
        this.target = target;
        return this;
    }

    @Override
    public NCp target(NPath target) {
        this.target = target;
        return this;
    }

    @Override
    public NCp target(Path target) {
        this.target = target == null ? null : NPath.of(target);
        return this;
    }

    @Override
    public NCp target(File target) {
        this.target = target == null ? null : NPath.of(target);
        return this;
    }

    @Override
    public NCp target(NOutputTarget target) {
        this.target = target;
        return this;
    }

    @Override
    public NCp source(NInputSource source) {
        this.source = source;
        return this;
    }

    @Override
    public NCp to(OutputStream target) {
        return target(target);
    }

    @Override
    public NCp to(NPrintStream target) {
        return target(target);
    }

    @Override
    public NCp to(Path target) {
        return target(target);
    }

    @Override
    public NCp to(File target) {
        return target(target);
    }

    @Override
    public NCp to(NPath target) {
        this.target = target;
        return this;
    }

    @Override
    public NCp options(NPathOption... pathOptions) {
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
    public Set<NPathOption> options() {
        return new LinkedHashSet<>(options);
    }

    @Override
    public NCpValidator validator() {
        return checker;
    }

    @Override
    public DefaultNCp validator(NCpValidator checker) {
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
    public NCp mkdirs(boolean mkdirs) {
        this.mkdirs = mkdirs;
        return this;
    }

    @Override
    public byte[] byteArrayResult() {
        NMemoryPrintStream b = NPrintStream.ofMem();
        to(b);
        removeOptions(NPathOption.SAFE);
        run();
        return b.bytes();
    }

    @Override
    public String stringResult() {
        return new String(byteArrayResult());
    }

    @Override
    public NCp run() {
        NAssert.requireNamedNonBlank(source, "source");
        NAssert.requireNamedNonBlank(target, "target");

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
                            || progressFactory() != null
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
    public NProgressFactory progressFactory() {
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
    public NCp progressFactory(NProgressFactory value) {
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
    public NCp progressMonitor(NProgressListener value) {
        this.progressMonitorFactory = value == null ? null : new SingletonNInputStreamProgressFactory(value);
        return this;
    }

    @Override
    public Boolean segmented() {
        return segmented;
    }

    @Override
    public NCp segmented(Boolean segmented) {
        this.segmented = segmented;
        return this;
    }

    @Override
    public int segmentedConnections() {
        return segmentedConnections;
    }

    @Override
    public NCp segmentedConnections(int connections) {
        this.segmentedConnections = Math.max(0, connections);
        return this;
    }

    @Override
    public long segmentedChunkSize() {
        return segmentedChunkSize;
    }

    @Override
    public NCp segmentedChunkSize(long chunkSize) {
        this.segmentedChunkSize = Math.max(0, chunkSize);
        return this;
    }

    @Override
    public long segmentedMinSize() {
        return segmentedMinSize;
    }

    @Override
    public NCp segmentedMinSize(long minSize) {
        this.segmentedMinSize = minSize < 0 ? -1 : minSize;
        return this;
    }

    @Override
    public boolean isSkipRoot() {
        return skipRoot;
    }

    @Override
    public NCp skipRoot(boolean skipRoot) {
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

    public Object sourceOrigin() {
        return sourceOrigin;
    }

    public NCp sourceOrigin(Object sourceOrigin) {
        this.sourceOrigin = sourceOrigin;
        return this;
    }

    public NMsg actionMessage() {
        return actionMsg;
    }

    public DefaultNCp actionMessage(NMsg actionMsg) {
        this.actionMsg = actionMsg;
        return this;
    }

    public String sourceTypeName() {
        return sourceTypeName;
    }

    public NCp sourceTypeName(String sourceTypeName) {
        this.sourceTypeName = sourceTypeName;
        return this;
    }

    private void checkInterrupted() {
        if (interrupted) {
            throw new NInterruptedException();
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
        Object origin = sourceOrigin();
        NProgressListener m = NProgressUtils.createProgressMonitor(
                NProgressUtils.MonitorType.DEFAULT, NPath.of(srcBase), origin, NWorkspace.of(),
                options.contains(NPathOption.LOG),
                options.contains(NPathOption.TRACE),
                progressFactory());
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
            try (InputStream in = NInputSourceBuilder.of(Files.newInputStream(source)).interruptible(true).createInputStream()) {
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
            in = NInputSourceBuilder.of(in).interruptible(true).createInputStream();
            interruptibleInstance = (NInterruptible) in;
            try (OutputStream out = Files.newOutputStream(target)) {
                return transferTo(in, out);
            }
        }
        return CoreIOUtils.copyWithBuffer(in, target, CoreIOUtils.asCopyOptions(options).toArray(new CopyOption[0]));
    }

    public long copy(InputStream in, OutputStream out, Set<NPathOption> options)
            throws IOException {
        if (options.contains(NPathOption.INTERRUPTIBLE)) {
            in = NInputSourceBuilder.of(in).interruptible(true).createInputStream();
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
            try (InputStream in = NInputSourceBuilder.of(Files.newInputStream(source)).interruptible(true).createInputStream()) {
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
        NAssert.requireNamedNonNull(out, "out");
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
        NAssert.requireNamedNonBlank(source, "source");
        NAssert.requireNamedNonBlank(target, "target");
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
                if (i > 1 && _LOG().isLoggable(Level.FINEST)) {
                    _LOG().log(NMsg.ofC("repeat download #%s %s",
                            i,
                            source).asFinest().withIntent(NMsgIntent.START));
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
        NSession session = NSession.of();
        NAssert.requireNamedNonNull(source, "source");
        NAssert.requireNamedNonNull(target, "target");
        NOutputTarget2 _target2 = new NOutputTarget2(target);
        NInputSource2 _source2 = new NInputSource2(source);
        boolean safe = options.contains(NPathOption.SAFE);
        if (checker != null && _target2.jpath == null && !safe) {
            throw new NIllegalArgumentException(NMsg.ofNtf("unsupported validation if neither safeCopy is armed nor path is defined"));
        }
        NMsg loggedSrc = _source2.source.metaData().message().orElse(NMsg.ofP("unknown-source"));
        NMsg loggedTarget = target.metaData().message().orElse(NMsg.ofP("unknown-target"));
        NMsg m = actionMessage();
        if (m == null) {
            m = NMsg.ofP("copy");
        }
        if (options.contains(NPathOption.LOG)) {
            session.terminal().printProgress(NMsg.ofC("%-14s %s to %s", m, loggedSrc, loggedTarget));
        }
        NSegmentedHttpDownload.Options segmented = (safe && _target2.jpath != null) ? segmentedDownloadOptions(_source2) : null;
        if (segmented == null) {
            monitorSource(_source2);
        }
        NChronometer chrono = NChronometer.of();
        if (_LOG().isLoggable(Level.FINEST)) {
            _LOG().log(NMsg.ofC("%s %s to %s",
                    m,
                    loggedSrc,
                    loggedTarget).asFinest().withIntent(NMsgIntent.START));
        }
        try {
            if (safe) {
                Path temp = null;
                if (_target2.jpath != null) {
                    NPath.of(_target2.jpath).mkParentDirs();
                    temp = _target2.jpath.resolveSibling(_target2.jpath.getFileName() + "." + UUID.randomUUID() + ".part~");
                } else {
                    temp = NPath.ofTempFile("temp~").toPath().get();
                }
                try {
                    boolean copied = false;
                    if (segmented != null) {
                        copied = copySegmented(_source2, temp, segmented);
                        if (!copied) {
                            monitorSource(_source2);
                        }
                    }
                    if (copied) {
                        // already in temp
                    } else if (_source2.jpath != null) {
                        copy(_source2.jpath, temp, new HashSet<>(Collections.singletonList(NPathOption.REPLACE_EXISTING)));
                    } else {
                        try (InputStream ins = _source2.source.inputStream()) {
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
                            if (Files.exists(_target2.jpath) && (temp == null || !Files.exists(temp) || NIOUtils.compareContent(temp, _target2.jpath))) {
                                //cannot write the file (used by another process), but no pbm because does not need to
                                return;
                            }
                            throw e;
                        }
                        temp = null;
                    } else {
                        try (OutputStream ops = target.outputStream()) {
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
                        try (InputStream ins = _source2.source.inputStream()) {
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
                            try (InputStream ins = _source2.source.inputStream()) {
                                copy(ins, bos, options);
                            }
                        }
                        try (OutputStream ops = target.outputStream()) {
                            copy(new ByteArrayInputStream(bos.toByteArray()), ops, options);
                        }
                        _validate(bos.toByteArray());
                    } else {
                        if (_source2.jpath != null) {
                            try (OutputStream ops = target.outputStream()) {
                                copy(_source2.jpath, ops);
                            }
                        } else {
                            try (InputStream ins = _source2.source.inputStream()) {
                                try (OutputStream ops = target.outputStream()) {
                                    copy(ins, ops, options);
                                }
                            }
                        }
                    }
                }
            }
            _LOG()

                    .log(NMsg.ofC(NI18n.of("%s %s to %s"), m, _source2.source, loggedTarget)
                            .withLevel(Level.CONFIG).withIntent(NMsgIntent.SUCCESS)
                            .withDurationMillis(chrono.durationMs())
                    );
        } catch (IOException ex) {
            _LOG()

                    .log(NMsg.ofC("error %s %s to %s : %s", m, _source2.source, loggedTarget, ex)
                            .withLevel(Level.CONFIG).withIntent(NMsgIntent.FAIL)
                            .withDurationMillis(chrono.durationMs())
                    );
            throw new NIOException(ex);
        }
    }


    private boolean isMonitored() {
        return options.contains(NPathOption.LOG)
                || options.contains(NPathOption.TRACE)
                || progressFactory() != null;
    }

    private void monitorSource(NInputSource2 s) {
        if (isMonitored()) {
            NInputStreamMonitor monitor = NInputStreamMonitor.of();
            monitor.source(s.source);
            monitor.logProgress(options.contains(NPathOption.LOG));
            monitor.traceProgress(options.contains(NPathOption.TRACE));
            monitor.origin(sourceOrigin());
            monitor.sourceTypeName(sourceTypeName());
            s.source = NInputSource.of(
                    monitor.progressFactory(progressFactory())
                            .length(s.source.metaData().contentLength().orElse(-1L))
                            .logProgress(options.contains(NPathOption.LOG))
                            .create());
        }
    }

    /**
     * segmented download options when {@code s} is a large enough http(s)
     * resource and segmenting is enabled, null otherwise. Values set on this
     * instance ({@link #segmented(Boolean)}, {@link #segmentedConnections(int)},
     * ...) win over the workspace defaults, given by the custom options
     * {@code ---download-connections} (default 4, 1 disables),
     * {@code ---download-chunk-size} (default 4m) and
     * {@code ---download-min-size} (default 8m).
     */
    private NSegmentedHttpDownload.Options segmentedDownloadOptions(NInputSource2 s) {
        if (Boolean.FALSE.equals(segmented) || s.path == null || !s.path.isHttp()) {
            return null;
        }
        NWorkspace ws = NWorkspace.of();
        NSegmentedHttpDownload.Options o = new NSegmentedHttpDownload.Options();
        int builtinConnections = o.connections;
        if (segmentedConnections > 0) {
            o.connections = segmentedConnections;
        } else {
            o.connections = ws.getCustomBootOption("---download-connections")
                    .flatMap(NLiteral::asInt).orElse(o.connections);
            if (o.connections < 2 && Boolean.TRUE.equals(segmented)) {
                // disabled by default but explicitly requested
                o.connections = builtinConnections;
            }
        }
        if (o.connections < 2) {
            return null;
        }
        o.chunkSize = segmentedChunkSize > 0 ? segmentedChunkSize
                : NSegmentedHttpDownload.parseSize(ws.getCustomBootOption("---download-chunk-size")
                .flatMap(NLiteral::asString).orNull(), o.chunkSize);
        o.minSize = segmentedMinSize >= 0 ? segmentedMinSize
                : NSegmentedHttpDownload.parseSize(ws.getCustomBootOption("---download-min-size")
                .flatMap(NLiteral::asString).orNull(), o.minSize);
        URL url = s.path.toURL().orNull();
        long length;
        try {
            length = s.path.contentLength();
        } catch (Exception ex) {
            return null;
        }
        if (!NSegmentedHttpDownload.isCandidate(url, length, o)) {
            return null;
        }
        NDuration ct = DefaultNHttpClient.getGlobalConnectionTimeoutOrDefault();
        NDuration rt = DefaultNHttpClient.getGlobalReadConnectionTimeoutOrDefault();
        o.connectTimeoutMs = ct == null ? 0 : (int) Math.min(Integer.MAX_VALUE, ct.toMillis());
        o.readTimeoutMs = rt == null ? 0 : (int) Math.min(Integer.MAX_VALUE, rt.toMillis());
        o.userAgent = "nhttpclient/" + ws.runtimeId().version();
        return o;
    }

    /**
     * @return false when the server does not support (or stopped honouring)
     * ranges and a plain download is needed
     */
    private boolean copySegmented(NInputSource2 s, Path temp, NSegmentedHttpDownload.Options o) throws IOException {
        URL url = s.path.toURL().get();
        NProgressListener listener = isMonitored()
                ? NProgressUtils.createProgressMonitor(NProgressUtils.MonitorType.STREAM, s.source, sourceOrigin(), NWorkspace.of(),
                options.contains(NPathOption.LOG),
                options.contains(NPathOption.TRACE),
                progressFactory())
                : null;
        NMsg name = NMsg.ofNtf(NText.of(s.source));
        Object src = s.source;
        NSegmentedHttpDownload.Result r = NSegmentedHttpDownload.download(url, temp, o, new NSegmentedHttpDownload.Progress() {
            long startTime;
            long lastTime;
            long lastCount;

            @Override
            public void onStart(long length) {
                startTime = lastTime = System.nanoTime();
                if (listener != null) {
                    listener.onProgress(NProgressEvent.ofStart(src, name, length));
                }
            }

            @Override
            public void onProgress(long done, long length) {
                long now = System.nanoTime();
                if (listener != null && listener.onProgress(NProgressEvent.ofProgress(src, name,
                        done, now - startTime, null, done - lastCount, now - lastTime, length, null))) {
                    lastCount = done;
                    lastTime = now;
                }
            }

            @Override
            public void onComplete(long done, long length, Throwable error) {
                long now = System.nanoTime();
                if (listener != null) {
                    listener.onProgress(NProgressEvent.ofComplete(src, name,
                            done, now - startTime, null, done - lastCount, now - lastTime, length, error));
                }
            }
        });
        if (_LOG().isLoggable(Level.FINEST)) {
            _LOG().log(NMsg.ofC("download %s : %s", url, r).asFinest());
        }
        return r != NSegmentedHttpDownload.Result.FALLBACK;
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
                throw new NCpValidatorException(NMsg.ofP("validate file failed"), ex);
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
