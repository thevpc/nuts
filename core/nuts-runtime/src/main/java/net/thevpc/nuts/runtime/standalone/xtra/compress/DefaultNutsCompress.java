/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.xtra.compress;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.progress.SingletonNutsInputStreamProgressFactory;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsPaths;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author thevpc
 */
public class DefaultNutsCompress implements NutsCompress {

    private final List<NutsInputSource> sources = new ArrayList<>();
    private final NutsWorkspace ws;
    private NutsLogger LOG;
    private boolean safe = true;
    private NutsOutputTarget target;
    private NutsSession session;
    private boolean skipRoot;
    private NutsProgressFactory progressFactory;
    private String format = "zip";
    private Set<NutsPathOption> options = new LinkedHashSet<>();

    public DefaultNutsCompress(NutsSession session) {
        this.session = session;
        this.ws = session.getWorkspace();
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    private static String concatPath(String a, String b) {
        if (a.endsWith("/")) {
            if (b.startsWith("/")) {
                return a + b.substring(1);
            } else {
                return a + b;
            }
        } else {
            if (b.startsWith("/")) {
                return a + b;
            } else {
                return a + "/" + b;
            }
        }
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = NutsLogger.of(DefaultNutsCompress.class, session);
        }
        return LOG;
    }

    private void checkSession() {
        NutsSessionUtils.checkSession(ws, session);
    }

    public void runZip() {
        checkSession();
        NutsUtils.requireNonBlank(sources, session, "source");
        NutsUtils.requireNonBlank(target, session, "target");
        _LOG(session).with().level(Level.FINEST).verb(NutsLoggerVerb.START).log(NutsMessage.ofJstyle("compress {0} to {1}", sources, target));
        try {
            OutputStream fW = null;
            ZipOutputStream zip = null;
            if (this.target instanceof NutsPath) {
                Path tempPath = null;
                if (isSafe()) {
                    tempPath = NutsPaths.of(session)
                            .createTempFile("zip").toFile();
                }
                if (this.target instanceof NutsPath) {
                    ((NutsPath) this.target).mkParentDirs();
                }
                if (tempPath == null) {
                    fW = target.getOutputStream();
                } else {
                    fW = Files.newOutputStream(tempPath);
                }
                try {
                    try {
                        zip = new ZipOutputStream(fW);
                        if (skipRoot) {
                            for (NutsInputSource s : sources) {
                                Item file1 = new Item(s, this);
                                if (file1.isSourceDirectory()) {
                                    for (Item c : file1.list()) {
                                        add("", c, zip);
                                    }
                                } else {
                                    add("", file1, zip);
                                }
                            }
                        } else {
                            for (NutsInputSource s : sources) {
                                add("", new Item(s, this), zip);
                            }
                        }
                    } finally {
                        if (zip != null) {
                            zip.close();
                        }
                    }
                } finally {
                    if (fW != null) {
                        fW.close();
                    }
                }
                if (tempPath != null) {
                    if (this.target instanceof NutsPath && ((NutsPath) target).isFile()) {
                        Files.move(tempPath, ((NutsPath) target).toFile(),
                                StandardCopyOption.REPLACE_EXISTING);
                    } else if (this.target instanceof NutsPath) {
                        try (InputStream ii = Files.newInputStream(tempPath)) {
                            try (OutputStream jj = target.getOutputStream()) {
                                CoreIOUtils.copy(ii, jj, session);
                            }
                        }
                    } else {
                        CoreIOUtils.copy(
                                Files.newInputStream(tempPath),
                                this.target.getOutputStream(), session
                        );
                    }
                }
            } else {
                throw new NutsIllegalArgumentException(getSession(), NutsMessage.ofCstyle("unsupported target %s", target));
            }
        } catch (IOException ex) {
            _LOG(session).with().level(Level.CONFIG).verb(NutsLoggerVerb.FAIL)
                    .log(NutsMessage.ofJstyle("error compressing {0} to {1} : {2}",
                            sources, target, ex));
            throw new NutsIOException(session, ex);
        }
    }

