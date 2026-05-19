/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.system;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;

import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.command.NExecutableType;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.platform.NEnv;
import net.thevpc.nuts.runtime.standalone.executor.system.ProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.util.ExtraApiUtils;
import net.thevpc.nuts.util.NCollections;
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

    public DefaultNSystemExecutable(String[] cmd,
                                    List<String> executorOptions,
                                    NExec execCommand) {
        super(cmd[0],
                NCmdLine.of(cmd).toString(),
                NExecutableType.SYSTEM, execCommand);
        this.cmd = cmd;
        this.executorOptions = NCollections.nonNullList(executorOptions);
        NCmdLine cmdLine = NCmdLine.of(this.executorOptions);
        while (cmdLine.hasNext()) {
            NArg aa = cmdLine.peek().get();
            switch (aa.key()) {
                case "--show-command": {
                    cmdLine.matcher().matchFlag((v) -> this.showCommand = (v.booleanValue())).anyMatch();
                    break;
                }
                default: {
                    cmdLine.skip();
                }
            }
        }
    }

    @Override
    public NId id() {
        return null;
    }

    private ProcessExecHelper resolveExecHelper() {
        Map<String, String> e2 = null;
        NExec execCommand = getExecCommand();
        Map<String, String> env1 = execCommand.env();
        if (env1 != null) {
            e2 = new HashMap<>((Map) env1);
        }
        return ProcessExecHelper.ofArgs(null,
                execCommand.command().toArray(new String[0]), e2,
                execCommand.directory() == null ? null : execCommand.directory().toPath().get(),
                showCommand, true,
                execCommand.sleepDuration(),
                execCommand.in(),
                execCommand.out(),
                execCommand.err(),
                execCommand.runAs(),
                executorOptions.toArray(new String[0]),
                ExtraApiUtils.asBooleanOr(execCommand.dry(), NSession.of().isDry())
        );
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
