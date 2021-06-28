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
public interface JShellFunctionManager {

    JShellFunction findFunction(String command);

    boolean containsFunction(String cmd);

    void declareFunction(JShellFunction cmd);

    void declareFunctions(JShellFunction... cmds);

    boolean unset(String name);

    JShellFunction[] getAll();

}
