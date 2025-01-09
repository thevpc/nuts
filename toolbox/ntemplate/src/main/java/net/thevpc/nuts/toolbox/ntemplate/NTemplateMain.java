package net.thevpc.nuts.toolbox.ntemplate;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLineContext;
import net.thevpc.nuts.cmdline.NCmdLineRunner;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.FileTemplater;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.TemplateConfig;
import net.thevpc.nuts.toolbox.ntemplate.project.NTemplateProject;
import net.thevpc.nuts.util.NLiteral;

public class NTemplateMain implements NApplication {
    TemplateConfig config = new TemplateConfig();
    private FileTemplater fileTemplater;

    public static void main(String[] args) {
        NApplication.main(NTemplateMain.class, args);
    }

    @Override
    public void run() {
        NApp.of().processCmdLine(new NCmdLineRunner() {

            @Override
            public boolean nextOption(NArg option, NCmdLine cmdLine, NCmdLineContext context) {
                switch (option.key()) {
                    case "-i":
                    case "--init": {
                        cmdLine.withNextEntry((v, r) -> config.addInitScript(v));
                        return true;
                    }
                    case "-s":
                    case "--scriptType": {
                        cmdLine.withNextEntry((v, r) -> config.setScriptType(v));
                        return true;
                    }
                    case "-t":
                    case "--to": {
                        cmdLine.withNextEntry((v, r) -> config.setTargetFolder(v));
                        return true;
                    }
                    case "-p":
                    case "--project": {
                        cmdLine.withNextEntry((v, r) -> config.setProjectPath(v));
                        return true;
                    }

                }
                return false;
            }

            @Override
            public boolean nextNonOption(NArg nonOption, NCmdLine cmdLine, NCmdLineContext context) {
                config.addSource(cmdLine.next().flatMap(NLiteral::asString).get());
                return false;
            }


            @Override
            public void run(NCmdLine cmdLine, NCmdLineContext context) {
                new NTemplateProject(config).run();
            }
        });
    }


}
