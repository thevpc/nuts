package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;

import net.thevpc.nuts.NIdLocation;

import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.runtime.standalone.DefaultNDescriptorBuilder;
import net.thevpc.nuts.runtime.standalone.NWorkspaceProfilerImpl;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNDefinitionBuilder;
import net.thevpc.nuts.NDefinitionBuilder;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.internal.DefaultInternalNExecutableCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.internal.NInternalCommand;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.app.cmdline.NCmdLineUtils;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNInstallInfo;
import net.thevpc.nuts.runtime.standalone.descriptor.parser.NDescriptorContentResolver;
import net.thevpc.nuts.runtime.standalone.executor.NExecutionContextUtils;
import net.thevpc.nuts.runtime.standalone.executor.system.NSysExecUtils;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.io.util.URLBuilder;
import net.thevpc.nuts.runtime.standalone.io.util.ZipOptions;
import net.thevpc.nuts.runtime.standalone.io.util.ZipUtils;
import net.thevpc.nuts.runtime.standalone.security.util.CoreDigestHelper;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NExecutableInformationExt;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NExecutionContextBuilder;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.executor.ArtifactExecutorComponent;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.bundle.DefaultNBundleInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.deploy.DefaultNDeployInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.artifact.DefaultNArtifactExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.open.DefaultNOpenExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.path.DefaultNArtifactPathExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.system.DefaultNSystemExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.remote.ssh.system.DefaultNSystemExecutableRemote;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.remote.ssh.artifact.DefaultSpawnExecutableNutsRemote;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.fetch.DefaultNFetchInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.help.DefaultNHelpInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.info.DefaultNInfoInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.install.DefaultNInstallInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.install.DefaultNReinstallInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.license.DefaultNLicenseInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.prepare.DefaultNPrepareInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.push.DefaultNPushInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.search.DefaultNSearchInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.alias.DefaultNAliasExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.DefaultNSettingsInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.undeploy.DefaultNUndeployInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.uninstall.DefaultNUninstallInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.update.DefaultNCheckUpdatesInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.update.DefaultNUpdateInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.version.DefaultNVersionInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.welcome.DefaultNWelcomeInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.which.DefaultNWhichInternalExecutable;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringPlaceHolderParser;
import net.thevpc.nuts.spi.NDependencySolver;
import net.thevpc.nuts.spi.NExecutorComponent;
import net.thevpc.nuts.time.NChronometer;
import net.thevpc.nuts.util.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;

/**
 * type: Command Class
 *
 * @author thevpc
 */
public class DefaultNExecCmd extends AbstractNExecCmd {

    public DefaultNExecCmd(NWorkspace workspace) {
        super(workspace);
    }


    private void refactorCommand() {
        if (getCommandDefinition() != null) {
            return;
        }
        boolean someUpdates = true;
        while (someUpdates) {
            someUpdates = false;
            List<String> command0 = new ArrayList<>(getCommand());
            if (!command0.isEmpty()) {
                String cmd = command0.get(0);
                if ("exec".equals(cmd)) {
                    NCmdLine cmdLine = NCmdLine.of(command0);
                    cmdLine.skip(); //skip exec
                    setCommand(new ArrayList<>());//reset command
                    while (cmdLine.hasNext()) {
                        configureLast(cmdLine);
                    }
                    someUpdates = true;
                } else if ("-".equals(cmd)) {
                    List<String> newCmd = new ArrayList<>();
                    newCmd.add(NConstants.Ids.NSH);
                    //remove the dash
                    command0.remove(0);
                    if (!command0.isEmpty()) {
                        newCmd.add("-c");
                        newCmd.addAll(command0);
                    }
                    setCommand(newCmd);
                    someUpdates = true;
                } else if (NArg.of(cmd).isOption()) {
                    ArrayList<String> aa = new ArrayList<>();
                    aa.add("exec");
                    aa.addAll(command0);
                    setCommand(aa);
                    someUpdates = true;
                }
            }
        }
    }

