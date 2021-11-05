/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.bundles.jshell;

import net.thevpc.nuts.NutsSession;

/**
 * @author thevpc
 */
public interface JShellFileSystem {

    String getInitialWorkingDir(NutsSession session);

    String getHomeWorkingDir(NutsSession session);

    boolean isAbsolute(String path, NutsSession session);

    String getAbsolutePath(String path, NutsSession session);

    boolean isDirectory(String path, NutsSession session);

    boolean exists(String path, NutsSession session);

}
