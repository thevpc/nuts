/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.bundles.jshell;

import java.io.File;

/**
 *
 * @author thevpc
 */
public class DefaultJShellCommandTypeResolver implements JShellCommandTypeResolver {

    @Override
    public JShellCommandType type(String item, JShellFileContext context) {
        String a = context.aliases().get(item);
        if (a != null) {
            return new JShellCommandType(item, "path", a, item + " is aliased to " + a);
        }
        String path = item;
        if (!item.startsWith("/")) {
            path = context.getCwd() + "/" + item;
        }
        if (new File(path).isFile()) {
            return new JShellCommandType(item, "path", path, item + " is " + path);
        }
        return null;
    }

}
