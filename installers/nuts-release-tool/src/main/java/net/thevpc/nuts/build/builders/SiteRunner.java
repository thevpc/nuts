/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.build.builders;

import net.thevpc.nuts.*;
import net.thevpc.nuts.build.util.AbstractRunner;
import net.thevpc.nuts.build.util.Mvn;
import net.thevpc.nuts.build.util.MvnArtifactType;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.NMaps;
import net.thevpc.nuts.util.NMsg;

/**
 * @author vpc
 */
public class SiteRunner extends AbstractRunner {

    boolean NUTS_FLAG_SITE = false;

    @Override
    public void configureAfterOptions() {
        context().NUTS_WEBSITE_BASE = context().root.resolve("documentation/website");
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg c = cmdLine.peek().orNull();
        switch (c.key()) {
            case "site": {
                cmdLine.withNextFlag((v, a, s) -> NUTS_FLAG_SITE = v);
                return true;
            }
        }
        return false;
    }

    public SiteRunner(NSession session) {
        super(session);
    }

    @Override
    public void configureBeforeOptions(NCmdLine cmdLine) {
    }

    @Override
    public void run() {
        if (NUTS_FLAG_SITE) {
            runSite();
        }
    }

    private void runSite() {
        echo("**** $v (nuts)...", NMaps.of("v", NMsg.ofStyled("build-nuts-site", NTextStyle.keyword())));
        NInstallCmd.of(session.copy().yes())
                .addIds("ntemplate", "ndocusaurus").run();

        echo("**** $v (nuts)...", NMaps.of("v", NMsg.ofStyled("ntemplate", NTextStyle.keyword())));
        NExecCmd.of(session).embedded()
                .addCommand(
                        "ntemplate",
                        "-p",
                        context().root.resolve(".dir-template").toString(),
                        "-t",
                        context().root.toString()
                ).failFast()
                .run();

        NPath.of(Mvn.localMaven() + "/" + Mvn.file(Nuts.getApiId(), MvnArtifactType.JAR), session)
                .copyTo(context().NUTS_WEBSITE_BASE.resolve("static/nuts-preview.jar")
                );

        echo("**** $v (nuts)...", NMaps.of("v", NMsg.ofStyled("ndocusaurus", NTextStyle.keyword())));
        NExecCmd.of(session).embedded()
                .addCommand(
                        "ndocusaurus",
                        "-d",
                        context().NUTS_WEBSITE_BASE.toString(),
                        "pdf",
                        "build"
                ).failFast()
                .run();
    }
}
