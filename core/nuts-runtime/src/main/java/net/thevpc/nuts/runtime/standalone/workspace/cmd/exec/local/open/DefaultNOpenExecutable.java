/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.open;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.boot.NBootCompleteRequest;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;

import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.command.NExecutableType;
import net.thevpc.nuts.platform.NEnv;
import net.thevpc.nuts.runtime.standalone.executor.system.NSysExecUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.AbstractNExecutableInformationExt;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.text.NMsg;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author thevpc
 */
public class DefaultNOpenExecutable extends AbstractNExecutableInformationExt {

    String[] cmd;
    private boolean showCommand = false;
    private String[] effectiveOpenExecutable;

    public DefaultNOpenExecutable(String[] cmd,
                                  List<String> executorOptions, NExec execCommand
    ) {
        super(cmd[0],
                NCmdLine.of(cmd).toString(),
                NExecutableType.SYSTEM, execCommand);
        this.cmd = cmd;
        this.executorOptions = executorOptions;

        NCmdLine.of(this.executorOptions).matcher()
                .with("--show-command").matchFlag(a->this.showCommand = (a.booleanValue()))
                .with("--nuts-exec-mode").matchFlag(a->this.completeRequest = NBootCompleteRequest.parseOrNull(a.stringValue()))
                .withAny().skip()
                .requireAll();

        switch (NEnv.of().osFamily()) {
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
    public NId id() {
        return null;
    }

    private NExec resolveExecHelper() {
        if (effectiveOpenExecutable == null) {
            throw new NIllegalArgumentException(NMsg.ofC("unable to resolve viewer for %s", cmd[0]));
        }
        NExec cc = getExecCommand().copy();
        cc.system();
        List<String> ss = new ArrayList<>(Arrays.asList(effectiveOpenExecutable));
        ss.addAll(Arrays.asList(cmd));
        cc.command(ss);
        return cc;
    }

    @Override
    public int execute() {
        if(completeRequest!=null){
            return 0;
        }
        return resolveExecHelper().run().exitCode();
    }

    @Override
    public NText helpText() {
        switch (NEnv.of().osFamily()) {
            case WINDOWS: {
                return NText.ofStyled("No help available. Try " + name() + " /help", NTextStyle.error());
            }
            default: {
                return NText.ofStyled("No help available. Try 'man " + name() + "' or '" + name() + " --help'", NTextStyle.error());
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
