package net.thevpc.nuts.toolbox.ntemplate;

import net.thevpc.commons.filetemplate.ExprEvaluator;
import net.thevpc.commons.filetemplate.FileTemplater;
import net.thevpc.commons.filetemplate.MimeTypeConstants;
import net.thevpc.commons.filetemplate.TemplateConfig;
import net.thevpc.commons.filetemplate.util.StringUtils;
import net.thevpc.jshell.JShellVar;
import net.thevpc.jshell.JShellVarListener;
import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nsh.AbstractNshBuiltin;
import net.thevpc.nuts.toolbox.nsh.NshExecutionContext;
import net.thevpc.nuts.toolbox.nsh.NutsJavaShell;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;

public class NTemplateMain extends NutsApplication {

    public static void main(String[] args) {
        NutsApplication.main(NTemplateMain.class, args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        appContext.processCommandLine(new NutsCommandLineProcessor() {
            TemplateConfig config = new TemplateConfig();
            private FileTemplater fileTemplater = new FileTemplater();
//            private String mimeType = null;

            @Override
            public boolean nextOption(NutsArgument option, NutsCommandLine commandline) {
                switch (option.getStringKey()) {
                    case "-i":
                    case "--init": {
                        config.addInitScript(commandline.nextString().getStringValue());
                        return true;
                    }
                    case "-s":
                    case "--scriptType": {
                        config.setScriptType(commandline.nextString().getStringValue());
                        return true;
                    }
                    case "-t":
                    case "--to": {
                        config.setTargetFolder(commandline.nextString().getStringValue());
                        return true;
                    }
                    case "-p":
                    case "--project": {
                        config.setProjectPath(commandline.nextString().getStringValue());
                        return true;
                    }

                }
                return false;
            }

            @Override
            public boolean nextNonOption(NutsArgument nonOption, NutsCommandLine commandline) {
                config.addSource(commandline.next().getString());
                return false;
            }

            @Override
            public void init(NutsCommandLine commandline) {
                fileTemplater.setDefaultExecutor(MimeTypeConstants.FTEX, new NshEvaluator(appContext, config,fileTemplater));
            }

            @Override
            public void exec() {
                fileTemplater.processProject(config);
            }
        });
    }


    private static class NshEvaluator implements ExprEvaluator {
        private NutsApplicationContext appContext;
        private TemplateConfig config;
        private NutsJavaShell shell;
        private FileTemplater fileTemplater;

        public NshEvaluator(NutsApplicationContext appContext, TemplateConfig config, FileTemplater fileTemplater) {
            this.appContext = appContext;
            this.config = config;
            this.fileTemplater = fileTemplater;
            shell = new NutsJavaShell(appContext);
            shell.setSession(shell.getSession().copy());
            shell.getRootContext().vars().addListener(
                    new JShellVarListener() {
                        @Override
                        public void varAdded(JShellVar jShellVar) {
                            setVar(jShellVar.getName(), jShellVar.getValue());
                        }

                        @Override
                        public void varValueUpdated(JShellVar jShellVar, String oldValue) {
                            setVar(jShellVar.getName(), jShellVar.getValue());
                        }

                        @Override
                        public void varRemoved(JShellVar jShellVar) {
                            setVar(jShellVar.getName(), null);
                        }
                    }
            );
            shell.getRootContext()
                    .builtins()
                    .set(
                            new AbstractNshBuiltin("process", 10) {
                                @Override
                                public void exec(String[] args, NshExecutionContext context) {
                                    if (args.length != 1) {
                                        throw new IllegalStateException(getName() + " : invalid arguments count");
                                    }
                                    String pathString = args[0];
                                    fileTemplater.getLog().debug("eval", getName() + "(" + StringUtils.toLiteralString(pathString) + ")");
                                    fileTemplater.executeRegularFile(Paths.get(pathString), null);
                                }
                            }
                    );
        }

        public void setVar(String varName, String newValue) {
            fileTemplater.getLog().debug("eval", varName + "=" + StringUtils.toLiteralString(newValue));
            fileTemplater.setVar(varName, newValue);
        }

        @Override
        public Object eval(String content, FileTemplater context) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream out1 = new PrintStream(out);
            shell.getSession().setTerminal(
                    shell.getWorkspace().io().term()
                            .createTerminal(
                                    new ByteArrayInputStream(new byte[0]),
                                    out1,
                                    out1,
                                    shell.getSession()
                            )
            );
            shell.executeString(content,shell.getRootContext());
            out1.flush();
            return out.toString();
        }

        @Override
        public String toString() {
            return "nsh";
        }
    }
}
