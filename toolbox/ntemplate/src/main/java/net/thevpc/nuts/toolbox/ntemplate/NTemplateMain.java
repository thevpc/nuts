package net.thevpc.nuts.toolbox.ntemplate;

import net.thevpc.commons.filetemplate.*;
import net.thevpc.commons.filetemplate.util.StringUtils;
import net.thevpc.jshell.*;
import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nsh.AbstractNshBuiltin;
import net.thevpc.nuts.toolbox.nsh.NshExecutionContext;
import net.thevpc.nuts.toolbox.nsh.NutsJavaShell;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

public class NTemplateMain extends NutsApplication {

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
            public void prepare(NutsCommandLine commandline) {
                fileTemplater= new NFileTemplater(appContext);
            }

            @Override
            public void exec() {
                fileTemplater.processProject(config);
            }
        });
    }


    private static class NshEvaluator implements ExprEvaluator {
        private NutsApplicationContext appContext;
        private NutsJavaShell shell;
        private FileTemplater fileTemplater;

        public NshEvaluator(NutsApplicationContext appContext, FileTemplater fileTemplater) {
            this.appContext = appContext;
            this.fileTemplater = fileTemplater;
            shell = new NutsJavaShell(appContext,new String[0]);
            shell.setSession(shell.getSession().copy());
            shell.getRootContext().vars().addVarListener(
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
                    shell.getWorkspace().term()
                            .createTerminal(
                                    new ByteArrayInputStream(new byte[0]),
                                    out1,
                                    out1
                            )
            );
            JShellFileContext ctx = shell.createSourceFileContext(
                    shell.getRootContext(),
                    context.getSourcePath().orElseGet(()->"nsh"),new String[0]
            );
            shell.executeString(content,ctx);
            out1.flush();
            return out.toString();
        }

        @Override
        public String toString() {
            return "nsh";
        }
    }

    private static class NFileTemplater extends FileTemplater {
        public NFileTemplater(NutsApplicationContext appContext) {
            this.setDefaultExecutor("text/ntemplate-nsh-project", new NshEvaluator(appContext, this));
            setProjectFileName("project.nsh");
            this.setLog(new TemplateLog() {
                NutsLoggerOp logOp;
                @Override
                public void info(String title, String message) {
                    log().verb(NutsLogVerb.INFO).level(Level.FINER)
                            .log( "{0} : {1}",title,message);
                }

                @Override
                public void debug(String title, String message) {
                    log().verb(NutsLogVerb.DEBUG).level(Level.FINER)
                            .log( "{0} : {1}",title,message);
                }

                @Override
                public void error(String title, String message) {
                    log().verb(NutsLogVerb.FAIL).level(Level.FINER).log( "{0} : {1}",title,message);
                }

                private NutsLoggerOp log() {
                    if(logOp==null) {
                         logOp = appContext.getWorkspace().log().of(NTemplateMain.class)
                                 .with().session(appContext.getSession())
                                 .style(NutsTextFormatStyle.JSTYLE)
                                ;
                    }
                    return logOp;
                }
            });
        }

        public void executeProjectFile(Path path, String mimeTypesString) {
            executeRegularFile(path,"text/ntemplate-nsh-project");
        }
    }
}
