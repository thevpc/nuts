/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ntemplate.filetemplate;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.eval.FtexEvaluator;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.processors.*;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.util.FileProcessorUtils;
import net.thevpc.nuts.toolbox.ntemplate.filetemplate.util.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * @author thevpc
 */
public class FileTemplater {

    public static final String ROOT_DIR = "rootDir";
    public static final String WORKING_DIR = "workingDir";
    public static final String SOURCE_PATH = "sourcePath";
    public static final String PROJECT_FILENAME = "project.ftex";
    private static final Map<String, TemplateProcessor> globalProcessorsByMimeType = new HashMap<>();
    private static final Map<String, TemplateProcessor> globalExecProcessorsByMimeType = new HashMap<>();
    private static final StreamToTemplateProcessor DEFAULT_PROCESSOR = new StreamToTemplateProcessor(new CopyStreamProcessor());
    private static final StreamToTemplateProcessor FTEX_PROCESSOR = new StreamToTemplateProcessor(new DefaultExecutor(new FtexEvaluator()));
    private static final TemplateProcessor IGNORE_PROCESSOR = new TemplateProcessor() {
        @Override
        public void processStream(InputStream source, OutputStream target, FileTemplater context) {

        }

        @Override
        public void processPath(Path source, String mimeType, FileTemplater context) {

        }
    };

    static {
        registerGlobalProcessorByMimeType(DollarVarStreamProcessor.INSTANCE, MimeTypeConstants.PLACEHOLDER_DOLLARS);
        registerGlobalProcessorByMimeType(DollarBracket2VarStreamProcessor.INSTANCE,
                MimeTypeConstants.ANY_TEXT,
                "application/x-shellscript",
                "application/json",
                "application/javascript",
                "application/java"
        );
        globalProcessorsByMimeType.put(MimeTypeConstants.ANY_TYPE, DEFAULT_PROCESSOR);
        globalExecProcessorsByMimeType.put(MimeTypeConstants.FTEX, FTEX_PROCESSOR);
        globalExecProcessorsByMimeType.put(MimeTypeConstants.IGNORE, IGNORE_PROCESSOR);
    }

    private final Map<String, Object> vars = new HashMap<>();
    private final Map<String, TemplateProcessor> processorsByMimeType = new HashMap<>();
    private final Map<String, TemplateProcessor> execProcessorsByMimeType = new HashMap<>();
    private FileTemplater parent;
    private Function<String, Object> customVarEvaluator;
    private TemplateProcessorFactory processorFactory;
    private TemplateProcessorFactory executorFactory;
    private MimeTypeResolver mimeTypeResolver;
    private String sourcePath;
    private String rootDir;
    private String workingDir;
    private boolean userParentProperties;
    private PathTranslator pathTranslator;
    private String projectFileName = PROJECT_FILENAME;
    private NutsSession session;

    public FileTemplater(NutsSession session) {
        this.session = session;
        this.setLog(new TemplateLog() {
            NutsLoggerOp logOp;

            @Override
            public void info(String title, String message) {
                log().verb(NutsLogVerb.INFO).level(Level.FINER)
                        .log(NutsMessage.jstyle("{0} : {1}", title, message));
            }

            @Override
            public void debug(String title, String message) {
                log().verb(NutsLogVerb.DEBUG).level(Level.FINER)
                        .log(NutsMessage.jstyle("{0} : {1}", title, message));
            }

            @Override
            public void error(String title, String message) {
                log().verb(NutsLogVerb.FAIL).level(Level.FINER)
                        .log(NutsMessage.jstyle("{0} : {1}", title, message));
            }

            private NutsLoggerOp log() {
                if (logOp == null) {
                    logOp = NutsLogger.of(FileTemplater.class, session)
                            .with()
                    ;
                }
                return logOp;
            }
        });
    }

    public FileTemplater(FileTemplater parent) {
        this(parent.session);
        this.parent = parent;
    }

