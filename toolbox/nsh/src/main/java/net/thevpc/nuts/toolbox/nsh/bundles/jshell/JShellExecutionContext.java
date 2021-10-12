package net.thevpc.nuts.toolbox.nsh.bundles.jshell;

import net.thevpc.nuts.*;

import java.io.InputStream;
import java.io.PrintStream;

public interface JShellExecutionContext {

    JShell getShell();

    InputStream in();

    NutsPrintStream out();

    NutsPrintStream err();

    JShellVariables vars();


    NutsWorkspace getWorkspace();

    NutsSession getSession();

    boolean configureFirst(NutsCommandLine cmd);

    void configureLast(NutsCommandLine cmd);

    JShellContext getShellContext();

    NutsApplicationContext getAppContext();

}
