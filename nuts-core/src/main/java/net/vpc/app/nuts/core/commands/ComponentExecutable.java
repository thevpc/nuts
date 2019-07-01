/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.commands;

import java.util.Properties;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.DefaultNutsExecCommand;

/**
 *
 * @author vpc
 */
public class ComponentExecutable extends AbstractNutsExecutableCommand {

    NutsDefinition def;
    String commandName;
    String[] appArgs;
    String[] executorOptions;
    Properties env;
    String dir;
    boolean failFast;
    NutsSession session;
    NutsExecutionType executionType;
    DefaultNutsExecCommand execCommand;

    public ComponentExecutable(NutsDefinition def, String commandName, String[] appArgs, String[] executorOptions, Properties env, String dir, boolean failFast, NutsSession session, NutsExecutionType executionType, DefaultNutsExecCommand execCommand) {
        super(commandName, def.getId().getLongName(), NutsExecutableType.COMPONENT);
        this.def = def;
        //all these information areavailable, an exception would be thrown if not!
        def.getContent();
        def.getDependencies();
        def.getEffectiveDescriptor();
        def.getInstallInformation();
        
        this.commandName = commandName;
        this.appArgs = appArgs;
        this.executorOptions = executorOptions;
        this.env = env;
        this.dir = dir;
        this.failFast = failFast;
        this.session = session;
        this.executionType = executionType;
        this.execCommand = execCommand;
    }

    @Override
    public NutsId getId() {
        return def.getId();
    }

    @Override
    public void execute() {
        if (!def.getInstallInformation().isInstalled()) {
            session.getWorkspace().security().checkAllowed(NutsConstants.Rights.AUTO_INSTALL, commandName);
//            if (session.getTerminal().ask()
//                    .forBoolean("%N is not yet installed. Continue",
//                            session.getWorkspace().id().set(def.getId().getLongNameId()).format()
//                    ).defaultValue(true).session(session).getBooleanValue()) {
                session.getWorkspace().install().id(def.getId()).run();
//            } else {
//                throw new NutsUserCancelException(session.getWorkspace());
//            }
        }
        execCommand.ws_exec(def, commandName, appArgs, executorOptions, env, dir, failFast, false, session, executionType);
    }

    @Override
    public String toString() {
        return "NUTS " + getId().toString() + " " + session.getWorkspace().commandLine().setArgs(appArgs).toString();
    }

}
