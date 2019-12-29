/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.main.commands;

import java.nio.file.Paths;
import java.util.*;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.app.DefaultNutsCommandLine;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;
import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;

/**
 * @author vpc
 */
public class DefaultNutsSystemExecutable extends AbstractNutsExecutableCommand {

    String[] cmd;
    String[] executorOptions;
    NutsSession session;
    private boolean showCommand = false;
    private boolean root;
    NutsExecCommand execCommand;

    public DefaultNutsSystemExecutable(String[] cmd, String[] executorOptions, NutsSession session, NutsExecCommand execCommand, boolean root) {
        super(cmd[0],
                session.getWorkspace().commandLine().create(cmd).toString(),
                NutsExecutableType.SYSTEM);
        this.cmd = cmd;
        this.root = root;
        this.execCommand = execCommand;
        this.executorOptions = executorOptions == null ? new String[0] : executorOptions;
        this.session = session;
        NutsCommandLine cmdLine = session.getWorkspace().commandLine().create(this.executorOptions);
        while (cmdLine.hasNext()) {
            NutsArgument a = cmdLine.peek();
            switch (a.getStringKey()) {
                case "--show-command": {
                    showCommand = cmdLine.nextBoolean().getBooleanValue();
                    break;
                }
                default: {
                    cmdLine.skip();
                }
            }
        }
    }

    @Override
    public NutsId getId() {
        return null;
    }

    private String[] resolveUserOrRootCommand() {
        if (root) {
            switch (execCommand.getSession().getWorkspace().config().getOsFamily()) {
                case LINUX:
                case UNIX:
                case MACOS:
                    {
                    List<String> a = new ArrayList<>();
                    a.addAll(Arrays.asList("su","-c"));
                    a.addAll(Arrays.asList(execCommand.getCommand()));
                    return a.toArray(new String[0]);
                }
                case WINDOWS:{
                    String s = (String)execCommand.getSession().getProperty("WINDOWS_ROOT_USER");
                    if(s==null) {
                        s = execCommand.getSession().getWorkspace().config().getEnv("WINDOWS_ROOT_USER", null);
                    }
                    if(CoreStringUtils.isBlank(s)){
                        s="Administrator";
                    }
                    List<String> a = new ArrayList<>();
                    a.addAll(Arrays.asList("runas","/user:"+s));
                    a.add(new DefaultNutsCommandLine(Arrays.asList(execCommand.getCommand())).toString());
                    return a.toArray(new String[0]);
                }
                default:{
                    throw new NutsExecutionException(execCommand.getSession().workspace(),"ROOT_CMD: Unsupported Platform "+execCommand.getSession().getWorkspace().config().getOsFamily(),12);
                }
            }
        } else {
            return execCommand.getCommand();
        }
    }

    @Override
    public void execute() {
        Map<String, String> e2 = null;
        Map<String, String> env1 = execCommand.getEnv();
        if (env1 != null) {
            e2 = new HashMap<>((Map) env1);
        }
        NutsWorkspaceUtils.of(session.getWorkspace()).execAndWait(resolveUserOrRootCommand(), e2, CoreIOUtils.toPath(execCommand.getDirectory()), session.getTerminal(), showCommand, true)
                .exec();
    }

    @Override
    public void dryExecute() {
        Map<String, String> e2 = null;
        Map<String, String> env1 = execCommand.getEnv();
        if (env1 != null) {
            e2 = new HashMap<>((Map) env1);
        }
        NutsWorkspaceUtils.of(session.getWorkspace()).execAndWait(resolveUserOrRootCommand(), e2, CoreIOUtils.toPath(execCommand.getDirectory()), session.getTerminal(), showCommand, true)
                .dryExec();
    }

    @Override
    public String getHelpText() {
        switch (session.getWorkspace().config().getOsFamily()) {
            case WINDOWS: {
                return "No help available. Try " + getName() + " /help";
            }
            default: {
                return "No help available. Try 'man " + getName() + "' or '" + getName() + " --help'";
            }
        }
    }

    @Override
    public String toString() {
        if (root) {
            return "ROOT_CMD " + session.workspace().commandLine().create(cmd).toString();
        } else {
            return "USER_CMD " + session.workspace().commandLine().create(cmd).toString();
        }
    }

}
