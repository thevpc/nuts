/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.fs;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.NSession;

/**
 *
 * @author thevpc
 */
public class DefaultNShellFileSystem implements NShellFileSystem {

    @Override
    public String getInitialWorkingDir(NSession session) {
        return System.getProperty("user.dir");
    }

    @Override
    public String getHomeWorkingDir(NSession session) {
        return System.getProperty("user.home");
    }

    @Override
    public String getAbsolutePath(String path, NSession session) {
        return NPath.of(path).normalize().toString();
    }

    @Override
    public boolean isAbsolute(String path, NSession session) {
        return NPath.of(path).isAbsolute();
    }

    @Override
    public boolean isDirectory(String path, NSession session) {
        return NPath.of(path).isDirectory();
    }

    @Override
    public boolean exists(String path, NSession session) {
        return NPath.of(path).exists();
    }
}
