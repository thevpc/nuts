/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.NutsExecCommand;
import net.thevpc.nuts.NutsExecutableType;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.runtime.standalone.executor.system.ProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author thevpc
 */
public class DefaultNutsSystemExecutable extends AbstractNutsExecutableCommand {

    String[] cmd;
    List<String> executorOptions;
    NutsSession session;
    NutsSession execSession;
    NutsExecCommand execCommand;
    private boolean showCommand = false;
    private final boolean inheritSystemIO;

    public DefaultNutsSystemExecutable(String[] cmd,
                                       List<String> executorOptions, NutsSession session, NutsSession execSession, NutsExecCommand execCommand) {
        super(cmd[0],
                NutsCommandLine.of(cmd).toString(),
                NutsExecutableType.SYSTEM);
        this.inheritSystemIO = execCommand.isInheritSystemIO();
        this.cmd = cmd;
        this.execCommand = execCommand;
        this.executorOptions = CoreCollectionUtils.nonNullList(executorOptions);
        this.session = session;
        this.execSession = execSession;
        NutsCommandLine cmdLine = NutsCommandLine.of(this.executorOptions);
        while (cmdLine.hasNext()) {
            NutsArgument a = cmdLine.peek().get(session);
            switch(a.getStringKey().orElse("")) {
                case "--show-command": {
                    showCommand = cmdLine.nextBooleanValueLiteral().get(session);
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

    private ProcessExecHelper resolveExecHelper() {
        Map<String, String> e2 = null;
        Map<String, String> env1 = execCommand.getEnv();
        if (env1 != null) {
            e2 = new HashMap<>((Map) env1);
        }
        return ProcessExecHelper.ofArgs(null,
                execCommand.getCommand().toArray(new String[0]), e2,
                execCommand.getDirectory()==null?null: NutsPath.of(execCommand.getDirectory(),session).toFile(),
                session.getTerminal(),
                execSession.getTerminal(), showCommand, true, execCommand.getSleepMillis(),
                inheritSystemIO,
                /*redirectErr*/ false,
                /*fileIn*/ null,
                /*fileOut*/ null,
                execCommand.getRunAs(),
                session);
    }





    @Override
    public void execute() {
        resolveExecHelper().exec();
    }

    @Override
    public void dryExecute() {
        resolveExecHelper().dryExec();
    }

    @Override
    public String getHelpText() {
        switch (execSession.env().getOsFamily()) {
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
        return execCommand.getRunAs() + " " + NutsCommandLine.of(cmd).toString();
    }

}
