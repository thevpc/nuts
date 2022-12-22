package net.thevpc.nuts.toolbox.docusaurus;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsCommandLineContext;
import net.thevpc.nuts.cmdline.NutsCommandLineProcessor;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;

import java.nio.file.Paths;

public class NDocusaurusMain implements NutsApplication {

    boolean start;
    boolean build;
    String workdir = null;
    boolean buildPdf = false;

    public static void main(String[] args) {
        NutsApplication.main(NDocusaurusMain.class, args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        appContext.processCommandLine(new NutsCommandLineProcessor() {
            @Override
            public boolean onCmdNextOption(NutsArgument option, NutsCommandLine commandLine, NutsCommandLineContext context) {
                NutsSession session = commandLine.getSession();
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
            public boolean onCmdNextNonOption(NutsArgument nonOption, NutsCommandLine commandLine, NutsCommandLineContext context) {
                NutsSession session = commandLine.getSession();
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
            public void onCmdFinishParsing(NutsCommandLine commandLine, NutsCommandLineContext context) {
                NutsSession session = commandLine.getSession();
                if (!start && !build && !buildPdf) {
                    commandLine.throwMissingArgument(
                            NutsMessage.ofCstyle("missing command. try %s", NutsMessage.ofCode("sh", "ndocusaurus pdf | start | build"))
                    );
                }
            }

            @Override
            public void onCmdExec(NutsCommandLine commandLine, NutsCommandLineContext context) {
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
                        .setAutoInstallNutsPackages(commandLine.getSession().boot().getBootOptions().getConfirm().orElse(NutsConfirmationMode.ASK) == NutsConfirmationMode.YES)
                        .run();
            }
        });
    }


}
