package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.artifact.NIdWriter;
import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.concurrent.NConcurrent;
import net.thevpc.nuts.concurrent.NScopedStack;
import net.thevpc.nuts.core.NBootOptions;
import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.concurrent.NScopedValue;
import net.thevpc.nuts.core.NIsolationLevel;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.elem.NElementReader;
import net.thevpc.nuts.elem.NElementWriter;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.internal.rpi.*;
import net.thevpc.nuts.io.NDigest;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.net.NWebCli;
import net.thevpc.nuts.platform.NEnv;
import net.thevpc.nuts.platform.NRuntimeDistributionFamily;
import net.thevpc.nuts.platform.NRuntimeDistribution;
import net.thevpc.nuts.reflect.NBeanContainer;
import net.thevpc.nuts.reflect.NBeanRef;
import net.thevpc.nuts.reflect.NReflectRepository;
import net.thevpc.nuts.runtime.standalone.*;
import net.thevpc.nuts.runtime.standalone.app.cmdline.DefaultNCmdLineRPI;
import net.thevpc.nuts.runtime.standalone.collections.DefaultNUtilsRPI;
import net.thevpc.nuts.runtime.standalone.concurrent.NConcurrentImpl;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElementRPI;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElementWriter;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElements;
import net.thevpc.nuts.runtime.standalone.elem.parser.DefaultNElementReader;
import net.thevpc.nuts.runtime.standalone.event.DefaultNWorkspaceEventModel;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNExtensions;
import net.thevpc.nuts.runtime.standalone.elem.parser.mapperstore.DefaultElementMapperStore;
import net.thevpc.nuts.runtime.standalone.extension.NExtensionCatalogManager;
import net.thevpc.nuts.runtime.standalone.extension.NExtensionTypeInfo;
import net.thevpc.nuts.runtime.standalone.format.DefaultNObjectObjectWriter;
import net.thevpc.nuts.runtime.standalone.id.format.DefaultNIdWriter;
import net.thevpc.nuts.runtime.standalone.io.cache.CachedSupplier;
import net.thevpc.nuts.runtime.standalone.io.inputstream.DefaultNIO;
import net.thevpc.nuts.runtime.standalone.io.inputstream.DefaultNIORPI;
import net.thevpc.nuts.runtime.standalone.log.DefaultNLog;
import net.thevpc.nuts.runtime.standalone.log.DefaultNLogRPI;
import net.thevpc.nuts.runtime.standalone.log.NLogSPIJUL;
import net.thevpc.nuts.runtime.standalone.platform.NEnvLocal;
import net.thevpc.nuts.runtime.standalone.reflect.DefaultNReflectRPI;
import net.thevpc.nuts.runtime.standalone.reflect.DefaultNReflectRepository;
import net.thevpc.nuts.runtime.standalone.store.NWorkspaceStore;
import net.thevpc.nuts.runtime.standalone.store.NWorkspaceStoreInMemory;
import net.thevpc.nuts.runtime.standalone.store.NWorkspaceStoreOnDisk;
import net.thevpc.nuts.runtime.standalone.collections.NLRUMapImpl;
import net.thevpc.nuts.runtime.standalone.collections.NNormalizedStringMapImpl;
import net.thevpc.nuts.runtime.standalone.text.DefaultNTextRPI;
import net.thevpc.nuts.runtime.standalone.version.format.DefaultNVersionWriter;
import net.thevpc.nuts.runtime.standalone.workspace.DefaultNWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.NFailSafeHelper;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultNExec;
import net.thevpc.nuts.runtime.standalone.xtra.digest.DefaultNDigest;
import net.thevpc.nuts.runtime.standalone.xtra.expr.NExprRPIImpl;
import net.thevpc.nuts.runtime.standalone.xtra.web.DefaultNWebCli;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.mon.NProgressMonitor;
import net.thevpc.nuts.text.NObjectObjectWriter;
import net.thevpc.nuts.text.NVersionWriter;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.runtime.standalone.util.NPropertiesHolder;
import net.thevpc.nuts.runtime.standalone.util.filters.DefaultNFilterModel;
import net.thevpc.nuts.runtime.standalone.text.DefaultNTextManagerModel;
import net.thevpc.nuts.runtime.standalone.log.DefaultNLogModel;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNBootModel;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNWorkspaceExtensionModel;
import net.thevpc.nuts.runtime.standalone.repository.config.DefaultNRepositoryModel;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.DefaultNInstalledRepository;
import net.thevpc.nuts.runtime.standalone.security.DefaultNWorkspaceSecurityModel;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.recom.SafeRecommendationConnector;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.recom.SimpleRecommendationConnector;
import net.thevpc.nuts.runtime.standalone.collections.NDefaultObservableMap;
import net.thevpc.nuts.collections.NLRUMap;
import net.thevpc.nuts.collections.NObservableMap;