    public static void registerGlobalProcessorByMimeType(TemplateProcessor p, String... mimetypes) {
        for (String mimetype : mimetypes) {
            globalProcessorsByMimeType.put(mimetype, p);
        }
    }

    public static void registerGlobalProcessorByMimeType(StreamProcessor p, String... mimetypes) {
        for (String mimetype : mimetypes) {
            globalProcessorsByMimeType.put(mimetype, new StreamToTemplateProcessor(p));
        }
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

    public NutsSession getSession() {
        return session;
    }

    public boolean isUserParentProperties() {
        return userParentProperties;
    }

    public FileTemplater setUserParentProperties(boolean userParentProperties) {
        this.userParentProperties = userParentProperties;
        return this;
    }

    public String getProjectFileName() {
        return projectFileName;
    }

    public FileTemplater setProjectFileName(String projectFileName) {
        if (projectFileName == null || projectFileName.isEmpty()) {
            projectFileName = PROJECT_FILENAME;
        }
        this.projectFileName = projectFileName;
        return this;
    }

    public FileTemplater newChild() {
        return new FileTemplater(this);
    }

    public TemplateProcessor getProcessor(String mimetype) {
        if (mimetype == null || mimetype.isEmpty() || mimetype.equals("*")) {
            mimetype = MimeTypeConstants.ANY_TYPE;
        }
        for (String mt : mimetype.split(";")) {
            mt = mt.trim();
            if (mt.length() > 0) {
                TemplateProcessor m = getProcessorExact(mt);
                if (m != null) {
                    return m;
                }
            }
        }
        for (String mt : mimetype.split(";")) {
            mt = mt.trim();
            if (mt.length() > 0) {
                int slash = mimetype.indexOf('/');
                if (slash > 0) {
                    String a = mimetype.substring(0, slash);
                    String b = mimetype.substring(slash + 1);
                    if (!b.equals("*")) {
                        TemplateProcessor m = getProcessorExact(a + "/*");
                        if (m != null) {
                            return m;
                        }
                    }
                }
            }
        }
        return getProcessorExact(MimeTypeConstants.ANY_TYPE);
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

    public FileTemplater setCustomVarEvaluator(Function<String, Object> customVarEvaluator) {
        this.customVarEvaluator = customVarEvaluator;
        return this;
    }

    public TemplateProcessor getDefaultProcessor(String mimetype) {
        TemplateProcessor processor = processorsByMimeType.get(mimetype);
        if (processor != null) {
            return processor;
        }
        if (parent != null) {
            return parent.getDefaultProcessor(mimetype);
        }
        return null;
    }

    public FileTemplater setDefaultProcessor(String mimetype, ExprEvaluator processor) {
        return setDefaultProcessor(mimetype, processor == null ? null : new StreamToTemplateProcessor(new DefaultExecutor(processor)));
    }

    public FileTemplater setDefaultProcessor(String mimetype, TemplateProcessor processor) {
        if (processor == null) {
            processorsByMimeType.remove(mimetype);
        } else {
            processorsByMimeType.put(mimetype, processor);
        }
        return this;
    }

    public TemplateProcessor getExecutor(String mimetype) {
        if (mimetype == null || mimetype.isEmpty() || mimetype.equals("*")) {
            mimetype = MimeTypeConstants.ANY_TYPE;
        }
        String[] mts = Stream.of(mimetype.split(";")).map(String::trim).filter(x -> x.length() > 0).toArray(String[]::new);
        for (String mt : mts) {
            TemplateProcessor m = getExecutorExact(mt);
            if (m != null) {
                return m;
            }
        }
        for (String mt : mts) {
            int slash = mt.indexOf('/');
            if (slash > 0) {
                String a = mt.substring(0, slash);
                String b = mt.substring(slash + 1);
                if (!b.equals("*")) {
                    TemplateProcessor m = getExecutorExact(a + "/*");
                    if (m != null) {
                        return m;
                    }
                }
            }
        }
        TemplateProcessor z = getExecutorExact(MimeTypeConstants.ANY_TYPE);
        if (z != null) {
            return z;
        }
        return DEFAULT_PROCESSOR;
    }

    public TemplateProcessor getProcessorExact(String mimetype) {
        if (processorFactory != null) {
            TemplateProcessor p = processorFactory.getProcessor(mimetype);
            if (p != null) {
                return p;
            }
        }
        TemplateProcessor p = processorsByMimeType.get(mimetype);
        if (p != null) {
            return p;
        }
        if (parent != null) {
            return parent.getProcessorExact(mimetype);
        }
        p = globalProcessorsByMimeType.get(mimetype);
        if (p != null) {
            return p;
        }
        //never return null!
        return null;//DEFAULT_PROCESSOR;
    }

    public TemplateProcessor getExecutorExact(String mimetype) {
        if (executorFactory != null) {
            TemplateProcessor p = executorFactory.getProcessor(mimetype);
            if (p != null) {
                return p;
            }
        }
        TemplateProcessor p = execProcessorsByMimeType.get(mimetype);
        if (p != null) {
            return p;
        }
        if (parent != null) {
            return parent.getExecutorExact(mimetype);
        }
        p = globalExecProcessorsByMimeType.get(mimetype);
        if (p != null) {
            return p;
        }
        return null;
    }

    public TemplateProcessor getDefaultExecutor(String mimetype) {
        TemplateProcessor processor = execProcessorsByMimeType.get(mimetype);
        if (processor != null) {
            return processor;
        }
        if (parent != null) {
            return parent.getDefaultExecutor(mimetype);
        }
        return null;
    }

    public FileTemplater setDefaultExecutor(String mimetype, ExprEvaluator executor) {
        return setDefaultExecutor(mimetype, executor == null ? null : new StreamToTemplateProcessor(new DefaultExecutor(executor)));
    }

    public FileTemplater setDefaultExecutor(String mimetype, TemplateProcessor executor) {
        if (executor == null) {
            execProcessorsByMimeType.remove(mimetype);
        } else {
            execProcessorsByMimeType.put(mimetype, executor);
        }
        return this;
    }

    public TemplateProcessorFactory getExecutorFactory() {
        if (executorFactory != null) {
            return executorFactory;
        }
        if (parent != null) {
            return parent.getExecutorFactory();
        }
        return null;
    }

    public FileTemplater setExecutorFactory(TemplateProcessorFactory processorFactory) {
        this.executorFactory = processorFactory;
        return this;
    }

    public TemplateProcessorFactory getProcessorFactory() {
        if (processorFactory != null) {
            return processorFactory;
        }
        if (parent != null) {
            return parent.getProcessorFactory();
        }
        return null;
    }

    public FileTemplater setProcessorFactory(TemplateProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
        return this;
    }

    public TemplateLog getLog() {
        TemplateLog ll = (TemplateLog) getVar("log", null);
        if (ll == null) {
            return DefaultTemplateLog.INSTANCE;
        }
        return ll;
    }

    public FileTemplater setLog(TemplateLog log) {
        return setVar("log", log);
    }

    public FileTemplater getParent() {
        return parent;
    }

    public FileTemplater setParent(FileTemplater parent) {
        this.parent = parent;
        return this;
    }

    public String getWorkingDirRequired() {
        return (String) getVar(WORKING_DIR).get();
    }

    public String getRootDirRequired() {
        return (String) getVar(ROOT_DIR).get();
    }

    public NutsOptional<String> getWorkingDir() {
        return (NutsOptional) getVar(WORKING_DIR);
    }

    public FileTemplater setWorkingDir(String workingDir) {
        return setVar(WORKING_DIR, workingDir);
    }

    public NutsOptional<String> getRootDir() {
        return (NutsOptional) getVar(ROOT_DIR);
    }

    public FileTemplater setRootDir(String rootDir) {
        return setVar(ROOT_DIR, rootDir);
    }

    public FileTemplater removeWorkingDir() {
        return remove(WORKING_DIR);
    }

    public FileTemplater removeRootDir() {
        return remove(ROOT_DIR);
    }

    public String getSourcePathRequired() {
        return (String) getVar(SOURCE_PATH).get();
    }

    public NutsOptional<String> getSourcePath() {
        return getVar(SOURCE_PATH);
    }

    public FileTemplater setSourcePath(String workingDir) {
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

    public FileTemplater setVar(String name, Object value) {
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

    public FileTemplater remove(String name) {
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

    public <T> NutsOptional<T> getVar(String name) {
        switch (name) {
            case SOURCE_PATH: {
                if (this.sourcePath != null) {
                    return (NutsOptional<T>) NutsOptional.of(getSession(), this.sourcePath);
                }
                break;
            }
            case WORKING_DIR: {
                if (this.workingDir != null) {
                    return (NutsOptional<T>) NutsOptional.of(getSession(), this.workingDir);
                }
                break;
            }
            case ROOT_DIR: {
                if (this.rootDir != null) {
                    return (NutsOptional<T>) NutsOptional.of(getSession(), this.rootDir);
                }
                break;
            }
        }
        T r = (T) vars.get(name);
        if (r != null) {
            return NutsOptional.of(getSession(), r);
        }
        if (vars.containsKey(name)) {
            return NutsOptional.ofNull(getSession());
        }
        if (customVarEvaluator != null) {
            r = (T) customVarEvaluator.apply(name);
            if (r != null) {
                return NutsOptional.of(getSession(), r);
            }
        }
        if (parent != null) {
            return parent.getVar(name);
        }
        return NutsOptional.ofEmpty(getSession(), session1 -> {
            String source = getSourcePath().orElse(null);
            if (source == null) {
                return NutsMessage.cstyle("not found : %s", StringUtils.escapeString(name));
            } else {
                return NutsMessage.cstyle("not found : %s in %s", StringUtils.escapeString(name), source);
            }
        });
    }

    public void processTree(Path path, Predicate<Path> filter) {
        if (!getRootDir().isPresent()) {
            setRootDir(path.toString());
        }
        Path path0 = toAbsolutePath(path);
        if (Files.isRegularFile(path0)) {
            if (filter == null || filter.test(path0)) {
                processRegularFile(path, null);
            }
        } else if (Files.isDirectory(path)) {
            try (Stream<Path> stream = Files.walk(path0).filter(x -> Files.isRegularFile(x))) {

                stream.forEach(x -> {
                    if (filter == null || filter.test(x)) {
                        try {
                            processRegularFile(x, null);
                        } catch (Exception ex) {
                            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("error processing %s : %s", x, ex), ex);
                        }
                    }
                });
            } catch (IOException e) {
                throw new NutsIOException(getSession(), e);
            }
        } else {
            throw new NutsIOException(getSession(), NutsMessage.cstyle("unsupported path %s", path));
        }
    }

    public void processFiles(Path path, Predicate<Path> filter) {
        Path path0 = toAbsolutePath(path);
        if (Files.isRegularFile(path0)) {
            if (filter == null || filter.test(path0)) {
                processRegularFile(path0, null);
            }
        } else if (Files.isDirectory(path0)) {
            try (Stream<Path> stream = Files.list(path0)) {
                stream.filter(filter)
                        .forEach(x -> {
                            if (Files.isRegularFile(x)) {
                                if (filter == null || filter.test(x)) {
                                    processRegularFile(path0, null);
                                }
                            }
                        });
            } catch (IOException e) {
                throw new NutsIOException(getSession(), e);
            }
        } else {
            throw new NutsIOException(getSession(), NutsMessage.cstyle("unsupported path %s", path));
        }
    }

    public void executeProjectFile(Path path, String mimeTypesString) {
        executeRegularFile(path, mimeTypesString);
    }

    public String executeRegularFile(Path path, String mimeTypesString) {
        Path absolutePath = toAbsolutePath(path);
        Path parentPath = absolutePath.getParent();
        if (!Files.isRegularFile(absolutePath)) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("no a file : %s", path));
        }
        String[] mimeTypesArray = mimeTypesString == null ? FileProcessorUtils.splitMimeTypes(getMimeTypeResolver().resolveMimetype(path.toString()))
                : FileProcessorUtils.splitMimeTypes(mimeTypesString);
        for (String mimeType : mimeTypesArray) {
            TemplateProcessor proc = null;
            try {
                proc = getExecutor(mimeType);
            } catch (Exception ex) {
                getLog().error("file", "error resolving executor for mimetype " + mimeType + " and file : " + path.toString() + ". " + ex.toString());
            }
            if (proc != null) {
                try {
                    String s1 = path.toString();
                    String s2 = absolutePath.toString();
                    if (s1.equals(s2)) {
                        getLog().debug("file", "[" + proc + "] [" + mimeType + "] execute path : " + s1);
                    } else {
                        getLog().debug("file", "[" + proc + "] [" + mimeType + "] execute path : " + s1 + " = " + s2);
                    }
                    try (InputStream in = Files.newInputStream(absolutePath)) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        proc.processStream(in, out,
                                newChild()
                                        .setUserParentProperties(true)
                                        .setWorkingDir(parentPath.toString())
                                        .setSourcePath(absolutePath.toString())
                        );
                        return out.toString();
                    }
                } catch (IOException ex) {
                    throw new NutsIOException(getSession(), NutsMessage.cstyle("error executing file : %s", path), ex);
                }
            }
        }
        throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("processor not found for %s", mimeTypesString));
    }

