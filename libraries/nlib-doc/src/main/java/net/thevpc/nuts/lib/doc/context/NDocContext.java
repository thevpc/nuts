/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.doc.context;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.lib.doc.*;
import net.thevpc.nuts.lib.doc.executor.NDocExecutor;
import net.thevpc.nuts.lib.doc.executor.NDocExecutorManager;
import net.thevpc.nuts.lib.doc.javadoc.MdDoclet;
import net.thevpc.nuts.lib.doc.javadoc.MdDocletConfig;
import net.thevpc.nuts.lib.doc.mimetype.DefaultNDocMimeTypeResolver;
import net.thevpc.nuts.lib.doc.mimetype.NDocMimeTypeResolver;
import net.thevpc.nuts.lib.doc.processor.*;
import net.thevpc.nuts.lib.doc.util.FileProcessorUtils;
import net.thevpc.nuts.lib.doc.util.StringUtils;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;

/**
 * @author thevpc
 */
public class NDocContext {

    public static final String ROOT_DIR = "rootDir";
    public static final String WORKING_DIR = "workingDir";
    public static final String SOURCE_PATH = "sourcePath";
    public static final String PROJECT_FILENAME = "project.nexpr";

    private final Map<String, Object> vars = new HashMap<>();
    private NDocContext parent;
    private Function<String, Object> customVarEvaluator;
    private NDocMimeTypeResolver mimeTypeResolver;
    private String sourcePath;
    private String rootDir;
    private String workingDir;
    private boolean userParentProperties;
    private NDocPathTranslator pathTranslator;
    private String projectFileName = PROJECT_FILENAME;
    private String contextName;
    private String currentMimeType;
    private NDocExecutorManager executorManager;
    private NDocProcessorManager processorManager;

    public NDocContext() {
        executorManager=new NDocExecutorManager(this);
        processorManager=new NDocProcessorManager(this);
        this.setLog(new NDocLog() {
            NLogOp logOp;

            @Override
            public void info(String title, String message) {
                log().verb(NLogVerb.INFO).level(Level.FINER)
                        .log(NMsg.ofC("%s : %s", title, message));
            }

            @Override
            public void debug(String title, String message) {
                log().verb(NLogVerb.DEBUG).level(Level.FINER)
                        .log(NMsg.ofC("%s : %s", title, message));
            }

            @Override
            public void error(String title, String message) {
                log().verb(NLogVerb.FAIL).level(Level.FINER)
                        .log(NMsg.ofC("%s : %s", title, message));
            }

            private NLogOp log() {
                if (logOp == null) {
                    logOp = NLog.of(NDocContext.class)
                            .with()
                    ;
                }
                return logOp;
            }
        });
    }

    public NDocContext(NDocContext parent) {
        this();
        this.parent = parent;
    }

    public NDocProcessorManager getProcessorManager() {
        return processorManager;
    }

    private static Path workingPath(Path p) {
        if (Files.isDirectory(p)) {
            return p;
        }
        Path pp = p.getParent();
        if (pp == null) {
            throw new IllegalArgumentException("Unsupported");
        }
        return pp;
    }

    public boolean isUserParentProperties() {
        return userParentProperties;
    }

    public NDocContext setUserParentProperties(boolean userParentProperties) {
        this.userParentProperties = userParentProperties;
        return this;
    }

    public String getProjectFileName() {
        return projectFileName;
    }

    public NDocContext setProjectFileName(String projectFileName) {
        if (projectFileName == null || projectFileName.isEmpty()) {
            projectFileName = PROJECT_FILENAME;
        }
        this.projectFileName = projectFileName;
        return this;
    }

    public NDocContext newChild() {
        return new NDocContext(this);
    }

    public String getContextName() {
        if (contextName != null) {
            return contextName;
        }
        if (parent != null) {
            return parent.getContextName();
        }
        return null;
    }

    public NDocExecutorManager getExecutorManager() {
        return executorManager;
    }



    public Function<String, Object> getCustomVarEvaluator() {
        if (customVarEvaluator != null) {
            return customVarEvaluator;
        }
        if (parent != null) {
            return parent.getCustomVarEvaluator();
        }
        return null;
    }

    public NDocContext setCustomVarEvaluator(Function<String, Object> customVarEvaluator) {
        this.customVarEvaluator = customVarEvaluator;
        return this;
    }



