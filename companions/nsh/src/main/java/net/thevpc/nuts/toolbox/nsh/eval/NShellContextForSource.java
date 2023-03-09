package net.thevpc.nuts.toolbox.nsh.eval;

import net.thevpc.nuts.cmdline.NCmdLineAutoComplete;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.toolbox.nsh.alias.NShellAliasManager;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinManager;
import net.thevpc.nuts.toolbox.nsh.fs.NShellFileSystem;
import net.thevpc.nuts.toolbox.nsh.nshell.NShell;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellFunctionManager;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellNode;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellVariables;

import java.util.Map;

public class NShellContextForSource extends AbstractNShellContext {
    private NShellContext other;

    public NShellContextForSource(NShellContext other) {
        this.other = other;
    }

    @Override
    public NShell getShell() {
        return other.getShell();
    }

    @Override
    public NShellNode getRootNode() {
        return other.getRootNode();
    }

    @Override
    public NShellContext setRootNode(NShellNode root) {
        other.setRootNode(root);
        return this;
    }

    @Override
    public NSession getSession() {
        NSession s = super.getSession();
        if(s!=null){
            return s;
        }
        return other.getSession();
    }

    @Override
    public NWorkspace getWorkspace() {
        return getSession().getWorkspace();
    }

    @Override
    public NShellNode getParentNode() {
        return other.getParentNode();
    }

    @Override
    public NShellContext setParentNode(NShellNode parent) {
        other.setParentNode(parent);
        return this;
    }

    @Override
    public NShellVariables vars() {
        return other.vars();
    }

    @Override
    public NShellFunctionManager functions() {
        return other.functions();
    }

    @Override
    public NShellContext setEnv(Map<String, String> env) {
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
    public NShellFileSystem getFileSystem() {
        return other.getFileSystem();
    }

    @Override
    public void setFileSystem(NShellFileSystem fileSystem) {
        other.setFileSystem(fileSystem);
    }

    @Override
    public NShellContext getParentContext() {
        return other.getParentContext();
    }

    @Override
    public NShellAliasManager aliases() {
        return other.aliases();
    }

    @Override
    public void setBuiltins(NShellBuiltinManager commandManager) {
        other.setBuiltins(commandManager);
    }

    @Override
    public NShellBuiltinManager builtins() {
        return other.builtins();
    }

    @Override
    public NShellResult getLastResult() {
        return other.getLastResult();
    }

    @Override
    public void setLastResult(NShellResult result) {
        other.setLastResult(result);
    }

    @Override
    public void setAliases(NShellAliasManager aliasManager) {
        other.setAliases(aliasManager);
    }

    @Override
    public NCmdLineAutoComplete getAutoComplete() {
        return other.getAutoComplete();
    }

    @Override
    public void setAutoComplete(NCmdLineAutoComplete value) {
        other.setAutoComplete(value);
    }

    @Override
    public void setFunctionManager(NShellFunctionManager functionManager) {
        other.setFunctionManager(functionManager);
    }
}
