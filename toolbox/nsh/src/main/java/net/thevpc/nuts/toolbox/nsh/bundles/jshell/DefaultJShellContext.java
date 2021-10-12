/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.bundles.jshell;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nsh.NshBuiltin;
import net.thevpc.nuts.toolbox.nsh.NutsBuiltinManager;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.util.DirectoryScanner;

import java.io.*;
import java.util.*;

/**
 * @author thevpc
 */
public class DefaultJShellContext extends AbstractJShellContext {

    private static final JShellResult OK_RESULT = new JShellResult(0, null, null);
    public String oldCommandLine = null;
    public JShellResult lastResult = OK_RESULT;
    public JShellContext parentContext;
    public int commandLineIndex = -1;
    private JShell shell;
    private JShellVariables vars;
    private JShellNode rootNode;
    private JShellNode parentNode;
    private Map<String, Object> userProperties = new HashMap<>();
    private JShellFunctionManager functionManager = new DefaultJShellFunctionManager();
    private JShellAliasManager aliasManager = new DefaultJShellAliasManager();
    private JShellBuiltinManager builtinManager;
    private String cwd = System.getProperty("user.dir");
    private JShellFileSystem fileSystem;
    private NutsCommandAutoComplete autoComplete;
    public DefaultJShellContext(JShell shell, JShellNode rootNode, JShellNode parentNode,
                                JShellContext parentContext, NutsWorkspace workspace, NutsSession session, JShellVariables vars,
                                String serviceName, String[] args
    ) {
        this(parentContext);
        setServiceName(serviceName);
        setArgs(args);
        this.vars = new JShellVariables(this);
        this.shell = shell;
        setFileSystem(new DefaultJShellFileSystem());
        if (parentContext != null) {
            setCwd(parentContext.getCwd());
        }
        workspace=(workspace != null ? workspace : parentContext != null ? parentContext.getWorkspace() : null);
        if (session == null) {
            if (workspace != null) {
                session = getWorkspace().createSession();
            }
        }
        setSession(session);
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
            setBuiltins(new NutsBuiltinManager());
            JShellAliasManager a = aliases();
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
            setCwd(parentContext.getCwd());
        }
    }

    //    public DefaultJShellContext(JShell shell, JShellFunctionManager functionManager, JShellAliasManager aliasManager,JShellVariables env, JShellNode root, JShellNode parent, InputStream in, PrintStream out, PrintStream err, String... args) {
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
    public DefaultJShellContext(JShellContext other) {
        this.parentContext = other;
        copyFrom(other);
    }

    @Override
    public JShell getShell() {
        return shell;
    }

    @Override
    public JShellNode getRootNode() {
        return rootNode;
    }

    public JShellContext setRootNode(JShellNode root) {
        this.rootNode = root;
        return this;
    }

    @Override
    public JShellNode getParentNode() {
        return parentNode;
    }

    @Override
    public JShellContext setParentNode(JShellNode parent) {
        this.parentNode = parent;
        return this;
    }


//    public JShellContext copy() {
//        DefaultJShellContext c = new DefaultJShellContext(shell);
//        c.copyFrom(this);
//        return c;
//    }

    @Override
    public JShellVariables vars() {
        return vars;
    }



    @Override
    public JShellFunctionManager functions() {
        return functionManager;
    }


    @Override
    public JShellContext setEnv(Map<String, String> env) {
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
    public String getCwd() {
        return cwd;
    }

    @Override
    public void setCwd(String cwd) {
        JShellFileSystem fs = getFileSystem();
        if (cwd == null || cwd.isEmpty()) {
            this.cwd = fs.getHomeWorkingDir();
        } else {
            String r =
                    fs.isAbsolute(cwd) ? cwd :
                            fs.getAbsolutePath(this.cwd + "/" + cwd);
            if (fs.exists(r)) {
                if (fs.isDirectory(r)) {
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
    public JShellFileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public void setFileSystem(JShellFileSystem fileSystem) {
        this.fileSystem = fileSystem;
        setCwd(this.fileSystem.getInitialWorkingDir());
    }

    @Override
    public JShellContext getParentContext() {
        return parentContext;
    }

    @Override
    public JShellAliasManager aliases() {
        return aliasManager;
    }

    @Override
    public void setBuiltins(JShellBuiltinManager builtinsManager) {
        this.builtinManager = builtinsManager;
    }

    @Override
    public JShellBuiltinManager builtins() {
        if (builtinManager == null) {
            builtinManager = new DefaultJShellCommandManager();
        }
        return builtinManager;
    }

    @Override
    public JShellResult getLastResult() {
        return lastResult;
    }

    @Override
    public void setLastResult(JShellResult lastResult) {
        this.lastResult = lastResult == null ? OK_RESULT : lastResult;
    }

    public void setAliases(JShellAliasManager aliasManager) {
        this.aliasManager = aliasManager == null ? new DefaultJShellAliasManager() : aliasManager;
    }

    public void copyFrom(JShellContext other) {
        if (other != null) {
            super.copyFrom(other);
            this.shell = other.getShell();
            this.vars = other.vars();
            this.userProperties = new HashMap<>();
            this.userProperties.putAll(other.getUserProperties());
            this.parentContext = other.getParentContext();
        }
    }


    @Override
    public NutsCommandAutoComplete getAutoComplete() {
        return autoComplete;
    }

    @Override
    public void setAutoComplete(NutsCommandAutoComplete autoComplete) {
        this.autoComplete = autoComplete;
    }

    public void setFunctionManager(JShellFunctionManager functionManager) {
        this.functionManager = functionManager == null ? new DefaultJShellFunctionManager() : functionManager;
    }


}
