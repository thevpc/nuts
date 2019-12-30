/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.runtime.io;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.log.NutsLogVerb;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;
import net.vpc.app.nuts.runtime.util.io.*;

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
public class DefaultNutsIOCompressAction implements NutsIOCompressAction {

    private final NutsLogger LOG;

    private boolean safe = true;
    private boolean logProgress = false;
    private List<InputSource> sources = new ArrayList<>();
    private CoreIOUtils.TargetItem target;
    private DefaultNutsIOManager iom;
    private NutsSession session;
    private boolean skipRoot;
    private NutsProgressFactory progressMonitorFactory;
    private String format="zip";

    public DefaultNutsIOCompressAction(DefaultNutsIOManager iom) {
        this.iom = iom;
        LOG = iom.getWorkspace().log().of(DefaultNutsIOCompressAction.class);
    }

    @Override
    public List<Object> getSources() {
        return (List) sources;
    }

    @Override
    public NutsIOCompressAction addSource(InputStream source) {
        this.sources.add(CoreIOUtils.createInputSource(source));
        return this;
    }

    @Override
    public NutsIOCompressAction addSource(File source) {
        this.sources.add(CoreIOUtils.createInputSource(source));
        return this;
    }

    @Override
    public NutsIOCompressAction addSource(Path source) {
        this.sources.add(CoreIOUtils.createInputSource(source));
        return this;
    }

    @Override
    public NutsIOCompressAction addSource(URL source) {
        this.sources.add(CoreIOUtils.createInputSource(source));
        return this;
    }

    @Override
    public NutsIOCompressAction setTarget(OutputStream target) {
        this.target = CoreIOUtils.createTarget(target);
        return this;
    }

    @Override
    public NutsIOCompressAction setTarget(Path target) {
        this.target = CoreIOUtils.createTarget(target);
        return this;
    }

    @Override
    public NutsIOCompressAction setTarget(File target) {
        this.target = CoreIOUtils.createTarget(target);
        return this;
    }

    @Override
    public NutsIOCompressAction setTarget(String target) {
        this.target = CoreIOUtils.createTarget(target);
        return this;
    }

    public NutsIOCompressAction addSource(Object source) {
        this.sources.add(CoreIOUtils.createInputSource(source));
        return this;
    }

    public NutsIOCompressAction addSource(String source) {
        this.sources.add(CoreIOUtils.createInputSource(source));
        return this;
    }

    @Override
    public NutsIOCompressAction to(Object target) {
        return setTarget(target);
    }

    @Override
    public NutsIOCompressAction to(String target) {
        return setTarget(target);
    }

    @Override
    public Object getTarget() {
        return target;
    }

    public NutsIOCompressAction setTarget(Object target) {
        this.target = CoreIOUtils.createTarget(target);
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

    @Override
    public NutsIOCompressAction to(File target) {
        return setTarget(target);
    }

    @Override
    public NutsIOCompressAction to(OutputStream target) {
        return setTarget(target);
    }

    @Override
    public NutsIOCompressAction to(Path target) {
        return setTarget(target);
    }

    @Override
    public NutsIOCompressAction logProgress() {
        setLogProgress(true);
        return this;
    }

    @Override
    public NutsIOCompressAction logProgress(boolean value) {
        setLogProgress(value);
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsIOCompressAction session(NutsSession session) {
        return setSession(session);
    }

    @Override
    public NutsIOCompressAction setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NutsIOCompressAction run() {
        switch (getFormat()){
            case "zip":{
                runZip();
                break;
            }
            default:{
                throw new NutsUnsupportedArgumentException(iom.getWorkspace(),"Unsupported format "+getFormat());
            }
        }
        return this;
    }

    public void runZip() {
        if (sources.isEmpty()) {
            throw new UnsupportedOperationException("Missing Source");
        }
        if (target == null) {
            throw new UnsupportedOperationException("Missing Target");
        }
        if (isLogProgress() || getProgressMonitorFactory() != null) {
            //how to monitor???
        }
        LOG.with().level(Level.FINEST).verb(NutsLogVerb.START).log( "compress {0} to {1}", sources, target);
        try {
            OutputStream fW = null;
            ZipOutputStream zip = null;
            if (this.target.isPath()) {
                Path tempPath = null;
                if (isSafe()) {
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
            LOG.with().level(Level.CONFIG).verb(NutsLogVerb.FAIL).log( "Error compressing {0} to {1} : {2}", sources, target.getValue(), ex.toString());
            throw new UncheckedIOException(ex);
        }
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
     * set progress factory responsible of creating progress monitor
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NutsIOCompressAction progressMonitorFactory(NutsProgressFactory value) {
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
    public NutsIOCompressAction setProgressMonitor(NutsProgressMonitor value) {
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
    public NutsIOCompressAction progressMonitor(NutsProgressMonitor value) {
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
    public boolean isSafe() {
        return safe;
    }

    @Override
    public NutsIOCompressAction setSafe(boolean value) {
        this.safe = value;
        return this;
    }

    @Override
    public NutsIOCompressAction safe() {
        setSafe(true);
        return this;
    }

    @Override
    public NutsIOCompressAction safe(boolean value) {
        setSafe(value);
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
    public NutsIOCompressAction skipRoot(boolean value) {
        return setSkipRoot(value);
    }

    @Override
    public NutsIOCompressAction skipRoot() {
        return skipRoot(true);
    }

    @Override
    public boolean isSkipRoot() {
        return skipRoot;
    }

    @Override
    public NutsIOCompressAction setSkipRoot(boolean value) {
        this.skipRoot=true;
        return this;
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
        if (CoreStringUtils.isBlank(format)) {
            format = "zip";
        }
        if ("zip".equals(format)) {
            this.format = format;
        } else {
            throw new NutsUnsupportedArgumentException(iom.getWorkspace(), "Unsupported compression format " + format);
        }
        return this;
    }
}
