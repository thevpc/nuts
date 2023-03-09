package net.thevpc.nuts.toolbox.nsh.eval;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineAutoComplete;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.toolbox.nsh.alias.NShellAliasManager;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinManager;
import net.thevpc.nuts.toolbox.nsh.fs.NShellFileSystem;
import net.thevpc.nuts.toolbox.nsh.nshell.NShell;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellFunctionManager;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellNode;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellVariables;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;

public interface NShellExecutionContext {

    NShell getShell();

    InputStream in();

    NPrintStream out();

    NPrintStream err();

    NWorkspace getWorkspace();

    NSession getSession();

    boolean configureFirst(NCmdLine cmd);

    boolean isAskVersion();

    void configureLast(NCmdLine cmd);

    NShellContext getShellContext();

    NApplicationContext getAppContext();

    NTerminalMode geTerminalMode();

    boolean isAskHelp();

    DefaultNShellExecutionContext setAskHelp(boolean askHelp);

    <T> T getOptions();

    NShellExecutionContext setOptions(Object options);

    NShellNode getRootNode();

    NShellNode getParentNode();

    NShellVariables vars();

    NShellFunctionManager functions();

    NShellExecutionContext setOut(PrintStream out);

    NShellExecutionContext setErr(PrintStream err);

    NShellExecutionContext setIn(InputStream in);

    NShellExecutionContext setEnv(Map<String, String> env);

    Map<String, Object> getUserProperties();

    String getCwd();

    String getHome();

    void setCwd(String cwd);

    NShellFileSystem getFileSystem();

    String getAbsolutePath(String path);

    String[] expandPaths(String path);

    NShellContext getParentContext();

    NShellAliasManager aliases();

    NShellBuiltinManager builtins();

    String getServiceName();

    NShellExecutionContext setSession(NSession session);

    NCmdLineAutoComplete getAutoComplete();
}
