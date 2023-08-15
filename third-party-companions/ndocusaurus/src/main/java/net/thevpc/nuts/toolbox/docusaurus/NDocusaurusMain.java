package net.thevpc.nuts.toolbox.docusaurus;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLineContext;
import net.thevpc.nuts.cmdline.NCmdLineProcessor;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.util.NMsg;

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
            public boolean onCmdNextOption(NArg option, NCmdLine cmdLine, NCmdLineContext context) {
                NSession session = cmdLine.getSession();
                switch (option.key()) {
                    case "-d":
                    case "--dir": {
                        if (workdir == null) {
                            cmdLine.withNextEntry((v, a, s) -> workdir = v);
                            return true;
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean onCmdNextNonOption(NArg nonOption, NCmdLine cmdLine, NCmdLineContext context) {
                NSession session = cmdLine.getSession();
                switch (nonOption.asString().get(session)) {
                    case "start": {
                        cmdLine.withNextFlag((v, a, s) -> start = v);
                        return true;
                    }
                    case "build": {
                        cmdLine.withNextFlag((v, a, s) -> build = v);
                        return true;
                    }
                    case "pdf": {
                        cmdLine.withNextFlag((v, a, s) -> buildPdf = v);
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void onCmdFinishParsing(NCmdLine cmdLine, NCmdLineContext context) {
                NSession session = cmdLine.getSession();
                if (!start && !build && !buildPdf) {
                    cmdLine.throwMissingArgument(
                            NMsg.ofC("missing command. try %s", NMsg.ofCode("sh", "ndocusaurus pdf | start | build"))
                    );
                }
            }

            @Override
            public void onCmdExec(NCmdLine cmdLine, NCmdLineContext context) {
                if (workdir == null) {
                    workdir = ".";
                }
                DocusaurusProject docusaurusProject = new DocusaurusProject(workdir,
                        Paths.get(workdir).resolve(".dir-template").resolve("src").toString(),
                        cmdLine.getSession());
                new DocusaurusCtrl(docusaurusProject, session)
                        .setBuildWebSite(build)
                        .setStartWebSite(start)
                        .setBuildPdf(buildPdf)
                        .setAutoInstallNutsPackages(NBootManager.of(cmdLine.getSession()).getBootOptions().getConfirm().orElse(NConfirmationMode.ASK) == NConfirmationMode.YES)
                        .run();
            }
        });
    }


}
