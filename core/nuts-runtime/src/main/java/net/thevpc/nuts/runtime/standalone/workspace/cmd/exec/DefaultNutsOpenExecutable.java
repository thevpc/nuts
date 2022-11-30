/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.runtime.standalone.executor.system.NutsSysExecUtils;
import net.thevpc.nuts.text.NutsText;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author thevpc
 */
public class DefaultNutsOpenExecutable extends AbstractNutsExecutableCommand {

    String[] cmd;
    String[] executorOptions;
    NutsSession session;
    NutsSession execSession;
    NutsExecCommand execCommand;
    private boolean showCommand = false;
    private String[] effectiveOpenExecutable;

    public DefaultNutsOpenExecutable(String[] cmd,
                                     String[] executorOptions, NutsSession session, NutsSession execSession, NutsExecCommand execCommand
    ) {
        super(cmd[0],
                NutsCommandLine.of(cmd).toString(),
                NutsExecutableType.SYSTEM);
        this.cmd = cmd;
        this.execCommand = execCommand;
        this.executorOptions = executorOptions == null ? new String[0] : executorOptions;
        this.session = session;
        this.execSession = execSession;
        NutsCommandLine commandLine = NutsCommandLine.of(this.executorOptions);
        while (commandLine.hasNext()) {
            NutsArgument aa = commandLine.peek().get(session);
            switch (aa.key()) {
                case "--show-command": {
                    commandLine.withNextBoolean((v, a, s) -> this.showCommand = (v), session);
                    break;
                }
                default: {
                    commandLine.skip();
                }
            }
        }
        switch (session.env().getOsFamily()) {
            case LINUX: {
                Path execPath = NutsSysExecUtils.sysWhich("xdg-open");
                if (execPath != null) {
                    effectiveOpenExecutable = new String[]{execPath.toString()};
                    break;
                }
                execPath = NutsSysExecUtils.sysWhich("gnome-open");
                if (execPath != null) {
                    effectiveOpenExecutable = new String[]{execPath.toString()};
                    break;
                }
                execPath = NutsSysExecUtils.sysWhich("cygstart");
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
                Path execPath = NutsSysExecUtils.sysWhich("open");
                if (execPath != null) {
                    effectiveOpenExecutable = new String[]{execPath.toString()};
                }
                break;
            }
        }
    }

    @Override
    public NutsId getId() {
        return null;
    }

    private NutsExecCommand resolveExecHelper() {
        if (effectiveOpenExecutable == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.ofCstyle("unable to resolve viewer for %s", cmd[0]));
        }
        NutsExecCommand cc = execCommand.copy();
        cc.setExecutionType(NutsExecutionType.SYSTEM);
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
    public NutsText getHelpText() {
        switch (execSession.env().getOsFamily()) {
            case WINDOWS: {
                return NutsTexts.of(session).ofStyled("No help available. Try " + getName() + " /help", NutsTextStyle.error());
            }
            default: {
                return NutsTexts.of(session).ofStyled("No help available. Try 'man " + getName() + "' or '" + getName() + " --help'", NutsTextStyle.error());
            }
        }
    }

    @Override
    public String toString() {
        if (effectiveOpenExecutable == null) {
            return "open --fail " + NutsCommandLine.of(cmd);
        }
        return "open --with " + effectiveOpenExecutable[0] + " " + NutsCommandLine.of(cmd);
    }

    @Override
    public NutsSession getSession() {
        return session;
    }
}
