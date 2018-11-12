package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.app.nuts.NutsApplication;
import net.vpc.app.nuts.NutsWorkspace;

public class Nsh extends NutsApplication{
    public static void main(String[] args) {
        new Nsh().launchAndExit(args);
    }

    @Override
    public int launch(String[] args, NutsWorkspace ws) {
        DefaultNutsConsole c=new DefaultNutsConsole(ws);
        return c.run(args);
    }
}
