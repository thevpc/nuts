package net.thevpc.nuts.toolbox.docusaurus;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCommandLineContext;
import net.thevpc.nuts.cmdline.NCommandLineProcessor;
import net.thevpc.nuts.cmdline.NArgument;
import net.thevpc.nuts.cmdline.NCommandLine;

import java.nio.file.Paths;

public class NDocusaurusMain implements NApplication {

    boolean start;
    boolean build;
    String workdir = null;
    boolean buildPdf = false;

    public static void main(String[] args) {
        NApplication.main(NDocusaurusMain.class, args);
    }

    @Override
    public void run(NApplicationContext appContext) {
        appContext.processCommandLine(new NCommandLineProcessor() {
            @Override
            public boolean onCmdNextOption(NArgument option, NCommandLine commandLine, NCommandLineContext context) {
                NSession session = commandLine.getSession();
                switch (option.key()) {
                    case "-d":
                    case "--dir": {
                        if (workdir == null) {
                            commandLine.withNextString((v, a, s) -> workdir = v);
                            return true;
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean onCmdNextNonOption(NArgument nonOption, NCommandLine commandLine, NCommandLineContext context) {
                NSession session = commandLine.getSession();
                switch (nonOption.asString().get(session)) {
                    case "start": {
                        commandLine.withNextBoolean((v, a, s) -> start = v);
                        return true;
                    }
                    case "build": {
                        commandLine.withNextBoolean((v, a, s) -> build = v);
                        return true;
                    }
                    case "pdf": {
                        commandLine.withNextBoolean((v, a, s) -> buildPdf = v);
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void onCmdFinishParsing(NCommandLine commandLine, NCommandLineContext context) {
                NSession session = commandLine.getSession();
                if (!start && !build && !buildPdf) {
                    commandLine.throwMissingArgument(
                            NMsg.ofCstyle("missing command. try %s", NMsg.ofCode("sh", "ndocusaurus pdf | start | build"))
                    );
                }
            }

            @Override
            public void onCmdExec(NCommandLine commandLine, NCommandLineContext context) {
                if (workdir == null) {
                    workdir = ".";
                }
                DocusaurusProject docusaurusProject = new DocusaurusProject(workdir,
                        Paths.get(workdir).resolve(".dir-template").resolve("src").toString(),
                        commandLine.getSession());
                new DocusaurusCtrl(docusaurusProject, appContext)
                        .setBuildWebSite(build)
                        .setStartWebSite(start)
                        .setBuildPdf(buildPdf)
                        .setAutoInstallNutsPackages(commandLine.getSession().boot().getBootOptions().getConfirm().orElse(NConfirmationMode.ASK) == NConfirmationMode.YES)
                        .run();
            }
        });
    }


}
