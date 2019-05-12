package net.vpc.toolbox.tomcat;

import net.vpc.app.nuts.NutsApplication;
import net.vpc.toolbox.tomcat.remote.RemoteTomcat;
import net.vpc.toolbox.tomcat.local.LocalTomcat;

import java.util.ArrayList;
import java.util.List;
import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsArgument;

public class TomcatMain extends NutsApplication {

    public static void main(String[] args) {
        new TomcatMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        List<String> argsList = new ArrayList<>();
        NutsCommandLine cmd = appContext.newCommandLine();
        Boolean local = null;
        while (cmd.hasNext()) {
            NutsArgument a = cmd.read();
            if (local == null) {
                if (appContext.configure(cmd)) {
                    //
                } else if ((a.getString().equals("--remote") || a.getString().equals("-r"))) {
                    local = false;
                } else if ((a.getString().equals("--local") || a.getString().equals("-l"))) {
                    local = true;
                } else if (a.isOption()) {
                    argsList.add(a.getString());
                } else {
                    argsList.add(a.getString());
                    local = true;
                }
            } else {
                argsList.add(a.getString());
            }
        }
        if (local == null) {
            local = true;
        }

        appContext.setArgs(argsList.toArray(new String[0]));
        if (local) {
            LocalTomcat m = new LocalTomcat(appContext);
            m.runArgs();
        } else {
            RemoteTomcat m = new RemoteTomcat(appContext);
            m.runArgs();
        }
    }

}
