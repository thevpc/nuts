package net.vpc.app.nuts.core.impl.def.wscommands;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.*;
import net.vpc.app.nuts.core.impl.def.commands.*;
import net.vpc.app.nuts.core.impl.def.executors.CustomNutsExecutorComponent;
import net.vpc.app.nuts.core.impl.def.DefaultNutsWorkspace;
import net.vpc.app.nuts.core.spi.NutsExecutableInformationExt;
import net.vpc.app.nuts.core.terminals.DefaultNutsSessionTerminal;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.core.wscommands.AbstractNutsExecCommand;

import java.util.*;
import java.util.logging.Logger;

/**
 * type: Command Class
 *
 * @author vpc
 */
public class DefaultNutsExecCommand extends AbstractNutsExecCommand {

    public static final Logger LOG = Logger.getLogger(DefaultNutsExecCommand.class.getName());
    public static final NutsDescriptor TEMP_DESC = new DefaultNutsDescriptorBuilder()
            .setId(CoreNutsUtils.parseNutsId("temp:exe#1.0"))
            .setPackaging("exe")
            .setExecutable(true)
            .setExecutor(new DefaultNutsArtifactCall(CoreNutsUtils.parseNutsId("exec")))
            .build();


    public DefaultNutsExecCommand(DefaultNutsWorkspace ws) {
        super(ws);
    }

    @Override
    public NutsExecutableInformation which() {
        DefaultNutsSessionTerminal terminal = new DefaultNutsSessionTerminal();
        terminal.install(ws);
        terminal.setParent(getValidSession().getTerminal());
        terminal.setOutMode(getValidSession().getTerminal().getOutMode());
        terminal.setErrMode(getValidSession().getTerminal().getErrMode());
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
                terminal.setErr(getValidSession().getTerminal().out());
            }
            terminal.setErrMode(terminal.getOutMode());
        }
        terminal.out().flush();
        terminal.err().flush();
        String[] ts = command.toArray(new String[0]);
        NutsExecutableInformationExt exec = null;
        switch (executionType) {
            case SYSCALL: {
                if (commandDefinition != null) {
                    throw new NutsIllegalArgumentException(ws, "Unable to run nuts as syscall");
                }
                exec = new DefaultNutsSystemExecutable(ts, getExecutorOptions(),
                        getValidSession().copy().setTerminal(terminal),
                        this
                );
                break;
            }
            case SPAWN:
            case EMBEDDED: {
                if (commandDefinition != null) {
                    return ws_exec0(commandDefinition, commandDefinition.getId().getLongName(), ts, getExecutorOptions(), env, directory, failFast, executionType, getValidSession());
                } else {
                    exec = execEmbeddedOrExternal(ts, getExecutorOptions(), getValidSession().copy().setTerminal(terminal));
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
            return new DefaultNutsArtifactPathExecutable(cmdName, args, executorOptions, executionType, getValidSession(), this);
        } else if (cmdName.contains(":")) {
            return ws_exec(cmdName, args, executorOptions, env, directory, failFast, executionType, session);
        } else {
            NutsWorkspaceCommandAlias command = null;
            command = ws.config().findCommandAlias(cmdName);
            if (command != null) {
                NutsCommandExecOptions o = new NutsCommandExecOptions().setExecutorOptions(executorOptions).setDirectory(directory).setFailFast(failFast)
                        .setExecutionType(executionType).setEnv(env);
                return new DefaultNutsAliasExecutable(command, o, session, args);
            } else {
                return ws_exec(cmdName, args, executorOptions, env, directory, failFast, executionType, session);
            }
        }
    }

    protected NutsExecutableInformationExt ws_exec(String commandName, String[] appArgs, String[] executorOptions, Map<String,String> env, String dir, boolean failFast, NutsExecutionType executionType, NutsSession session) {
        NutsDefinition def = null;
        NutsId nid = ws.id().parse(commandName);
        NutsSession searchSession = session.copy().trace(false);
        List<NutsId> ff = ws.search().id(nid).session(searchSession).setOptional(false).latest().failFast(false)
                .defaultVersions()
                .installed().getResultIds().list();
        if (ff.isEmpty()) {
            //retest whithout checking it the parseVersion is default or not
            // this help recovering from "invalid default parseVersion" issue
            ff = ws.search().id(nid).session(searchSession).setOptional(false).latest().failFast(false)
                    .installed().getResultIds().list();
        }
        if (ff.isEmpty()) {
            //now search online
            // this helps recovering from "invalid default parseVersion" issue
            ff = ws.search().id(nid).session(searchSession).setOptional(false).failFast(false).online().latest()
                    .getResultIds().list();
        }
        if (ff.isEmpty()) {
            throw new NutsNotFoundException(ws, nid);
        } else if (ff.size() > 1) {
            throw new NutsTooManyElementsException(ws, nid.toString());
        }
        NutsId goodId = ff.get(0);
        def = ws.fetch().id(goodId)
                .session(searchSession)
                .setOptional(false).dependencies()
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
        session = NutsWorkspaceUtils.validateSession(ws, session);
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
