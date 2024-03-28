/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.eval;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLineAutoComplete;
import net.thevpc.nuts.toolbox.nsh.alias.DefaultNShellAliasManager;
import net.thevpc.nuts.toolbox.nsh.alias.NShellAliasManager;
import net.thevpc.nuts.toolbox.nsh.cmds.DefaultNShellCommandManager;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinManager;
import net.thevpc.nuts.toolbox.nsh.cmds.NBuiltinManager;
import net.thevpc.nuts.toolbox.nsh.fs.DefaultNShellFileSystem;
import net.thevpc.nuts.toolbox.nsh.fs.NShellFileSystem;
import net.thevpc.nuts.toolbox.nsh.nshell.NShell;
import net.thevpc.nuts.toolbox.nsh.nodes.DefaultNShellFunctionManager;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellFunctionManager;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellNode;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellVariables;

import java.util.*;

/**
 * @author thevpc
 */
public class DefaultNShellContext extends AbstractNShellContext {

    private static final NShellResult OK_RESULT = new NShellResult(0, null, null);
    public String oldCmdLine = null;
    public NShellResult lastResult = OK_RESULT;
    public NShellContext parentContext;
    public int commandLineIndex = -1;
    private NShell shell;
    private NShellVariables vars;
    private NShellNode rootNode;
    private NShellNode parentNode;
    private Map<String, Object> userProperties = new HashMap<>();
    private NShellFunctionManager functionManager = new DefaultNShellFunctionManager();
    private NShellAliasManager aliasManager = new DefaultNShellAliasManager();
    private NShellBuiltinManager builtinManager;
    private String cwd = System.getProperty("user.dir");
    private NShellFileSystem fileSystem;
    private NCmdLineAutoComplete autoComplete;
    public DefaultNShellContext(NShell shell, NShellNode rootNode, NShellNode parentNode,
                                NShellContext parentContext, NWorkspace workspace, NSession session, NShellVariables vars,
                                String serviceName, String[] args
    ) {
        this(parentContext);
        workspace=(workspace != null ? workspace : parentContext != null ? parentContext.getWorkspace() : null);
        if (session == null) {
            if (workspace != null) {
                session = getWorkspace().createSession();
            }
        }
        setSession(session);
        setServiceName(serviceName);
        setArgs(args);
        this.vars = new NShellVariables(this);
        this.shell = shell;
        setFileSystem(new DefaultNShellFileSystem());
        if (parentContext != null) {
            setDirectory(parentContext.getDirectory());
        }
        setRootNode(rootNode);
        setParentNode(parentNode);
        if (parentContext != null) {
            vars().set(parentContext.vars());
            setBuiltins(parentContext.builtins());
            for (String a : parentContext.aliases().getAll()) {
                aliases().set(a, parentContext.aliases().get(a));
            }
        } else {
            for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
                vars().export(entry.getKey(), entry.getValue());
            }
            setBuiltins(new NBuiltinManager());
            NShellAliasManager a = aliases();
            a.set(".", "source");
            a.set("[", "test");

            a.set("ll", "ls");
            a.set("..", "cd ..");
            a.set("...", "cd ../..");
        }
        if (vars != null) {
            for (Map.Entry<Object, Object> entry : vars.getAll().entrySet()) {
                vars().set((String) entry.getKey(), (String) entry.getValue());
            }
        }

        this.parentContext = parentContext;//.copy();
        if (parentContext != null) {
            setDirectory(parentContext.getDirectory());
        }
    }

    //    public DefaultNShellContext(NShell shell, NShellFunctionManager functionManager, NShellAliasManager aliasManager,NShellVariables env, NShellNode root, NShellNode parent, InputStream in, PrintStream out, PrintStream err, String... args) {
//        setShell(shell);
//        setVars(env);
//        setAliases(aliasManager);
//        setFunctionManager(functionManager);
//        setRoot(root);
//        setParent(parent);
//        setIn(in);
//        setOut(out);
//        setErr(err);
//        setArgs(args);
//    }
    public DefaultNShellContext(NShellContext other) {
        this.parentContext = other;
        setAll(other);
    }

    @Override
    public NShell getShell() {
        return shell;
    }

    @Override
    public NShellNode getRootNode() {
        return rootNode;
    }

    public NShellContext setRootNode(NShellNode root) {
        this.rootNode = root;
        return this;
    }

    @Override
    public NShellNode getParentNode() {
        return parentNode;
    }

    @Override
    public NShellContext setParentNode(NShellNode parent) {
        this.parentNode = parent;
        return this;
    }


//    public NShellContext copy() {
//        DefaultNShellContext c = new DefaultNShellContext(shell);
//        c.copyFrom(this);
//        return c;
//    }

    @Override
    public NShellVariables vars() {
        return vars;
    }



    @Override
    public NShellFunctionManager functions() {
        return functionManager;
    }


    @Override
    public NShellContext setEnv(Map<String, String> env) {
        if (env != null) {
            this.vars.set(env);
        }
        return this;
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return userProperties;
    }

    @Override
    public String getDirectory() {
        return cwd;
    }

    @Override
    public String getHome() {
        return System.getProperty("user.home");
    }

    @Override
    public void setDirectory(String cwd) {
        NShellFileSystem fs = getFileSystem();
        if (cwd == null || cwd.isEmpty()) {
            this.cwd = fs.getHomeWorkingDir(getSession());
        } else {
            String r =
                    fs.isAbsolute(cwd,getSession()) ? cwd :
                            fs.getAbsolutePath(this.cwd + "/" + cwd,getSession());
            if (fs.exists(r,getSession())) {
                if (fs.isDirectory(r,getSession())) {
                    this.cwd = r;
                } else {
                    throw new IllegalArgumentException("not a directory : " + cwd);
                }
            } else {
                throw new IllegalArgumentException("no such file or directory : " + cwd);
            }
        }
    }

    @Override
    public NShellFileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public void setFileSystem(NShellFileSystem fileSystem) {
        this.fileSystem = fileSystem;
        setDirectory(this.fileSystem.getInitialWorkingDir(getSession()));
    }

    @Override
    public NShellContext getParentContext() {
        return parentContext;
    }

    @Override
    public NShellAliasManager aliases() {
        return aliasManager;
    }

    @Override
    public void setBuiltins(NShellBuiltinManager builtinsManager) {
        this.builtinManager = builtinsManager;
    }

    @Override
    public NShellBuiltinManager builtins() {
        if (builtinManager == null) {
            builtinManager = new DefaultNShellCommandManager();
        }
        return builtinManager;
    }

    @Override
    public NShellResult getLastResult() {
        return lastResult;
    }

    @Override
    public void setLastResult(NShellResult lastResult) {
        this.lastResult = lastResult == null ? OK_RESULT : lastResult;
    }

    public void setAliases(NShellAliasManager aliasManager) {
        this.aliasManager = aliasManager == null ? new DefaultNShellAliasManager() : aliasManager;
    }

    public void setAll(NShellContext other) {
        if (other != null) {
            super.setAll(other);
            this.shell = other.getShell();
            this.vars = other.vars();
            this.userProperties = new HashMap<>();
            this.userProperties.putAll(other.getUserProperties());
            this.parentContext = other.getParentContext();
        }
    }


    @Override
    public NCmdLineAutoComplete getAutoComplete() {
        return autoComplete;
    }

    @Override
    public void setAutoComplete(NCmdLineAutoComplete autoComplete) {
        this.autoComplete = autoComplete;
    }

    public void setFunctionManager(NShellFunctionManager functionManager) {
        this.functionManager = functionManager == null ? new DefaultNShellFunctionManager() : functionManager;
    }


}
