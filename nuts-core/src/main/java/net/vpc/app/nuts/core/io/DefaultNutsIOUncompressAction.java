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
import java.util.List;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author vpc
 */
public class DefaultNutsIOUncompressAction implements NutsPathUncompressAction {

    private final NutsLogger LOG;

    private boolean skipRoot = false;
    private boolean safeCopy = true;
    private boolean monitorable = false;
    private InputSource source;
    private CoreIOUtils.TargetItem target;
    private DefaultNutsIOManager iom;
    private NutsSession session;
    private boolean includeDefaultMonitorFactory;
    private NutsInputStreamProgressFactory progressMonitorFactory;

    public DefaultNutsIOUncompressAction(DefaultNutsIOManager iom) {
        this.iom = iom;
        LOG = iom.getWorkspace().log().of(DefaultNutsIOUncompressAction.class);
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public NutsPathUncompressAction setSource(InputStream source) {
        this.source = CoreIOUtils.createInputSource(source);
        return this;
    }

    @Override
    public NutsPathUncompressAction setSource(File source) {
        this.source = CoreIOUtils.createInputSource(source);
        return this;
    }

    @Override
    public NutsPathUncompressAction setSource(Path source) {
        this.source = CoreIOUtils.createInputSource(source);
        return this;
    }

    @Override
    public NutsPathUncompressAction setSource(URL source) {
        this.source = CoreIOUtils.createInputSource(source);
        return this;
    }

    @Override
    public NutsPathUncompressAction setTarget(Path target) {
        this.target = CoreIOUtils.createTarget(target);
        return this;
    }

    @Override
    public NutsPathUncompressAction setTarget(String target) {
        this.target = CoreIOUtils.createTarget(target);
        return this;
    }

    @Override
    public NutsPathUncompressAction setTarget(File target) {
        this.target = CoreIOUtils.createTarget(target);
        return this;
    }

    public DefaultNutsIOUncompressAction setSource(Object source) {
        this.source = CoreIOUtils.createInputSource(source);
        return this;
    }

    @Override
    public NutsPathUncompressAction from(String source) {
        this.source = CoreIOUtils.createInputSource(source);
        return this;
    }

    @Override
    public NutsPathUncompressAction to(String target) {
        this.target = CoreIOUtils.createTarget(target);
        return this;
    }

    @Override
    public NutsPathUncompressAction from(Object source) {
        this.source = CoreIOUtils.createInputSource(source);
        return this;
    }

    @Override
    public NutsPathUncompressAction to(Object target) {
        this.target = CoreIOUtils.createTarget(target);
        return this;
    }

    @Override
    public Object getTarget() {
        return target;
    }

    //    @Override
    public DefaultNutsIOUncompressAction setTarget(Object target) {
        this.target = CoreIOUtils.createTarget(target);
        return this;
    }

    @Override
    public boolean isMonitorable() {
        return monitorable;
    }

    @Override
    public DefaultNutsIOUncompressAction setMonitorable(boolean monitorable) {
        this.monitorable = monitorable;
        return this;
    }

    @Override
    public NutsPathUncompressAction from(InputStream source) {
        return setSource(source);
    }

    @Override
    public NutsPathUncompressAction from(File source) {
        return setSource(source);
    }

    @Override
    public NutsPathUncompressAction from(Path source) {
        return setSource(source);
    }

    @Override
    public NutsPathUncompressAction from(URL source) {
        return setSource(source);
    }

    @Override
    public NutsPathUncompressAction to(File target) {
        return setTarget(target);
    }

    @Override
    public NutsPathUncompressAction to(Path target) {
        return setTarget(target);
    }

    @Override
    public boolean isSafeCopy() {
        return safeCopy;
    }

    @Override
    public DefaultNutsIOUncompressAction setSafeCopy(boolean safeCopy) {
        this.safeCopy = safeCopy;
        return this;
    }

    @Override
    public NutsPathUncompressAction safeCopy() {
        setSafeCopy(true);
        return this;
    }

    @Override
    public NutsPathUncompressAction safeCopy(boolean safeCopy) {
        setSafeCopy(safeCopy);
        return this;
    }

    @Override
    public NutsPathUncompressAction monitorable() {
        setMonitorable(true);
        return this;
    }

    @Override
    public NutsPathUncompressAction monitorable(boolean safeCopy) {
        setMonitorable(safeCopy);
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsPathUncompressAction session(NutsSession session) {
        return setSession(session);
    }

    @Override
    public NutsPathUncompressAction setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    /**
     * Unzip it
     *
     * @param zipFile      input zip file
     * @param outputFolder zip file output folder
     */
    public static void unzip(NutsWorkspace ws, String zipFile, String outputFolder, UnzipOptions options) throws IOException {
    }

    @Override
    public void run() {
        InputSource _source = source;
        if (_source == null) {
            throw new UnsupportedOperationException("Missing Source");
        }
        if (target == null) {
            throw new UnsupportedOperationException("Missing Target");
        }
        if (isMonitorable() || getProgressMonitorFactory() != null) {
            if (_source.isPath()) {
                _source = CoreIOUtils.createInputSource(iom.monitor().source(_source.getPath().toString()).session(session)
                        .progressFactory(getProgressMonitorFactory())
                        .includeDefaultFactory(isIncludeDefaultMonitorFactory())
                        .create());
            } else if (_source.isURL()) {
                _source = CoreIOUtils.createInputSource(iom.monitor().source(_source.getURL().toString()).session(session)
                        .progressFactory(getProgressMonitorFactory())
                        .includeDefaultFactory(isIncludeDefaultMonitorFactory())
                        .create());
            } else {
                _source = CoreIOUtils.createInputSource(iom.monitor().source(_source.open()).session(session)
                        .progressFactory(getProgressMonitorFactory())
                        .includeDefaultFactory(isIncludeDefaultMonitorFactory())
                        .create());
            }
        }
        //boolean _source_isPath = _source.isPath();
//        if (!path.toLowerCase().startsWith("file://")) {
//            LOG.log(Level.FINE, "downloading url {0} to file {1}", new Object[]{path, file});
//        } else {
        LOG.log(Level.FINEST, NutsLogVerb.START, "Copy {0} to {1}", _source, target);
//        }
        List<Path> created = new ArrayList<>();
        try {

            byte[] buffer = new byte[1024];

            //create output directory is not exists
            Path folder = target.getPath();
            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }

            //get the zip file content
            InputStream _in = _source.open();
            try {
                try (ZipInputStream zis = new ZipInputStream(_in)) {
                    //get the zipped file list entry
                    ZipEntry ze = zis.getNextEntry();
                    String root = null;
                    while (ze != null) {

                        String fileName = ze.getName();
                        if (skipRoot) {
                            if (root == null) {
                                if (fileName.endsWith("/")) {
                                    root = fileName;
                                    ze = zis.getNextEntry();
                                    continue;
                                } else {
                                    throw new IOException("tot a single root zip");
                                }
                            }
                            if (fileName.startsWith(root)) {
                                fileName = fileName.substring(root.length());
                            } else {
                                throw new IOException("tot a single root zip");
                            }
                        }
                        if (fileName.endsWith("/")) {
                            Path newFile = folder.resolve(fileName);
                            Files.createDirectories(newFile);
                        } else {
                            Path newFile = folder.resolve(fileName);
                            iom.getWorkspace().log().of(ZipUtils.class).log(Level.FINEST, NutsLogVerb.WARNING, "file unzip : " + newFile);
                            //create all non exists folders
                            //else you will hit FileNotFoundException for compressed folder
                            if (newFile.getParent() != null) {
                                Files.createDirectories(newFile.getParent());
                            }
                            try (OutputStream fos = Files.newOutputStream(newFile)) {
                                int len;
                                while ((len = zis.read(buffer)) > 0) {
                                    fos.write(buffer, 0, len);
                                }
                            }
                        }
                        ze = zis.getNextEntry();
                    }
                    zis.closeEntry();
                }
            }finally {
                _in.close();
            }
        } catch (IOException ex) {
            LOG.log(Level.CONFIG, NutsLogVerb.FAIL, "Error copying {0} to {1} : {2}", _source.getSource(), target.getValue(), ex.toString());
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
    public NutsPathUncompressAction setIncludeDefaultMonitorFactory(boolean value) {
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
    public NutsPathUncompressAction includeDefaultMonitorFactory(boolean value) {
        return setIncludeDefaultMonitorFactory(value);
    }

    /**
     * always include default factory (console) even if progressMonitorFactory is defined
     *
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NutsPathUncompressAction includeDefaultMonitorFactory() {
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
    public NutsPathUncompressAction setProgressMonitorFactory(NutsInputStreamProgressFactory value) {
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
    public NutsPathUncompressAction progressMonitorFactory(NutsInputStreamProgressFactory value) {
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
    public NutsPathUncompressAction setProgressMonitor(NutsProgressMonitor value) {
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
    public NutsPathUncompressAction progressMonitor(NutsProgressMonitor value) {
        return setProgressMonitor(value);
    }

    @Override
    public NutsPathUncompressAction skipRoot(boolean value) {
        return setSkipRoot(value);
    }

    @Override
    public NutsPathUncompressAction skipRoot() {
        return skipRoot(true);
    }

    @Override
    public boolean isSkipRoot() {
        return skipRoot;
    }

    @Override
    public NutsPathUncompressAction setSkipRoot(boolean value) {
        this.skipRoot=true;
        return this;
    }
}
