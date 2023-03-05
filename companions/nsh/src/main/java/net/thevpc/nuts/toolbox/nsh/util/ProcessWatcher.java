/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.util;

import java.io.IOException;

/**
 *
 * @author thevpc
 */
public class ProcessWatcher {

    public static int runAndWait(ExecProcessInfo theHandler) throws IOException {
        Process process = Runtime.getRuntime().exec(
                        theHandler.cmdarray,
                        theHandler.envp,
                        theHandler.dir
                );
        ProcessWatcher2 w = new ProcessWatcher2(process, theHandler);
        w.start();
        return w.waitfor();
    }

    public static int runAndWait(Process theProcess, ProcessStringsHandler theHandler) {
        ProcessWatcher1 w = new ProcessWatcher1(theProcess, theHandler);
        w.start();
        return w.waitfor();
    }

    public static void runAsynch(Process theProcess, ProcessStringsHandler theHandler) {
        ProcessWatcher1 w = new ProcessWatcher1(theProcess, theHandler);
        w.start();
    }
}
