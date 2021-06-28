/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.bundles.jshell;

import net.thevpc.nuts.toolbox.nsh.bundles.jshell.util.DirectoryScanner;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author thevpc
 */
public class DefaultJShellContext extends AbstractJShellContext {

    private static final JShellResult OK_RESULT = new JShellResult(0, null, null);
    private JShell shell;
    private JShellVariables vars;
    private JShellNode rootNode;
    private JShellNode parentNode;
    private InputStream in = System.in;
    private PrintStream out = System.out;
    private PrintStream err = System.err;
    private Map<String, Object> userProperties = new HashMap<>();
    private JShellFunctionManager functionManager = new DefaultJShellFunctionManager();
    private JShellAliasManager aliasManager = new DefaultJShellAliasManager();
    private JShellBuiltinManager builtinsManager;
    private String cwd=System.getProperty("user.dir");
    private JShellFileSystem fileSystem;
    public String oldCommandLine = null;
    public JShellResult lastResult = OK_RESULT;
    public JShellContext parentContext;

    public DefaultJShellContext(JShell shell) {
        this.vars = new JShellVariables(this);
        this.shell=shell;
        setFileSystem(new DefaultJShellFileSystem());
    }

    @Override
    public JShellBuiltinManager builtins() {
        if(builtinsManager==null){
            builtinsManager=new DefaultJShellCommandManager();
        }
        return builtinsManager;
    }

    @Override
    public void setBuiltins(JShellBuiltinManager builtinsManager) {
        this.builtinsManager = builtinsManager;
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
        this.parentContext=other;
        copyFrom(other);
    }

    @Override
    public JShellContext getParentContext() {
        return parentContext;
    }

    @Override
    public JShellAliasManager aliases() {
        return aliasManager;
    }

    public void setAliases(JShellAliasManager aliasManager) {
        this.aliasManager = aliasManager == null ? new DefaultJShellAliasManager() : aliasManager;
    }

    public void copyFrom(JShellContext other) {
        if (other != null) {
            this.shell = other.getShell();
            this.vars = other.vars();
            this.functionManager = other.functions();
            this.aliasManager = other.aliases();
            this.builtinsManager = other.builtins();
            this.rootNode = other.getRootNode();
            this.parentNode = other.getParentNode();
            this.in = other.in();
            this.out = other.out();
            this.err = other.err();
            this.userProperties = new HashMap<>();
            this.userProperties.putAll(other.getUserProperties());
            setFileSystem(other.getFileSystem());
            this.cwd = other.getCwd();
            this.parentContext = other.getParentContext();
        }
    }

//    public JShellContext copy() {
//        DefaultJShellContext c = new DefaultJShellContext(shell);
//        c.copyFrom(this);
//        return c;
//    }

    @Override
    public void setFileSystem(JShellFileSystem fileSystem) {
        this.fileSystem = fileSystem;
        setCwd(this.fileSystem.getInitialWorkingDir());
    }

    @Override
    public JShellFunctionManager functions() {
        return functionManager;
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return userProperties;
    }

    @Override
    public JShell getShell() {
        return shell;
    }

    @Override
    public JShellNode getRootNode() {
        return rootNode;
    }

    @Override
    public JShellNode getParentNode() {
        return parentNode;
    }

    @Override
    public InputStream in() {
        return in;
    }

    @Override
    public PrintStream out() {
        return out;
    }

    @Override
    public PrintStream err() {
        return err;
    }

    @Override
    public JShellVariables vars() {
        return vars;
    }


    public JShellContext setRoot(JShellNode root) {
        this.rootNode = root;
        return this;
    }

    @Override
    public JShellContext setParentNode(JShellNode parent) {
        this.parentNode = parent;
        return this;
    }

    public JShellContext setIn(InputStream in) {
        this.in = in == null ? System.in : in;
        return this;
    }

    public JShellContext setOut(PrintStream out) {
        this.out = out == null ? System.out : out;
        return this;
    }

    public JShellContext setErr(PrintStream err) {
        this.err = err == null ? System.err : err;
        return this;
    }

    public JShellExecutionContext createCommandContext(JShellBuiltin command, JShellFileContext context) {
        return new DefaultJShellExecutionContext(context);
    }

    @Override
    public List<JShellAutoCompleteCandidate> resolveAutoCompleteCandidates(String commandName, List<String> autoCompleteWords, int wordIndex, String autoCompleteLine, JShellFileContext ctx) {
        return new ArrayList<>();
    }

    @Override
    public JShellContext setEnv(Map<String, String> env) {
        if (env != null) {
            this.vars.set(env);
        }
        return this;
    }

    public void setFunctionManager(JShellFunctionManager functionManager) {
        this.functionManager = functionManager == null ? new DefaultJShellFunctionManager() : functionManager;
    }

    @Override
    public void setCwd(String cwd) {
        JShellFileSystem fs = getFileSystem();
        if(cwd==null || cwd.isEmpty()){
            this.cwd = fs.getHomeWorkingDir();
        }else {
            String r =
                    fs.isAbsolute(cwd)?cwd:
                    fs.getAbsolutePath(this.cwd + "/" + cwd);
            if(fs.exists(r)) {
                if(fs.isDirectory(r)) {
                    this.cwd = r;
                }else{
                    throw new IllegalArgumentException("not a directory : "+cwd);
                }
            }else{
                throw new IllegalArgumentException("no such file or directory : "+cwd);
            }
        }
    }

    @Override
    public String getCwd() {
        return cwd;
    }

    @Override
    public JShellFileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public String getAbsolutePath(String path) {
        if (new File(path).isAbsolute()) {
            return getFileSystem().getAbsolutePath(path);
        }
        return getFileSystem().getAbsolutePath(getCwd() + "/" + path);
    }

    @Override
    public String[] expandPaths(String path) {
        return new DirectoryScanner(path).toArray();
    }

    @Override
    public JShellResult getLastResult() {
        return lastResult;
    }

    @Override
    public void setLastResult(JShellResult lastResult) {
        this.lastResult = lastResult == null ? OK_RESULT : lastResult;
    }


    @Override
    public Watcher bindStreams(InputStream out, InputStream err, OutputStream in) {
        WatcherImpl w=new WatcherImpl();
        new Thread(() -> {
            byte[] buffer=new byte[4024];
            int x;
            boolean some=false;
            while(true){
                if(out!=null) {
                    try {
                        if (out.available() > 0) {
                            x = out.read(buffer);
                            if (x > 0) {
                                out().write(buffer, 0, x);
                                some=true;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(err!=null) {
                    try {
                        if (err.available() > 0) {
                            x = err.read(buffer);
                            if (x > 0) {
                                err().write(buffer, 0, x);
                                some=true;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(in!=null) {
                    try {
                        if (in().available() > 0) {
                            x = in().read(buffer);
                            if (x > 0) {
                                in.write(buffer, 0, x);
                                some=true;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(!some && w.askStopped){
                    break;
                }
            }
        }).start();
        return w;
    }
    
    public class WatcherImpl implements Watcher{
        boolean stopped;
        boolean askStopped;
        int threads;

        @Override
        public void stop() {
            if(!askStopped){
                askStopped=true;
            }
        }

        @Override
        public boolean isStopped() {
            return stopped;
        }
    }

}
