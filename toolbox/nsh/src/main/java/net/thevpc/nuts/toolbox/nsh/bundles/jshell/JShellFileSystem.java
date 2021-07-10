/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.bundles.jshell;

/**
 * @author thevpc
 */
public interface JShellFileSystem {

    String getInitialWorkingDir();

    String getHomeWorkingDir();

    boolean isAbsolute(String path);

    String getAbsolutePath(String path);

    boolean isDirectory(String path);

    boolean exists(String path);

}
