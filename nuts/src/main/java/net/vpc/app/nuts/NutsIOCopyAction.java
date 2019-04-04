/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;

/**
 *
 * @author vpc
 */
public interface NutsIOCopyAction {

    Object getSource();

    NutsIOCopyAction setSource(InputStream source);

    NutsIOCopyAction setSource(File source);

    NutsIOCopyAction setSource(Path source);

    NutsIOCopyAction setSource(URL source);

    NutsIOCopyAction from(String source);

    NutsIOCopyAction from(InputStream source);

    NutsIOCopyAction from(File source);

    NutsIOCopyAction from(Path source);

    NutsIOCopyAction from(URL source);

    Object getTarget();

//    NutsIOCopyAction setTarget(Object target);
    NutsIOCopyAction setTarget(OutputStream target);

    NutsIOCopyAction setTarget(Path target);

    NutsIOCopyAction setTarget(File target);

//    NutsIOCopyAction to(Object target);
    NutsIOCopyAction to(OutputStream target);

    NutsIOCopyAction to(String target);

    NutsIOCopyAction to(Path target);

    NutsIOCopyAction to(File target);

    Checker getChecker();

    NutsIOCopyAction check(Checker validationVerifier);

    NutsIOCopyAction setChecker(Checker validationVerifier);

    boolean isSafeCopy();

    NutsIOCopyAction safeCopy();

    NutsIOCopyAction safeCopy(boolean safeCopy);

    NutsIOCopyAction setSafeCopy(boolean safeCopy);

    NutsTerminalProvider getTerminalProvider();

    NutsIOCopyAction setTerminalProvider(NutsTerminalProvider terminalProvider);

    void run();

    NutsIOCopyAction monitorable(boolean safeCopy);

    NutsIOCopyAction monitorable();

    boolean isMonitorable();

    NutsIOCopyAction setMonitorable(boolean monitorable);

    public static class ValidationException extends RuntimeException {

        public ValidationException() {
        }

        public ValidationException(String message) {
            super(message);
        }

        public ValidationException(String message, Throwable cause) {
            super(message, cause);
        }

        public ValidationException(Throwable cause) {
            super(cause);
        }

    }

    public static interface Checker {

        void check(Path path) throws ValidationException;
    }

}
