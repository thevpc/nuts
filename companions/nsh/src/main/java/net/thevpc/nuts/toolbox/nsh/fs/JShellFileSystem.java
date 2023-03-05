/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.fs;

import net.thevpc.nuts.NSession;

/**
 * @author thevpc
 */
public interface JShellFileSystem {

    String getInitialWorkingDir(NSession session);

    String getHomeWorkingDir(NSession session);

    boolean isAbsolute(String path, NSession session);

    String getAbsolutePath(String path, NSession session);

    boolean isDirectory(String path, NSession session);

    boolean exists(String path, NSession session);

}