    public void processRegularFile(Path path, String mimeType) {
        Path absolutePath = toAbsolutePath(path);
        Path parentPath = absolutePath.getParent();
        if (!Files.isRegularFile(absolutePath)) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("unsupported file : %s", path.toString()));
        }
        String[] mimeTypes = mimeType == null ? FileProcessorUtils.splitMimeTypes(getMimeTypeResolver().resolveMimetype(path.toString()))
                : FileProcessorUtils.splitMimeTypes(mimeType);
        for (String mimeType0 : mimeTypes) {
            TemplateProcessor proc = null;
            try {
                proc = getProcessor(mimeType0);
            } catch (Exception ex) {
                getLog().error("file", "error resolving processor for mimetype " + mimeType0 + " and file : " + path.toString() + ". " + ex.toString());
            }
            if (proc != null) {
                String s1 = path.toString();
                String s2 = absolutePath.toString();
                if (s1.equals(s2)) {
                    getLog().debug("file", "[" + proc + "] [" + mimeType + "] process path : " + s1);
                } else {
                    getLog().debug("file", "[" + proc + "] [" + mimeType + "] process path : " + s1 + " = " + s2);
                }
                proc.processPath(path, mimeType0,
                        newChild()
                                .setUserParentProperties(true)
                                .setWorkingDir(parentPath.toString())
                                .setSourcePath(absolutePath.toString())
                );
            }
        }
    }

    private Path toAbsolutePath(Path path) {
        return FileProcessorUtils.toRealPath(path, Paths.get(getWorkingDirRequired()));
    }

    public String executeString(String source, String mimeType) {
        return executeStream(new ByteArrayInputStream(source.getBytes()), mimeType);
    }

    public String executeStream(InputStream source, String mimeType) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            String[] mimeTypes = FileProcessorUtils.splitMimeTypes(mimeType);
            for (String mimeType0 : mimeTypes) {
                TemplateProcessor proc = null;
                proc = getExecutor(mimeType0);
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
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("unsupported mimetype : %s", mimeType));
        } catch (IOException ex) {
            throw new NutsIOException(getSession(), ex);
        }
    }

    public String processString(String source, String mimeType) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        processStream(new ByteArrayInputStream(source.getBytes()), bos, mimeType);
        return bos.toString();
    }

    public void processStream(InputStream source, OutputStream target, String mimeType) {
        String[] mimeTypes = FileProcessorUtils.splitMimeTypes(mimeType);
        for (String mimeType0 : mimeTypes) {
            TemplateProcessor proc = null;
            try {
                proc = getProcessor(mimeType0);
            } catch (Exception ex) {
                getLog().error("file", "unsupported mimeType : " + mimeType0 + ". " + ex);
            }
            if (proc != null) {
                try {
                    proc.processStream(source, target,
                            newChild()
                                    .setUserParentProperties(true)
                    );
                } catch (Exception ex) {
                    getLog().error("file", "error processing mimeType : " + mimeType + ". " + ex);
                }
                return;
            }
        }
        throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("unsupported mimetype : %s", mimeType));
    }

    public MimeTypeResolver getMimeTypeResolver() {
        if (mimeTypeResolver != null) {
            return mimeTypeResolver;
        }
        if (parent != null) {
            return parent.getMimeTypeResolver();
        }
        return DefaultMimeTypeResolver.DEFAULT;
    }

    public FileTemplater setMimeTypeResolver(MimeTypeResolver mimeTypeResolver) {
        this.mimeTypeResolver = mimeTypeResolver;
        return this;
    }

    public PathTranslator getPathTranslator() {
        if (pathTranslator != null) {
            return pathTranslator;
        }
        if (parent != null) {
            return parent.getPathTranslator();
        }
        return null;
    }

    public FileTemplater setPathTranslator(PathTranslator pathTranslator) {
        this.pathTranslator = pathTranslator;
        return this;
    }

    public FileTemplater setPathTranslator(Path from, Path to) {
        return setPathTranslator(new DefaultPathTranslator(from, to));
    }

    public FileTemplater setTargetPath(Path to) {
        return setPathTranslator(new DefaultPathTranslator(Paths.get(getWorkingDirRequired()), to));
    }

    public void processProject(TemplateConfig config) {
        String projectPath = config.getProjectPath();
        String scriptType = config.getScriptType();
        String targetFolder = config.getTargetFolder();
        if (projectPath == null) {
            if (config.getPaths().isEmpty()) {
                throw new NutsIllegalArgumentException(getSession(), NutsMessage.plain("missing path to process"));
            }
            if (targetFolder == null) {
                throw new NutsIllegalArgumentException(getSession(), NutsMessage.plain("missing target folder"));
            }
        }
        Path oProjectDirPath = Paths.get(projectPath);
        Path oProjectFile = oProjectDirPath.resolve(getProjectFileName());
        Path oProjectSrc = oProjectDirPath.resolve("src");
        if (!Files.isDirectory(oProjectSrc)) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("invalid project, missing src/ folder : %s", oProjectDirPath));
        }
        if (!Files.isRegularFile(oProjectFile)) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("invalid project, missing project.ftex : %s", oProjectDirPath));
        }
        List<String> initScripts = new ArrayList<>(config.getInitScripts());
        initScripts.add(oProjectFile.toString());

        List<String> paths = new ArrayList<>(config.getPaths());
        paths.add(oProjectSrc.toString());

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
        if (projectPath != null) {
            //&& targetFolder==null
            String tf = (String) this.getVar("targetFolder", null);
            if (tf != null && targetFolder == null) {
                targetFolder = tf;
            }
        }
        if (targetFolder == null) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.plain("missing target folder"));
        }
        FileProcessorUtils.mkdirs(Paths.get(targetFolder));
        try {
            targetFolder = Paths.get(targetFolder).toRealPath().toString();
        } catch (IOException ex) {
            throw new NutsIOException(getSession(), ex);
        }
        for (String path : paths) {
            Path opath;
            try {
                opath = Paths.get(path).toRealPath();
            } catch (IOException ex) {
                throw new NutsIOException(getSession(), ex);
            }
            if (Files.isDirectory(opath)) {
                this.setWorkingDir(opath.toString());
                if (targetFolder != null) {
                    this.setPathTranslator(new DefaultPathTranslator(opath, Paths.get(targetFolder)));
                }
            } else {
                Path ppath = opath.getParent();
                this.setWorkingDir(ppath.toString());
                if (targetFolder != null) {
                    this.setPathTranslator(new DefaultPathTranslator(opath, ppath));
                }
            }
            this.processTree(opath, config.getPathFilter());
        }
    }

}