    private void add(String path, Item srcFolder, ZipOutputStream zip) {
        if (srcFolder.isSourceDirectory()) {
            addFolderToZip(path, srcFolder, zip);
        } else {
            addFileToZip(path, srcFolder, zip, false);
        }
    }

    private void addFolderToZip(String path, Item srcFolder, ZipOutputStream zip) throws UncheckedIOException {
        Item[] dirChildren = srcFolder.list();
        if (dirChildren.length == 0) {
            addFileToZip(path, srcFolder, zip, true);
        } else {
            for (Item c : dirChildren) {
                if (path.equals("")) {
                    addFileToZip(srcFolder.getName(), c, zip, false);
                } else {
                    addFileToZip(concatPath(path, c.getName()), c, zip, false);
                }
            }
        }
    }

    private void addFileToZip(String path, Item srcFile, ZipOutputStream zip, boolean flag) throws UncheckedIOException {
//        File folder = new File(srcFile);
        String pathPrefix = path;
        if (!pathPrefix.endsWith("/")) {
            pathPrefix = pathPrefix + "/";
        }
        if (!pathPrefix.startsWith("/")) {
            pathPrefix = "/" + pathPrefix;
        }
        try {

            if (flag) {
                zip.putNextEntry(new ZipEntry(pathPrefix + srcFile.getName() + "/"));
            } else {
                if (srcFile.isSourceDirectory()) {
                    addFolderToZip(pathPrefix, srcFile, zip);
                } else {
                    byte[] buf = new byte[1024];
                    int len;
                    InputStream in = srcFile.open();
                    zip.putNextEntry(new ZipEntry(pathPrefix + srcFile.getName()));
                    while ((len = in.read(buf)) > 0) {
                        zip.write(buf, 0, len);
                    }
                }
            }
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }

    @Override
    public NutsCompress setFormatOption(String option, Object value) {
        return this;
    }

    @Override
    public Object getFormatOption(String option) {
        return null;
    }

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public NutsCompress setFormat(String format) {
        checkSession();
        if (NutsBlankable.isBlank(format)) {
            format = "zip";
        }
        switch (format) {
            case "zip":
            case "gzip":
            case "gz": {
                this.format = format;
                break;
            }
            default: {
                throw new NutsUnsupportedArgumentException(getSession(), NutsMessage.ofCstyle("unsupported compression format %s", format));
            }
        }
        return this;
    }

    @Override
    public List<NutsInputSource> getSources() {
        return sources;
    }

    public NutsCompress addSource(NutsInputSource source) {
        if (source != null) {
            this.sources.add(source);
        }
        return this;
    }

    @Override
    public NutsCompress addSource(InputStream source) {
        checkSession();
        if (source == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.ofPlain("null source"));
        }
        this.sources.add(NutsIO.of(session).createInputSource(source));
        return this;
    }

