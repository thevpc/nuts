/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NExecutableInformationExt;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

/**
 * @author thevpc
 */
public abstract class AbstractNExecutableCommand implements NExecutableInformationExt {

    protected NExecutableType type;
    protected String name;
    protected String value;
    private final NExecCommand execCommand;
    public AbstractNExecutableCommand(String name, NExecutableType type, NExecCommand execCommand) {
        this.type = type;
        this.name = name;
        this.execCommand = execCommand;
    }

    public AbstractNExecutableCommand(String name, String value, NExecutableType type, NExecCommand execCommand) {
        this.type = type;
        this.name = name;
        this.value = value;
        this.execCommand = execCommand;
    }

    public NSession getSession() {
        return getExecCommand().getSession();
    }

    public NExecCommand getExecCommand() {
        return execCommand;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NExecutableType getType() {
        return type;
    }

    @Override
    public String getDescription() {
        return toString();
    }

    @Override
    public NText getHelpText() {
        return NTexts.of(getSession()).ofStyled(
                "No help available. Try '" + getName() + " --help'",
                NTextStyle.error()
        );
    }

}
