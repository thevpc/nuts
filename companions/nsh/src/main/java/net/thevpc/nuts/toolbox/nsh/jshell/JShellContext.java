package net.thevpc.nuts.toolbox.nsh.jshell;

import net.thevpc.nuts.cmdline.NCommandAutoComplete;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;

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

    JShellContext setRootNode(JShellNode root);

    JShellNode getParentNode();

    JShellContext setParentNode(JShellNode parent);

    InputStream in();

    NPrintStream out();

    NPrintStream err();

    JShellVariables vars();

    Watcher bindStreams(InputStream out, InputStream err, OutputStream in);


    JShellFunctionManager functions();


    JShellContext setOut(PrintStream out);

    JShellContext setErr(PrintStream out);

    JShellContext setIn(InputStream in);

    JShellExecutionContext createCommandContext(JShellBuiltin command);

    List<JShellAutoCompleteCandidate> resolveAutoCompleteCandidates(String commandName, List<String> autoCompleteWords, int wordIndex, String autoCompleteLine);

    JShellContext setEnv(Map<String, String> env);

    Map<String, Object> getUserProperties();

    String getCwd();

    String getHome();

    void setCwd(String cwd);

    JShellFileSystem getFileSystem();

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

    void setAll(JShellContext other);

    String getServiceName();

    void setServiceName(String serviceName);

    void setArgs(String[] args);

    String getArg(int index);

    int getArgsCount();

    String[] getArgsArray();

    List<String> getArgsList();

//    JShellContext copy() ;

    NSession getSession();

    JShellContext setSession(NSession session);

    NWorkspace getWorkspace();

    NCommandAutoComplete getAutoComplete();

    void setAutoComplete(NCommandAutoComplete value);

    void setFunctionManager(JShellFunctionManager functionManager);

    interface Watcher {
        void stop();

        boolean isStopped();
    }

}
