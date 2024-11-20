package net.thevpc.nuts.toolbox.docusaurus;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLineContext;
import net.thevpc.nuts.cmdline.NCmdLineRunner;
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
    public void run() {
        NSession session = NSession.of().get();
        session.runAppCmdLine(new NCmdLineRunner() {
            @Override
            public boolean nextOption(NArg option, NCmdLine cmdLine, NCmdLineContext context) {
                switch (option.key()) {
                    case "-d":
                    case "--dir": {
                        if (workdir == null) {
                            cmdLine.withNextEntry((v, a) -> workdir = v);
                            return true;
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean nextNonOption(NArg nonOption, NCmdLine cmdLine, NCmdLineContext context) {
                switch (nonOption.asString().get()) {
                    case "start": {
                        cmdLine.withNextFlag((v, a) -> start = v);
                        return true;
                    }
                    case "build": {
                        cmdLine.withNextFlag((v, a) -> build = v);
                        return true;
                    }
                    case "pdf": {
                        cmdLine.withNextFlag((v, a) -> buildPdf = v);
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void validate(NCmdLine cmdLine, NCmdLineContext context) {
                if (!start && !build && !buildPdf) {
                    cmdLine.throwMissingArgument(
                            NMsg.ofC("missing command. try %s", NMsg.ofCode("sh", "ndocusaurus pdf | start | build"))
                    );
                }
            }

            @Override
            public void run(NCmdLine cmdLine, NCmdLineContext context) {
                if (workdir == null) {
                    workdir = ".";
                }
                DocusaurusProject docusaurusProject = new DocusaurusProject(workdir,
                        Paths.get(workdir).resolve(".dir-template").resolve("src").toString()
                );
                new DocusaurusCtrl(docusaurusProject)
                        .setBuildWebSite(build)
                        .setStartWebSite(start)
                        .setBuildPdf(buildPdf)
                        .setAutoInstallNutsPackages(NBootManager.of().getBootOptions().getConfirm().orElse(NConfirmationMode.ASK) == NConfirmationMode.YES)
                        .run();
            }
        });
    }


}