    @Override
    public NExecutableInformation which() {
        NSession session = NSession.of();
        refactorCommand();

        NExecutableInformationExt exec = null;
        NExecutionType executionType = this.getExecutionType();
        NRunAs runAs = this.getRunAs();
        if (executionType == null) {
            executionType = session.getExecutionType().orDefault();
        }
        if (executionType == null) {
            executionType = NExecutionType.SPAWN;
        }
        switch (executionType) {
            case OPEN: {
                NAssert.requireNonNull(getCommandDefinition(), "artifact definition");
                NAssert.requireNonBlank(command, "command");
                String target = getConnexionString();
                if (!NBlankable.isBlank(target)) {
                    throw new NIllegalArgumentException(NMsg.ofC("cannot run %s command remotely", executionType));
                }
                String[] ts = command.toArray(new String[0]);
                exec = new DefaultNOpenExecutable(ts, getExecutorOptions().toArray(new String[0]), this);
                break;
            }
            case SYSTEM: {
                RemoteInfo0 remoteInfo0 = resolveRemoteInfo0();
                if (remoteInfo0 != null) {
                    NExecutionType finalExecutionType = executionType;
                    NAssert.requireNull(getCommandDefinition(), () -> NMsg.ofC("unable to run artifact as %s cmd", finalExecutionType));
                    NAssert.requireNonBlank(command, "command");
                    String[] ts = command.toArray(new String[0]);
                    return new DefaultNSystemExecutableRemote(
                            remoteInfo0.commExec, ts,
                            getExecutorOptions(),
                            this,
                            remoteInfo0.in0,
                            remoteInfo0.out0,
                            remoteInfo0.err0
                    );
                } else {
                    NExecutionType finalExecutionType = executionType;
                    NAssert.requireNull(getCommandDefinition(), () -> NMsg.ofC("unable to run artifact as %s cmd", finalExecutionType));
                    NAssert.requireNonBlank(command, "command");
                    String[] ts = command.toArray(new String[0]);
                    List<String> tsl = new ArrayList<>(Arrays.asList(ts));
                    if (CoreStringUtils.firstIndexOf(ts[0], new char[]{'/', '\\'}) < 0) {
                        Path p = NSysExecUtils.sysWhich(ts[0]);
                        if (p != null) {
                            tsl.set(0, p.toString());
                        }
                    }
                    exec = new DefaultNSystemExecutable(tsl.toArray(new String[0]),
                            getExecutorOptions(),
                            this
                    );
                }
                break;
            }
            case SPAWN:
            case EMBEDDED: {
                if (getCommandDefinition() != null) {
                    RemoteInfo0 remoteInfo0 = resolveRemoteInfo0();
                    if (remoteInfo0 != null) {
                        String[] ts = command == null ? new String[0] : command.toArray(new String[0]);
                        return new DefaultSpawnExecutableNutsRemote(remoteInfo0.commExec,
                                getCommandDefinition(),
                                getCommandDefinition().getId().toString(),
                                NCmdLine.of(ts).toString(),
                                ts, getExecutorOptions(), this, remoteInfo0.in0, remoteInfo0.out0, remoteInfo0.err0);
                    } else {
                        String[] ts = command == null ? new String[0] : command.toArray(new String[0]);
                        return ws_execDef(getCommandDefinition(), getCommandDefinition().getId().getLongName(), ts, getExecutorOptions(), workspaceOptions, env, directory, failFast,
                                executionType, runAs);
                    }
                } else {
                    NAssert.requireNonBlank(command, "command");
                    String[] ts = command.toArray(new String[0]);
                    exec = execEmbeddedOrExternal(ts, getExecutorOptions(), getWorkspaceOptions(), session);
                }
                break;
            }
            default: {
                throw new NUnsupportedArgumentException(NMsg.ofC("invalid execution type %s", executionType));
            }
        }
        return exec;
    }

    private void runLoop(NExecutableInformationExt exec) {
        int count = 0;
        NExecutionException err = null;
        while (true) {
            err = null;
            try {
                runOnce(exec);
            } catch (NExecutionException e) {
                err = e;
            }
            count++;
            if (this.multipleRunsMaxCount >= 0) {
                if (count >= this.multipleRunsMaxCount) {
                    break;
                }
            }
            if (executionTime.getTimeAsMillis() <= multipleRunsMinTimeMs) {
                //exec exited too fast
                break;
            }
            if (multipleRunsSafeTimeMs > 0) {
                NWorkspaceProfilerImpl.sleep(multipleRunsSafeTimeMs, "DefaultNExecCmd::runLoop");
            }
        }
        if (err != null) {
            throw err;
        }
    }

    private void runOnceOrMultiple(NExecutableInformationExt exec) {
        if (this.multipleRuns || !NBlankable.isBlank(this.multipleRunsCron)) {
            if (NBlankable.isBlank(this.multipleRunsCron)) {
                runLoop(exec);
            } else {
                if (this.multipleRuns) {
                    runLoop(exec);
                } else {
                    //cron not supported yet
                    runOnce(exec);
                }
            }
        } else {
            runOnce(exec);
        }
    }

    private void runOnce(NExecutableInformationExt exec) {
        executed = true;
        executionTime = null;
        NChronometer chrono = NChronometer.startNow();
        try {
            int exitCode = NExecutionException.SUCCESS;
            try {
                exitCode = exec.execute();
            } catch (NExecutionException ex) {
                String p = getExtraErrorMessage();
                if (p != null) {
                    resultException = new NExecutionException(
                            NMsg.ofC("execution failed with code %s and message : %s", ex.getExitCode(), p),
                            ex, ex.getExitCode());
                } else {
                    resultException = ex;
                }
            } catch (Exception ex) {
                String p = getExtraErrorMessage();
                int exceptionExitCode = NExceptionHandler.resolveExitCode(ex).orElse(NExecutionException.ERROR_255);
                if (exceptionExitCode != NExecutionException.SUCCESS) {
                    if (!NBlankable.isBlank(p)) {
                        resultException = new NExecutionException(
                                NMsg.ofC("execution of (%s) failed with code %s ; error was : %s ; notes : %s", exec, exceptionExitCode, ex, p),
                                ex, exceptionExitCode);
                    } else {
                        resultException = new NExecutionException(
                                NMsg.ofC("execution of (%s) failed with code %s ; error was : %s", exec, exceptionExitCode, ex),
                                ex, exceptionExitCode);
                    }
                }
            }
            if (resultException == null) {
                if (exitCode != NExecutionException.SUCCESS) {
                    resultException = new NExecutionException(
                            NMsg.ofC("execution of (%s) failed with code %s", exec, exitCode),
                            exitCode);
                }
            }
            if (resultException != null && resultException.getExitCode() != NExecutionException.SUCCESS && failFast) {
                throw resultException;
//            checkFailFast(result.getExitCode());
            }
        } finally {
            chrono.stop();
            executionTime = chrono.getDuration();
        }
    }

    @Override
    public NExecCmd run() {
        try (NExecutableInformationExt exec = (NExecutableInformationExt) which()) {
            runOnceOrMultiple(exec);
        }
        return this;
    }