    public NDocLog getLog() {
        NDocLog ll = (NDocLog) getVar("log", null);
        if (ll == null) {
            return DefaultNDocLog.INSTANCE;
        }
        return ll;
    }

    public NDocContext setLog(NDocLog log) {
        return setVar("log", log);
    }

    public NDocContext getParent() {
        return parent;
    }

    public NDocContext setParent(NDocContext parent) {
        this.parent = parent;
        return this;
    }

    public String getWorkingDirRequired() {
        return (String) getVar(WORKING_DIR).get();
    }

    public String getRootDirRequired() {
        return (String) getVar(ROOT_DIR).get();
    }

    public NOptional<String> getWorkingDir() {
        return (NOptional) getVar(WORKING_DIR);
    }

    public NDocContext setWorkingDir(String workingDir) {
        return setVar(WORKING_DIR, workingDir);
    }

    public NOptional<String> getRootDir() {
        return (NOptional) getVar(ROOT_DIR);
    }

    public NDocContext setRootDir(String rootDir) {
        return setVar(ROOT_DIR, rootDir);
    }

    public NDocContext removeWorkingDir() {
        return remove(WORKING_DIR);
    }

    public NDocContext removeRootDir() {
        return remove(ROOT_DIR);
    }

    public String getSourcePathRequired() {
        return (String) getVar(SOURCE_PATH).get();
    }

    public NOptional<String> getSourcePath() {
        return getVar(SOURCE_PATH);
    }

    public NDocContext setSourcePath(String workingDir) {
        return setVar(SOURCE_PATH, workingDir);
    }

    public boolean isSet(String name) {
        switch (name) {
            case ROOT_DIR:
            case SOURCE_PATH:
            case WORKING_DIR: {
                return true;
            }
        }
        return vars.containsKey(name);
    }

    public void setVars(Map<String, Object> vars) {
        if (vars != null) {
            for (Map.Entry<String, Object> e : vars.entrySet()) {
                setVar(e.getKey(), e.getValue());
            }
        }
    }

    public NDocContext setVar(String name, Object value) {
        switch (name) {
            case SOURCE_PATH: {
                this.sourcePath = (String) value;
                return this;
            }
            case WORKING_DIR: {
                this.workingDir = (String) value;
                return this;
            }
            case ROOT_DIR: {
                this.rootDir = (String) value;
                return this;
            }
        }
        if (parent != null && userParentProperties) {
            parent.setVar(name, value);
            return this;
        }
        vars.put(name, value);
        return this;
    }

    public NDocContext remove(String name) {
        switch (name) {
            case ROOT_DIR:
            case SOURCE_PATH:
            case WORKING_DIR: {
                setVar(name, null);
                return this;
            }
        }
        if (parent != null && userParentProperties) {
            parent.remove(name);
            return this;
        }
        vars.remove(name);
        return this;
    }

    public Map<String, Object> getVars() {
        Map<String, Object> result = new HashMap<>(vars);
        if (parent != null) {
            for (Map.Entry<String, Object> e : parent.getVars().entrySet()) {
                if (!result.containsKey(e.getKey())) {
                    result.put(e.getKey(), e.getValue());
                }
            }
        }
        return result;
    }

    public Object getVar(String name, Object defaultValue) {
        return getVar(name).orElse(defaultValue);
    }

    public <T> NOptional<T> getVar(String name) {
        switch (name) {
            case SOURCE_PATH: {
                if (this.sourcePath != null) {
                    return (NOptional<T>) NOptional.of(this.sourcePath);
                }
                break;
            }
            case WORKING_DIR: {
                if (this.workingDir != null) {
                    return (NOptional<T>) NOptional.of(this.workingDir);
                }
                break;
            }
            case ROOT_DIR: {
                if (this.rootDir != null) {
                    return (NOptional<T>) NOptional.of(this.rootDir);
                }
                break;
            }
        }
        T r = (T) vars.get(name);
        if (r != null) {
            return NOptional.of(r);
        }
        if (vars.containsKey(name)) {
            return NOptional.ofNull();
        }
        if (customVarEvaluator != null) {
            r = (T) customVarEvaluator.apply(name);
            if (r != null) {
                return NOptional.of(r);
            }
        }
        if (parent != null) {
            return parent.getVar(name);
        }
        return NOptional.ofEmpty(() -> {
            String source = getSourcePath().orElse(null);
            if (source == null) {
                return NMsg.ofC("not found : %s", StringUtils.escapeString(name));
            } else {
                return NMsg.ofC("not found : %s in %s", StringUtils.escapeString(name), source);
            }
        });
    }



//    public void processResourceTree(Path path, String targetFolder, Predicate<Path> filter) {
//        getProcessorManager().
//    }

