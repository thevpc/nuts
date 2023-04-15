/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.executor.system.NSysExecUtils;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author thevpc
 */
public class DefaultNOpenExecutable extends AbstractNExecutableCommand {

    String[] cmd;
    String[] executorOptions;
    NSession session;
    NSession execSession;
    NExecCommand execCommand;
    private boolean showCommand = false;
    private String[] effectiveOpenExecutable;

    public DefaultNOpenExecutable(String[] cmd,
                                  String[] executorOptions, NSession session, NSession execSession, NExecCommand execCommand
    ) {
        super(cmd[0],
                NCmdLine.of(cmd).toString(),
                NExecutableType.SYSTEM);
        this.cmd = cmd;
        this.execCommand = execCommand;
        this.executorOptions = executorOptions == null ? new String[0] : executorOptions;
        this.session = session;
        this.execSession = execSession;
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
        switch (NEnvs.of(session).getOsFamily()) {
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

    private NExecCommand resolveExecHelper() {
        if (effectiveOpenExecutable == null) {
            throw new NIllegalArgumentException(session, NMsg.ofC("unable to resolve viewer for %s", cmd[0]));
        }
        NExecCommand cc = execCommand.copy();
        cc.setExecutionType(NExecutionType.SYSTEM);
        List<String> ss = new ArrayList<>(Arrays.asList(effectiveOpenExecutable));
        ss.addAll(Arrays.asList(cmd));
        cc.setCommand(ss);
        return cc;
    }

    @Override
    public void execute() {
        resolveExecHelper().run();
    }

    @Override
    public NText getHelpText() {
        switch (NEnvs.of(execSession).getOsFamily()) {
            case WINDOWS: {
                return NTexts.of(session).ofStyled("No help available. Try " + getName() + " /help", NTextStyle.error());
            }
            default: {
                return NTexts.of(session).ofStyled("No help available. Try 'man " + getName() + "' or '" + getName() + " --help'", NTextStyle.error());
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

    @Override
    public NSession getSession() {
        return session;
    }
}
