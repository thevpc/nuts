/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.docusaurus;

import net.thevpc.nuts.*;
import net.thevpc.nuts.lib.md.convert.Docusaurus2Asciidoctor;
import net.thevpc.nuts.lib.md.docusaurus.DocusaurusFolder;
import net.thevpc.nuts.lib.md.docusaurus.DocusaurusProject;
import net.thevpc.nuts.toolbox.nsh.AbstractNshBuiltin;
import net.thevpc.nuts.toolbox.nsh.NshExecutionContext;
import net.thevpc.nuts.toolbox.nsh.NutsJavaShell;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.*;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.*;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.util.FileProcessorUtils;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.util.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Function;
import java.util.logging.Level;

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
        String genSidebarMenuString = project.getConfig()
                .getSafeObject("customFields")
                .getSafeObject("docusaurus")
                .getSafe("generateSidebarMenu")
                .toString();
        boolean genSidebarMenu=Boolean.parseBoolean(genSidebarMenuString);
        Path basePath = base;
        Path preProcessor = getPreProcessorBaseDir();
        if (Files.isDirectory(preProcessor) && Files.isRegularFile(preProcessor.resolve("project.nsh"))) {
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
            TemplateConfig config = new TemplateConfig()
                    .setProjectPath(preProcessor.toString())
                    .setTargetFolder(getTargetBaseDir().toString());
            new NFileTemplater(appContext)
                    .setWorkingDir(base.toString())
                    .setMimeTypeResolver(
                            new DefaultMimeTypeResolver()
                                    .setExtensionMimeType("ftex", MimeTypeConstants.FTEX)
                                    .setNameMimeType(DocusaurusProject.DOCUSAURUS_FOLDER_CONFIG, DocusaurusProject.DOCUSAURUS_FOLDER_CONFIG_MIMETYPE)
                    )
                    .setDefaultProcessor(DocusaurusProject.DOCUSAURUS_FOLDER_CONFIG_MIMETYPE, new FolderConfigProcessor())
                    .setCustomVarEvaluator(new DocusaurusCustomVarEvaluator())
                    .processProject(
                            config
                    );
        }
        if (genSidebarMenu) {
            DocusaurusFolder root = project.getPhysicalDocsFolder();
            root = new DocusaurusFolder(
                    "someSidebar", "someSidebar", 0, appContext.getWorkspace().elem().forNull(), root.getChildren()
            );
            String s = "module.exports = {\n" +
                    root.toJSON(1)
                    + "\n};";
            try {
                Files.write(base.resolve("sidebars.js"),s.getBytes());
            } catch (IOException e) {
                throw new NutsIOException(appContext.getSession(),e.getMessage());
            }
//            System.out.println(s);
        }
        if (isBuildPdf() && !project.getConfig().getSafeObject("customFields")
                .getSafeObject("asciidoctor")
                .getSafe("path")
                .isNull()) {
            Docusaurus2Asciidoctor d2a = new Docusaurus2Asciidoctor(project);
            d2a.run();
        }
        if (isBuildWebSite()) {
            runNativeCommand(base, getEffectiveNpmCommandPath(), "run-script", "build");
            String copyBuildPath = project.getConfig().getSafeObject("customFields")
                    .getSafe("copyBuildPath")
                    .asString();
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
                .embedded()
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
                .setExecutionType(NutsExecutionType.EMBEDDED)
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
        return Paths.get(project.getDocusaurusBaseFolder()).resolve(".dir-template");
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
                    NutsElement t = project.getConfig().get(varName.replace('.', '/'));
                    if (t.isNull()) {
                        return null;
                    }
                    if (t.isString()) {
                        return t.asString();
                    }
                    if (t.isArray()) {
                        return t.asArray().stream().map(x->x.toString()).toArray(String[]::new);
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
            NutsElement config = DocusaurusFolder.ofFolder(appContext.getSession(), source.getParent(), Paths.get(context.getRootDirRequired()).resolve("docs"), 0)
                    .getConfig().asObject().getSafe("type");
            if (
                    "javadoc".equals(config.asObject().get("name").asString())
                            || "doc".equals(config.asObject().get("name").asString())
            ) {
                String[] sources = config.asSafeObject().getSafeArray("sources")
                        .stream().map(x->x.toString()).toArray(String[]::new);
                if (sources.length == 0) {
                    throw new IllegalArgumentException("missing doc sources in " + source);
                }
                String[] packages = config.asSafeObject().getSafeArray("packages").stream().map(x->x.toString()).toArray(String[]::new);
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


    private static class NshEvaluator implements ExprEvaluator {
        private NutsApplicationContext appContext;
        private NutsJavaShell shell;
        private FileTemplater fileTemplater;

        public NshEvaluator(NutsApplicationContext appContext, FileTemplater fileTemplater) {
            this.appContext = appContext;
            this.fileTemplater = fileTemplater;
            shell = new NutsJavaShell(appContext,new String[0]);
            shell.setSession(shell.getSession().copy());
            shell.getRootContext().vars().addVarListener(
                    new JShellVarListener() {
                        @Override
                        public void varAdded(JShellVar jShellVar, JShellVariables vars, JShellContext context) {
                            setVar(jShellVar.getName(), jShellVar.getValue());
                        }

                        @Override
                        public void varValueUpdated(JShellVar jShellVar, String oldValue, JShellVariables vars, JShellContext context) {
                            setVar(jShellVar.getName(), jShellVar.getValue());
                        }

                        @Override
                        public void varRemoved(JShellVar jShellVar, JShellVariables vars, JShellContext context) {
                            setVar(jShellVar.getName(), null);
                        }
                    }
            );
            shell.getRootContext()
                    .builtins()
                    .set(
                            new AbstractNshBuiltin("process", 10) {
                                @Override
                                public void exec(String[] args, NshExecutionContext context) {
                                    if (args.length != 1) {
                                        throw new IllegalStateException(getName() + " : invalid arguments count");
                                    }
                                    String pathString = args[0];
                                    fileTemplater.getLog().debug("eval", getName() + "(" + StringUtils.toLiteralString(pathString) + ")");
                                    fileTemplater.executeRegularFile(Paths.get(pathString), null);
                                }
                            }
                    );
        }

        public void setVar(String varName, String newValue) {
            fileTemplater.getLog().debug("eval", varName + "=" + StringUtils.toLiteralString(newValue));
            fileTemplater.setVar(varName, newValue);
        }

        @Override
        public Object eval(String content, FileTemplater context) {
            NutsPrintStream out = shell.getWorkspace().io().createMemoryPrintStream();
            shell.getSession().setTerminal(
                    shell.getWorkspace().term()
                            .createTerminal(
                                    new ByteArrayInputStream(new byte[0]),
                                    out,
                                    out)
            );
            JShellFileContext ctx = shell.createSourceFileContext(
                    shell.getRootContext(),
                    context.getSourcePath().orElseGet(()->"nsh"),new String[0]
            );
            shell.executeString(content,ctx);
            return out.toString();
        }

        @Override
        public String toString() {
            return "nsh";
        }
    }

    private static class NFileTemplater extends FileTemplater {
        public NFileTemplater(NutsApplicationContext appContext) {
            this.setDefaultExecutor("text/ntemplate-nsh-project", new NshEvaluator(appContext, this));
            setProjectFileName("project.nsh");
            this.setLog(new TemplateLog() {
                NutsLoggerOp logOp;
                @Override
                public void info(String title, String message) {
                    log().verb(NutsLogVerb.INFO).level(Level.FINER)
                            .log( "{0} : {1}",title,message);
                }

                @Override
                public void debug(String title, String message) {
                    log().verb(NutsLogVerb.DEBUG).level(Level.FINER)
                            .log( "{0} : {1}",title,message);
                }

                @Override
                public void error(String title, String message) {
                    log().verb(NutsLogVerb.FAIL).level(Level.FINER).log( "{0} : {1}",title,message);
                }

                private NutsLoggerOp log() {
                    if(logOp==null) {
                        logOp = appContext.getWorkspace().log().of(DocusaurusCtrl.class)
                                .with().session(appContext.getSession())
                                .style(NutsTextFormatStyle.JSTYLE)
                        ;
                    }
                    return logOp;
                }
            });
        }

        public void executeProjectFile(Path path, String mimeTypesString) {
            executeRegularFile(path,"text/ntemplate-nsh-project");
        }
    }

}
