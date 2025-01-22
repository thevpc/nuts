/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.docusaurus;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.lib.doc.*;
import net.thevpc.nuts.lib.doc.context.NDocContext;
import net.thevpc.nuts.lib.doc.mimetype.DefaultNDocMimeTypeResolver;
import net.thevpc.nuts.lib.doc.mimetype.MimeTypeConstants;
import net.thevpc.nuts.lib.doc.util.FileProcessorUtils;
import net.thevpc.nuts.util.NComparator;
import net.thevpc.nuts.util.NMsg;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author thevpc
 */
public class DocusaurusCtrl {

    private DocusaurusProject project;
    private boolean buildPdf;
    private boolean buildWebSite;
    private boolean startWebSite;
    private boolean autoInstallNutsPackages;
    private String ndocVersion;
    private String npmCommandPath;
    private Map<String, Object> vars = new HashMap<>();

    public DocusaurusCtrl(DocusaurusProject project) {
        this.project = project;
    }


    public boolean isBuildPdf() {
        return buildPdf;
    }

    public DocusaurusCtrl setBuildPdf(boolean buildPdf) {
        this.buildPdf = buildPdf;
        return this;
    }

    public boolean isBuildWebSite() {
        return buildWebSite;
    }

    public DocusaurusCtrl setBuildWebSite(boolean buildWebSite) {
        this.buildWebSite = buildWebSite;
        return this;
    }

    public boolean isStartWebSite() {
        return startWebSite;
    }

    public DocusaurusCtrl setStartWebSite(boolean startWebSite) {
        this.startWebSite = startWebSite;
        return this;
    }

    public void run() {
        NPath base = null;
            base = NPath.of(project.getDocusaurusBaseFolder()).toAbsolute().normalize();
        boolean genSidebarMenu = project.getConfigDocusaurusExtra()
                .asObject().orElse(NObjectElement.ofEmpty())
                .getBooleanByPath("generateSidebarMenu")
                .orElse(false);
        NPath basePath = base;
        NPath preProcessor = getPreProcessorBaseDir();
        NSession session = NSession.of();
        if (preProcessor != null && preProcessor.isDirectory()) {
//            Files.walk(base).filter(x->Files.isDirectory(base))
            NPath docs = basePath.resolve("docs");
            if (basePath.resolve("node_modules").isDirectory()
                    && basePath.resolve("docusaurus.config.js").isRegularFile()) {
                session.out().print(NMsg.ofC("clear folder %s%n", docs));
                deletePathChildren(docs);
            }

            session.out().print(NMsg.ofC("process template %s -> %s%n", preProcessor, getTargetBaseDir()));
            NDocProjectConfig config = new NDocProjectConfig()
                    .setProjectPath(preProcessor.toString())
                    .setTargetFolder(getTargetBaseDir().toString())
                    .setContextName("ndocusaurus")
                    .setVars(vars);
            NDocContext nexpr = new DaucusaurusNDocContext()
                    //.setWorkingDir(base.toString())
                    .setMimeTypeResolver(
                            new DefaultNDocMimeTypeResolver()
                                    .setExtensionMimeType("nexpr", MimeTypeConstants.NEXPR)
                                    .setNameMimeType(DocusaurusProject.DOCUSAURUS_FOLDER_CONFIG, DocusaurusProject.DOCUSAURUS_FOLDER_CONFIG_MIMETYPE)
                    )
                    .setCustomVarEvaluator(new DocusaurusCustomVarEvaluator(project));
            nexpr.getProcessorManager()
                    .setDefaultProcessor(DocusaurusProject.DOCUSAURUS_FOLDER_CONFIG_MIMETYPE, new DocusaurusFolderConfigProcessor(this));
            nexpr.run(config);
        }
        if (genSidebarMenu) {
            DocusaurusFolder root = project.getPhysicalDocsFolder();
            root = new DocusaurusFolder(
                    "someSidebar", "someSidebar", 0, NElements.of().ofObject().build(), root.getChildren(),
                    root.getContent(),
                    project.getPhysicalDocsFolderBasePath().toString()
            );
            String s = "module.exports = {\n" +
                    root.toJSON(1)
                    + "\n};";
            if (session.isPlainOut()) {
                session.out().print(NMsg.ofC("build sidebar %s%n", base.resolve("sidebars.js")));
                session.out().print(NMsg.ofC("\tusing release folder : %s%n", project.getPhysicalDocsFolderBasePath()));
                session.out().print(NMsg.ofC("\tusing config folder  : %s%n", project.getPhysicalDocsFolderConfigPath()));
            }
            base.resolve("sidebars.js").writeString(s);
        }
        if (isBuildPdf() && !project.getConfigAsciiDoctor().getStringByPath("path").isEmpty()) {
            Docusaurus2Asciidoctor d2a = new Docusaurus2Asciidoctor(project);
            session.out().print(NMsg.ofC("build adoc file : %s%n", d2a.getAdocFile()));
            d2a.createAdocFile();
            session.out().print(NMsg.ofC("build pdf  file : %s%n", d2a.getPdfFile()));
            d2a.createPdfFile();
        }
        if (isBuildWebSite()) {
            session.out().print(NMsg.ofC("build website%n"));
            runNativeCommand(base, getEffectiveNpmCommandPath(), "run-script", "build");
            String copyBuildPath = project.getConfigCustom().getStringByPath("copyBuildPath")
                    .get();
            if (copyBuildPath != null && copyBuildPath.length() > 0) {
                String fromPath = base.resolve("build").normalize().toString();
                NPath toPath = FileProcessorUtils.toAbsolute(NPath.of(copyBuildPath), base);
                deleteFolderIfFound(toPath, "index.html", "404.html", "sitemap.xml");
                NDocContext nDocContext = new NDocContext()
                        .setWorkingDir(fromPath)
                        .setTargetPath(toPath)
                        .setMimeTypeResolver((String path) -> MimeTypeConstants.ANY_TYPE);
                nDocContext.run(
                        new NDocProjectConfig()
                                .setTargetFolder(toPath.toString())
                                .addSource(NPath.of(fromPath).toAbsolute().toString())
                );
                nDocContext
                        .processSourceTree(NPath.of(fromPath), null);
            }
        }
        if (isStartWebSite()) {
            runNativeCommand(base, getEffectiveNpmCommandPath(), "start");
        }
    }

