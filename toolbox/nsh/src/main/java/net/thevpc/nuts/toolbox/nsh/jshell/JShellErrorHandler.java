/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.jshell;

/**
 *
 * @author thevpc
 */
public interface JShellErrorHandler {

    int errorToCode(Throwable th);
    
    String errorToMessage(Throwable th);

    void onError(String message, Throwable th, JShellContext context);

    boolean isQuitException(Throwable th);
}
