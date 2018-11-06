package net.vpc.toolbox.tomcat;

import net.vpc.app.nuts.Nuts;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.toolbox.tomcat.client.TomcatClient;
import net.vpc.toolbox.tomcat.server.TomcatServer;

import java.util.Arrays;
import java.util.List;

public class TomcatMain {
    public static void main(String[] args) {
        NutsWorkspace ws = Nuts.openWorkspace(args);
        List<String> argsList= Arrays.asList(Nuts.skipNutsArgs(args));
        if(argsList.size()==0){
            throw new IllegalArgumentException("Expected --client or --server");
        }
        if (argsList.get(0).equals("--client")) {
            argsList.remove(0);
            TomcatClient m = new TomcatClient(ws);
            m.runArgs(args);
            return;
        }
        if (argsList.get(0).equals("--server")) {
            argsList.remove(0);
        }
        TomcatServer m = new TomcatServer(ws);
        m.runArgs(args);
    }
}
