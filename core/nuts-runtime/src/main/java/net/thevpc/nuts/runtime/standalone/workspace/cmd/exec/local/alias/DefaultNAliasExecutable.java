/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.alias;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.AbstractNExecutableInformationExt;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

/**
 * @author thevpc
 */
public class DefaultNAliasExecutable extends AbstractNExecutableInformationExt {

    NCustomCmd command;
    NCmdExecOptions o;
   String[] args;

    public DefaultNAliasExecutable(NCustomCmd command, NCmdExecOptions o, String[] args, NExecCmd execCommand) {
        super(command.getName(),
                NCmdLine.of(command.getCommand()).toString(),
                NExecutableType.ALIAS,execCommand);
        this.command = command;
        this.o = o;
        this.args = args;
    }

    @Override
    public NId getId() {
        return command.getOwner();
    }

    @Override
    public int execute() {
        return command.exec(args, o, getSession());
    }


    @Override
    public NText getHelpText() {
        NText t = command.getHelpText(getSession());
        if (t != null) {
            return t;
        }
        return NTexts.of(getSession()).ofStyled("No help available. Try '" + getName() + " --help'", NTextStyle.error());
    }

    @Override
    public String toString() {
        return "alias " + command.getName() + " @ " + command.getOwner();
    }

}
