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
public interface NutsPathCopyAction {

    Object getSource();

    NutsPathCopyAction setSource(InputStream source);

    NutsPathCopyAction setSource(File source);

    NutsPathCopyAction setSource(Path source);

    NutsPathCopyAction setSource(URL source);

    NutsPathCopyAction from(String source);

    NutsPathCopyAction from(InputStream source);

    NutsPathCopyAction from(File source);

    NutsPathCopyAction from(Path source);

    NutsPathCopyAction from(URL source);

    Object getTarget();

//    NutsIOCopyAction setTarget(Object target);
    NutsPathCopyAction setTarget(OutputStream target);

    NutsPathCopyAction setTarget(Path target);

    NutsPathCopyAction setTarget(File target);

//    NutsIOCopyAction to(Object target);
    NutsPathCopyAction to(OutputStream target);

    NutsPathCopyAction to(String target);

    NutsPathCopyAction to(Path target);

    NutsPathCopyAction to(File target);

    Checker getChecker();

    NutsPathCopyAction check(Checker validationVerifier);

    NutsPathCopyAction setChecker(Checker validationVerifier);

    boolean isSafeCopy();

    NutsPathCopyAction safeCopy();

    NutsPathCopyAction safeCopy(boolean safeCopy);

    NutsPathCopyAction setSafeCopy(boolean safeCopy);

    NutsTerminalProvider getTerminalProvider();

    NutsPathCopyAction setTerminalProvider(NutsTerminalProvider terminalProvider);

    void run();

    NutsPathCopyAction monitorable(boolean safeCopy);

    NutsPathCopyAction monitorable();

    boolean isMonitorable();

    NutsPathCopyAction setMonitorable(boolean monitorable);

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
