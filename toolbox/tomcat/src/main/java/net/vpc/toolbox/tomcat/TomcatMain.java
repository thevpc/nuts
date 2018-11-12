package net.vpc.toolbox.tomcat;

import net.vpc.app.nuts.NutsApplication;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.toolbox.tomcat.client.TomcatClient;
import net.vpc.toolbox.tomcat.server.TomcatServer;

import java.util.Arrays;

public class TomcatMain extends NutsApplication {
    public static void main(String[] args) {
        new TomcatMain().launchAndExit(args);
    }

    @Override
    public int launch(String[] args, NutsWorkspace ws) {
        if (args.length == 0) {
            throw new IllegalArgumentException("Expected --client or --server");
        }
        if (args[0].equals("--client") || args[0].equals("-c")) {
            TomcatClient m = new TomcatClient(ws);
            return m.runArgs(Arrays.copyOfRange(args, 1, args.length));
        }
        if (args[0].equals("--server") || args[0].equals("-s")) {
            TomcatServer m = new TomcatServer(ws);
            return m.runArgs(Arrays.copyOfRange(args, 1, args.length));
        } else {
            TomcatServer m = new TomcatServer(ws);
            return m.runArgs(args);
        }
    }
}
