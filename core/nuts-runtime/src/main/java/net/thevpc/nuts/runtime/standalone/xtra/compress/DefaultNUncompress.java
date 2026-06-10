/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.xtra.compress;

import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NCompressPackaging;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.log.NLog;

import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.runtime.standalone.xtra.time.SingletonNInputStreamProgressFactory;
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
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNUncompress implements NUncompress {

    private NLog LOG;

    private boolean skipRoot = false;
    private boolean safe = true;
    private String packaging = "zip";
    private NInputSource source;
    private NOutputTarget target;
    private NProgressFactory progressFactory;
    private Set<NPathOption> options = new LinkedHashSet<>();
    private NUncompressVisitor visitor;
    private NUncompressPackaging packagingImpl;

    public DefaultNUncompress() {
    }

    protected NLog _LOG() {
        if (LOG == null) {
            LOG = NLog.of(DefaultNUncompress.class);
        }
        return LOG;
    }

    @Override
    public String packaging() {
        return packaging;
    }

    @Override
    public NUncompress packaging(String packaging) {
        if (NBlankable.isBlank(packaging)) {
            packaging = "zip";
        }
        this.packagingImpl = NWorkspaceExt.of().getModel().extensionCatalogManager.createSupported(NUncompressPackaging.class, packaging,"net.thevpc.nuts.spi.compression", packaging.toLowerCase()).get();
        return this;
    }

    @Override
    public NInputSource source() {
        return source;
    }

    @Override
    public NUncompress source(NInputSource source) {
        this.source = source;
        return this;
    }

    @Override
    public NUncompress target(NOutputTarget target) {
        this.target = target;
        return this;
    }


    @Override
    public NUncompress source(InputStream source) {
        this.source = source == null ? null : NInputSource.of(source);
        return this;
    }

    @Override
    public NUncompress source(NPath source) {
        this.source = source;
        return this;
    }

    @Override
    public NUncompress source(File source) {
        this.source = source == null ? null : NPath.of(source);
        return this;
    }

    @Override
    public NUncompress source(Path source) {
        this.source = source == null ? null : NPath.of(source);
        return this;
    }

    @Override
    public NUncompress source(URL source) {
        this.source = source == null ? null : NPath.of(source);
        return this;
    }

    @Override
    public NUncompress target(Path target) {
        this.target = target == null ? null : NPath.of(target);
        return this;
    }

    @Override
    public NUncompress target(File target) {
        this.target = target == null ? null : NPath.of(target);
        return this;
    }

    public NUncompress target(NPath target) {
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
    public NOutputTarget target() {
        return target;
    }

    //    @Override


    @Override
    public NUncompress from(InputStream source) {
        return source(source);
    }

    @Override
    public NUncompress from(File source) {
        return source(source);
    }

    @Override
    public NUncompress from(Path source) {
        return source(source);
    }

    @Override
    public NUncompress from(URL source) {
        return source(source);
    }

    @Override
    public NUncompress to(File target) {
        return target(target);
    }

    @Override
    public NUncompress to(Path target) {
        return target(target);
    }

    @Override
    public boolean isSafe() {
        return safe;
    }

    @Override
    public DefaultNUncompress safe(boolean value) {
        this.safe = value;
        return this;
    }

    @Override
    public NUncompress run() {
        CompressType compressType = toCompressType(packaging());
        NAssert.requireNamedNonNull(source, "source");

        NInputSource _source = source;
        if (options.contains(NPathOption.LOG)
                || options.contains(NPathOption.TRACE)
                || progressFactory() != null) {
            NInputStreamMonitor monitor = NInputStreamMonitor.of();
            monitor.origin(source);
            monitor.logProgress(options.contains(NPathOption.LOG));
            monitor.traceProgress(options.contains(NPathOption.TRACE));
            monitor.progressFactory(progressFactory());
            monitor.source(source);
            _source = NInputSource.of(monitor.create());
        }

        if (visitor == null && target == null) {
            NAssert.requireNamedNonNull(target, "target");
        } else if (visitor != null && target != null) {
            throw new NIllegalArgumentException(NMsg.ofC("invalid target %s when visitor is specified", target));
        } else if (visitor != null) {
            return runVisitor(_source, compressType);
        }

        _LOG()
                .log(NMsg.ofC("uncompress %s to %s", _source, target).asFine().withIntent(NMsgIntent.START)
                        .withLevel(Level.FINEST).withIntent(NMsgIntent.START)
                );

        if (packagingImpl == null) {
            this.packagingImpl = NExtensions.of().createSupported(NUncompressPackaging.class, this).get();
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
        throw new NUnsupportedArgumentException(NMsg.ofC("unsupported format %s", format));
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
                throw new NUnsupportedArgumentException(NMsg.ofC("unsupported format %s", format));
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
    public NProgressFactory progressFactory() {
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
    public NUncompress progressFactory(NProgressFactory value) {
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
    public NUncompress progressMonitor(NProgressListener value) {
        this.progressFactory = value == null ? null : new SingletonNInputStreamProgressFactory(value);
        return this;
    }

    @Override
    public boolean isSkipRoot() {
        return skipRoot;
    }

    @Override
    public NUncompress skipRoot(boolean value) {
        this.skipRoot = value;
        return this;
    }

    @Override
    public NUncompress formatOption(String option, Object value) {
        return this;
    }

    @Override
    public Object formatOption(String option) {
        return null;
    }

    @Override
    public NUncompress options(NPathOption... pathOptions) {
        return addOptions(pathOptions);
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
    public Set<NPathOption> options() {
        return new LinkedHashSet<>(options);
    }

    private enum CompressType {
        ZIP,
        GZIP
    }

}