import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.function.Supplier;

public class NWorkspaceModel {
    public NLog LOG;
    public NLogRPI defaultNLogRPI;
    public NReflectRPI defaultNReflectRPI;
    public NElementRPI defaultNElementRPI;
    public NIORPI defaultNIORPI;
    public NUtilsRPI defaultNUtilsRPI;
    public NTextRPI defaultNTextRPI;
    public NCmdLineRPI defaultNCmdLineRPI;


    public NExtensionCatalogManager extensionCatalogManager=new NExtensionCatalogManager();


    public NWorkspace workspace;
    public NScopedValue<NSession> sessionScopes = new NScopedValue<>();
    public NSession initSession;
    public DefaultNBootModel bootModel;
    public DefaultNWorkspaceSecurityModel securityModel;
    public DefaultNFilterModel filtersModel;
    public DefaultNWorkspaceConfigModel configModel;
    public DefaultNWorkspaceLocationModel locationsModel;
    public DefaultNRepositoryModel repositoryModel;
    public DefaultNWorkspaceEventModel eventsModel;
    public DefaultNTextManagerModel textModel;
    public String uuid;
    public String location;
    public String name;
    public String hashName;
    public NId apiId;
    public NId runtimeId;
    public DefaultNInstalledRepository installedRepository;
    public final NScopedStack<NBeanContainer> scopedBeanContainerStack = new NScopedStack<>(null);
    public final NBeanContainer scopedBeanContainer = new StackBasedNBeanContainer();
    public DefaultNLogModel logModel;
    public DefaultNPlatformModel sdkModel;
    public DefaultNWorkspaceExtensionModel extensionModel;
    public DefaultCustomCommandsModel commandModel;
    public DefaultImportModel importModel;
    public String apiDigest;
    public String installationDigest;
    public SafeRecommendationConnector recomm;
    public List<String> recommendedCompanions = new ArrayList<>();
    public NPropertiesHolder properties = new NPropertiesHolder();
    public NVersion askedApiVersion;
    public NId askedRuntimeId;
    public NBootOptions initialBootOptions;
    public NLRUMap<NId, CachedSupplier<NDefinition>> cachedDefs = new NLRUMapImpl<>(100);
    public DefaultNExtensions extensions;
    public NWorkspaceStore store;
    public DefaultElementMapperStore defaultElementMapperStore = new DefaultElementMapperStore();
    public DefaultNElements defaultElements;
    public NScopedValue<NProgressMonitor> currentProgressMonitors = new NScopedValue<>();
    protected NObservableMap<String, Object> userProperties;
    private String pid;
    private NEnvLocal env;
    public ClassLoader bootClassLoader;
    private final Map<NRuntimeDistributionFamily, List<NRuntimeDistribution>> configPlatforms = new LinkedHashMap<>();
    private NReflectRepository defaultReflectRepository;


    public NWorkspaceModel(NWorkspace workspace, NBootOptions initialBootOptions) {
        this.workspace = workspace;
        this.userProperties = new NDefaultObservableMap<>();
        this.logModel = new DefaultNLogModel(workspace);
        this.LOG = new DefaultNLog(DefaultNWorkspace.class.getName(), new NLogSPIJUL(DefaultNWorkspace.class.getName()), logModel, false);
        if (initialBootOptions.isolationLevel().orNull() == NIsolationLevel.MEMORY) {
            this.store = new NWorkspaceStoreInMemory();
        } else {
            this.store = new NWorkspaceStoreOnDisk();
        }
        this.recomm = new SafeRecommendationConnector(new SimpleRecommendationConnector());
        this.initialBootOptions = initialBootOptions;
        // initialized here because they just do nothing...
        this.commandModel = new DefaultCustomCommandsModel(workspace);
        this.importModel = new DefaultImportModel(workspace);
        this.eventsModel = new DefaultNWorkspaceEventModel(workspace);
        this.repositoryModel = new DefaultNRepositoryModel(workspace);
        this.extensions = new DefaultNExtensions(this);
        this.bootModel = new DefaultNBootModel(workspace, this, initialBootOptions, LOG);
        this.bootClassLoader = initialBootOptions.classWorldLoader().orNull();
    }

