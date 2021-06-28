/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.bundles.jshell;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author thevpc
 */
public class DefaultJShellFunctionManager implements JShellFunctionManager {

    public final Map internalFunctions = new HashMap();

    @Override
    public JShellFunction findFunction(String n) {
        return (JShellFunction) internalFunctions.get(n);
    }

    @Override
    public void declareFunction(JShellFunction cmd) {
        synchronized (internalFunctions) {
            internalFunctions.put(cmd.getName(), cmd);
        }
    }

    @Override
    public void declareFunctions(JShellFunction... cmds) {
        synchronized (internalFunctions) {
            for (JShellFunction cmd : cmds) {
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
    public JShellFunction[] getAll() {
        return (JShellFunction[]) internalFunctions.values().toArray(new JShellFunction[0]);
    }
}
