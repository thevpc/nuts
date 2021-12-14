package net.thevpc.nuts.toolbox.nsh.jshell;

import net.thevpc.nuts.NutsCommandAutoComplete;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;

import java.util.Map;

public class JShellContextForSource extends AbstractJShellContext{
    private JShellContext other;

    public JShellContextForSource(JShellContext other) {
        this.other = other;
    }

    @Override
    public JShell getShell() {
        return other.getShell();
    }

    @Override
    public JShellNode getRootNode() {
        return other.getRootNode();
    }

    @Override
    public JShellContext setRootNode(JShellNode root) {
        other.setRootNode(root);
        return this;
    }

    @Override
    public NutsSession getSession() {
        NutsSession s = super.getSession();
        if(s!=null){
            return s;
        }
        return other.getSession();
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return getSession().getWorkspace();
    }

    @Override
    public JShellNode getParentNode() {
        return other.getParentNode();
    }

    @Override
    public JShellContext setParentNode(JShellNode parent) {
        other.setParentNode(parent);
        return this;
    }

    @Override
    public JShellVariables vars() {
        return other.vars();
    }

    @Override
    public JShellFunctionManager functions() {
        return other.functions();
    }

    @Override
    public JShellContext setEnv(Map<String, String> env) {
        other.setEnv(env);
        return this;
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return other.getUserProperties();
    }

    @Override
    public String getCwd() {
        return other.getCwd();
    }

    @Override
    public String getHome() {
        return other.getHome();
    }

    @Override
    public void setCwd(String cwd) {
        other.setCwd(cwd);
    }

    @Override
    public JShellFileSystem getFileSystem() {
        return other.getFileSystem();
    }

    @Override
    public void setFileSystem(JShellFileSystem fileSystem) {
        other.setFileSystem(fileSystem);
    }

    @Override
    public JShellContext getParentContext() {
        return other.getParentContext();
    }

    @Override
    public JShellAliasManager aliases() {
        return other.aliases();
    }

    @Override
    public void setBuiltins(JShellBuiltinManager commandManager) {
        other.setBuiltins(commandManager);
    }

    @Override
    public JShellBuiltinManager builtins() {
        return other.builtins();
    }

    @Override
    public JShellResult getLastResult() {
        return other.getLastResult();
    }

    @Override
    public void setLastResult(JShellResult result) {
        other.setLastResult(result);
    }

    @Override
    public void setAliases(JShellAliasManager aliasManager) {
        other.setAliases(aliasManager);
    }

    @Override
    public NutsCommandAutoComplete getAutoComplete() {
        return other.getAutoComplete();
    }

    @Override
    public void setAutoComplete(NutsCommandAutoComplete value) {
        other.setAutoComplete(value);
    }

    @Override
    public void setFunctionManager(JShellFunctionManager functionManager) {
        other.setFunctionManager(functionManager);
    }
}
