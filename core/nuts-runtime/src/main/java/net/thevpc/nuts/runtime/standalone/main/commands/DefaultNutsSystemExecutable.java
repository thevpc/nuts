/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.main.commands;

import java.util.*;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.core.app.DefaultNutsCommandLine;

/**
 * @author thevpc
 */
public class DefaultNutsSystemExecutable extends AbstractNutsExecutableCommand {

    String[] cmd;
    String[] executorOptions;
    NutsSession traceSession;
    NutsSession execSession;
    private boolean showCommand = false;
    private boolean root;
    NutsExecCommand execCommand;

    public DefaultNutsSystemExecutable(String[] cmd, String[] executorOptions,NutsSession traceSession, NutsSession execSession, NutsExecCommand execCommand, boolean root) {
        super(cmd[0],
                execSession.getWorkspace().commandLine().create(cmd).toString(),
                NutsExecutableType.SYSTEM);
        this.cmd = cmd;
        this.root = root;
        this.execCommand = execCommand;
        this.executorOptions = executorOptions == null ? new String[0] : executorOptions;
        this.traceSession = traceSession;
        this.execSession = execSession;
        NutsCommandLine cmdLine = execSession.getWorkspace().commandLine().create(this.executorOptions);
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
            NutsSession session=NutsWorkspaceUtils.of(execSession.getWorkspace()).validateSession(execCommand.getSession());
            switch (session.getWorkspace().env().getOsFamily()) {
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
                    String s = (String) session.getProperty("WINDOWS_ROOT_USER");
                    if(s==null) {
                        s = session.getWorkspace().env().get("WINDOWS_ROOT_USER", null);
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
                    throw new NutsExecutionException(session.getWorkspace(),"ROOT_CMD: Unsupported Platform "+ session.getWorkspace().env().getOsFamily(),12);
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
        NutsWorkspaceUtils.of(execSession.getWorkspace()).execAndWait(resolveUserOrRootCommand(), e2, CoreIOUtils.toPath(execCommand.getDirectory()),
                traceSession.getTerminal(),
                execSession.getTerminal(), showCommand, true, execCommand.getSleepMillis(), traceSession)
                .exec();
    }

    @Override
    public void dryExecute() {
        Map<String, String> e2 = null;
        Map<String, String> env1 = execCommand.getEnv();
        if (env1 != null) {
            e2 = new HashMap<>((Map) env1);
        }
        NutsWorkspaceUtils.of(execSession.getWorkspace()).execAndWait(resolveUserOrRootCommand(), e2, CoreIOUtils.toPath(execCommand.getDirectory()), 
                traceSession.getTerminal(),
                execSession.getTerminal(), 
                showCommand, true, execCommand.getSleepMillis(), traceSession)
                .dryExec();
    }

    @Override
    public String getHelpText() {
        switch (execSession.getWorkspace().env().getOsFamily()) {
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
            return "ROOT_CMD " + execSession.getWorkspace().commandLine().create(cmd).toString();
        } else {
            return "USER_CMD " + execSession.getWorkspace().commandLine().create(cmd).toString();
        }
    }

}
