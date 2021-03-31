package net.thevpc.nuts.toolbox.ndb;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.ndb.derby.NDerbyMain;
import net.thevpc.nuts.toolbox.ndb.nmysql.NMysqlMain;

public class NdbMain extends NutsApplication {

    public static void main(String[] args) {
        new NdbMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext context) {
        NutsCommandLine commandLine = context.getCommandLine();
        if (commandLine.hasNext()) {
            if (commandLine.next("mysql") != null) {
                new NMysqlMain().run(context, commandLine);
                return;
            } else if (commandLine.next("derby") != null) {
                new NDerbyMain().run(context, commandLine);
                return;
            } else {
                context.configureLast(commandLine);
            }
        }
        context.printHelp();
    }
}
