package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsTerminal;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.javashell.cmds.CommandContext;

public interface NutsCommandContext extends CommandContext {
    NutsConsoleContext consoleContext();

    NutsWorkspace getWorkspace();

    NutsSession getSession();

    boolean configure(CommandLine cmd);

    NutsTerminal getTerminal();

    NutsJavaShell getShell();

    boolean isVerbose();
}
