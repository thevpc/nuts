package net.thevpc.nuts.toolbox.ndb.base.cmd;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.CmdRedirect;
import net.thevpc.nuts.toolbox.ndb.base.NdbCmd;
import net.thevpc.nuts.toolbox.ndb.base.NdbSupportBase;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.AtName;
import net.thevpc.nuts.util.NRef;

import java.io.IOException;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class RestoreCmd<C extends NdbConfig> extends NdbCmd<C> {
    public RestoreCmd(NdbSupportBase<C> support, String... names) {
        super(support, "restore");
        this.names.addAll(Arrays.asList(names));
    }


    @Override
    public void run(NSession session, NCmdLine cmdLine) {
        NRef<AtName> name = NRef.ofNull(AtName.class);
        NRef<NPath> file = NRef.ofNull(NPath.class);
        C otherOptions = createConfigInstance();
        while (cmdLine.hasNext()) {
            if (cmdLine.isNextOption()) {
                switch (cmdLine.peek().get(session).key()) {
                    case "--name": {
                        readConfigNameOption(cmdLine, session, name);
                        break;
                    }
                    case "--file": {
                        cmdLine.withNextEntry((v, a, s) -> {
                            file.set(NPath.of(v, s));
                        });
                        break;
                    }
                    default: {
                        fillOptionLast(cmdLine, otherOptions);
                    }
                }
            } else {
                cmdLine.throwUnexpectedArgument();
            }
        }
        String dumpExt = getSupport().getDumpExt(otherOptions, session);

        C options = loadFromName(name, otherOptions);
        NPath sqlFile;
        revalidateOptions(options);
        getSupport().prepareDump(options, session);
        NdbSupportBase.DumpRestoreMode dumpRestoreMode = getSupport().getDumpRestoreMode(options, session);
        if (file.get() == null) {
            throw new NIllegalArgumentException(session, NMsg.ofPlain("missing file"));
        } else {
            if (isRemoteCommand(options)) {
                NPath remoteTempFolder = getSupport().getRemoteTempFolder(options, session);
                NPath upFile = remoteTempFolder.resolveSibling(file.get());
                run(sysCmd(session)
                        .addCommand("scp")
                        .addCommand(file.get().isDirectory() ? "-r" : null)//when null ignored!
                        .addCommand(file.get().toString())
                        .addCommand(options.getRemoteUser() + "@" + options.getRemoteServer() + ":" + upFile.toString())
                );
                switch (dumpRestoreMode) {
                    case FILE: {
                        if (file.get().getName().toLowerCase().endsWith(".zip")) {
                            try (ZipInputStream zis = new ZipInputStream(file.get().getInputStream())) {
                                //get the zipped file list entry
                                ZipEntry ze = zis.getNextEntry();
                                while (ze != null) {
                                    String fileName = ze.getName();
                                    if (fileName.endsWith("/")) {
                                        //upFile.resolveSibling(fileName).mkdirs();
                                    } else {
                                        if (fileName.endsWith(dumpExt)) {
                                            sshRestore(upFile.resolve(fileName), options, session);
                                        }
                                    }
                                    ze = zis.getNextEntry();
                                }
                                zis.closeEntry();
                            } catch (IOException ex) {
                                throw new NIOException(session, ex);
                            }
                            sshRm(upFile,options, session);
                        } else {
                            sshRestore(upFile, options, session);
                            sshRm(upFile,options, session);
                        }
                        break;
                    }
                    case FOLDER: {
                        if (/*file.get().isFile() && */file.get().getName().toLowerCase().endsWith(".zip")) {
                            NPath unzippedFolder = file.get().resolveSibling(file.get().getLongBaseName());
                            NExecCommand zipExec = sysSsh(options, session)
                                    .addCommand("unzip")
                                    .addCommand("-q")
                                    .addCommand("-o")
                                    .addCommand(upFile.toString())
                                    .addCommand("-d")
                                    .addCommand(unzippedFolder.toString());
                            run(zipExec);
                            sshRestore(unzippedFolder, options, session);
                            sshRm(upFile,options, session);
                            unzippedFolder.deleteTree();
                        } else {
                            sqlFile = file.get();
                            if (!sqlFile.isDirectory()) {
                                throw new NIllegalArgumentException(session, NMsg.ofC("expected folder %s", sqlFile));
                            }
                            sshRestore(upFile, options, session);
                            sshRm(upFile,options, session);
                        }
                    }
                }

            } else {
                switch (dumpRestoreMode) {
                    case FILE: {
                        if (file.get().getName().toLowerCase().endsWith(".zip")) {
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
                                            restoreFile(newFile, options, session);
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
                            sqlFile = file.get();
                            restoreFile(sqlFile, options, session);
                        }
                        break;
                    }
                    case FOLDER: {
                        if (/*file.get().isFile() && */file.get().getName().toLowerCase().endsWith(".zip")) {
                            NPath zipPath = file.get();
                            NPath unzippedFolder = file.get().resolveSibling(file.get().getLongBaseName());
                            NExecCommand zipExec = sysCmd(session)
                                    .addCommand("unzip")
                                    .addCommand(session.isTrace()?null:"-q")
                                    .addCommand("-o")
                                    .addCommand(zipPath.toString())
                                    .addCommand("-d")
                                    .addCommand(unzippedFolder.toString());
                            run(zipExec);
                            restoreFile(unzippedFolder, options, session);
                            unzippedFolder.deleteTree();
                        } else {
                            sqlFile = file.get();
                            if (!sqlFile.isDirectory()) {
                                throw new NIllegalArgumentException(session, NMsg.ofC("expected folder %s", sqlFile));
                            }
                            restoreFile(sqlFile, options, session);
                        }
                    }
                }
            }
        }
    }


    private void sshRestore(NPath upRestorePath, C options, NSession session) {
        CmdRedirect restoreCommand = getSupport().createRestoreCommand(upRestorePath, options, session);
        NExecCommand nExecCommand = sysSsh(options, session).addCommand(
                restoreCommand.getCmd().toString()
                        + (restoreCommand.getPath() == null ? "" : (" > " + restoreCommand.getPath()))
        );
        run(nExecCommand);
    }

    private void restoreFile(NPath sqlFile, C options, NSession session) {
        if (!sqlFile.exists()) {
            throw new NIllegalArgumentException(session, NMsg.ofC("does not exist %s", sqlFile));
        }
        CmdRedirect restoreCommand = getSupport().createRestoreCommand(sqlFile, options, session);
        NExecCommand nExecCommand = sysCmd(session).addCommand(restoreCommand.getCmd().toStringArray());
        if (restoreCommand.getPath() != null) {
            nExecCommand.setRedirectInputFile(restoreCommand.getPath());
        }
        run(nExecCommand);
    }
}
