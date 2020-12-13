/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.main.commands;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.main.wscommands.DefaultNutsExecCommand;

import java.io.PrintStream;
import java.util.*;

/**
 * @author thevpc
 */
public class DefaultNutsArtifactExecutable extends AbstractNutsExecutableCommand {

    NutsDefinition def;
    String commandName;
    String[] appArgs;
    String[] executorOptions;
    Map<String, String> env;
    String dir;
    boolean failFast;
    NutsSession traceSession;
    NutsSession execSession;
    NutsExecutionType executionType;
    DefaultNutsExecCommand execCommand;
    boolean autoInstall = true;

    public DefaultNutsArtifactExecutable(NutsDefinition def, String commandName, String[] appArgs, String[] executorOptions,
                                         Map<String, String> env, String dir, boolean failFast,
                                         NutsSession traceSession,
                                         NutsSession execSession,
                                         NutsExecutionType executionType, DefaultNutsExecCommand execCommand) {
        super(commandName, def.getId().getLongName(), NutsExecutableType.ARTIFACT);
        this.def = def;
        //all these information areavailable, an exception would be thrown if not!
        def.getContent();
        def.getDependencies();
        def.getEffectiveDescriptor();
        def.getInstallInformation();

        this.commandName = commandName;
        this.appArgs = appArgs;
        this.env = env;
        this.dir = dir;
        this.failFast = failFast;
        this.traceSession = traceSession;
        this.execSession = execSession;
        this.executionType = executionType;
        this.execCommand = execCommand;

        List<String> executorOptionsList = new ArrayList<>();
        for (String option : executorOptions) {
            NutsArgument a = traceSession.getWorkspace().commandLine().createArgument(option);
            if (a.getStringKey().equals("--nuts-auto-install")) {
                if (a.isKeyValue()) {
                    autoInstall = a.isNegated() != a.getBooleanValue();
                } else {
                    autoInstall = true;
                }
            } else {
                executorOptionsList.add(option);
            }
        }
        this.executorOptions = executorOptionsList.toArray(new String[0]);
    }

    @Override
    public NutsId getId() {
        return def.getId();
    }

    @Override
    public void execute() {
        Set<NutsInstallStatus> installStatus = def.getInstallInformation().getInstallStatus();
        if (autoInstall && !installStatus.contains(NutsInstallStatus.INSTALLED)) {
            traceSession.getWorkspace().install().id(def.getId()).run();
            Set<NutsInstallStatus> st = traceSession.getWorkspace().fetch().setId(def.getId()).getResultDefinition().getInstallInformation().getInstallStatus();
            if (!st.contains(NutsInstallStatus.INSTALLED)) {
                return;
            }
        } else if (installStatus.contains(NutsInstallStatus.INSTALLED) && installStatus.contains(NutsInstallStatus.OBSOLETE)) {
            traceSession.getWorkspace().install().id(def.getId()).run();
        }
        LinkedHashSet<NutsDependency> reinstall=new LinkedHashSet<>();
        for (NutsDependency dependency : def.getDependencies()) {
            NutsDependencyScope scope = execSession.getWorkspace().dependency().parser().parseScope(dependency.getScope());
            boolean acceptedScope=scope!=NutsDependencyScope.TEST_COMPILE
                    &&scope!=NutsDependencyScope.TEST_PROVIDED
                    &&scope!=NutsDependencyScope.TEST_RUNTIME;
            if(acceptedScope) {
                Set<NutsInstallStatus> st = traceSession.getWorkspace().fetch().setOffline().setId(dependency.toId()).getResultDefinition().getInstallInformation().getInstallStatus();
                if (st.contains(NutsInstallStatus.OBSOLETE) || st.contains(NutsInstallStatus.NOT_INSTALLED)) {
                    reinstall.add(dependency);
                }
            }
        }
        if(!reinstall.isEmpty()){
            NutsInstallCommand iii = traceSession.getWorkspace().install().setStrategy(NutsInstallStrategy.REINSTALL);
            for (NutsDependency nutsId : reinstall) {
                iii.id(nutsId.toId());
            }
            iii.run();
            for (NutsDependency dependency : reinstall) {
                boolean optional = execSession.getWorkspace().dependency().parser().parseOptional(dependency.getOptional());

                Set<NutsInstallStatus> st = traceSession.getWorkspace().fetch().setOffline().setId(dependency.toId()).getResultDefinition().getInstallInformation().getInstallStatus();
                if (st.contains(NutsInstallStatus.OBSOLETE)||st.contains(NutsInstallStatus.NOT_INSTALLED) && !optional) {
                    throw new NutsUnexpectedException(execSession.getWorkspace(),"unresolved dependency "+dependency+" has status "+st);
                }
            }
        }
        execCommand.ws_exec(def, commandName, appArgs, executorOptions, env, dir, failFast, false, traceSession, execSession, executionType, false);
    }

    @Override
    public void dryExecute() {
        if (autoInstall && !def.getInstallInformation().getInstallStatus().contains(NutsInstallStatus.INSTALLED)) {
            execSession.getWorkspace().security().checkAllowed(NutsConstants.Permissions.AUTO_INSTALL, commandName);
            PrintStream out = execSession.out();
            out.printf("[dry] ==install== %s%n", def.getId().getLongName());
        }
        execCommand.ws_exec(def, commandName, appArgs, executorOptions, env, dir, failFast, false, traceSession, execSession, executionType, true);
    }

    @Override
    public String toString() {
        return "NUTS " + getId().toString() + " " + execSession.getWorkspace().commandLine().create(appArgs).toString();
    }

}
