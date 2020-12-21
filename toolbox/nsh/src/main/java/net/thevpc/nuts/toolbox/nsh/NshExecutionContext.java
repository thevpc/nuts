package net.thevpc.nuts.toolbox.nsh;

import net.thevpc.jshell.JShellFileContext;
import net.thevpc.jshell.JShellExecutionContext;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.NutsCommandLine;

public interface NshExecutionContext extends JShellExecutionContext {

    @Override
    JShellFileContext getGlobalContext();

    NutsShellContext getNutsShellContext();

    NutsWorkspace getWorkspace();

    NutsWorkspace workspace();

    NutsSession session();

    NutsSession getSession();

    boolean configureFirst(NutsCommandLine cmd);

    @Override
    NutsJavaShell getShell();
}
