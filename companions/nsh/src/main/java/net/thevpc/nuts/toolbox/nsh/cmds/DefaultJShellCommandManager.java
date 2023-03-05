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
public class DefaultJShellCommandManager implements JShellBuiltinManager {

    public final Map internalCommands = new HashMap();

    @Override
    public JShellBuiltin find(String n) {
        return (JShellBuiltin) internalCommands.get(n);
    }

    @Override
    public void set(JShellBuiltin cmd) {
        synchronized (internalCommands) {
            internalCommands.put(cmd.getName(), cmd);
        }
    }

    @Override
    public void set(JShellBuiltin... cmds) {
        synchronized (internalCommands) {
            for (JShellBuiltin cmd : cmds) {
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
    public JShellBuiltin[] getAll() {
        return (JShellBuiltin[]) internalCommands.values().toArray(new JShellBuiltin[0]);
    }

    public JShellBuiltin get(String cmd) {
        JShellBuiltin command = find(cmd);
        if (command == null) {
            throw new NoSuchElementException("JShellCommandNode not found : " + cmd);
        }
        if (!command.isEnabled()) {
            throw new NoSuchElementException("JShellCommandNode disabled : " + cmd);
        }
        return command;
    }
}
