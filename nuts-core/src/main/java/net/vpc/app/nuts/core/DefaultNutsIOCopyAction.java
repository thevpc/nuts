/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.NutsIOCopyAction;
import net.vpc.app.nuts.NutsTerminalProvider;
import static net.vpc.app.nuts.core.util.CoreIOUtils.mkdirs;
import net.vpc.app.nuts.core.util.NutsIOHelper;
import net.vpc.common.io.FileValidationException;
import net.vpc.common.io.IOUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsIOCopyAction implements NutsIOCopyAction {

    private static final Logger log = Logger.getLogger(DefaultNutsIOCopyAction.class.getName());

    private Checker checker;
    private boolean safeCopy = true;
    private boolean monitorable = false;
    private NutsIOHelper.SourceItem source;
    private NutsIOHelper.TargetItem target;
    private DefaultNutsIOManager iom;
    private NutsTerminalProvider terminalProvider;

    public DefaultNutsIOCopyAction(DefaultNutsIOManager iom) {
        this.iom = iom;
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public NutsIOCopyAction setSource(InputStream source) {
        this.source = NutsIOHelper.createSource(source);
        return this;
    }

    @Override
    public NutsIOCopyAction setSource(File source) {
        this.source = NutsIOHelper.createSource(source);
        return this;
    }

    @Override
    public NutsIOCopyAction setSource(Path source) {
        this.source = NutsIOHelper.createSource(source);
        return this;
    }

    @Override
    public NutsIOCopyAction setSource(URL source) {
        this.source = NutsIOHelper.createSource(source);
        return this;
    }

    @Override
    public NutsIOCopyAction setTarget(OutputStream target) {
        this.target = NutsIOHelper.createTarget(target);
        return this;
    }

    @Override
    public NutsIOCopyAction setTarget(Path target) {
        this.target = NutsIOHelper.createTarget(target);
        return this;
    }

    @Override
    public NutsIOCopyAction setTarget(File target) {
        this.target = NutsIOHelper.createTarget(target);
        return this;
    }

    public DefaultNutsIOCopyAction setSource(Object source) {
        this.source = NutsIOHelper.createSource(source);
        return this;
    }

    @Override
    public NutsIOCopyAction from(String source) {
        this.source = NutsIOHelper.createSource(source);
        return this;
    }

    @Override
    public NutsIOCopyAction to(String target) {
        this.target = NutsIOHelper.createTarget(target);
        return this;
    }

    @Override
    public Object getTarget() {
        return target;
    }

//    @Override
    public DefaultNutsIOCopyAction setTarget(Object target) {

        return this;
    }

    @Override
    public Checker getChecker() {
        return checker;
    }

    @Override
    public DefaultNutsIOCopyAction setChecker(Checker checker) {
        this.checker = checker;
        return this;
    }

    @Override
    public boolean isSafeCopy() {
        return safeCopy;
    }

    @Override
    public DefaultNutsIOCopyAction setSafeCopy(boolean safeCopy) {
        this.safeCopy = safeCopy;
        return this;
    }

    @Override
    public boolean isMonitorable() {
        return monitorable;
    }

    @Override
    public DefaultNutsIOCopyAction setMonitorable(boolean monitorable) {
        this.monitorable = monitorable;
        return this;
    }

    @Override
    public NutsIOCopyAction from(InputStream source) {
        return setSource(source);
    }

    @Override
    public NutsIOCopyAction from(File source) {
        return setSource(source);
    }

    @Override
    public NutsIOCopyAction from(Path source) {
        return setSource(source);
    }

    @Override
    public NutsIOCopyAction from(URL source) {
        return setSource(source);
    }

    @Override
    public NutsIOCopyAction to(File target) {
        return setTarget(target);
    }

    @Override
    public NutsIOCopyAction to(OutputStream target) {
        return setTarget(target);
    }

    @Override
    public NutsIOCopyAction to(Path target) {
        return setTarget(target);
    }

    @Override
    public NutsIOCopyAction check(Checker validationVerifier) {
        return setChecker(validationVerifier);
    }

    @Override
    public NutsIOCopyAction safeCopy() {
        setSafeCopy(true);
        return this;
    }

    @Override
    public NutsIOCopyAction safeCopy(boolean safeCopy) {
        setSafeCopy(safeCopy);
        return this;
    }

    @Override
    public NutsIOCopyAction monitorable() {
        setMonitorable(true);
        return this;
    }

    @Override
    public NutsIOCopyAction monitorable(boolean safeCopy) {
        setMonitorable(safeCopy);
        return this;
    }

    @Override
    public NutsTerminalProvider getTerminalProvider() {
        return terminalProvider;
    }

    @Override
    public NutsIOCopyAction setTerminalProvider(NutsTerminalProvider terminalProvider) {
        this.terminalProvider = terminalProvider;
        return this;
    }

    @Override
    public void run() {
        NutsIOHelper.SourceItem _source = source;
        if (_source == null) {
            throw new UnsupportedOperationException("Missing Source");
        }
        if (target == null) {
            throw new UnsupportedOperationException("Missing Target");
        }
        boolean _target_isPath = target.isPath();
        if (checker != null && !_target_isPath && !safeCopy) {
            throw new IllegalArgumentException("Unsupported validation if not safeCopy not path target");
        }
        if (monitorable) {
            if (_source.isPath()) {
                _source = NutsIOHelper.createSource(iom.monitorInputStream(_source.getPath().toString(), _source.getPath().toString(), terminalProvider));
            } else if (_source.getValue() instanceof URL) {
                _source = NutsIOHelper.createSource(iom.monitorInputStream(_source.getValue().toString(), _source.getValue().toString(), terminalProvider));
            } else {
                _source = NutsIOHelper.createSource(iom.monitorInputStream(_source.getStream(), -1, _source.getValue().toString(), terminalProvider));
            }
        }
        boolean _source_isPath = _source.isPath();
        if (safeCopy) {
            Path temp = null;
            if (_target_isPath) {
                Path to = target.getPath();
                mkdirs(to.getParent());
                temp = to.resolveSibling(to.getFileName() + "~");
            } else {
                temp = iom.createTempFile("temp~");
            }
            try {
                try {
                    if (_source_isPath) {
                        Files.copy(_source.getPath(), temp, StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        Files.copy(_source.getStream(), temp, StandardCopyOption.REPLACE_EXISTING);
                    }
                    if (checker != null) {
                        try {
                            checker.check(temp);
                        } catch (Exception ex) {
                            if (ex instanceof FileValidationException) {
                                throw ex;
                            }
                            throw new FileValidationException("Validate file " + temp + " failed", ex);
                        }
                    }
                    if (_target_isPath) {
                        Files.move(temp, target.getPath(), StandardCopyOption.REPLACE_EXISTING);
                        temp = null;
                    } else {
                        Files.copy(temp, target.getStream());
                    }
                } finally {
                    if (temp != null) {
                        Files.delete(temp);
                    }
                }
            } catch (IOException ex) {
                log.log(Level.CONFIG, "[ERROR  ] Error copying {0} to {1} : {2}", new Object[]{_source.getValue(), target.getValue(), ex.toString()});
                throw new UncheckedIOException(ex);
            }
        } else {
            try {
                if (_target_isPath) {
                    Path to = target.getPath();
                    mkdirs(to.getParent());
                    if (_source_isPath) {
                        Files.copy(_source.getPath(), target.getPath(), StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        Files.copy(_source.getStream(), target.getPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                } else {
                    if (_source_isPath) {
                        Files.copy(_source.getPath(), target.getStream());
                    } else {
                        IOUtils.copy(_source.getStream(), target.getStream());
                    }
                }
                if (checker != null) {
                    try {
                        checker.check(target.getPath());
                    } catch (Exception ex) {
                        if (ex instanceof FileValidationException) {
                            throw ex;
                        }
                        throw new FileValidationException("Validate file " + target.getValue() + " failed", ex);
                    }
                }
            } catch (IOException ex) {
                log.log(Level.CONFIG, "[ERROR  ] Error copying {0} to {1} : {2}", new Object[]{_source.getValue(), target.getValue(), ex.toString()});
                throw new UncheckedIOException(ex);
            }
        }
    }
}
