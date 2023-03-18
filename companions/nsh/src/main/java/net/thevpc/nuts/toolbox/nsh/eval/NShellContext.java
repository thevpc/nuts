package net.thevpc.nuts.toolbox.nsh.eval;

import net.thevpc.nuts.cmdline.NCmdLineAutoComplete;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.toolbox.nsh.alias.NShellAliasManager;
import net.thevpc.nuts.toolbox.nsh.autocomplete.NShellAutoCompleteCandidate;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinManager;
import net.thevpc.nuts.toolbox.nsh.fs.NShellFileSystem;
import net.thevpc.nuts.toolbox.nsh.nshell.NShell;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellFunctionManager;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellNode;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellVariables;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

/**
 * Created by vpc on 11/4/16.
 */
public interface NShellContext {

    NShell getShell();

    NShellNode getRootNode();

    NShellContext setRootNode(NShellNode root);

    NShellNode getParentNode();

    NShellContext setParentNode(NShellNode parent);

    InputStream in();

    NPrintStream out();

    NPrintStream err();

    NShellVariables vars();

    Watcher bindStreams(InputStream out, InputStream err, OutputStream in);


    NShellFunctionManager functions();


    NShellContext setOut(PrintStream out);

    NShellContext setErr(PrintStream out);

    NShellContext setIn(InputStream in);

    NShellExecutionContext createCommandContext(NShellBuiltin command);

    List<NShellAutoCompleteCandidate> resolveAutoCompleteCandidates(String commandName, List<String> autoCompleteWords, int wordIndex, String autoCompleteLine);

    NShellContext setEnv(Map<String, String> env);

    Map<String, Object> getUserProperties();

    String getDirectory();

    String getHome();

    void setDirectory(String cwd);

    NShellFileSystem getFileSystem();

    void setFileSystem(NShellFileSystem fileSystem);

    String getAbsolutePath(String path);

    String[] expandPaths(String path);

    NShellContext getParentContext();

    NShellAliasManager aliases();

    void setBuiltins(NShellBuiltinManager commandManager);

    NShellBuiltinManager builtins();

    NShellResult getLastResult();

    void setLastResult(NShellResult result);

    void setAliases(NShellAliasManager aliasManager);

    void setAll(NShellContext other);

    String getServiceName();

    void setServiceName(String serviceName);

    void setArgs(String[] args);

    String getArg(int index);

    int getArgsCount();

    String[] getArgsArray();

    List<String> getArgsList();

//    NShellContext copy() ;

    NSession getSession();

    NShellContext setSession(NSession session);

    NWorkspace getWorkspace();

    NCmdLineAutoComplete getAutoComplete();

    void setAutoComplete(NCmdLineAutoComplete value);

    void setFunctionManager(NShellFunctionManager functionManager);

    interface Watcher {
        void stop();

        boolean isStopped();
    }

}
