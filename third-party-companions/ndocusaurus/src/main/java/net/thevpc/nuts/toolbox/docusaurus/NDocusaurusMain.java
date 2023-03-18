package net.thevpc.nuts.toolbox.docusaurus;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLineContext;
import net.thevpc.nuts.cmdline.NCmdLineProcessor;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;

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
    public void run(NSession session) {
        session.processAppCommandLine(new NCmdLineProcessor() {
            @Override
            public boolean onCmdNextOption(NArg option, NCmdLine commandLine, NCmdLineContext context) {
                NSession session = commandLine.getSession();
                switch (option.key()) {
                    case "-d":
                    case "--dir": {
                        if (workdir == null) {
                            commandLine.withNextEntry((v, a, s) -> workdir = v);
                            return true;
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean onCmdNextNonOption(NArg nonOption, NCmdLine commandLine, NCmdLineContext context) {
                NSession session = commandLine.getSession();
                switch (nonOption.asString().get(session)) {
                    case "start": {
                        commandLine.withNextFlag((v, a, s) -> start = v);
                        return true;
                    }
                    case "build": {
                        commandLine.withNextFlag((v, a, s) -> build = v);
                        return true;
                    }
                    case "pdf": {
                        commandLine.withNextFlag((v, a, s) -> buildPdf = v);
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void onCmdFinishParsing(NCmdLine commandLine, NCmdLineContext context) {
                NSession session = commandLine.getSession();
                if (!start && !build && !buildPdf) {
                    commandLine.throwMissingArgument(
                            NMsg.ofC("missing command. try %s", NMsg.ofCode("sh", "ndocusaurus pdf | start | build"))
                    );
                }
            }

            @Override
            public void onCmdExec(NCmdLine commandLine, NCmdLineContext context) {
                if (workdir == null) {
                    workdir = ".";
                }
                DocusaurusProject docusaurusProject = new DocusaurusProject(workdir,
                        Paths.get(workdir).resolve(".dir-template").resolve("src").toString(),
                        commandLine.getSession());
                new DocusaurusCtrl(docusaurusProject, session)
                        .setBuildWebSite(build)
                        .setStartWebSite(start)
                        .setBuildPdf(buildPdf)
                        .setAutoInstallNutsPackages(NBootManager.of(commandLine.getSession()).getBootOptions().getConfirm().orElse(NConfirmationMode.ASK) == NConfirmationMode.YES)
                        .run();
            }
        });
    }


}