    public <T> NOptional<T> createDefault(Class<T> type,Object supportCriteria) {
        //fallback needed in bootstrap or if the extensions are broken!
        switch (type.getName()) {
            case "net.thevpc.nuts.text.NObjectObjectWriter": {
                NObjectObjectWriter p = NExtensionTypeInfo.getOrComputeCachedBean(DefaultNObjectObjectWriter.class, NObjectObjectWriter.class, NScopeType.SESSION, DefaultNObjectObjectWriter::new);
                return NOptional.of((T) p);
            }
            case "net.thevpc.nuts.io.NIO": {
                NIO p = NExtensionTypeInfo.getOrComputeCachedBean(DefaultNIO.class, NIO.class, NScopeType.WORKSPACE, DefaultNIO::new);
                return NOptional.of((T) p);
            }
            case "net.thevpc.nuts.elem.NElements": {
                NElements p = NExtensionTypeInfo.getOrComputeCachedBean(DefaultNElements.class, NElements.class, NScopeType.SESSION, DefaultNElements::new);
                return NOptional.of((T) p);
            }
            case "net.thevpc.nuts.elem.NElementWriter": {
                NElementWriter p = NExtensionTypeInfo.getOrComputeCachedBean(DefaultNElementWriter.class, NElementWriter.class, NScopeType.SESSION, DefaultNElementWriter::new);
                return NOptional.of((T) p);
            }
            case "net.thevpc.nuts.elem.NElementReader": {
                NElementReader p = NExtensionTypeInfo.getOrComputeCachedBean(DefaultNElementReader.class, NElementReader.class, NScopeType.SESSION, DefaultNElementReader::new);
                return NOptional.of((T) p);
            }
            case "net.thevpc.nuts.io.NDigest": {
                NDigest p = NExtensionTypeInfo.getOrComputeCachedBean(DefaultNDigest.class, NDigest.class, NScopeType.SESSION, DefaultNDigest::new);
                return NOptional.of((T) p);
            }
            case "net.thevpc.nuts.artifact.NIdWriter": {
                NIdWriter p = NExtensionTypeInfo.getOrComputeCachedBean(DefaultNIdWriter.class, NIdWriter.class, NScopeType.SESSION, DefaultNIdWriter::new);
                return NOptional.of((T) p);
            }
            case "net.thevpc.nuts.text.NVersionWriter": {
                NVersionWriter p = NExtensionTypeInfo.getOrComputeCachedBean(DefaultNVersionWriter.class, NVersionWriter.class, NScopeType.SESSION, DefaultNVersionWriter::new);
                return NOptional.of((T) p);
            }
            case "net.thevpc.nuts.command.NExec": {
                NExec p = NExtensionTypeInfo.getOrComputeCachedBean(DefaultNExec.class, NExec.class, NScopeType.SESSION, DefaultNExec::new);
                return NOptional.of((T) p);
            }
            case "net.thevpc.nuts.net.NWebCli": {
                NWebCli p = NExtensionTypeInfo.getOrComputeCachedBean(DefaultNWebCli.class, NWebCli.class, NScopeType.SESSION, DefaultNWebCli::new);
                return NOptional.of((T) p);
            }
            case "net.thevpc.nuts.artifact.NIdBuilder": {
                return NOptional.of((T) new DefaultNIdBuilder());
            }
            case "net.thevpc.nuts.artifact.NDependencyBuilder": {
                return NOptional.of((T) new DefaultNDependencyBuilder());
            }
            case "net.thevpc.nuts.artifact.NEnvConditionBuilder": {
                return NOptional.of((T) new DefaultNEnvConditionBuilder());
            }
            case "net.thevpc.nuts.artifact.NDescriptorBuilder": {
                return NOptional.of((T) new DefaultNDescriptorBuilder());
            }
            case "net.thevpc.nuts.core.NBootOptionsBuilder": {
                return NOptional.of((T) new DefaultNBootOptionsBuilder());
            }
            case "net.thevpc.nuts.core.NWorkspaceOptionsBuilder": {
                return NOptional.of((T) new DefaultNWorkspaceOptionsBuilder());
            }
            case "net.thevpc.nuts.concurrent.NConcurrent": {
                NConcurrent p = NExtensionTypeInfo.getOrComputeCachedBean(NConcurrentImpl.class, NConcurrent.class, NScopeType.WORKSPACE, NConcurrentImpl::new);
                return NOptional.of((T) p);
            }
            case "net.thevpc.nuts.platform.NEnv": {
                if (supportCriteria == null) {
                    NEnvLocal env = getEnv();
                    return NOptional.of((T) env);
                }
                break;
            }
            default: {
                //wont use NLog because not yet initialized!
            }
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("missing %s", type));
    }
    public <T> T createRPI(Class<T> cls) {
        switch (cls.getName()) {
            case "net.thevpc.nuts.app.NApplication": {
                return (T) NWorkspaceExt.of().getApp();
            }
            case "net.thevpc.nuts.internal.rpi.NLogRPI": {
                NLogRPI t = defaultNLogRPI;
                if (t == null) {
                    t = new DefaultNLogRPI();
                    defaultNLogRPI = t;
                }
                return (T) t;
            }
            //log will need Element Factory so...

            //log will need NCollectionsRPI so...
            case "net.thevpc.nuts.internal.rpi.NUtilsRPI": {
                NUtilsRPI t = defaultNUtilsRPI;
                if (t == null) {
                    t = new DefaultNUtilsRPI();
                    defaultNUtilsRPI = t;
                }
                return (T) t;
            }
            case "net.thevpc.nuts.internal.rpi.NTextRPI": {
                NTextRPI t = defaultNTextRPI;
                if (t == null) {
                    t = new DefaultNTextRPI();
                    defaultNTextRPI = t;
                }
                return (T) t;
            }
            case "net.thevpc.nuts.internal.rpi.NReflectRPI": {
                NReflectRPI t = defaultNReflectRPI;
                if (t == null) {
                    t = new DefaultNReflectRPI();
                    defaultNReflectRPI = t;
                }
                return (T) t;
            }
            case "net.thevpc.nuts.internal.rpi.NElementRPI": {
                NElementRPI t = defaultNElementRPI;
                if (t == null) {
                    t = new DefaultNElementRPI();
                    defaultNElementRPI = t;
                }
                return (T) t;
            }
            case "net.thevpc.nuts.internal.rpi.NIORPI": {
                NIORPI t = defaultNIORPI;
                if (t == null) {
                    t = new DefaultNIORPI();
                    defaultNIORPI = t;
                }
                return (T) t;
            }
            case "net.thevpc.nuts.internal.rpi.NCmdLineRPI": {
                NCmdLineRPI t = defaultNCmdLineRPI;
                if (t == null) {
                    t = new DefaultNCmdLineRPI();
                    defaultNCmdLineRPI = t;
                }
                return (T) t;
            }
            case "net.thevpc.nuts.internal.rpi.NExprRPI": {
                return ((T) new NExprRPIImpl());
            }
        }
        return null;
    }

