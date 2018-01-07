package net.vpc.app.nuts.extensions.cmd;

import net.vpc.app.nuts.NutsCommandContext;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.apps.javashell.parser.DefaultJavaShellEvalContext;
import net.vpc.apps.javashell.parser.Env;
import net.vpc.apps.javashell.parser.JavaShellEvalContext;
import net.vpc.apps.javashell.parser.nodes.Node;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class NutsJavaShellEvalContext extends DefaultJavaShellEvalContext {
    private NutsCommandContext commandContext;
    private NutsWorkspace workspace;

    public NutsJavaShellEvalContext(JavaShellEvalContext parentContext) {
        super(parentContext);
        if(parentContext instanceof NutsJavaShellEvalContext){
            this.commandContext = ((NutsJavaShellEvalContext) parentContext).commandContext;
            this.workspace = ((NutsJavaShellEvalContext) parentContext).workspace;
            this.commandContext.getUserProperties().put(JavaShellEvalContext.class.getName(),this);
        }
    }
    public NutsJavaShellEvalContext(NutsJavaShell shell, String[] args, Node root, Node parent, NutsCommandContext commandContext, NutsWorkspace workspace, Env env) {
        super(shell, env, root, parent, null, null, null, args);
        this.commandContext = commandContext;
        this.workspace = workspace;
    }

    public NutsCommandContext getCommandContext() {
        return commandContext;
    }

    @Override
    public InputStream getStdIn() {
        return commandContext.getTerminal().getIn();
    }

    @Override
    public OutputStream getStdOut() {
        return commandContext.getTerminal().getOut();
    }

    @Override
    public OutputStream getStdErr() {
        return commandContext.getTerminal().getErr();
    }


    @Override
    public JavaShellEvalContext setOut(OutputStream out) {
        try {
            commandContext.getTerminal().setOut(workspace.createEnhancedPrintStream(out));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Override
    public JavaShellEvalContext setIn(InputStream in) {
        commandContext.getTerminal().setIn(in);
        return this;
    }
}
