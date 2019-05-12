package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.common.javashell.cmds.CommandContext;
import net.vpc.app.nuts.NutsSessionTerminal;
import net.vpc.app.nuts.NutsTerminalMode;

public interface NutsCommandContext extends CommandContext {

    @Override
    NutsConsoleContext consoleContext();

    NutsWorkspace getWorkspace();

    NutsSession getSession();

    boolean configure(NutsCommandLine cmd);

    NutsSessionTerminal getTerminal();

    @Override
    NutsJavaShell getShell();

    boolean isVerbose();

    void setTerminalMode(NutsTerminalMode outMode);
}
