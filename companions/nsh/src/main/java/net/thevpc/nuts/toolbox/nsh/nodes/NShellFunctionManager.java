/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.nodes;

/**
 *
 * @author thevpc
 */
public interface NShellFunctionManager {

    NShellFunction findFunction(String command);

    boolean containsFunction(String cmd);

    void declareFunction(NShellFunction cmd);

    void declareFunctions(NShellFunction... cmds);

    boolean unset(String name);

    NShellFunction[] getAll();

}
