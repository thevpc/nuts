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

    NutsWorkspace ws;
    NutsDefinition def;
    String commandName;
    String[] appArgs;
    String[] executorOptions;
    Properties env;
    String dir;
    boolean failFast;
    NutsSession session;
    boolean embedded;
    DefaultNutsExecCommand execCommand;

    public ComponentExecutable(NutsDefinition def, String commandName, String[] appArgs, String[] executorOptions, Properties env, String dir, boolean failFast, NutsWorkspace ws, NutsSession session, boolean embedded, DefaultNutsExecCommand execCommand) {
        super(commandName, def.getId().getLongName(), NutsExecutableType.COMPONENT);
        this.def = def;
        this.ws = ws;
        this.commandName = commandName;
        this.appArgs = appArgs;
        this.executorOptions = executorOptions;
        this.env = env;
        this.dir = dir;
        this.failFast = failFast;
        this.session = session;
        this.embedded = embedded;
        this.execCommand = execCommand;
    }

    @Override
    public NutsId getId() {
        return def.getId();
    }

    @Override
    public void execute() {
        if (!def.getInstallation().isInstalled()) {
            ws.security().checkAllowed(NutsConstants.Rights.AUTO_INSTALL, commandName);
            if (session.getTerminal().ask()
                    .forBoolean("==%s== is not yet installed. Do you want to proceed", def.getId().getLongName()).defaultValue(true).session(session).getBooleanResult()) {
                ws.install().id(def.getId()).setSession(session.force()).run();
            } else {
                throw new NutsUserCancelException(ws);
            }
        }
        execCommand.ws_exec(def, commandName, appArgs, executorOptions, env, dir, failFast, session, embedded);
    }

    @Override
    public String toString() {
        return "NUTS " + getId().toString() + " " + ws.parser().parseCommand(appArgs).getCommandLine();
    }

}
