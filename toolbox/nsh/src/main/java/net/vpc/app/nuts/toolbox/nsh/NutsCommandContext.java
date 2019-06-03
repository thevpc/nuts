package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.NutsCommand;
import net.vpc.common.javashell.JShellCommandContext;

public interface NutsCommandContext extends JShellCommandContext {

    @Override
    NutsShellContext getGlobalContext();

    NutsWorkspace getWorkspace();
    
    NutsCommandContext printOutObject(Object any);
    
    NutsCommandContext printErrObject(Object any);

    NutsSession getSession();

    boolean configureFirst(NutsCommand cmd);

    NutsSessionTerminal getTerminal();

    @Override
    NutsJavaShell getShell();

    boolean isVerbose();

    NutsCommandContext setTerminalMode(NutsTerminalMode outMode);
}
