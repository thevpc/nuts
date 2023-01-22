package net.thevpc.nuts.toolbox.ndb.base.cmd;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCommandLine;
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
//    public void run(NApplicationContext appContext, NCommandLine commandLine) {
//        NRef<AtName> name = NRef.ofNull(AtName.class);
//        ExtendedQuery eq = new ExtendedQuery(getName());
//        C otherOptions = createConfigInstance();
//        NSession session = appContext.getSession();
//
//        String status = "";
//        while (commandLine.hasNext()) {
//            switch (status) {
//                case "": {
//                    switch (commandLine.peek().get(session).key()) {
//                        case "--config": {
//                            readConfigNameOption(commandLine, session, name);
//                            break;
//                        }
//                        case "--entity":
//                        case "--table":
//                        case "--collection": {
//                            commandLine.withNextString((v, a, s) -> eq.setTable(v));
//                            break;
//                        }
//                        case "--where": {
//                            status = "--where";
//                            commandLine.withNextBoolean((v, a, s) -> {
//                            });
//                            break;
//                        }
//                        case "--set": {
//                            status = "--set";
//                            commandLine.withNextBoolean((v, a, s) -> {
//                            });
//                            break;
//                        }
//                        default: {
//                            fillOptionLast(commandLine, otherOptions);
//                        }
//                    }
//                    break;
//                }
//                case "--where": {
//                    switch (commandLine.peek().get(session).key()) {
//                        case "--set": {
//                            status = "--set";
//                            commandLine.withNextBoolean((v, a, s) -> {
//                            });
//                            break;
//                        }
//                        default: {
//                            eq.getWhere().add(commandLine.next().get().toString());
//                        }
//                    }
//                    break;
//                }
//                case "--set": {
//                    switch (commandLine.peek().get(session).key()) {
//                        case "--where": {
//                            status = "--where";
//                            commandLine.withNextBoolean((v, a, s) -> {
//                            });
//                            break;
//                        }
//                        default: {
//                            eq.getSet().add(commandLine.next().get().toString());
//                        }
//                    }
//                    break;
//                }
//            }
//        }
//        if (NBlankable.isBlank(eq.getTable())) {
//            commandLine.throwMissingArgumentByName("--table");
//        }
//
//        C options = loadFromName(name, otherOptions);
//        support.revalidateOptions(options);
//        if (NBlankable.isBlank(otherOptions.getDatabaseName())) {
//            commandLine.throwMissingArgumentByName("--dbname");
//        }
//        runDump(eq, options, session);
//    }
//
//    protected void runDump(ExtendedQuery eq, C options, NSession session) {
//        throw new NIllegalArgumentException(session, NMsg.ofPlain("invalid"));
//    }


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

        C options = loadFromName(name, otherOptions);
        revalidateOptions(options);
        getSupport().prepareDump(options, session);
        String simpleName = null;
        NPath sqlPath;
        NPath zipPath;
        boolean sql = false;
        boolean zip = false;
        String simpleName0 = options.getDatabaseName() + "-" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(new Date());
        String dumpExt = NStringUtils.trim(getSupport().getDumpExt(options, session));
        if (file.get() == null) {
            simpleName = simpleName0;
            sqlPath = NPath.of(simpleName + dumpExt, session);
            zipPath = NPath.of(simpleName + ".zip", session);
            sql = false;
            zip = true;
        } else if (file.get().isDirectory()) {
            simpleName = simpleName0;
            sqlPath = file.get().resolve(simpleName + dumpExt);
            zipPath = file.get().resolve(simpleName + ".zip");
            sql = false;
            zip = true;
        } else {
            simpleName = file.get().getBaseName();
            if (file.get().getName().toLowerCase().endsWith(".zip")) {
                zipPath = file.get();
                sqlPath = zipPath.resolveSibling(simpleName + dumpExt);
                sql = false;
                zip = true;
            } else if (dumpExt.length() > 0 && file.get().getName().toLowerCase().endsWith(dumpExt)) {
                sqlPath = file.get();
                zipPath = sqlPath.resolveSibling(simpleName + ".zip");
                sql = true;
                zip = false;
            } else {
                sqlPath = file.get().resolveSibling(file.get().getName() + dumpExt);
                zipPath = file.get().resolveSibling(file.get().getName() + ".zip");
                sql = false;
                zip = true;
            }
        }
        if (isRemoteCommand(options)) {
            NPath remoteTempFolder = getSupport().getRemoteTempFolder(options, session);
            NPath remoteSQL = remoteTempFolder.resolve(simpleName0 + dumpExt);
            NPath remoteZip = remoteTempFolder.resolve(simpleName0 + ".zip");
            CmdRedirect dumpCommand = getSupport().createDumpCommand(remoteSQL, options, session);
            run(getSupport().sysSsh(options, session)
                    .addCommand(dumpCommand.toString())
            );
            if (zip) {
                run(sysSsh(options, session)
                        .addCommand("zip")
                        .addCommand("-q")
                        .addCommand(remoteZip.toString())
                        .addCommand(remoteSQL.toString())
                );
            }
            if (!sql) {
                sshRm(remoteSQL,options, session);
            } else {
                sshPull(remoteSQL,sqlPath,options, session);
                sshRm(remoteSQL,options, session);
            }
            if (zip) {
                sshPull(remoteZip,zipPath,options, session);
                sshRm(remoteSQL,options, session);
            }
        } else {
            CmdRedirect dumpCommand = getSupport().createDumpCommand(sqlPath, options, session);
            NExecCommand nExecCommand = sysCmd(session).addCommand(dumpCommand.getCmd().toStringArray());
            if (dumpCommand.getPath() != null) {
                nExecCommand.setRedirectOutputFile(dumpCommand.getPath());
            }
            run(nExecCommand);
            if (zip) {
                NExecCommand zipExec = sysCmd(session)
                        .addCommand("zip")
                        .addCommand("-q")
                        ;
                if (sqlPath.isDirectory()) {
                    zipExec.addCommand("-r");
                    if (true) {
                        zipExec.addCommand("-j");
                    }
                }
                zipExec.addCommand(zipPath.toString());
                zipExec.addCommand(sqlPath.toString());
                run(zipExec);
            }
            if (!sql) {
                sqlPath.deleteTree();
            }
        }
    }

}
