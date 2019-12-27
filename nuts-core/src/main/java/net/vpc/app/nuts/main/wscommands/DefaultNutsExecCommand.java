package net.vpc.app.nuts.main.wscommands;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.config.DefaultNutsArtifactCall;
import net.vpc.app.nuts.runtime.config.DefaultNutsDescriptorBuilder;
import net.vpc.app.nuts.runtime.DefaultNutsExecutionContext;
import net.vpc.app.nuts.runtime.DefaultNutsSupportLevelContext;
import net.vpc.app.nuts.main.commands.*;
import net.vpc.app.nuts.main.executors.CustomNutsExecutorComponent;
import net.vpc.app.nuts.main.DefaultNutsWorkspace;
import net.vpc.app.nuts.core.wscommands.NutsExecutableInformationExt;
import net.vpc.app.nuts.runtime.terminals.DefaultNutsSessionTerminal;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.runtime.wscommands.AbstractNutsExecCommand;

import java.util.*;

/**
 * type: Command Class
 *
 * @author vpc
 */
public class DefaultNutsExecCommand extends AbstractNutsExecCommand {

    public final NutsLogger LOG;
    public static final NutsDescriptor TEMP_DESC = new DefaultNutsDescriptorBuilder()
            .id(CoreNutsUtils.parseNutsId("temp:exe#1.0"))
            .packaging("exe")
            .executable(true)
            .executor(new DefaultNutsArtifactCall(CoreNutsUtils.parseNutsId("exec")))
            .build();


    public DefaultNutsExecCommand(DefaultNutsWorkspace ws) {
        super(ws);
        LOG=ws.log().of(DefaultNutsExecCommand.class);
    }

