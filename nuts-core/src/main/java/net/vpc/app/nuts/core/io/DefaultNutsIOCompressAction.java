/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.io;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.log.NutsLogVerb;
import net.vpc.app.nuts.core.util.io.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author vpc
 */
public class DefaultNutsIOCompressAction implements NutsPathCompressAction {

    private final NutsLogger LOG;

    private boolean safeCopy = true;
    private boolean monitorable = false;
    private List<InputSource> sources = new ArrayList<>();
    private CoreIOUtils.TargetItem target;
    private DefaultNutsIOManager iom;
    private NutsSession session;
    private boolean includeDefaultMonitorFactory;
    private boolean skipRoot;
    private NutsInputStreamProgressFactory progressMonitorFactory;

    public DefaultNutsIOCompressAction(DefaultNutsIOManager iom) {
        this.iom = iom;
        LOG = iom.getWorkspace().log().of(DefaultNutsIOCompressAction.class);
    }

    @Override
    public List<Object> getSources() {
        return (List) sources;
    }

    @Override
    public NutsPathCompressAction addSource(InputStream source) {
        this.sources.add(CoreIOUtils.createInputSource(source));
        return this;
    }

    @Override
    public NutsPathCompressAction addSource(File source) {
        this.sources.add(CoreIOUtils.createInputSource(source));
        return this;
    }

    @Override
    public NutsPathCompressAction addSource(Path source) {
        this.sources.add(CoreIOUtils.createInputSource(source));
        return this;
    }

    @Override
    public NutsPathCompressAction addSource(URL source) {
        this.sources.add(CoreIOUtils.createInputSource(source));
        return this;
    }

    @Override
    public NutsPathCompressAction setTarget(OutputStream target) {
        this.target = CoreIOUtils.createTarget(target);
        return this;
    }

    @Override
    public NutsPathCompressAction setTarget(Path target) {
        this.target = CoreIOUtils.createTarget(target);
        return this;
    }

    @Override
    public NutsPathCompressAction setTarget(File target) {
        this.target = CoreIOUtils.createTarget(target);
        return this;
    }

    @Override
    public NutsPathCompressAction setTarget(String target) {
        this.target = CoreIOUtils.createTarget(target);
        return this;
    }

    public NutsPathCompressAction addSource(Object source) {
        this.sources.add(CoreIOUtils.createInputSource(source));
        return this;
    }

    @Override
    public NutsPathCompressAction to(Object target) {
        return setTarget(target);
    }

    @Override
    public NutsPathCompressAction to(String target) {
        return setTarget(target);
    }

    @Override
    public Object getTarget() {
        return target;
    }

    public NutsPathCompressAction setTarget(Object target) {
        this.target = CoreIOUtils.createTarget(target);
        return this;
    }

    @Override
    public boolean isMonitorable() {
        return monitorable;
    }

    @Override
    public DefaultNutsIOCompressAction setMonitorable(boolean monitorable) {
        this.monitorable = monitorable;
        return this;
    }

    @Override
    public NutsPathCompressAction to(File target) {
        return setTarget(target);
    }

    @Override
    public NutsPathCompressAction to(OutputStream target) {
        return setTarget(target);
    }

    @Override
    public NutsPathCompressAction to(Path target) {
        return setTarget(target);
    }

    @Override
    public NutsPathCompressAction monitorable() {
        setMonitorable(true);
        return this;
    }

