/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ndb.postgres;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.toolbox.ndb.RdbSupport;
import net.thevpc.nuts.toolbox.ndb.nmysql.util.AtName;
import net.thevpc.nuts.toolbox.ndb.util.NdbUtils;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author thevpc
 */
public class NPostgreSQLMain extends RdbSupport<NPostgresConfig> {

    public NPostgreSQLMain(NApplicationContext appContext) {
        super("postgresql", NPostgresConfig.class, appContext, "org.postgresql:postgresql#42.5.1", "org.postgresql.Driver");
    }
    protected void dump(NCommandLine commandLine, NSession session) {
        commandLine.setCommandName(dbType + " dump");
        NRef<AtName> name = NRef.ofNull(AtName.class);
        NRef<NPath> file = NRef.ofNull(NPath.class);
        NPostgresConfig otherOptions = createConfigInstance();
        while (commandLine.hasNext()) {
            if (commandLine.isNextOption()) {
                switch (commandLine.peek().get(session).key()) {
                    case "--name": {
                        commandLine.withNextString((v, a, s) -> {
                            if (name.isNull()) {
                                String name2 = NdbUtils.checkName(a.getStringValue().get(session), session);
                                name.set(new AtName(name2));
                            } else {
                                commandLine.throwUnexpectedArgument(NMsg.ofPlain("already defined"));
                            }
                        });
                        break;
                    }
                    case "--file": {
                        commandLine.withNextString((v, a, s) -> {
                            file.set(NPath.of(v, s));
                        });
                        break;
                    }
                    default: {
                        if (fillOption(commandLine, otherOptions)) {

                        } else if (appContext.configureFirst(commandLine)) {

                        } else {
                            session.configureLast(commandLine);
                        }
                    }
                }
            } else {
                commandLine.throwUnexpectedArgument();
            }
        }

        NPostgresConfig options = null;
        if (name.isNull()) {
            name.set(new AtName(""));
            options = loadConfig(name.get()).orNull();
            if (options == null) {
                options = createConfigInstance();
            }
            options.setNonNull(otherOptions);
        } else {
            options = loadConfig(name.get()).get();
            options.setNonNull(otherOptions);
        }
        revalidateOptions(options);
        preparePgpass(options, session);
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
            NPath remoteTempFolder = getRemoteTempFolder(options, session);
            NPath remoteSQL = remoteTempFolder.resolve(simpleName0 + ".sql");
            NPath remoteZip = remoteTempFolder.resolve(simpleName0 + ".zip");
            run(sysSsh(options, session)
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


    protected void restore(NCommandLine commandLine, NSession session) {
        commandLine.setCommandName(dbType + " restore");
        NRef<AtName> name = NRef.ofNull(AtName.class);
        NRef<NPath> file = NRef.ofNull(NPath.class);
        NPostgresConfig otherOptions = createConfigInstance();
        while (commandLine.hasNext()) {
            if (commandLine.isNextOption()) {
                switch (commandLine.peek().get(session).key()) {
                    case "--name": {
                        commandLine.withNextString((v, a, s) -> {
                            if (name.isNull()) {
                                String name2 = NdbUtils.checkName(a.getStringValue().get(session), session);
                                name.set(new AtName(name2));
                            } else {
                                commandLine.throwUnexpectedArgument(NMsg.ofPlain("already defined"));
                            }
                        });
                        break;
                    }
                    case "--file": {
                        commandLine.withNextString((v, a, s) -> {
                            file.set(NPath.of(v, s));
                        });
                        break;
                    }
                    default: {
                        if (fillOption(commandLine, otherOptions)) {

                        } else if (appContext.configureFirst(commandLine)) {

                        } else {
                            session.configureLast(commandLine);
                        }
                    }
                }
            } else {
                commandLine.throwUnexpectedArgument();
            }
        }

        NPostgresConfig options = null;
        if (name.isNull()) {
            name.set(new AtName(""));
            options = loadConfig(name.get()).orNull();
            if (options == null) {
                options = createConfigInstance();
            }
            options.setNonNull(otherOptions);
        } else {
            options = loadConfig(name.get()).get();
            options.setNonNull(otherOptions);
        }
        NPath sqlFile;
        revalidateOptions(options);
        preparePgpass(options, session);
        if (file.get() == null) {
            throw new NIllegalArgumentException(session, NMsg.ofPlain("missing file"));
        } else {
            if (file.get().isDirectory()) {

            }
            if (file.get().getName().toLowerCase().endsWith(".sql")) {
                sqlFile = file.get();
                run(sysCmd(session)
                        .addCommand("pg_restore")
                        .addCommand(options.getDatabaseName())
                        .addCommand("--host=" + options.getHost())
                        .addCommand("--port=" + options.getPort())
                        .addCommand("--username=" + options.getUser())
                        .setRedirectInputFile(sqlFile)
                );
            } else if (file.get().getName().toLowerCase().endsWith(".zip")) {
                try (ZipInputStream zis = new ZipInputStream(file.get().getInputStream())) {
                    //get the zipped file list entry
                    ZipEntry ze = zis.getNextEntry();
                    while (ze != null) {
                        String fileName = ze.getName();
                        if (fileName.endsWith("/")) {
                            file.get().resolveSibling(fileName).mkdirs();
                        } else {
                            if (fileName.endsWith(".sql")) {
                                NPath newFile = file.get().resolve(fileName);
                                newFile.getParent().mkdirs();
                                byte[] buffer = new byte[8196];
                                try (OutputStream fos = newFile.getOutputStream()) {
                                    int len;
                                    while ((len = zis.read(buffer)) > 0) {
                                        fos.write(buffer, 0, len);
                                    }
                                }
                                run(sysCmd(session)
                                        .addCommand("pg_restore")
                                        .addCommand(options.getDatabaseName())
                                        .addCommand("--host=" + options.getHost())
                                        .addCommand("--port=" + options.getPort())
                                        .addCommand("--username=" + options.getUser())
                                        .setRedirectInputFile(newFile)
                                );
                                newFile.delete();
                            }
                        }
                        ze = zis.getNextEntry();
                    }
                    zis.closeEntry();
                } catch (IOException ex) {
                    throw new NIOException(session, ex);
                }
            } else {
                throw new NIllegalArgumentException(session, NMsg.ofPlain("missing file"));
            }
        }
    }

    protected void revalidateOptions(NPostgresConfig options) {
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

    private void preparePgpass(NPostgresConfig options, NSession session) {

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

    protected void showTables(NCommandLine commandLine, NSession session) {
        commandLine.setCommandName(dbType + " show-tables");
        NRef<AtName> name = NRef.ofNull(AtName.class);
        NPostgresConfig otherOptions = createConfigInstance();
        while (commandLine.hasNext()) {
            if (commandLine.isNextOption()) {
                switch (commandLine.peek().get(session).key()) {
                    case "--name": {
                        commandLine.withNextString((v, a, s) -> {
                            if (name.isNull()) {
                                String name2 = NdbUtils.checkName(a.getStringValue().get(session), session);
                                name.set(new AtName(name2));
                            } else {
                                commandLine.throwUnexpectedArgument(NMsg.ofPlain("already defined"));
                            }
                        });
                        break;
                    }
                    default: {
                        if (fillOption(commandLine, otherOptions)) {

                        } else if (appContext.configureFirst(commandLine)) {

                        } else {
                            session.configureLast(commandLine);
                        }
                    }
                }
            } else {
                commandLine.throwUnexpectedArgument();
            }
        }

        NPostgresConfig options = null;
        if (name.isNull()) {
            name.set(new AtName(""));
            options = loadConfig(name.get()).orNull();
            if (options == null) {
                options = createConfigInstance();
            }
            options.setNonNull(otherOptions);
        } else {
            options = loadConfig(name.get()).get();
            options.setNonNull(otherOptions);
        }
        runSQL(Arrays.asList("SELECT schemaname,tablename FROM pg_catalog.pg_tables WHERE schemaname != 'pg_catalog' AND schemaname != 'information_schema'"), options, true, session);
    }

    protected String createJdbcURL(NPostgresConfig options) {
        return NMsg.ofVstyle("jdbc:postgresql://${server}:${port}/${database}",
                NMaps.of(
                        "server", NOptional.of(options.getHost()).ifBlank("localhost").get(),
                        "port", NOptional.of(options.getPort()).mapIf(x -> x <= 0, x -> null, x -> x).ifBlank(5432).get(),
                        "database", NOptional.of(options.getDatabaseName()).ifBlank("db").get()
                )).toString();
    }
}