    private NExecutableInformationExt execEmbeddedOrExternal(String[] cmd, List<String> executorOptions, List<String> workspaceOptions, NSession prepareSession) {
        NSession session = NSession.of();
        NAssert.requireNonBlank(cmd, "command");
        String[] args = new String[cmd.length - 1];
        System.arraycopy(cmd, 1, args, 0, args.length);
        String cmdName = cmd[0];
        //resolve internal commands!
        NExecutionType executionType = getExecutionType();
        if (executionType == null) {
            executionType = session.getExecutionType().orDefault();
        }
        if (executionType == null) {
            executionType = NExecutionType.SPAWN;
        }
        NRunAs runAs = getRunAs();
        CmdKind cmdKind = null;
        NId goodId = null;
        String goodKw = null;
        boolean forceInstalled = false;
        if (cmdName.endsWith("!")) {
            goodId = NId.get(cmdName.substring(0, cmdName.length() - 1)).orNull();
            if (goodId != null) {
                forceInstalled = true;
            }
        } else {
            goodId = NId.get(cmdName).orNull();
        }

        if (cmdName.equalsIgnoreCase(".") || cmdName.equals("..")) {
            cmdKind = CmdKind.PATH;
        } else if (cmdName.contains("/") || cmdName.contains("\\")) {
            if (goodId != null) {
                cmdKind = CmdKind.ID;
            } else {
                cmdKind = CmdKind.PATH;
            }
        } else if (cmdName.contains(":") || cmdName.contains("#")) {
            if (goodId != null) {
                cmdKind = CmdKind.ID;
            } else {
                throw new NNotFoundException(null, NMsg.ofC("unable to resolve id %s", cmdName));
            }
        } else {
            if (cmdName.endsWith("!")) {
                //name that terminates with '!'
                goodKw = cmdName.substring(0, cmdName.length() - 1);
                forceInstalled = true;
            } else {
                goodKw = cmdName;
            }
            cmdKind = CmdKind.KEYWORD;
        }
        switch (cmdKind) {
            case PATH: {
                RemoteInfo0 remoteInfo0 = resolveRemoteInfo0();
                if (remoteInfo0 != null) {
                    NAssert.requireNonBlank(command, "command");
                    List<String> ts = new ArrayList<>(command);
//                    NDefinition def2 = NSearchCmd.of()
//                            .addId(session.getWorkspace().getApiId())
//                            .latest()
//                            .setDependencyFilter(NDependencyFilters.of().byRunnable())
//                            .failFast()
//                            .getResultDefinitions()
//                            .findFirst().get();
                    return new DefaultSpawnExecutableNutsRemote(remoteInfo0.commExec, null,
                            !ts.isEmpty() ? ts.get(0) : "",
                            (!NBlankable.isBlank(getConnexionString()) && ts.size() == 1) ? ts.get(0) : NCmdLine.of(ts).toString(),
                            ts.toArray(new String[0]), getExecutorOptions(), this, remoteInfo0.in0, remoteInfo0.out0, remoteInfo0.err0);
                } else {
                    CharacterizedExecFile c = null;
                    NPath path = null;
                    try {
                        path = NPath.of(cmdName);
                        c = characterizeForExec(path, executorOptions);
                    } catch (Exception ex) {
                        //
                    }
                    if (c != null) {
                        NDescriptor descriptor = c.getDescriptor();
                        if (descriptor != null) {
                            try {
                                descriptor = NWorkspace.of().resolveEffectiveDescriptor(descriptor, new NDescriptorEffectiveConfig().setIgnoreCurrentEnvironment(true));
                            } catch (NNotFoundException ex) {
                                //ignore
                                _LOGOP().level(Level.WARNING).verb(NLogVerb.WARNING)
                                        .log(NMsg.ofC("executable artifact descriptor found, but one of its parents or dependencies is not: %s : missing %s", descriptor.getId(),
                                                ex.getId()));
                                throw ex;
                            }
                            NId _id = descriptor.getId();

                            NDefinitionBuilder nutToRun = new DefaultNDefinitionBuilder()
                                    .setId(_id.getLongId())
                                    .setDependency(_id.toDependency())
                                    .setDescriptor(descriptor)
                                    .setContent(NPath.of(c.getContentFile()).setUserCache(false).setUserTemporary(!c.getTemps().isEmpty()))
                                    .setInstallInformation(DefaultNInstallInfo.notInstalled(_id));

                            NDependencySolver resolver = NDependencySolver.of();
                            NDependencyFilters ff = NDependencyFilters.of();

                            resolver
                                    .setRepositoryFilter(null)
                                    .setDependencyFilter(ff.byScope(NDependencyScopePattern.RUN)
//                            .and(ff.byOptional(getOptional())
//                            ).and(getDependencyFilter())
                                    );
                            for (NDependency dependency : descriptor.getDependencies()) {
                                resolver.add(dependency);
                            }
                            nutToRun.setDependencies(resolver.solve());
                            NDefinition nd = nutToRun.build();
                            try {
                                NExecutorComponentAndContext ec = this.ws_execId2(nd, cmdName, args, executorOptions, workspaceOptions, this.getEnv(),
                                        this.getDirectory(), this.isFailFast(), true,
                                        this.getIn(),
                                        this.getOut(),
                                        this.getErr(),
                                        executionType, runAs);
                                return new DefaultNArtifactPathExecutable(cmdName, args, executorOptions, workspaceOptions, executionType, runAs, this, nd, c, null, ec);
                            } catch (Exception ex) {
                                //fallback to other cases
                                c.close();
                            }
                        } else {
                            c.close();
                        }
                    }
                    if (path != null) {
                        if (path.isLocal() && path.isRegularFile()) {
                            List<String> cmdArr = new ArrayList<>();
                            cmdArr.add(cmdName);
                            cmdArr.addAll(Arrays.asList(args));
                            return new DefaultNSystemExecutable(cmdArr.toArray(new String[0]), executorOptions, this);
                        }
                        List<String> cmdArr = new ArrayList<>();
                        cmdArr.add(cmdName);
                        cmdArr.addAll(Arrays.asList(args));
                        return new DefaultUnknownExecutable(cmdArr.toArray(new String[0]), this);
                    }
                    throw new NNotFoundException(goodId, NMsg.ofC("unable to resolve id %s", path));
                }
            }
            case ID: {
                NId idToExec = findExecId(goodId, prepareSession, forceInstalled, true);
                RemoteInfo0 remoteInfo0 = resolveRemoteInfo0();
                if (remoteInfo0 != null) {
                    NAssert.requireNonBlank(command, "command");
                    List<String> ts = new ArrayList<>(command);
                    if (ts.isEmpty()) {
                        throw new NUnsupportedArgumentException(NMsg.ofPlain("missing command"));
                    }
                    String id = ts.get(0);
                    ts.remove(0);
                    NDefinition def2 = NSearchCmd.of()
                            .addId(id)
                            .latest()
                            .setDependencyFilter(NDependencyFilters.of().byRunnable())
                            .failFast()
                            .getResultDefinitions()
                            .findFirst().get();
                    return new DefaultSpawnExecutableNutsRemote(remoteInfo0.commExec, def2, id,
                            NCmdLine.of(ts).toString(),
                            ts.toArray(new String[0]), getExecutorOptions(), this, remoteInfo0.in0, remoteInfo0.out0, remoteInfo0.err0);

                } else {
                    if (idToExec != null) {
                        return ws_execId(idToExec, cmdName, args, executorOptions, workspaceOptions, executionType, runAs);
                    } else {
                        throw new NNotFoundException(goodId);
                    }
                }
            }
            case KEYWORD: {
                Map<String, NInternalCommand> internalCommands = NWorkspaceExt.of().getCommandModel().getInternalCommands();
                NInternalCommand ic = internalCommands.get(goodKw);
                if (ic != null) {
                    RemoteInfo0 remoteInfo0 = resolveRemoteInfo0();
                    if (remoteInfo0 != null) {
                        return _runRemoteInternalCommand(goodKw, remoteInfo0);
                    }
                    return new DefaultInternalNExecutableCommand(ic, args, this);
                }
                switch (goodKw) {
                    case "update": {
                        RemoteInfo0 remoteInfo0 = resolveRemoteInfo0();
                        if (remoteInfo0 != null) {
                            return _runRemoteInternalCommand(goodKw, remoteInfo0);
                        }
                        return new DefaultNUpdateInternalExecutable(args, this);
                    }
                    case "check-updates": {
                        RemoteInfo0 remoteInfo0 = resolveRemoteInfo0();
                        if (remoteInfo0 != null) {
                            return _runRemoteInternalCommand(goodKw, remoteInfo0);
                        }
                        return new DefaultNCheckUpdatesInternalExecutable(args, this);
                    }
                    case "install": {
                        RemoteInfo0 remoteInfo0 = resolveRemoteInfo0();
                        if (remoteInfo0 != null) {
                            return _runRemoteInternalCommand(goodKw, remoteInfo0);
                        }
                        return new DefaultNInstallInternalExecutable(args, this);
                    }
                    case "reinstall": {
                        RemoteInfo0 remoteInfo0 = resolveRemoteInfo0();
                        if (remoteInfo0 != null) {
                            return _runRemoteInternalCommand(goodKw, remoteInfo0);
                        }
                        return new DefaultNReinstallInternalExecutable(args, this);
                    }
                    case "uninstall": {
                        RemoteInfo0 remoteInfo0 = resolveRemoteInfo0();
                        if (remoteInfo0 != null) {
                            return _runRemoteInternalCommand(goodKw, remoteInfo0);
                        }
                        return new DefaultNUninstallInternalExecutable(args, this);
                    }
                    case "deploy": {
                        RemoteInfo0 remoteInfo0 = resolveRemoteInfo0();
                        if (remoteInfo0 != null) {
                            return _runRemoteInternalCommand(goodKw, remoteInfo0);
                        }
                        return new DefaultNDeployInternalExecutable(args, this);
                    }
                    case "undeploy": {
                        RemoteInfo0 remoteInfo0 = resolveRemoteInfo0();
                        if (remoteInfo0 != null) {
                            return _runRemoteInternalCommand(goodKw, remoteInfo0);
                        }
                        return new DefaultNUndeployInternalExecutable(args, this);
                    }
                    case "push": {
                        RemoteInfo0 remoteInfo0 = resolveRemoteInfo0();
                        if (remoteInfo0 != null) {
                            return _runRemoteInternalCommand(goodKw, remoteInfo0);
                        }
                        return new DefaultNPushInternalExecutable(args, this);
                    }
                    case "fetch": {
                        RemoteInfo0 remoteInfo0 = resolveRemoteInfo0();
                        if (remoteInfo0 != null) {
                            return _runRemoteInternalCommand(goodKw, remoteInfo0);
                        }
                        return new DefaultNFetchInternalExecutable(args, this);
                    }
                    case "search": {
                        RemoteInfo0 remoteInfo0 = resolveRemoteInfo0();
                        if (remoteInfo0 != null) {
                            return _runRemoteInternalCommand(goodKw, remoteInfo0);
                        }
                        return new DefaultNSearchInternalExecutable(args, this);
                    }
                    case "version": {
                        RemoteInfo0 remoteInfo0 = resolveRemoteInfo0();
                        if (remoteInfo0 != null) {
                            return _runRemoteInternalCommand(goodKw, remoteInfo0);
                        }
                        return new DefaultNVersionInternalExecutable(args, this);
                    }
                    case "prepare": {
                        RemoteInfo0 remoteInfo0 = resolveRemoteInfo0();
                        if (remoteInfo0 != null) {
                            return _runRemoteInternalCommand(goodKw, remoteInfo0);
                        }
                        return new DefaultNPrepareInternalExecutable(args, this);
                    }
                    case "license": {
                        RemoteInfo0 remoteInfo0 = resolveRemoteInfo0();
                        if (remoteInfo0 != null) {
                            return _runRemoteInternalCommand(goodKw, remoteInfo0);
                        }
                        return new DefaultNLicenseInternalExecutable(args, this);
                    }
                    case "bundle": {
                        RemoteInfo0 remoteInfo0 = resolveRemoteInfo0();
                        if (remoteInfo0 != null) {
                            return _runRemoteInternalCommand(goodKw, remoteInfo0);
                        }
                        return new DefaultNBundleInternalExecutable(args, this);
                    }
                    case "help": {
                        RemoteInfo0 remoteInfo0 = resolveRemoteInfo0();
                        if (remoteInfo0 != null) {
                            return _runRemoteInternalCommand(goodKw, remoteInfo0);
                        }
                        return new DefaultNHelpInternalExecutable(args, this);
                    }
                    case "welcome": {
                        RemoteInfo0 remoteInfo0 = resolveRemoteInfo0();
                        if (remoteInfo0 != null) {
                            return _runRemoteInternalCommand(goodKw, remoteInfo0);
                        }
                        return new DefaultNWelcomeInternalExecutable(args, this);
                    }
                    case "info": {
                        RemoteInfo0 remoteInfo0 = resolveRemoteInfo0();
                        if (remoteInfo0 != null) {
                            return _runRemoteInternalCommand(goodKw, remoteInfo0);
                        }
                        return new DefaultNInfoInternalExecutable(args, this);
                    }
                    case "which": {
                        RemoteInfo0 remoteInfo0 = resolveRemoteInfo0();
                        if (remoteInfo0 != null) {
                            return _runRemoteInternalCommand(goodKw, remoteInfo0);
                        }
                        return new DefaultNWhichInternalExecutable(args, this);
                    }
                    case "exec": {
                        RemoteInfo0 remoteInfo0 = resolveRemoteInfo0();
                        if (remoteInfo0 != null) {
                            return _runRemoteInternalCommand(goodKw, remoteInfo0);
                        }
                        return new DefaultNExecInternalExecutable(args, this);
                    }
                    case "settings": {
                        RemoteInfo0 remoteInfo0 = resolveRemoteInfo0();
                        if (remoteInfo0 != null) {
                            return _runRemoteInternalCommand(goodKw, remoteInfo0);
                        }
                        return new DefaultNSettingsInternalExecutable(args, this);
                    }
                }
                RemoteInfo0 remoteInfo0 = resolveRemoteInfo0();
                if (remoteInfo0 != null) {
                    NExecutionType finalExecutionType = executionType;
                    NAssert.requireNull(getCommandDefinition(), () -> NMsg.ofC("unable to run artifact as %s cmd", finalExecutionType));
                    NAssert.requireNonBlank(command, "command");
                    String[] ts = command.toArray(new String[0]);
                    return new DefaultNSystemExecutableRemote(
                            remoteInfo0.commExec, ts,
                            getExecutorOptions(),
                            this,
                            remoteInfo0.in0,
                            remoteInfo0.out0,
                            remoteInfo0.err0
                    );
                }
                NCustomCmd command = null;
                command = NWorkspace.of().findCommand(goodKw);
                if (command != null) {
                    NCmdExecOptions o = new NCmdExecOptions().setExecutorOptions(executorOptions).setDirectory(directory).setFailFast(failFast)
                            .setExecutionType(executionType).setEnv(env);
                    return new DefaultNAliasExecutable(command, o, args, this);
                } else {
                    NId idToExec = null;
                    if (goodId != null) {
                        idToExec = findExecId(goodId, prepareSession, forceInstalled, true);
                    }
                    if (idToExec == null) {
                        Path sw = NSysExecUtils.sysWhich(cmdName);
                        if (sw != null) {
                            List<String> cmdArr = new ArrayList<>();
                            cmdArr.add(sw.toString());
                            cmdArr.addAll(Arrays.asList(args));
                            return new DefaultNSystemExecutable(cmdArr.toArray(new String[0]), executorOptions, this);
                        }
                        List<String> cmdArr = new ArrayList<>();
                        cmdArr.add(cmdName);
                        cmdArr.addAll(Arrays.asList(args));
                        return new DefaultUnknownExecutable(cmdArr.toArray(new String[0]), this);
                    }
                    return ws_execId(idToExec, cmdName, args, executorOptions, workspaceOptions, executionType, runAs);
                }
            }
        }
        throw new NNotFoundException(goodId, NMsg.ofC("unable to resolve id %s", cmdName));
    }

