/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.jshell;

import net.thevpc.nuts.io.NPath;

/**
 *
 * @author thevpc
 */
public class DefaultJShellCommandTypeResolver implements JShellCommandTypeResolver {

    @Override
    public JShellCommandType type(String item, JShellContext context) {
        String a = context.aliases().get(item);
        if (a != null) {
            return new JShellCommandType(item, "path", a, item + " is aliased to " + a);
        }
        NPath path = NPath.of(item,context.getSession()).toAbsolute(context.getCwd());
        if (path.exists()) {
            return new JShellCommandType(item, "path", path.toString(), item + " is " + path);
        }
        return null;
    }

}
