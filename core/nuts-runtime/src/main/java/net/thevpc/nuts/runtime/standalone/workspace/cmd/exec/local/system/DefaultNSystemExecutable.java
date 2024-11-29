/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.system;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.env.NEnvs;
import net.thevpc.nuts.runtime.standalone.executor.system.ProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.util.ExtraApiUtils;
import net.thevpc.nuts.util.NCoreCollectionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.AbstractNExecutableInformationExt;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author thevpc
 */
public class DefaultNSystemExecutable extends AbstractNExecutableInformationExt {

    String[] cmd;
    List<String> executorOptions;
    private boolean showCommand = false;

    public DefaultNSystemExecutable(NWorkspace workspace,String[] cmd,
                                    List<String> executorOptions,
                                    NExecCmd execCommand) {
        super(workspace,cmd[0],
                NCmdLine.of(cmd).toString(),
                NExecutableType.SYSTEM, execCommand);
        this.cmd = cmd;
        this.executorOptions = NCoreCollectionUtils.nonNullList(executorOptions);
        NCmdLine cmdLine = NCmdLine.of(this.executorOptions);
        while (cmdLine.hasNext()) {
            NArg aa = cmdLine.peek().get();
            switch (aa.key()) {
                case "--show-command": {
                    cmdLine.withNextFlag((v, a) -> this.showCommand = (v));
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

    private ProcessExecHelper resolveExecHelper() {
        Map<String, String> e2 = null;
        NExecCmd execCommand = getExecCommand();
        Map<String, String> env1 = execCommand.getEnv();
        if (env1 != null) {
            e2 = new HashMap<>((Map) env1);
        }
        return ProcessExecHelper.ofArgs(null,
                execCommand.getCommand().toArray(new String[0]), e2,
                execCommand.getDirectory() == null ? null : execCommand.getDirectory().toPath().get(),
                showCommand, true,
                execCommand.getSleepMillis(),
                execCommand.getIn(),
                execCommand.getOut(),
                execCommand.getErr(),
                execCommand.getRunAs(),
                executorOptions.toArray(new String[0]),
                ExtraApiUtils.asBooleanOr(execCommand.getDry(),NSession.get().isDry()), workspace
        );
    }


    @Override
    public int execute() {
        return resolveExecHelper().exec();
    }


    @Override
    public NText getHelpText() {
        switch (NEnvs.of().getOsFamily()) {
            case WINDOWS: {
                return NText.ofStyled(
                        "No help available. Try " + getName() + " /help",
                        NTextStyle.error()
                );
            }
            default: {
                return
                        NText.ofStyled(
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
