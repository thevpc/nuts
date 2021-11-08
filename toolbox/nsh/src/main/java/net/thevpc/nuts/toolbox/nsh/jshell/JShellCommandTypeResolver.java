/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.jshell;

/**
 *
 * @author thevpc
 */
public interface JShellCommandTypeResolver {

    JShellCommandType type(String path0, JShellContext context);

}
