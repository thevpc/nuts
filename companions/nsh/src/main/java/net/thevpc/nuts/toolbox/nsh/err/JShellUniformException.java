/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.err;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NSession;

/**
 *
 * @author thevpc
 */
public class JShellUniformException extends JShellException {

    private boolean quit;

    public JShellUniformException(NSession session, int code, boolean quit, Throwable cause) {
        super(session, NMsg.ofPlain("error"),cause,code);
        this.quit = quit;
    }

    public void throwQuit() {
        if (getCause() instanceof JShellQuitException) {
            throw (JShellQuitException) getCause();
        }
        if (getCause() instanceof RuntimeException) {
            throw (RuntimeException) getCause();
        }
        throw new JShellQuitException(getSession(), getCause(), getExitCode());
    }

    public void throwAny() {
        if (getCause() instanceof JShellQuitException) {
            throw (JShellQuitException) getCause();
        }
        if (getCause() instanceof RuntimeException) {
            throw (RuntimeException) getCause();
        }
        throw new JShellException(getSession(),getFormattedMessage(), getCause(),getExitCode());
    }

    public boolean isQuit() {
        return quit;
    }

}