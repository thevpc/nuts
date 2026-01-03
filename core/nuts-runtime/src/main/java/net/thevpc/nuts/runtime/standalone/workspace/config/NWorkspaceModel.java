package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.concurrent.NScopedStack;
import net.thevpc.nuts.core.NBootOptions;
import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.concurrent.NScopedValue;
import net.thevpc.nuts.core.NIsolationLevel;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogs;
import net.thevpc.nuts.platform.NExecutionEngineFamily;
import net.thevpc.nuts.platform.NExecutionEngineLocation;
import net.thevpc.nuts.reflect.NBeanContainer;
import net.thevpc.nuts.reflect.NBeanRef;
import net.thevpc.nuts.runtime.standalone.app.NAppImpl;
import net.thevpc.nuts.runtime.standalone.event.DefaultNWorkspaceEventModel;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNExtensions;
import net.thevpc.nuts.runtime.standalone.elem.parser.mapperstore.DefaultElementMapperStore;
import net.thevpc.nuts.runtime.standalone.io.cache.CachedSupplier;
import net.thevpc.nuts.runtime.standalone.log.DefaultNLog;
import net.thevpc.nuts.runtime.standalone.log.NLogSPIJUL;
import net.thevpc.nuts.runtime.standalone.platform.NEnvLocal;
import net.thevpc.nuts.runtime.standalone.store.NWorkspaceStore;
import net.thevpc.nuts.runtime.standalone.store.NWorkspaceStoreInMemory;
import net.thevpc.nuts.runtime.standalone.store.NWorkspaceStoreOnDisk;
import net.thevpc.nuts.runtime.standalone.workspace.DefaultNWorkspace;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.time.NProgressMonitor;
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

import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.function.Supplier;

public class NWorkspaceModel {
    public NLog LOG;
    public NLogs defaultNLogs;
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
    public NLRUMap<NId, CachedSupplier<NDefinition>> cachedDefs = new NLRUMap<>(100);
    public DefaultNExtensions extensions;
    public NWorkspaceStore store;
    public DefaultElementMapperStore defaultElementMapperStore = new DefaultElementMapperStore();
    public NScopedValue<NProgressMonitor> currentProgressMonitors = new NScopedValue<>();
    protected NObservableMap<String, Object> userProperties;
    private String pid;
    private NEnvLocal env;
    private final Map<NExecutionEngineFamily, List<NExecutionEngineLocation>> configPlatforms = new LinkedHashMap<>();

    public NWorkspaceEnvScope currentEnvInitial = new NWorkspaceEnvScope();
    public NScopedValue<NWorkspaceEnvScope> currentEnv = NScopedValue.of();

    public NWorkspaceEnvScope getRequiredNWorkspaceEnvScope() {
        NWorkspaceEnvScope u = currentEnv.getOrElse(() -> null);
        if (u == null) {
            throw new NIllegalStateException(NMsg.ofC("workspace environment was nto found"));
        }
        return u;
    }


    public NWorkspaceModel(NWorkspace workspace, NBootOptions initialBootOptions) {
        this.workspace = workspace;
        this.userProperties = new NDefaultObservableMap<>();
        this.logModel = new DefaultNLogModel(workspace);
        this.LOG = new DefaultNLog(DefaultNWorkspace.class.getName(), new NLogSPIJUL(DefaultNWorkspace.class.getName()), logModel, false);
        if (initialBootOptions.getIsolationLevel().orNull() == NIsolationLevel.MEMORY) {
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
    }

    public void init() {
        askedApiVersion = initialBootOptions.getApiVersion().orNull();
        askedRuntimeId = initialBootOptions.getRuntimeId().orNull();
        if (askedRuntimeId == null) {
            askedRuntimeId = NId.getRuntime("").get();

        }
        currentEnvInitial.currentApp = new NAppImpl();
        currentEnvInitial.env = rootEnv();
        currentEnv.getOrCompute(() -> currentEnvInitial);
        this.textModel = new DefaultNTextManagerModel(workspace);
        this.apiId = NId.getApi(Nuts.getVersion()).get();
        this.runtimeId = NId.get(
                askedRuntimeId.getGroupId(),
                askedRuntimeId.getArtifactId(),
                NVersion.get(askedRuntimeId.getVersion().toString()).get()).get();
        this.logModel.init(this.bootModel.getBootEffectiveOptions(), initialBootOptions);
        this.bootModel.init();
    }

    public Map<String, String> appendEnv(Map<String, String> env) {
        Map<String, String> curr = getRequiredNWorkspaceEnvScope().env;
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
        switch (getEnv().getOsFamily()) {
            case WINDOWS: {
                return new NCaseInsensitiveStringMap<>();
            }
        }
        return new HashMap<>();
    }


    public NEnvLocal getEnv() {
        if (env == null) {
            env = new NEnvLocal();
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

    public Map<NExecutionEngineFamily, List<NExecutionEngineLocation>> getConfigPlatforms() {
        return configPlatforms;
    }

    private class StackBasedNBeanContainer implements NBeanContainer {
        @Override
        public <T> NOptional<T> get(NBeanRef ref) {
            List<NBeanContainer> all;
            synchronized (scopedBeanContainerStack) {
                all = scopedBeanContainerStack.getStackSnapshot();
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
