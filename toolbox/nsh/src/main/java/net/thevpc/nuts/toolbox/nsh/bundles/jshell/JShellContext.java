package net.thevpc.nuts.toolbox.nsh.bundles.jshell;

import net.thevpc.nuts.NutsCommandAutoComplete;
import net.thevpc.nuts.NutsPrintStream;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

/**
 * Created by vpc on 11/4/16.
 */
public interface JShellContext {

    JShell getShell();

    JShellNode getRootNode();

    JShellContext setRoot(JShellNode root);

    JShellNode getParentNode();

    JShellContext setParentNode(JShellNode parent);

    InputStream in();

    NutsPrintStream out();

    NutsPrintStream err();

    JShellVariables vars();

    Watcher bindStreams(InputStream out, InputStream err, OutputStream in);


    JShellFunctionManager functions();


    JShellContext setOut(PrintStream out);

    JShellContext setErr(PrintStream out);

    JShellContext setIn(InputStream in);

    JShellExecutionContext createCommandContext(JShellBuiltin command, JShellFileContext context);

    List<JShellAutoCompleteCandidate> resolveAutoCompleteCandidates(String commandName, List<String> autoCompleteWords, int wordIndex, String autoCompleteLine, JShellFileContext ctx);

    JShellContext setEnv(Map<String,String> env);

    Map<String, Object> getUserProperties();

    String getCwd();

    JShellFileSystem getFileSystem();

    void setCwd(String cwd);

    void setFileSystem(JShellFileSystem fileSystem);
    
    String getAbsolutePath(String path);

    String[] expandPaths(String path);

    JShellContext getParentContext();

    JShellAliasManager aliases();

    void setBuiltins(JShellBuiltinManager commandManager);

    JShellBuiltinManager builtins();

    JShellResult getLastResult();

    void setLastResult(JShellResult result);

    void setAliases(JShellAliasManager aliasManager);

    void copyFrom(JShellContext other);

//    JShellContext copy() ;

    interface Watcher{
        void stop();
        boolean isStopped();
    }

    NutsSession session();

    NutsSession getSession();

    JShellContext setSession(NutsSession session);

    NutsWorkspace workspace();

    NutsWorkspace getWorkspace();

    void setWorkspace(NutsWorkspace workspace);

    NutsCommandAutoComplete getAutoComplete();

    void setAutoComplete(NutsCommandAutoComplete value);
}
