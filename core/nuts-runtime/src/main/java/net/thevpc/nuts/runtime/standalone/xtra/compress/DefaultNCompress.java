/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.xtra.compress;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.progress.SingletonNInputStreamProgressFactory;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NPaths;
import net.thevpc.nuts.spi.NSupportLevelContext;
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
public class DefaultNCompress implements NCompress {

    private final List<NInputSource> sources = new ArrayList<>();
    private final NWorkspace ws;
    private NLogger LOG;
    private boolean safe = true;
    private NOutputTarget target;
    private NSession session;
    private boolean skipRoot;
    private NProgressFactory progressFactory;
    private String format = "zip";
    private Set<NPathOption> options = new LinkedHashSet<>();

    public DefaultNCompress(NSession session) {
        this.session = session;
        this.ws = session.getWorkspace();
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
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

    protected NLogger _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLogger.of(DefaultNCompress.class, session);
        }
        return LOG;
    }

    private void checkSession() {
        NSessionUtils.checkSession(ws, session);
    }

    public void runZip() {
        checkSession();
        NUtils.requireNonBlank(sources, "source", session);
        NUtils.requireNonBlank(target, "target", session);
        _LOG(session).with().level(Level.FINEST).verb(NLoggerVerb.START).log(NMsg.ofJstyle("compress {0} to {1}", sources, target));
        try {
            OutputStream fW = null;
            ZipOutputStream zip = null;
            if (this.target instanceof NPath) {
                Path tempPath = null;
                if (isSafe()) {
                    tempPath = NPaths.of(session)
                            .createTempFile("zip").toFile();
                }
                if (this.target instanceof NPath) {
                    ((NPath) this.target).mkParentDirs();
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
                            for (NInputSource s : sources) {
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
                            for (NInputSource s : sources) {
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
                    if (this.target instanceof NPath && ((NPath) target).isFile()) {
                        Files.move(tempPath, ((NPath) target).toFile(),
                                StandardCopyOption.REPLACE_EXISTING);
                    } else if (this.target instanceof NPath) {
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
                throw new NIllegalArgumentException(getSession(), NMsg.ofCstyle("unsupported target %s", target));
            }
        } catch (IOException ex) {
            _LOG(session).with().level(Level.CONFIG).verb(NLoggerVerb.FAIL)
                    .log(NMsg.ofJstyle("error compressing {0} to {1} : {2}",
                            sources, target, ex));
            throw new NIOException(session, ex);
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
            throw new NIOException(session, ex);
        }
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
    public String getFormat() {
        return format;
    }

    @Override
    public NCompress setFormat(String format) {
        checkSession();
        if (NBlankable.isBlank(format)) {
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
                throw new NUnsupportedArgumentException(getSession(), NMsg.ofCstyle("unsupported compression format %s", format));
            }
        }
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
        this.sources.add(NIO.of(session).createInputSource(source));
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
    public Object getTarget() {
        return target;
    }

    @Override
    public NCompress setTarget(OutputStream target) {
        if (target == null) {
            this.target = null;
        } else {
            checkSession();
            this.target = NIO.of(session).createOutputTarget(target);
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
        switch (getFormat()) {
            case "zip":
            case "gzip":
            case "gz": {
                runZip();
                break;
            }
            default: {
                throw new NUnsupportedArgumentException(getSession(), NMsg.ofCstyle("unsupported compression format %s", getFormat()));
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

    private static class Item {

        private final NInputSource inSource;
        private final DefaultNCompress c;

        public Item(NInputSource value, DefaultNCompress c) {
            this.inSource = value;
            this.c = c;
        }

        public boolean isSourcePath() {
            return ((inSource instanceof NPath));
        }

        public boolean isSourceDirectory() {
            return isSourcePath() && ((NPath) inSource).isDirectory();
        }

        private Item[] list() {
            if (isSourcePath()) {
                NPath p = (NPath) inSource;
                return p.stream().map(
                                NFunction.of(
                                        x -> new Item(x, c),
                                        "NutsStreamOrPath::of"
                                )
                        )
                        .toArray(Item[]::new);
            }
            return new Item[0];
        }

        public InputStream open() {
            if (c.options.contains(NPathOption.LOG)
                    || c.options.contains(NPathOption.TRACE)
                    || c.getProgressFactory() != null) {
                NInputStreamMonitor monitor = NInputStreamMonitor.of(c.session);
                monitor.setOrigin(inSource);
                monitor.setLogProgress(c.options.contains(NPathOption.LOG));
                monitor.setTraceProgress(c.options.contains(NPathOption.TRACE));
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
