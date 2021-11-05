/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.bundles.jshell;

import net.thevpc.nuts.NutsPath;
import net.thevpc.nuts.NutsSession;

import java.io.File;
import java.nio.file.Paths;

/**
 *
 * @author thevpc
 */
public class DefaultJShellFileSystem implements JShellFileSystem {

    @Override
    public String getInitialWorkingDir(NutsSession session) {
        return System.getProperty("user.dir");
    }

    @Override
    public String getHomeWorkingDir(NutsSession session) {
        return System.getProperty("user.home");
    }

    @Override
    public String getAbsolutePath(String path, NutsSession session) {
        return NutsPath.of(path,session).normalize().toString();
    }

    @Override
    public boolean isAbsolute(String path, NutsSession session) {
        return NutsPath.of(path,session).isAbsolute();
    }

    @Override
    public boolean isDirectory(String path, NutsSession session) {
        return NutsPath.of(path,session).isDirectory();
    }

    @Override
    public boolean exists(String path, NutsSession session) {
        return NutsPath.of(path,session).exists();
    }
}
