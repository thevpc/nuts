package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NSessionTerminal;
import net.thevpc.nuts.runtime.standalone.app.cmdline.NCommandLineUtils;
import net.thevpc.nuts.runtime.standalone.executor.NExecutionContextUtils;
import net.thevpc.nuts.runtime.standalone.executor.system.NSysExecUtils;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NExecutableInformationExt;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NExecutionContextBuilder;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.executor.ArtifactExecutorComponent;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.deploy.DefaultNDeployInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.fetch.DefaultNFetchInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.help.DefaultNHelpInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.info.DefaultNInfoInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.install.DefaultNInstallInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.install.DefaultNReinstallInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.license.DefaultNLicenseInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.prepare.DefaultNPrepareInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.push.DefaultNPushInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.search.DefaultNSearchInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.alias.DefaultNAliasExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.DefaultNSettingsInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.undeploy.DefaultNUndeployInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.uninstall.DefaultNUninstallInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.update.DefaultNCheckUpdatesInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.update.DefaultNUpdateInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.version.DefaultNVersionInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.welcome.DefaultNWelcomeInternalExecutable;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.which.DefaultNWhichInternalExecutable;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringPlaceHolderParser;
import net.thevpc.nuts.spi.NExecutorComponent;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NStream;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * type: Command Class
 *
 * @author thevpc
 */
public class DefaultNExecCommand extends AbstractNExecCommand {

    public DefaultNExecCommand(NSession session) {
        super(session);
    }


