package net.thevpc.nuts.toolbox.nsh.jshell;

import net.thevpc.nuts.*;

import java.io.InputStream;

public interface JShellExecutionContext {

    JShell getShell();

    InputStream in();

    NutsPrintStream out();

    NutsPrintStream err();

    NutsWorkspace getWorkspace();

    NutsSession getSession();

    boolean configureFirst(NutsCommandLine cmd);

    boolean isAskVersion();

    void configureLast(NutsCommandLine cmd);

    JShellContext getShellContext();

    NutsApplicationContext getAppContext();

    boolean isAskHelp();

    <T> T getOptions();

    JShellExecutionContext setOptions(Object options);
}
