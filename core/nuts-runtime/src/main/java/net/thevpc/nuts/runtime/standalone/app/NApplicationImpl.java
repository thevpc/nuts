package net.thevpc.nuts.runtime.standalone.app;

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
import net.thevpc.nuts.reflect.NReflectUtils;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.runtime.standalone.session.DefaultNSession;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.reflect.NTypeLoader;
import net.thevpc.nuts.runtime.standalone.util.NTypeLoaderImpl;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NWorkspaceModel;
import net.thevpc.nuts.spi.NAppResolverSPI;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextTransformConfig;
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
public class NApplicationImpl implements NApplication, Cloneable, NCopiable {
    private Class sourceType;
    private NApplicationHandler application;
    private Object source;
    private final NPath[] folders = new NPath[NStoreType.values().length];
    private final NPath[] sharedFolders = new NPath[NStoreType.values().length];
    /**
     * auto complete info for "auto-complete" mode
     */
    private NArgCompletePosition completePos;
    private NId id;
    private String bundleName;
    private NClock startTime;
    private List<String> args;
    private NApplicationMode mode = NApplicationMode.RUN;
    private NAppStoreLocationResolver storeLocationResolver;
    private boolean prepared;
    private static final NTypeLoader springBootType = new NTypeLoaderImpl("org.springframework.boot.web.servlet.support.SpringBootServletInitializer");
    private static final NTypeLoader quarkusAppType = new NTypeLoaderImpl("io.quarkus.runtime.QuarkusApplication");
    private static final NTypeLoader micronautAppType = new NTypeLoaderImpl("io.micronaut.runtime.Micronaut");
    private static final NTypeLoader jServletType = new NTypeLoaderImpl("jakarta.servlet.http.HttpServlet");
    private static final NTypeLoader xServletType = new NTypeLoaderImpl("javax.servlet.http.HttpServlet");
    private static final NTypeLoader osgiType = new NTypeLoaderImpl("org.osgi.framework.BundleActivator");
    /**
     * previous parse for "update" mode
     */
    private NVersion previousVersion;
    private List<String> modeArgs = new ArrayList<>();

    public NApplicationImpl() {
    }

    public void setBundleName(String bundleName) {
        this.bundleName = bundleName;
    }

