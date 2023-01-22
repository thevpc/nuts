package net.thevpc.nuts.toolbox.ndb.sql.sqlbase.cmd;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.CmdRedirect;
import net.thevpc.nuts.toolbox.ndb.base.cmd.RestoreCmd;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.AtName;
import net.thevpc.nuts.toolbox.ndb.sql.sqlbase.SqlSupport;
import net.thevpc.nuts.util.NRef;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SqlRestoreCmd<C extends NdbConfig> extends RestoreCmd<C> {
    public SqlRestoreCmd(SqlSupport<C> support, String... names) {
        super(support, names);
    }

    @Override
    public SqlSupport<C> getSupport() {
        return (SqlSupport<C>) super.getSupport();
    }

    public void run(NApplicationContext appContext, NCommandLine commandLine) {
        NSession session = appContext.getSession();
        NRef<AtName> name = NRef.ofNull(AtName.class);
        NRef<NPath> file = NRef.ofNull(NPath.class);
        C otherOptions = createConfigInstance();
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
        String dumpExt = getSupport().getDumpExt(otherOptions, session);

        C options = loadFromName(name, otherOptions);
        NPath sqlFile;
        revalidateOptions(options);
        getSupport().prepareDump(options, session);
        if (file.get() == null) {
            throw new NIllegalArgumentException(session, NMsg.ofPlain("missing file"));
        } else {
            if (file.get().isDirectory()) {

            }
            if (file.get().getName().toLowerCase().endsWith(".sql")) {
                sqlFile = file.get();
                CmdRedirect restoreCommand = getSupport().createRestoreCommand(sqlFile, options, session);
                NExecCommand nExecCommand = sysCmd(session).addCommand(restoreCommand.getCmd().toStringArray());
                if (restoreCommand.getPath() != null) {
                    nExecCommand.setRedirectInputFile(restoreCommand.getPath());
                }
                run(nExecCommand);
            } else if (file.get().getName().toLowerCase().endsWith(".zip")) {
                try (ZipInputStream zis = new ZipInputStream(file.get().getInputStream())) {
                    //get the zipped file list entry
                    ZipEntry ze = zis.getNextEntry();
                    while (ze != null) {
                        String fileName = ze.getName();
                        if (fileName.endsWith("/")) {
                            file.get().resolveSibling(fileName).mkdirs();
                        } else {
                            if (fileName.endsWith(dumpExt)) {
                                NPath newFile = file.get().resolve(fileName);
                                newFile.getParent().mkdirs();
                                CmdRedirect restoreCommand = getSupport().createRestoreCommand(newFile, options, session);
                                NExecCommand nExecCommand = sysCmd(session).addCommand(restoreCommand.getCmd().toStringArray());
                                if (restoreCommand.getPath() != null) {
                                    nExecCommand.setRedirectInputFile(restoreCommand.getPath());
                                }
                                run(nExecCommand);
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
