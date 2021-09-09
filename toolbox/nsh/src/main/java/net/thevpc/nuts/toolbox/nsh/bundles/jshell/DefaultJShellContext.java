/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.bundles.jshell;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nsh.*;
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
    private Map<String, Object> userProperties = new HashMap<>();
    private JShellFunctionManager functionManager = new DefaultJShellFunctionManager();
    private JShellAliasManager aliasManager = new DefaultJShellAliasManager();
    private JShellBuiltinManager builtinsManager;
    private String cwd=System.getProperty("user.dir");
    private JShellFileSystem fileSystem;
    public String oldCommandLine = null;
    public JShellResult lastResult = OK_RESULT;
    public JShellContext parentContext;
    private NutsWorkspace workspace;
    private NutsSession session;
    //    private NutsSessionTerminal terminal;
    private NutsCommandAutoComplete autoComplete;

    public DefaultJShellContext(JShell shell, JShellNode rootNode, JShellNode parentNode,
                                JShellContext parentContext, NutsWorkspace workspace, NutsSession session, JShellVariables vars) {
        this(parentContext);
        this.vars = new JShellVariables(this);
        this.shell=shell;
        setFileSystem(new DefaultJShellFileSystem());
        if (parentContext != null) {
            setCwd(parentContext.getCwd());
        }
        this.workspace = workspace != null ? workspace : parentContext != null ? parentContext.getWorkspace() : null;
        if (session == null) {
            if (this.workspace != null) {
                session = this.workspace.createSession();
            }
        }
        this.session = session;
        setRoot(rootNode);
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
        this.workspace = workspace != null ? workspace : parentContext != null ? parentContext.getWorkspace() : null;
        if (session == null) {
            if (this.workspace != null) {
                session = this.workspace.createSession();
            }
        }
        this.session = session;
        setRoot(rootNode);
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
            this.userProperties = new HashMap<>();
            this.userProperties.putAll(other.getUserProperties());
            setFileSystem(other.getFileSystem());
            this.cwd = other.getCwd();
            this.parentContext = other.getParentContext();
            this.workspace = other.workspace();
            this.session = other.session() == null ? null : other.session().copy();
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
        return getSession().getTerminal().in();
    }

    @Override
    public NutsPrintStream out() {
        return getSession().getTerminal().getOut();
    }

    @Override
    public NutsPrintStream err() {
        return getSession().getTerminal().getErr();
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

    @Override
    public JShellContext setIn(InputStream in) {
        getSession().getTerminal().setIn(in);
        return this;
    }

    @Override
    public JShellContext setOut(PrintStream out) {
        getSession().getTerminal().setOut(
                getSession().getWorkspace().io().createPrintStream(out)
        );
//        commandContext.getTerminal().setOut(workspace.createPrintStream(out,
//                true//formatted
//        ));
        return this;
    }

    public JShellContext setErr(PrintStream err) {
        getSession().getTerminal().setErr(
                getSession().getWorkspace().io().createPrintStream(err)
        );
        return this;
    }

//    public JShellExecutionContext createCommandContext(JShellBuiltin command, JShellFileContext context) {
//        return new DefaultJShellExecutionContext(context);
//    }
    public JShellExecutionContext createCommandContext(JShellBuiltin command, JShellFileContext context) {
        DefaultJShellExecutionContext c = new DefaultJShellExecutionContext(this, (NshBuiltin) command, context);
//        c.setMode(getTerminalMode());
//        c.setVerbose(isVerbose());
        return c;
    }
    @Override
    public List<JShellAutoCompleteCandidate> resolveAutoCompleteCandidates(String commandName, List<String> autoCompleteWords, int wordIndex, String autoCompleteLine, JShellFileContext ctx) {
        JShellBuiltin command = this.builtins().find(commandName);
        NutsCommandAutoComplete autoComplete = new NutsDefaultCommandAutoComplete()
                .setSession(session).setLine(autoCompleteLine).setWords(autoCompleteWords).setCurrentWordIndex(wordIndex);

        if (command != null && command instanceof NshBuiltin) {
            ((NshBuiltin) command).autoComplete(new DefaultJShellExecutionContext(this, (NshBuiltin) command, ctx), autoComplete);
        } else {
            NutsWorkspace ws = this.getWorkspace();
            List<NutsId> nutsIds = ws.search()
                    .addId(commandName)
                    .setLatest(true)
                    .addScope(NutsDependencyScopePattern.RUN)
                    .setOptional(false)
                    .setSession(this.getSession().copy().setTrace(false).setFetchStrategy(NutsFetchStrategy.OFFLINE))
                    .getResultIds().list();
            if (nutsIds.size() == 1) {
                NutsId selectedId = nutsIds.get(0);
                NutsDefinition def = ws.search().addId(selectedId).setEffective(true).setSession(this.getSession()
                        .copy().setTrace(false).setFetchStrategy(NutsFetchStrategy.OFFLINE)).getResultDefinitions().required();
                NutsDescriptor d = def.getDescriptor();
                String nuts_autocomplete_support = NutsUtilStrings.trim(d.getPropertyValue("nuts.autocomplete"));
                if (d.isApplication()
                        || "true".equalsIgnoreCase(nuts_autocomplete_support)
                        || "supported".equalsIgnoreCase(nuts_autocomplete_support)) {
                    NutsExecCommand t = ws.exec()
                            .grabOutputString()
                            .grabErrorString()
                            .addCommand(
                                    selectedId
                                            .getLongName(),
                                    "--nuts-exec-mode=auto-complete " + wordIndex
                            )
                            .addCommand(autoCompleteWords)
                            .run();
                    if (t.getResult() == 0) {
                        String rr = t.getOutputString();
                        for (String s : rr.split("\n")) {
                            s = s.trim();
                            if (s.length() > 0) {
                                if (s.startsWith(NutsApplicationContext.AUTO_COMPLETE_CANDIDATE_PREFIX)) {
                                    s = s.substring(NutsApplicationContext.AUTO_COMPLETE_CANDIDATE_PREFIX.length()).trim();
                                    NutsCommandLineManager commandLineFormat = workspace.commandLine();
                                    NutsCommandLine args = commandLineFormat.parse(s);
                                    String value = null;
                                    String display = null;
                                    if (args.hasNext()) {
                                        value = args.next().getString();
                                        if (args.hasNext()) {
                                            display = args.next().getString();
                                        }
                                    }
                                    if (value != null) {
                                        if (display == null) {
                                            display = value;
                                        }
                                        autoComplete.addCandidate(
                                                commandLineFormat.createCandidate(
                                                        value
                                                ).build()
                                        );
                                    }
                                } else {
                                    //ignore all the rest!
                                    break;
                                }
                            }
                        }
                    }
                }
            }

        }
        List<JShellAutoCompleteCandidate> all = new ArrayList<>();
        for (NutsArgumentCandidate a : autoComplete.getCandidates()) {
            all.add(new JShellAutoCompleteCandidate(a.getValue(), a.getDisplay()));
        }
        return all;
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

    @Override
    public NutsSession session() {
        return getSession();
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public JShellContext setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NutsWorkspace workspace() {
        return getWorkspace();
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public void setWorkspace(NutsWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public NutsCommandAutoComplete getAutoComplete() {
        return autoComplete;
    }

    @Override
    public void setAutoComplete(NutsCommandAutoComplete autoComplete) {
        this.autoComplete = autoComplete;
    }

}
