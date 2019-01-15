package net.vpc.toolbox.tomcat;

import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.app.NutsApplication;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;
import net.vpc.toolbox.tomcat.remote.RemoteTomcat;
import net.vpc.toolbox.tomcat.local.LocalTomcat;

import java.util.ArrayList;
import java.util.List;

public class TomcatMain extends NutsApplication {
    public static void main(String[] args) {
        new TomcatMain().launchAndExit(args);
    }

    @Override
    public int launch(NutsApplicationContext appContext) {
        String[] args = appContext.getArgs();
        if (args.length == 0) {
            throw new NutsExecutionException("Expected --local or --remote",2);
        }
        List<String> argsList = new ArrayList<>();
        CommandLine cmd = new CommandLine(args);
        Boolean local = null;
        while (cmd.hasNext()) {
            Argument a = cmd.read();
            if (local == null) {
                if(appContext.configure(cmd)){
                    //
                }else if (a.getExpression().equals("--remote") || a.getExpression().equals("-r")) {
                    local = false;
                } else if (a.getExpression().equals("--local") || a.getExpression().equals("-l")) {
                    local = true;
                } else if (a.isOption()) {
                    argsList.add(a.getExpression());
                } else {
                    argsList.add(a.getExpression());
                    local = true;
                }
            } else {
                argsList.add(a.getExpression());
            }
        }
        if (local == null) {
            local = true;
        }

        appContext.setArgs(argsList.toArray(new String[0]));
        if (local) {
            LocalTomcat m = new LocalTomcat(appContext);
            return m.runArgs();
        } else {
            RemoteTomcat m = new RemoteTomcat(appContext);
            return m.runArgs();
        }
    }


}
