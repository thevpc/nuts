package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.app.nuts.app.NutsApplication;
import net.vpc.app.nuts.app.NutsApplicationContext;

import java.util.Arrays;

public class Nsh extends NutsApplication {
    public static void main(String[] args) {
        new Nsh().launchAndExit(args);
    }

    @Override
    public int launch(NutsApplicationContext appContext) {
        String[] args=appContext.getArgs();
        NutsJavaShell c=new NutsJavaShell(appContext);
        return c.run(args);
    }
}
