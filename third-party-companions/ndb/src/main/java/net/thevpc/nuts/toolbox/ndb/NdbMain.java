package net.thevpc.nuts.toolbox.ndb;

import net.thevpc.nuts.NApplication;
import net.thevpc.nuts.NApplicationContext;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.toolbox.ndb.derby.NDerbyMain;
import net.thevpc.nuts.toolbox.ndb.nmysql.NMysqlMain;
import net.thevpc.nuts.toolbox.ndb.postgres.NPostgreSQLMain;

public class NdbMain implements NApplication {

    public static void main(String[] args) {
        new NdbMain().runAndExit(args);
    }

    @Override
    public void run(NApplicationContext context) {
        NCommandLine commandLine = context.getCommandLine();
        if (commandLine.hasNext()) {
            if (commandLine.next("mysql").isPresent()) {
                new NMysqlMain(context).run(context, commandLine);
                return;
            } else if (commandLine.next("derby").isPresent()) {
                new NDerbyMain(context).run(context, commandLine);
                return;
            } else if (commandLine.next("postgres").isPresent()) {
                new NPostgreSQLMain(context).run(context, commandLine);
                return;
            } else {
                context.configureLast(commandLine);
            }
        }
        context.printHelp();
    }
}
