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
import net.thevpc.nuts.util.NMaps;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.toolbox.docusaurus.DocusaurusCtrl;
import net.thevpc.nuts.toolbox.docusaurus.DocusaurusProject;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.TemplateConfig;
import net.thevpc.nuts.toolbox.ntemplate.project.NTemplateProject;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NMsg;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author vpc
 */
public class SiteRunner extends AbstractRunner {

    boolean NUTS_FLAG_SITE = false;

    public SiteRunner() {
        super();
    }

    @Override
    public void configureAfterOptions() {
        context().NUTS_WEBSITE_BASE = context().root.resolve("documentation/website");
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg c = cmdLine.peek().orNull();
        switch (c.key()) {
            case "site": {
                cmdLine.withNextFlag((v, a) -> NUTS_FLAG_SITE = v);
                return true;
            }
        }
        return false;
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


    private Map<String, Object> prepareVars() {
        Map<String, Object> vars = new HashMap<>();
        String latestApiVersion = Nuts.getVersion().toString();
        String stableApiVersion = NAssert.requireNonBlank(context().nutsStableVersion, "nutsStableVersion");
        String stableRuntimeVersion = NAssert.requireNonBlank(context().runtimeStableVersion, "runtimeStableVersion");
        String latestJarLocation = "https://raw.githubusercontent.com/thevpc/nuts-preview/master/net/thevpc/nuts/nuts/" + latestApiVersion + "/nuts-" + latestApiVersion + ".jar";
        String stableJarLocation = "https://repo.maven.apache.org/maven2/net/thevpc/nuts/nuts/" + stableApiVersion + "/nuts-" + stableApiVersion + ".jar";

        vars.put("buildTime", new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date()));
        vars.put("latestApiVersion", latestApiVersion);
        String latestRuntimeVersion = NDescriptorParser.of()
                .setDescriptorStyle(NDescriptorStyle.MAVEN)
                .parse(context().root.resolve("core/nuts-runtime/pom.xml")).get().getId().getVersion().toString();
        vars.put("latestRuntimeVersion", latestRuntimeVersion);
        vars.put("latestJarLocation", latestJarLocation);
        vars.put("stableApiVersion", stableApiVersion);
        vars.put("stableRuntimeVersion", stableRuntimeVersion);
        vars.put("stableJarLocation", stableJarLocation);
        vars.put("jarLocation", latestJarLocation);
        vars.put("apiVersion", latestApiVersion);
        vars.put("runtimeVersion", latestRuntimeVersion);
        return vars;
    }

    private void runSiteGithubRepo() {
        echo("**** $v (nuts)...", NMaps.of("v", NMsg.ofStyled("ntemplate", NTextStyle.keyword())));
        NTemplateProject templateProject = new NTemplateProject(
                new TemplateConfig()
                        .setContextName("nuts-release-tool")
                        .setProjectPath(context().root.resolve(".dir-template").toString())
                        .setTargetFolder(context().root.toString())

        );
        for (Map.Entry<String, Object> e : prepareVars().entrySet()) {
            templateProject.setVar(e.getKey(), e.getValue());
        }
        NPath.of(Mvn.localMaven() + "/" + Mvn.file(Nuts.getApiId(), MvnArtifactType.JAR))
                .copyTo(context().NUTS_WEBSITE_BASE.resolve("static/nuts-preview.jar")
                );
    }

    private void runSiteGithubDocumentation() {
        NInstallCmd.of().addIds("ndocusaurus").run();
        echo("**** $v (nuts)...", NMaps.of("v", NMsg.ofStyled("ndocusaurus", NTextStyle.keyword())));
        String workdir = context().NUTS_WEBSITE_BASE.toString();
        DocusaurusProject docusaurusProject = new DocusaurusProject(workdir,
                Paths.get(workdir).resolve(".dir-template").resolve("src").toString());
        DocusaurusCtrl docusaurusCtrl = new DocusaurusCtrl(docusaurusProject)
                .setBuildWebSite(true)
                .setStartWebSite(false)
                .setBuildPdf(true)
                .setAutoInstallNutsPackages(NWorkspace.of()
                        .getBootOptions().getConfirm().orElse(NConfirmationMode.ASK) == NConfirmationMode.YES)
                .setVars(prepareVars());

        docusaurusCtrl.run();

    }
}
