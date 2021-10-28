/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.io.NutsStreamOrPath;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author thevpc
 */
public class DefaultNutsIOCompressAction implements NutsIOCompressAction {

    private NutsLogger LOG;
    private final List<NutsStreamOrPath> sources = new ArrayList<>();
    private final NutsWorkspace ws;
    private boolean safe = true;
    private boolean logProgress = false;
    private NutsStreamOrPath target;
    private NutsSession session;
    private boolean skipRoot;
    private NutsProgressFactory progressMonitorFactory;
    private String format = "zip";

    public DefaultNutsIOCompressAction(NutsWorkspace ws) {
        this.ws = ws;
    }
    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = session.log().of(DefaultNutsIOCompressAction.class);
        }
        return LOG;
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
                    tempPath = session.io().tmp()
                            .createTempFile("zip").toFile();
                }
                if (this.target.isPath()) {
                    NutsPath parent = this.target.getPath().getParent();
                    if (parent != null) {
                        parent.mkdir(true);
                    }
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
                                this.target.getOutputStream()
                        );
                    } else if (this.target.isPath()
                            && this.target.getPath().isFile()) {
                        Files.move(tempPath, this.target.getPath().toFile(),
                                StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        try (InputStream ii = Files.newInputStream(tempPath)) {
                            try (OutputStream jj = this.target.getPath().getOutputStream()) {
                                CoreIOUtils.copy(ii, jj);
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
            throw new UncheckedIOException(ex);
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
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public NutsIOCompressAction setFormatOption(String option, Object value) {
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
    public NutsIOCompressAction setFormat(String format) {
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

    @Override
    public NutsIOCompressAction addSource(NutsPath source) {
        if (source != null) {
            this.sources.add(NutsStreamOrPath.of(source));
        }
        return this;
    }

    public NutsIOCompressAction addSource(String source) {
        if (source != null) {
            this.sources.add(NutsStreamOrPath.of(session.io().path(source)));
        }
        return this;
    }

    @Override
    public NutsIOCompressAction addSource(InputStream source) {
        if (source == null) {
            checkSession();
            throw new NutsIllegalArgumentException(session, NutsMessage.plain("null source"));
        }
        this.sources.add(NutsStreamOrPath.of(source));
        return this;
    }

    @Override
    public NutsIOCompressAction addSource(File source) {
        if (source == null) {
            checkSession();
            throw new NutsIllegalArgumentException(session, NutsMessage.plain("null source"));
        }
        this.sources.add(NutsStreamOrPath.of(session.io().path(source)));
        return this;
    }

    @Override
    public NutsIOCompressAction addSource(Path source) {
        if (source == null) {
            checkSession();
            throw new NutsIllegalArgumentException(session, NutsMessage.plain("null source"));
        }
        this.sources.add(NutsStreamOrPath.of(session.io().path(source)));
        return this;
    }

    @Override
    public NutsIOCompressAction addSource(URL source) {
        if (source == null) {
            checkSession();
            throw new NutsIllegalArgumentException(session, NutsMessage.plain("null source"));
        }
        this.sources.add(NutsStreamOrPath.of(session.io().path(source)));
        return this;
    }

    @Override
    public Object getTarget() {
        return target;
    }

    @Override
    public NutsIOCompressAction setTarget(OutputStream target) {
        if (target == null) {
            this.target = null;
        } else {
            this.target = NutsStreamOrPath.of(target);
        }
        return this;
    }

    @Override
    public NutsIOCompressAction setTarget(NutsPath target) {
        if (target == null) {
            this.target = null;
        } else {
            this.target = NutsStreamOrPath.of(target);
        }
        return this;
    }

    @Override
    public NutsIOCompressAction setTarget(Path target) {
        if (target == null) {
            this.target = null;
        } else {
            this.target = NutsStreamOrPath.of(session.io().path(target));
        }
        return this;
    }

    @Override
    public NutsIOCompressAction setTarget(File target) {
        if (target == null) {
            this.target = null;
        } else {
            this.target = NutsStreamOrPath.of(session.io().path(target));
        }
        return this;
    }

    @Override
    public NutsIOCompressAction setTarget(String target) {
        if (target == null) {
            this.target = null;
        } else {
            this.target = NutsStreamOrPath.of(session.io().path(target));
        }
        return this;
    }

    @Override
    public NutsIOCompressAction to(NutsPath target) {
        return setTarget(target);
    }

    @Override
    public NutsIOCompressAction to(OutputStream target) {
        return setTarget(target);
    }

    @Override
    public NutsIOCompressAction to(String target) {
        return setTarget(target);
    }

    @Override
    public NutsIOCompressAction to(Path target) {
        return setTarget(target);
    }

    @Override
    public NutsIOCompressAction to(File target) {
        return setTarget(target);
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsIOCompressAction setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(ws, session);
        return this;
    }

    @Override
    public NutsIOCompressAction run() {
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
    public DefaultNutsIOCompressAction setLogProgress(boolean value) {
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
    public NutsIOCompressAction setProgressMonitorFactory(NutsProgressFactory value) {
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
    public NutsIOCompressAction setProgressMonitor(NutsProgressMonitor value) {
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
    public NutsIOCompressAction setSafe(boolean value) {
        this.safe = value;
        return this;
    }

    @Override
    public boolean isSkipRoot() {
        return skipRoot;
    }

    @Override
    public NutsIOCompressAction setSkipRoot(boolean value) {
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
                return Arrays.stream(p.getChildren()).map(x->new Item(NutsStreamOrPath.of(x)))
                        .toArray(Item[]::new);
            }
            return new Item[0];
        }

        public InputStream open() {
            if(o.isInputStream()){
                return o.getInputStream();
            }
            return o.getPath().getInputStream();
        }

        public String getName() {
            return o.getName();
        }
    }
}
