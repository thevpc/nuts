/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands.settings;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.wscommands.exec.AbstractNutsExecutableCommand;

/**
 *
 * @author thevpc
 */
public class DefaultNutsAliasExecutable extends AbstractNutsExecutableCommand {

    NutsWorkspaceCustomCommand command;
    NutsCommandExecOptions o;
    NutsSession session;
    String[] args;

    public DefaultNutsAliasExecutable(NutsWorkspaceCustomCommand command, NutsCommandExecOptions o, NutsSession session, String[] args) {
        super(command.getName(),
                NutsCommandLine.of(command.getCommand(),session).toString(),
                NutsExecutableType.ALIAS);
        this.command = command;
        this.o = o;
        this.session = session;
        this.args = args;
    }

    @Override
    public NutsId getId() {
        return command.getOwner();
    }

    @Override
    public void execute() {
        command.exec(args, o, session);
    }

    @Override
    public void dryExecute() {
        command.dryExec(args, o, session);
    }

    @Override
    public String getHelpText() {
        String t = command.getHelpText(session);
        if (t != null) {
            return t;
        }
        return "No help available. Try '" + getName() + " --help'";
    }

    @Override
    public String toString() {
        return "CMD " + command.getName() + " @ " + command.getOwner();
    }

}
