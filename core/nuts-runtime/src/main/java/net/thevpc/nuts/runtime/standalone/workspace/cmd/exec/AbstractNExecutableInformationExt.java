/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NExecutableInformationExt;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;

/**
 * @author thevpc
 */
public abstract class AbstractNExecutableInformationExt implements NExecutableInformationExt {

    protected NExecutableType type;
    protected String name;
    protected String value;
    private final NExecCmd execCommand;
    public AbstractNExecutableInformationExt(String name, NExecutableType type, NExecCmd execCommand) {
        this.type = type;
        this.name = name;
        this.execCommand = execCommand;
    }

    public AbstractNExecutableInformationExt(String name, String value, NExecutableType type, NExecCmd execCommand) {
        this.type = type;
        this.name = name;
        this.value = value;
        this.execCommand = execCommand;
    }

    protected NLog LOG(){
        return NLog.of(getClass());
    }

    public NExecCmd getExecCommand() {
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
        return NText.ofStyled(
                "No help available. Try '" + getName() + " --help'",
                NTextStyle.error()
        );
    }

    @Override
    public void close() {

    }
}
