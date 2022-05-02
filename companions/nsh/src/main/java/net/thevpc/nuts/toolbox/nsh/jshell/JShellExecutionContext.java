package net.thevpc.nuts.toolbox.nsh.jshell;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsCommandAutoComplete;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.io.NutsTerminalMode;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;

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

    NutsTerminalMode geTerminalMode();

    boolean isAskHelp();

    DefaultJShellExecutionContext setAskHelp(boolean askHelp);

    <T> T getOptions();

    JShellExecutionContext setOptions(Object options);

    JShellNode getRootNode();

    JShellNode getParentNode();

    JShellVariables vars();

    JShellFunctionManager functions();

    JShellExecutionContext setOut(PrintStream out);

    JShellExecutionContext setErr(PrintStream err);

    JShellExecutionContext setIn(InputStream in);

    JShellExecutionContext setEnv(Map<String, String> env);

    Map<String, Object> getUserProperties();

    String getCwd();

    String getHome();

    void setCwd(String cwd);

    JShellFileSystem getFileSystem();

    String getAbsolutePath(String path);

    String[] expandPaths(String path);

    JShellContext getParentContext();

    JShellAliasManager aliases();

    JShellBuiltinManager builtins();

    String getServiceName();

    JShellExecutionContext setSession(NutsSession session);

    NutsCommandAutoComplete getAutoComplete();
}