    public void executeProjectFile(Path path, String mimeTypesString) {
        getExecutorManager().executeRegularFile(path, mimeTypesString);
    }






    public Path toAbsolutePath(Path path) {
        return FileProcessorUtils.toRealPath(path, Paths.get(getWorkingDirRequired()));
    }

    public String executeString(String source, String mimeType) {
        return executeStream(new ByteArrayInputStream(source.getBytes()), mimeType);
    }

    public Object eval(String source, String mimeType) {
        return eval(new ByteArrayInputStream(source == null ? new byte[0] : source.getBytes()), mimeType);
    }

    public Object eval(InputStream source, String mimeType) {
        String[] mimeTypes = FileProcessorUtils.splitMimeTypes(mimeType);
        String lastError=null;
        for (String mimeType0 : mimeTypes) {
            NDocExecutor proc = null;
            proc = getExecutorManager().getExecutor(mimeType0);
            if (proc != null) {
                try {
                    return proc.eval(source,
                            newChild()
                                    .setUserParentProperties(true)
                    );
                } catch (Exception ex) {
                    lastError = "error processing mimeType : " + mimeType + ". " + ex.toString();
                    getLog().error("file", lastError);
                }
            }
        }
        if(lastError!=null){
            throw new NIllegalArgumentException(NMsg.ofC("%s", lastError));
        }
        throw new NIllegalArgumentException(NMsg.ofC("unsupported mimetype : %s", mimeType));
    }

