/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.alias;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.boot.NBootCompleteRequest;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NCmdExecOptions;
import net.thevpc.nuts.command.NCustomCmd;
import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.command.NExecutableType;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.AbstractNExecutableInformationExt;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;

import java.util.List;

/**
 * @author thevpc
 */
public class DefaultNAliasExecutable extends AbstractNExecutableInformationExt {

    NCustomCmd command;
    NCmdExecOptions o;
   String[] args;

    public DefaultNAliasExecutable(NCustomCmd command, NCmdExecOptions o, String[] args, NExec execCommand, List<String> executorOptions) {
        super(command.name(),
                NCmdLine.of(command.command()).toString(),
                NExecutableType.ALIAS,execCommand);
        this.executorOptions=executorOptions;
        this.command = command;
        this.o = o;
        this.args = args;
        NCmdLine.of(this.executorOptions).matcher()
                .with("--show-command").matchFlag(a->this.showCommand = (a.booleanValue()))
                .with("--nuts-exec-mode").matchFlag(a->this.completeRequest = NBootCompleteRequest.parseOrNull(a.stringValue()))
                .withAny().skip()
                .requireAll();
    }

    @Override
    public NId id() {
        return command.owner();
    }

    @Override
    public int execute() {
        if(completeRequest!=null){
            return 0;
        }
        return command.exec(args, o);
    }


    @Override
    public NText helpText() {
        NText t = command.helpText();
        if (t != null) {
            return t;
        }
        return NText.ofStyled("No help available. Try '" + name() + " --help'", NTextStyle.error());
    }

    @Override
    public String toString() {
        return "alias " + command.name() + " @ " + command.owner();
    }

}
