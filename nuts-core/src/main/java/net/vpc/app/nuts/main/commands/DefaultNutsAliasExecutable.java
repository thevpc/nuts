/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.main.commands;

import net.vpc.app.nuts.NutsCommandExecOptions;
import net.vpc.app.nuts.NutsExecutableType;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspaceCommandAlias;

/**
 *
 * @author vpc
 */
public class DefaultNutsAliasExecutable extends AbstractNutsExecutableCommand {

    NutsWorkspaceCommandAlias command;
    NutsCommandExecOptions o;
    NutsSession session;
    String[] args;

    public DefaultNutsAliasExecutable(NutsWorkspaceCommandAlias command, NutsCommandExecOptions o, NutsSession session, String[] args) {
        super(command.getName(),
                session.workspace().commandLine().create(command.getCommand()).toString(),
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
        String t = command.getHelpText();
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
