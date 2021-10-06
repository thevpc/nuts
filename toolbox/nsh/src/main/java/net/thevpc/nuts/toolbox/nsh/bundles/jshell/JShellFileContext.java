//package net.thevpc.nuts.toolbox.nsh.bundles.jshell;
//
//import net.thevpc.nuts.NutsPrintStream;
//
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.io.PrintStream;
//import java.util.List;
//import java.util.Map;
//
//public interface JShellFileContext {
//    JShellContext getShellContext();
//
//    String getServiceName();
//
//    void setServiceName(String serviceName);
//
//    void setArgs(String[] args);
//
//    String getArg(int index);
//
//    int getArgsCount();
//
//    String[] getArgsArray();
//
//    List<String> getArgsList();
//
//    JShell getShell();
//
//    JShellNode getRootNode();
//
//    JShellFileContext setRootNode(JShellNode root);
//
//    JShellNode getParentNode();
//
//    JShellFileContext setParentNode(JShellNode parent);
//
//    InputStream in();
//
//    NutsPrintStream out();
//
//    NutsPrintStream err();
//
//    JShellVariables vars();
//
//    JShellContext.Watcher bindStreams(InputStream out, InputStream err, OutputStream in);
//
//
//    JShellFunctionManager functions();
//
//
//    JShellFileContext setOut(PrintStream out);
//
//    JShellFileContext setErr(PrintStream out);
//
//    JShellFileContext setIn(InputStream in);
//
//
//    JShellExecutionContext createCommandContext(JShellBuiltin command);
//
//    List<JShellAutoCompleteCandidate> resolveAutoCompleteCandidates(String commandName, List<String> autoCompleteWords, int wordIndex, String autoCompleteLine);
//
//    JShellFileContext setEnv(Map<String, String> env);
//
//    Map<String, Object> getUserProperties();
//
//    String getCwd();
//
//    void setCwd(String cwd);
//
//    JShellFileSystem getFileSystem();
//
//    void setFileSystem(JShellFileSystem fileSystem);
//
//    String getAbsolutePath(String path);
//
//    String[] expandPaths(String path);
//
//    JShellAliasManager aliases();
//
//    void setBuiltins(JShellBuiltinManager commandManager);
//
//    JShellBuiltinManager builtins();
//
//    JShellResult getLastResult();
//
//    void setLastResult(JShellResult result);
//
//    void setAliases(JShellAliasManager aliasManager);
//}
