/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.bundles.jshell;

import java.io.File;
import java.nio.file.Paths;

/**
 *
 * @author thevpc
 */
public class DefaultJShellFileSystem implements JShellFileSystem {

    @Override
    public String getInitialWorkingDir() {
        return System.getProperty("user.dir");
    }

    @Override
    public String getHomeWorkingDir() {
        return System.getProperty("user.home");
    }

    @Override
    public String getAbsolutePath(String path) {
        return Paths.get(path).normalize().toString();
    }

    @Override
    public boolean isAbsolute(String path) {
        return new File(path).isAbsolute();
    }

    @Override
    public boolean isDirectory(String path) {
        return new File(path).isDirectory();
    }

    @Override
    public boolean exists(String path) {
        return new File(path).exists();
    }
}
