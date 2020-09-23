package net.vpc.toolbox.ntemplater;

import net.vpc.app.nuts.NutsApplication;
import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsCommandLineProcessor;
import net.vpc.commons.filetemplate.FileTemplater;
import net.vpc.commons.filetemplate.TemplateConfig;

public class NTemplaterMain extends NutsApplication {

    public static void main(String[] args) {
        NutsApplication.main(NTemplaterMain.class, args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        appContext.processCommandLine(new NutsCommandLineProcessor() {
            private FileTemplater fileTemplater = new FileTemplater();
            TemplateConfig config = new TemplateConfig();
//            private String mimeType = null;

            @Override
            public boolean nextOption(NutsArgument option, NutsCommandLine cmdLine) {
                switch (option.getStringKey()) {
                    case "-i":
                    case "--init": {
                        config.addInitScript(cmdLine.nextString().getStringValue());
                        return true;
                    }
                    case "-s":
                    case "--scriptType": {
                        config.setScriptType(cmdLine.nextString().getStringValue());
                        return true;
                    }
                    case "-t":
                    case "--to": {
                        config.setTargetFolder(cmdLine.nextString().getStringValue());
                        return true;
                    }
                    case "-p":
                    case "--project": {
                        config.setProjectPath(cmdLine.nextString().getStringValue());
                        return true;
                    }

                }
                return false;
            }

            @Override
            public boolean nextNonOption(NutsArgument nonOption, NutsCommandLine cmdLine) {
                config.addSource(cmdLine.next().getString());
                return false;
            }

            @Override
            public void exec() {
                fileTemplater.processProject(config);
            }
        });
    }

    
}
