/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.cmds;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 *
 * @author thevpc
 */
public class DefaultNShellCommandManager implements NShellBuiltinManager {

    public final Map internalCommands = new HashMap();

    @Override
    public NShellBuiltin find(String n) {
        return (NShellBuiltin) internalCommands.get(n);
    }

    @Override
    public void set(NShellBuiltin cmd) {
        synchronized (internalCommands) {
            internalCommands.put(cmd.getName(), cmd);
        }
    }

    @Override
    public void set(NShellBuiltin... cmds) {
        synchronized (internalCommands) {
            for (NShellBuiltin cmd : cmds) {
                internalCommands.put(cmd.getName(), cmd);
            }
        }
    }

    @Override
    public boolean contains(String cmd) {
        synchronized (internalCommands) {
            return internalCommands.containsKey(cmd);
        }
    }

    @Override
    public boolean unset(String cmd) {
        synchronized (internalCommands) {
            return internalCommands.remove(cmd) != null;
        }
    }

    @Override
    public NShellBuiltin[] getAll() {
        return (NShellBuiltin[]) internalCommands.values().toArray(new NShellBuiltin[0]);
    }

    public NShellBuiltin get(String cmd) {
        NShellBuiltin command = find(cmd);
        if (command == null) {
            throw new NoSuchElementException("NShellCommandNode not found : " + cmd);
        }
        if (!command.isEnabled()) {
            throw new NoSuchElementException("NShellCommandNode disabled : " + cmd);
        }
        return command;
    }
}
