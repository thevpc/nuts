/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.docusaurus;

import net.thevpc.commons.filetemplate.*;
import net.thevpc.commons.filetemplate.util.FileProcessorUtils;
import net.thevpc.commons.ljson.LJSON;
import net.thevpc.commons.md.convert.Docusaurus2Asciidoctor;
import net.thevpc.commons.md.docusaurus.DocusaurusFolder;
import net.thevpc.commons.md.docusaurus.DocusaurusProject;
import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsConfirmationMode;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsString;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Function;

/**
 * @author vpc
 */
public class DocusaurusCtrl {

    private DocusaurusProject project;
    private boolean buildPdf;
    private boolean buildWebSite;
    private boolean startWebSite;
    private boolean autoInstallNutsPackages;
    private String ndocVersion;
    private String npmCommandPath;
    private NutsApplicationContext appContext;

    public DocusaurusCtrl(DocusaurusProject project,NutsApplicationContext appContext) {
        this.project = project;
        this.appContext = appContext;
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
            base = Paths.get(project.getDocusaurusBaseFolder()).normalize().toRealPath();
        } catch (IOException e) {
            throw new UncheckedIOException("Invalid Docusaurus Base Folder: " + project.getDocusaurusBaseFolder(), e);
        }
        String genSidebarMenuString = project.getConfig().get("customFields.docusaurus.generateSidebarMenu").toString();
        boolean genSidebarMenu=Boolean.parseBoolean(genSidebarMenuString);
        Path basePath = base;
        Path preProcessor = getPreProcessorBaseDir();
        if (Files.isDirectory(preProcessor) && Files.isRegularFile(preProcessor.resolve(FileTemplater.PROJECT_FTEX_FILENAME))) {
//            Files.walk(base).filter(x->Files.isDirectory(base))
            try {
                Path docs = basePath.resolve("docs");
                if (Files.isDirectory(basePath.resolve("node_modules")) && Files.isRegularFile(basePath.resolve("docusaurus.config.js"))) {
                    Files.list(docs).forEach(
                            x -> deletePath(x)
                    );
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            new FileTemplater()
                    .setWorkingDir(base.toString())
                    .setMimeTypeResolver(
                            new DefaultMimeTypeResolver()
                                    .setExtensionMimeType("ftex", MimeTypeConstants.FTEX)
                                    .setNameMimeType(DocusaurusProject.DOCUSAURUS_FOLDER_CONFIG, DocusaurusProject.DOCUSAURUS_FOLDER_CONFIG_MIMETYPE)
                    )
                    .setDefaultProcessor(DocusaurusProject.DOCUSAURUS_FOLDER_CONFIG_MIMETYPE, new FolderConfigProcessor())
                    .setCustomVarEvaluator(new DocusaurusCustomVarEvaluator())
                    .processProject(
                            new TemplateConfig()
                                    .setProjectPath(preProcessor.toString())
                                    .setTargetFolder(getTargetBaseDir().toString())
                    );
        }
        if (genSidebarMenu) {
            DocusaurusFolder root = project.getPhysicalDocsFolder();
            root = new DocusaurusFolder(
                    "someSidebar", "someSidebar", 0, LJSON.NULL, root.getChildren()
            );
//            String s = "module.exports = {\n" +
//                    root.toJSON(1)
//                    + "\n};";
//            System.out.println(s);
        }
        if (isBuildPdf() && !project.getConfig().get("customFields.asciidoctor.path").isUndefined()) {
            Docusaurus2Asciidoctor d2a = new Docusaurus2Asciidoctor(project);
            d2a.run();
        }
        if (isBuildWebSite()) {
            runNativeCommand(base, getEffectiveNpmCommandPath(), "run-script", "build");
            String copyBuildPath = project.getConfig().get("customFields.copyBuildPath").asString();
            if (copyBuildPath != null && copyBuildPath.length() > 0) {
                String fromPath = null;
                try {
                    fromPath = base.resolve("build").toRealPath().toString();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                Path toPath = FileProcessorUtils.toAbsolute(Paths.get(copyBuildPath), base);
                deleteFolderIfFound(toPath, "index.html", "404.html", "sitemap.xml");
                new FileTemplater()
                        .setWorkingDir(fromPath)
                        .setTargetPath(toPath)
                        .setMimeTypeResolver((String path) -> MimeTypeConstants.ANY_TYPE)
                        .processTree(Paths.get(fromPath), null);
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
        NutsSession s=appContext.getSession();
        appContext.getWorkspace()
                .exec()
                .userCmd()
                .addCommand(cmd).setDirectory(workFolder.toString())
                .setSession(s)
                .setFailFast(true).getResult();
    }

    private void runCommand(Path workFolder, boolean yes,String... cmd) {
        NutsSession s=appContext.getSession().copy();
        if(yes){
            s=s.setConfirm(NutsConfirmationMode.YES);
        }else{
            s=s.setConfirm(NutsConfirmationMode.ERROR);
        }
        appContext.getWorkspace().exec().addCommand(cmd).setDirectory(workFolder.toString())
                .setSession(s)
                .setFailFast(true).getResult();
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

    private Path getPreProcessorBaseDir() {
        return Paths.get(project.getDocusaurusBaseFolder()).resolve("dir-template");
    }

    public boolean isAutoInstallNutsPackages() {
        return autoInstallNutsPackages;
    }

    public DocusaurusCtrl setAutoInstallNutsPackages(boolean autoInstallNutsPackages) {
        this.autoInstallNutsPackages = autoInstallNutsPackages;
        return this;
    }

    private class DocusaurusCustomVarEvaluator implements Function<String, Object> {

        public DocusaurusCustomVarEvaluator() {
        }

        @Override
        public Object apply(String varName) {
            switch (varName) {
                case "projectName": {
                    return project.getProjectName();
                }
                case "projectTitle": {
                    return project.getTitle();
                }
                default: {
                    LJSON t = project.getConfig().get(varName.replace('.', '/'));
                    if (t.isUndefined()) {
                        return null;
                    }
                    if (t.isString()) {
                        return t.asString();
                    }
                    if (t.isArray()) {
                        return t.asStringArray();
                    }
                    return t.asString();
                }
            }
        }
    }

    private class FolderConfigProcessor implements TemplateProcessor {
        public FolderConfigProcessor() {
        }

        @Override
        public void processStream(InputStream source, OutputStream target, FileTemplater context) {
            throw new IllegalArgumentException("Unsupported");
        }

        @Override
        public void processPath(Path source, String mimeType, FileTemplater context) {
            LJSON config = DocusaurusFolder.ofFolder(source.getParent(), Paths.get(context.getRootDirRequired()).resolve("docs"), 0)
                    .getConfig().get("type");
            if (
                    "javadoc".equals(config.get("name").asString())
                            || "doc".equals(config.get("name").asString())
            ) {
                String[] sources = config.get("sources").asStringArray();
                if (sources == null || sources.length == 0) {
                    throw new IllegalArgumentException("Missing doc sources in " + source);
                }
                String[] packages = config.get("packages").asStringArray();
                String target = context.getPathTranslator().translatePath(source.getParent().toString());
                if (target == null) {
                    throw new IllegalArgumentException("Invalid source " + source.getParent());
                }
                ArrayList<String> cmd = new ArrayList<>();
//                cmd.add("--bot");
                cmd.add("ndoc" + ((getNdocVersion() == null || getNdocVersion().isEmpty()) ? "" : "#" + (getNdocVersion())));
                cmd.add("--backend=docusaurus");
                for (String s : sources) {
                    s=context.processString(s, MimeTypeConstants.PLACEHOLDER_DOLLARS);
                    cmd.add("--source");
                    cmd.add(FileProcessorUtils.toAbsolutePath(Paths.get(s),source.getParent()).toString());
                }
                if(packages!=null) {
                    for (String s : packages) {
                        s=context.processString(s, MimeTypeConstants.PLACEHOLDER_DOLLARS);
                        cmd.add("--package");
                        cmd.add(s);
                    }
                }
                cmd.add("--target");
                cmd.add(target);
                FileProcessorUtils.mkdirs(Paths.get(target));
                runCommand(Paths.get(target), autoInstallNutsPackages,cmd.toArray(new String[0]));
            }

        }

        @Override
        public String toString() {
            return "DocusaurusFolderConfig";
        }
    }
}
