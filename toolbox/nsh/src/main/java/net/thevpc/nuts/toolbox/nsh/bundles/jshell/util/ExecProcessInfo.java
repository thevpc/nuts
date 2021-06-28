/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.bundles.jshell.util;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author thevpc
 */
public class ExecProcessInfo {
    public final String[] cmdarray;
    public final String[] envp; 
    public final File dir;
    public final InputStream in;
    public final OutputStream out;
    public final OutputStream err;

    public ExecProcessInfo(String[] cmdarray, String[] envp, File dir, InputStream in, OutputStream out, OutputStream err) {
        this.cmdarray = cmdarray;
        this.envp = envp;
        this.dir = dir;
        this.in = in;
        this.out = out;
        this.err = err;
    }
    
}
