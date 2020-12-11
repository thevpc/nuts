package net.thevpc.nuts.toolbox.docusaurus;

import net.thevpc.nuts.*;
import net.thevpc.commons.md.docusaurus.DocusaurusProject;

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
            boolean buildPdf=false;

            @Override
            public boolean nextOption(NutsArgument option, NutsCommandLine commandline) {
                switch (option.getStringKey()) {
                    case "-d":
                    case "--dir": {
                        if (workdir == null) {
                            workdir = commandline.nextString().getStringValue();
                            return true;
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean nextNonOption(NutsArgument nonOption, NutsCommandLine commandline) {
                switch (nonOption.getString()) {
                    case "start": {
                        start = commandline.nextBoolean().getBooleanValue();
                        return true;
                    }
                    case "build": {
                        build = commandline.nextBoolean().getBooleanValue();
                        return true;
                    }
                    case "pdf": {
                        buildPdf = commandline.nextBoolean().getBooleanValue();
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void prepare(NutsCommandLine commandline) {
                if(!start && !build && !buildPdf){
                    commandline.required("missing command. try ```sh ndocusaurus pdf | start | build```");
                }
            }

            @Override
            public void exec() {
                if (workdir == null) {
                    workdir = ".";
                }
                DocusaurusProject docusaurusProject = new DocusaurusProject(workdir);
                new DocusaurusCtrl(docusaurusProject,appContext)
                        .setBuildWebSite(build)
                        .setStartWebSite(start)
                        .setBuildPdf(buildPdf)
                        .setAutoInstallNutsPackages(appContext.getWorkspace().config().options().getConfirm()== NutsConfirmationMode.YES)
                        .run();
            }
        });
    }

}
