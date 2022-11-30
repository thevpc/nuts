package net.thevpc.nuts.toolbox.docusaurus;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;

import java.nio.file.Paths;
import java.util.function.Predicate;

public class NDocusaurusMain implements NutsApplication, NutsAppCmdProcessor {

    boolean start;
    boolean build;
    String workdir = null;
    boolean buildPdf = false;

    public static void main(String[] args) {
        NutsApplication.main(NDocusaurusMain.class, args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        appContext.processCommandLine(this);
    }

    @Override
    public boolean onCmdNextOption(NutsArgument option, NutsCommandLine commandLine, NutsApplicationContext context) {
        NutsSession session = context.getSession();
        switch (option.getKey().asString().get(session)) {
            case "-d":
            case "--dir": {
                if (workdir == null) {
                    commandLine.withNextString((v, a, s)->workdir=v,session);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onCmdNextNonOption(NutsArgument nonOption, NutsCommandLine commandLine, NutsApplicationContext context) {
        NutsSession session = context.getSession();
        switch (nonOption.asString().get(session)) {
            case "start": {
                commandLine.withNextBoolean((v, a, s) -> start=v,session);
                return true;
            }
            case "build": {
                commandLine.withNextBoolean((v, a, s) -> build=v,session);
                return true;
            }
            case "pdf": {
                commandLine.withNextBoolean((v, a, s) -> buildPdf=v,session);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCmdFinishParsing(NutsCommandLine commandLine, NutsApplicationContext context) {
        NutsSession session = context.getSession();
        if (!start && !build && !buildPdf) {
            commandLine.throwMissingArgument(
                    NutsMessage.ofCstyle("missing command. try %s", NutsMessage.ofCode("sh", "ndocusaurus pdf | start | build")),
                    session);
        }
    }

    @Override
    public void onCmdExec(NutsCommandLine commandLine, NutsApplicationContext context) {
        if (workdir == null) {
            workdir = ".";
        }
        DocusaurusProject docusaurusProject = new DocusaurusProject(workdir,
                Paths.get(workdir).resolve(".dir-template").resolve("src").toString(),
                context.getSession());
        new DocusaurusCtrl(docusaurusProject, context)
                .setBuildWebSite(build)
                .setStartWebSite(start)
                .setBuildPdf(buildPdf)
                .setAutoInstallNutsPackages(context.getSession().boot().getBootOptions().getConfirm().orElse(NutsConfirmationMode.ASK) == NutsConfirmationMode.YES)
                .run();
    }

}
