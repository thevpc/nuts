package net.vpc.app.nuts.extensions.cmd;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.NutsTerminalDelegate;
import net.vpc.apps.javashell.cmds.JavaShellInternalCmd;
import net.vpc.apps.javashell.parser.JavaShellEvalContext;

class NutsShellInternalCmd implements JavaShellInternalCmd {
    private final NutsCommand ncommand;
    private final  DefaultNutsCommandLineConsoleComponent component;

    public NutsShellInternalCmd(NutsCommand ncommand, DefaultNutsCommandLineConsoleComponent component) {
        this.ncommand = ncommand;
        this.component = component;
    }

    @Override
    public int exec(String[] command, JavaShellEvalContext shell) throws Exception {
        NutsJavaShellEvalContext ncontext=(NutsJavaShellEvalContext) shell;
        NutsCommandContext commandContext = ncontext.getCommandContext();
        NutsSession session = component.getContext().getSession().copy();
        session.setTerminal(new NutsTerminalDelegate(
                component.getContext().getTerminal(),
                shell.getStdIn(),
                component.getContext().getWorkspace().createEnhancedPrintStream(shell.getStdOut()),
                component.getContext().getWorkspace().createEnhancedPrintStream(shell.getStdErr())

        ));
        commandContext.setSession(session);
        commandContext.setEnv(shell.getEnv().getEnv());
        ncommand.exec(command, commandContext);
        return 0;
    }

    @Override
    public String getHelp() {
        return ncommand.getHelp();
    }

    @Override
    public String getName() {
        return ncommand.getName();
    }

    @Override
    public String getHelpHeader() {
        return ncommand.getHelpHeader();
    }
}
