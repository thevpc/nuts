package net.thevpc.nuts.toolbox.nsh.eval;


import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineAutoComplete;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.toolbox.nsh.alias.NShellAliasManager;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinManager;
import net.thevpc.nuts.toolbox.nsh.fs.NShellFileSystem;
import net.thevpc.nuts.toolbox.nsh.nshell.NShell;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellFunctionManager;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellNode;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellVariables;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;

public class DefaultNShellExecutionContext implements NShellExecutionContext {


    private NShellContext shellContext;
    private NSession session;
    private NShellBuiltin builtin;
    private NTerminalMode terminalMode = null;
    private boolean askHelp;
    private boolean askVersion;
    private Object options;

    public DefaultNShellExecutionContext(NShellContext shellContext, NShellBuiltin command) {
        this.shellContext = shellContext;
        //each execution has its very own session!
        this.session = shellContext.getSession().copy();
        this.builtin = command;
    }


    @Override
    public NWorkspace getWorkspace() {
        return shellContext.getWorkspace();
    }

    @Override
    public NSession getSession() {
        return session;
    }


    @Override
    public NShell getShell() {
        return shellContext.getShell();
    }

    @Override
    public NPrintStream out() {
        return getSession().out();
    }

    @Override
    public NPrintStream err() {
        return getSession().err();
    }

    @Override
    public InputStream in() {
        return getSession().in();
    }


    @Override
    public NShellContext getShellContext() {
        return shellContext;
    }
    @Override
    public boolean configureFirst(NCmdLine cmd) {
        NArg a = cmd.peek().get(session);
        if (a == null) {
            return false;
        }
        switch(a.key()) {
            case "--help": {
                cmd.skip();
                setAskHelp(true);
                while(cmd.hasNext()){
                    getSession().configureLast(cmd);
                }
                break;
            }
            case "--version": {
                cmd.skip();
                setAskVersion(true);
                while(cmd.hasNext()){
                    getSession().configureLast(cmd);
                }
                break;
//                cmd.skip();
//                if (cmd.isExecMode()) {
//                    out().print(NMsg.ofC("%s%n", NutsIdResolver.of(getSession()).resolveId(getClass()).getVersion().toString()));
//                    cmd.skipAll();
//                }
//                throw new NutsExecutionException(shellContext.getSession(), NMsg.ofC("Help"), 0);
            }
            default: {
                if (getSession() != null && getSession().configureFirst(cmd)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isAskVersion() {
        return askVersion;
    }

    public DefaultNShellExecutionContext setAskVersion(boolean askVersion) {
        this.askVersion = askVersion;
        return this;
    }

    @Override
    public void configureLast(NCmdLine cmd) {
        if (!configureFirst(cmd)) {
            cmd.throwUnexpectedArgument();
        }
    }

    @Override
    public NApplicationContext getAppContext() {
        return getShell().getAppContext();
    }

    @Override
    public NTerminalMode geTerminalMode() {
        return terminalMode;
    }

    @Override
    public boolean isAskHelp() {
        return askHelp;
    }

    @Override
    public DefaultNShellExecutionContext setAskHelp(boolean askHelp) {
        this.askHelp = askHelp;
        return this;
    }

    public <T> T getOptions() {
        return (T) options;
    }

    public DefaultNShellExecutionContext setOptions(Object options) {
        this.options = options;
        return this;
    }

    @Override
    public NShellNode getRootNode() {
        return shellContext.getRootNode();
    }

    @Override
    public NShellNode getParentNode() {
        return shellContext.getParentNode();
    }

    @Override
    public NShellVariables vars() {
        return shellContext.vars();
    }

    @Override
    public NShellFunctionManager functions() {
        return shellContext.functions();
    }

    @Override
    public NShellExecutionContext setOut(PrintStream out) {
        getSession().getTerminal().setErr(NPrintStream.of(out, getSession()));
        return this;
    }

    @Override
    public NShellExecutionContext setErr(PrintStream err) {
        getSession().getTerminal().setErr(NPrintStream.of(err, getSession()));
        return this;
    }

    @Override
    public NShellExecutionContext setIn(InputStream in) {
        getSession().getTerminal().setIn(in);
        return this;
    }

    @Override
    public NShellExecutionContext setEnv(Map<String, String> env) {
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
    public String getHome() {
        return shellContext.getHome();
    }

    @Override
    public void setCwd(String cwd) {
        shellContext.setCwd(cwd);
    }

    @Override
    public NShellFileSystem getFileSystem() {
        return shellContext.getFileSystem();
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
    public NShellContext getParentContext() {
        return shellContext.getParentContext();
    }

    @Override
    public NShellAliasManager aliases() {
        return shellContext.aliases();
    }

    @Override
    public NShellBuiltinManager builtins() {
        return shellContext.builtins();
    }

    @Override
    public String getServiceName() {
        return shellContext.getServiceName();
    }

//    public void setArgs(String[] args) {
//        shellContext.setArgs(args);
//    }
//
//    public String getArg(int index) {
//        return shellContext.getArg(index);
//    }
//
//    public int getArgsCount() {
//        return shellContext.getArgsCount();
//    }
//
//    public String[] getArgsArray() {
//        return shellContext.getArgsArray();
//    }
//
//    public List<String> getArgsList() {
//        return shellContext.getArgsList();
//    }

    @Override
    public NShellExecutionContext setSession(NSession session) {
        this.session=session;
        return this;
    }

    @Override
    public NCmdLineAutoComplete getAutoComplete() {
        return shellContext.getAutoComplete();
    }
}
