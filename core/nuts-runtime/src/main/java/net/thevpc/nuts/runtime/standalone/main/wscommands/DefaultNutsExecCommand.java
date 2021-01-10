package net.thevpc.nuts.runtime.standalone.main.wscommands;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.commands.ws.NutsExecutableInformationExt;
import net.thevpc.nuts.runtime.core.filters.installstatus.NutsInstallStatusFilter2;
import net.thevpc.nuts.runtime.standalone.main.DefaultNutsWorkspace;
import net.thevpc.nuts.runtime.standalone.main.commands.*;
import net.thevpc.nuts.runtime.core.commands.ws.DefaultNutsExecutionContext;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.main.executors.CustomNutsExecutorComponent;
import net.thevpc.nuts.runtime.core.terminals.DefaultNutsSessionTerminal;
import net.thevpc.nuts.runtime.standalone.wscommands.AbstractNutsExecCommand;
import net.thevpc.nuts.NutsExecutorComponent;

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
        DefaultNutsSessionTerminal terminal = new DefaultNutsSessionTerminal();
        NutsWorkspaceUtils.setSession(terminal,getValidWorkspaceSession());
        NutsSession traceSession = getValidWorkspaceSession();
        terminal.setParent(traceSession.getTerminal());
        terminal.setOutMode(traceSession.getTerminal().getOutMode());
        terminal.setErrMode(traceSession.getTerminal().getErrMode());
        if (isGrabOutputString()) {
            terminal.setOutMode(NutsTerminalMode.INHERITED);
        }
        if (isGrabErrorString()) {
            terminal.setErrMode(NutsTerminalMode.INHERITED);
        }
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
            terminal.setErrMode(terminal.getOutMode());
        }
        terminal.out().flush();
        terminal.err().flush();
        String[] ts = command.toArray(new String[0]);
        NutsExecutableInformationExt exec = null;
        NutsSession execSession = traceSession.copy();
        execSession.setTerminal(terminal);
        NutsExecutionType executionType = this.getExecutionType();
        if(executionType==null){
            executionType=session.getExecutionType();
        }
        if(executionType==null){
            executionType=NutsExecutionType.SPAWN;
        }
        switch (executionType) {
            case USER_CMD: {
                if (commandDefinition != null) {
                    throw new NutsIllegalArgumentException(ws, "unable to run nuts as user-cmd");
                }
                exec = new DefaultNutsSystemExecutable(ts, getExecutorOptions(),
                        traceSession,
                        execSession,
                        this,
                        false
                );
                break;
            }
            case ROOT_CMD: {
                if (commandDefinition != null) {
                    throw new NutsIllegalArgumentException(ws, "unable to run nuts as root-cmd");
                }
                exec = new DefaultNutsSystemExecutable(ts, getExecutorOptions(),
                        traceSession,
                        execSession,
                        this,
                        true
                );
                break;
            }
            case SPAWN:
            case EMBEDDED: {
                if (commandDefinition != null) {
                    return ws_execDef(commandDefinition, commandDefinition.getId().getLongName(), ts, getExecutorOptions(), env, directory, failFast,
                            executionType, traceSession, execSession);
                } else {
                    exec = execEmbeddedOrExternal(ts, getExecutorOptions(), traceSession, execSession);
                }
                break;
            }
            default: {
                throw new NutsUnsupportedArgumentException(ws, "invalid executionType " + executionType);
            }
        }
        return exec;
    }

    @Override
    public NutsExecCommand run() {
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
                result = new NutsExecutionException(ws,
                        "execution failed with code " + ex.getExitCode() + " and message : " + p,
                        ex, ex.getExitCode());
            } else {
                result = ex;
            }
        } catch (Exception ex) {
            String p = getExtraErrorMessage();
            if (p != null) {
                result = new NutsExecutionException(ws,
                        "execution failed with code " + 244 + " and message : " + p,
                        ex, 244);
            } else {
                result = new NutsExecutionException(ws, ex, 244);
            }
        }
        if (result != null && result.getExitCode()!=0 && failFast) {
            throw result;
//            checkFailFast(result.getExitCode());
        }
        return this;
    }

    private NutsExecutorComponent resolveNutsExecutorComponent(NutsId nutsId) {
        for (NutsExecutorComponent nutsExecutorComponent : ws.extensions().createAll(NutsExecutorComponent.class, session)) {
            if (nutsExecutorComponent.getId().equalsShortName(nutsId)
                    || nutsExecutorComponent.getId().getArtifactId().equals(nutsId.toString())
                    || nutsExecutorComponent.getId().toString().equals("net.thevpc.nuts.exec:exec-" + nutsId.toString())) {
                return nutsExecutorComponent;
            }
        }
        return new CustomNutsExecutorComponent(nutsId);
    }

    private NutsExecutorComponent resolveNutsExecutorComponent(NutsDefinition nutsDefinition) {
        NutsExecutorComponent executorComponent = ws.extensions().createSupported(NutsExecutorComponent.class, nutsDefinition, session);
        if (executorComponent != null) {
            return executorComponent;
        }
        throw new NutsNotFoundException(ws, nutsDefinition.getId());
    }

    private NutsExecutableInformationExt execEmbeddedOrExternal(String[] cmd, String[] executorOptions, NutsSession prepareSession, NutsSession execSession) {
        if (cmd == null || cmd.length == 0) {
            throw new NutsIllegalArgumentException(ws, "missing command");
        }
        String[] args = new String[cmd.length - 1];
        System.arraycopy(cmd, 1, args, 0, args.length);
        String cmdName = cmd[0];
        //resolve internal commands!
        switch (cmdName) {
            case "update": {
                return new DefaultNutsUpdateInternalExecutable(args, execSession);
            }
            case "check-updates": {
                return new DefaultNutsCheckUpdatesInternalExecutable(args, execSession);
            }
            case "install": {
                return new DefaultNutsInstallInternalExecutable(args, execSession);
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
        }
        NutsExecutionType executionType = getExecutionType();
        if(executionType==null){
            executionType=session.getExecutionType();
        }
        if(executionType==null){
            executionType=NutsExecutionType.SPAWN;
        }
        if (cmdName.contains("/") || cmdName.contains("\\")) {
            return new DefaultNutsArtifactPathExecutable(cmdName, args, executorOptions, executionType, prepareSession, execSession, this);
        } else if (cmdName.contains(":")) {
            boolean forceInstalled = false;
            if (cmdName.endsWith("!")) {
                cmdName = cmdName.substring(0, cmdName.length() - 1);
                forceInstalled = true;
            }
            NutsId idToExec = findExecId(cmdName, prepareSession, forceInstalled,true);
            if(idToExec==null){
                throw new NutsNotFoundException(ws,cmdName);
            }
            return ws_execId(idToExec,cmdName, args, executorOptions, env, directory, failFast, executionType, prepareSession, execSession);
        } else {
            NutsWorkspaceCommandAlias command = null;
            boolean forceInstalled = false;
            if (cmdName.endsWith("!")) {
                cmdName = cmdName.substring(0, cmdName.length() - 1);
                forceInstalled = true;
            }
            command = ws.aliases().find(cmdName, prepareSession);
            if (command != null) {
                NutsCommandExecOptions o = new NutsCommandExecOptions().setExecutorOptions(executorOptions).setDirectory(directory).setFailFast(failFast)
                        .setExecutionType(executionType).setEnv(env);
                return new DefaultNutsAliasExecutable(command, o, execSession, args);
            } else {
                NutsId idToExec = findExecId(cmdName, prepareSession, forceInstalled,true);
                if(idToExec==null){
                    List<String> cmdArr=new ArrayList<>();
                    cmdArr.add(cmdName);
                    cmdArr.addAll(Arrays.asList(args));
                    return new DefaultNutsSystemExecutable(cmdArr.toArray(new String[0]) , executorOptions, prepareSession, execSession, this,false);
                }
                return ws_execId(idToExec,cmdName, args, executorOptions, env, directory, failFast, executionType, prepareSession, execSession);
            }
        }
    }

    protected NutsId findExecId(String commandName, NutsSession traceSession, boolean forceInstalled,boolean ignoreIfUserCommand) {
        NutsId nid = ws.id().parser().parse(commandName);
        if (nid == null) {
            return null;
        }
        NutsSession noProgressSession = traceSession.copy().setProgressOptions("none");
        List<NutsId> ff = ws.search().addId(nid).setSession(noProgressSession).setOptional(false).setLatest(true).setFailFast(false)
                .setDefaultVersions(true)
                //                .configure(true,"--trace-monitor")
                .setInstallStatus(ws.filters().installStatus().byDeployed())
                .getResultIds().list();
        if (ff.isEmpty()) {
            //retest without checking if the parseVersion is default or not
            // this help recovering from "invalid default parseVersion" issue
            ff = ws.search().addId(nid).setSession(noProgressSession).setOptional(false).setLatest(true).setFailFast(false)
                    .setInstallStatus(ws.filters().installStatus().byDeployed())
                    .setSession(noProgressSession)
                    .getResultIds().list();
        }
        if (ff.isEmpty()) {
            if (!forceInstalled) {
                if(ignoreIfUserCommand && isUserCommand(commandName)){
                    return null;
                }
                //now search online
                // this helps recovering from "invalid default parseVersion" issue
                if (traceSession.isPlainTrace()) {
                    traceSession.out().printf("%s is %s, will search for it online. Type ```error CTRL^C``` to stop...\n",
                            ws.formats().text().factory().styled(commandName,NutsTextNodeStyle.primary(1)),
                            ws.formats().text().factory().styled("not installed",NutsTextNodeStyle.error())
                            );
                    traceSession.out().flush();
                }
                ff = ws.search().addId(nid).setSession(noProgressSession).setOptional(false).setFailFast(false).setOnline().setLatest(true)
                        //                        .configure(true,"--trace-monitor")
                        .getResultIds().list();
            }
        }
        if (ff.isEmpty()) {
            return null;
        } else {
            List<NutsVersion> versions = ff.stream().map(x -> x.getVersion()).distinct().collect(Collectors.toList());
            if (versions.size() > 1) {
                throw new NutsTooManyElementsException(ws, nid.toString() + " can be resolved to all ("+ff.size()+") of " + ff);
            }
        }
        return ff.get(0);
    }

    public boolean isUserCommand(String s){
        String p = System.getenv().get("PATH");
        if(p!=null){
            char r = File.pathSeparatorChar;
            for (String z : p.split(""+r)) {
                Path t = Paths.get(z);
                switch (ws.env().getOsFamily()){
                    case WINDOWS:{
                        if(Files.isRegularFile(t.resolve(s))){
                            return true;
                        }
                        if(Files.isRegularFile(t.resolve(s+".exe"))){
                            return true;
                        }
                        if(Files.isRegularFile(t.resolve(s+".bat"))){
                            return true;
                        }
                        break;
                    }
                    default:{
                        Path fp = t.resolve(s);
                        if(Files.isRegularFile(fp)){
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

    protected NutsExecutableInformationExt ws_execId(NutsId goodId, String commandName, String[] appArgs, String[] executorOptions, Map<String, String> env, String dir, boolean failFast, NutsExecutionType executionType,
                                                     NutsSession traceSession, NutsSession execSession) {
        NutsSession noProgressSession = traceSession.copy().setProgressOptions("none");
        NutsDefinition def = ws.fetch().setId(goodId)
                .setSession(noProgressSession)
                .setOptional(false).setDependencies(true)
                .setFailFast(true)
                .setEffective(true)
                .setContent(true)
                .addScope(NutsDependencyScopePattern.RUN)
                .getResultDefinition();
        return ws_execDef(def, commandName, appArgs, executorOptions, env, dir, failFast, executionType, traceSession, execSession);
    }

    protected NutsExecutableInformationExt ws_execDef(NutsDefinition def, String commandName, String[] appArgs, String[] executorOptions, Map<String, String> env, String dir, boolean failFast, NutsExecutionType executionType, NutsSession traceSession, NutsSession execSession) {
        return new DefaultNutsArtifactExecutable(def, commandName, appArgs, executorOptions, env, dir, failFast, traceSession, execSession, executionType, this);
    }

    public void ws_execId(NutsDefinition def, String commandName, String[] appArgs, String[] executorOptions, Map<String, String> env, String dir, boolean failFast, boolean temporary,
                          NutsSession traceSession,
                          NutsSession execSession,
                          NutsExecutionType executionType, boolean dry) {
        ws.security().checkAllowed(NutsConstants.Permissions.EXEC, commandName, session);
        execSession = NutsWorkspaceUtils.of(ws).validateSession(execSession);
        if (def != null && def.getPath() != null) {
            NutsDescriptor descriptor = def.getDescriptor();
            if (!descriptor.isExecutable()) {
//                session.getTerminal().getErr().println(nutToRun.getId()+" is not executable... will perform extra checks.");
//                throw new NutsNotExecutableException(descriptor.getId());
            }
            NutsArtifactCall executor = descriptor.getExecutor();
            NutsExecutorComponent execComponent = null;
            List<String> executorArgs = new ArrayList<>();
            Map<String, String> execProps = null;
            if (executor == null) {
                execComponent = resolveNutsExecutorComponent(def);
            } else {
                if (executor.getId() == null) {
                    execComponent = resolveNutsExecutorComponent(def);
                } else {
                    execComponent = resolveNutsExecutorComponent(executor.getId());
                }
                executorArgs.addAll(Arrays.asList(executor.getArguments()));
                execProps = executor.getProperties();
            }
            executorArgs.addAll(Arrays.asList(executorOptions));
            final NutsExecutionContext executionContext = new DefaultNutsExecutionContext(def,
                    appArgs, executorArgs.toArray(new String[0]),
                    env, execProps, dir, traceSession, execSession, ws, failFast,
                    temporary,
                    executionType,
                    commandName,
                    getSleepMillis()
            );
            if (dry) {
                execComponent.dryExec(executionContext);
            } else {
                execComponent.exec(executionContext);
            }
            return;

        }
        throw new NutsNotFoundException(ws, def == null ? null : def.getId());
    }
}
