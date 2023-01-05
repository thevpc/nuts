package net.thevpc.nuts.toolbox.ndb.sql.postgres.cmd;

import net.thevpc.nuts.NApplicationContext;
import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.AtName;
import net.thevpc.nuts.toolbox.ndb.sql.postgres.NPostgreSQLMain;
import net.thevpc.nuts.toolbox.ndb.sql.postgres.NPostgresConfig;
import net.thevpc.nuts.toolbox.ndb.sql.sqlbase.cmd.SqlDumpCmd;
import net.thevpc.nuts.util.NRef;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PostgresRestoreCmd extends SqlDumpCmd<NPostgresConfig> {
    public PostgresRestoreCmd(NPostgreSQLMain support) {
        super(support);
    }

    @Override
    public NPostgreSQLMain getSupport() {
        return (NPostgreSQLMain) super.getSupport();
    }

    public void run(NApplicationContext appContext, NCommandLine commandLine) {
        NSession session=appContext.getSession();
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
        NPath sqlFile;
        revalidateOptions(options);
        getSupport().preparePgpass(options, session);
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

}
