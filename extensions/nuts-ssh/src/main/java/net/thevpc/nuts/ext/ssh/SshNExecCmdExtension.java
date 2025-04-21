package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NWorkspaceCmdLineParser;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogVerb;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class SshNExecCmdExtension implements NExecCmdExtension {
    private String[] resolveNutsExecutableCommand(NExecCmdExtensionContext context) {
        NSession session = context.getSession();
        NExecCmd execCommand = context.getExecCommand();
        NDefinition def = execCommand.getCommandDefinition();
        NExecutionType executionType = execCommand.getExecutionType();
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
                wOptions.setBot(session.isBot());
                wOptions.setConfirm(session.getConfirm().orDefault());
                wOptions.setDry(session.isDry());
                wOptions.setShowStacktrace(session.getShowStacktrace().orDefault());
                wOptions.setExpireTime(session.getExpireTime().orNull());
                wOptions.setGui(session.isGui());
                wOptions.setLocale(session.getLocale().orDefault());
                wOptions.setTerminalMode(session.getTerminal().getOut().getTerminalMode());
                wOptions.setTrace(session.isTrace());
                wOptions.setTransitive(session.isTransitive());
                // will be processed "in amont"
                //wOptions.setRunAs(session1.getRunAs());
                //if(getExecCommand().getRunAs()!=null) {
                //    wOptions.setRunAs(getExecCommand().getRunAs());
                //}
                wOptions.setFetchStrategy(session.getFetchStrategy().orDefault());
                wOptions.setExecutionType(session.getExecutionType().orDefault());
                wOptions.setExecutionType(executionType);
                wOptions.setOutLinePrefix(session.getOutLinePrefix());
                wOptions.setOutputFormat(session.getOutputFormat().orDefault());
                wOptions.setOutputFormatOptions(session.getOutputFormatOptions());

                String[] executorOptions = execCommand.getExecutorOptions().toArray(new String[0]);
                RemoteConnexionStringInfo k = RemoteConnexionStringInfo.of(execCommand.getTarget());
                wOptions.setWorkspace(k.getWorkspaceName(this));
                cmd.add(k.getJavaCommand(this));
                cmd.add("-jar");
                cmd.add(k.getNutsJar(this));
                NCmdLine ncmdLine = wOptions.toCmdLine(new NWorkspaceOptionsConfig().setCompact(true));
                cmd.addAll(ncmdLine.toStringList());
                int dependenciesCount = 0;
                boolean requireTempRepo = false;
                if (def != null) {
                    for (NDependency d : def.getDependencies().get()) {
                        NId id = d.toId();
                        //just ignore nuts base deps!
                        if (
                                !id.equalsLongId(session.getWorkspace().getApiId())
                                        && !id.equalsLongId(session.getWorkspace().getRuntimeId())
                        ) {
                            k.copyId(id, k.getStoreLocationCacheRepoSSH(this), null);
                            dependenciesCount++;
                        }
                    }
                    if (true) {
                        k.copyId(def.getId(), k.getStoreLocationCacheRepoSSH(this), null);
                        dependenciesCount++;
                    }
                }
                //if (dependenciesCount > 0) {
                //    if (requireTempRepo) {
                cmd.add("-r=" + k.getStoreLocationCacheRepoSSH(this).getLocation());
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
                    cmd.add(def.getId().toString());
                }
                //wil not call context.getCommand() because we already added def!
                cmd.addAll(execCommand.getCommand());

                return k.buildEffectiveCommand(cmd.toArray(new String[0]), execCommand.getRunAs(), executorOptions, this);
            }
            case SYSTEM: {
                //effective command including def which should be null!
                return context.getCommand();
            }
            default: {
                throw new NUnsupportedEnumException(executionType);
            }
        }
    }

    @Override
    public int exec(NExecCmdExtensionContext context) {
//        NExecCmd execCommand = context.getExecCommand();
        NSession session = context.getSession();
        //String[] executorOptions = execCommand.getExecutorOptions().toArray(new String[0]);
        //RemoteConnexionStringInfo k = RemoteConnexionStringInfo.of(execCommand.getTarget(), session);
        //String[] remoteCommand = k.buildEffectiveCommand(context.getCommand(), execCommand.getRunAs(), executorOptions, this, session);
        String target = context.getTarget();
        NAssert.requireNonBlank(target, "target");
        NConnexionStringBuilder z = DefaultNConnexionStringBuilder.of(target).orNull();
        NAssert.requireNonBlank(z, "target");
        NLog log = NLog.of(SshNExecCmdExtension.class);
        log.with().level(Level.FINER).verb(NLogVerb.START).log(NMsg.ofC("[%s] %s", z, NCmdLine.of(context.getCommand())));
        String[] command = resolveNutsExecutableCommand(context);
        try (SShConnection c = new SShConnection(
                target,
                context.in(),
                context.out(),
                context.err()
        )) {
            return c.execStringCommand(NCmdLine.of(command).toString());
        }
    }

    public int exec0(NExecCmdExtensionContext context) {
        String target = context.getTarget();
        NAssert.requireNonBlank(target, "target");
        NConnexionStringBuilder z = DefaultNConnexionStringBuilder.of(target).orNull();
        NAssert.requireNonBlank(z, "target");
        NSession session = context.getSession();
        NLog log = NLog.of(SshNExecCmdExtension.class);
        log.with().level(Level.FINER).verb(NLogVerb.START).log(NMsg.ofC("[%s] %s", z, NCmdLine.of(context.getCommand())));
        try (SShConnection c = new SShConnection(
                target,
                context.in(),
                context.out(),
                context.err()
        )) {
            String[] command = context.getCommand();
            return c.execStringCommand(NCmdLine.of(command).toString());
        }
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        Object c = context.getConstraints();
        if (c instanceof String) {
            NConnexionStringBuilder z = DefaultNConnexionStringBuilder.of((String) c).orNull();
            if (z != null && "ssh".equals(z.getProtocol())) {
                return NConstants.Support.DEFAULT_SUPPORT;
            }
        }
        if (c instanceof DefaultNConnexionStringBuilder) {
            NConnexionStringBuilder z = (NConnexionStringBuilder) c;
            if ("ssh".equals(z.getProtocol())) {
                return NConstants.Support.DEFAULT_SUPPORT;
            }
        }
        return NConstants.Support.NO_SUPPORT;
    }

}
