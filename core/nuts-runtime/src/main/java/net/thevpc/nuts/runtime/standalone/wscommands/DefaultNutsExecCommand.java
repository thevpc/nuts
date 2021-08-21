package net.thevpc.nuts.runtime.standalone.wscommands;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.commands.ws.NutsExecutableInformationExt;
import net.thevpc.nuts.runtime.core.commands.ws.NutsExecutionContextBuilder;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.core.util.CoreNutsDependencyUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.DefaultNutsWorkspace;
import net.thevpc.nuts.runtime.standalone.executors.ArtifactExecutorComponent;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.DefaultNutsSettingsInternalExecutable;

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
public class DefaultNutsExecCommand extends AbstractNutsExecCommand {

    public DefaultNutsExecCommand(DefaultNutsWorkspace ws) {
        super(ws);
    }

    @Override
    public NutsExecutableInformation which() {
        checkSession();
        NutsSession traceSession = getSession();
        NutsSession execSession = traceSession.copy();
        NutsSessionTerminal terminal = execSession.getWorkspace().term().createTerminal(traceSession.getTerminal());
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
        String[] ts = command.toArray(new String[0]);
        if (ts.length == 0) {
            throw new NutsIllegalArgumentException(traceSession, NutsMessage.plain("missing command"));
        }
        NutsExecutableInformationExt exec = null;
        execSession.setTerminal(terminal);
        NutsExecutionType executionType = this.getExecutionType();
        NutsRunAs runAs = this.getRunAs();
        if (executionType == null) {
            executionType = session.getExecutionType();
        }
        if (executionType == null) {
            executionType = NutsExecutionType.SPAWN;
        }
        switch (executionType) {
            case OPEN: {
                if (commandDefinition != null) {
                    throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("unable to run open artifact"));
                }
                exec = new DefaultNutsOpenExecutable(ts, getExecutorOptions(), traceSession, execSession, this);
                break;
            }
            case SYSTEM: {
                if (commandDefinition != null) {
                    throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("unable to run artifact as " + executionType + "cmd"));
                }
                List<String> tsl = new ArrayList<>(Arrays.asList(ts));
                if (CoreStringUtils.firstIndexOf(ts[0], new char[]{'/', '\\'}) < 0) {
                    Path p = CoreIOUtils.sysWhich(ts[0]);
                    if (p != null) {
                        tsl.set(0, p.toString());
                    }
                }
                exec = new DefaultNutsSystemExecutable(tsl.toArray(new String[0]), getExecutorOptions(),
                        traceSession,
                        execSession,
                        this
                );
                break;
            }
            case SPAWN:
            case EMBEDDED: {
                if (commandDefinition != null) {
                    return ws_execDef(commandDefinition, commandDefinition.getId().getLongName(), ts, getExecutorOptions(), env, directory, failFast,
                            executionType, runAs, traceSession, execSession);
                } else {
                    exec = execEmbeddedOrExternal(ts, getExecutorOptions(), traceSession, execSession);
                }
                break;
            }
            default: {
                throw new NutsUnsupportedArgumentException(getSession(), NutsMessage.cstyle("invalid execution type %s", executionType));
            }
        }
        return exec;
    }

    @Override
    public NutsExecCommand run() {
        checkSession();
        NutsExecutableInformationExt exec = (NutsExecutableInformationExt) which();
        executed = true;
        try {
            if (dry) {
                exec.dryExecute();
            } else {
                exec.execute();
            }
        } catch (NutsExecutionException ex) {
            String p = getExtraErrorMessage();
            if (p != null) {
                result = new NutsExecutionException(getSession(),
                        NutsMessage.cstyle("execution failed with code %s and message : %s", ex.getExitCode(), p),
                        ex, ex.getExitCode());
            } else {
                result = ex;
            }
        } catch (Exception ex) {
            String p = getExtraErrorMessage();
            if (p != null) {
                result = new NutsExecutionException(getSession(),
                        NutsMessage.cstyle("execution failed with code %s and message : %s", 244, p),
                        ex, 244);
            } else {
                result = new NutsExecutionException(getSession(), ex, 244);
            }
        }
        if (result != null && result.getExitCode() != 0 && failFast) {
            throw result;
//            checkFailFast(result.getExitCode());
        }
        return this;
    }

    private NutsExecutableInformationExt execEmbeddedOrExternal(String[] cmd, String[] executorOptions, NutsSession prepareSession, NutsSession execSession) {
        if (cmd == null || cmd.length == 0) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("missing command"));
        }
        String[] args = new String[cmd.length - 1];
        System.arraycopy(cmd, 1, args, 0, args.length);
        String cmdName = cmd[0];
        //resolve internal commands!
        NutsExecutionType executionType = getExecutionType();
        if (executionType == null) {
            executionType = session.getExecutionType();
        }
        if (executionType == null) {
            executionType = NutsExecutionType.SPAWN;
        }
        NutsRunAs runAs = getRunAs();
        CmdKind cmdKind = null;
        NutsId goodId = null;
        String goodKw = null;
        boolean forceInstalled = false;
        if (cmdName.endsWith("!")) {
            goodId = session.getWorkspace().id().parser().setLenient(true).parse(cmdName.substring(0, cmdName.length() - 1));
            if (goodId != null) {
                forceInstalled = true;
            }
        } else {
            goodId = session.getWorkspace().id().parser().setLenient(true).parse(cmdName);
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
                throw new NutsNotFoundException(getSession(), null, NutsMessage.cstyle("unable to resolve id %", cmdName));
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
                return new DefaultNutsArtifactPathExecutable(cmdName, args, executorOptions, executionType, runAs, prepareSession, execSession, this, isInheritSystemIO());
            }
            case ID: {
                NutsId idToExec = findExecId(goodId, prepareSession, forceInstalled, true);
                if (idToExec != null) {
                    return ws_execId(idToExec, cmdName, args, executorOptions, executionType, runAs, prepareSession, execSession);
                } else {
                    throw new NutsNotFoundException(getSession(), goodId);
                }
            }
            case KEYWORD: {
                switch (goodKw) {
                    case "update": {
                        return new DefaultNutsUpdateInternalExecutable(args, execSession);
                    }
                    case "check-updates": {
                        return new DefaultNutsCheckUpdatesInternalExecutable(args, execSession);
                    }
                    case "install": {
                        return new DefaultNutsInstallInternalExecutable(args, execSession);
                    }
                    case "reinstall": {
                        return new DefaultNutsReinstallInternalExecutable(args, execSession);
                    }
                    case "uninstall": {
                        return new DefaultNutsUninstallInternalExecutable(args, execSession);
                    }
                    case "deploy": {
                        return new DefaultNutsDeployInternalExecutable(args, execSession);
                    }
                    case "undeploy": {
                        return new DefaultNutsUndeployInternalExecutable(args, execSession);
                    }
                    case "push": {
                        return new DefaultNutsPushInternalExecutable(args, execSession);
                    }
                    case "fetch": {
                        return new DefaultNutsFetchInternalExecutable(args, execSession);
                    }
                    case "search": {
                        return new DefaultNutsSearchInternalExecutable(args, execSession);
                    }
                    case "version": {
                        return new DefaultNutsVersionInternalExecutable(args, execSession, this);
                    }
                    case "license": {
                        return new DefaultNutsLicenseInternalExecutable(args, execSession);
                    }
                    case "help": {
                        return new DefaultNutsHelpInternalExecutable(args, execSession);
                    }
                    case "welcome": {
                        return new DefaultNutsWelcomeInternalExecutable(args, execSession);
                    }
                    case "info": {
                        return new DefaultNutsInfoInternalExecutable(args, execSession);
                    }
                    case "which": {
                        return new DefaultNutsWhichInternalExecutable(args, execSession, this);
                    }
                    case "exec": {
                        return new DefaultNutsExecInternalExecutable(args, execSession, this);
                    }
                    case "settings": {
                        return new DefaultNutsSettingsInternalExecutable(args, execSession);
                    }
                }
                NutsWorkspaceCustomCommand command = null;
                command = prepareSession.getWorkspace().commands().findCommand(goodKw);
                if (command != null) {
                    NutsCommandExecOptions o = new NutsCommandExecOptions().setExecutorOptions(executorOptions).setDirectory(directory).setFailFast(failFast)
                            .setExecutionType(executionType).setEnv(env);
                    return new DefaultNutsAliasExecutable(command, o, execSession, args);
                } else {
                    NutsId idToExec = null;
                    if (goodId != null) {
                        idToExec = findExecId(goodId, prepareSession, forceInstalled, true);
                    }
                    if (idToExec == null) {
                        Path sw = CoreIOUtils.sysWhich(cmdName);
                        if (sw != null) {
                            List<String> cmdArr = new ArrayList<>();
                            cmdArr.add(sw.toString());
                            cmdArr.addAll(Arrays.asList(args));
                            return new DefaultNutsSystemExecutable(cmdArr.toArray(new String[0]), executorOptions, prepareSession, execSession, this);
                        }
                        List<String> cmdArr = new ArrayList<>();
                        cmdArr.add(cmdName);
                        cmdArr.addAll(Arrays.asList(args));
                        return new DefaultNutsSystemExecutable(cmdArr.toArray(new String[0]), executorOptions, prepareSession, execSession, this);
                    }
                    return ws_execId(idToExec, cmdName, args, executorOptions, executionType, runAs, prepareSession, execSession);
                }
            }
        }
        throw new NutsNotFoundException(getSession(), goodId, NutsMessage.cstyle("unable to resolve id %", cmdName));
    }

    protected NutsId findExecId(NutsId nid, NutsSession traceSession, boolean forceInstalled, boolean ignoreIfUserCommand) {
        NutsWorkspace ws = traceSession.getWorkspace();
        if (nid == null) {
            return null;
        }
        NutsSession noProgressSession = traceSession.copy().setProgressOptions("none");
        List<NutsId> ff = ws.search().addId(nid).setSession(noProgressSession).setOptional(false).setLatest(true).setFailFast(false)
                .setInstallStatus(ws.filters().installStatus().byDeployed(true))
                .getResultDefinitions().stream()
                .sorted(Comparator.comparing(x -> !x.getInstallInformation().isDefaultVersion())) // default first
                .map(NutsDefinition::getId).collect(Collectors.toList());
//        if (ff.isEmpty()) {
//            //retest without checking if the parseVersion is default or not
//            // this help recovering from "invalid default parseVersion" issue
//            ff = ws.search().addId(nid).setSession(noProgressSession).setOptional(false).setLatest(true).setFailFast(false)
//                    .setInstallStatus(ws.filters().installStatus().byDeployed(true))
//                    .setSession(noProgressSession)
//                    .getResultIds().list();
//        }
        if (ff.isEmpty()) {
            if (!forceInstalled) {
                if (ignoreIfUserCommand && isUserCommand(nid.toString())) {
                    return null;
                }
                //now search online
                // this helps recovering from "invalid default parseVersion" issue
                if (traceSession.isPlainTrace()) {
                    traceSession.out().resetLine().printf("%s is %s, will search for it online. Type ```error CTRL^C``` to stop...\n",
                            nid,
                            ws.text().forStyled("not installed", NutsTextStyle.error())
                    );
                    traceSession.out().flush();
                }
                ff = ws.search().addId(nid).setSession(noProgressSession.copy().setFetchStrategy(NutsFetchStrategy.ONLINE))
                        .setOptional(false).setFailFast(false)
                        .setLatest(true)
                        //                        .configure(true,"--trace-monitor")
                        .getResultIds().list();
            }
        }
        if (ff.isEmpty()) {
            return null;
        } else {
            List<NutsVersion> versions = ff.stream().map(x -> x.getVersion()).distinct().collect(Collectors.toList());
            if (versions.size() > 1) {
                throw new NutsTooManyElementsException(getSession(),
                        NutsMessage.cstyle("%s can be resolved to all (%d) of %s", nid, ff.size(), ff)
                );
            }
        }
        return ff.get(0);
    }

    public boolean isUserCommand(String s) {
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        String p = System.getenv().get("PATH");
        if (p != null) {
            char r = File.pathSeparatorChar;
            for (String z : p.split("" + r)) {
                Path t = Paths.get(z);
                switch (ws.env().getOsFamily()) {
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

    protected NutsExecutableInformationExt ws_execId(NutsId goodId, String commandName, String[] appArgs, String[] executorOptions,
                                                     NutsExecutionType executionType, NutsRunAs runAs,
                                                     NutsSession traceSession, NutsSession execSession) {
        NutsSession noProgressSession = traceSession.copy().setProgressOptions("none");
        NutsDefinition def = ws.fetch().setId(goodId)
                .setSession(noProgressSession)
                .setDependencies(true)
                .setFailFast(true)
                .setEffective(true)
                .setContent(true)
                //
                .setOptional(false)
                .addScope(NutsDependencyScopePattern.RUN)
                .setDependencyFilter(CoreNutsDependencyUtils.createJavaRunDependencyFilter(traceSession))
                //
                .getResultDefinition();
        return ws_execDef(def, commandName, appArgs, executorOptions, env, directory, failFast, executionType, runAs, traceSession, execSession);
    }

    protected NutsExecutableInformationExt ws_execDef(NutsDefinition def, String commandName, String[] appArgs, String[] executorOptions, Map<String, String> env, String dir, boolean failFast, NutsExecutionType executionType, NutsRunAs runAs, NutsSession traceSession, NutsSession execSession) {
        return new DefaultNutsArtifactExecutable(def, commandName, appArgs, executorOptions, env, dir, failFast, traceSession, execSession, executionType, runAs, this);
    }

    public void ws_execId(NutsDefinition def, String commandName, String[] appArgs, String[] executorOptions, Map<String, String> env, String dir, boolean failFast, boolean temporary,
                          NutsSession traceSession,
                          NutsSession execSession,
                          NutsExecutionType executionType,
                          NutsRunAs runAs,
                          boolean dry
    ) {
        //TODO ! one of the sessions needs to be removed!
        NutsWorkspaceUtils.checkSession(ws, session);
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        ws.security().setSession(session).checkAllowed(NutsConstants.Permissions.EXEC, commandName);
        NutsWorkspaceUtils.checkSession(ws, execSession);
        NutsWorkspaceUtils.checkSession(ws, traceSession);
        if (def != null && def.getPath() != null) {
            NutsDescriptor descriptor = def.getDescriptor();
            if (!descriptor.isExecutable()) {
//                session.getTerminal().getErr().println(nutToRun.getId()+" is not executable... will perform extra checks.");
//                throw new NutsNotExecutableException(descriptor.getId());
            }
            NutsArtifactCall executorCall = descriptor.getExecutor();
            NutsExecutorComponent execComponent = null;
            List<String> executorArgs = new ArrayList<>();
            Map<String, String> execProps = null;

            if (executorCall != null) {
                NutsId eid = executorCall.getId();
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
                        NutsResultList<NutsDefinition> q = getSession().getWorkspace().search().addId(eid).setLatest(true)
                                .getResultDefinitions();
                        NutsDefinition[] availableExecutors = q.stream().limit(2).toArray(NutsDefinition[]::new);
                        if (availableExecutors.length > 1) {
                            throw new NutsTooManyElementsException(session, NutsMessage.cstyle("too many results for executor %s", eid));
                        } else if (availableExecutors.length == 1) {
                            execComponent = new ArtifactExecutorComponent(availableExecutors[0].getId(), session);
                        } else {
                            // availableExecutors.length=0;
                            throw new NutsNotFoundException(session, eid, NutsMessage.cstyle("executor not found %s", eid));
                        }
                    }
                }
            }
            if (execComponent == null) {
                execComponent = getSession().getWorkspace().extensions().createSupported(NutsExecutorComponent.class, def);
                if (execComponent == null) {
                    throw new NutsNotFoundException(getSession(), def.getId());
                }
            }
            if (executorCall != null) {
                executorArgs.addAll(Arrays.asList(executorCall.getArguments()));
                execProps = executorCall.getProperties();
            }
            executorArgs.addAll(Arrays.asList(executorOptions));
            NutsExecutionContextBuilder ecb = NutsWorkspaceExt.of(ws).createExecutionContext();
            NutsExecutionContext executionContext = ecb
                    .setDefinition(def)
                    .setArguments(appArgs)
                    .setExecutorArguments(executorArgs.toArray(new String[0]))
                    .setEnv(env)
                    .setExecutorProperties(execProps)
                    .setCwd(dir)
                    .setWorkspace(traceSession.getWorkspace())
                    .setTraceSession(traceSession)
                    .setExecSession(execSession)
                    .setFailFast(failFast)
                    .setTemporary(temporary)
                    .setExecutionType(executionType)
                    .setRunAs(runAs)
                    .setCommandName(commandName)
                    .setSleepMillis(getSleepMillis())
                    .setInheritSystemIO(isInheritSystemIO())
                    .setRedirectOuputFile(getRedirectOuputFile())
                    .setRedirectInpuFile(getRedirectInpuFile())
                    .build();
            if (dry) {
                execComponent.dryExec(executionContext);
            } else {
                execComponent.exec(executionContext);
            }
            return;

        }
        throw new NutsNotFoundException(getSession(), def == null ? null : def.getId());
    }

    enum CmdKind {
        PATH,
        ID,
        KEYWORD,
    }
}
