/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.boot.NBootCompleteRequest;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.command.NExecutableType;
import net.thevpc.nuts.command.NExecutionException;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.text.NMsg;

import java.util.List;

/**
 * @author bacali95
 * @since 0.8.3
 */
public class DefaultUnknownExecutable extends AbstractNExecutableInformationExt {


    public DefaultUnknownExecutable(String[] cmd, NExec execCommand, List<String> executorOptions) {
        super(cmd[0], NCmdLine.of(cmd).toString(), NExecutableType.UNKNOWN,execCommand);
        this.executorOptions=executorOptions;
        NCmdLine.of(this.executorOptions).matcher()
                .when("--show-command").asFlag(a->this.showCommand = (a.booleanValue()))
                .when("--nuts-exec-mode").asFlag(a->this.completeRequest = NBootCompleteRequest.parseOrNull(a.stringValue()))
                .whenAny().skip()
                .requireAll();
    }

    @Override
    public int execute() {
        if(completeRequest!=null){
            return 0;
        }
        NSession session = NSession.of();
        if(session.isDry()){
            throw new NExecutionException(NMsg.ofC("cannot execute an unknown command : %s", name), NExecutionException.ERROR_1);
        }else {
            throw new NExecutionException(NMsg.ofC("cannot execute an unknown command : %s", name), NExecutionException.ERROR_1);
        }
    }

    @Override
    public NId id() {
        return null;
    }
}
