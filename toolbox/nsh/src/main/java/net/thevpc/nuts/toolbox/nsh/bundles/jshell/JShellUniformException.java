/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.bundles.jshell;

/**
 *
 * @author thevpc
 */
public class JShellUniformException extends JShellException {

    private boolean quit;

    public JShellUniformException(int code, boolean quit, Throwable cause) {
        super(code,cause);
        this.quit = quit;
    }

    public void throwQuit() {
        if (getCause() instanceof JShellQuitException) {
            throw (JShellQuitException) getCause();
        }
        if (getCause() instanceof RuntimeException) {
            throw (RuntimeException) getCause();
        }
        throw new JShellQuitException(getResult(), getCause());
    }

    public void throwAny() {
        if (getCause() instanceof JShellQuitException) {
            throw (JShellQuitException) getCause();
        }
        if (getCause() instanceof RuntimeException) {
            throw (RuntimeException) getCause();
        }
        throw new JShellException(getResult(), getCause());
    }

    public boolean isQuit() {
        return quit;
    }

}
