/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.err;

import net.thevpc.nuts.util.NMsg;

/**
 *
 * @author thevpc
 */
public class NShellUniformException extends NShellException {

    private boolean quit;

    public NShellUniformException(int code, boolean quit, Throwable cause) {
        super(NMsg.ofPlain("error"),cause,code);
        this.quit = quit;
    }

    public void throwQuit() {
        if (getCause() instanceof NShellQuitException) {
            throw (NShellQuitException) getCause();
        }
        if (getCause() instanceof RuntimeException) {
            throw (RuntimeException) getCause();
        }
        throw new NShellQuitException(getCause(), getExitCode());
    }

    public void throwAny() {
        if (getCause() instanceof NShellQuitException) {
            throw (NShellQuitException) getCause();
        }
        if (getCause() instanceof RuntimeException) {
            throw (RuntimeException) getCause();
        }
        throw new NShellException(getFormattedMessage(), getCause(),getExitCode());
    }

    public boolean isQuit() {
        return quit;
    }

}