    @Override
    public NutsPathCompressAction monitorable(boolean safeCopy) {
        setMonitorable(safeCopy);
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsPathCompressAction session(NutsSession session) {
        return setSession(session);
    }

    @Override
    public NutsPathCompressAction setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public void run() {
        if (sources.isEmpty()) {
            throw new UnsupportedOperationException("Missing Source");
        }
        if (target == null) {
            throw new UnsupportedOperationException("Missing Target");
        }
        if (isMonitorable() || getProgressMonitorFactory() != null) {
            //how to monitor???
        }
        LOG.log(Level.FINEST, NutsLogVerb.START, "compress {0} to {1}", sources, target);
        try {
            OutputStream fW = null;
            ZipOutputStream zip = null;
            if (this.target.isPath()) {
                Path tempPath = null;
                if (isSafeCopy()) {
                    tempPath = iom.createTempFile("zip");
                }
                Path path = this.target.getPath();
                if (path.getParent() != null) {
                    Files.createDirectories(path.getParent());
                }
                if (tempPath == null) {
                    fW = target.open();
                } else {
                    fW = Files.newOutputStream(tempPath);
                }
                try {
                    try {
                        zip = new ZipOutputStream(fW);
                        if (skipRoot) {
                            for (InputSource s : sources) {
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
                            for (InputSource s : sources) {
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
                    Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                throw new NutsIllegalArgumentException(iom.getWorkspace(), "Unsupported target " + target);
            }
        } catch (IOException ex) {
            LOG.log(Level.CONFIG, NutsLogVerb.FAIL, "Error compressing {0} to {1} : {2}", sources, target.getValue(), ex.toString());
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * when true, will include default factory (console) even if progressMonitorFactory is defined
     *
     * @return true if always include default factory
     * @since 0.5.8
     */
    @Override
    public boolean isIncludeDefaultMonitorFactory() {
        return includeDefaultMonitorFactory;
    }

    /**
     * when true, will include default factory (console) even if progressMonitorFactory is defined
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NutsPathCompressAction setIncludeDefaultMonitorFactory(boolean value) {
        this.includeDefaultMonitorFactory = value;
        return this;
    }

    /**
     * when true, will include default factory (console) even if progressMonitorFactory is defined
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NutsPathCompressAction includeDefaultMonitorFactory(boolean value) {
        return setIncludeDefaultMonitorFactory(value);
    }

    /**
     * always include default factory (console) even if progressMonitorFactory is defined
     *
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NutsPathCompressAction includeDefaultMonitorFactory() {
        return includeDefaultMonitorFactory(true);
    }

    /**
     * return progress factory responsible of creating progress monitor
     *
     * @return progress factory responsible of creating progress monitor
     * @since 0.5.8
     */
    @Override
    public NutsInputStreamProgressFactory getProgressMonitorFactory() {
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
    public NutsPathCompressAction setProgressMonitorFactory(NutsInputStreamProgressFactory value) {
        this.progressMonitorFactory = value;
        return this;
    }

    /**
     * set progress factory responsible of creating progress monitor
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NutsPathCompressAction progressMonitorFactory(NutsInputStreamProgressFactory value) {
        return setProgressMonitorFactory(value);
    }

    /**
     * set progress monitor. Will create a singeleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NutsPathCompressAction setProgressMonitor(NutsProgressMonitor value) {
        this.progressMonitorFactory = value == null ? null : new SingletonNutsInputStreamProgressFactory(value);
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
    public NutsPathCompressAction progressMonitor(NutsProgressMonitor value) {
        return setProgressMonitor(value);
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
    public boolean isSafeCopy() {
        return safeCopy;
    }

    @Override
    public NutsPathCompressAction setSafeCopy(boolean safeCopy) {
        this.safeCopy = safeCopy;
        return this;
    }

    @Override
    public NutsPathCompressAction safeCopy() {
        setSafeCopy(true);
        return this;
    }

    @Override
    public NutsPathCompressAction safeCopy(boolean safeCopy) {
        setSafeCopy(safeCopy);
        return this;
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
//            System.out.println("[FOLDER ]" + pathPrefix + folder.getName());
                zip.putNextEntry(new ZipEntry(pathPrefix + srcFile.getName() + "/"));
            } else {
                if (srcFile.isDirectory()) {
                    addFolderToZip(pathPrefix, srcFile, zip);
                } else {
//                System.out.println("[FILE  ]" + pathPrefix + folder.getName() + " - " + srcFile);
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

    private static class Item {
        private Object o;

        public Item(Object value) {
            this.o = value;
        }

        public boolean isDirectory() {
            if (o instanceof File) {
                return ((File) o).isDirectory();
            }
            if (o instanceof Path) {
                return Files.isDirectory(((Path) o));
            }
            if (o instanceof InputSource) {
                InputSource s = (InputSource) o;
                if (s.isPath()) {
                    return Files.isDirectory(s.getPath());
                }
            }
            return false;
        }

        private Item[] list() {
            if (o instanceof File) {
                File[] g = ((File) o).listFiles();
                if (g == null) {
                    return new Item[0];
                }
                return Arrays.stream(g).map(Item::new).toArray(Item[]::new);
            }
            if (o instanceof Path) {
                Path o1 = (Path) o;
                if (Files.isDirectory(o1)) {
                    try {
                        return Files.list(o1).map(Item::new).toArray(Item[]::new);
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                }
            }
            if (o instanceof InputSource) {
                InputSource s = (InputSource) o;
                if (s.isPath()) {
                    Path o1 = s.getPath();
                    try {
                        return Files.list(o1).map(Item::new).toArray(Item[]::new);
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                }
            }
            return new Item[0];
        }

        public InputStream open() {
            if (o instanceof File) {
                try {
                    return new FileInputStream(((File) o));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            if (o instanceof Path) {
                try {
                    return Files.newInputStream(((Path) o));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            if (o instanceof InputSource) {
                InputSource s = (InputSource) o;
                return s.open();
            }
            throw new UncheckedIOException(new IOException("Unsupported type " + o));
        }

        public String getName() {
            if (o instanceof File) {
                return ((File) o).getName();
            }
            if (o instanceof Path) {
                return ((Path) o).getFileName().toString();
            }
            if (o instanceof InputSource) {
                InputSource s = (InputSource) o;
                return s.getPath().getFileName().toString();
            }
            return "";
        }
    }

    @Override
    public NutsPathCompressAction skipRoot(boolean value) {
        return setSkipRoot(value);
    }

    @Override
    public NutsPathCompressAction skipRoot() {
        return skipRoot(true);
    }

    @Override
    public boolean isSkipRoot() {
        return skipRoot;
    }

    @Override
    public NutsPathCompressAction setSkipRoot(boolean value) {
        this.skipRoot=true;
        return this;
    }

}