    @Override
    public NExecutableInformation which() {
        checkSession();
        NSession traceSession = getSession();
        NSession execSession = traceSession.copy();
        NSessionTerminal terminal = NSessionTerminal.of(traceSession.getTerminal(), execSession);
        if (this.in != null) {
            terminal.setIn(this.in);
        }
        if (this.out != null) {
            terminal.setOut(this.out);
        }
        if (isRedirectErrorStream()) {
            if (this.out != null) {
                terminal.setErr(this.out);
            } else {
                terminal.setErr(traceSession.getTerminal().out());
            }
        }
        terminal.out().flush();
        terminal.err().flush();
        NExecutableInformationExt exec = null;
        execSession.setTerminal(terminal);
        NExecutionType executionType = this.getExecutionType();
        NRunAs runAs = this.getRunAs();
        if (executionType == null) {
            executionType = session.getExecutionType();
        }
        if (executionType == null) {
            executionType = NExecutionType.SPAWN;
        }
        switch (executionType) {
            case OPEN: {
                NAssert.requireNonNull(commandDefinition, "artifact definition", session);
                NAssert.requireNonBlank(command, "command", session);
                String[] ts = command.toArray(new String[0]);
                exec = new DefaultNOpenExecutable(ts, getExecutorOptions().toArray(new String[0]), traceSession, execSession, this);
                break;
            }
            case SYSTEM: {
                NExecutionType finalExecutionType = executionType;
                NAssert.requireNull(commandDefinition, () -> NMsg.ofC("unable to run artifact as %s cmd", finalExecutionType), session);
                NAssert.requireNonBlank(command, "command", session);
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
                        traceSession,
                        execSession,
                        this
                );
                break;
            }
            case SPAWN:
            case EMBEDDED: {
                if (commandDefinition != null) {
                    String[] ts = command == null ? new String[0] : command.toArray(new String[0]);
                    return ws_execDef(commandDefinition, commandDefinition.getId().getLongName(), ts, getExecutorOptions(), workspaceOptions, env, directory, failFast,
                            executionType, runAs, traceSession, execSession);
                } else {
                    NAssert.requireNonBlank(command, "command", session);
                    String[] ts = command.toArray(new String[0]);
                    exec = execEmbeddedOrExternal(ts, getExecutorOptions(), getWorkspaceOptions(), traceSession, execSession);
                }
                break;
            }
            default: {
                throw new NUnsupportedArgumentException(getSession(), NMsg.ofC("invalid execution type %s", executionType));
            }
        }
        return exec;
    }

    @Override
    public NExecCommand run() {
        checkSession();
        NExecutableInformationExt exec = (NExecutableInformationExt) which();
        executed = true;
        try {
            exec.execute();
        } catch (NExecutionException ex) {
            String p = getExtraErrorMessage();
            if (p != null) {
                result = new NExecutionException(getSession(),
                        NMsg.ofC("execution failed with code %s and message : %s", ex.getExitCode(), p),
                        ex, ex.getExitCode());
            } else {
                result = ex;
            }
        } catch (Exception ex) {
            String p = getExtraErrorMessage();
            int exitCode = NExceptionWithExitCodeBase.resolveExitCode(ex).orElse(244);
            if (exitCode != 0) {
                if (!NBlankable.isBlank(p)) {
                    result = new NExecutionException(getSession(),
                            NMsg.ofC("execution of (%s) failed with code %s ; error was : %s ; notes : %s", exitCode, exec, ex, p),
                            ex, exitCode);
                } else {
                    result = new NExecutionException(getSession(),
                            NMsg.ofC("execution of (%s) failed with code %s ; error was : %s", exitCode, exec, ex),
                            ex, exitCode);
                }
            }
        }
        if (result != null && result.getExitCode() != 0 && failFast) {
            throw result;
//            checkFailFast(result.getExitCode());
        }
        return this;
    }

    private NExecutableInformationExt execEmbeddedOrExternal(String[] cmd, List<String> executorOptions, List<String> workspaceOptions, NSession prepareSession, NSession execSession) {
        NAssert.requireNonBlank(cmd, "command", session);
        String[] args = new String[cmd.length - 1];
        System.arraycopy(cmd, 1, args, 0, args.length);
        String cmdName = cmd[0];
        //resolve internal commands!
        NExecutionType executionType = getExecutionType();
        if (executionType == null) {
            executionType = session.getExecutionType();
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
            goodId = NId.of(cmdName.substring(0, cmdName.length() - 1)).orNull();
            if (goodId != null) {
                forceInstalled = true;
            }
        } else {
            goodId = NId.of(cmdName).orNull();
        }

        if (cmdName.contains("/") || cmdName.contains("\\")) {
            if (goodId != null) {
                cmdKind = CmdKind.ID;
            } else {
                cmdKind = CmdKind.PATH;
            }
        } else if (cmdName.contains(":") || cmdName.contains("#")) {
            if (goodId != null) {
                cmdKind = CmdKind.ID;
            } else {
                throw new NNotFoundException(getSession(), null, NMsg.ofC("unable to resolve id %s", cmdName));
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
                return new DefaultNArtifactPathExecutable(cmdName, args, executorOptions, workspaceOptions, executionType, runAs, prepareSession, execSession, this, isInheritSystemIO());
            }
            case ID: {
                NId idToExec = findExecId(goodId, prepareSession, forceInstalled, true);
                if (idToExec != null) {
                    return ws_execId(idToExec, cmdName, args, executorOptions, workspaceOptions, executionType, runAs, prepareSession, execSession);
                } else {
                    throw new NNotFoundException(getSession(), goodId);
                }
            }
            case KEYWORD: {
                switch (goodKw) {
                    case "update": {
                        return new DefaultNUpdateInternalExecutable(args, execSession);
                    }
                    case "check-updates": {
                        return new DefaultNCheckUpdatesInternalExecutable(args, execSession);
                    }
                    case "install": {
                        return new DefaultNInstallInternalExecutable(args, execSession);
                    }
                    case "reinstall": {
                        return new DefaultNReinstallInternalExecutable(args, execSession);
                    }
                    case "uninstall": {
                        return new DefaultNUninstallInternalExecutable(args, execSession);
                    }
                    case "deploy": {
                        return new DefaultNDeployInternalExecutable(args, execSession);
                    }
                    case "undeploy": {
                        return new DefaultNUndeployInternalExecutable(args, execSession);
                    }
                    case "push": {
                        return new DefaultNPushInternalExecutable(args, execSession);
                    }
                    case "fetch": {
                        return new DefaultNFetchInternalExecutable(args, execSession);
                    }
                    case "search": {
                        return new DefaultNSearchInternalExecutable(args, execSession);
                    }
                    case "version": {
                        return new DefaultNVersionInternalExecutable(args, execSession, this);
                    }
                    case "prepare": {
                        return new DefaultNPrepareInternalExecutable(args, execSession);
                    }
                    case "license": {
                        return new DefaultNLicenseInternalExecutable(args, execSession);
                    }
                    case "help": {
                        return new DefaultNHelpInternalExecutable(args, execSession);
                    }
                    case "welcome": {
                        return new DefaultNWelcomeInternalExecutable(args, execSession);
                    }
                    case "info": {
                        return new DefaultNInfoInternalExecutable(args, execSession);
                    }
                    case "which": {
                        return new DefaultNWhichInternalExecutable(args, execSession, this);
                    }
                    case "exec": {
                        return new DefaultNExecInternalExecutable(args, execSession, this);
                    }
                    case "settings": {
                        return new DefaultNSettingsInternalExecutable(args, execSession);
                    }
                }
                NWorkspaceCustomCommand command = null;
                command = NCustomCommandManager.of(prepareSession).findCommand(goodKw);
                if (command != null) {
                    NCommandExecOptions o = new NCommandExecOptions().setExecutorOptions(executorOptions).setDirectory(directory).setFailFast(failFast)
                            .setExecutionType(executionType).setEnv(env);
                    return new DefaultNAliasExecutable(command, o, execSession, args);
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
                            return new DefaultNSystemExecutable(cmdArr.toArray(new String[0]), executorOptions, prepareSession, execSession, this);
                        }
                        List<String> cmdArr = new ArrayList<>();
                        cmdArr.add(cmdName);
                        cmdArr.addAll(Arrays.asList(args));
                        return new DefaultUnknownExecutable(cmdArr.toArray(new String[0]), execSession);
                    }
                    return ws_execId(idToExec, cmdName, args, executorOptions, workspaceOptions, executionType, runAs, prepareSession, execSession);
                }
            }
        }
        throw new NNotFoundException(getSession(), goodId, NMsg.ofC("unable to resolve id %s", cmdName));
    }

    protected NId findExecId(NId nid, NSession traceSession, boolean forceInstalled, boolean ignoreIfUserCommand) {
        NWorkspace ws = traceSession.getWorkspace();
        if (nid == null) {
            return null;
        }
        List<NId> ff = NSearchCommand.of(traceSession).addId(nid).setOptional(false).setLatest(true).setFailFast(false)
                .setInstallStatus(NInstallStatusFilters.of(session).byDeployed(true))
                .getResultDefinitions().stream()
                .sorted(Comparator.comparing(x -> !x.getInstallInformation().get(session).isDefaultVersion())) // default first
                .map(NDefinition::getId).collect(Collectors.toList());
        if (ff.isEmpty()) {
            if (!forceInstalled) {
                if (ignoreIfUserCommand && isUserCommand(nid.toString())) {
                    return null;
                }
                //now search online
                // this helps recover from "invalid default parseVersion" issue
                if (traceSession.isPlainTrace()) {
                    traceSession.out().resetLine().println(NMsg.ofC("%s is %s, will search for it online. Type ```error CTRL^C``` to stop...",
                            nid,
                            NTexts.of(session).ofStyled("not installed", NTextStyle.error())
                    ));
                    traceSession.out().flush();
                }
                ff = NSearchCommand.of(traceSession).addId(nid).setSession(traceSession.copy().setFetchStrategy(NFetchStrategy.ONLINE))
                        .setOptional(false).setFailFast(false)
                        .setLatest(true)
                        //                        .configure(true,"--trace-monitor")
                        .getResultIds().toList();
            }
        }
        if (ff.isEmpty()) {
            return null;
        } else {
            List<NVersion> versions = ff.stream().map(NId::getVersion).distinct().collect(Collectors.toList());
            if (versions.size() > 1) {
                throw new NTooManyElementsException(getSession(),
                        NMsg.ofC("%s can be resolved to all (%d) of %s", nid, ff.size(), ff)
                );
            }
        }
        return ff.get(0);
    }

    public boolean isUserCommand(String s) {
        checkSession();
        NSession session = getSession();
        String p = System.getenv().get("PATH");
        if (p != null) {
            char r = File.pathSeparatorChar;
            for (String z : p.split("" + r)) {
                Path t = Paths.get(z);
                switch (NEnvs.of(session).getOsFamily()) {
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
                                                  List<String> workspaceOptions, NExecutionType executionType, NRunAs runAs,
                                                  NSession session, NSession execSession) {
        NDefinition def = NFetchCommand.of(session).setId(goodId)
                .setDependencies(true)
                .setFailFast(true)
                .setEffective(true)
                .setContent(true)
                //
                .setOptional(false)
                .addScope(NDependencyScopePattern.RUN)
                .setDependencyFilter(NDependencyFilters.of(session).byRunnable())
                //
                .getResultDefinition();
        return ws_execDef(def, commandName, appArgs, executorOptions, this.workspaceOptions, env, directory, failFast, executionType, runAs, session, execSession);
    }

    protected NExecutableInformationExt ws_execDef(NDefinition def, String commandName, String[] appArgs, List<String> executorOptions, List<String> workspaceOptions, Map<String, String> env, String dir, boolean failFast, NExecutionType executionType, NRunAs runAs, NSession traceSession, NSession execSession) {
        return new DefaultNArtifactExecutable(def, commandName, appArgs, executorOptions, workspaceOptions, env, dir, failFast, traceSession, execSession, executionType, runAs, this);
    }

    public void ws_execId(NDefinition def, String commandName, String[] appArgs,
                          List<String> executorOptions,
                          List<String> workspaceOptions, Map<String, String> env, String dir, boolean failFast, boolean temporary,
                          NSession session,
                          NSession execSession,
                          NExecutionType executionType,
                          NRunAs runAs
    ) {
        //TODO ! one of the sessions needs to be removed!
        NSessionUtils.checkSession(ws, this.session);
        checkSession();
        NWorkspace ws = getSession().getWorkspace();
        NWorkspaceSecurityManager.of(this.session).checkAllowed(NConstants.Permissions.EXEC, commandName);
        NSessionUtils.checkSession(ws, execSession);
        NSessionUtils.checkSession(ws, session);
        if (def != null && def.getContent().isPresent()) {
            NDescriptor descriptor = def.getDescriptor();
            if (!descriptor.isExecutable()) {
//                session.getTerminal().getErr().println(nutToRun.getId()+" is not executable... will perform extra checks.");
//                throw new NutsNotExecutableException(descriptor.getId());
            }
            NArtifactCall executorCall = descriptor.getExecutor();
            NExecutorComponent execComponent = null;

            List<String> executorArgs = new ArrayList<>();
            Map<String, String> execProps = null;

            if (executorCall != null) {
                NId eid = executorCall.getId();
                if (eid != null) {
                    //process special executors
                    if (eid.getGroupId() == null) {
                        if (eid.getArtifactId().equals("nuts")) {
                            eid = eid.builder().setGroupId("net.thevpc.nuts").build();
                        } else if (eid.getArtifactId().equals("nsh")) {
                            eid = eid.builder().setGroupId("net.thevpc.nuts.toolbox").build();
                        }
                    }
                    if (eid.getGroupId() != null) {
                        //nutsDefinition
                        NStream<NDefinition> q = NSearchCommand.of(getSession()).addId(eid).setLatest(true)
                                .setDistinct(true)
                                .getResultDefinitions();
                        NDefinition[] availableExecutors = q.stream().limit(2).toArray(NDefinition[]::new);
                        if (availableExecutors.length > 1) {
                            throw new NTooManyElementsException(this.session, NMsg.ofC("too many results for executor %s", eid));
                        } else if (availableExecutors.length == 1) {
                            execComponent = new ArtifactExecutorComponent(availableExecutors[0].getId(), this.session);
                        } else {
                            // availableExecutors.length=0;
                            throw new NNotFoundException(this.session, eid, NMsg.ofC("executor not found %s", eid));
                        }
                    }
                }
            }
            if (execComponent == null) {
                execComponent = getSession().extensions().createSupported(NExecutorComponent.class, true, def);
            }
            if (executorCall != null) {
                for (String argument : executorCall.getArguments()) {
                    executorArgs.add(StringPlaceHolderParser.replaceDollarPlaceHolders(argument,
                            def, session, NExecutionContextUtils.DEFINITION_PLACEHOLDER
                    ));
                }
                execProps = executorCall.getProperties();
            }
            NCommandLineUtils.OptionsAndArgs optionsAndArgs = NCommandLineUtils.parseOptionsFirst(executorArgs.toArray(new String[0]));

            executorArgs.clear();
            executorArgs.addAll(Arrays.asList(optionsAndArgs.getOptions()));
            executorArgs.addAll(executorOptions);
            executorArgs.addAll(Arrays.asList(optionsAndArgs.getArgs()));

            NExecutionContextBuilder ecb = NWorkspaceExt.of(ws).createExecutionContext();
            NExecutionContext executionContext = ecb
                    .setDefinition(def)
                    .setArguments(appArgs)
                    .setExecutorOptions(executorArgs.toArray(new String[0]))
                    .setWorkspaceOptions(workspaceOptions)
                    .setEnv(env)
                    .setExecutorProperties(execProps)
                    .setCwd(dir)
                    .setWorkspace(session.getWorkspace())
                    .setSession(session)
                    .setExecSession(execSession)
                    .setFailFast(failFast)
                    .setTemporary(temporary)
                    .setExecutionType(executionType)
                    .setRunAs(runAs)
                    .setCommandName(commandName)
                    .setSleepMillis(getSleepMillis())
                    .setInheritSystemIO(isInheritSystemIO())
                    .setRedirectOutputFile(getRedirectOutputFile())
                    .setRedirectInputFile(getRedirectInputFile())
                    .build();
            execComponent.exec(executionContext);
            return;

        }
        throw new NNotFoundException(getSession(), def == null ? null : def.getId());
    }

    enum CmdKind {
        PATH,
        ID,
        KEYWORD,
    }
}
