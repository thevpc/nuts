/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.alias;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.AbstractNExecutableCommand;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

/**
 * @author thevpc
 */
public class DefaultNAliasExecutable extends AbstractNExecutableCommand {

    NCustomCommand command;
    NCommandExecOptions o;
    NSession session;
    String[] args;

    public DefaultNAliasExecutable(NCustomCommand command, NCommandExecOptions o, NSession session, String[] args) {
        super(command.getName(),
                NCmdLine.of(command.getCommand()).toString(),
                NExecutableType.ALIAS);
        this.command = command;
        this.o = o;
        this.session = session;
        this.args = args;
    }

    @Override
    public NId getId() {
        return command.getOwner();
    }

    @Override
    public void execute() {
        command.exec(args, o, session);
    }


    @Override
    public NText getHelpText() {
        NText t = command.getHelpText(session);
        if (t != null) {
            return t;
        }
        return NTexts.of(session).ofStyled("No help available. Try '" + getName() + " --help'", NTextStyle.error());
    }

    @Override
    public String toString() {
        return "alias " + command.getName() + " @ " + command.getOwner();
    }

    @Override
    public NSession getSession() {
        return session;
    }
}