    @Override
    public NApplication copy() {
        NApplicationImpl cloned = null;
        try {
            cloned = (NApplicationImpl) this.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        cloned.sourceType = this.sourceType();
        cloned.application = this.handler();
        cloned.source = this.source();
        NStoreType[] values = NStoreType.values();
        for (int i = 0; i < values.length; i++) {
            NStoreType value = values[i];
            cloned.folders[i] = this.getFolder(value);
        }
        for (int i = 0; i < values.length; i++) {
            NStoreType value = values[i];
            cloned.sharedFolders[i] = this.getSharedFolder(value);
        }
        cloned.completePos = this.completePosition();
        cloned.startTime = this.startTime();
        cloned.args = this.arguments() == null ? null : new ArrayList<>(this.arguments());
        cloned.mode = this.mode();
        cloned.storeLocationResolver = this.storeLocationResolver();
        cloned.previousVersion = this.previousVersion;
        cloned.modeArgs = this.modeArguments() == null ? null : new ArrayList<>(this.modeArguments());
        return cloned;
    }

    @Override
    public NApplication copyFrom(NApplication other) {
        //boolean withDefaults = false;
        this.id = other.id().orNull();
        this.sourceType = other.sourceType();
        this.application = other.handler();
        this.source = other.source();
        NStoreType[] values = NStoreType.values();
        for (int i = 0; i < values.length; i++) {
            NStoreType value = values[i];
            this.folders[i] = other.getFolder(value);
        }
        for (int i = 0; i < values.length; i++) {
            NStoreType value = values[i];
            this.sharedFolders[i] = other.getSharedFolder(value);
        }
        this.completePos = other.completePosition();
        this.startTime = other.startTime();
        this.args = other.arguments() == null ? null : new ArrayList<>(other.arguments());
        this.mode = other.mode();
        this.storeLocationResolver = other.storeLocationResolver();
        this.previousVersion = other.previousVersion().orNull();
        this.modeArgs = other.modeArguments() == null ? null : new ArrayList<>(other.modeArguments());
        return this;
    }

    @Override
    public NOptional<NId> id() {
        return NOptional.ofNamed(this.id, "app-id");
    }

    public NApplicationHandler handler() {
        return application;
    }

    public void prepare(NAppInitInfo appInitInfo) {
        AppClassInfo appClassInfo = resolveAppClassInfo(appInitInfo);
        NId expectedId = NId.getForClass(appClassInfo.appClass).orNull();
        if (prepared) {
            throw new NIllegalStateException(NMsg.ofC("application already prepared : was %s (%s), now %s (%s)", id, sourceType, expectedId, appClassInfo.appClass));
        }
        prepared = true;
        String[] args0 = appInitInfo.args();
        NClock startTime = appInitInfo.startTime();
        this.storeLocationResolver = appInitInfo.storeLocationSupplier();
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
        NArgCompletePosition wordIndex = null;
        if (!args.isEmpty() && args.get(0).startsWith("--nuts-exec-mode=")) {
            NCmdLine execModeCommand = NCmdLine.parseDefault(
                    args.get(0).substring(args.get(0).indexOf('=') + 1)).get();
            if (execModeCommand.hasNext()) {
                NArg a = execModeCommand.next().get();
                switch (a.key()) {
                    case "auto-complete":
                    case "complete": {
                        this.mode = NApplicationMode.COMPLETE;
                        if (execModeCommand.hasNext()) {
                            wordIndex = NArgCompletePosition.of(execModeCommand.next().get().stringValue()).orNull();
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
            _appId = NId.getForClass(appClassInfo.appClass).orNull();
            if (NBlankable.isBlank(_appId)) {
                throw new NExecutionException(NMsg.ofC("invalid Nuts Application (%s). Id cannot be resolved", appClassInfo.appClass.getName()), NExecutionException.ERROR_255);
            }
            this.id = _appId;
        }
        this.args = new ArrayList<>(args);
        this.sourceType = appClassInfo.appClass;
        this.application = appClassInfo.application;
        this.source = appClassInfo.source;
        for (NStoreType folder : NStoreType.values()) {
            this.setFolder(folder, NPath.of(NStoreKey.of(this.id).type(folder)));
            this.setSharedFolder(folder, NPath.of(NStoreKey.ofShared(this.id).type(folder)));
        }
        if (this.mode == NApplicationMode.COMPLETE) {
            this.completePos = wordIndex;
        } else {
            this.completePos = null;
        }
        if (bundleName == null) {
            bundleName = resolveAppNameFromClass(this.sourceType, _appId.artifactId());
        }
    }

    private static class AppClassInfo {
        private Class<?> appClass;
        private Object source;
        private NApplicationHandler application;

        public AppClassInfo(Class<?> appClass, Object source, NApplicationHandler application) {
            this.appClass = appClass;
            this.source = source;
            this.application = application;
        }
    }

    private AppClassInfo resolveAppClassInfo(NAppInitInfo appInitInfo) {
        Class<?> appClass = appInitInfo.sourceType();
        Object source = appInitInfo.source();
        NApplicationHandler application = appInitInfo.application();
        if (appClass == null && source == null) {
            if (application != null) {
                source = application;
                appClass = application.getClass();
            } else {
                application = resolveApplicationCustomResolver();
                if (application != null) {
                    appClass = NReflectUtils.unproxyType(application.getClass());
                    source = application;
                } else {
                    appClass = resolveApplicationFromStackTrace();
                    if (appClass == null) {
                        throw new NIllegalArgumentException(NMsg.ofC("unable to resolve application class from the current stacktrace"));
                    }
                    NAssert.requireNamedNonNull(appClass, "applicationType");
                    source = createInstance(appClass);
                    application = NApplicationHandler.createApplicationInstanceFromAnnotatedInstance(source);
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
                    appClass = NReflectUtils.unproxyType(source.getClass());
                } else {
                    if (!appClass.isInstance(source)) {
                        throw new NIllegalArgumentException(NMsg.ofC("invalid application instance (%s). Expected %s", source.getClass(), appClass));
                    }
                }
            }
            if (application == null) {
                application = NApplicationHandler.createApplicationInstanceFromAnnotatedInstance(source);
            }
        }
        return new AppClassInfo(appClass == null ? null : NReflectUtils.unproxyType(appClass), source, application);
    }

    private NApplicationHandler resolveApplicationCustomResolver() {
        ServiceLoader<NAppResolverSPI> nAppResolverClassLoader = ServiceLoader.load(NAppResolverSPI.class);
        for (NAppResolverSPI r : nAppResolverClassLoader) {
            Object o = r.resolveCurrentApplication();
            if (o != null) {
                return NApplicationHandler.createApplicationInstanceFromAnnotatedInstance(o);
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
            Class<?> c = type.type().orNull();
            if (c != null) {
                if (Modifier.isAbstract(c.getModifiers())) {
                    return null;
                }
                if ("main".equals(m)) {
                    return type.getDeclaredMethod("main", String[].class).filter(
                                    main -> Modifier.isStatic(main.getModifiers()) && Modifier.isPublic(main.getModifiers()))
                            .isPresent() ? c : null;
                } else {
                    if (isAssignableFromAny(c, springBootType, quarkusAppType, micronautAppType, jServletType, xServletType, osgiType)) {
                        return c;
                    }
                }
            }
        }
        return null;
    }

    private boolean isAssignableFromAny(Class<?> c, NTypeLoader... loaders) {
        for (NTypeLoader loader : loaders) {
            if (loader.type().filter(x -> x.isAssignableFrom(c)).isPresent()) {
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
        NLog nLog = NLog.of(NApplicationImpl.class);
        try {
            return applicationType == null ? null : applicationType.getConstructor().newInstance();
        } catch (Exception e) {
            nLog.debug(NMsg.ofC("createInstance %s failed : %s", applicationType, e));
            throw NException.ofUncheckedException(e);
        }
    }

    @Override
    public Object source() {
        return source;
    }

    public String bundleName() {
        return bundleName;
    }

    private static String resolveAppNameFromClass(Class clazz, String defaultName) {
        String n = null;
        String baseFilePath = NOptional.of(clazz.getProtectionDomain()).then(x -> x.getCodeSource()).then(x -> x.getLocation()).then(x -> x.getPath()).orNull();
        NLog nLog = NLog.of(NApplicationImpl.class);
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
            n = NStringUtils.strip(n);
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
    public NApplicationMode mode() {
        return this.mode;
    }

    @Override
    public List<String> modeArguments() {
        return this.modeArgs;
    }

    @Override
    public NArgCompletePosition completePosition() {
        return this.completePos;
    }

    @Override
    public NOptional<NText> helpText() {
        NText h = null;
        try {
            h = NWorkspaceExt.of().resolveDefaultHelp(sourceType());
        } catch (Exception ex) {
            //
        }
        if (h != null) {
            try {
                h = NText.transform(h, new NTextTransformConfig()
                        .processTitleNumbers(true)
                        .normalize(true)
                        .flatten(true)
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
        NText h = NWorkspaceExt.of().resolveDefaultHelp(sourceType());
        h = NText.transform(h, new NTextTransformConfig()
                .processTitleNumbers(true)
                .normalize(true)
                .flatten(true)
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
    public Class<?> sourceType() {
        return this.sourceType;
    }

    @Override
    public NPath binFolder() {
        return getFolder(NStoreType.BIN);
    }

    @Override
    public NPath confFolder() {
        return getFolder(NStoreType.CONF);
    }

    @Override
    public NPath logFolder() {
        return getFolder(NStoreType.LOG);
    }

    @Override
    public NPath tempFolder() {
        return getFolder(NStoreType.TEMP);
    }

    @Override
    public NPath varFolder() {
        return getFolder(NStoreType.VAR);
    }

    @Override
    public NPath libFolder() {
        return getFolder(NStoreType.LIB);
    }

    @Override
    public NPath runFolder() {
        return getFolder(NStoreType.RUN);
    }

    @Override
    public NPath cacheFolder() {
        return getFolder(NStoreType.CACHE);
    }

    @Override
    public NPath getVersionFolder(NStoreType storeType, String version) {
        if (version == null
                || version.isEmpty()
                || version.equalsIgnoreCase("current")
                || version.equals(id().get().version().value())) {
            return getFolder(storeType);
        }
        NId newId = id().get().builder().version(version).build();
        if (this.storeLocationResolver != null) {
            NPath r = this.storeLocationResolver.getStoreLocation(newId, storeType);
            if (r != null) {
                return r;
            }
        }
        return NPath.of(NStoreKey.of(newId).type(storeType));
    }

    @Override
    public NPath sharedAppsFolder() {
        return getSharedFolder(NStoreType.BIN);
    }

    @Override
    public NPath sharedConfFolder() {
        return getSharedFolder(NStoreType.CONF);
    }

    @Override
    public NPath sharedLogFolder() {
        return getSharedFolder(NStoreType.LOG);
    }

    @Override
    public NPath sharedTempFolder() {
        return getSharedFolder(NStoreType.TEMP);
    }

    @Override
    public NPath sharedVarFolder() {
        return getSharedFolder(NStoreType.VAR);
    }

    @Override
    public NPath sharedLibFolder() {
        return getSharedFolder(NStoreType.LIB);
    }

    @Override
    public NPath sharedRunFolder() {
        return getSharedFolder(NStoreType.RUN);
    }

    @Override
    public NPath getSharedFolder(NStoreType location) {
        return this.sharedFolders[location.ordinal()];
    }

    @Override
    public NOptional<NVersion> version() {
        return this.id().map(NId::version);
    }

    @Override
    public List<String> arguments() {
        return this.args;
    }

    @Override
    public NClock startTime() {
        return this.startTime;
    }

    @Override
    public NOptional<NVersion> previousVersion() {
        return NOptional.ofNamed(previousVersion, "previousVersion");
    }

    @Override
    public NCmdLine cmdLine() {
        NId appId = id().orNull();
        if (appId == null) {
            return null;
        }
        List<String> appArguments = arguments();
        if (appArguments == null) {
            return null;
        }
        NArgCompletePosition cp = completePosition();
        return NCmdLine.of(appArguments)
                .commandName(appId.artifactId())
                .completePosition(cp)
                ;
    }

    @Override
    public NPath getFolder(NStoreType location) {
        return this.folders[location.ordinal()];
    }

    @Override
    public boolean isExecMode() {
        return completePosition() == null;
    }

    @Override
    public NAppStoreLocationResolver storeLocationResolver() {
        return this.storeLocationResolver;
    }

    public NApplication setVersionStoreLocationSupplier(NAppStoreLocationResolver appVersionStoreLocationSupplier) {
        this.storeLocationResolver = appVersionStoreLocationSupplier;
        return this;
    }

    public NApplication setMode(NApplicationMode mode) {
        this.mode = mode;
        return this;
    }

    public NApplication setModeArgs(List<String> modeArgs) {
        this.modeArgs = modeArgs;
        return this;
    }

    public NApplication setFolder(NStoreType location, NPath folder) {
        this.folders[location.ordinal()] = folder;
        return this;
    }

    public NApplication setSharedFolder(NStoreType location, NPath folder) {
        this.sharedFolders[location.ordinal()] = folder;
        return this;
    }

    //    @Override
    public NApplication id(NId appId) {
        this.id = appId;
        return this;
    }

    //    @Override
    public NApplication arguments(List<String> args) {
        this.args = args;
        return this;
    }

    public NApplication arguments(String[] args) {
        this.args = new ArrayList<>(Arrays.asList(args));
        return this;
    }

    public NApplication startTime(NClock startTime) {
        this.startTime = startTime;
        return this;
    }

    public NApplication setPreviousVersion(NVersion previousVersion) {
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

}