    public String executeStream(InputStream source, String mimeType) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            String[] mimeTypes = FileProcessorUtils.splitMimeTypes(mimeType);
            for (String mimeType0 : mimeTypes) {
                NDocExecutor proc = null;
                proc = getExecutorManager().getExecutor(mimeType0);
                if (proc != null) {
                    try {
                        proc.processStream(source, bos,
                                newChild()
                                        .setUserParentProperties(true)
                        );
                    } catch (Exception ex) {
                        getLog().error("file", "error processing mimeType : " + mimeType + ". " + ex.toString());
                    }
                    return bos.toString();
                }
            }
            throw new NIllegalArgumentException(NMsg.ofC("unsupported mimetype : %s", mimeType));
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }



    public NDocMimeTypeResolver getMimeTypeResolver() {
        if (mimeTypeResolver != null) {
            return mimeTypeResolver;
        }
        if (parent != null) {
            return parent.getMimeTypeResolver();
        }
        return DefaultNDocMimeTypeResolver.DEFAULT;
    }

    public NDocContext setMimeTypeResolver(NDocMimeTypeResolver mimeTypeResolver) {
        this.mimeTypeResolver = mimeTypeResolver;
        return this;
    }

    public NDocPathTranslator getPathTranslator() {
        if (pathTranslator != null) {
            return pathTranslator;
        }
        if (parent != null) {
            return parent.getPathTranslator();
        }
        return null;
    }

    public NDocContext setPathTranslator(NDocPathTranslator pathTranslator) {
        this.pathTranslator = pathTranslator;
        return this;
    }

    public NDocContext setPathTranslator(Path from, Path to) {
        return setPathTranslator(new DefaultNDocPathTranslator(from, to));
    }

    public NDocContext setTargetPath(Path to) {
        return setPathTranslator(new DefaultNDocPathTranslator(Paths.get(getWorkingDirRequired()), to));
    }

    public String getCurrentMimeType() {
        return currentMimeType;
    }

    public void setCurrentMimeType(String currentMimeType) {
        this.currentMimeType = currentMimeType;
    }

    public void run(NDocProjectConfig config) {
        String scriptType = config.getScriptType();
        String targetFolder = config.getTargetFolder();
        setVars(config.getVars());
        this.contextName = NStringUtils.trimToNull(config.getContextName());
        String projectPath = config.getProjectPath();
        boolean projectFolderSpecified = !NBlankable.isBlank(projectPath);
        List<String> initScripts = new ArrayList<>(config.getInitScripts());
        List<String> paths = new ArrayList<>(config.getSourcePaths());
        if (!projectFolderSpecified) {
            if (NBlankable.isBlank(targetFolder)) {
                throw new NIllegalArgumentException(NMsg.ofPlain("missing target folder"));
            }
        } else {
            Path oProjectDirPath = Paths.get(projectPath);
            Path oProjectFile = oProjectDirPath.resolve(getProjectFileName());
            Path oProjectSrc = oProjectDirPath.resolve("src");
            if (!Files.isDirectory(oProjectSrc)) {
                throw new NIllegalArgumentException(NMsg.ofC("invalid project, missing 'src/' folder : %s", oProjectDirPath));
            }
            if (!Files.isRegularFile(oProjectFile)) {
                throw new NIllegalArgumentException(NMsg.ofC("invalid project, missing project.nexpr : %s", oProjectDirPath));
            }
            initScripts.add(oProjectFile.toString());
            paths.add(oProjectSrc.toString());
        }

        if (config.getSourcePaths().isEmpty()) {
            throw new NIllegalArgumentException(NMsg.ofPlain("missing path to process"));
        }

        if (scriptType != null) {
            if (scriptType.indexOf('/') < 0) {
                scriptType = "text/" + scriptType;
            }
        } else {
            //scriptType = MimeTypeConstants.APPLICATION_SIMPLE_EXPR;
        }
        for (String initScript : initScripts) {
            Path fpath = Paths.get(initScript).toAbsolutePath();
            this.setWorkingDir(workingPath(fpath).toString());
            this.executeProjectFile(fpath, scriptType);
        }
        if (projectFolderSpecified) {
            //&& targetFolder==null
            String tf = (String) this.getVar("targetFolder", null);
            if (tf != null && targetFolder == null) {
                targetFolder = tf;
            }
        }
        if (targetFolder == null) {
            throw new NIllegalArgumentException(NMsg.ofPlain("missing target folder"));
        }
        config.setTargetFolder(resolvePath(targetFolder));
        if (config.isClean()) {
            clean(config);
        }

        FileProcessorUtils.mkdirs(Paths.get(targetFolder));

        if (config.getResourcePaths() != null) {
            for (String path : new LinkedHashSet<>(config.getResourcePaths())) {
                getProcessorManager().processResourceTree(Paths.get(path), targetFolder, config.getPathFilter());
            }
        }
        runJavadoc(config);
        for (String path : paths) {
            getProcessorManager().processSourceTree(Paths.get(path), targetFolder, config.getPathFilter());
        }
    }

    private void runJavadoc(NDocProjectConfig config) {
        if (config.getJavaSourcePaths().isEmpty()) {
            return;
        }
        String javadocTarget = config.getJavadocTarget();
        if (javadocTarget == null) {
            javadocTarget = ".";
        }
        new MdDoclet().start(new MdDocletConfig()
                .addSources(config.getJavaSourcePaths())
                .addPackages(config.getJavaPackages())
                .setTarget(javadocTarget)
                .setBackend(config.getJavadocBackend())
        );
    }

    private void clean(NDocProjectConfig config) {
        boolean clean = true;
        String targetFolder = config.getTargetFolder();
        NPath p2 = NPath.of(resolvePath(targetFolder)).toAbsolute();
        for (String path : config.getSourcePaths()) {
            NPath p1 = NPath.of(resolvePath(path)).toAbsolute();
            if (p1.isEqOrDeepChildOf(p2)) {
                clean = false;
            }
            if (p2.isEqOrDeepChildOf(p1)) {
                clean = false;
            }
        }
        for (String path : config.getResourcePaths()) {
            NPath p1 = NPath.of(resolvePath(path)).toAbsolute();
            if (p1.isEqOrDeepChildOf(p2)) {
                clean = false;
            }
            if (p2.isEqOrDeepChildOf(p1)) {
                clean = false;
            }
        }
        if (clean && p2.exists()) {
            for (NPath nPath : p2.list()) {
                nPath.deleteTree();
            }
        }
    }


    private String resolvePath(String path) {
        try {
            return Paths.get(path).toRealPath().toString();
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    public void processSourceTree(Path path, Predicate<Path> filter) {
        getProcessorManager().processSourceTree(path,filter);
    }
}
