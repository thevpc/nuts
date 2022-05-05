/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.alias;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.AbstractNutsExecutableCommand;
import net.thevpc.nuts.text.NutsText;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;

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
                NutsCommandLine.of(command.getCommand()).toString(),
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
    public NutsText getHelpText() {
        NutsText t = command.getHelpText(session);
        if (t != null) {
            return t;
        }
        return NutsTexts.of(session).ofStyled("No help available. Try '" + getName() + " --help'", NutsTextStyle.error());
    }

    @Override
    public String toString() {
        return "alias " + command.getName() + " @ " + command.getOwner();
    }

    @Override
    public NutsSession getSession() {
        return session;
    }
}