    private NExecutableInformationExt _runRemoteInternalCommand(String goodKw, RemoteInfo0 remoteInfo0) {
        return new DefaultSpawnExecutableNutsRemote(remoteInfo0.commExec, null/*def2*/, goodKw,
                NCmdLine.of(command).toString(),
                command.toArray(new String[0]), getExecutorOptions(), this, remoteInfo0.in0, remoteInfo0.out0, remoteInfo0.err0);
    }

    protected NId findExecId(NId nid, NSession traceSession, boolean forceInstalled, boolean ignoreIfUserCommand) {
        if (nid == null) {
            return null;
        }
        switch (NStringUtils.firstNonNull(nid.getShortName(), "")) {
            case NConstants.Ids.NUTS_APP_ARTIFACT_ID: {
                nid = nid.builder().setGroupId(NConstants.Ids.NUTS_GROUP_ID).build();
                break;
            }
            case NConstants.Ids.NUTS_API:
            case NConstants.Ids.NUTS_RUNTIME:
            case NConstants.Ids.NUTS_API_ARTIFACT_ID:
            case NConstants.Ids.NUTS_RUNTIME_ARTIFACT_ID:
            case "net.thevpc.nuts:nuts-boot":
            case "nuts-boot":
            case "net.thevpc.nuts:nuts-lib":
            case "nuts-lib": {
                throw new NNotFoundException(nid, NMsg.ofC("%s is not executable", nid));
            }
        }
        if (NConstants.Ids.NUTS_APP_ARTIFACT_ID.equals(nid.getShortName())) {
            nid = nid.builder().setGroupId(NConstants.Ids.NUTS_GROUP_ID).build();
        }
        NSession.of().getTerminal().printProgress(NMsg.ofC("start searching for %s", nid));
        NId ff = NSearchCmd.of(nid)
                .setDependencyFilter(NDependencyFilters.of().byRunnable()).setLatest(true).setFailFast(false)
                .setDefinitionFilter(NDefinitionFilters.of().byDeployed(true))
                .getResultDefinitions().stream()
                .sorted(Comparator.comparing(x -> !x.getInstallInformation().get().isDefaultVersion())) // default first
                .map(NDefinition::getId).findFirst().orElse(null);
        if (ff == null) {
            if (!forceInstalled) {
                if (ignoreIfUserCommand && isUserCommand(nid.toString())) {
                    return null;
                }
                //now search online
                // this helps recover from "invalid default parseVersion" issue
                if (traceSession.isPlainTrace()) {
                    traceSession.out().resetLine().println(NMsg.ofC("%s is %s, will search for it...",
                            nid,
                            NText.ofStyledError("not installed")
                    ));
                    traceSession.out().flush();
                }
                ff = NSearchCmd.of(nid)
                        .setFetchStrategy(NFetchStrategy.OFFLINE)
                        .setDependencyFilter(NDependencyFilters.of().byRunnable())
                        .setFailFast(false)
                        .setLatest(true)
                        .getResultIds().findFirst().orElse(null);
                if (ff == null && NSession.of().getFetchStrategy().orElse(NFetchStrategy.ONLINE) != NFetchStrategy.OFFLINE) {
                    if (traceSession.isPlainTrace()) {
                        traceSession.out().resetLine().println(NMsg.ofC("%s is %s, will search for it online. Type %s to stop...",
                                nid,
                                NText.ofStyledError("not installed"),
                                NText.ofStyledError("CTRL^C")
                        ));
                        traceSession.out().flush();
                    }
                    ff = NSearchCmd.of(nid)
                            .setFetchStrategy(NFetchStrategy.ONLINE)
                            .setDependencyFilter(NDependencyFilters.of().byRunnable())
                            .setFailFast(false)
                            .setLatest(true)
                            .getResultIds().findFirst().orElse(null);
                }
            }
        }
        if (ff == null) {
            return null;
        } else {
            return ff;
        }
    }

