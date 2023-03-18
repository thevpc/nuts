/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.cmdresolver;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.nsh.eval.NShellContext;

/**
 *
 * @author thevpc
 */
public class DefaultNShellCommandTypeResolver implements NShellCommandTypeResolver {

    @Override
    public NShellCommandResolution type(String item, NShellContext context) {
        String a = context.aliases().get(item);
        if (a != null) {
            return new NShellCommandResolution(item, "path", a, item + " is aliased to " + a);
        }
        NPath path = NPath.of(item,context.getSession()).toAbsolute(context.getDirectory());
        if (path.exists()) {
            return new NShellCommandResolution(item, "path", path.toString(), item + " is " + path);
        }
        return null;
    }

}
