package net.thevpc.nuts.runtime.standalone.app;

import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.app.*;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.cmdline.*;

import net.thevpc.nuts.command.NExecutionException;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NStoreKey;
import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.runtime.standalone.app.cmdline.NCmdLineUtils;
import net.thevpc.nuts.runtime.standalone.session.DefaultNSession;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.reflect.NTypeLoader;
import net.thevpc.nuts.runtime.standalone.util.NTypeLoaderImpl;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaClassUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NWorkspaceModel;
import net.thevpc.nuts.spi.NAppResolver;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextTransformConfig;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.time.NClock;
import net.thevpc.nuts.util.*;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NComponentScope(NScopeType.SHARED_SESSION)
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class NAppImpl implements NApp, Cloneable, NCopiable {
    private Class sourceType;
    private NApplication application;
    private Object source;
    private final NPath[] folders = new NPath[NStoreType.values().length];
    private final NPath[] sharedFolders = new NPath[NStoreType.values().length];
    /**
     * auto complete info for "auto-complete" mode
     */
    private NCmdLineAutoComplete autoComplete;
    private NId id;
    private String bundleName;
    private NClock startTime;
    private List<String> args;
    private NApplicationMode mode = NApplicationMode.RUN;
    private NAppStoreLocationResolver storeLocationResolver;
    private boolean prepared;
    private static NTypeLoader springBootType = new NTypeLoaderImpl("org.springframework.boot.web.servlet.support.SpringBootServletInitializer");
    private static NTypeLoader quarkusAppType = new NTypeLoaderImpl("io.quarkus.runtime.QuarkusApplication");
    private static NTypeLoader micronautAppType = new NTypeLoaderImpl("io.micronaut.runtime.Micronaut");
    private static NTypeLoader jServletType = new NTypeLoaderImpl("jakarta.servlet.http.HttpServlet");
    private static NTypeLoader xServletType = new NTypeLoaderImpl("javax.servlet.http.HttpServlet");
    private static NTypeLoader osgiType = new NTypeLoaderImpl("org.osgi.framework.BundleActivator");
    /**
     * previous parse for "update" mode
     */
    private NVersion previousVersion;
    private List<String> modeArgs = new ArrayList<>();

    public NAppImpl() {
    }

    public void setBundleName(String bundleName) {
        this.bundleName = bundleName;
    }

    @Override
    public NApp copy() {
        NAppImpl cloned = null;
        try {
            cloned = (NAppImpl) this.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        cloned.sourceType = this.getSourceType();
        cloned.application = this.getApplication();
        cloned.source = this.getSource();
        NStoreType[] values = NStoreType.values();
        for (int i = 0; i < values.length; i++) {
            NStoreType value = values[i];
            cloned.folders[i] = this.getFolder(value);
        }
        for (int i = 0; i < values.length; i++) {
            NStoreType value = values[i];
            cloned.sharedFolders[i] = this.getSharedFolder(value);
        }
        cloned.autoComplete = this.getAutoComplete();
        cloned.startTime = this.getStartTime();
        cloned.args = this.getArguments() == null ? null : new ArrayList<>(this.getArguments());
        cloned.mode = this.getMode();
        cloned.storeLocationResolver = this.getStoreLocationResolver();
        cloned.previousVersion = this.previousVersion;
        cloned.modeArgs = this.getModeArguments() == null ? null : new ArrayList<>(this.getModeArguments());
        return cloned;
    }

    @Override
    public NApp copyFrom(NApp other) {
        //boolean withDefaults = false;
        this.id = other.getId().orNull();
        this.sourceType = other.getSourceType();
        this.application = other.getApplication();
        this.source = other.getSource();
        NStoreType[] values = NStoreType.values();
        for (int i = 0; i < values.length; i++) {
            NStoreType value = values[i];
            this.folders[i] = other.getFolder(value);
        }
        for (int i = 0; i < values.length; i++) {
            NStoreType value = values[i];
            this.sharedFolders[i] = other.getSharedFolder(value);
        }
        this.autoComplete = other.getAutoComplete();
        this.startTime = other.getStartTime();
        this.args = other.getArguments() == null ? null : new ArrayList<>(other.getArguments());
        this.mode = other.getMode();
        this.storeLocationResolver = other.getStoreLocationResolver();
        this.previousVersion = other.getPreviousVersion().orNull();
        this.modeArgs = other.getModeArguments() == null ? null : new ArrayList<>(other.getModeArguments());
        return this;
    }

    @Override
    public NOptional<NId> getId() {
        return NOptional.ofNamed(this.id, "app-id");
    }

    public NApplication getApplication() {
        return application;
    }

    public void prepare(NAppInitInfo appInitInfo) {
        if (prepared) {
            throw new NIllegalStateException(NMsg.ofC("application already prepared"));
        }
        prepared = true;
        String[] args0 = appInitInfo.getArgs();
        Class<?> appClass = appInitInfo.getSourceType();
        Object source = appInitInfo.getSource();
        NApplication application = appInitInfo.getApplication();
        if (appClass == null && source == null) {
            if (application != null) {
                source = application;
                appClass = application.getClass();
            } else {
                application=resolveApplicationCustomResolver();
                if(application!=null) {
                    appClass=NApplications.unproxyType(application.getClass());
                    source=application;
                }else{
                    appClass=resolveApplicationFromStackTrace();
                    if (appClass == null) {
                        throw new NIllegalArgumentException(NMsg.ofC("unable to resolve application class from the current stacktrace"));
                    }
                    NAssert.requireNamedNonNull(appClass, "applicationType");
                    source = createInstance(appClass);
                    application = NApplications.createApplicationInstanceFromAnnotatedInstance(source);
                }
            }
        } else {
            if (appClass != null) {
                if (source == null) {
                    source = createInstance(appClass);
                } else {
                    if (!appClass.isInstance(source)) {
                        throw new NIllegalArgumentException(NMsg.ofC("invalid application instance (%s). Expected %s", source.getClass(), appClass));
                    }
                }
            }
            if (source != null) {
                if (appClass == null) {
                    appClass = NApplications.unproxyType(source.getClass());
                } else {
                    if (!appClass.isInstance(source)) {
                        throw new NIllegalArgumentException(NMsg.ofC("invalid application instance (%s). Expected %s", source.getClass(), appClass));
                    }
                }
            }
            if (application == null) {
                application = NApplications.createApplicationInstanceFromAnnotatedInstance(source);
            }
        }
//        Class appClass =
//                (applicationInstance instanceof NApplications.AnnotationClassNApplication) ?
//                        ((NApplications.AnnotationClassNApplication) applicationInstance).getAppInstance().getClass()
//                        : applicationInstance.getClass();


        NClock startTime = appInitInfo.getStartTime();
        this.storeLocationResolver = appInitInfo.getStoreLocationSupplier();
        List<String> args = new ArrayList<>();
        if (args0 != null) {
            for (String s : args0) {
                if (s == null) {
                    s = "";
                }
                args.add(s);
            }
        }
        this.startTime = startTime == null ? NClock.now() : startTime;
        int wordIndex = -1;
        if (args.size() > 0 && args.get(0).startsWith("--nuts-exec-mode=")) {
            NCmdLine execModeCommand = NCmdLine.parseDefault(
                    args.get(0).substring(args.get(0).indexOf('=') + 1)).get();
            if (execModeCommand.hasNext()) {
                NArg a = execModeCommand.next().get();
                switch (a.key()) {
                    case "auto-complete": {
                        this.mode = NApplicationMode.AUTO_COMPLETE;
                        if (execModeCommand.hasNext()) {
                            wordIndex = execModeCommand.next().get().intValue();
                        }
                        this.modeArgs = execModeCommand.toStringList();
                        execModeCommand.skipAll();
                        break;
                    }
                    case "install": {
                        this.mode = NApplicationMode.INSTALL;
                        this.modeArgs = execModeCommand.toStringList();
                        execModeCommand.skipAll();
                        break;
                    }
                    case "uninstall": {
                        this.mode = NApplicationMode.UNINSTALL;
                        this.modeArgs = execModeCommand.toStringList();
                        execModeCommand.skipAll();
                        break;
                    }
                    case "update": {
                        this.mode = NApplicationMode.UPDATE;
                        if (execModeCommand.hasNext()) {
                            this.previousVersion = NVersion.get(execModeCommand.next().get().image()).get();
                        }
                        this.modeArgs = execModeCommand.toStringList();
                        execModeCommand.skipAll();
                        break;
                    }
                    default: {
                        throw new NExecutionException(NMsg.ofC("Unsupported nuts-exec-mode : %s", args.get(0)), NExecutionException.ERROR_255);
                    }
                }
            }
            args = args.subList(1, args.size());
        }
        NId _appId = this.id; // if already set!
        if (NBlankable.isBlank(this.id)) {
            //("=== Inherited "+_appId);
            _appId = NId.getForClass(appClass).orNull();
            if (NBlankable.isBlank(_appId)) {
                throw new NExecutionException(NMsg.ofC("invalid Nuts Application (%s). Id cannot be resolved", appClass.getName()), NExecutionException.ERROR_255);
            }
            this.id = _appId;
        }
        this.args = new ArrayList<>(args);
        this.sourceType = appClass == null ? null : NApplications.unproxyType(appClass);
        this.application = application;
        this.source = source;
        for (NStoreType folder : NStoreType.values()) {
            this.setFolder(folder, NPath.of(NStoreKey.of(this.id).type(folder)));
            this.setSharedFolder(folder, NPath.of(NStoreKey.ofShared(this.id).type(folder)));
        }
        if (this.mode == NApplicationMode.AUTO_COMPLETE) {
            //TODO fix me
//            this.workspace.term().setSession(session).getSystemTerminal()
//                    .setMode(NutsTerminalMode.FILTERED);
            if (wordIndex < 0) {
                wordIndex = args.size();
            }
            this.autoComplete = new AppCmdLineAutoComplete(args, wordIndex);
        } else {
            this.autoComplete = null;
        }
        if (bundleName == null) {
            bundleName = resolveAppNameFromClass(this.sourceType, _appId.getArtifactId());
        }
    }

    private NApplication resolveApplicationCustomResolver() {
        ServiceLoader<NAppResolver> nAppResolverClassLoader=ServiceLoader.load(NAppResolver.class);
        for (NAppResolver r : nAppResolverClassLoader) {
            Object o = r.resolveCurrentApplication();
            if(o!=null) {
                return NApplications.createApplicationInstanceFromAnnotatedInstance(o);
            }
        }
        return null;
    }

    private Class<?> resolveApplicationFromStackTrace() {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
//        NLog nLog = NLog.of(NAppImpl.class);
//        nLog.log(NMsg.ofC("looking for application in stacktrace"));
//        for (int i = 0; i < stackTrace.length; i++) {
//            nLog.log(NMsg.ofC("\t%s", stackTrace[i]));
//        }
        for (int i = 0; i < stackTrace.length; i++) {
            Class c = resolveClassWithMain(stackTrace[i]);
            if (c != null) {
                source = createInstance(c);
                return c;
            }
        }
        return null;
    }

    private Class<?> resolveClassWithMain(StackTraceElement stackTraceElement) {
        String m = stackTraceElement.getMethodName();
        if (m != null && stackTraceElement.getClassName() != null && !stackTraceElement.getClassName().isEmpty()) {
            NTypeLoader type = NTypeLoader.of(stackTraceElement.getClassName());
            Class<?> c = type.getType().orNull();
            if (c != null) {
                if (Modifier.isAbstract(c.getModifiers())) {
                    return null;
                }
                if ("main".equals(m)) {
                    return type.getDeclaredMethod("main", String[].class).filter(
                            main->Modifier.isStatic(main.getModifiers()) && Modifier.isPublic(main.getModifiers()))
                            .isPresent() ?c:null;
                } else {
                    if (isAssignableFromAny(c, springBootType, quarkusAppType, micronautAppType, jServletType, xServletType,osgiType)) {
                        return c;
                    }
                }
            }
        }
        return null;
    }

    private boolean isAssignableFromAny(Class<?> c, NTypeLoader... loaders) {
        for (NTypeLoader loader : loaders) {
            if (loader.getType().filter(x -> x.isAssignableFrom(c)).isPresent()) {
                return true;
            }
        }
        return false;
    }
    /**
     * Creates an application instance by calling a no-argument constructor.
     * Errors are wrapped in RuntimeExceptions for simplicity.
     */
    private Object createInstance(Class applicationType) {
        NLog nLog = NLog.of(NAppImpl.class);
        try {
            return applicationType == null ? null : applicationType.getConstructor().newInstance();
        } catch (Exception e) {
            nLog.debug(NMsg.ofC("createInstance %s failed : %s", applicationType,e));
            throw NExceptions.ofUncheckedException(e);
        }
    }

    @Override
    public Object getSource() {
        return source;
    }

    public String getBundleName() {
        return bundleName;
    }

    private static String resolveAppNameFromClass(Class clazz, String defaultName) {
        String n = null;
        String baseFilePath = NOptional.of(clazz.getProtectionDomain()).then(x -> x.getCodeSource()).then(x -> x.getLocation()).then(x -> x.getPath()).orNull();
        NLog nLog = NLog.of(NAppImpl.class);
        nLog.debug(NMsg.ofC("resolveAppNameFromClass %s (%s) , baseFilePath=%s", clazz, defaultName, baseFilePath));
        if (baseFilePath != null) {
            try {
                String jar = extractVar(baseFilePath, "x", "(?<x>.*)[.]jar[!]/BOOT-INF/classes[!]/?");
                if (jar != null) {
                    n = extractVar(new File(jar).getName(), "n", "(?<n>-[^.]+)[.]jar");
                } else {
                    File file = new File(baseFilePath);
                    File parentFile = file.getParentFile();
                    File parentFile2 = parentFile == null ? null : parentFile.getParentFile();
                    File parentFile3 = parentFile2 == null ? null : parentFile2.getParentFile();
                    if (
                            file.getName().toLowerCase().endsWith(".jar")
                                    && parentFile3 != null
                                    && parentFile.getName().equals("lib")
                                    && parentFile2.getName().equals("WEB-INF")
                    ) {
                        n = parentFile3.getName();
                        // /WEB-INF/lib/library.jar
                        nLog.debug(NMsg.ofC("resolveAppNameFromClass [PARTIAL-/WEB-INF/lib/lib.jar] " + clazz + " (" + defaultName + ") " + baseFilePath + " ==> RESULT = " + n));
                    } else if (
                            file.getName().equals("classes")
                                    && parentFile2 != null
                                    && parentFile.getName().equals("WEB-INF")
                    ) {
                        n = parentFile2.getName();
                        // /WEB-INF/classes/
                        nLog.debug(NMsg.ofC("resolveAppNameFromClass [PARTIAL-/WEB-INF/classes] " + clazz + " (" + defaultName + ") " + baseFilePath + " ==> RESULT = " + n));
                    } else if (parentFile2 != null) {
                        n = parentFile2.getName();
                        nLog.debug(NMsg.ofC("resolveAppNameFromClass [PARTIAL-OTHER] " + clazz + " (" + defaultName + ") " + baseFilePath + " ==> RESULT = " + n));
                    }
                }
            } catch (Exception ex) {
                //
            }
        }
        if (n != null) {
            n = n.trim();
            if (!n.isEmpty()) {
                try {
                    n = URLDecoder.decode(n, StandardCharsets.UTF_8.name());
                } catch (UnsupportedEncodingException e) {
                }
                if (n.contains("##")) {
                    n = n.split("##")[0];
                }
                nLog.debug(NMsg.ofC("resolveAppNameFromClass " + clazz + " (" + defaultName + ") " + baseFilePath + " ==> RESULT = " + n));
                return n;
            }
        }
        nLog.debug(NMsg.ofC("resolveAppNameFromClass " + clazz + " (" + defaultName + ") " + baseFilePath + " ==> RESULT = " + n));
        return defaultName;
    }

    private static String extractVar(String str, String varName, String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(str);
        if (m.matches()) {
            return m.group(varName);
        }
        return null;
    }

    @Override
    public NApplicationMode getMode() {
        return this.mode;
    }

    @Override
    public List<String> getModeArguments() {
        return this.modeArgs;
    }

    @Override
    public NCmdLineAutoComplete getAutoComplete() {
        return this.autoComplete;
    }

    @Override
    public NOptional<NText> getHelpText() {
        NText h = null;
        try {
            h = NWorkspaceExt.of().resolveDefaultHelp(getSourceType());
        } catch (Exception ex) {
            //
        }
        if (h != null) {
            try {
                h = NTexts.of().transform(h, new NTextTransformConfig()
                        .setProcessTitleNumbers(true)
                        .setNormalize(true)
                        .setFlatten(true)
                );
            } catch (Exception ex) {
                //
                return NOptional.ofNamedError("application help", ex);
            }
        }
        return NOptional.ofNamed(h, "application help");
    }

    @Override
    public void printHelp() {
        NText h = NWorkspaceExt.of().resolveDefaultHelp(getSourceType());
        h = NTexts.of().transform(h, new NTextTransformConfig()
                .setProcessTitleNumbers(true)
                .setNormalize(true)
                .setFlatten(true)
        );
        if (h == null) {
            NOut.println(NMsg.ofC("Help is %s.", NMsg.ofStyledError("missing")));
        } else {
            NOut.println(h);
        }
        //need flush if the help is syntactically incorrect
        NOut.flush();
    }

    @Override
    public Class<?> getSourceType() {
        return this.sourceType;
    }

    @Override
    public NPath getBinFolder() {
        return getFolder(NStoreType.BIN);
    }

    @Override
    public NPath getConfFolder() {
        return getFolder(NStoreType.CONF);
    }

    @Override
    public NPath getLogFolder() {
        return getFolder(NStoreType.LOG);
    }

    @Override
    public NPath getTempFolder() {
        return getFolder(NStoreType.TEMP);
    }

    @Override
    public NPath getVarFolder() {
        return getFolder(NStoreType.VAR);
    }

    @Override
    public NPath getLibFolder() {
        return getFolder(NStoreType.LIB);
    }

    @Override
    public NPath getRunFolder() {
        return getFolder(NStoreType.RUN);
    }

    @Override
    public NPath getCacheFolder() {
        return getFolder(NStoreType.CACHE);
    }

    @Override
    public NPath getVersionFolder(NStoreType storeType, String version) {
        if (version == null
                || version.isEmpty()
                || version.equalsIgnoreCase("current")
                || version.equals(getId().get().getVersion().getValue())) {
            return getFolder(storeType);
        }
        NId newId = getId().get().builder().setVersion(version).build();
        if (this.storeLocationResolver != null) {
            NPath r = this.storeLocationResolver.getStoreLocation(newId, storeType);
            if (r != null) {
                return r;
            }
        }
        return NPath.of(NStoreKey.of(newId).type(storeType));
    }

    @Override
    public NPath getSharedAppsFolder() {
        return getSharedFolder(NStoreType.BIN);
    }

    @Override
    public NPath getSharedConfFolder() {
        return getSharedFolder(NStoreType.CONF);
    }

    @Override
    public NPath getSharedLogFolder() {
        return getSharedFolder(NStoreType.LOG);
    }

    @Override
    public NPath getSharedTempFolder() {
        return getSharedFolder(NStoreType.TEMP);
    }

    @Override
    public NPath getSharedVarFolder() {
        return getSharedFolder(NStoreType.VAR);
    }

    @Override
    public NPath getSharedLibFolder() {
        return getSharedFolder(NStoreType.LIB);
    }

    @Override
    public NPath getSharedRunFolder() {
        return getSharedFolder(NStoreType.RUN);
    }

    @Override
    public NPath getSharedFolder(NStoreType location) {
        return this.sharedFolders[location.ordinal()];
    }

    @Override
    public NOptional<NVersion> getVersion() {
        return this.getId().map(NId::getVersion);
    }

    @Override
    public List<String> getArguments() {
        return this.args;
    }

    @Override
    public NClock getStartTime() {
        return this.startTime;
    }

    @Override
    public NOptional<NVersion> getPreviousVersion() {
        return NOptional.ofNamed(previousVersion, "previousVersion");
    }

    @Override
    public NCmdLine getCmdLine() {
        NId appId = getId().orNull();
        if (appId == null) {
            return null;
        }
        List<String> appArguments = getArguments();
        if (appArguments == null) {
            return null;
        }
        return NCmdLine.of(appArguments)
                .setCommandName(appId.getArtifactId())
                .setAutoComplete(getAutoComplete())
                ;
    }

    @Override
    public void runCmdLine(NCmdLineRunner commandLineRunner) {
        getCmdLine()
                .setSource(this)
                .run(commandLineRunner);
    }

    @Override
    public NPath getFolder(NStoreType location) {
        return this.folders[location.ordinal()];
    }

    @Override
    public boolean isExecMode() {
        return getAutoComplete() == null;
    }

    @Override
    public NAppStoreLocationResolver getStoreLocationResolver() {
        return this.storeLocationResolver;
    }

    public NApp setVersionStoreLocationSupplier(NAppStoreLocationResolver appVersionStoreLocationSupplier) {
        this.storeLocationResolver = appVersionStoreLocationSupplier;
        return this;
    }

    public NApp setMode(NApplicationMode mode) {
        this.mode = mode;
        return this;
    }

    public NApp setModeArgs(List<String> modeArgs) {
        this.modeArgs = modeArgs;
        return this;
    }

    public NApp setFolder(NStoreType location, NPath folder) {
        this.folders[location.ordinal()] = folder;
        return this;
    }

    public NApp setSharedFolder(NStoreType location, NPath folder) {
        this.sharedFolders[location.ordinal()] = folder;
        return this;
    }

    //    @Override
    public NApp setId(NId appId) {
        this.id = appId;
        return this;
    }

    //    @Override
    public NApp setArguments(List<String> args) {
        this.args = args;
        return this;
    }

    public NApp setArguments(String[] args) {
        this.args = new ArrayList<>(Arrays.asList(args));
        return this;
    }

    public NApp setStartTime(NClock startTime) {
        this.startTime = startTime;
        return this;
    }

    public NApp setPreviousVersion(NVersion previousVersion) {
        this.previousVersion = previousVersion;
        return this;
    }

    public <T> T getOrComputeProperty(String name, NScopeType scope, Supplier<T> supplier) {
        NAssert.requireNamedNonNull(supplier);
        if (scope == null) {
            scope = NScopeType.SHARED_SESSION;
        }
        switch (scope) {
            case SHARED_SESSION:
            case TRANSITIVE_SESSION:
            case SESSION: {
                return ((DefaultNSession) NSession.of()).getPropertiesHolder().getOrComputeProperty(name, supplier, scope);
            }
            case WORKSPACE: {
                return ((NWorkspaceExt.of())).getModel().properties.getOrComputeProperty(name, supplier, NScopeType.WORKSPACE);
            }
            case PROTOTYPE: {
                return supplier.get();
            }
            default: {
                throw new NUnsupportedEnumException(scope);
            }
        }
    }

    public <T> T setProperty(String name, NScopeType scope, T value) {
        if (scope == null) {
            scope = NScopeType.SHARED_SESSION;
        }
        switch (scope) {
            case SESSION:
            case SHARED_SESSION: {
                return (T) ((DefaultNSession) NSession.of()).getPropertiesHolder().setProperty(name, value, scope);
            }
            case TRANSITIVE_SESSION: {
                return (T) ((DefaultNSession) NSession.of()).getPropertiesHolder().setProperty(name, CoreNUtils.checkCopiableValue(value), scope);
            }
            case WORKSPACE: {
                NWorkspaceModel m = ((NWorkspaceExt.of())).getModel();
                return (T) m.properties.setProperty(name, value, NScopeType.WORKSPACE);
            }
            case PROTOTYPE:
            default: {
                throw new NUnsupportedEnumException(scope);
            }
        }
    }

    private static class AppCmdLineAutoComplete extends NCmdLineAutoCompleteBase {

        private final ArrayList<String> words;
        private final int wordIndex;

        public AppCmdLineAutoComplete(List<String> args, int wordIndex) {
            words = new ArrayList<>(args);
            this.wordIndex = wordIndex;
        }

        @Override
        public String getLine() {
            return NCmdLine.of(getWords()).toString();
        }

        @Override
        public List<String> getWords() {
            return words;
        }

        @Override
        public int getCurrentWordIndex() {
            return wordIndex;
        }

        @Override
        protected NArgCandidate addCandidatesImpl(NArgCandidate value) {
            NArgCandidate c = super.addCandidatesImpl(value);
            String v = value.getValue();
            if (v == null) {
                throw new NExecutionException(NMsg.ofPlain("candidate cannot be null"), NExecutionException.ERROR_2);
            }
            String d = value.getDisplay();
            if (Objects.equals(v, d) || d == null) {
                NOut.println(NMsg.ofC("%s", NConstants.Apps.AUTO_COMPLETE_CANDIDATE_PREFIX + NCmdLineUtils.escapeArgument(v)));
            } else {
                NOut.println(NMsg.ofC("%s", NConstants.Apps.AUTO_COMPLETE_CANDIDATE_PREFIX + NCmdLineUtils.escapeArgument(v) + " " + NCmdLineUtils.escapeArgument(d)));
            }
            return c;
        }
    }
}
