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
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.TemplateConfig;
import net.thevpc.nuts.toolbox.ntemplate.project.NTemplateProject;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMaps;
import net.thevpc.nuts.util.NMsg;

import java.text.SimpleDateFormat;
import java.util.Date;

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

        runSiteGithubRepo();
        runSiteGithubDocumentation();
    }


    private void runSiteGithubRepo() {
        echo("**** $v (nuts)...", NMaps.of("v", NMsg.ofStyled("ntemplate", NTextStyle.keyword())));
//        NExecCmd.of(session).embedded()
//                .addCommand(
//                        "ntemplate",
//                        "-p",
//                        context().root.resolve(".dir-template").toString(),
//                        "-t",
//                        context().root.toString()
//                ).failFast()
//                .run();
        NTemplateProject templateProject=new NTemplateProject(
                new TemplateConfig()
                        .setContextName("nuts-release-tool")
                        .setProjectPath(context().root.resolve(".dir-template").toString())
                        .setTargetFolder(context().root.toString())
                ,
                session
        );
        String apiVersion = Nuts.getVersion().toString();

        String latestJarLocation = "https://raw.githubusercontent.com/thevpc/nuts-preview/master/net/thevpc/nuts/nuts/" +apiVersion + "/nuts-" + apiVersion + ".jar";

        templateProject.setVar("buildTime",new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date()));
        templateProject.setVar("latestApiVersion", apiVersion);
        String latestRuntimeVersion = NDescriptorParser.of(session)
                .setDescriptorStyle(NDescriptorStyle.MAVEN)
                .parse(context().root.resolve("core/nuts-runtime/pom.xml")).get().getId().getVersion().toString();
        templateProject.setVar("latestRuntimeVersion", latestRuntimeVersion);
        templateProject.setVar("latestJarLocation", latestJarLocation);
        templateProject.setVar("stableApiVersion", NAssert.requireNonBlank(context().nutsStableVersion,"nutsStableVersion"));
        templateProject.setVar("stableRuntimeVersion", NAssert.requireNonBlank(context().runtimeStableVersion,"runtimeStableVersion"));
        templateProject.setVar("stableJarLocation", "https://repo.maven.apache.org/maven2/net/thevpc/nuts/nuts/"+context().nutsStableVersion+"/nuts-"+context().nutsStableVersion+".jar");
        templateProject.setVar("jarLocation", latestJarLocation);
        templateProject.setVar("apiVersion", apiVersion);
        templateProject.setVar("runtimeVersion", latestRuntimeVersion);
        templateProject.run();

        NPath.of(Mvn.localMaven() + "/" + Mvn.file(Nuts.getApiId(), MvnArtifactType.JAR), session)
                .copyTo(context().NUTS_WEBSITE_BASE.resolve("static/nuts-preview.jar")
                );
    }

    private void runSiteGithubDocumentation() {
        NInstallCmd.of(session.copy().yes()).addIds("ndocusaurus").run();
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
