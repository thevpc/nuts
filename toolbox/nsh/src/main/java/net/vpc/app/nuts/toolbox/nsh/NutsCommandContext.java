package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.NutsCommand;
import net.vpc.common.javashell.cmds.JShellCommandContext;

public interface NutsCommandContext extends JShellCommandContext {

    @Override
    NutsConsoleContext shellContext();

    NutsWorkspace getWorkspace();
    
    void printObject(Object any);

    NutsSession getSession();

    boolean configureFirst(NutsCommand cmd);

    NutsSessionTerminal getTerminal();

    @Override
    NutsJavaShell getShell();

    boolean isVerbose();

    void setTerminalMode(NutsTerminalMode outMode);
}
