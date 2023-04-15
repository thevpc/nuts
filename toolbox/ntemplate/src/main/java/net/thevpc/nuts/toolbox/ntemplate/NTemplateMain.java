package net.thevpc.nuts.toolbox.ntemplate;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLineContext;
import net.thevpc.nuts.cmdline.NCmdLineProcessor;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NSessionTerminal;
import net.thevpc.nuts.toolbox.nsh.eval.NShellContext;
import net.thevpc.nuts.toolbox.nsh.nshell.*;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellVar;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellVarListener;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellVariables;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.ExprEvaluator;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.FileTemplater;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.ProcessCmd;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.TemplateConfig;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.util.StringUtils;

import java.nio.file.Path;

public class NTemplateMain implements NApplication {
    TemplateConfig config = new TemplateConfig();
    private FileTemplater fileTemplater;

    public static void main(String[] args) {
        NApplication.main(NTemplateMain.class, args);
    }

    @Override
    public void run(NSession session) {
        session.processAppCommandLine(new NCmdLineProcessor() {

            @Override
            public boolean onCmdNextOption(NArg option, NCmdLine cmdLine, NCmdLineContext context) {
                switch (option.key()) {
                    case "-i":
                    case "--init": {
                        cmdLine.withNextEntry((v, r, s) -> config.addInitScript(v));
                        return true;
                    }
                    case "-s":
                    case "--scriptType": {
                        cmdLine.withNextEntry((v, r, s) -> config.setScriptType(v));
                        return true;
                    }
                    case "-t":
                    case "--to": {
                        cmdLine.withNextEntry((v, r, s) -> config.setTargetFolder(v));
                        return true;
                    }
                    case "-p":
                    case "--project": {
                        cmdLine.withNextEntry((v, r, s) -> config.setProjectPath(v));
                        return true;
                    }

                }
                return false;
            }

            @Override
            public boolean onCmdNextNonOption(NArg nonOption, NCmdLine cmdLine, NCmdLineContext context) {
                NSession session = cmdLine.getSession();
                config.addSource(cmdLine.next().flatMap(NLiteral::asString).get(session));
                return false;
            }

            @Override
            public void onCmdFinishParsing(NCmdLine cmdLine, NCmdLineContext context) {
                fileTemplater = new NFileTemplater(session);
            }

            @Override
            public void onCmdExec(NCmdLine cmdLine, NCmdLineContext context) {
                fileTemplater.processProject(config);
            }
        });
    }


    private static class NshEvaluator implements ExprEvaluator {
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

    private static class NFileTemplater extends FileTemplater {
        public NFileTemplater(NSession session) {
            super(session);
            this.setDefaultExecutor("text/ntemplate-nsh-project", new NshEvaluator(session, this));
            setProjectFileName("project.nsh");
        }

        public void executeProjectFile(Path path, String mimeTypesString) {
            executeRegularFile(path, "text/ntemplate-nsh-project");
        }
    }

}
