package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.app.nuts.Nuts;
import net.vpc.app.nuts.NutsWorkspace;

import java.util.Arrays;

public class Nsh {
    public static void main(String[] args) {
        NutsWorkspace ws = Nuts.openWorkspace(args);
        args=ws.getBootOptions().getApplicationArguments();
        DefaultNutsConsole c=new DefaultNutsConsole(ws);
        c.run(args);
    }
}