    public boolean isUserCommand(String s) {
        String p = NWorkspace.of().getSysEnv("PATH").orNull();
        if (p != null) {
            char r = File.pathSeparatorChar;
            for (String z : p.split("" + r)) {
                Path t = Paths.get(z);
                switch (NWorkspace.of().getOsFamily()) {
                    case WINDOWS: {
                        if (Files.isRegularFile(t.resolve(s))) {
                            return true;
                        }
                        if (Files.isRegularFile(t.resolve(s + ".exe"))) {
                            return true;
                        }
                        if (Files.isRegularFile(t.resolve(s + ".bat"))) {
                            return true;
                        }
                        break;
                    }
                    default: {
                        Path fp = t.resolve(s);
                        if (Files.isRegularFile(fp)) {
                            //if(Files.isExecutable(fp)) {
                            return true;
                            //}
                        }
                    }
                }
            }
        }
        return false;
    }

    protected NExecutableInformationExt ws_execId(NId goodId, String commandName, String[] appArgs, List<String> executorOptions,
                                                  List<String> workspaceOptions, NExecutionType executionType, NRunAs runAs) {
        NDefinition def = null;
        try {
            def = NFetchCmd.of(goodId)
                    .failFast()
                    .setDependencyFilter(NDependencyFilters.of().byRunnable())
                    .setRepositoryFilter(NRepositoryFilters.of().installedRepo())
                    .getResultDefinition();
        } catch (Exception ex) {
            //try to find locally
        }
        if (def == null) {
            def = NFetchCmd.of(goodId)
                    .failFast()
                    .setDependencyFilter(NDependencyFilters.of().byRunnable())
                    .getResultDefinition();
        }
        return ws_execDef(def, commandName, appArgs, executorOptions, this.workspaceOptions, env, directory, failFast, executionType, runAs);
    }

