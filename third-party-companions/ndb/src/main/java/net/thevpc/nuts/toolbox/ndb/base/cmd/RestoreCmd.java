package net.thevpc.nuts.toolbox.ndb.base.cmd;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NExecInput;
import net.thevpc.nuts.io.NPathExtensionType;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.CmdRedirect;
import net.thevpc.nuts.toolbox.ndb.base.NdbCmd;
import net.thevpc.nuts.toolbox.ndb.base.NdbSupportBase;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.AtName;
import net.thevpc.nuts.util.NMsg;
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
    public void run(NCmdLine cmdLine) {
        NRef<AtName> name = NRef.ofNull(AtName.class);
        NRef<NPath> file = NRef.ofNull(NPath.class);
        C otherOptions = createConfigInstance();
        while (cmdLine.hasNext()) {
            if (cmdLine.isNextOption()) {
                switch (cmdLine.peek().get().key()) {
                    case "--name": {
                        readConfigNameOption(cmdLine, name);
                        break;
                    }
                    case "--file": {
                        cmdLine.withNextEntry((v, a) -> {
                            file.set(NPath.of(v));
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
        String dumpExt = getSupport().getDumpExt(otherOptions);

        C options = loadFromName(name, otherOptions);
        NPath sqlFile;
        revalidateOptions(options);
        getSupport().prepareDump(options);
        NdbSupportBase.DumpRestoreMode dumpRestoreMode = getSupport().getDumpRestoreMode(options);
        if (file.get() == null) {
            throw new NIllegalArgumentException(NMsg.ofPlain("missing file"));
        } else {
            if (isRemoteCommand(options)) {
                NPath remoteTempFolder = getSupport().getRemoteTempFolder(options);
                NPath upFile = remoteTempFolder.resolveSibling(file.get());
                run(sysCmd()
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
                                            sshRestore(upFile.resolve(fileName), options);
                                        }
                                    }
                                    ze = zis.getNextEntry();
                                }
                                zis.closeEntry();
                            } catch (IOException ex) {
                                throw new NIOException(ex);
                            }
                            sshRm(upFile,options);
                        } else {
                            sshRestore(upFile, options);
                            sshRm(upFile,options);
                        }
                        break;
                    }
                    case FOLDER: {
                        if (/*file.get().isFile() && */file.get().getName().toLowerCase().endsWith(".zip")) {
                            NPath unzippedFolder = file.get().resolveSibling(file.get().getNameParts(NPathExtensionType.SHORT).getBaseName());
                            NExecCmd zipExec = sysSsh(options)
                                    .addCommand("unzip")
                                    .addCommand("-q")
                                    .addCommand("-o")
                                    .addCommand(upFile.toString())
                                    .addCommand("-d")
                                    .addCommand(unzippedFolder.toString());
                            run(zipExec);
                            sshRestore(unzippedFolder, options);
                            sshRm(upFile,options);
                            unzippedFolder.deleteTree();
                        } else {
                            sqlFile = file.get();
                            if (!sqlFile.isDirectory()) {
                                throw new NIllegalArgumentException(NMsg.ofC("expected folder %s", sqlFile));
                            }
                            sshRestore(upFile, options);
                            sshRm(upFile,options);
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
                                            restoreFile(newFile, options);
                                            newFile.delete();
                                        }
                                    }
                                    ze = zis.getNextEntry();
                                }
                                zis.closeEntry();
                            } catch (IOException ex) {
                                throw new NIOException(ex);
                            }
                        } else {
                            sqlFile = file.get();
                            restoreFile(sqlFile, options);
                        }
                        break;
                    }
                    case FOLDER: {
                        if (/*file.get().isFile() && */file.get().getName().toLowerCase().endsWith(".zip")) {
                            NPath zipPath = file.get();
                            NPath unzippedFolder = file.get().resolveSibling(file.get().getNameParts(NPathExtensionType.SHORT).getBaseName());
                            NSession session = NSession.get().get();
                            NExecCmd zipExec = sysCmd()
                                    .addCommand("unzip")
                                    .addCommand(session.isTrace()?null:"-q")
                                    .addCommand("-o")
                                    .addCommand(zipPath.toString())
                                    .addCommand("-d")
                                    .addCommand(unzippedFolder.toString());
                            run(zipExec);
                            restoreFile(unzippedFolder, options);
                            unzippedFolder.deleteTree();
                        } else {
                            sqlFile = file.get();
                            if (!sqlFile.isDirectory()) {
                                throw new NIllegalArgumentException(NMsg.ofC("expected folder %s", sqlFile));
                            }
                            restoreFile(sqlFile, options);
                        }
                    }
                }
            }
        }
    }


    private void sshRestore(NPath upRestorePath, C options) {
        CmdRedirect restoreCommand = getSupport().createRestoreCommand(upRestorePath, options);
        NExecCmd nExecCmd = sysSsh(options).addCommand(
                restoreCommand.getCmd().toString()
                        + (restoreCommand.getPath() == null ? "" : (" > " + restoreCommand.getPath()))
        );
        run(nExecCmd);
    }

    private void restoreFile(NPath sqlFile, C options) {
        if (!sqlFile.exists()) {
            throw new NIllegalArgumentException(NMsg.ofC("does not exist %s", sqlFile));
        }
        CmdRedirect restoreCommand = getSupport().createRestoreCommand(sqlFile, options);
        NExecCmd nExecCmd = sysCmd().addCommand(restoreCommand.getCmd().toStringArray());
        if (restoreCommand.getPath() != null) {
            nExecCmd.setIn(NExecInput.ofPath(restoreCommand.getPath()));
        }
        run(nExecCmd);
    }
}