    @Override
    public NutsCompress addSource(File source) {
        checkSession();
        if (source == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.ofPlain("null source"));
        }
        this.sources.add(NutsPath.of(source, session));
        return this;
    }

    @Override
    public NutsCompress addSource(Path source) {
        if (source == null) {
            checkSession();
            throw new NutsIllegalArgumentException(session, NutsMessage.ofPlain("null source"));
        }
        this.sources.add(NutsPath.of(source, session));
        return this;
    }

    @Override
    public NutsCompress addSource(URL source) {
        if (source == null) {
            checkSession();
            throw new NutsIllegalArgumentException(session, NutsMessage.ofPlain("null source"));
        }
        this.sources.add(NutsPath.of(source, session));
        return this;
    }

    @Override
    public NutsCompress addSource(NutsPath source) {
        if (source != null) {
            this.sources.add(source);
        }
        return this;
    }

    @Override
    public Object getTarget() {
        return target;
    }

    @Override
    public NutsCompress setTarget(OutputStream target) {
        if (target == null) {
            this.target = null;
        } else {
            checkSession();
            this.target = NutsIO.of(session).createOutputTarget(target);
        }
        return this;
    }

    @Override
    public NutsCompress setTarget(NutsOutputTarget target) {
        this.target = target;
        return this;
    }

    @Override
    public NutsCompress setTarget(Path target) {
        if (target == null) {
            this.target = null;
        } else {
            this.target = NutsPath.of(target, session);
        }
        return this;
    }

    @Override
    public NutsCompress setTarget(File target) {
        if (target == null) {
            this.target = null;
        } else {
            this.target = NutsPath.of(target, session);
        }
        return this;
    }

    @Override
    public NutsCompress setTarget(String target) {
        if (target == null) {
            this.target = null;
        } else {
            this.target = NutsPath.of(target, session);
        }
        return this;
    }

    @Override
    public NutsCompress setTarget(NutsPath target) {
        this.target = target;
        return this;
    }

    @Override
    public NutsCompress to(NutsPath target) {
        return setTarget(target);
    }

    @Override
    public NutsCompress to(OutputStream target) {
        return setTarget(target);
    }

    @Override
    public NutsCompress to(String target) {
        return setTarget(target);
    }

    @Override
    public NutsCompress to(Path target) {
        return setTarget(target);
    }

    @Override
    public NutsCompress to(File target) {
        return setTarget(target);
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsCompress setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(ws, session);
        return this;
    }

    @Override
    public NutsCompress run() {
        checkSession();
        switch (getFormat()) {
            case "zip":
            case "gzip":
            case "gz": {
                runZip();
                break;
            }
            default: {
                throw new NutsUnsupportedArgumentException(getSession(), NutsMessage.ofCstyle("unsupported compression format %s", getFormat()));
            }
        }
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
    public NutsCompress setProgressFactory(NutsProgressFactory value) {
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
    public NutsCompress setProgressMonitor(NutsProgressListener value) {
        this.progressFactory = value == null ? null : new SingletonNutsInputStreamProgressFactory(value);
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
    public NutsCompress setSafe(boolean value) {
        this.safe = value;
        return this;
    }

    @Override
    public boolean isSkipRoot() {
        return skipRoot;
    }

    @Override
    public NutsCompress setSkipRoot(boolean value) {
        this.skipRoot = value;
        return this;
    }

    private static class Item {

        private final NutsInputSource inSource;
        private final DefaultNutsCompress c;

        public Item(NutsInputSource value, DefaultNutsCompress c) {
            this.inSource = value;
            this.c = c;
        }

        public boolean isSourcePath() {
            return ((inSource instanceof NutsPath));
        }

        public boolean isSourceDirectory() {
            return isSourcePath() && ((NutsPath) inSource).isDirectory();
        }

        private Item[] list() {
            if (isSourcePath()) {
                NutsPath p = (NutsPath) inSource;
                return p.list().map(
                                NutsFunction.of(
                                        x -> new Item(x, c),
                                        "NutsStreamOrPath::of"
                                )
                        )
                        .toArray(Item[]::new);
            }
            return new Item[0];
        }

        public InputStream open() {
            if (c.options.contains(NutsPathOption.LOG)
                    || c.options.contains(NutsPathOption.TRACE)
                    || c.getProgressFactory() != null) {
                NutsInputStreamMonitor monitor = NutsInputStreamMonitor.of(c.session);
                monitor.setOrigin(inSource);
                monitor.setLogProgress(c.options.contains(NutsPathOption.LOG));
                monitor.setTraceProgress(c.options.contains(NutsPathOption.TRACE));
                monitor.setProgressFactory(c.getProgressFactory());
                monitor.setSource(inSource);
                return monitor.create();
            }
            return inSource.getInputStream();
        }

        public String getName() {
            return inSource.getInputMetaData().getName().orElse(inSource.toString());
        }
    }

    @Override
    public NutsCompress addOptions(NutsPathOption... pathOptions) {
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
    public NutsCompress removeOptions(NutsPathOption... pathOptions) {
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
    public NutsCompress clearOptions() {
        options.clear();
        return this;
    }

    @Override
    public Set<NutsPathOption> getOptions() {
        return new LinkedHashSet<>(options);
    }

}
