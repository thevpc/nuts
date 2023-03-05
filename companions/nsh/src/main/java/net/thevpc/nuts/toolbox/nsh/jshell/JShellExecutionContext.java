package net.thevpc.nuts.toolbox.nsh.jshell;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineAutoComplete;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.toolbox.nsh.alias.JShellAliasManager;
import net.thevpc.nuts.toolbox.nsh.cmds.JShellBuiltinManager;
import net.thevpc.nuts.toolbox.nsh.fs.JShellFileSystem;
import net.thevpc.nuts.toolbox.nsh.nodes.JShellFunctionManager;
import net.thevpc.nuts.toolbox.nsh.nodes.JShellNode;
import net.thevpc.nuts.toolbox.nsh.nodes.JShellVariables;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;

public interface JShellExecutionContext {

    JShell getShell();

    InputStream in();

    NPrintStream out();

    NPrintStream err();

    NWorkspace getWorkspace();

    NSession getSession();

    boolean configureFirst(NCmdLine cmd);

    boolean isAskVersion();

    void configureLast(NCmdLine cmd);

    JShellContext getShellContext();

    NApplicationContext getAppContext();

    NTerminalMode geTerminalMode();

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

    JShellExecutionContext setSession(NSession session);

    NCmdLineAutoComplete getAutoComplete();
}
