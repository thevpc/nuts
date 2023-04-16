/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.executor.AbstractSyncIProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

import java.util.List;

/**
 * @author thevpc
 */
public class DefaultNSystemExecutableRemote extends AbstractNExecutableCommand {

    String[] cmd;
    List<String> executorOptions;
    NSession session;
    NSession execSession;
    NExecCommand execCommand;
    private boolean showCommand = false;
    private final boolean inheritSystemIO;
    private NExecCommandExtension commExec;

    public DefaultNSystemExecutableRemote(NExecCommandExtension commExec, String[] cmd,
                                          List<String> executorOptions, NSession session, NSession execSession, NExecCommand execCommand) {
        super(cmd[0],
                NCmdLine.of(cmd).toString(),
                NExecutableType.SYSTEM);
        this.inheritSystemIO = execCommand.isInheritSystemIO();
        this.cmd = cmd;
        this.execCommand = execCommand;
        this.executorOptions = CoreCollectionUtils.nonNullList(executorOptions);
        this.session = session;
        this.execSession = execSession;
        this.commExec = commExec;
        NCmdLine cmdLine = NCmdLine.of(this.executorOptions);
        while (cmdLine.hasNext()) {
            NArg aa = cmdLine.peek().get(session);
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


        return new AbstractSyncIProcessExecHelper(session) {
            @Override
            public int exec() {
                return commExec.exec(new DefaultNExecCommandExtensionContext(
                        execCommand.getTarget(),
                        cmd, execSession
                ));
            }
        };
    }


    @Override
    public void execute() {
        resolveExecHelper().exec();
    }


    @Override
    public NText getHelpText() {
        switch (NEnvs.of(execSession).getOsFamily()) {
            case WINDOWS: {
                return NTexts.of(session).ofStyled(
                        "No help available. Try " + getName() + " /help",
                        NTextStyle.error()
                );
            }
            default: {
                return
                        NTexts.of(session).ofStyled(
                                "No help available. Try 'man " + getName() + "' or '" + getName() + " --help'",
                                NTextStyle.error()
                        );
            }
        }
    }

    @Override
    public String toString() {
        return execCommand.getRunAs() + " " + NCmdLine.of(cmd).toString();
    }

    @Override
    public NSession getSession() {
        return session;
    }
}
