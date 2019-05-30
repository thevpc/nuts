/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format;

import java.io.PrintStream;
import java.io.PrintWriter;

import net.vpc.app.nuts.NutsCommand;
import net.vpc.app.nuts.NutsObjectFormat;
import net.vpc.app.nuts.NutsWorkspace;

/**
 *
 * @author vpc
 */
public abstract class NutsObjectFormatBase extends DefaultFormatBase<NutsObjectFormat> implements NutsObjectFormat {

    public NutsObjectFormatBase(NutsWorkspace ws, String name) {
        super(ws,name);
    }

    @Override
    public boolean configureFirst(NutsCommand commandLine) {
        return false;
    }

    @Override
    public void print(PrintStream w) {
        PrintWriter pw = new PrintWriter(w);
        print(pw);
    }
}
