package net.thevpc.nuts.toolbox.docusaurus;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NTerminal;
import net.thevpc.nuts.lib.doc.context.NDocContext;
import net.thevpc.nuts.lib.doc.executor.NDocExprEvaluator;
import net.thevpc.nuts.lib.doc.executor.nsh.ProcessCmd;
import net.thevpc.nuts.lib.doc.util.StringUtils;
import net.thevpc.nuts.toolbox.nsh.eval.NShellContext;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellVar;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellVarListener;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellVariables;
import net.thevpc.nuts.toolbox.nsh.nshell.NShell;
import net.thevpc.nuts.toolbox.nsh.nshell.NShellConfiguration;

public class DocusaurusNshEvaluator implements NDocExprEvaluator {
    private NShell shell;
    private NDocContext fileTemplater;

    public DocusaurusNshEvaluator(NDocContext fileTemplater) {
        this.fileTemplater = fileTemplater;
        shell = new NShell(new NShellConfiguration().setIncludeDefaultBuiltins(true).setIncludeExternalExecutor(true));
        shell.getRootContext().setSession(shell.getRootContext().getSession().copy());
        shell.getRootContext().vars().addVarListener(
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
        shell.getRootContext()
                .builtins()
                .set(new ProcessCmd(fileTemplater));
    }

    public void setVar(String varName, String newValue) {
        fileTemplater.getLog().debug("eval", varName + "=" + StringUtils.toLiteralString(newValue));
        fileTemplater.setVar(varName, newValue);
    }

    @Override
    public Object eval(String content, NDocContext context) {
        return NSession.of().copy()
                .setTerminal(NTerminal.ofMem())
                .callWith(
                        () -> {
                            NShellContext ctx = shell.createInlineContext(
                                    shell.getRootContext(),
                                    context.getSourcePath().orElseGet(() -> "nsh"), new String[0]
                            );
                            shell.executeScript(content, ctx);
                            return NSession.of().out().toString();
                        }
                );

    }

    @Override
    public String toString() {
        return "nsh";
    }

}
