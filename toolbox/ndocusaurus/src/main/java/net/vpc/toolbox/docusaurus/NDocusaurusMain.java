package net.vpc.toolbox.docusaurus;

import net.vpc.app.nuts.*;
import net.vpc.commons.md.docusaurus.DocusaurusProject;
import net.vpc.commons.docusaurus.DocusaurusCtrl;

public class NDocusaurusMain extends NutsApplication {

    public static void main(String[] args) {
        main(NDocusaurusMain.class, args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        appContext.processCommandLine(new NutsCommandLineProcessor() {
            boolean start;
            boolean build;
            String workdir = null;
            boolean updateSidebarMenu =true;
            boolean buildPdf=true;

            @Override
            public boolean nextOption(NutsArgument option, NutsCommandLine cmdLine) {
                switch (option.getStringKey()) {
                    case "-d":
                    case "-dir": {
                        if (workdir == null) {
                            workdir = cmdLine.nextString().getStringValue();
                        } else {
                            cmdLine.unexpectedArgument();
                        }
                        break;
                    }
                    case "--pdf": {
                        buildPdf=cmdLine.nextBoolean().getBooleanValue();
                        break;
                    }
                    case "--updateSidebarMenu": {
                        updateSidebarMenu=cmdLine.nextBoolean().getBooleanValue();
                        break;
                    }
                    default: {
                        return false;
                    }
                }
                return false;
            }

            @Override
            public boolean nextNonOption(NutsArgument nonOption, NutsCommandLine cmdLine) {
                switch (nonOption.getString()) {
                    case "start": {
                        start = cmdLine.nextBoolean().getBooleanValue();
                        return true;
                    }
                    case "build": {
                        build = cmdLine.nextBoolean().getBooleanValue();
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void exec() {
                if (workdir == null) {
                    workdir = ".";
                }
                DocusaurusProject docusaurusProject = new DocusaurusProject(workdir);
                new DocusaurusCtrl(docusaurusProject)
                        .setBuildWebSite(build)
                        .setStartWebSite(start)
                        .setBuildPdf(buildPdf)
                        .setUpdateSidebarMenu(updateSidebarMenu)
                        .setAutoInstallNutsPackages(appContext.getWorkspace().config().options().getConfirm()== NutsConfirmationMode.YES)
                        .run();
            }
        });
    }

}