    protected NExecutableInformationExt ws_execDef(NDefinition def, String commandName, String[] appArgs, List<String> executorOptions, List<String> workspaceOptions, Map<String, String> env, NPath dir, boolean failFast, NExecutionType executionType, NRunAs runAs) {
        return new DefaultNArtifactExecutable(def, commandName, appArgs, executorOptions, workspaceOptions, env, dir, failFast, executionType, runAs, this);
    }

    public int ws_execId(NDefinition def, String commandName, String[] appArgs,
                         List<String> executorOptions,
                         List<String> workspaceOptions, Map<String, String> env, NPath dir, boolean failFast, boolean temporary,
                         NExecInput in,
                         NExecOutput out,
                         NExecOutput err,
                         NExecutionType executionType,
                         NRunAs runAs
    ) {
        NExecutorComponentAndContext e = ws_execId2(def, commandName, appArgs,
                executorOptions,
                workspaceOptions, env, dir, failFast, temporary,
                in,
                out,
                err,
                executionType,
                runAs);
        return e.component.exec(e.executionContext);
    }

    public static class NExecutorComponentAndContext {
        NExecutorComponent component;
        NExecutionContext executionContext;

        public NExecutorComponentAndContext(NExecutorComponent component, NExecutionContext executionContext) {
            this.component = component;
            this.executionContext = executionContext;
        }

