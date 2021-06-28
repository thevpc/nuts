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
public class DefaultJShellErrorHandler implements JShellErrorHandler {

    @Override
    public boolean isRequireExit(Throwable th) {
        return th instanceof JShellQuitException;
    }

    @Override
    public int errorToCode(Throwable th) {
        if (th instanceof JShellException) {
            return ((JShellException) th).getResult();
        }
        return 1;
    }

    @Override
    public String errorToMessage(Throwable th) {
        return th.toString();
    }

    @Override
    public void onError(String message, Throwable th, JShellFileContext context) {
        th.printStackTrace();
    }
}
