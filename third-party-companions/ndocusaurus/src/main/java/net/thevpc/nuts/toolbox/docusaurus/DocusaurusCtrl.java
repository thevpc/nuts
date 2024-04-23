/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.docusaurus;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NArrayElement;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NSessionTerminal;
import net.thevpc.nuts.toolbox.nsh.eval.NShellContext;
import net.thevpc.nuts.toolbox.nsh.nshell.*;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellVar;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellVarListener;
import net.thevpc.nuts.toolbox.nsh.nodes.NShellVariables;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.*;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.util.FileProcessorUtils;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.util.StringUtils;
import net.thevpc.nuts.util.NMsg;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

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
    private NSession session;

    public DocusaurusCtrl(DocusaurusProject project, NSession session) {
        this.project = project;
        this.session = session;
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
        NSession session = project.getSession();
        Path base = null;
        try {
            base = Paths.get(project.getDocusaurusBaseFolder()).toAbsolutePath().normalize().toRealPath();
        } catch (IOException e) {
            throw new UncheckedIOException("invalid Docusaurus Base Folder: " + project.getDocusaurusBaseFolder(), e);
        }
        boolean genSidebarMenu = project.getConfigDocusaurusExtra()
                .asObject().orElse(NObjectElement.ofEmpty(session))
                .getBooleanByPath("generateSidebarMenu")
                .orElse(false);
        Path basePath = base;
        Path preProcessor = getPreProcessorBaseDir();
        if (Files.isDirectory(preProcessor) && Files.isRegularFile(preProcessor.resolve("project.nsh"))) {
//            Files.walk(base).filter(x->Files.isDirectory(base))
            Path docs = basePath.resolve("docs");
            if (Files.isDirectory(basePath.resolve("node_modules")) && Files.isRegularFile(basePath.resolve("docusaurus.config.js"))) {
                session.out().print(NMsg.ofC("clear folder %s%n", docs));
                deletePathChildren(docs);
            }

            session.out().print(NMsg.ofC("process template %s -> %s%n", preProcessor, getTargetBaseDir()));
            TemplateConfig config = new TemplateConfig()
                    .setProjectPath(preProcessor.toString())
                    .setTargetFolder(getTargetBaseDir().toString())
                    .setContextName("ndocusaurus")
                    ;
            new NFileTemplater(session)
                    .setWorkingDir(base.toString())
                    .setMimeTypeResolver(
                            new DefaultMimeTypeResolver()
                                    .setExtensionMimeType("ftex", MimeTypeConstants.FTEX)
                                    .setNameMimeType(DocusaurusProject.DOCUSAURUS_FOLDER_CONFIG, DocusaurusProject.DOCUSAURUS_FOLDER_CONFIG_MIMETYPE)
                    )
                    .setDefaultProcessor(DocusaurusProject.DOCUSAURUS_FOLDER_CONFIG_MIMETYPE, new FolderConfigProcessor())
                    .setCustomVarEvaluator(new DocusaurusCustomVarEvaluator(project))
                    .processProject(config);
        }
        if (genSidebarMenu) {
            DocusaurusFolder root = project.getPhysicalDocsFolder();
            root = new DocusaurusFolder(
                    "someSidebar", "someSidebar", 0, NElements.of(session).ofObject().build(), root.getChildren(),
                    root.getContent(session),
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
                throw new NIOException(session, NMsg.ofC("%s", e));
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
                    .get(session);
            if (copyBuildPath != null && copyBuildPath.length() > 0) {
                String fromPath = null;
                try {
                    fromPath = base.resolve("build").toRealPath().toString();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                Path toPath = FileProcessorUtils.toAbsolute(Paths.get(copyBuildPath), base);
                deleteFolderIfFound(toPath, "index.html", "404.html", "sitemap.xml");
                new FileTemplater(session)
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
        NExecCmd.of(session)
                .setExecutionType(NExecutionType.EMBEDDED)
                .addCommand(cmd).setDirectory(NPath.of(workFolder, session))
                .failFast().getResultCode();
    }

    private void runCommand(Path workFolder, boolean yes, String... cmd) {
        NSession s = session.copy();
        if (yes) {
            s = s.setConfirm(NConfirmationMode.YES);
        } else {
            s = s.setConfirm(NConfirmationMode.ERROR);
        }
        NExecCmd.of(s).addCommand(cmd).setDirectory(NPath.of(workFolder, session))
                .setExecutionType(NExecutionType.EMBEDDED)
                .failFast().getResultCode();
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

    private Path getPreProcessorBaseDir() {
        return Paths.get(project.getDocusaurusBaseFolder()).resolve(".dir-template");
    }

    public boolean isAutoInstallNutsPackages() {
        return autoInstallNutsPackages;
    }

    public DocusaurusCtrl setAutoInstallNutsPackages(boolean autoInstallNutsPackages) {
        this.autoInstallNutsPackages = autoInstallNutsPackages;
        return this;
    }

    private static class NshEvaluator implements ExprEvaluator {
        private NSession session;
        private NShell shell;
        private FileTemplater fileTemplater;

        public NshEvaluator(NSession session, FileTemplater fileTemplater) {
            this.session = session;
            this.fileTemplater = fileTemplater;
            shell = new NShell(new NShellConfiguration().setSession(session).setIncludeDefaultBuiltins(true).setIncludeExternalExecutor(true));
            shell.getRootContext().setSession(shell.getRootContext().getSession().copy());
            shell.getRootContext().vars().addVarListener(
                    new NShellVarListener() {
                        @Override
                        public void varAdded(NShellVar nShellVar, NShellVariables vars, NShellContext context) {
                            setVar(nShellVar.getName(), nShellVar.getValue());
                        }

                        @Override
                        public void varValueUpdated(NShellVar nShellVar, String oldValue, NShellVariables vars, NShellContext context) {
                            setVar(nShellVar.getName(), nShellVar.getValue());
                        }

                        @Override
                        public void varRemoved(NShellVar nShellVar, NShellVariables vars, NShellContext context) {
                            setVar(nShellVar.getName(), null);
                        }
                    }
            );
            shell.getRootContext()
                    .builtins()
                    .set(new ProcessCmd(fileTemplater));
        }

        public void setVar(String varName, String newValue) {
            fileTemplater.getLog().debug("eval", varName + "=" + StringUtils.toLiteralString(newValue));
            fileTemplater.setVar(varName, newValue);
        }

        @Override
        public Object eval(String content, FileTemplater context) {
            NSession session = context.getSession().copy();
            session.setTerminal(NSessionTerminal.ofMem(session));
            NShellContext ctx = shell.createInlineContext(
                    shell.getRootContext(),
                    context.getSourcePath().orElseGet(() -> "nsh"), new String[0]
            );
            ctx.setSession(session);
            shell.executeScript(content, ctx);
            return session.out().toString();
        }

        @Override
        public String toString() {
            return "nsh";
        }

    }

    private static class NFileTemplater extends FileTemplater {
        public NFileTemplater(NSession session) {
            super(session.copy().setAppId(null).setAppArguments(new String[0]));
            this.setDefaultExecutor("text/ntemplate-nsh-project", new NshEvaluator(getSession(), this));
            setProjectFileName("project.nsh");
        }

        public void executeProjectFile(Path path, String mimeTypesString) {
            executeRegularFile(path, "text/ntemplate-nsh-project");
        }
    }

    private static class DocusaurusCustomVarEvaluator implements Function<String, Object> {
        NElement config;
        String projectName;
        String projectTitle;
        DocusaurusProject project;

        public DocusaurusCustomVarEvaluator(DocusaurusProject project) {
            this.project = project;
            config = project.getConfig();
            projectName = project.getProjectName();
            projectTitle = project.getTitle();
        }

        @Override
        public Object apply(String varName) {
            NSession session = project.getSession();
            switch (varName) {
                case "projectName": {
                    return projectName;
                }
                case "projectTitle": {
                    return projectTitle;
                }
                default: {
                    String[] a = Arrays.stream(varName.split("[./]")).map(String::trim).filter(x -> !x.isEmpty())
                            .toArray(String[]::new);
                    NElement config=this.config;
                    if(config!=null) {
                        for (String s : a) {
                            config = config.asObject().orElse(NObjectElement.ofEmpty(session)).get(s).orNull();
                            if (config == null) {
                                return null;
                            }
                        }
                    }
                    if(config==null){
                        return null;
                    }
                    if (config.isNull()) {
                        return null;
                    }
                    if (config.isString()) {
                        return config.asString().get(session);
                    }
                    if (config.isArray()) {
                        return config.asArray().get(session).stream().map(Object::toString).toArray(String[]::new);
                    }
                    return config.asString().get(session);
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
            NSession session = context.getSession();
            NObjectElement config = DocusaurusFolder.ofFolder(session, source.getParent(),
                            Paths.get(context.getRootDirRequired()).resolve("docs"),
                            getPreProcessorBaseDir().resolve("src"),
                            0)
                    .getConfig().getObject("type").get(session);
            if (
                    "javadoc".equals(config.getString("name").orNull())
                            || "doc".equals(config.getString("name").orNull())
            ) {
                String[] sources = config.getArray("sources").orElse(NArrayElement.ofEmpty(session))
                        .stream().map(x -> x.asString().orElse(null))
                        .filter(Objects::nonNull).toArray(String[]::new);
                if (sources.length == 0) {
                    throw new IllegalArgumentException("missing doc sources in " + source);
                }
                String[] packages = config.getArray("packages").orElse(NArrayElement.ofEmpty(session))
                        .stream().map(x -> x.asString().orNull()).filter(Objects::nonNull).toArray(String[]::new);
                String target = context.getPathTranslator().translatePath(source.getParent().toString());
                if (target == null) {
                    throw new IllegalArgumentException("invalid source " + source.getParent());
                }
                ArrayList<String> cmd = new ArrayList<>();
//                cmd.add("--bot");
                cmd.add("ndoc" + ((getNdocVersion() == null || getNdocVersion().isEmpty()) ? "" : "#" + (getNdocVersion())));
                cmd.add("--backend=docusaurus");
                for (String s : sources) {
                    s = context.processString(s, MimeTypeConstants.PLACEHOLDER_DOLLARS);
                    cmd.add("--source");
                    cmd.add(FileProcessorUtils.toAbsolutePath(Paths.get(s), source.getParent()).toString());
                }
                for (String s : packages) {
                    s = context.processString(s, MimeTypeConstants.PLACEHOLDER_DOLLARS);
                    cmd.add("--package");
                    cmd.add(s);
                }
                cmd.add("--target");
                cmd.add(target);
                FileProcessorUtils.mkdirs(Paths.get(target));
                runCommand(Paths.get(target), autoInstallNutsPackages, cmd.toArray(new String[0]));
            }

        }

        @Override
        public String toString() {
            return "DocusaurusFolderConfig";
        }
    }

}
