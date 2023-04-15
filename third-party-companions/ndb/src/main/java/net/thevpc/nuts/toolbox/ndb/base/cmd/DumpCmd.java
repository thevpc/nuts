package net.thevpc.nuts.toolbox.ndb.base.cmd;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.format.NObjectFormat;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.CmdRedirect;
import net.thevpc.nuts.toolbox.ndb.base.NdbCmd;
import net.thevpc.nuts.toolbox.ndb.base.NdbSupportBase;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.AtName;
import net.thevpc.nuts.util.NRef;
import net.thevpc.nuts.util.NStringUtils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class DumpCmd<C extends NdbConfig> extends NdbCmd<C> {
    public DumpCmd(NdbSupportBase<C> support, String... names) {
        super(support, "dump");
        this.names.addAll(Arrays.asList(names));
    }


//    @Override
//    public void run(NSession session, NCmdLine cmdLine) {
//        NRef<AtName> name = NRef.ofNull(AtName.class);
//        ExtendedQuery eq = new ExtendedQuery(getName());
//        C otherOptions = createConfigInstance();
//
//        String status = "";
//        while (cmdLine.hasNext()) {
//            switch (status) {
//                case "": {
//                    switch (cmdLine.peek().get(session).key()) {
//                        case "--config": {
//                            readConfigNameOption(cmdLine, session, name);
//                            break;
//                        }
//                        case "--entity":
//                        case "--table":
//                        case "--collection": {
//                            cmdLine.withNextString((v, a, s) -> eq.setTable(v));
//                            break;
//                        }
//                        case "--where": {
//                            status = "--where";
//                            cmdLine.withNextBoolean((v, a, s) -> {
//                            });
//                            break;
//                        }
//                        case "--set": {
//                            status = "--set";
//                            cmdLine.withNextBoolean((v, a, s) -> {
//                            });
//                            break;
//                        }
//                        default: {
//                            fillOptionLast(cmdLine, otherOptions);
//                        }
//                    }
//                    break;
//                }
//                case "--where": {
//                    switch (cmdLine.peek().get(session).key()) {
//                        case "--set": {
//                            status = "--set";
//                            cmdLine.withNextBoolean((v, a, s) -> {
//                            });
//                            break;
//                        }
//                        default: {
//                            eq.getWhere().add(cmdLine.next().get().toString());
//                        }
//                    }
//                    break;
//                }
//                case "--set": {
//                    switch (cmdLine.peek().get(session).key()) {
//                        case "--where": {
//                            status = "--where";
//                            cmdLine.withNextBoolean((v, a, s) -> {
//                            });
//                            break;
//                        }
//                        default: {
//                            eq.getSet().add(cmdLine.next().get().toString());
//                        }
//                    }
//                    break;
//                }
//            }
//        }
//        if (NBlankable.isBlank(eq.getTable())) {
//            cmdLine.throwMissingArgumentByName("--table");
//        }
//
//        C options = loadFromName(name, otherOptions);
//        support.revalidateOptions(options);
//        if (NBlankable.isBlank(otherOptions.getDatabaseName())) {
//            cmdLine.throwMissingArgumentByName("--dbname");
//        }
//        runDump(eq, options, session);
//    }
//
//    protected void runDump(ExtendedQuery eq, C options, NSession session) {
//        throw new NIllegalArgumentException(session, NMsg.ofPlain("invalid"));
//    }


    public void run(NSession session, NCmdLine cmdLine) {
        NRef<AtName> name = NRef.ofNull(AtName.class);
        NRef<NPath> file = NRef.ofNull(NPath.class);
        C otherOptions = createConfigInstance();
        NRef<Integer> roll = NRef.of(-1);
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
                    case "--roll": {
                        cmdLine.withNextEntryValue((v, a, s) -> {
                            roll.set(v.asInt().get());
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

        C options = loadFromName(name, otherOptions);
        revalidateOptions(options);
        getSupport().prepareDump(options, session);
        String simpleName = null;
        NPath plainFolderPath;
        NPath zipPath;
        boolean plainFolder = false;
        boolean zip = false;
        String dumpExt = NStringUtils.trim(getSupport().getDumpExt(options, session));
        if (file.get() == null) {
            if (roll.get() > 0) {
                zipPath=NPath.of(NObjectFormat.of(session)
                        .setFormatParam("count",roll.get())
                        .setValue(NPath.ofUserDirectory(session).resolve(options.getDatabaseName() + "#.zip"))
                        .setNtf(false)
                        .format().filteredText(),session);
                simpleName = zipPath.getBaseName();
                plainFolderPath = zipPath.resolve(simpleName + dumpExt);
            } else {
                simpleName = options.getDatabaseName() + "-" + new SimpleDateFormat("yyyyMMddHHmmssSSSSSS").format(new Date());
                plainFolderPath = NPath.of(simpleName + dumpExt, session);
                zipPath = NPath.of(simpleName + ".zip", session);
            }
            plainFolder = false;
            zip = true;

        } else if (file.get().isDirectory()) {
            if (roll.get() > 0) {
                zipPath=NPath.of(NObjectFormat.of(session)
                        .setFormatParam("count",roll.get())
                        .setValue(file.get().resolve(options.getDatabaseName() + "#.zip"))
                        .setNtf(false)
                        .format().filteredText(),session);

                simpleName = zipPath.getBaseName();
                plainFolderPath = zipPath.resolve(simpleName + dumpExt);

            } else {
                simpleName = options.getDatabaseName() + "-" + new SimpleDateFormat("yyyyMMddHHmmssSSSSSS").format(new Date());
                plainFolderPath = file.get().resolve(simpleName + dumpExt);
                zipPath = file.get().resolve(simpleName + ".zip");
            }
            plainFolder = false;
            zip = true;
        } else {
            NPath nFile = file.get();
            simpleName = nFile.getBaseName();
            if (nFile.getName().toLowerCase().endsWith(".zip")) {
                if (roll.get() > 0) {
                    zipPath=NPath.of(NObjectFormat.of(session)
                            .setFormatParam("count",roll.get())
                            .setValue(nFile)
                            .setNtf(false)
                            .format().filteredText(),session);
                    plainFolderPath = zipPath.resolveSibling(zipPath.getName() + dumpExt);
                } else {
                    zipPath = nFile;
                    plainFolderPath = zipPath.resolveSibling(simpleName + dumpExt);
                }
                plainFolder = false;
                zip = true;
            } else if (dumpExt.length() > 0 && nFile.getName().toLowerCase().endsWith(dumpExt)) {
                if (roll.get() > 0) {
                    plainFolderPath=NPath.of(NObjectFormat.of(session)
                            .setFormatParam("count",roll.get())
                            .setValue(nFile)
                            .setNtf(false)
                            .format().filteredText(),session);
                    zipPath = plainFolderPath.resolveSibling(plainFolderPath.getBaseName() + ".zip");
                } else {
                    plainFolderPath = nFile;
                    zipPath = plainFolderPath.resolveSibling(simpleName + ".zip");
                }
                plainFolder = true;
                zip = false;
            } else {
                if (roll.get() > 0) {
                    NPath roll1=NPath.of(NObjectFormat.of(session)
                            .setFormatParam("count",roll.get())
                            .setValue(nFile)
                            .setNtf(false)
                            .format().filteredText(),session);
                    plainFolderPath = roll1.resolveSibling(roll1.getName() + dumpExt);
                    zipPath = nFile.resolveSibling(roll1.getName() + ".zip");
                } else {
                    plainFolderPath = nFile.resolveSibling(nFile.getName() + dumpExt);
                    zipPath = nFile.resolveSibling(nFile.getName() + ".zip");
                }
                plainFolder = false;
                zip = true;
            }
        }
        if (isRemoteCommand(options)) {
            String simpleName0 = zipPath.getBaseName();
            NPath remoteTempFolder = getSupport().getRemoteTempFolder(options, session);
            NPath remotePlainFolder = remoteTempFolder.resolve(simpleName0 + dumpExt);
            NPath remoteZip = remoteTempFolder.resolve(simpleName0 + ".zip");
            CmdRedirect dumpCommand = getSupport().createDumpCommand(remotePlainFolder, options, session);
            run(getSupport().sysSsh(options, session)
                    .addCommand(dumpCommand.toString())
            );
            if (zip) {
                if (getSupport().isFolderArchive(options)) {
                    String sf = getSupport().getZipSubFolder(options);
                    if (NBlankable.isBlank(sf)) {
                        run(sysSsh(options, session)
                                .addCommand("cd " + remotePlainFolder.toString() + " ; zip -q -r "
                                        + remoteZip.toString()
                                        + " ."
                                )
                        );
                    } else {
                        run(sysSsh(options, session)
                                .addCommand("cd " + remotePlainFolder.resolve(sf).toString() + " ; zip -q -r "
                                        + remoteZip.toString()
                                        + " ."
                                )
                        );
                    }
                } else {
                    run(sysSsh(options, session)
                            .addCommand("zip -q -r "
                                    + remoteZip.toString()
                                    + " "
                                    + remotePlainFolder.toString()
                            )
                    );
                }
            }
            if (!plainFolder) {
                sshRm(remotePlainFolder, options, session);
            } else {
                sshPull(remotePlainFolder, plainFolderPath, options, session);
                sshRm(remotePlainFolder, options, session);
            }
            if (zip) {
                sshPull(remoteZip, zipPath, options, session);
                sshRm(remotePlainFolder, options, session);
            }
        } else {
            CmdRedirect dumpCommand = getSupport().createDumpCommand(plainFolderPath, options, session);
            NExecCommand nExecCommand = sysCmd(session).addCommand(dumpCommand.getCmd().toStringArray());
            if (dumpCommand.getPath() != null) {
                nExecCommand.setRedirectOutputFile(dumpCommand.getPath());
            }
            run(nExecCommand);
            if (zip) {
                if (getSupport().isFolderArchive(options)) {
                    String sf = getSupport().getZipSubFolder(options);
                    if (NBlankable.isBlank(sf)) {
                        NExecCommand zipExec = sysCmd(session)
                                .addCommand("zip")
                                .addCommand("-q");
                        if (plainFolderPath.isDirectory()) {
                            zipExec.addCommand("-r");
                            if (true) {
                                zipExec.addCommand("-j");
                            }
                        }
                        zipExec.addCommand(zipPath.toString());
                        zipExec.addCommand(".");
                        zipExec.setDirectory(plainFolderPath);
                        run(zipExec);
                    } else {

                        NExecCommand zipExec = sysCmd(session)
                                .addCommand("zip")
                                .addCommand("-q");
                        if (plainFolderPath.isDirectory()) {
                            zipExec.addCommand("-r");
                            if (true) {
                                zipExec.addCommand("-j");
                            }
                        }
                        zipExec.addCommand(zipPath.toString());
                        zipExec.addCommand(".");
                        zipExec.setDirectory(plainFolderPath.resolve(sf));
                        run(zipExec);
                    }
                } else {
                    NExecCommand zipExec = sysCmd(session)
                            .addCommand("zip")
                            .addCommand("-q");
                    if (plainFolderPath.isDirectory()) {
                        zipExec.addCommand("-r");
                        if (true) {
                            zipExec.addCommand("-j");
                        }
                    }
                    zipExec.addCommand(zipPath.toString());
                    zipExec.addCommand(plainFolderPath.toString());
                    zipExec.setDirectory(plainFolderPath.getParent());
                    run(zipExec);
                }


            }
            if (!plainFolder) {
                plainFolderPath.deleteTree();
            }
        }
    }

}