        public NExecutorComponent getComponent() {
            return component;
        }

        public NExecutionContext getExecutionContext() {
            return executionContext;
        }
    }

    public NExecutorComponentAndContext ws_execId2(NDefinition def, String commandName, String[] appArgs,
                                                   List<String> executorOptions,
                                                   List<String> workspaceOptions, Map<String, String> env, NPath dir, boolean failFast, boolean temporary,
                                                   NExecInput in,
                                                   NExecOutput out,
                                                   NExecOutput err,
                                                   NExecutionType executionType,
                                                   NRunAs runAs
    ) {
        //TODO ! one of the sessions needs to be removed!
        NSession session = NSession.of();
        NWorkspaceSecurityManager.of().checkAllowed(NConstants.Permissions.EXEC, commandName);
        if (def != null && def.getContent().isPresent()) {
            NDescriptor descriptor = def.getDescriptor();
            if (!descriptor.isExecutable()) {
//                session.getTerminal().getErr().println(nutToRun.getId()+" is not executable... will perform extra checks.");
//                throw new NutsNotExecutableException(descriptor.getId());
            }
            NArtifactCall executorCall = descriptor.getExecutor();
            NExecutorComponent execComponent = null;

            List<String> executorArgs = new ArrayList<>();

            if (executorCall != null) {
                NId eid = executorCall.getId();
                if (eid != null) {
                    //process special executors
                    if (eid.getGroupId() == null) {
                        if (eid.getArtifactId().equals("nuts")) {
                            eid = eid.builder().setGroupId("net.thevpc.nuts").build();
                        } else if (eid.getArtifactId().equals("nsh")) {
                            eid = eid.builder().setGroupId("net.thevpc.nsh").build();
                        }
                    }
                    if (eid.getGroupId() != null) {
                        //nutsDefinition
                        NStream<NDefinition> q = NSearchCmd.of().addId(eid).setLatest(true)
                                .setDistinct(true)
                                .getResultDefinitions();
                        NDefinition[] availableExecutors = q.stream().limit(2).toArray(NDefinition[]::new);
                        if (availableExecutors.length > 1) {
                            throw new NTooManyElementsException(NMsg.ofC("too many results for executor %s", eid));
                        } else if (availableExecutors.length == 1) {
                            execComponent = new ArtifactExecutorComponent(availableExecutors[0].getId());
                        } else {
                            // availableExecutors.length=0;
                            throw new NNotFoundException(eid, NMsg.ofC("executor not found %s", eid));
                        }
                    }
                }
            }
            if (execComponent == null) {
                execComponent = NExtensions.of().createComponent(NExecutorComponent.class, def).get();
            }
            if (executorCall != null) {
                for (String argument : executorCall.getArguments()) {
                    executorArgs.add(StringPlaceHolderParser.replaceDollarPlaceHolders(argument,
                            def, NExecutionContextUtils.DEFINITION_PLACEHOLDER
                    ));
                }
            }
            NCmdLineUtils.OptionsAndArgs optionsAndArgs = NCmdLineUtils.parseOptionsFirst(executorArgs.toArray(new String[0]));

            executorArgs.clear();
            executorArgs.addAll(Arrays.asList(optionsAndArgs.getOptions()));
            executorArgs.addAll(executorOptions);
            executorArgs.addAll(Arrays.asList(optionsAndArgs.getArgs()));

            NExecutionContextBuilder ecb = NWorkspaceExt.of().createExecutionContext();
            NExecutionContext executionContext = ecb
                    .setDefinition(def)
                    .setArguments(appArgs)
                    .setExecutorOptions(executorArgs.toArray(new String[0]))
                    .setWorkspaceOptions(workspaceOptions)
                    .setEnv(env)
                    .setDirectory(dir)
                    .setFailFast(failFast)
                    .setTemporary(temporary)
                    .setExecutionType(executionType)
                    .setRunAs(runAs)
                    .setCommandName(commandName)
                    .setSleepMillis(getSleepMillis())
                    .setIn(in)
                    .setOut(out)
                    .setErr(err)
                    .setDry(session.isDry())
                    .build();
            return new NExecutorComponentAndContext(execComponent, executionContext);
        }
        throw new NNotFoundException(def == null ? null : def.getId());
    }

    enum CmdKind {
        PATH,
        ID,
        KEYWORD,
    }

