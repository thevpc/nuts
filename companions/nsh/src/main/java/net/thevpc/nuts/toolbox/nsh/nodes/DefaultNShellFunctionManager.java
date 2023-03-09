/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.nodes;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author thevpc
 */
public class DefaultNShellFunctionManager implements NShellFunctionManager {

    public final Map internalFunctions = new HashMap();

    @Override
    public NShellFunction findFunction(String n) {
        return (NShellFunction) internalFunctions.get(n);
    }

    @Override
    public void declareFunction(NShellFunction cmd) {
        synchronized (internalFunctions) {
            internalFunctions.put(cmd.getName(), cmd);
        }
    }

    @Override
    public void declareFunctions(NShellFunction... cmds) {
        synchronized (internalFunctions) {
            for (NShellFunction cmd : cmds) {
                internalFunctions.put(cmd.getName(), cmd);
            }
        }
    }

    @Override
    public boolean containsFunction(String cmd) {
        synchronized (internalFunctions) {
            return internalFunctions.containsKey(cmd);
        }
    }

    @Override
    public boolean unset(String cmd) {
        synchronized (internalFunctions) {
            return internalFunctions.remove(cmd) != null;
        }
    }

    @Override
    public NShellFunction[] getAll() {
        return (NShellFunction[]) internalFunctions.values().toArray(new NShellFunction[0]);
    }
}
