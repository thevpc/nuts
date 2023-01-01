/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArgument;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.io.NOutStream;

import java.util.*;

/**
 * @author thevpc
 */
public class DefaultNArtifactExecutable extends AbstractNExecutableCommand {

    NDefinition def;
    String commandName;
    String[] appArgs;
    List<String> executorOptions;
    List<String> workspaceOptions;
    Map<String, String> env;
    String dir;
    boolean failFast;
    NSession session;
    NSession execSession;
    NExecutionType executionType;
    NRunAs runAs;
    DefaultNExecCommand execCommand;
    boolean autoInstall = true;

    public DefaultNArtifactExecutable(NDefinition def, String commandName, String[] appArgs, List<String> executorOptions,
                                      List<String> workspaceOptions, Map<String, String> env, String dir, boolean failFast,
                                      NSession session,
                                      NSession execSession,
                                      NExecutionType executionType, NRunAs runAs, DefaultNExecCommand execCommand) {
        super(commandName, def.getId().getLongName(), NExecutableType.ARTIFACT);
        this.def = def;
        this.runAs = runAs;
        //all these information areavailable, an exception would be thrown if not!
        def.getContent().get(session);
        def.getDependencies().get(session);
        def.getEffectiveDescriptor().get(session);
//        def.getInstallInformation();

        this.commandName = commandName;
        this.appArgs = appArgs;
        this.env = env;
        this.dir = dir;
        this.failFast = failFast;
        this.session = session;
        this.execSession = execSession;
        this.executionType = executionType;
        this.execCommand = execCommand;

        List<String> executorOptionsList = new ArrayList<>();
        NArtifactCall exc = def.getDescriptor().getExecutor();
        if (exc != null) {

        }
        for (String option : executorOptions) {
            NArgument a = NArgument.of(option);
            if (a.key().equals("--nuts-auto-install")) {
                if (a.isKeyValue()) {
                    autoInstall = a.isNegated() != a.getBooleanValue().get(session);
                } else {
                    autoInstall = true;
                }
            } else {
                executorOptionsList.add(option);
            }
        }
        this.executorOptions = executorOptionsList;
        this.workspaceOptions = workspaceOptions;
    }

    @Override
    public NId getId() {
        return def.getId();
    }

    @Override
    public void execute() {
        if (execSession.isDry()) {
            if (autoInstall && !def.getInstallInformation().get(session).getInstallStatus().isInstalled()) {
                execSession.security().checkAllowed(NConstants.Permissions.AUTO_INSTALL, commandName);
                NOutStream out = execSession.out();
                out.printf("[dry] ==install== %s%n", def.getId().getLongName());
            }
            execCommand.ws_execId(def, commandName, appArgs, executorOptions, workspaceOptions, env, dir, failFast, false, session, execSession.copy().setDry(true), executionType, runAs);
            return;
        }
        NInstallStatus installStatus = def.getInstallInformation().get(session).getInstallStatus();
        if (!installStatus.isInstalled()) {
            if (autoInstall) {
                session.install().setSession(session).addId(def.getId()).run();
                NInstallStatus st = session.fetch().setSession(session).setId(def.getId()).getResultDefinition().getInstallInformation().get(session).getInstallStatus();
                if (!st.isInstalled()) {
                    throw new NUnexpectedException(execSession, NMsg.ofCstyle("auto installation of %s failed", def.getId()));
                }
            } else {
                throw new NUnexpectedException(execSession, NMsg.ofCstyle("you must install %s to be able to run it", def.getId()));
            }
        } else if (installStatus.isObsolete()) {
            if (autoInstall) {
                session.install()
                        .configure(true, "--reinstall")
                        .setSession(session).addId(def.getId()).run();
            }
        }
//        LinkedHashSet<NutsDependency> reinstall = new LinkedHashSet<>();
//        NutsDependencyFilter depFilter = NutsDependencyUtils.createJavaRunDependencyFilter(traceSession);
//        for (NutsDependency dependency : def.getDependencies()) {
//            if (depFilter.acceptDependency(def.getId(), dependency, traceSession)) {
//                NutsInstallStatus st = tracesession.fetch()
//                        .setSession(traceSession.copy().setFetchStrategy(NutsFetchStrategy.OFFLINE))
//                        .setId(dependency.toId()).getResultDefinition().getInstallInformation().getInstallStatus();
//                if (st.isObsolete() || st.isNonDeployed()) {
//                    reinstall.add(dependency);
//                }
//            }
//        }
//        if (!reinstall.isEmpty()) {
//            NutsInstallCommand iii = tracesession.install().setSession(traceSession).setStrategy(NutsInstallStrategy.REINSTALL);
//            for (NutsDependency nutsId : reinstall) {
//                iii.id(nutsId.toId());
//            }
//            iii.run();
//            for (NutsDependency dependency : reinstall) {
//                boolean optional = execsession.dependency().parser().parseOptional(dependency.getOptional());
//
//                NutsInstallStatus st = tracesession.fetch().setSession(traceSession.copy().setFetchStrategy(NutsFetchStrategy.OFFLINE))
//                        .setId(dependency.toId()).getResultDefinition().getInstallInformation().getInstallStatus();
//                if ((st.isObsolete() || st.isNonDeployed()) && !optional) {
//                    throw new NutsUnexpectedException(execSession, "unresolved dependency " + dependency + " has status " + st);
//                }
//            }
//        }
        execCommand.ws_execId(def, commandName, appArgs, executorOptions, workspaceOptions, env, dir, failFast, false, session, execSession.copy().setDry(false), executionType, runAs);
    }

    @Override
    public String toString() {
        return "nuts " + getId().toString() + " " + NCommandLine.of(appArgs).toString();
    }

    @Override
    public NSession getSession() {
        return session;
    }
}