    public void init() {
        defaultElements = new DefaultNElements(false,false);//lets enable global config
        askedApiVersion = initialBootOptions.apiVersion().orNull();
        askedRuntimeId = initialBootOptions.runtimeId().orNull();
        if (askedRuntimeId == null) {
            askedRuntimeId = NId.getRuntime("").get();
        }
        ((DefaultNWorkspace)NWorkspace.of()).env = rootEnv();
        this.textModel = new DefaultNTextManagerModel(workspace);
        this.apiId = NId.getApi(Nuts.version()).get();
        this.runtimeId = NId.get(
                askedRuntimeId.groupId(),
                askedRuntimeId.artifactId(),
                NVersion.get(askedRuntimeId.version().toString()).get()).get();
        this.logModel.init(this.bootModel.getBootEffectiveOptions(), initialBootOptions);
        this.bootModel.init();
    }

    public Map<String, String> appendEnv(Map<String, String> env) {
        Map<String, String> curr = NWorkspaceExt.of().getSysEnv();
        Map<String, String> m = newSysEnvEmptyMap();
        m.putAll(curr);
        if (env != null) {
            for (Map.Entry<String, String> e : env.entrySet()) {
                String k = e.getKey();
                String v = e.getValue();
                if (k != null) {
                    if (v == null) {
                        m.remove(k);
                    } else {
                        m.put(k, v);
                    }
                }
            }
        }
        return m;
    }


