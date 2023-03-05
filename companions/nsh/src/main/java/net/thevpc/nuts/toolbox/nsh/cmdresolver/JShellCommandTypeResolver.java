/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.cmdresolver;

import net.thevpc.nuts.toolbox.nsh.jshell.JShellContext;

/**
 *
 * @author thevpc
 */
public interface JShellCommandTypeResolver {

    JShellCommandResolution type(String path0, JShellContext context);

}
