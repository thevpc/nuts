package net.thevpc.nuts.toolbox.docusaurus;

import net.thevpc.nuts.*;

import java.nio.file.Paths;

public class NDocusaurusMain implements NutsApplication {

    public static void main(String[] args) {
        NutsApplication.main(NDocusaurusMain.class, args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        appContext.processCommandLine(new NutsCommandLineProcessor() {
            boolean start;
            boolean build;
            String workdir = null;
            boolean buildPdf=false;

            @Override
            public boolean onNextOption(NutsArgument option, NutsCommandLine commandline) {
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
            public boolean onNextNonOption(NutsArgument nonOption, NutsCommandLine commandline) {
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
            public void onPrepare(NutsCommandLine commandline) {
                if(!start && !build && !buildPdf){
                    commandline.required(NutsMessage.cstyle("missing command. try ```sh ndocusaurus pdf | start | build```"));
                }
            }

            @Override
            public void onExec() {
                if (workdir == null) {
                    workdir = ".";
                }
                DocusaurusProject docusaurusProject = new DocusaurusProject(workdir,
                        Paths.get(workdir).resolve(".dir-template").resolve("src").toString(),
                        appContext.getSession());
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
