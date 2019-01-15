package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.javashell.cmds.CommandContext;
import net.vpc.app.nuts.NutsSessionTerminal;

public interface NutsCommandContext extends CommandContext {
    NutsConsoleContext consoleContext();

    NutsWorkspace getWorkspace();

    NutsSession getSession();

    boolean configure(CommandLine cmd);

    NutsSessionTerminal getTerminal();

    NutsJavaShell getShell();

    boolean isVerbose();
}
