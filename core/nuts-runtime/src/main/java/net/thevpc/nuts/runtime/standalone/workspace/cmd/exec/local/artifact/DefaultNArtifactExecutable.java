/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.artifact;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.AbstractNExecutableCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultNExecCommand;
import net.thevpc.nuts.util.NMsg;

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
    NPath dir;
    boolean failFast;
    NExecutionType executionType;
    NRunAs runAs;
    DefaultNExecCommand execCommand;
    boolean autoInstall = true;

    public DefaultNArtifactExecutable(NDefinition def, String commandName, String[] appArgs, List<String> executorOptions,
                                      List<String> workspaceOptions, Map<String, String> env, NPath dir, boolean failFast,
                                      NExecutionType executionType, NRunAs runAs, DefaultNExecCommand execCommand) {
        super(commandName, def.getId().getLongName(), NExecutableType.ARTIFACT, execCommand);
        this.def = def;
        this.runAs = runAs;
        //all these information are available, an exception would be thrown if not!
        def.getContent().get(getSession());
        def.getDependencies().get(getSession());
        def.getEffectiveDescriptor().get(getSession());
//        def.getInstallInformation();

        this.commandName = commandName;
        this.appArgs = appArgs;
        this.env = env;
        this.dir = dir;
        this.failFast = failFast;
        this.executionType = executionType;
        this.execCommand = execCommand;

        List<String> executorOptionsList = new ArrayList<>();
        NArtifactCall exc = def.getDescriptor().getExecutor();
        if (exc != null) {

        }
        for (String option : executorOptions) {
            NArg a = NArg.of(option);
            if (a.key().equals("--nuts-auto-install")) {
                if (a.isKeyValue()) {
                    autoInstall = a.isNegated() != a.getBooleanValue().get(getSession());
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
    public int execute() {
        NSession session = getSession();
        if (session.isDry()) {
            if (autoInstall && !def.getInstallInformation().get(session).getInstallStatus().isInstalled()) {
                NWorkspaceSecurityManager.of(session).checkAllowed(NConstants.Permissions.AUTO_INSTALL, commandName);
                NPrintStream out = session.out();
                out.println(NMsg.ofC("[dry] ==install== %s", def.getId().getLongName()));
            }
            execCommand.ws_execId(def, commandName, appArgs, executorOptions, workspaceOptions, env, dir, failFast,
                    false, session, execCommand.getIn(), execCommand.getOut(), execCommand.getErr(), executionType, runAs);
            return NExecutionException.SUCCESS;
        }
        NInstallStatus installStatus = def.getInstallInformation().get(session).getInstallStatus();
        if (!installStatus.isInstalled()) {
            if (autoInstall) {
                NInstallCommand.of(session).addId(def.getId()).run();
                NInstallStatus st = NFetchCommand.of(def.getId(), session).getResultDefinition().getInstallInformation().get(session).getInstallStatus();
                if (!st.isInstalled()) {
                    throw new NUnexpectedException(session, NMsg.ofC("auto installation of %s failed", def.getId()));
                }
            } else {
                throw new NUnexpectedException(session, NMsg.ofC("you must install %s to be able to run it", def.getId()));
            }
        } else if (installStatus.isObsolete()) {
            if (autoInstall) {
                NInstallCommand.of(session)
                        .configure(true, "--reinstall")
                        .addId(def.getId()).run();
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
        return execCommand.ws_execId(def, commandName, appArgs, executorOptions, workspaceOptions, env, dir, failFast, false, session
                , execCommand.getIn()
                , execCommand.getOut()
                , execCommand.getErr()
                , executionType, runAs);
    }

    @Override
    public String toString() {
        return "nuts " + getId().toString() + " " + NCmdLine.of(appArgs).toString();
    }

}
