package net.thevpc.nuts.toolbox.ndb;

import net.thevpc.nuts.NApplication;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoSupport;
import net.thevpc.nuts.toolbox.ndb.sql.derby.NDerbyMain;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.NMysqlMain;
import net.thevpc.nuts.toolbox.ndb.sql.postgres.NPostgresSupport;

public class NdbMain implements NApplication {

    public static void main(String[] args) {
        new NdbMain().runAndExit(args);
    }

    @Override
    public void run(NSession session) {
        run(session.getAppCommandLine(), session);
    }

    public void run(NCmdLine commandLine, NSession session) {
        while (commandLine.hasNext()) {
            if (commandLine.next("mysql", "mariadb").isPresent()) {
                new NMysqlMain(session).run(session, commandLine);
                return;
            } else if (commandLine.next("derby").isPresent()) {
                new NDerbyMain(session).run(session, commandLine);
                return;
            } else if (commandLine.next("mongo", "mongodb").isPresent()) {
                new NMongoSupport(session).run(session, commandLine);
                return;
            } else if (commandLine.next("postgres", "postgresql").isPresent()) {
                new NPostgresSupport(session).run(session, commandLine);
                return;
            } else {
                session.configureLast(commandLine);
            }
        }
        session.printAppHelp();
    }
}
