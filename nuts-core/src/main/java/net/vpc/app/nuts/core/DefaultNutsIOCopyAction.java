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
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsTerminalProvider;
import net.vpc.app.nuts.NutsPathCopyAction;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.io.InputSource;

/**
 *
 * @author vpc
 */
public class DefaultNutsIOCopyAction implements NutsPathCopyAction {

    private static final Logger log = Logger.getLogger(DefaultNutsIOCopyAction.class.getName());

    private Validator checker;
    private boolean safeCopy = true;
    private boolean monitorable = false;
    private InputSource source;
    private CoreIOUtils.TargetItem target;
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
    public NutsPathCopyAction setSource(InputStream source) {
        this.source = CoreIOUtils.createInputSource(source);
        return this;
    }

    @Override
    public NutsPathCopyAction setSource(File source) {
        this.source = CoreIOUtils.createInputSource(source);
        return this;
    }

    @Override
    public NutsPathCopyAction setSource(Path source) {
        this.source = CoreIOUtils.createInputSource(source);
        return this;
    }

    @Override
    public NutsPathCopyAction setSource(URL source) {
        this.source = CoreIOUtils.createInputSource(source);
        return this;
    }

    @Override
    public NutsPathCopyAction setTarget(OutputStream target) {
        this.target = CoreIOUtils.createTarget(target);
        return this;
    }

    @Override
    public NutsPathCopyAction setTarget(Path target) {
        this.target = CoreIOUtils.createTarget(target);
        return this;
    }

    @Override
    public NutsPathCopyAction setTarget(File target) {
        this.target = CoreIOUtils.createTarget(target);
        return this;
    }

    public DefaultNutsIOCopyAction setSource(Object source) {
        this.source = CoreIOUtils.createInputSource(source);
        return this;
    }

    @Override
    public NutsPathCopyAction from(String source) {
        this.source = CoreIOUtils.createInputSource(source);
        return this;
    }

    @Override
    public NutsPathCopyAction to(String target) {
        this.target = CoreIOUtils.createTarget(target);
        return this;
    }

    @Override
    public NutsPathCopyAction from(Object source) {
        this.source = CoreIOUtils.createInputSource(source);
        return this;
    }

    @Override
    public NutsPathCopyAction to(Object target) {
        this.target = CoreIOUtils.createTarget(target);
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
    public Validator getChecker() {
        return checker;
    }

    @Override
    public DefaultNutsIOCopyAction setValidator(Validator checker) {
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
    public NutsPathCopyAction from(InputStream source) {
        return setSource(source);
    }

    @Override
    public NutsPathCopyAction from(File source) {
        return setSource(source);
    }

    @Override
    public NutsPathCopyAction from(Path source) {
        return setSource(source);
    }

    @Override
    public NutsPathCopyAction from(URL source) {
        return setSource(source);
    }

    @Override
    public NutsPathCopyAction to(File target) {
        return setTarget(target);
    }

    @Override
    public NutsPathCopyAction to(OutputStream target) {
        return setTarget(target);
    }

    @Override
    public NutsPathCopyAction to(Path target) {
        return setTarget(target);
    }

    @Override
    public NutsPathCopyAction validator(Validator validationVerifier) {
        return setValidator(validationVerifier);
    }

    @Override
    public NutsPathCopyAction safeCopy() {
        setSafeCopy(true);
        return this;
    }

    @Override
    public NutsPathCopyAction safeCopy(boolean safeCopy) {
        setSafeCopy(safeCopy);
        return this;
    }

    @Override
    public NutsPathCopyAction monitorable() {
        setMonitorable(true);
        return this;
    }

    @Override
    public NutsPathCopyAction monitorable(boolean safeCopy) {
        setMonitorable(safeCopy);
        return this;
    }

    @Override
    public NutsTerminalProvider getTerminalProvider() {
        return terminalProvider;
    }

    @Override
    public NutsPathCopyAction setTerminalProvider(NutsTerminalProvider terminalProvider) {
        this.terminalProvider = terminalProvider;
        return this;
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
        boolean _target_isPath = target.isPath();
        if (checker != null && !_target_isPath && !safeCopy) {
            throw new NutsIllegalArgumentException("Unsupported validation if neither safeCopy is armed nor path is defined");
        }
        if (monitorable) {
            if (_source.isPath()) {
                _source = CoreIOUtils.createInputSource(iom.monitorInputStream(_source.getPath().toString(), _source.getPath().toString(), terminalProvider));
            } else if (_source.isURL()) {
                _source = CoreIOUtils.createInputSource(iom.monitorInputStream(_source.getURL().toString(), _source.getSource().toString(), terminalProvider));
            } else {
                _source = CoreIOUtils.createInputSource(iom.monitorInputStream(_source.open(), terminalProvider));
            }
        }
        boolean _source_isPath = _source.isPath();
        if (safeCopy) {
            Path temp = null;
            if (_target_isPath) {
                Path to = target.getPath();
                CoreIOUtils.mkdirs(to.getParent());
                temp = to.resolveSibling(to.getFileName() + "~");
            } else {
                temp = iom.createTempFile("temp~");
            }
            try {
                try {
                    if (_source_isPath) {
                        Files.copy(_source.getPath(), temp, StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        try (InputStream ins = _source.open()) {
                            Files.copy(ins, temp, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                    if (checker != null) {
                        try {
                            checker.validate(temp);
                        } catch (Exception ex) {
                            if (ex instanceof ValidationException) {
                                throw ex;
                            }
                            throw new ValidationException("Validate file " + temp + " failed", ex);
                        }
                    }
                    if (_target_isPath) {
                        Files.move(temp, target.getPath(), StandardCopyOption.REPLACE_EXISTING);
                        temp = null;
                    } else {
                        try (OutputStream ops = target.open()) {
                            Files.copy(temp, ops);
                        }
                    }
                } finally {
                    if (temp != null) {
                        Files.delete(temp);
                    }
                }
            } catch (IOException ex) {
                log.log(Level.CONFIG, "[ERROR  ] Error copying {0} to {1} : {2}", new Object[]{_source.getSource(), target.getValue(), ex.toString()});
                throw new UncheckedIOException(ex);
            }
        } else {
            try {
                if (_target_isPath) {
                    Path to = target.getPath();
                    CoreIOUtils.mkdirs(to.getParent());
                    if (_source_isPath) {
                        Files.copy(_source.getPath(), target.getPath(), StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        try (InputStream ins = _source.open()) {
                            Files.copy(ins, target.getPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                } else {
                    if (_source_isPath) {
                        try (OutputStream ops = target.open()) {
                            Files.copy(_source.getPath(), ops);
                        }
                    } else {
                        try (InputStream ins = _source.open()) {
                            try (OutputStream ops = target.open()) {
                                CoreIOUtils.copy(ins, ops);
                            }
                        }
                    }
                }
                if (checker != null) {
                    try {
                        checker.validate(target.getPath());
                    } catch (Exception ex) {
                        if (ex instanceof ValidationException) {
                            throw ex;
                        }
                        throw new ValidationException("Validate file " + target.getValue() + " failed", ex);
                    }
                }
            } catch (IOException ex) {
                log.log(Level.CONFIG, "[ERROR  ] Error copying {0} to {1} : {2}", new Object[]{_source.getSource(), target.getValue(), ex.toString()});
                throw new UncheckedIOException(ex);
            }
        }
    }
}
