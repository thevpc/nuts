/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.open;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.executor.system.NSysExecUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.AbstractNExecutableInformationExt;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NMsg;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author thevpc
 */
public class DefaultNOpenExecutable extends AbstractNExecutableInformationExt {

    String[] cmd;
    String[] executorOptions;
    private boolean showCommand = false;
    private String[] effectiveOpenExecutable;

    public DefaultNOpenExecutable(NWorkspace workspace,String[] cmd,
                                  String[] executorOptions, NExecCmd execCommand
    ) {
        super(workspace,cmd[0],
                NCmdLine.of(cmd).toString(),
                NExecutableType.SYSTEM, execCommand);
        this.cmd = cmd;
        this.executorOptions = executorOptions == null ? new String[0] : executorOptions;
        NCmdLine cmdLine = NCmdLine.of(this.executorOptions);
        NSession session = workspace.currentSession();
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
        switch (NEnvs.of().getOsFamily()) {
            case LINUX: {
                Path execPath = NSysExecUtils.sysWhich("xdg-open");
                if (execPath != null) {
                    effectiveOpenExecutable = new String[]{execPath.toString()};
                    break;
                }
                execPath = NSysExecUtils.sysWhich("gnome-open");
                if (execPath != null) {
                    effectiveOpenExecutable = new String[]{execPath.toString()};
                    break;
                }
                execPath = NSysExecUtils.sysWhich("cygstart");
                if (execPath != null) {
                    effectiveOpenExecutable = new String[]{execPath.toString()};
                    break;
                }
                break;
            }
            case WINDOWS: {
                effectiveOpenExecutable = new String[]{"cmd", "/c", "start"};
                break;
            }
            case MACOS: {
                Path execPath = NSysExecUtils.sysWhich("open");
                if (execPath != null) {
                    effectiveOpenExecutable = new String[]{execPath.toString()};
                }
                break;
            }
        }
    }

    @Override
    public NId getId() {
        return null;
    }

    private NExecCmd resolveExecHelper() {
        if (effectiveOpenExecutable == null) {
            NSession session = workspace.currentSession();
            throw new NIllegalArgumentException(NMsg.ofC("unable to resolve viewer for %s", cmd[0]));
        }
        NExecCmd cc = getExecCommand().copy();
        cc.system();
        List<String> ss = new ArrayList<>(Arrays.asList(effectiveOpenExecutable));
        ss.addAll(Arrays.asList(cmd));
        cc.setCommand(ss);
        return cc;
    }

    @Override
    public int execute() {
        return resolveExecHelper().run().getResultCode();
    }

    @Override
    public NText getHelpText() {
        NSession session = workspace.currentSession();
        switch (NEnvs.of().getOsFamily()) {
            case WINDOWS: {
                return NText.ofStyled("No help available. Try " + getName() + " /help", NTextStyle.error());
            }
            default: {
                return NText.ofStyled("No help available. Try 'man " + getName() + "' or '" + getName() + " --help'", NTextStyle.error());
            }
        }
    }

    @Override
    public String toString() {
        if (effectiveOpenExecutable == null) {
            return "open --fail " + NCmdLine.of(cmd);
        }
        return "open --with " + effectiveOpenExecutable[0] + " " + NCmdLine.of(cmd);
    }

}
