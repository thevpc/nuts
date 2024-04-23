package net.thevpc.nuts.toolbox.ntemplate.project;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NSessionTerminal;
import net.thevpc.nuts.toolbox.nsh.eval.NShellContext;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellVar;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellVarListener;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellVariables;
import net.thevpc.nuts.toolbox.nsh.nshell.NShell;
import net.thevpc.nuts.toolbox.nsh.nshell.NShellConfiguration;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.ExprEvaluator;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.FileTemplater;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.ProcessCmd;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.util.StringUtils;

class NshEvaluator implements ExprEvaluator {
    private final NSession session;
    private final NShell shell;
    private final FileTemplater fileTemplater;

    public NshEvaluator(NSession session, FileTemplater fileTemplater) {
        this.session = session;
        this.fileTemplater = fileTemplater;
        shell = new NShell(new NShellConfiguration().setSession(session)
                .setIncludeDefaultBuiltins(true).setIncludeExternalExecutor(true)
                .setArgs()
        );
        NShellContext rootContext = shell.getRootContext();
        rootContext.setSession(rootContext.getSession().copy());
        rootContext.vars().addVarListener(
                new NShellVarListener() {
                    @Override
                    public void varAdded(NShellVar nShellVar, NShellVariables vars, NShellContext context) {
                        setVar(nShellVar.getName(), nShellVar.getValue());
                    }

                    @Override
                    public void varValueUpdated(NShellVar nShellVar, String oldValue, NShellVariables vars, NShellContext context) {
                        setVar(nShellVar.getName(), nShellVar.getValue());
                    }

                    @Override
                    public void varRemoved(NShellVar nShellVar, NShellVariables vars, NShellContext context) {
                        setVar(nShellVar.getName(), null);
                    }
                }
        );
        rootContext
                .builtins()
                .set(new ProcessCmd(fileTemplater));
    }

    public void setVar(String varName, String newValue) {
        fileTemplater.getLog().debug("eval", varName + "=" + StringUtils.toLiteralString(newValue));
        fileTemplater.setVar(varName, newValue);
    }

    @Override
    public Object eval(String content, FileTemplater context) {
        NShellContext ctx = shell.createInlineContext(shell.getRootContext(), context.getSourcePath().orElse("nsh"), new String[0]);
        NSession session = context.getSession().copy();
        session.setTerminal(NSessionTerminal.ofMem(session));
        ctx.setSession(session);
        shell.executeScript(content, ctx);
        return session.out().toString();
    }

    @Override
    public String toString() {
        return "nsh";
    }
}
