/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.remote.ssh.system;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.boot.NBootCompleteRequest;
import net.thevpc.nuts.cmdline.NCmdLine;

import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.platform.NEnv;
import net.thevpc.nuts.spi.NExecTargetSPI;
import net.thevpc.nuts.command.NExecutableType;
import net.thevpc.nuts.command.NExecutionException;
import net.thevpc.nuts.io.NExecInput;
import net.thevpc.nuts.io.NExecOutput;
import net.thevpc.nuts.runtime.standalone.executor.AbstractSyncIProcessExecHelper;
import net.thevpc.nuts.collections.NCollections;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.AbstractNExecutableInformationExt;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultNExecTargetCommandContext;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NMsg;

import java.io.IOException;
import java.util.List;

/**
 * @author thevpc
 */
public class DefaultNSystemExecutableRemote extends AbstractNExecutableInformationExt {

    String[] cmd;
    private NExecTargetSPI commExec;
    private NExecInput in;
    private NExecOutput out;
    private NExecOutput err;

    public DefaultNSystemExecutableRemote(NExecTargetSPI commExec, String[] cmd,
                                          List<String> executorOptions,
                                          NExec execCommand,
                                          NExecInput in,
                                          NExecOutput out,
                                          NExecOutput err
    ) {
        super(cmd[0],
                NCmdLine.of(cmd).toString(),
                NExecutableType.SYSTEM, execCommand);
        this.in = in;
        this.out = out;
        this.err = err;
        this.cmd = cmd;
        this.executorOptions = NCollections.nonNullList(executorOptions);
        this.commExec = commExec;
        NCmdLine.of(this.executorOptions).matcher()
                .with("--show-command").matchFlag(a->this.showCommand = (a.booleanValue()))
                .with("--nuts-exec-mode").matchFlag(a->this.completeRequest = NBootCompleteRequest.parseOrNull(a.stringValue()))
                .withAny().skip()
                .requireAll();
    }

    @Override
    public NId id() {
        return null;
    }

    private AbstractSyncIProcessExecHelper resolveExecHelper() {
        if(completeRequest!=null){
            return new AbstractSyncIProcessExecHelper() {
                @Override
                public int exec() {
                    return NExecutionException.SUCCESS;
                }
            };
        }
        return new AbstractSyncIProcessExecHelper() {
            @Override
            public int exec() {

                NExec execCommand = getExecCommand();
                try(DefaultNExecTargetCommandContext d=new DefaultNExecTargetCommandContext(
                        execCommand.connectionString(),
                        cmd,
                        in,
                        out,
                        err,
                        execCommand
                )) {
                    return commExec.exec(d);
                }catch (IOException ex){
                    throw new NExecutionException(NMsg.ofC("command failed :%s", ex), ex);
                }
            }
        };
    }

    @Override
    public int execute() {
        return resolveExecHelper().exec();
    }


    @Override
    public NText helpText() {
        switch (NEnv.of().osFamily()) {
            case WINDOWS: {
                return NText.ofStyled(
                        "No help available. Try " + name() + " /help",
                        NTextStyle.error()
                );
            }
            default: {
                return
                        NText.ofStyled(
                                "No help available. Try 'man " + name() + "' or '" + name() + " --help'",
                                NTextStyle.error()
                        );
            }
        }
    }

    @Override
    public String toString() {
        return getExecCommand().runAs() + " " + NCmdLine.of(cmd).toString();
    }

}