    public static CharacterizedExecFile characterizeForExec(NInputSource contentFile, List<String> execOptions) {
        String classifier = null;//TODO how to get classifier?
        CharacterizedExecFile c = new CharacterizedExecFile();
        try {
            c.setStreamOrPath(contentFile);
            c.setContentFile(CoreIOUtils.toPathInputSource(contentFile, c.getTemps(), true));
            Path fileSource = c.getContentFile();
            if (!Files.exists(fileSource)) {
                throw new NIllegalArgumentException(NMsg.ofC("file does not exists %s", fileSource));
            }
            if (Files.isDirectory(fileSource)) {
                Path ext = fileSource.resolve(NConstants.Files.DESCRIPTOR_FILE_NAME);
                if (Files.exists(ext)) {
                    c.setDescriptor(NDescriptorParser.of().parse(ext).get());
                } else {
                    c.setDescriptor(NDescriptorContentResolver.resolveNutsDescriptorFromFileContent(c.getContentFile(), execOptions));
                }
                if (c.getDescriptor() != null) {
                    if ("zip".equals(c.getDescriptor().getPackaging())) {
                        Path zipFilePath = NPath.of(fileSource + ".zip")
                                .toAbsolute().toPath().get();
                        ZipUtils.zip(fileSource.toString(), new ZipOptions(), zipFilePath.toString());
                        c.setContentFile(zipFilePath);
                        c.addTemp(zipFilePath);
                    } else {
                        throw new NIllegalArgumentException(NMsg.ofPlain("invalid nuts folder source. expected 'zip' ext in descriptor"));
                    }
                }
            } else if (Files.isRegularFile(fileSource)) {
                if (c.getContentFile().getFileName().toString().endsWith(NConstants.Files.DESCRIPTOR_FILE_NAME)) {
                    try (InputStream in = Files.newInputStream(c.getContentFile())) {
                        c.setDescriptor(NDescriptorParser.of().parse(in).get());
                    }
                    c.setContentFile(null);
                    if (c.getStreamOrPath() instanceof NPath && ((NPath) c.getStreamOrPath()).isURL()) {
                        URLBuilder ub = new URLBuilder(((NPath) c.getStreamOrPath()).toURL().toString());
                        try {
                            c.setContentFile(CoreIOUtils.toPathInputSource(
                                    NPath.of(ub.resolveSibling(NWorkspace.of().getDefaultIdFilename(c.getDescriptor().getId())).toURL()),
                                    c.getTemps(), true));
                        } catch (Exception ex) {
                            //TODO FIX ME
                            ex.printStackTrace();
                        }
                    }
                    if (c.getContentFile() == null) {
                        for (NIdLocation location0 : c.getDescriptor().getLocations()) {
                            if (CoreFilterUtils.acceptClassifier(location0, classifier)) {
                                String location = location0.getUrl();
                                if (NPath.of(location).isHttp()) {
                                    try {
                                        c.setContentFile(CoreIOUtils.toPathInputSource(
                                                NPath.of(CoreIOUtils.urlOf(location)),
                                                c.getTemps(), true));
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                } else {
                                    URLBuilder ub = new URLBuilder(((NPath) c.getStreamOrPath()).toURL().toString());
                                    try {
                                        c.setContentFile(CoreIOUtils.toPathInputSource(
                                                NPath.of(ub.resolveSibling(NWorkspace.of().getDefaultIdFilename(c.getDescriptor().getId())).toURL()),
                                                c.getTemps(), true));
                                    } catch (Exception ex) {
                                        //TODO add log here
                                        ex.printStackTrace();
                                    }
                                }
                                if (c.getContentFile() == null) {
                                    break;
                                }
                            }
                        }
                    }
                    if (c.getContentFile() == null) {
                        throw new NIllegalArgumentException(NMsg.ofC("unable to locale package for %s", c.getStreamOrPath()));
                    }
                } else {
                    c.setDescriptor(NDescriptorContentResolver.resolveNutsDescriptorFromFileContent(c.getContentFile(), execOptions));
                    if (c.getDescriptor() == null) {
                        CoreDigestHelper d = new CoreDigestHelper();
                        d.append(c.getContentFile());
                        String artifactId = d.getDigest();
                        c.setDescriptor(new DefaultNDescriptorBuilder()
                                .setId("temp:" + artifactId + "#1.0")
                                .setPackaging(NIOUtils.getFileExtension(contentFile.getMetaData().getName().orElse("")))
                                .build());
                    }
                }
            } else {
                throw new NIllegalArgumentException(NMsg.ofC("path does not denote a valid file or folder %s", c.getStreamOrPath()));
            }
        } catch (IOException | UncheckedIOException ex) {
            throw new NIOException(ex);
        }
        return c;
    }

    private RemoteInfo0 resolveRemoteInfo0() {
        String target = getConnexionString();
        if (!NBlankable.isBlank(target)) {
            NConnexionString connexionString = NConnexionString.of(target);
            if ("ssh".equals(connexionString.getProtocol())) {
                NExtensions.of()
                        .loadExtension(NId.get("net.thevpc.nuts.ext:next-ssh").get());
            }
            if ("nagent".equals(connexionString.getProtocol())) {
                NExtensions.of()
                        .loadExtension(NId.get("com.cts.nuts.enterprise:next-agent").get());
            }
            RemoteInfo0 ii = new RemoteInfo0();
            ii.commExec = NExtensions.of().createComponent(NExecCmdExtension.class, connexionString)
                    .orElseThrow(() -> new NIllegalArgumentException(NMsg.ofC("invalid execution target string : %s", target)));
            ii.in0 = CoreIOUtils.validateIn(in);
            ii.out0 = CoreIOUtils.validateOut(out);
            ii.err0 = CoreIOUtils.validateOut(err);
            return ii;
        }
        return null;
    }

    private static class RemoteInfo0 {
        NExecCmdExtension commExec;
        NExecInput in0;
        NExecOutput out0;
        NExecOutput err0;
    }
}
