/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ndb.sql.postgres;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.toolbox.ndb.base.CmdRedirect;
import net.thevpc.nuts.toolbox.ndb.sql.postgres.cmd.PostgresDumpCmd;
import net.thevpc.nuts.toolbox.ndb.sql.postgres.cmd.PostgresRestoreCmd;
import net.thevpc.nuts.toolbox.ndb.sql.postgres.cmd.PostgresShowDatabasesCmd;
import net.thevpc.nuts.toolbox.ndb.sql.postgres.cmd.PostgresShowTablesCmd;
import net.thevpc.nuts.toolbox.ndb.sql.sqlbase.SqlSupport;
import net.thevpc.nuts.util.*;

import java.util.*;

/**
 * @author thevpc
 */
public class NPostgresSupport extends SqlSupport<NPostgresConfig> {

    public NPostgresSupport(NSession session) {
        super("postgresql", NPostgresConfig.class, session, "org.postgresql:postgresql#42.5.1", "org.postgresql.Driver");
        declareNdbCmd(new PostgresShowTablesCmd(this));
        declareNdbCmd(new PostgresShowDatabasesCmd(this));
        declareNdbCmd(new PostgresDumpCmd(this));
        declareNdbCmd(new PostgresRestoreCmd(this));
    }

    public void revalidateOptions(NPostgresConfig options) {
        int port = NOptional.of(options.getPort()).mapIf(x -> x <= 0, x -> null, x -> x).ifBlank(5432).get();
        String host = NOptional.of(options.getHost()).ifBlank("localhost").get();
        String user = options.getUser();
        String password = options.getPassword();
        if (NBlankable.isBlank(user) && NBlankable.isBlank(password)) {
            user = "postgres";
            password = "postgres";
        } else if ("postgres".equals(user) && NBlankable.isBlank(password)) {
            password = "postgres";
        }
        options.setPassword(password);
        options.setUser(user);
        options.setHost(host);
        options.setPort(port);
        if (isRemoteHost(options.getRemoteServer())) {
            if (NBlankable.isBlank(options.getRemoteUser())) {
                options.setRemoteUser(System.getProperty("user.name"));
            }
        }
    }

    public void prepareDump(NPostgresConfig options, NSession session) {

        if (isRemoteCommand(options)) {

        } else {
            NPath pgpass = NPath.ofUserHome(session).resolve(".pgpass");
            String u = options.getHost() + ":" + options.getPort() + ":" + options.getDatabaseName() + ":" + options.getUser();
            if (pgpass.exists()) {
                String q = pgpass.getLines().filter(x -> x.startsWith(u + ":")).findFirst().orElse(null);
                if (q != null) {
                    String storedPassword = q.substring(u.length() + 1);
                    if (!NBlankable.isBlank(options.getPassword()) && !Objects.equals(options.getPassword(), storedPassword)) {
                        throw new NIllegalArgumentException(session, NMsg.ofPlain("stored password does not match"));
                    }
                } else {
                    if (NBlankable.isBlank(options.getPassword())) {
                        throw new NIllegalArgumentException(session, NMsg.ofPlain("missing password"));
                    }
                    pgpass.writeString(u + ":" + options.getPassword() + "\n", NPathOption.APPEND);
                    run(sysCmd(session)
                            .addCommand("chmod", "0600", pgpass.toString())
                    );
                }
            } else {
                pgpass.writeString(u + ":" + options.getPassword() + "\n", NPathOption.APPEND);
            }
        }
    }

    public String createJdbcURL(NPostgresConfig options) {
        return NMsg.ofV("jdbc:postgresql://${server}:${port}/${database}",
                NMaps.of(
                        "server", NOptional.of(options.getHost()).ifBlank("localhost").get(),
                        "port", NOptional.of(options.getPort()).mapIf(x -> x <= 0, x -> null, x -> x).ifBlank(5432).get(),
                        "database", NOptional.of(options.getDatabaseName()).ifBlank("postgres").get()
                )).toString();
    }


    public CmdRedirect createDumpCommand(NPath remoteSql, NPostgresConfig options, NSession session) {
        return new CmdRedirect(
                NCmdLine.of(
                        new String[]{
                                "pg_dump",
                                options.getDatabaseName(),
                                "--clean",
                                "--create",
                                "--host=" + options.getHost(),
                                "--port=" + options.getPort(),
                                "--username=" + options.getUser()
                        }
                )
                , remoteSql
        );
    }


    public CmdRedirect createRestoreCommand(NPath remoteSql, NPostgresConfig options, NSession session) {
//        return new CmdRedirect(
//                NCmdLine.of(
//                        new String[]{
//                                "pg_restore",
//                                options.getDatabaseName(),
//                                "--host=" + options.getHost(),
//                                "--port=" + options.getPort(),
//                                "--username=" + options.getUser()
//                        }
//                )
//                , remoteSql
//        );
        return new CmdRedirect(
                NCmdLine.of(
                        new String[]{
                                "psql",
                                "-d",options.getDatabaseName(),
                                "--host=" + options.getHost(),
                                "--port=" + options.getPort(),
                                "--username=" + options.getUser()
                        }
                )
                , remoteSql
        );
    }

}
