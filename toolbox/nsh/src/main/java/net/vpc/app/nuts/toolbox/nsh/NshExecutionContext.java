package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.app.nuts.*;
import net.vpc.common.javashell.JShellExecutionContext;
import net.vpc.app.nuts.NutsCommandLine;

public interface NshExecutionContext extends JShellExecutionContext {

    @Override
    NutsShellContext getGlobalContext();

    NutsWorkspace getWorkspace();

    NutsSession session();

    NutsSession getSession();

    boolean configureFirst(NutsCommandLine cmd);

    @Override
    NutsJavaShell getShell();
}
