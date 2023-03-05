/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.cmds;

import net.thevpc.nuts.toolbox.nsh.cmds.JShellBuiltin;

/**
 *
 * @author thevpc
 */
public interface JShellBuiltinManager {

    JShellBuiltin find(String command);
    
    JShellBuiltin get(String command);

    boolean contains(String cmd);

    void set(JShellBuiltin cmd);

    void set(JShellBuiltin... cmds);

    boolean unset(String name);

    JShellBuiltin[] getAll();

}