    @Override
    public NutsExecutableInformation which() {
        DefaultNutsSessionTerminal terminal = new DefaultNutsSessionTerminal();
        NutsWorkspaceUtils.of(ws).setWorkspace(terminal);
        NutsSession session = getSession();
        terminal.setParent(session.getTerminal());
        terminal.setOutMode(session.getTerminal().getOutMode());
        terminal.setErrMode(session.getTerminal().getErrMode());
        if(isGrabOutputString()){
            terminal.setOutMode(NutsTerminalMode.INHERITED);
        }
        if(isGrabErrorString()){
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
                terminal.setErr(session.getTerminal().out());
            }
            terminal.setErrMode(terminal.getOutMode());
        }
        terminal.out().flush();
        terminal.err().flush();
        String[] ts = command.toArray(new String[0]);
        NutsExecutableInformationExt exec = null;
        NutsSession sessionCopy = session.copy();
        sessionCopy.setTerminal(terminal);
        switch (executionType) {
            case SYSCALL: {
                if (commandDefinition != null) {
                    throw new NutsIllegalArgumentException(ws, "Unable to run nuts as syscall");
                }
                exec = new DefaultNutsSystemExecutable(ts, getExecutorOptions(),
                        sessionCopy,
                        this
                );
                break;
            }
            case SPAWN:
            case EMBEDDED: {
                if (commandDefinition != null) {
                    return ws_exec0(commandDefinition, commandDefinition.getId().getLongName(), ts, getExecutorOptions(), env, directory, failFast, executionType, sessionCopy);
                } else {
                    exec = execEmbeddedOrExternal(ts, getExecutorOptions(), sessionCopy);
                }
                break;
            }
            default: {
                throw new NutsUnsupportedArgumentException(ws, "Invalid executionType " + executionType);
            }
        }
        return exec;
    }

    @Override
    public NutsExecCommand run() {
        NutsExecutableInformationExt exec = (NutsExecutableInformationExt) which();
        executed = true;
        try {
            if(dry){
                exec.dryExecute();
            }else {
                exec.execute();
            }
        } catch (NutsExecutionException ex) {
            String p = getExtraErrorMessage();
            if (p != null) {
                result = new NutsExecutionException(ws,
                        "Execution Failed with code " + ex.getExitCode() + " and message : " + p,
                        ex, ex.getExitCode());
            } else {
                result = ex;
            }
        } catch (Exception ex) {
            String p = getExtraErrorMessage();
            if (p != null) {
                result = new NutsExecutionException(ws,
                        "Execution Failed with code " + 244 + " and message : " + p,
                        ex, 244);
            } else {
                result = new NutsExecutionException(ws, ex, 244);
            }
        }
        if (result != null && failFast) {
            throw result;
//            checkFailFast(result.getExitCode());
        }
        return this;
    }

    private NutsExecutorComponent resolveNutsExecutorComponent(NutsId nutsId) {
        for (NutsExecutorComponent nutsExecutorComponent : ws.extensions().createAll(NutsExecutorComponent.class)) {
            if (nutsExecutorComponent.getId().equalsShortName(nutsId)
                    || nutsExecutorComponent.getId().getArtifactId().equals(nutsId.toString())
                    || nutsExecutorComponent.getId().toString().equals("net.vpc.app.nuts.exec:exec-" + nutsId.toString())) {
                return nutsExecutorComponent;
            }
        }
        return new CustomNutsExecutorComponent(nutsId);
    }

    private NutsExecutorComponent resolveNutsExecutorComponent(NutsDefinition nutsDefinition) {
        NutsExecutorComponent executorComponent = ws.extensions().createSupported(NutsExecutorComponent.class, new DefaultNutsSupportLevelContext<>(ws, nutsDefinition));
        if (executorComponent != null) {
            return executorComponent;
        }
        throw new NutsNotFoundException(ws, nutsDefinition.getId());
    }

    private NutsExecutableInformationExt execEmbeddedOrExternal(String[] cmd, String[] executorOptions, NutsSession session) {
        if (cmd == null || cmd.length == 0) {
            throw new NutsIllegalArgumentException(ws, "Missing command");
        }
        String[] args = new String[cmd.length - 1];
        System.arraycopy(cmd, 1, args, 0, args.length);
        String cmdName = cmd[0];
        //resolve internal commands!
        switch (cmdName) {
            case "update": {
                return new DefaultNutsUpdateInternalExecutable(args, session);
            }
            case "check-updates": {
                return new DefaultNutsCheckUpdatesInternalExecutable(args, session);
            }
            case "install": {
                return new DefaultNutsInstallInternalExecutable(args, session);
            }
            case "uninstall": {
                return new DefaultNutsUninstallInternalExecutable(args, session);
            }
            case "deploy": {
                return new DefaultNutsDeployInternalExecutable(args, session);
            }
            case "undeploy": {
                return new DefaultNutsUndeployInternalExecutable(args, session);
            }
            case "push": {
                return new DefaultNutsPushInternalExecutable(args, session);
            }
            case "fetch": {
                return new DefaultNutsFetchInternalExecutable(args, session);
            }
            case "search": {
                return new DefaultNutsSearchInternalExecutable(args, session);
            }
            case "version": {
                return new DefaultNutsVersionInternalExecutable(args, session, this);
            }
            case "license": {
                return new DefaultNutsLicenseInternalExecutable(args, session);
            }
            case "help": {
                return new DefaultNutsHelpInternalExecutable(args, session);
            }
            case "welcome": {
                return new DefaultNutsWelcomeInternalExecutable(args, session);
            }
            case "info": {
                return new DefaultNutsInfoInternalExecutable(args, session);
            }
            case "which": {
                return new DefaultNutsWhichInternalExecutable(args, session, this);
            }
            case "exec": {
                return new DefaultNutsExecInternalExecutable(args, session, this);
            }
        }
        if (cmdName.contains("/") || cmdName.contains("\\")) {
            return new DefaultNutsArtifactPathExecutable(cmdName, args, executorOptions, executionType, getSession(), this);
        } else if (cmdName.contains(":")) {
            boolean forceInstalled=false;
            if(cmdName.endsWith("!")){
                cmdName=cmdName.substring(0,cmdName.length()-1);
                forceInstalled=true;
            }
            return ws_exec(cmdName, args, executorOptions, env, directory, failFast, executionType, session,forceInstalled);
        } else {
            NutsWorkspaceCommandAlias command = null;
            boolean forceInstalled=false;
            if(cmdName.endsWith("!")){
                cmdName=cmdName.substring(0,cmdName.length()-1);
                forceInstalled=true;
            }
            command = ws.config().findCommandAlias(cmdName);
            if (command != null) {
                NutsCommandExecOptions o = new NutsCommandExecOptions().setExecutorOptions(executorOptions).setDirectory(directory).setFailFast(failFast)
                        .setExecutionType(executionType).setEnv(env);
                return new DefaultNutsAliasExecutable(command, o, session, args);
            } else {
                return ws_exec(cmdName, args, executorOptions, env, directory, failFast, executionType, session,forceInstalled);
            }
        }
    }

    protected NutsExecutableInformationExt ws_exec(String commandName, String[] appArgs, String[] executorOptions, Map<String,String> env, String dir, boolean failFast, NutsExecutionType executionType, NutsSession session,boolean forceInstalled) {
        NutsDefinition def = null;
        NutsId nid = ws.id().parse(commandName);
        if(nid==null){
            throw new NutsNotFoundException(ws, commandName);
        }
        NutsSession searchSession = CoreNutsUtils.silent(session);
        List<NutsId> ff = ws.search().id(nid).session(searchSession).optional(false).latest().failFast(false)
                .defaultVersions()
//                .configure(true,"--trace-monitor")
                .installStatus(NutsInstallStatus.INSTALLED).getResultIds().list();
        if (ff.isEmpty()) {
            //retest without checking if the parseVersion is default or not
            // this help recovering from "invalid default parseVersion" issue
                ff = ws.search().id(nid).session(searchSession).optional(false).latest().failFast(false)
                        .installStatus(NutsInstallStatus.INSTALLED).getResultIds().list();
        }
        if (ff.isEmpty()) {
            if(!forceInstalled) {
                //now search online
                // this helps recovering from "invalid default parseVersion" issue
                if(session.isPlainTrace()) {
                    session.out().printf("##%s## is @@not installed@@, will search for it online. Type ((CTRL\\^C)) to stop...\n", commandName);
                    session.out().flush();
                }
                ff = ws.search().id(nid).session(searchSession).optional(false).failFast(false).online().latest()
//                        .configure(true,"--trace-monitor")
                        .getResultIds().list();
            }
        }
        if (ff.isEmpty()) {
            throw new NutsNotFoundException(ws, nid);
        } else if (ff.size() > 1) {
            throw new NutsTooManyElementsException(ws, nid.toString()+" can be resolved to all of "+ff);
        }
        NutsId goodId = ff.get(0);
        def = ws.fetch().id(goodId)
                .session(searchSession)
                .optional(false).dependencies()
                .failFast()
                .effective()
                .content()
                .scope(NutsDependencyScopePattern.RUN)
                .getResultDefinition();
        return ws_exec0(def, commandName, appArgs, executorOptions, env, dir, failFast, executionType, session);
    }

    protected NutsExecutableInformationExt ws_exec0(NutsDefinition def, String commandName, String[] appArgs, String[] executorOptions, Map<String,String> env, String dir, boolean failFast, NutsExecutionType executionType, NutsSession session) {
        return new DefaultNutsArtifactExecutable(def, commandName, appArgs, executorOptions, env, dir, failFast, session, executionType, this);
    }

    public void ws_exec(NutsDefinition def, String commandName, String[] appArgs, String[] executorOptions, Map<String,String> env, String dir, boolean failFast, boolean temporary, NutsSession session, NutsExecutionType executionType,boolean dry) {
        ws.security().checkAllowed(NutsConstants.Permissions.EXEC, commandName);
        session = NutsWorkspaceUtils.of(ws).validateSession( session);
        if (def != null && def.getPath() != null) {
            NutsDescriptor descriptor = def.getDescriptor();
            if (!descriptor.isExecutable()) {
//                session.getTerminal().getErr().println(nutToRun.getId()+" is not executable... will perform extra checks.");
//                throw new NutsNotExecutableException(descriptor.getId());
            }
            NutsArtifactCall executor = descriptor.getExecutor();
            NutsExecutorComponent execComponent = null;
            List<String> executorArgs = new ArrayList<>();
            Map<String,String> execProps = null;
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
                    env, execProps, dir, session, ws, true,
                    temporary,
                    executionType,
                    commandName
            );
            if(dry){
                execComponent.dryExec(executionContext);
            }else {
                execComponent.exec(executionContext);
            }
            return;

        }
        throw new NutsNotFoundException(ws, def == null ? null : def.getId());
    }
}
