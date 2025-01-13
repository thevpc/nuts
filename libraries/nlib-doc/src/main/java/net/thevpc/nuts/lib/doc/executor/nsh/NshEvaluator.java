package net.thevpc.nuts.lib.doc.executor.nsh;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NTerminal;
import net.thevpc.nuts.lib.doc.executor.NDocExprEvaluator;
import net.thevpc.nuts.lib.doc.context.NDocContext;
import net.thevpc.nuts.toolbox.nsh.eval.NShellContext;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellVar;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellVarListener;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellVariables;
import net.thevpc.nuts.toolbox.nsh.nshell.NShell;
import net.thevpc.nuts.toolbox.nsh.nshell.NShellConfiguration;
import net.thevpc.nuts.lib.doc.util.StringUtils;

public class NshEvaluator implements NDocExprEvaluator {
    private final NShell shell;
    private final NDocContext docContext;

    public NshEvaluator(NDocContext docContext) {
        this.docContext = docContext;
        shell = new NShell(new NShellConfiguration()
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
                .set(new ProcessCmd(docContext));
    }

    public void setVar(String varName, String newValue) {
        docContext.getLog().debug("eval", varName + "=" + StringUtils.toLiteralString(newValue));
        docContext.setVar(varName, newValue);
    }

    @Override
    public Object eval(String content, NDocContext context) {
        NShellContext ctx = shell.createInlineContext(shell.getRootContext(), context.getSourcePath().orElse("nsh"), new String[0]);
        NSession session = NSession.of();
        session.setTerminal(NTerminal.ofMem());
        ctx.setSession(session);
        shell.executeScript(content, ctx);
        return session.out().toString();
    }

    @Override
    public String toString() {
        return "nsh";
    }
}
