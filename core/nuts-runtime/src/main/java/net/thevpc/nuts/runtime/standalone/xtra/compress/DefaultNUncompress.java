/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.xtra.compress;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.runtime.standalone.io.progress.SingletonNInputStreamProgressFactory;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.spi.NUncompressPackaging;
import net.thevpc.nuts.time.NProgressFactory;
import net.thevpc.nuts.time.NProgressListener;
import net.thevpc.nuts.util.*;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * @author thevpc
 */
public class DefaultNUncompress implements NUncompress {

    private NLog LOG;

    private boolean skipRoot = false;
    private boolean safe = true;
    private String packaging = "zip";
    private NWorkspace ws;
    private NInputSource source;
    private NOutputTarget target;
    private NSession session;
    private NProgressFactory progressFactory;
    private Set<NPathOption> options = new LinkedHashSet<>();
    private NUncompressVisitor visitor;
    private NUncompressPackaging packagingImpl;

    public DefaultNUncompress(NSession session) {
        this.session = session;
        this.ws = session.getWorkspace();
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    protected NLogOp _LOGOP(NSession session) {
        return _LOG(session).with().session(session);
    }

    protected NLog _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLog.of(DefaultNUncompress.class, session);
        }
        return LOG;
    }

    @Override
    public String getPackaging() {
        return packaging;
    }

    @Override
    public NUncompress setPackaging(String packaging) {
        checkSession();
        this.packagingImpl = NExtensions.of(session).createComponent(NUncompressPackaging.class, this).get();
        return this;
    }

    @Override
    public NInputSource getSource() {
        return source;
    }

    protected void checkSession() {
        NSessionUtils.checkSession(ws, session);
    }

    @Override
    public NUncompress setSource(NInputSource source) {
        this.source = source;
        return this;
    }

    @Override
    public NUncompress setTarget(NOutputTarget target) {
        this.target = target;
        return this;
    }


    @Override
    public NUncompress setSource(InputStream source) {
        checkSession();
        this.source = source == null ? null : NIO.of(session).ofInputSource(source);
        return this;
    }

    @Override
    public NUncompress setSource(NPath source) {
        checkSession();
        this.source = source;
        return this;
    }

    @Override
    public NUncompress setSource(File source) {
        this.source = source == null ? null : NPath.of(source, session);
        return this;
    }

    @Override
    public NUncompress setSource(Path source) {
        this.source = source == null ? null : NPath.of(source, session);
        return this;
    }

    @Override
    public NUncompress setSource(URL source) {
        this.source = source == null ? null : NPath.of(source, session);
        return this;
    }

    @Override
    public NUncompress setTarget(Path target) {
        this.target = target == null ? null : NPath.of(target, session);
        return this;
    }

    @Override
    public NUncompress setTarget(File target) {
        this.target = target == null ? null : NPath.of(target, session);
        return this;
    }

    public NUncompress setTarget(NPath target) {
        this.target = target;
        return this;
    }

    @Override
    public NUncompress from(NPath source) {
        this.source = source;
        return this;
    }

    @Override
    public NUncompress to(NPath target) {
        this.target = target;
        return this;
    }

    @Override
    public NOutputTarget getTarget() {
        return target;
    }

    //    @Override


    @Override
    public NUncompress from(InputStream source) {
        return setSource(source);
    }

    @Override
    public NUncompress from(File source) {
        return setSource(source);
    }

    @Override
    public NUncompress from(Path source) {
        return setSource(source);
    }

    @Override
    public NUncompress from(URL source) {
        return setSource(source);
    }

    @Override
    public NUncompress to(File target) {
        return setTarget(target);
    }

    @Override
    public NUncompress to(Path target) {
        return setTarget(target);
    }

    @Override
    public boolean isSafe() {
        return safe;
    }

    @Override
    public DefaultNUncompress setSafe(boolean value) {
        this.safe = value;
        return this;
    }

    @Override
    public NSession getSession() {
        return session;
    }

    @Override
    public NUncompress setSession(NSession session) {
        this.session = NWorkspaceUtils.bindSession(ws, session);
        return this;
    }

    @Override
    public NUncompress run() {
        checkSession();
        CompressType compressType = toCompressType(getPackaging());
        NAssert.requireNonNull(source, "source", getSession());

        NInputSource _source = source;
        if (options.contains(NPathOption.LOG)
                || options.contains(NPathOption.TRACE)
                || getProgressFactory() != null) {
            NInputStreamMonitor monitor = NInputStreamMonitor.of(session);
            monitor.setOrigin(source);
            monitor.setLogProgress(options.contains(NPathOption.LOG));
            monitor.setTraceProgress(options.contains(NPathOption.TRACE));
            monitor.setProgressFactory(getProgressFactory());
            monitor.setSource(source);
            _source = NIO.of(session).ofInputSource(monitor.create());
        }

        if (visitor == null && target == null) {
            NAssert.requireNonNull(target, "target", getSession());
        } else if (visitor != null && target != null) {
            throw new NIllegalArgumentException(getSession(), NMsg.ofC("invalid target %s when visitor is specified", target));
        } else if (visitor != null) {
            return runVisitor(_source, compressType);
        }

        _LOGOP(session).level(Level.FINEST).verb(NLogVerb.START)
                .log(NMsg.ofJ("uncompress {0} to {1}", _source, target));

        if (packagingImpl == null) {
            this.packagingImpl = NExtensions.of(session).createComponent(NUncompressPackaging.class, this).get();
        }
        packagingImpl.uncompressPackage(this, _source);
        return this;
    }


    @Override
    public NUncompress visit(NUncompressVisitor visitor) {
        this.visitor = visitor;
        return this;
    }

    private CompressType toCompressType(String format) {
        if (NBlankable.isBlank(format)) {
            format = "zip";
        }
        switch (format) {
            case "zip": {
                return CompressType.ZIP;
            }
            case "gzip":
            case "gz": {
                return CompressType.GZIP;
            }
        }
        throw new NUnsupportedArgumentException(getSession(), NMsg.ofC("unsupported format %s", format));
    }

    private NUncompress runVisitor(NInputSource source, CompressType format) {
        switch (format) {
            case ZIP: {
                new NUncompressZip().visitPackage(this, source, visitor);
                break;
            }
            case GZIP: {
                new NUncompressGzip().visitPackage(this, source, visitor);
                break;
            }
            default: {
                throw new NUnsupportedArgumentException(getSession(), NMsg.ofC("unsupported format %s", format));
            }
        }
        return this;
    }


    /**
     * return progress factory responsible for creating progress monitor
     *
     * @return progress factory responsible for creating progress monitor
     * @since 0.5.8
     */
    @Override
    public NProgressFactory getProgressFactory() {
        return progressFactory;
    }

    /**
     * set progress factory responsible of creating progress monitor
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NUncompress setProgressFactory(NProgressFactory value) {
        this.progressFactory = value;
        return this;
    }

    /**
     * set progress monitor. Will create a singeleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NUncompress setProgressMonitor(NProgressListener value) {
        this.progressFactory = value == null ? null : new SingletonNInputStreamProgressFactory(value);
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
    public NUncompress progressMonitor(NProgressListener value) {
        return setProgressMonitor(value);
    }

    @Override
    public boolean isSkipRoot() {
        return skipRoot;
    }

    @Override
    public NUncompress setSkipRoot(boolean value) {
        this.skipRoot = value;
        return this;
    }

    @Override
    public NUncompress setFormatOption(String option, Object value) {
        return this;
    }

    @Override
    public Object getFormatOption(String option) {
        return null;
    }

    @Override
    public NUncompress addOptions(NPathOption... pathOptions) {
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
    public NUncompress removeOptions(NPathOption... pathOptions) {
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
    public NUncompress clearOptions() {
        options.clear();
        return this;
    }

    @Override
    public Set<NPathOption> getOptions() {
        return new LinkedHashSet<>(options);
    }

    private enum CompressType {
        ZIP,
        GZIP
    }

}
