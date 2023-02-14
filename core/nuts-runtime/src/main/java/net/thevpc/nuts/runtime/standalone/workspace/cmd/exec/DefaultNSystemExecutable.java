/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.executor.system.ProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author thevpc
 */
public class DefaultNSystemExecutable extends AbstractNExecutableCommand {

    String[] cmd;
    List<String> executorOptions;
    NSession session;
    NSession execSession;
    NExecCommand execCommand;
    private boolean showCommand = false;
    private final boolean inheritSystemIO;

    public DefaultNSystemExecutable(String[] cmd,
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
        NCmdLine commandLine = NCmdLine.of(this.executorOptions);
        while (commandLine.hasNext()) {
            NArg aa = commandLine.peek().get(session);
            switch (aa.key()) {
                case "--show-command": {
                    commandLine.withNextFlag((v, a, s) -> this.showCommand = (v));
                    break;
                }
                default: {
                    commandLine.skip();
                }
            }
        }
    }

    @Override
    public NId getId() {
        return null;
    }

    private ProcessExecHelper resolveExecHelper() {
        Map<String, String> e2 = null;
        Map<String, String> env1 = execCommand.getEnv();
        if (env1 != null) {
            e2 = new HashMap<>((Map) env1);
        }
        return ProcessExecHelper.ofArgs(null,
                execCommand.getCommand().toArray(new String[0]), e2,
                execCommand.getDirectory() == null ? null : NPath.of(execCommand.getDirectory(), session).toFile(),
                session.getTerminal(),
                execSession.getTerminal(), showCommand, true, execCommand.getSleepMillis(),
                inheritSystemIO,
                /*redirectErr*/ false,
                /*fileIn*/ execCommand.getRedirectInputFile(),
                /*fileOut*/ execCommand.getRedirectOutputFile(),
                execCommand.getRunAs(),
                session);
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