    public String getNdocVersion() {
        return ndocVersion;
    }

    public DocusaurusCtrl setNdocVersion(String ndocVersion) {
        this.ndocVersion = ndocVersion;
        return this;
    }

    public String getNpmCommandPath() {
        return npmCommandPath;
    }

    public DocusaurusCtrl setNpmCommandPath(String npmCommandPath) {
        this.npmCommandPath = npmCommandPath;
        return this;
    }

    public String getEffectiveNpmCommandPath() {
        String npm = getNpmCommandPath();
        if (npm == null || npm.trim().isEmpty()) {
            return "npm";
        }
        return npm;
    }

    private void runNativeCommand(NPath workFolder, String... cmd) {
        NExecCmd.of()
                .setExecutionType(NExecutionType.EMBEDDED)
                .addCommand(cmd).setDirectory(workFolder)
                .failFast().getResultCode();
    }

    public void runCommand(NPath workFolder, boolean yes, String... cmd) {
        NSession.of().copy().setConfirm(yes ? NConfirmationMode.YES : NConfirmationMode.ERROR)
                .runWith(() -> {
                    NExecCmd.of().addCommand(cmd).setDirectory(workFolder)
                            .setExecutionType(NExecutionType.EMBEDDED)
                            .failFast().getResultCode();
                });
    }

    private boolean deleteFolderIfFound(NPath toPath, String... names) {
        if (!toPath.isDirectory()) {
            return false;
        }
        for (String name : names) {
            if (!toPath.resolve(name).isRegularFile()) {
                return false;
            }
        }
        deletePath(toPath);
        return true;
    }

    private void deletePathChildren(NPath path) {
        if (path.isDirectory()) {
            path.list().forEach(
                    x -> deletePath(x)
            );
        }
    }

    private void deletePath(NPath toPath) {
        toPath.walk()
                .sorted(NComparator.of(Comparator.<NPath>reverseOrder()))
                .forEach(x -> {
                    x.delete();
                });
    }

    private Path getTargetBaseDir() {
        return Paths.get(project.getDocusaurusBaseFolder()) //                .resolve("pre-processor-target")
                ;
    }

    public NPath getPreProcessorBaseDir() {
        return NPath.of(project.getDocusaurusBaseFolder()).resolve(".dir-template");
    }

    public boolean isAutoInstallNutsPackages() {
        return autoInstallNutsPackages;
    }

    public DocusaurusCtrl setAutoInstallNutsPackages(boolean autoInstallNutsPackages) {
        this.autoInstallNutsPackages = autoInstallNutsPackages;
        return this;
    }

    public DocusaurusCtrl setVar(String name, Object value) {
        vars.put(name, value);
        return this;
    }

    public DocusaurusCtrl setVars(Map<String, Object> vars) {
        if (vars != null) {
            for (Map.Entry<String, Object> e : vars.entrySet()) {
                setVar(e.getKey(), e.getValue());
            }
        }
        return this;
    }

}
