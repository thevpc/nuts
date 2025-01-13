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
        Path base = null;
        try {
            base = Paths.get(project.getDocusaurusBaseFolder()).toAbsolutePath().normalize().toRealPath();
        } catch (IOException e) {
            throw new UncheckedIOException("invalid Docusaurus Base Folder: " + project.getDocusaurusBaseFolder(), e);
        }
        boolean genSidebarMenu = project.getConfigDocusaurusExtra()
                .asObject().orElse(NObjectElement.ofEmpty())
                .getBooleanByPath("generateSidebarMenu")
                .orElse(false);
        Path basePath = base;
        Path preProcessor = getPreProcessorBaseDir();
        NSession session = NSession.of();
        if (preProcessor != null && Files.isDirectory(preProcessor)) {
//            Files.walk(base).filter(x->Files.isDirectory(base))
            Path docs = basePath.resolve("docs");
            if (Files.isDirectory(basePath.resolve("node_modules"))
                    && Files.isRegularFile(basePath.resolve("docusaurus.config.js"))) {
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
            try {
                Files.write(base.resolve("sidebars.js"), s.getBytes());
            } catch (IOException e) {
                throw new NIOException(NMsg.ofC("%s", e));
            }
//            System.out.println(s);
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
                String fromPath = null;
                try {
                    fromPath = base.resolve("build").toRealPath().toString();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                Path toPath = FileProcessorUtils.toAbsolute(Paths.get(copyBuildPath), base);
                deleteFolderIfFound(toPath, "index.html", "404.html", "sitemap.xml");
                NDocContext nDocContext = new NDocContext()
                        .setWorkingDir(fromPath)
                        .setTargetPath(toPath)
                        .setMimeTypeResolver((String path) -> MimeTypeConstants.ANY_TYPE);
                nDocContext.run(
                        new NDocProjectConfig()
                                .setTargetFolder(toPath.toString())
                                .addSource(Paths.get(fromPath).toAbsolutePath().toString())
                );
                nDocContext
                        .processSourceTree(Paths.get(fromPath), null);
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

    private void runNativeCommand(Path workFolder, String... cmd) {
        NExecCmd.of()
                .setExecutionType(NExecutionType.EMBEDDED)
                .addCommand(cmd).setDirectory(NPath.of(workFolder))
                .failFast().getResultCode();
    }

    public void runCommand(Path workFolder, boolean yes, String... cmd) {
        NSession.of().copy().setConfirm(yes ? NConfirmationMode.YES : NConfirmationMode.ERROR)
                .runWith(() -> {
                    NExecCmd.of().addCommand(cmd).setDirectory(NPath.of(workFolder))
                            .setExecutionType(NExecutionType.EMBEDDED)
                            .failFast().getResultCode();
                });
    }

    private boolean deleteFolderIfFound(Path toPath, String... names) {
        if (!Files.isDirectory(toPath)) {
            return false;
        }
        for (String name : names) {
            if (!Files.isRegularFile(toPath.resolve(name))) {
                return false;
            }
        }
        deletePath(toPath);
        return true;
    }

    private void deletePathChildren(Path path) {
        if (Files.isDirectory(path)) {
            try {
                Files.list(path).forEach(
                        x -> deletePath(x)
                );
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private void deletePath(Path toPath) {
        try {
            Files.walk(toPath)
                    .sorted(Comparator.reverseOrder())
                    .forEach(x -> {
                        try {
                            Files.delete(x);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path getTargetBaseDir() {
        return Paths.get(project.getDocusaurusBaseFolder()) //                .resolve("pre-processor-target")
                ;
    }

    public Path getPreProcessorBaseDir() {
        return Paths.get(project.getDocusaurusBaseFolder()).resolve(".dir-template");
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
