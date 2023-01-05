package net.thevpc.nuts.toolbox.ndb.sql.postgres.cmd;

import net.thevpc.nuts.NApplicationContext;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.AtName;
import net.thevpc.nuts.toolbox.ndb.sql.postgres.NPostgreSQLMain;
import net.thevpc.nuts.toolbox.ndb.sql.postgres.NPostgresConfig;
import net.thevpc.nuts.toolbox.ndb.sql.sqlbase.SqlSupport;
import net.thevpc.nuts.toolbox.ndb.sql.sqlbase.cmd.SqlDumpCmd;
import net.thevpc.nuts.toolbox.ndb.sql.sqlbase.cmd.SqlShowTablesCmd;
import net.thevpc.nuts.util.NRef;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PostgresDumpCmd extends SqlDumpCmd<NPostgresConfig> {
    public PostgresDumpCmd(NPostgreSQLMain support) {
        super(support);
    }

    @Override
    public NPostgreSQLMain getSupport() {
        return (NPostgreSQLMain) super.getSupport();
    }

    public void run(NApplicationContext appContext, NCommandLine commandLine) {
        NSession session = appContext.getSession();
        NRef<AtName> name = NRef.ofNull(AtName.class);
        NRef<NPath> file = NRef.ofNull(NPath.class);
        NPostgresConfig otherOptions = createConfigInstance();
        while (commandLine.hasNext()) {
            if (commandLine.isNextOption()) {
                switch (commandLine.peek().get(session).key()) {
                    case "--name": {
                        readConfigNameOption(commandLine, session, name);
                        break;
                    }
                    case "--file": {
                        commandLine.withNextString((v, a, s) -> {
                            file.set(NPath.of(v, s));
                        });
                        break;
                    }
                    default: {
                        fillOptionLast(commandLine, otherOptions);
                    }
                }
            } else {
                commandLine.throwUnexpectedArgument();
            }
        }

        NPostgresConfig options = loadFromName(name, otherOptions);
        revalidateOptions(options);
        getSupport().preparePgpass(options, session);
        String simpleName = null;
        NPath sqlPath;
        NPath zipPath;
        boolean sql = false;
        boolean zip = false;
        String simpleName0 = options.getDatabaseName() + "-" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(new Date());
        if (file.get() == null) {
            simpleName = simpleName0;
            sqlPath = NPath.of(simpleName + ".sql", session);
            zipPath = NPath.of(simpleName + ".zip", session);
            sql = false;
            zip = true;
        } else if (file.get().isDirectory()) {
            simpleName = simpleName0;
            sqlPath = file.get().resolve(simpleName + ".sql");
            zipPath = file.get().resolve(simpleName + ".zip");
            sql = false;
            zip = true;
        } else {
            simpleName = file.get().getBaseName();
            if (file.get().getName().toLowerCase().endsWith(".sql")) {
                sqlPath = file.get();
                zipPath = sqlPath.resolveSibling(simpleName + ".zip");
                sql = true;
                zip = false;
            } else if (file.get().getName().toLowerCase().endsWith(".zip")) {
                zipPath = file.get();
                sqlPath = zipPath.resolveSibling(simpleName + ".sql");
                sql = false;
                zip = true;
            } else {
                sqlPath = file.get().resolveSibling(file.get().getName() + ".sql");
                zipPath = file.get().resolveSibling(file.get().getName() + ".zip");
                sql = false;
                zip = true;
            }
        }
        if (isRemoteCommand(options)) {
            NPath remoteTempFolder = getSupport().getRemoteTempFolder(options, session);
            NPath remoteSQL = remoteTempFolder.resolve(simpleName0 + ".sql");
            NPath remoteZip = remoteTempFolder.resolve(simpleName0 + ".zip");
            run(getSupport().sysSsh(options, session)
                    .addCommand("pg_dump " + options.getDatabaseName() + " --clean --create --host=" + options.getHost() + " --port=" + options.getPort() + " --username=" + options.getUser() + " > " + remoteSQL)
            );
            if (zip) {
                run(sysSsh(options, session)
                        .addCommand("zip")
                        .addCommand(remoteZip.toString())
                        .addCommand(remoteSQL.toString())
                );
            }
            if (!sql) {
                run(sysSsh(options, session)
                        .addCommand("rm " + remoteSQL)
                );
            } else {
                run(sysCmd(session)
                        .addCommand("scp", options.getRemoteUser() + "@" + options.getRemoteServer() + ":" + remoteSQL)
                        .addCommand(sqlPath.toString())
                );
                run(sysSsh(options, session)
                        .addCommand("rm")
                        .addCommand(remoteSQL.toString())
                );
            }
            if (zip) {
                run(sysCmd(session)
                        .addCommand("scp", options.getRemoteUser() + "@" + options.getRemoteServer() + ":" + remoteZip)
                        .addCommand(zipPath.toString())
                );
                run(sysSsh(options, session)
                        .addCommand("rm")
                        .addCommand(remoteZip.toString())
                );
            }
        } else {
            run(sysCmd(session)
                    .addCommand("pg_dump")
                    .addCommand(options.getDatabaseName())
                    .addCommand("--clean")
                    .addCommand("--create")
                    .addCommand("--host=" + options.getHost())
                    .addCommand("--port=" + options.getPort())
                    .addCommand("--username=" + options.getUser())
                    .setRedirectOutputFile(sqlPath)
            );
            if (zip) {
                run(sysCmd(session)
                        .addCommand("zip")
                        .addCommand(zipPath.toString())
                        .addCommand(sqlPath.toString())
                );
            }
            if (!sql) {
                sqlPath.delete();
            }
        }
    }
}
