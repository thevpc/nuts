/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import net.vpc.app.nuts.NutsCommandArg;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsExecCommand;
import net.vpc.app.nuts.NutsExecutableType;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsPlatformUtils;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;

/**
 *
 * @author vpc
 */
public class SystemExecutable extends AbstractExecutable {
    
    String[] cmd;
    String[] executorOptions;
    NutsSession session;
    private boolean showCommand = false;
    private boolean failFast = true;
    NutsExecCommand execCommand;
    NutsWorkspace ws;

    public SystemExecutable(String[] cmd, String[] executorOptions, NutsWorkspace ws, NutsSession session, NutsExecCommand execCommand) {
        super(cmd[0], NutsExecutableType.SYSTEM);
        this.cmd = cmd;
        this.ws = ws;
        this.execCommand = execCommand;
        this.executorOptions = executorOptions == null ? new String[0] : executorOptions;
        this.session = session;
        NutsCommandLine cmdLine = new NutsCommandLine(this.executorOptions);
        while (cmdLine.hasNext()) {
            NutsCommandArg a = cmdLine.next();
            switch (a.strKey()) {
                case "--show-command":
                    {
                        showCommand = a.getBooleanValue();
                        break;
                    }
                case "--fail-fast":
                    {
                        failFast = a.getBooleanValue();
                        break;
                    }
            }
        }
    }

    @Override
    public NutsId getId() {
        return null;
    }

    @Override
    public void execute() {
        Map<String, String> e2 = null;
        Properties env1 = execCommand.getEnv();
        if (env1 != null) {
            e2 = new HashMap<>((Map) env1);
        }
        CoreIOUtils.execAndWait(ws, execCommand.getCommand(), e2, ws.io().path(execCommand.getDirectory()), session.getTerminal(), showCommand, failFast);
    }

    @Override
    public String getHelpText() {
        switch (NutsPlatformUtils.getPlatformOsFamily()) {
            case WINDOWS:
                {
                    return "No help available. Try " + getName() + " /help";
                }
            default:
                {
                    return "No help available. Try 'man " + getName() + "' or '" + getName() + " --help'";
                }
        }
    }

    @Override
    public String toString() {
        return "SYSEXEC " + NutsCommandLine.escapeArguments(cmd);
    }
    
}