    public Map<String, String> rootEnv() {
        Map<String, String> m = newSysEnvEmptyMap();
        m.putAll(System.getenv());
        return m;
    }

    public Map<String, String> newSysEnvEmptyMap() {
        switch (getEnv().osFamily()) {
            case WINDOWS: {
                return NNormalizedStringMapImpl.ofCaseInsensitive();
            }
        }
        return new HashMap<>();
    }


    public NEnvLocal getEnv() {
        if (env == null) {
            env = (NEnvLocal) NExtensionTypeInfo.getOrComputeCachedBean(NEnvLocal.class, NEnv.class, NScopeType.WORKSPACE, NEnvLocal::new);
        }
        return env;
    }

    public String getPid() {
        if (pid == null) {
            String fallback = "";
            // Note: may fail in some JVM implementations
            // therefore fallback has to be provided

            // something like '<pid>@<hostname>', at least in SUN / Oracle JVMs
            final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
            final int index = jvmName.indexOf('@');
            if (index < 1) {
                // part before '@' empty (index = 0) / '@' not found (index = -1)
                return pid = fallback;
            }

            try {
                return pid = String.valueOf(Long.toString(Long.parseLong(jvmName.substring(0, index))));
            } catch (NumberFormatException e) {
                // ignore
            }
            return pid = fallback;
        }
        return pid;
    }


    public Map<String, Object> getProperties() {
        return userProperties;
    }

    public NOptional<Object> getProperty(String property) {
        Object v = userProperties.get(property);
        return NOptional.ofNamed(v, property);
    }

//    public NElement getPropertyElement(String property) {
//        return NElements.of()
//                .toElement(getProperty(property));
//    }

    public <T> T getOrCreateProperty(Class<T> property, Supplier<T> supplier) {
        return getOrCreateProperty(property.getName(), supplier);
    }

    public synchronized <T> T getOrCreateProperty(String property, Supplier<T> supplier) {
        Object o = getProperty(property).orNull();
        if (o != null) {
            return (T) o;
        }
        o = supplier.get();
        setProperty(property, o);
        return (T) o;
    }

    public void setProperty(String property, Object value) {
        if (value == null) {
            userProperties.remove(property);
        } else {
            userProperties.put(property, value);
        }
    }

    public NWorkspace getWorkspace() {
        return workspace;
    }

    public Map<NRuntimeDistributionFamily, List<NRuntimeDistribution>> getConfigPlatforms() {
        return configPlatforms;
    }

    public NReflectRepository getDefaultReflectRepository() {
        if(defaultReflectRepository==null){
            defaultReflectRepository=new DefaultNReflectRepository();
        }
        return defaultReflectRepository;
    }

    private class StackBasedNBeanContainer implements NBeanContainer {
        @Override
        public <T> NOptional<T> get(NBeanRef ref) {
            List<NBeanContainer> all;
            synchronized (scopedBeanContainerStack) {
                all = scopedBeanContainerStack.stackSnapshot();
            }
            NOptional<T> firstError = null;
            for (int i = all.size() - 1; i >= 0; i--) {
                NBeanContainer e = all.get(i);
                NOptional<T> r = e.get(ref);
                if (r.isPresent()) {
                    return r;
                } else if (firstError == null) {
                    firstError = r;
                }
            }
            if (firstError != null) {
                return firstError;
            }
            return NOptional.ofNamedEmpty(NMsg.ofC("bean %s", ref));
        }
    }
}
