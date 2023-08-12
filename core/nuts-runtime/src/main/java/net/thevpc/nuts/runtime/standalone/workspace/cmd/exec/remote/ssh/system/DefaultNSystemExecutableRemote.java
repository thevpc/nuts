/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.remote.ssh.system;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.executor.AbstractSyncIProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.AbstractNExecutableCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultNExecCommandExtensionContext;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.remote.ssh.RemoteConnexionStringInfo;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

import java.io.IOException;
import java.util.List;

/**
 * @author thevpc
 */
public class DefaultNSystemExecutableRemote extends AbstractNExecutableCommand {

    String[] cmd;
    List<String> executorOptions;
    private boolean showCommand = false;
    private NExecCommandExtension commExec;
    private NExecInput in;
    private NExecOutput out;
    private NExecOutput err;

    public DefaultNSystemExecutableRemote(NExecCommandExtension commExec, String[] cmd,
                                          List<String> executorOptions,
                                          NExecCommand execCommand,
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
        this.executorOptions = CoreCollectionUtils.nonNullList(executorOptions);
        this.commExec = commExec;
        NCmdLine cmdLine = NCmdLine.of(this.executorOptions);
        while (cmdLine.hasNext()) {
            NArg aa = cmdLine.peek().get(getSession());
            switch (aa.key()) {
                case "--show-command": {
                    cmdLine.withNextFlag((v, a, s) -> this.showCommand = (v));
                    break;
                }
                default: {
                    cmdLine.skip();
                }
            }
        }
    }

    @Override
    public NId getId() {
        return null;
    }

    private AbstractSyncIProcessExecHelper resolveExecHelper() {
//        Map<String, String> e2 = null;
//        Map<String, String> env1 = execCommand.getEnv();
//        if (env1 != null) {
//            e2 = new HashMap<>((Map) env1);
//        }
//        return ProcessExecHelper.ofArgs(null,
//                execCommand.getCommand().toArray(new String[0]), e2,
//                execCommand.getDirectory() == null ? null : execCommand.getDirectory().toFile(),
//                session.getTerminal(),
//                execSession.getTerminal(), showCommand, true, execCommand.getSleepMillis(),
//                inheritSystemIO,
//                /*redirectErr*/ false,
//                /*fileIn*/ execCommand.getRedirectInputFile(),
//                /*fileOut*/ execCommand.getRedirectOutputFile(),
//                execCommand.getRunAs(),
//                session);


        return new AbstractSyncIProcessExecHelper(getSession()) {
            @Override
            public int exec() {
                NSession session = getSession();
                NExecCommand execCommand = getExecCommand();
                String[] executorOptions = execCommand.getExecutorOptions().toArray(new String[0]);
                RemoteConnexionStringInfo k = RemoteConnexionStringInfo.of(execCommand.getTarget(), session);
                String[] remoteCommand = k.buildEffectiveCommand(cmd, execCommand.getRunAs(), executorOptions, commExec, session);
                try(DefaultNExecCommandExtensionContext d=new DefaultNExecCommandExtensionContext(
                        execCommand.getTarget(),
                        remoteCommand,
                        session,
                        in,
                        out,
                        err
                )) {
                    return commExec.exec(d);
                }catch (IOException ex){
                    throw new NExecutionException(session, NMsg.ofC("command failed :%s", ex), ex);
                }
            }
        };
    }

    @Override
    public int execute() {
        return resolveExecHelper().exec();
    }


    @Override
    public NText getHelpText() {
        switch (NEnvs.of(getSession()).getOsFamily()) {
            case WINDOWS: {
                return NTexts.of(getSession()).ofStyled(
                        "No help available. Try " + getName() + " /help",
                        NTextStyle.error()
                );
            }
            default: {
                return
                        NTexts.of(getSession()).ofStyled(
                                "No help available. Try 'man " + getName() + "' or '" + getName() + " --help'",
                                NTextStyle.error()
                        );
            }
        }
    }

    @Override
    public String toString() {
        return getExecCommand().getRunAs() + " " + NCmdLine.of(cmd).toString();
    }

}
