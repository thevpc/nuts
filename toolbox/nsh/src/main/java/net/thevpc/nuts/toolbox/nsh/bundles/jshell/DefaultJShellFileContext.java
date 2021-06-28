package net.thevpc.nuts.toolbox.nsh.bundles.jshell;


import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DefaultJShellFileContext implements JShellFileContext {
    private JShellContext shellContext;
    private String serviceName;
    private List<String> args=new ArrayList<>();
    public int commandLineIndex = -1;

    public DefaultJShellFileContext(JShellContext shellContext, String serviceName, String[] args) {
        this.shellContext = shellContext;
        this.serviceName = serviceName;
        this.args.addAll(Arrays.asList(args));
    }

    @Override
    public JShellContext getShellContext() {
        return shellContext;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public void setServiceName(String serviceName) {
        this.serviceName=serviceName;
    }

    @Override
    public void setArgs(String[] args) {
        this.args.clear();
        this.args.addAll(Arrays.asList(args));
    }

    @Override
    public String getArg(int index) {
        List<String> argsList = getArgsList();
        if(index>=0 && index<argsList.size()) {
            String r = argsList.get(index);
            return r==null?"":r;
        }
        return "";
    }

    @Override
    public int getArgsCount() {
        return args.size();
    }

    @Override
    public String[] getArgsArray() {
        return args.toArray(new String[0]);
    }

    @Override
    public List<String> getArgsList() {
        return args;
    }

    @Override
    public JShell getShell() {
        return shellContext.getShell();
    }

    @Override
    public JShellNode getRoot() {
        return shellContext.getRootNode();
    }

    @Override
    public JShellFileContext setRoot(JShellNode root) {
        shellContext.setRoot(root);
        return this;
    }

    @Override
    public JShellNode getParent() {
        return shellContext.getParentNode();
    }

    @Override
    public JShellFileContext setParent(JShellNode parent) {
        shellContext.setParentNode(parent);
        return this;
    }

    @Override
    public InputStream in() {
        return shellContext.in();
    }

    @Override
    public PrintStream out() {
        return shellContext.out();
    }

    @Override
    public PrintStream err() {
        return shellContext.err();
    }

    @Override
    public JShellVariables vars() {
        return shellContext.vars();
    }

    @Override
    public JShellContext.Watcher bindStreams(InputStream out, InputStream err, OutputStream in) {
        return shellContext.bindStreams(out,err,in);
    }

    @Override
    public JShellFunctionManager functions() {
        return shellContext.functions();
    }

    @Override
    public JShellFileContext setOut(PrintStream out) {
        shellContext.setOut(out);
        return this;
    }

    @Override
    public JShellFileContext setErr(PrintStream out) {
        shellContext.setErr(out);
        return this;
    }

    @Override
    public JShellFileContext setIn(InputStream in) {
        shellContext.setIn(in);
        return this;
    }


    @Override
    public JShellExecutionContext createCommandContext(JShellBuiltin command) {
        return shellContext.createCommandContext(command, this);
    }

    @Override
    public List<JShellAutoCompleteCandidate> resolveAutoCompleteCandidates(String commandName, List<String> autoCompleteWords, int wordIndex, String autoCompleteLine) {
        return shellContext.resolveAutoCompleteCandidates(commandName, autoCompleteWords, wordIndex, autoCompleteLine, this);
    }

    @Override
    public JShellFileContext setEnv(Map<String, String> env) {
        shellContext.setEnv(env);
        return this;
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return shellContext.getUserProperties();
    }

    @Override
    public String getCwd() {
        return shellContext.getCwd();
    }

    @Override
    public JShellFileSystem getFileSystem() {
        return shellContext.getFileSystem();
    }

    @Override
    public void setFileSystem(JShellFileSystem fileSystem) {
        shellContext.setFileSystem(fileSystem);
    }

    @Override
    public String getAbsolutePath(String path) {
        return shellContext.getAbsolutePath(path);
    }

    @Override
    public String[] expandPaths(String path) {
        return shellContext.expandPaths(path);
    }

    @Override
    public JShellAliasManager aliases() {
        return shellContext.aliases();
    }

    @Override
    public void setBuiltins(JShellBuiltinManager commandManager) {
        shellContext.setBuiltins(commandManager);
    }

    @Override
    public JShellBuiltinManager builtins() {
        return shellContext.builtins();
    }

    @Override
    public JShellResult getLastResult() {
        return shellContext.getLastResult();
    }

    @Override
    public void setLastResult(JShellResult result) {
        shellContext.setLastResult(result);
    }

    @Override
    public void setAliases(JShellAliasManager aliasManager) {
        shellContext.setAliases(aliasManager);
    }

    @Override
    public void setCwd(String cwd) {
        shellContext.setCwd(cwd);
    }
}
