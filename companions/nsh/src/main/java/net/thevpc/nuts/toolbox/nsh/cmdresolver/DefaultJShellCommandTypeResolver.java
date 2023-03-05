/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.cmdresolver;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellContext;

/**
 *
 * @author thevpc
 */
public class DefaultJShellCommandTypeResolver implements JShellCommandTypeResolver {

    @Override
    public JShellCommandResolution type(String item, JShellContext context) {
        String a = context.aliases().get(item);
        if (a != null) {
            return new JShellCommandResolution(item, "path", a, item + " is aliased to " + a);
        }
        NPath path = NPath.of(item,context.getSession()).toAbsolute(context.getCwd());
        if (path.exists()) {
            return new JShellCommandResolution(item, "path", path.toString(), item + " is " + path);
        }
        return null;
    }

}
