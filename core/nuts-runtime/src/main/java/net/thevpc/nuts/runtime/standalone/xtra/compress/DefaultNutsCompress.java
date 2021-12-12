/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.xtra.compress;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.io.util.NutsStreamOrPath;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.io.progress.SingletonNutsInputStreamProgressFactory;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author thevpc
 */
public class DefaultNutsCompress implements NutsCompress {

    private final List<NutsStreamOrPath> sources = new ArrayList<>();
    private final NutsWorkspace ws;
    private NutsLogger LOG;
    private boolean safe = true;
    private boolean logProgress = false;
    private NutsStreamOrPath target;
    private NutsSession session;
    private boolean skipRoot;
    private NutsProgressFactory progressMonitorFactory;
    private String format = "zip";

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
            LOG = NutsLogger.of(DefaultNutsCompress.class,session);
        }
        return LOG;
    }

    private void checkSession() {
        NutsWorkspaceUtils.checkSession(ws, session);
    }

    public void runZip() {
        checkSession();
        if (sources.isEmpty()) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("missing source"));
        }
        if (target == null) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("missing target"));
        }
        if (isLogProgress() || getProgressMonitorFactory() != null) {
            //how to monitor???
        }
        _LOG(session).with().level(Level.FINEST).verb(NutsLogVerb.START).log(NutsMessage.jstyle("compress {0} to {1}", sources, target));
        try {
            OutputStream fW = null;
            ZipOutputStream zip = null;
            if (this.target.isPath()) {
                Path tempPath = null;
                if (isSafe()) {
                    tempPath = NutsTmp.of(session)
                            .createTempFile("zip").toFile();
                }
                if (this.target.isPath()) {
                    this.target.getPath().mkParentDirs();
                }
                if (tempPath == null) {
                    fW = target.isOutputStream() ? target.getOutputStream() : target.getPath().getOutputStream();
                } else {
                    fW = Files.newOutputStream(tempPath);
                }
                try {
                    try {
                        zip = new ZipOutputStream(fW);
                        if (skipRoot) {
                            for (NutsStreamOrPath s : sources) {
                                Item file1 = new Item(s);
                                if (file1.isDirectory()) {
                                    for (Item c : file1.list()) {
                                        add("", c, zip);
                                    }
                                } else {
                                    add("", file1, zip);
                                }
                            }
                        } else {
                            for (NutsStreamOrPath s : sources) {
                                add("", new Item(s), zip);
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
                    if (this.target.isOutputStream()) {
                        CoreIOUtils.copy(
                                Files.newInputStream(tempPath),
                                this.target.getOutputStream(),session
                        );
                    } else if (this.target.isPath()
                            && this.target.getPath().isFile()) {
                        Files.move(tempPath, this.target.getPath().toFile(),
                                StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        try (InputStream ii = Files.newInputStream(tempPath)) {
                            try (OutputStream jj = this.target.getPath().getOutputStream()) {
                                CoreIOUtils.copy(ii, jj,session);
                            }
                        }
                    }
                }
            } else {
                throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("unsupported target %s", target));
            }
        } catch (IOException ex) {
            _LOG(session).with().level(Level.CONFIG).verb(NutsLogVerb.FAIL)
                    .log(NutsMessage.jstyle("error compressing {0} to {1} : {2}",
                            sources, target.getValue(), ex));
            throw new NutsIOException(session,ex);
        }
    }

    private void add(String path, Item srcFolder, ZipOutputStream zip) {
        if (srcFolder.isDirectory()) {
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
                if (srcFile.isDirectory()) {
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
            throw new NutsIOException(session,ex);
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
                throw new NutsUnsupportedArgumentException(getSession(), NutsMessage.cstyle("unsupported compression format %s", format));
            }
        }
        return this;
    }

    @Override
    public List<Object> getSources() {
        return (List) sources;
    }

    public NutsCompress addSource(String source) {
        if (source != null) {
            this.sources.add(NutsStreamOrPath.of(NutsPath.of(source, session)));
        }
        return this;
    }

    @Override
    public NutsCompress addSource(InputStream source) {
        checkSession();
        if (source == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.plain("null source"));
        }
        this.sources.add(NutsStreamOrPath.of(source,session));
        return this;
    }

    @Override
    public NutsCompress addSource(File source) {
        checkSession();
        if (source == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.plain("null source"));
        }
        this.sources.add(NutsStreamOrPath.of(NutsPath.of(source,session)));
        return this;
    }

    @Override
    public NutsCompress addSource(Path source) {
        if (source == null) {
            checkSession();
            throw new NutsIllegalArgumentException(session, NutsMessage.plain("null source"));
        }
        this.sources.add(NutsStreamOrPath.of(NutsPath.of(source,session)));
        return this;
    }

    @Override
    public NutsCompress addSource(URL source) {
        if (source == null) {
            checkSession();
            throw new NutsIllegalArgumentException(session, NutsMessage.plain("null source"));
        }
        this.sources.add(NutsStreamOrPath.of(NutsPath.of(source,session)));
        return this;
    }

    @Override
    public NutsCompress addSource(NutsPath source) {
        if (source != null) {
            this.sources.add(NutsStreamOrPath.of(source));
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
            this.target = NutsStreamOrPath.of(target,session);
        }
        return this;
    }

    @Override
    public NutsCompress setTarget(Path target) {
        if (target == null) {
            this.target = null;
        } else {
            this.target = NutsStreamOrPath.of(NutsPath.of(target,session));
        }
        return this;
    }

    @Override
    public NutsCompress setTarget(File target) {
        if (target == null) {
            this.target = null;
        } else {
            this.target = NutsStreamOrPath.of(NutsPath.of(target,session));
        }
        return this;
    }

    @Override
    public NutsCompress setTarget(String target) {
        if (target == null) {
            this.target = null;
        } else {
            this.target = NutsStreamOrPath.of(NutsPath.of(target,session));
        }
        return this;
    }

    @Override
    public NutsCompress setTarget(NutsPath target) {
        if (target == null) {
            this.target = null;
        } else {
            this.target = NutsStreamOrPath.of(target);
        }
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
                throw new NutsUnsupportedArgumentException(getSession(), NutsMessage.cstyle("unsupported compression format %s", getFormat()));
            }
        }
        return this;
    }

    @Override
    public boolean isLogProgress() {
        return logProgress;
    }

    @Override
    public DefaultNutsCompress setLogProgress(boolean value) {
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
    public NutsCompress setProgressMonitorFactory(NutsProgressFactory value) {
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
    public NutsCompress setProgressMonitor(NutsProgressMonitor value) {
        this.progressMonitorFactory = value == null ? null : new SingletonNutsInputStreamProgressFactory(value);
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
        this.skipRoot = true;
        return this;
    }

    private static class Item {

        private final NutsStreamOrPath o;

        public Item(NutsStreamOrPath value) {
            this.o = value;
        }

        public boolean isDirectory() {
            return (o.isPath() && o.getPath().isDirectory());
        }

        private Item[] list() {
            if (o.isPath()) {
                NutsPath p = o.getPath();
                return p.list().map(
                                NutsFunction.of(
                                x -> new Item(NutsStreamOrPath.of(x)),
                                "NutsStreamOrPath::of"
                        )
                        )
                        .toArray(Item[]::new);
            }
            return new Item[0];
        }

        public InputStream open() {
            if (o.isInputStream()) {
                return o.getInputStream();
            }
            return o.getPath().getInputStream();
        }

        public String getName() {
            return o.getName();
        }
    }
}
