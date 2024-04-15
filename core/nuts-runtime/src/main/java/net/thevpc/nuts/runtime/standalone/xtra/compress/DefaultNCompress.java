/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.xtra.compress;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.progress.SingletonNInputStreamProgressFactory;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NCompressPackaging;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NFunction;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.time.NProgressFactory;
import net.thevpc.nuts.time.NProgressListener;
import net.thevpc.nuts.util.NMsg;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author thevpc
 */
public class DefaultNCompress implements NCompress {

    private final List<NInputSource> sources = new ArrayList<>();
    private final NWorkspace ws;
    private NLog LOG;
    private boolean safe = true;
    private NOutputTarget target;
    private NSession session;
    private boolean skipRoot;
    private NProgressFactory progressFactory;
    private String packaging = "zip";
    private NCompressPackaging packagingImpl;
    private Set<NPathOption> options = new LinkedHashSet<>();

    public DefaultNCompress(NSession session) {
        this.session = session;
        this.ws = session.getWorkspace();
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    protected NLog _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLog.of(DefaultNCompress.class, session);
        }
        return LOG;
    }

    private void checkSession() {
        NSessionUtils.checkSession(ws, session);
    }


    @Override
    public NCompress setFormatOption(String option, Object value) {
        return this;
    }

    @Override
    public Object getFormatOption(String option) {
        return null;
    }

    @Override
    public String getPackaging() {
        return packaging;
    }

    @Override
    public NCompress setPackaging(String packaging) {
        checkSession();
        if (NBlankable.isBlank(packaging)) {
            packaging = "zip";
        }
        this.packaging = packaging;
        this.packagingImpl = NExtensions.of(session).createComponent(NCompressPackaging.class, this).get();
        return this;
    }

    @Override
    public List<NInputSource> getSources() {
        return sources;
    }

    public NCompress addSource(NInputSource source) {
        if (source != null) {
            this.sources.add(source);
        }
        return this;
    }

    @Override
    public NCompress addSource(InputStream source) {
        checkSession();
        if (source == null) {
            throw new NIllegalArgumentException(session, NMsg.ofPlain("null source"));
        }
        this.sources.add(NInputSource.of(source,session));
        return this;
    }

    @Override
    public NCompress addSource(File source) {
        checkSession();
        if (source == null) {
            throw new NIllegalArgumentException(session, NMsg.ofPlain("null source"));
        }
        this.sources.add(NPath.of(source, session));
        return this;
    }

    @Override
    public NCompress addSource(Path source) {
        if (source == null) {
            checkSession();
            throw new NIllegalArgumentException(session, NMsg.ofPlain("null source"));
        }
        this.sources.add(NPath.of(source, session));
        return this;
    }

    @Override
    public NCompress addSource(URL source) {
        if (source == null) {
            checkSession();
            throw new NIllegalArgumentException(session, NMsg.ofPlain("null source"));
        }
        this.sources.add(NPath.of(source, session));
        return this;
    }

    @Override
    public NCompress addSource(NPath source) {
        if (source != null) {
            this.sources.add(source);
        }
        return this;
    }

    @Override
    public NOutputTarget getTarget() {
        return target;
    }

    @Override
    public NCompress setTarget(OutputStream target) {
        if (target == null) {
            this.target = null;
        } else {
            checkSession();
            this.target = NOutputTarget.of(target,session);
        }
        return this;
    }

    @Override
    public NCompress setTarget(NOutputTarget target) {
        this.target = target;
        return this;
    }

    @Override
    public NCompress setTarget(Path target) {
        if (target == null) {
            this.target = null;
        } else {
            this.target = NPath.of(target, session);
        }
        return this;
    }

    @Override
    public NCompress setTarget(File target) {
        if (target == null) {
            this.target = null;
        } else {
            this.target = NPath.of(target, session);
        }
        return this;
    }

    @Override
    public NCompress setTarget(String target) {
        if (target == null) {
            this.target = null;
        } else {
            this.target = NPath.of(target, session);
        }
        return this;
    }

    @Override
    public NCompress setTarget(NPath target) {
        this.target = target;
        return this;
    }

    @Override
    public NCompress to(NPath target) {
        return setTarget(target);
    }

    @Override
    public NCompress to(OutputStream target) {
        return setTarget(target);
    }

    @Override
    public NCompress to(String target) {
        return setTarget(target);
    }

    @Override
    public NCompress to(Path target) {
        return setTarget(target);
    }

    @Override
    public NCompress to(File target) {
        return setTarget(target);
    }

    @Override
    public NSession getSession() {
        return session;
    }

    @Override
    public NCompress setSession(NSession session) {
        this.session = NWorkspaceUtils.bindSession(ws, session);
        return this;
    }

    @Override
    public NCompress run() {
        checkSession();
        if (packagingImpl == null) {
            this.packagingImpl = NExtensions.of(session).createComponent(NCompressPackaging.class, this).get();
        }
        packagingImpl.compressPackage(this);
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
     * set progress factory responsible for creating progress monitor
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NCompress setProgressFactory(NProgressFactory value) {
        this.progressFactory = value;
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
    public NCompress setProgressMonitor(NProgressListener value) {
        this.progressFactory = value == null ? null : new SingletonNInputStreamProgressFactory(value);
        return this;
    }

    //    private static void zipDir(String dirName, String nameZipFile) throws IOException {
//        ZipOutputStream zip = null;
//        FileOutputStream fW = null;
//        fW = new FileOutputStream(nameZipFile);
//        zip = new ZipOutputStream(fW);
//        addFolderToZip("", dirName, zip);
//        zip.close();
//        fW.close();
//    }
    @Override
    public boolean isSafe() {
        return safe;
    }

    @Override
    public NCompress setSafe(boolean value) {
        this.safe = value;
        return this;
    }

    @Override
    public boolean isSkipRoot() {
        return skipRoot;
    }

    @Override
    public NCompress setSkipRoot(boolean value) {
        this.skipRoot = value;
        return this;
    }

    public static class Item {

        private final NInputSource inSource;
        private final NCompress c;

        public Item(NInputSource value, NCompress c) {
            this.inSource = value;
            this.c = c;
        }

        public boolean isSourcePath() {
            return ((inSource instanceof NPath));
        }

        public boolean isSourceDirectory() {
            return isSourcePath() && ((NPath) inSource).isDirectory();
        }

        public Item[] list() {
            if (isSourcePath()) {
                NPath p = (NPath) inSource;
                return p.stream().map(
                                NFunction.of(
                                        (NPath x) -> new Item(x, c)
                                ).withDesc(NEDesc.of("NutsStreamOrPath::of"))
                        )
                        .toArray(Item[]::new);
            }
            return new Item[0];
        }

        public InputStream open() {
            if (c.getOptions().contains(NPathOption.LOG)
                    || c.getOptions().contains(NPathOption.TRACE)
                    || c.getProgressFactory() != null) {
                NInputStreamMonitor monitor = NInputStreamMonitor.of(c.getSession());
                monitor.setOrigin(inSource);
                monitor.setLogProgress(c.getOptions().contains(NPathOption.LOG));
                monitor.setTraceProgress(c.getOptions().contains(NPathOption.TRACE));
                monitor.setProgressFactory(c.getProgressFactory());
                monitor.setSource(inSource);
                return monitor.create();
            }
            return inSource.getInputStream();
        }

        public String getName() {
            return inSource.getMetaData().getName().orElse(inSource.toString());
        }
    }

    @Override
    public NCompress addOptions(NPathOption... pathOptions) {
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
    public NCompress removeOptions(NPathOption... pathOptions) {
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
    public NCompress clearOptions() {
        options.clear();
        return this;
    }

    @Override
    public Set<NPathOption> getOptions() {
        return new LinkedHashSet<>(options);
    }

}
