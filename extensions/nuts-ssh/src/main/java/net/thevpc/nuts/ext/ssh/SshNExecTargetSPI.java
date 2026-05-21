package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NDependency;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.*;
import net.thevpc.nuts.core.*;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.net.NConnectionStringBuilder;
import net.thevpc.nuts.spi.NExecTargetCommandContext;
import net.thevpc.nuts.spi.NExecTargetSPI;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.log.NLog;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SshNExecTargetSPI implements NExecTargetSPI {
    private CmdStr resolveNutsExecutableCommand(NExecTargetCommandContext context) {
        NSession session = NSession.of();
        NExec execCommand = context.execCommand();
        NDefinition def = execCommand.commandDefinition();
        NExecutionType executionType = execCommand.executionType();
        if (executionType == null) {
            executionType = NExecutionType.SPAWN;
        }


        ArrayList<String> cmd = new ArrayList<>();
        switch (executionType) {
            case OPEN: {
                throw new NIllegalArgumentException(NMsg.ofPlain("unsupported remote open execution type"));
            }
            case EMBEDDED:
            case SPAWN: {
                NWorkspaceOptionsBuilder wOptions = NWorkspaceOptionsBuilder.of();
                wOptions.bot(session.isBot());
                wOptions.confirm(session.confirm().orDefault());
                wOptions.dry(session.isDry());
                wOptions.showStacktrace(session.showStacktrace().orDefault());
                wOptions.expireTime(session.expireTime().orNull());
                wOptions.gui(session.isGui());
                wOptions.locale(session.locale().orDefault());
                wOptions.terminalMode(session.terminal().out().terminalMode());
                wOptions.trace(session.isTrace());
                wOptions.transitive(session.isTransitive());
                // will be processed "in amont"
                //wOptions.setRunAs(session1.getRunAs());
                //if(getExecCommand().getRunAs()!=null) {
                //    wOptions.setRunAs(getExecCommand().getRunAs());
                //}
                wOptions.fetchStrategy(session.fetchStrategy().orDefault());
                wOptions.executionType(session.executionType().orDefault());
                wOptions.executionType(executionType);
                wOptions.outLinePrefix(session.outLinePrefix());
                wOptions.outputFormat(session.outputFormat().orDefault());
                wOptions.outputFormatOptions(session.outputFormatOptions());

                String[] executorOptions = execCommand.executorOptions().toArray(new String[0]);
                RemoteConnectionStringInfo k = RemoteConnectionStringInfo.of(execCommand.connectionString());
                wOptions.workspace(k.getWorkspaceName(this));
                cmd.add(k.getJavaCommand(this));
                cmd.add("-jar");
                cmd.add(k.getNutsJar(this));
                NCmdLine ncmdLine = wOptions.toCmdLine(new NWorkspaceOptionsConfig().compact(true));
                cmd.addAll(ncmdLine.toStringList());
                int dependenciesCount = 0;
                boolean requireTempRepo = false;
                if (def != null) {
                    for (NDependency d : def.dependencies().get()) {
                        NId id = d.toId();
                        //just ignore nuts base deps!
                        if (
                                !id.equalsLongId(session.workspace().apiId())
                                        && !id.equalsLongId(session.workspace().runtimeId())
                        ) {
                            k.copyId(id, k.getStoreLocationCacheRepoSSH(this), null);
                            dependenciesCount++;
                        }
                    }
                    if (true) {
                        k.copyId(def.id(), k.getStoreLocationCacheRepoSSH(this), null);
                        dependenciesCount++;
                    }
                }
                //if (dependenciesCount > 0) {
                //    if (requireTempRepo) {
                cmd.add("-r=" + k.getStoreLocationCacheRepoSSH(this).location());
                //    }
                //}
                cmd.add("---caller-app=remote-nuts");

                // I'll see later on what to include here...
                List<String> exportedExecutorOptions = new ArrayList<>();
                {
                    NCmdLine executorOptionsCmdLine = NCmdLine.of(executorOptions);
                    //copy all nuts options here...
                    while (executorOptionsCmdLine.hasNext()) {
                        NOptional<List<NArg>> o = NWorkspaceCmdLineParser.nextNutsArgument(executorOptionsCmdLine, null);
                        if (o.isPresent()) {
                            switch (o.get().get(0).key()) {
                                case "--exec": {
                                    executorOptionsCmdLine.skip();
                                    break;
                                }
                                default: {
                                    cmd.addAll(o.get().stream().map(x -> x.toString()).collect(Collectors.toList()));
                                }
                            }
                        } else {
                            executorOptionsCmdLine.skip();
                        }
                        break;
                    }
                }

                cmd.add("--exec");
                cmd.addAll(exportedExecutorOptions);

                if (def != null) {
                    cmd.add(def.id().toString());
                }
                //wil not call context.getCommand() because we already added def!
                cmd.addAll(execCommand.command());

                return new CmdStr(
                        k.buildEffectiveCommand(cmd.toArray(new String[0]), execCommand.runAs(), executorOptions, this),
                        false
                );
            }
            case SYSTEM: {
                //effective command including def which should be null!
                return new CmdStr(context.command(), context.isRawCommand() && context.command().length == 1);
            }
            default: {
                throw new NUnsupportedEnumException(executionType);
            }
        }
    }

    private String runOnceSystemGrab(String cmd, NConnectionString connectionString) {
        try (SshConnection sshc = SshConnectionPool.of().acquire(connectionString)) {
            return sshc.execStringCommandGrabbed(cmd).outString();
        }
    }

    @Override
    public int exec(NExecTargetCommandContext context) {
        NConnectionString target = context.connectionString();
        NAssert.requireNamedNonBlank(target, "target");
        NConnectionStringBuilder z = target.builder();
        NAssert.requireNamedNonBlank(z, "target");
        NLog log = NLog.of(SshNExecTargetSPI.class);
        log.log(NMsg.ofC("[%s] %s", z, NCmdLine.of(context.command())).asFiner().withIntent(NMsgIntent.START));
        NExecutionType executionType = context.execCommand().executionType();
        if (executionType == null) {
            executionType = NExecutionType.SPAWN;
        }
        boolean userWorkspace = executionType != NExecutionType.SYSTEM;
        if (userWorkspace) {
            CmdStr command = resolveNutsExecutableCommand(context);
            try (SshConnection c = SshConnectionPool.of().acquire(target)) {
                if (command.rawCommand) {
                    return c.execStringCommand(command.command[0], new IOBindings(context.in(), context.out(), context.err()));
                } else {
                    return c.execArrayCommand(command.command, new IOBindings(context.in(), context.out(), context.err()));
                }
            }
        } else {
            try (SshConnection c = SshConnectionPool.of().acquire(target)) {
                CmdStr command = new CmdStr(context.command(), context.isRawCommand() && context.command().length == 1);
                if (command.rawCommand) {
                    return c.execStringCommand(command.command[0], new IOBindings(context.in(), context.out(), context.err()));
                } else {
                    return c.execArrayCommand(command.command, new IOBindings(context.in(), context.out(), context.err()));
                }
            }
        }
    }

    private static class CmdStr {
        String[] command;
        boolean rawCommand;

        public CmdStr(String[] command, boolean rawCommand) {
            this.command = command;
            this.rawCommand = rawCommand;
        }
    }

    public int exec0(NExecTargetCommandContext context) {
        NConnectionString target = context.connectionString();
        NAssert.requireNamedNonBlank(target, "target");
        NConnectionStringBuilder z = target.builder();
        NAssert.requireNamedNonBlank(z, "target");
        NLog log = NLog.of(SshNExecTargetSPI.class);
        log.log(NMsg.ofC("[%s] %s", z, NCmdLine.of(context.command())).asFiner().withIntent(NMsgIntent.START));
        try (SshConnection c = SshConnectionPool.of().acquire(target)) {
            String[] command = context.command();
            return c.execArrayCommand(command, new IOBindings(context.in(), context.out(), context.err()));
        }
    }

    @NScore
    public static int getScore(NScorableContext context) {
        Object c = context.criteria();
        if (c instanceof String) {
            NConnectionStringBuilder z = NConnectionStringBuilder.get((String) c).orNull();
            if (z != null && isSupportedProtocol(z.protocol())) {
                return NScorable.DEFAULT_SCORE;
            }
        }
        if (c instanceof NConnectionStringBuilder) {
            NConnectionStringBuilder z = (NConnectionStringBuilder) c;
            if (isSupportedProtocol(z.protocol())) {
                return NScorable.DEFAULT_SCORE;
            }
        }
        if (c instanceof NConnectionString) {
            NConnectionString z = (NConnectionString) c;
            if (isSupportedProtocol(z.protocol())) {
                return NScorable.DEFAULT_SCORE;
            }
        }
        return NScorable.UNSUPPORTED_SCORE;
    }


    private static boolean isSupportedProtocol(String protocol) {
        return (
                "ssh".equals(protocol)
        );
    }

}
