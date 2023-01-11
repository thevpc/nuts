/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ndb.sql.postgres;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
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
public class NPostgreSQLMain extends SqlSupport<NPostgresConfig> {

    public NPostgreSQLMain(NApplicationContext appContext) {
        super("postgresql", NPostgresConfig.class, appContext, "org.postgresql:postgresql#42.5.1", "org.postgresql.Driver");
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
        if (NBlankable.isBlank(options.getRemoteUser())) {
            options.setRemoteUser(System.getProperty("user.name"));
        }
    }

    public void preparePgpass(NPostgresConfig options, NSession session) {

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
                        "database", NOptional.of(options.getDatabaseName()).ifBlank("db").get()
                )).toString();
    }
}
