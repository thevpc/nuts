package net.thevpc.nuts.toolbox.ntemplate;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCommandLineContext;
import net.thevpc.nuts.cmdline.NCommandLineProcessor;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.io.NSessionTerminal;
import net.thevpc.nuts.toolbox.nsh.jshell.*;
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
    public void run(NApplicationContext appContext) {
        appContext.processCommandLine(new NCommandLineProcessor() {

            @Override
            public boolean onCmdNextOption(NArg option, NCommandLine commandLine, NCommandLineContext context) {
                switch (option.key()) {
                    case "-i":
                    case "--init": {
                        commandLine.withNextString((v, r, s) -> config.addInitScript(v));
                        return true;
                    }
                    case "-s":
                    case "--scriptType": {
                        commandLine.withNextString((v, r, s) -> config.setScriptType(v));
                        return true;
                    }
                    case "-t":
                    case "--to": {
                        commandLine.withNextString((v, r, s) -> config.setTargetFolder(v));
                        return true;
                    }
                    case "-p":
                    case "--project": {
                        commandLine.withNextString((v, r, s) -> config.setProjectPath(v));
                        return true;
                    }

                }
                return false;
            }

            @Override
            public boolean onCmdNextNonOption(NArg nonOption, NCommandLine commandLine, NCommandLineContext context) {
                NSession session = commandLine.getSession();
                config.addSource(commandLine.next().flatMap(NLiteral::asString).get(session));
                return false;
            }

            @Override
            public void onCmdFinishParsing(NCommandLine commandLine, NCommandLineContext context) {
                fileTemplater = new NFileTemplater(appContext);
            }

            @Override
            public void onCmdExec(NCommandLine commandLine, NCommandLineContext context) {
                fileTemplater.processProject(config);
            }
        });
    }


    private static class NshEvaluator implements ExprEvaluator {
        private final NApplicationContext appContext;
        private final JShell shell;
        private final FileTemplater fileTemplater;

        public NshEvaluator(NApplicationContext appContext, FileTemplater fileTemplater) {
            this.appContext = appContext;
            this.fileTemplater = fileTemplater;
            shell = new JShell(appContext, new String[0]);
            JShellContext rootContext = shell.getRootContext();
            rootContext.setSession(rootContext.getSession().copy());
            rootContext.vars().addVarListener(
                    new JShellVarListener() {
                        @Override
                        public void varAdded(JShellVar jShellVar, JShellVariables vars, JShellContext context) {
                            setVar(jShellVar.getName(), jShellVar.getValue());
                        }

                        @Override
                        public void varValueUpdated(JShellVar jShellVar, String oldValue, JShellVariables vars, JShellContext context) {
                            setVar(jShellVar.getName(), jShellVar.getValue());
                        }

                        @Override
                        public void varRemoved(JShellVar jShellVar, JShellVariables vars, JShellContext context) {
                            setVar(jShellVar.getName(), null);
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
            JShellContext ctx = shell.createInlineContext(shell.getRootContext(), context.getSourcePath().orElse("nsh"), new String[0]);
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
        public NFileTemplater(NApplicationContext appContext) {
            super(appContext.getSession());
            this.setDefaultExecutor("text/ntemplate-nsh-project", new NshEvaluator(appContext, this));
            setProjectFileName("project.nsh");
        }

        public void executeProjectFile(Path path, String mimeTypesString) {
            executeRegularFile(path, "text/ntemplate-nsh-project");
        }
    }

}
