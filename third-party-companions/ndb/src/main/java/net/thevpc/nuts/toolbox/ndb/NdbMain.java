package net.thevpc.nuts.toolbox.ndb;

import net.thevpc.nuts.NutsApplication;
import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.toolbox.ndb.derby.NDerbyMain;
import net.thevpc.nuts.toolbox.ndb.nmysql.NMysqlMain;
import net.thevpc.nuts.toolbox.ndb.postgres.NPostgreSQLMain;

public class NdbMain implements NutsApplication {

    public static void main(String[] args) {
        new NdbMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext context) {
        NutsCommandLine commandLine = context.getCommandLine();
        if (commandLine.hasNext()) {
            if (commandLine.next("mysql").isPresent()) {
                new NMysqlMain().run(context, commandLine);
                return;
            } else if (commandLine.next("derby").isPresent()) {
                new NDerbyMain().run(context, commandLine);
                return;
            } else if (commandLine.next("postgres").isPresent()) {
                new NPostgreSQLMain().run(context, commandLine);
                return;
            } else {
                context.configureLast(commandLine);
            }
        }
        context.printHelp();
    }
}
