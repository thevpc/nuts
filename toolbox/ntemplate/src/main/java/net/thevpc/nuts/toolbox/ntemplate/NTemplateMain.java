package net.thevpc.nuts.toolbox.ntemplate;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nsh.AbstractJShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.*;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.ExprEvaluator;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.FileTemplater;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.ProcessCmd;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.TemplateConfig;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NTemplateMain implements NutsApplication {

    public static void main(String[] args) {
        NutsApplication.main(NTemplateMain.class, args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        appContext.processCommandLine(new NutsCommandLineProcessor() {
            TemplateConfig config = new TemplateConfig();
            private FileTemplater fileTemplater;
//            private String mimeType = null;

            @Override
            public boolean onNextOption(NutsArgument option, NutsCommandLine commandline) {
                switch (option.getKey().getString()) {
                    case "-i":
                    case "--init": {
                        config.addInitScript(commandline.nextString().getValue().getString());
                        return true;
                    }
                    case "-s":
                    case "--scriptType": {
                        config.setScriptType(commandline.nextString().getValue().getString());
                        return true;
                    }
                    case "-t":
                    case "--to": {
                        config.setTargetFolder(commandline.nextString().getValue().getString());
                        return true;
                    }
                    case "-p":
                    case "--project": {
                        config.setProjectPath(commandline.nextString().getValue().getString());
                        return true;
                    }

                }
                return false;
            }

            @Override
            public boolean onNextNonOption(NutsArgument nonOption, NutsCommandLine commandline) {
                config.addSource(commandline.next().getString());
                return false;
            }

            @Override
            public void onPrepare(NutsCommandLine commandline) {
                fileTemplater = new NFileTemplater(appContext);
            }

            @Override
            public void onExec() {
                fileTemplater.processProject(config);
            }
        });
    }


    private static class NshEvaluator implements ExprEvaluator {
        private NutsApplicationContext appContext;
        private JShell shell;
        private FileTemplater fileTemplater;

        public NshEvaluator(NutsApplicationContext appContext, FileTemplater fileTemplater) {
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
            JShellContext ctx=shell.createInlineContext(shell.getRootContext(),context.getSourcePath().orElse("nsh"),new String[0]);
            NutsSession session = context.getSession().copy();
            session.setTerminal(NutsSessionTerminal.ofMem(session));
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
        public NFileTemplater(NutsApplicationContext appContext) {
            super(appContext.getSession());
            this.setDefaultExecutor("text/ntemplate-nsh-project", new NshEvaluator(appContext, this));
            setProjectFileName("project.nsh");
        }

        public void executeProjectFile(Path path, String mimeTypesString) {
            executeRegularFile(path, "text/ntemplate-nsh-project");
        }
    }
}
