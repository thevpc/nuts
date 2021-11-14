/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.CoreNutsConstants;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.app.DefaultNutsWorkspaceLocationManager;
import net.thevpc.nuts.runtime.core.common.TimePeriod;
import net.thevpc.nuts.runtime.core.config.NutsRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.core.events.DefaultNutsWorkspaceEvent;
import net.thevpc.nuts.runtime.core.io.*;
import net.thevpc.nuts.runtime.core.model.CoreNutsWorkspaceOptions;
import net.thevpc.nuts.runtime.core.model.DefaultNutsDefinition;
import net.thevpc.nuts.runtime.core.repos.NutsInstalledRepository;
import net.thevpc.nuts.runtime.core.repos.NutsRepositorySelector;
import net.thevpc.nuts.runtime.core.terminals.*;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.core.util.CoreNutsDependencyUtils;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.*;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNutsBootManager;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNutsBootModel;
import net.thevpc.nuts.runtime.standalone.bridges.maven.MavenUtils;
import net.thevpc.nuts.runtime.standalone.config.compat.CompatUtils;
import net.thevpc.nuts.runtime.standalone.config.compat.NutsVersionCompat;
import net.thevpc.nuts.runtime.standalone.config.compat.v502.NutsVersionCompat502;
import net.thevpc.nuts.runtime.standalone.config.compat.v506.NutsVersionCompat506;
import net.thevpc.nuts.runtime.standalone.config.compat.v507.NutsVersionCompat507;
import net.thevpc.nuts.runtime.standalone.config.compat.v803.NutsVersionCompat803;
import net.thevpc.nuts.runtime.standalone.io.NutsPrintStreamNull;
import net.thevpc.nuts.runtime.standalone.io.NutsWorkspaceVarExpansionFunction;
import net.thevpc.nuts.runtime.standalone.security.ReadOnlyNutsWorkspaceOptions;
import net.thevpc.nuts.runtime.standalone.solvers.NutsDependencySolverUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author thevpc
 */
public class DefaultNutsWorkspaceConfigModel {
    public static final Pattern MOSTLY_URL_PATTERN = Pattern.compile("([a-zA-Z][a-zA-Z0-9_-]+):.*");

    public final NutsPrintStream nullOut;
    private final DefaultNutsWorkspace ws;
    private final Map<String, NutsUserConfig> configUsers = new LinkedHashMap<>();
    private final NutsWorkspaceStoredConfig storedConfig = new NutsWorkspaceStoredConfigImpl();
    private final ClassLoader bootClassLoader;
    private final URL[] bootClassWorldURLs;
    private final NutsWorkspaceOptions options;
    private final NutsWorkspaceInitInformation initOptions;
    private final Function<String, String> pathExpansionConverter;
    private final WorkspaceSystemTerminalAdapter workspaceSystemTerminalAdapter;
    private final List<NutsPathFactory> pathFactories = new ArrayList<>();
    private final NutsPathFactory invalidPathFactory;
    private final DefaultNutsBootModel bootModel;
    protected NutsWorkspaceConfigBoot storeModelBoot = new NutsWorkspaceConfigBoot();
    protected NutsWorkspaceConfigApi storeModelApi = new NutsWorkspaceConfigApi();
    protected NutsWorkspaceConfigRuntime storeModelRuntime = new NutsWorkspaceConfigRuntime();
    protected NutsWorkspaceConfigSecurity storeModelSecurity = new NutsWorkspaceConfigSecurity();
    protected NutsWorkspaceConfigMain storeModelMain = new NutsWorkspaceConfigMain();
    protected Map<String, NutsDependencySolverFactory> dependencySolvers;
    private NutsLogger LOG;
    private DefaultNutsWorkspaceCurrentConfig currentConfig;
    private boolean storeModelBootChanged = false;
    private boolean storeModelApiChanged = false;
    private boolean storeModelRuntimeChanged = false;
    private boolean storeModelSecurityChanged = false;
    private boolean storeModelMainChanged = false;
    private long startCreateTime;
    private long endCreateTime;
    private NutsIndexStoreFactory indexStoreClientFactory;
    //    private Set<String> excludedRepositoriesSet = new HashSet<>();
    private NutsStoreLocationsMap preUpdateConfigStoreLocations;
    private NutsRepositorySelector.SelectorList parsedBootRepositoriesList;
    //    private NutsRepositorySelector[] parsedBootRepositoriesArr;
    private ExecutorService executorService;
    private NutsSessionTerminal terminal;
    private NutsSystemTerminal systemTerminal;
    //    private final NutsLogger LOG;
    private InputStream stdin = null;
    private NutsPrintStream stdout;
    private NutsPrintStream stderr;

    public DefaultNutsWorkspaceConfigModel(final DefaultNutsWorkspace ws, NutsWorkspaceInitInformation initOptions) {
        this.ws = ws;
        this.initOptions = initOptions;
        this.options = this.initOptions.getOptions();
        this.bootClassLoader = initOptions.getClassWorldLoader() == null ? Thread.currentThread().getContextClassLoader() : initOptions.getClassWorldLoader();
        this.bootClassWorldURLs = initOptions.getClassWorldURLs() == null ? null : Arrays.copyOf(initOptions.getClassWorldURLs(), initOptions.getClassWorldURLs().length);
        workspaceSystemTerminalAdapter = new WorkspaceSystemTerminalAdapter(ws);

        this.pathExpansionConverter = new NutsWorkspaceVarExpansionFunction(NutsWorkspaceUtils.defaultSession(ws));
        this.bootModel = (DefaultNutsBootModel) ((DefaultNutsBootManager) ws.boot()).getModel();
        this.stdout = bootModel.stdout();
        this.stderr = bootModel.stderr();
        this.stdin = bootModel.stdin();
        this.nullOut = new NutsPrintStreamNull(bootModel.bootSession());
        addPathFactory(new FilePathFactory());
        addPathFactory(new ClasspathNutsPathFactory());
        addPathFactory(new URLPathFactory());
        addPathFactory(new NutsResourcePathFactory());
        invalidPathFactory=new InvalidFilePathFactory();
        //        this.excludedRepositoriesSet = this.options.getExcludedRepositories() == null ? null : new HashSet<>(CoreStringUtils.split(Arrays.asList(this.options.getExcludedRepositories()), " ,;"));
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = NutsLogger.of(DefaultNutsWorkspaceConfigModel.class, session);
        }
        return LOG;
    }

    public DefaultNutsWorkspaceCurrentConfig getCurrentConfig() {
        return currentConfig;
    }

    public void setCurrentConfig(DefaultNutsWorkspaceCurrentConfig currentConfig) {
        this.currentConfig = currentConfig;
    }

    public NutsWorkspaceStoredConfig stored() {
        return storedConfig;
    }

    public ClassLoader getBootClassLoader() {
        return bootClassLoader;
    }

    public URL[] getBootClassWorldURLs() {
        return bootClassWorldURLs == null ? null : Arrays.copyOf(bootClassWorldURLs, bootClassWorldURLs.length);
    }

    public boolean isReadOnly() {
        return options.isReadOnly();
    }

    public boolean save(boolean force, NutsSession session) {
        if (!force && !isConfigurationChanged()) {
            return false;
        }
        NutsWorkspaceUtils.of(session).checkReadOnly();
        NutsWorkspaceUtils.checkSession(this.ws, session);
        boolean ok = false;
        session.security().checkAllowed(NutsConstants.Permissions.SAVE, "save");
        NutsPath apiVersionSpecificLocation = session.locations().getStoreLocation(session.getWorkspace().getApiId(), NutsStoreLocation.CONFIG);
        NutsElements elem = NutsElements.of(session);
        if (force || storeModelBootChanged) {

            Path file = session.locations().getWorkspaceLocation().toFile().resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
            storeModelBoot.setConfigVersion(DefaultNutsWorkspace.VERSION_WS_CONFIG_BOOT);
            if (storeModelBoot.getExtensions() != null) {
                for (NutsWorkspaceConfigBoot.ExtensionConfig extension : storeModelBoot.getExtensions()) {
                    //inherited
                    extension.setConfigVersion(null);
                }
            }
            elem.json().setValue(storeModelBoot)
                    .setNtf(false).print(file);
            storeModelBootChanged = false;
            ok = true;
        }

        NutsPath configVersionSpecificLocation = session.locations().getStoreLocation(session.getWorkspace().getApiId().builder().setVersion(NutsConstants.Versions.RELEASE).build(), NutsStoreLocation.CONFIG);
        if (force || storeModelSecurityChanged) {
            storeModelSecurity.setUsers(configUsers.isEmpty() ? null : configUsers.values().toArray(new NutsUserConfig[0]));

            NutsPath file = configVersionSpecificLocation.resolve(CoreNutsConstants.Files.WORKSPACE_SECURITY_CONFIG_FILE_NAME);
            storeModelSecurity.setConfigVersion(current().getApiVersion());
            if (storeModelSecurity.getUsers() != null) {
                for (NutsUserConfig extension : storeModelSecurity.getUsers()) {
                    //inherited
                    extension.setConfigVersion(null);
                }
            }
            elem.setSession(session).json().setValue(storeModelSecurity)
                    .setNtf(false).print(file);
            storeModelSecurityChanged = false;
            ok = true;
        }

        if (force || storeModelMainChanged) {
            List<NutsPlatformLocation> plainSdks = new ArrayList<>();
            plainSdks.addAll(Arrays.asList(session.env().platforms().findPlatforms()));
            storeModelMain.setPlatforms(plainSdks);
            storeModelMain.setRepositories(new ArrayList<>(
                    Arrays.stream(session.repos().getRepositories()).filter(x -> !x.config().isTemporary())
                            .map(x -> x.config().getRepositoryRef()).collect(Collectors.toList())
            ));

            NutsPath file = configVersionSpecificLocation.resolve(CoreNutsConstants.Files.WORKSPACE_MAIN_CONFIG_FILE_NAME);
            storeModelMain.setConfigVersion(current().getApiVersion());
            if (storeModelMain.getCommandFactories() != null) {
                for (NutsCommandFactoryConfig item : storeModelMain.getCommandFactories()) {
                    //inherited
                    item.setConfigVersion(null);
                }
            }
            if (storeModelMain.getRepositories() != null) {
                for (NutsRepositoryRef item : storeModelMain.getRepositories()) {
                    //inherited
                    item.setConfigVersion(null);
                }
            }
            if (storeModelMain.getPlatforms() != null) {
                for (NutsPlatformLocation item : storeModelMain.getPlatforms()) {
                    //inherited
                    item.setConfigVersion(null);
                }
            }
            elem.setSession(session).json().setValue(storeModelMain)
                    .setNtf(false).print(file);
            storeModelMainChanged = false;
            ok = true;
        }

        if (force || storeModelApiChanged) {
            NutsPath afile = apiVersionSpecificLocation.resolve(NutsConstants.Files.WORKSPACE_API_CONFIG_FILE_NAME);
            storeModelApi.setConfigVersion(current().getApiVersion());
            if (storeModelSecurity.getUsers() != null) {
                for (NutsUserConfig item : storeModelSecurity.getUsers()) {
                    //inherited
                    item.setConfigVersion(null);
                }
            }
            elem.setSession(session).json().setValue(storeModelApi)
                    .setNtf(false).print(afile);
            storeModelApiChanged = false;
            ok = true;
        }
        if (force || storeModelRuntimeChanged) {
            NutsPath runtimeVersionSpecificLocation = session.locations().getStoreLocation(NutsStoreLocation.CONFIG)
                    .resolve(NutsConstants.Folders.ID).resolve(session.locations().getDefaultIdBasedir(session.getWorkspace().getRuntimeId()));
            NutsPath afile = runtimeVersionSpecificLocation.resolve(NutsConstants.Files.WORKSPACE_RUNTIME_CONFIG_FILE_NAME);
            storeModelRuntime.setConfigVersion(current().getApiVersion());
            elem.setSession(session).json().setValue(storeModelRuntime)
                    .setNtf(false).print(afile);
            storeModelRuntimeChanged = false;
            ok = true;
        }
        NutsException error = null;
        for (NutsRepository repo : session.repos().getRepositories()) {
            try {
                if (repo.config() instanceof NutsRepositoryConfigManagerExt) {
                    ok |= ((NutsRepositoryConfigManagerExt) (repo.config())).getModel().save(force, session);
                }
            } catch (NutsException ex) {
                error = ex;
            }
        }
        if (error != null) {
            throw error;
        }

        return ok;
    }

    public boolean save(NutsSession session) {
        return save(true, session);
    }

    public NutsWorkspaceBootConfig loadBootConfig(String _ws, boolean global, boolean followLinks, NutsSession session) {
        String _ws0 = _ws;
        String effWorkspaceName = null;
        String lastConfigPath = null;
        NutsWorkspaceConfigBoot lastConfigLoaded = null;
        boolean defaultLocation = false;
        if (_ws != null && _ws.matches("[a-z-]+://.*")) {
            //this is a protocol based workspace
            //String protocol=ws.substring(0,ws.indexOf("://"));
            effWorkspaceName = "remote-bootstrap";
            lastConfigPath = NutsUtilPlatforms.getWorkspaceLocation(null,
                    global,
                    CoreNutsUtils.resolveValidWorkspaceName(effWorkspaceName));
            lastConfigLoaded = parseBootConfig(NutsPath.of(lastConfigPath, session), session);
            defaultLocation = true;
            return new DefaultNutsWorkspaceBootConfig(session, _ws0, lastConfigPath, effWorkspaceName, defaultLocation, lastConfigLoaded);
        } else if (followLinks) {
            defaultLocation = CoreNutsUtils.isValidWorkspaceName(_ws);
            int maxDepth = 36;
            for (int i = 0; i < maxDepth; i++) {
                lastConfigPath
                        = CoreNutsUtils.isValidWorkspaceName(_ws)
                        ? NutsUtilPlatforms.getWorkspaceLocation(
                        null,
                        global,
                        CoreNutsUtils.resolveValidWorkspaceName(_ws)
                ) : CoreIOUtils.getAbsolutePath(_ws);

                NutsWorkspaceConfigBoot configLoaded = parseBootConfig(NutsPath.of(lastConfigPath, session), session);
                if (configLoaded == null) {
                    //not loaded
                    break;
                }
                if (NutsBlankable.isBlank(configLoaded.getWorkspace())) {
                    lastConfigLoaded = configLoaded;
                    break;
                }
                _ws = configLoaded.getWorkspace();
                if (i >= maxDepth - 1) {
                    throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("cyclic workspace resolution"));
                }
            }
            if (lastConfigLoaded == null) {
                return null;
            }
            effWorkspaceName = CoreNutsUtils.resolveValidWorkspaceName(_ws);
            return new DefaultNutsWorkspaceBootConfig(session, _ws0, lastConfigPath, effWorkspaceName, defaultLocation, lastConfigLoaded);
        } else {
            defaultLocation = CoreNutsUtils.isValidWorkspaceName(_ws);
            lastConfigPath
                    = CoreNutsUtils.isValidWorkspaceName(_ws)
                    ? NutsUtilPlatforms.getWorkspaceLocation(
                    null,
                    global,
                    CoreNutsUtils.resolveValidWorkspaceName(_ws)
            ) : CoreIOUtils.getAbsolutePath(_ws);

            lastConfigLoaded = parseBootConfig(NutsPath.of(lastConfigPath, session), session);
            if (lastConfigLoaded == null) {
                return null;
            }
            effWorkspaceName = CoreNutsUtils.resolveValidWorkspaceName(_ws);
            return new DefaultNutsWorkspaceBootConfig(session, _ws0, lastConfigPath, effWorkspaceName, defaultLocation, lastConfigLoaded);
        }
    }

    public NutsWorkspaceOptionsBuilder optionsBuilder(NutsSession session) {
        return new CoreNutsWorkspaceOptions(session);
    }

    public boolean isExcludedExtension(String extensionId, NutsWorkspaceOptions options, NutsSession session) {
        if (extensionId != null && options != null) {
            NutsId pnid = NutsId.of(extensionId, session);
            String shortName = pnid.getShortName();
            String artifactId = pnid.getArtifactId();
            for (String excludedExtensionList : options.getExcludedExtensions()) {
                for (String s : excludedExtensionList.split("[;, ]")) {
                    if (s.length() > 0) {
                        if (s.equals(shortName) || s.equals(artifactId)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public NutsWorkspaceOptions getOptions(NutsSession session) {
        return new ReadOnlyNutsWorkspaceOptions(options, session);
    }

    public NutsId createContentFaceId(NutsId id, NutsDescriptor desc) {
        Map<String, String> q = id.getProperties();
        q.put(NutsConstants.IdProperties.PACKAGING, NutsUtilStrings.trim(desc.getPackaging()));
//        q.put(NutsConstants.QUERY_EXT,NutsUtilStrings.trim(descriptor.getExt()));
        q.put(NutsConstants.IdProperties.FACE, NutsConstants.QueryFaces.CONTENT);
        return id.builder().setProperties(q).build();
    }

    public NutsWorkspaceListManager createWorkspaceListManager(String name, NutsSession session) {
        return new DefaultNutsWorkspaceListManager(session, name);
    }

    //
//    public void setBootConfig(NutsBootConfig other) {
//        if (other == null) {
//            other = new NutsBootConfig();
//        }
//        if (!NutsBlankable.isBlank(other.getRuntimeId())) {
//            NutsSession searchSession = ws.createSession().silent();
//            other.setRuntimeDependencies(ws.search().setSession(searchSession).addId(other.getRuntimeId())
//                    .scope(NutsDependencyScopePattern.RUN)
//                    .inlineDependencies()
//                    .duplicates(false)
//                    .getResultDefinitions().stream()
//                    .map(x -> x.getId().getLongName())
//                    .collect(Collectors.joining(";"))
//            );
//        } else {
//            other.setRuntimeDependencies("");
//        }
//        config.setApiVersion(other.getApiVersion());
//        config.setRuntimeId(other.getRuntimeId());
//        config.setRuntimeDependencies(other.getRuntimeDependencies());
//        config.setBootRepositories(other.getBootRepositories());
//        fireConfigurationChanged();
//    }
    public boolean isSupportedRepositoryType(String repositoryType, NutsSession session) {
        if (NutsBlankable.isBlank(repositoryType)) {
            repositoryType = NutsConstants.RepoTypes.NUTS;
        }
        return session.extensions().createAllSupported(NutsRepositoryFactoryComponent.class,
                new NutsRepositoryConfig().setType(repositoryType)).size() > 0;
    }

    public NutsAddRepositoryOptions[] getDefaultRepositories(NutsSession session) {
//        session = NutsWorkspaceUtils.of(getWorkspace()).validateSession(session);
        List<NutsAddRepositoryOptions> all = new ArrayList<>();
        for (NutsRepositoryFactoryComponent provider : session.extensions()
                .createAll(NutsRepositoryFactoryComponent.class)) {
            for (NutsAddRepositoryOptions d : provider.getDefaultRepositories(session)) {
                all.add(d);
            }
        }
        Collections.sort(all, new Comparator<NutsAddRepositoryOptions>() {

            public int compare(NutsAddRepositoryOptions o1, NutsAddRepositoryOptions o2) {
                return Integer.compare(o1.getOrder(), o2.getOrder());
            }
        });
        return all.toArray(new NutsAddRepositoryOptions[0]);
    }

    public Set<String> getAvailableArchetypes(NutsSession session) {
        Set<String> set = new HashSet<>();
        set.add("default");
        for (NutsWorkspaceArchetypeComponent extension : session.extensions()
                .createAllSupported(NutsWorkspaceArchetypeComponent.class, null)) {
            set.add(extension.getName());
        }
        return set;
    }

    //
//    public char[] decryptString(char[] input) {
//        if (input == null || input.length == 0) {
//            return new char[0];
//        }
//        return CoreIOUtils.bytesToChars(decryptString(CoreIOUtils.charsToBytes(input)));
//    }
//
//
//    public char[] encryptString(char[] input) {
//        if (input == null || input.length == 0) {
//            return new char[0];
//        }
//        return CoreIOUtils.bytesToChars(encryptString(CoreIOUtils.charsToBytes(input)));
//    }
//
//
//    public byte[] decryptString(byte[] input) {
//        if (input == null || input.length == 0) {
//            return new byte[0];
//        }
//        String passphrase = getEnv(CoreSecurityUtils.ENV_KEY_PASSPHRASE, CoreSecurityUtils.DEFAULT_PASSPHRASE);
//        return CoreSecurityUtils.httpDecrypt(input, passphrase);
//    }
//
//
//    public byte[] encryptString(byte[] input) {
//        if (input == null || input.length == 0) {
//            return new byte[0];
//        }
//        String passphrase = getEnv(CoreSecurityUtils.ENV_KEY_PASSPHRASE, CoreSecurityUtils.DEFAULT_PASSPHRASE);
//        return CoreSecurityUtils.httpEncrypt(input, passphrase);
//    }
    public NutsPath resolveRepositoryPath(NutsPath repositoryLocation, NutsSession session) {
        NutsPath root = this.getRepositoriesRoot(session);
        return repositoryLocation
                .toAbsolute(root != null ? root :
                        session.locations().getStoreLocation(NutsStoreLocation.CONFIG)
                                .resolve(NutsConstants.Folders.REPOSITORIES))
                ;
    }

    //
//    public boolean isGlobal() {
//        return config.isGlobal();
//    }
    public NutsIndexStoreFactory getIndexStoreClientFactory() {
        return indexStoreClientFactory;
    }

    public String getBootRepositories() {
        return current().getBootRepositories();
    }

    public String getJavaCommand() {
        return current().getJavaCommand();
    }

    public String getJavaOptions() {
        return current().getJavaOptions();
    }

    public boolean isGlobal() {
        return current().isGlobal();
    }

    public long getCreationStartTimeMillis() {
        return startCreateTime;
    }

    public long getCreationFinishTimeMillis() {
        return endCreateTime;
    }

    public long getCreationTimeMillis() {
        return endCreateTime - startCreateTime;
    }

    public NutsWorkspaceConfigMain getStoreModelMain() {
        return storeModelMain;
    }

    //    public String getApiVersion() {
//        if (currentConfig == null) {
//            return Nuts.getVersion();
//        }
//        return current().getApiVersion();
//    }
//
//    public NutsId getApiId() {
//        if (currentConfig == null) {
//            NutsSession session=NutsWorkspaceUtils.defaultSession(ws);
//            return session.id().parser().parseList(NutsConstants.Ids.NUTS_API + "#" + Nuts.getVersion());
//        }
//        return current().getApiId();
//    }
//
//    public NutsId getRuntimeId() {
//        return current().getRuntimeId();
//    }
    public DefaultNutsWorkspaceCurrentConfig current() {
        if (currentConfig == null) {
            throw new IllegalStateException("unable to use workspace.current(). Still in initialize status");
        }
        return currentConfig;
    }

    public void setStartCreateTimeMillis(long startCreateTime) {
        this.startCreateTime = startCreateTime;
    }

    public void setConfigBoot(NutsWorkspaceConfigBoot config, NutsSession options) {
        setConfigBoot(config, options, true);
    }

    public void setConfigApi(NutsWorkspaceConfigApi config, NutsSession session) {
        setConfigApi(config, session, true);
    }

    public void setConfigRuntime(NutsWorkspaceConfigRuntime config, NutsSession options) {
        setConfigRuntime(config, options, true);
    }

    public void setConfigSecurity(NutsWorkspaceConfigSecurity config, NutsSession session) {
        setConfigSecurity(config, session, true);
    }

    public void setConfigMain(NutsWorkspaceConfigMain config, NutsSession session) {
        setConfigMain(config, session, true);
    }

    public void setEndCreateTimeMillis(long endCreateTime) {
        this.endCreateTime = endCreateTime;
    }

    public void prepareBootApi(NutsId apiId, NutsId runtimeId, boolean force, NutsSession session) {
        if (apiId == null) {
            throw new NutsNotFoundException(session, apiId);
        }
        NutsPath apiConfigFile = session.locations().getStoreLocation(apiId, NutsStoreLocation.CONFIG)
                .resolve(NutsConstants.Files.WORKSPACE_API_CONFIG_FILE_NAME);
        if (force || !apiConfigFile.isRegularFile()) {
            Map<String, Object> m = new LinkedHashMap<>();
            if (runtimeId == null) {
                runtimeId = session.search().addId(NutsConstants.Ids.NUTS_RUNTIME)
                        .setRuntime(true)
                        .setTargetApiVersion(apiId.getVersion())
                        .setFailFast(false).setLatest(true).getResultIds().first();
            }
            if (runtimeId == null) {
                runtimeId = MavenUtils.of(session).resolveLatestMavenId(NutsId.of(NutsConstants.Ids.NUTS_RUNTIME, session),
                        (rtVersion) -> rtVersion.startsWith(apiId.getVersion().getValue() + "."),
                        session);
            }
            if (runtimeId == null) {
                throw new NutsNotFoundException(session, runtimeId);
            }
            m.put("configVersion", apiId.getVersion().getValue());
            m.put("apiVersion", apiId.getVersion().getValue());
            m.put("runtimeId", runtimeId.getLongName());
            String javaCommand = getStoredConfigApi().getJavaCommand();
            String javaOptions = getStoredConfigApi().getJavaOptions();
            m.put("javaCommand", javaCommand);
            m.put("javaOptions", javaOptions);
            NutsElements.of(session).json().setValue(m)
                    .setNtf(false).print(apiConfigFile);
        }
        //downloadId(apiId, force, null, true, NutsIdType.API, session);
    }

    public void prepareBootRuntime(NutsId id, boolean force, NutsSession session) {
        prepareBootRuntimeOrExtension(id, force, NutsIdType.RUNTIME, session);
    }

    public void prepareBootExtension(NutsId id, boolean force, NutsSession session) {
        prepareBootRuntimeOrExtension(id, force, NutsIdType.EXTENSION, session);
    }

    public void installBootIds(NutsSession session) {
        downloadId(ws.getApiId(), session);
        downloadId(ws.getRuntimeId(), session);

//        prepareBootApi(ws.getApiId(), ws.getRuntimeId(), force, session);
//        prepareBootRuntime(ws.getRuntimeId(), force, session);
        List<NutsWorkspaceConfigBoot.ExtensionConfig> extensions = getStoredConfigBoot().getExtensions();
        if (extensions != null) {
            for (NutsWorkspaceConfigBoot.ExtensionConfig extension : extensions) {
                if (extension.isEnabled()) {
                    //prepareBootExtension(extension.getId(), force, session);
                    downloadId(extension.getId(), session);
                }
            }
        }
    }

    public boolean isConfigurationChanged() {
        return storeModelBootChanged || storeModelApiChanged || storeModelRuntimeChanged || storeModelSecurityChanged || storeModelMainChanged;
    }

    public boolean loadWorkspace(NutsSession session) {
        try {
            NutsWorkspaceUtils.checkSession(ws, session);
            NutsWorkspaceConfigBoot _config = parseBootConfig(session);
            if (_config == null) {
                return false;
            }
            DefaultNutsWorkspaceCurrentConfig cconfig = new DefaultNutsWorkspaceCurrentConfig(ws).merge(_config, session);
            if (cconfig.getApiId() == null) {
                cconfig.setApiId(NutsId.of(NutsConstants.Ids.NUTS_API + "#" + initOptions.getApiVersion(), session));
            }
            if (cconfig.getRuntimeId() == null) {
                cconfig.setRuntimeId(initOptions.getRuntimeId() == null ? null : initOptions.getRuntimeId().toString(), session);
            }
            if (cconfig.getRuntimeBootDescriptor() == null) {
                cconfig.setRuntimeBootDescriptor(initOptions.getRuntimeBootDescriptor());
            }
            if (cconfig.getExtensionBootDescriptors() == null) {
                cconfig.setExtensionBootDescriptors(initOptions.getExtensionBootDescriptors());
            }
            if (cconfig.getBootRepositories() == null) {
                cconfig.setBootRepositories(initOptions.getBootRepositories());
            }
            cconfig.merge(getOptions(session), session);

            setCurrentConfig(cconfig.build(session.locations().getWorkspaceLocation(), session));

            NutsVersionCompat compat = createNutsVersionCompat(Nuts.getVersion(), session);
            NutsWorkspaceConfigApi aconfig = compat.parseApiConfig(session);
            if (aconfig != null) {
                cconfig.merge(aconfig, session);
            }
            NutsWorkspaceConfigRuntime rconfig = compat.parseRuntimeConfig(session);
            if (rconfig != null) {
                cconfig.merge(rconfig, session);
            }
            NutsWorkspaceConfigSecurity sconfig = compat.parseSecurityConfig(session);
            NutsWorkspaceConfigMain mconfig = compat.parseMainConfig(session);
            if (options.isRecover() || options.isReset()) {
                //always reload boot resolved versions!
                cconfig.setApiId(NutsId.of(NutsConstants.Ids.NUTS_API + "#" + initOptions.getApiVersion(), session));
                cconfig.setRuntimeId(initOptions.getRuntimeId() == null ? null : initOptions.getRuntimeId().toString(), session);
                cconfig.setRuntimeBootDescriptor(initOptions.getRuntimeBootDescriptor());
                cconfig.setExtensionBootDescriptors(initOptions.getExtensionBootDescriptors());
                cconfig.setBootRepositories(initOptions.getBootRepositories());
            }
            setCurrentConfig(cconfig
                    .build(session.locations().getWorkspaceLocation(), session)
            );
            setConfigBoot(_config, session, false);
            setConfigApi(aconfig, session, false);
            setConfigRuntime(rconfig, session, false);
            setConfigSecurity(sconfig, session, false);
            setConfigMain(mconfig, session, false);
            storeModelBootChanged = false;
            storeModelApiChanged = false;
            storeModelRuntimeChanged = false;
            storeModelSecurityChanged = false;
            storeModelMainChanged = false;
            return true;
        } catch (RuntimeException ex) {
            if (session.boot().getBootOptions().isRecover()) {
                onLoadWorkspaceError(ex, session);
            } else {
                throw ex;
            }
        }
        return false;
    }

    public void setBootApiVersion(String value, NutsSession session) {
        if (!Objects.equals(value, storeModelApi.getApiVersion())) {
//            options = CoreNutsUtils.validate(options, ws);
            storeModelApi.setApiVersion(value);
            fireConfigurationChanged("api-version", session, ConfigEventType.API);
        }
    }

    public void setBootRuntimeId(String value, NutsSession session) {
        if (!Objects.equals(value, storeModelApi.getRuntimeId())) {
//            options = CoreNutsUtils.validate(options, ws);
            storeModelApi.setRuntimeId(value);
            fireConfigurationChanged("runtime-id", session, ConfigEventType.API);
        }
    }

    public void setBootRuntimeDependencies(String value, NutsSession session) {
        if (!Objects.equals(value, storeModelRuntime.getDependencies())) {
//            options = CoreNutsUtils.validate(options, ws);
            storeModelRuntime.setDependencies(value);
            setConfigRuntime(storeModelRuntime, session, true);
        }
    }

    public void setBootRepositories(String value, NutsSession session) {
        if (!Objects.equals(value, storeModelBoot.getBootRepositories())) {
//            options = CoreNutsUtils.validate(options, ws);
            storeModelBoot.setBootRepositories(value);
            fireConfigurationChanged("boot-repositories", session, ConfigEventType.API);
        }
    }

    public NutsUserConfig getUser(String userId, NutsSession session) {
        NutsWorkspaceUtils.checkSession(ws, session);
        NutsUserConfig _config = getSecurity(userId);
        if (_config == null) {
            if (NutsConstants.Users.ADMIN.equals(userId) || NutsConstants.Users.ANONYMOUS.equals(userId)) {
                _config = new NutsUserConfig(userId, null, null, null);
                setUser(_config, session);
            }
        }
        return _config;
    }

    public NutsUserConfig[] getUsers(NutsSession session) {
//        session = NutsWorkspaceUtils.of(getWorkspace()).validateSession(session);
        return configUsers.values().toArray(new NutsUserConfig[0]);
    }

    public void setUser(NutsUserConfig config, NutsSession session) {
        if (config != null) {
            configUsers.put(config.getUser(), config);
            fireConfigurationChanged("user", session, ConfigEventType.SECURITY);
        }
    }

    public void removeUser(String userId, NutsSession session) {
        NutsUserConfig old = getSecurity(userId);
        if (old != null) {
            configUsers.remove(userId);
            fireConfigurationChanged("users", session, ConfigEventType.SECURITY);
        }
    }

    public void setSecure(boolean secure, NutsSession session) {
        if (secure != storeModelSecurity.isSecure()) {
            storeModelSecurity.setSecure(secure);
            fireConfigurationChanged("secure", session, ConfigEventType.SECURITY);
        }
    }

    public void fireConfigurationChanged(String configName, NutsSession session, ConfigEventType t) {
//        session = NutsWorkspaceUtils.of(ws).validateSession(session);
        ((DefaultImportManager) session.imports()).getModel().invalidateCache();
        switch (t) {
            case API: {
                storeModelApiChanged = true;
                break;
            }
            case RUNTIME: {
                storeModelRuntimeChanged = true;
                break;
            }
            case SECURITY: {
                storeModelSecurityChanged = true;
                break;
            }
            case MAIN: {
                storeModelMainChanged = true;
                break;
            }
            case BOOT: {
                storeModelBootChanged = true;
                break;
            }
        }
        DefaultNutsWorkspaceEvent evt = new DefaultNutsWorkspaceEvent(session, null, "config." + configName, null, true);
        for (NutsWorkspaceListener workspaceListener : session.events().getWorkspaceListeners()) {
            workspaceListener.onConfigurationChanged(evt);
        }
    }

    //    
    public NutsWorkspaceConfigApi getStoredConfigApi() {
        if (storeModelApi.getApiVersion() == null) {
            storeModelApi.setApiVersion(Nuts.getVersion());
        }
        return storeModelApi;
    }

    public NutsWorkspaceConfigBoot getStoredConfigBoot() {
        return storeModelBoot;
    }

    public NutsWorkspaceConfigSecurity getStoredConfigSecurity() {
        return storeModelSecurity;
    }

    public NutsWorkspaceConfigMain getStoredConfigMain() {
        return storeModelMain;
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }

    public NutsDependencySolver createDependencySolver(String name, NutsSession session) {
        NutsDependencySolverFactory c = getSolversMap(session).get(NutsDependencySolverUtils.resolveSolverName(name));
        if (c != null) {
            return c.create(session);
        }
        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("dependency solver not found %s", name));
    }

    private Map<String, NutsDependencySolverFactory> getSolversMap(NutsSession session) {
        if (dependencySolvers == null) {
            dependencySolvers = new LinkedHashMap<>();
            for (NutsDependencySolverFactory nutsDependencySolver : session.extensions().createAllSupported(NutsDependencySolverFactory.class, null)) {
                dependencySolvers.put(nutsDependencySolver.getName(), nutsDependencySolver);
            }
        }
        return dependencySolvers;
    }

    public NutsDependencySolverFactory[] getDependencySolvers(NutsSession session) {
        return getSolversMap(session).values().toArray(new NutsDependencySolverFactory[0]);
    }


    public NutsPath getRepositoriesRoot(NutsSession session) {
        return session.locations().getStoreLocation(NutsStoreLocation.CONFIG).resolve(NutsConstants.Folders.REPOSITORIES);
    }

    public NutsPath getTempRepositoriesRoot(NutsSession session) {
        return session.locations().getStoreLocation(NutsStoreLocation.TEMP).resolve(NutsConstants.Folders.REPOSITORIES);
    }

    public boolean isValidWorkspaceFolder(NutsSession session) {
        Path file = session.locations().getWorkspaceLocation().toFile().resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        return Files.isRegularFile(file);
    }

    public NutsAuthenticationAgent createAuthenticationAgent(String authenticationAgent, NutsSession session) {
        authenticationAgent = NutsUtilStrings.trim(authenticationAgent);
        NutsAuthenticationAgent supported = null;
        if (authenticationAgent.isEmpty()) {
            supported = session.extensions().createSupported(NutsAuthenticationAgent.class, true, "");
        } else {
            List<NutsAuthenticationAgent> agents = session.extensions().createAllSupported(NutsAuthenticationAgent.class, authenticationAgent);
            for (NutsAuthenticationAgent agent : agents) {
                if (agent.getId().equals(authenticationAgent)) {
                    supported = agent;
                }
            }
        }
        if (supported == null) {
            throw new NutsExtensionNotFoundException(session, NutsAuthenticationAgent.class, authenticationAgent);
        }
        NutsWorkspaceUtils.setSession(supported, session);
        return supported;
    }

    //
//    public void setExcludedRepositories(String[] excludedRepositories, NutsUpdateOptions options) {
//        excludedRepositoriesSet = excludedRepositories == null ? null : new HashSet<>(CoreStringUtils.split(Arrays.asList(excludedRepositories), " ,;"));
//    }
    public void setUsers(NutsUserConfig[] users, NutsSession session) {
        for (NutsUserConfig u : getUsers(session)) {
            removeUser(u.getUser(), session);
        }
        for (NutsUserConfig conf : users) {
            setUser(conf, session);
        }
    }

    public NutsWorkspaceConfigRuntime getStoredConfigRuntime() {
        return storeModelRuntime;
    }

    public NutsId createSdkId(String type, String version, NutsSession session) {
        return NutsWorkspaceUtils.of(session).createSdkId(type, version);
    }

    public void onExtensionsPrepared(NutsSession session) {
        try {
            indexStoreClientFactory = session.extensions().createSupported(NutsIndexStoreFactory.class, false, null);
        } catch (Exception ex) {
            //
        }
        if (indexStoreClientFactory == null) {
            indexStoreClientFactory = new DummyNutsIndexStoreFactory();
        }
    }

    public void setConfigApi(NutsWorkspaceConfigApi config, NutsSession session, boolean fire) {
        this.storeModelApi = config == null ? new NutsWorkspaceConfigApi() : config;
        if (fire) {
            fireConfigurationChanged("boot-api-config", session, ConfigEventType.API);
        }
    }

    public void setConfigRuntime(NutsWorkspaceConfigRuntime config, NutsSession session, boolean fire) {
        this.storeModelRuntime = config == null ? new NutsWorkspaceConfigRuntime() : config;
        if (fire) {
            fireConfigurationChanged("boot-runtime-config", session, ConfigEventType.RUNTIME);
        }
    }

    private void setConfigSecurity(NutsWorkspaceConfigSecurity config, NutsSession session, boolean fire) {
        this.storeModelSecurity = config == null ? new NutsWorkspaceConfigSecurity() : config;
        configUsers.clear();
        if (this.storeModelSecurity.getUsers() != null) {
            for (NutsUserConfig s : this.storeModelSecurity.getUsers()) {
                configUsers.put(s.getUser(), s);
            }
        }
        storeModelSecurityChanged = true;
        if (fire) {
            fireConfigurationChanged("config-security", session, ConfigEventType.SECURITY);
        }
    }

    private void setConfigMain(NutsWorkspaceConfigMain config, NutsSession session, boolean fire) {
        this.storeModelMain = config == null ? new NutsWorkspaceConfigMain() : config;
        DefaultNutsPlatformManager d = (DefaultNutsPlatformManager) session.env().platforms();
        d.getModel().setPlatforms(this.storeModelMain.getPlatforms().toArray(new NutsPlatformLocation[0]), session);
        NutsRepositoryManager repos = session.repos();
        repos.removeAllRepositories();
        if (this.storeModelMain.getRepositories() != null) {
            for (NutsRepositoryRef ref : this.storeModelMain.getRepositories()) {
                repos
                        .addRepository(
                                CoreNutsUtils.refToOptions(ref)
                        );
            }
        }

        storeModelMainChanged = true;
        if (fire) {
            fireConfigurationChanged("config-main", session, ConfigEventType.MAIN);
        }
    }

    private void setConfigBoot(NutsWorkspaceConfigBoot config, NutsSession session, boolean fire) {
        this.storeModelBoot = config;
        if (NutsBlankable.isBlank(config.getUuid())) {
            config.setUuid(UUID.randomUUID().toString());
            fire = true;
        }
        if (fire) {
            fireConfigurationChanged("config-master", session, ConfigEventType.BOOT);
        }
    }

    public String getBootClassWorldString(NutsSession session) {
        StringBuilder sb = new StringBuilder();
        for (URL bootClassWorldURL : getBootClassWorldURLs()) {
            if (sb.length() > 0) {
                sb.append(File.pathSeparator);
            }
            if (CoreIOUtils.isPathFile(bootClassWorldURL.toString())) {
                File f = CoreIOUtils.toPathFile(bootClassWorldURL.toString(), session).toFile();
                sb.append(f.getPath());
            } else {
                sb.append(bootClassWorldURL.toString().replace(":", "\\:"));
            }
        }
        return sb.toString();
    }

    //    
//    public Path getBootNutsJar() {
//        try {
//            NutsId baseId = ws.id().parseRequired(NutsConstants.Ids.NUTS_API);
//            String urlPath = "META-INF/maven/" + baseId.getGroup() + "/" + baseId.getName() + "/pom.properties";
//            URL resource = Nuts.class.getResource(urlPath);
//            if (resource != null) {
//                URL runtimeURL = CoreIOUtils.resolveURLFromResource(Nuts.class, urlPath);
//                return CoreIOUtils.resolveLocalPathFromURL(runtimeURL);
//            }
//        } catch (Exception e) {
//            //e.printStackTrace();
//        }
//        // This will happen when running app from  nuts dev project so that classes folder is considered as
//        // binary class path instead of a single jar file.
//        // In that case we will gather nuts from maven .m2 repository
//        PomId m = PomIdResolver.resolvePomId(Nuts.class, null);
//        if (m != null) {
//            Path f = ws.io().path(System.getProperty("user.home"), ".m2", "repository", m.getGroupId().replace('.', '/'), m.getArtifactId(), m.getVersion(),
//                    ws.locations().getDefaultIdFilename(
//                            ws.elements().setGroup(m.getGroupId()).setName(m.getArtifactId()).setVersion(m.getVersion())
//                                    .setFaceComponent()
//                                    .setPackaging("jar")
//                                    .build()
//                    ));
//            if (Files.exists(f)) {
//                return f;
//            }
//        }
//        return null;
//    }
    public String toString() {
        String s1 = "NULL";
        String s2 = "NULL";
        s1 = ws.getApiId().toString();
        s2 = String.valueOf(ws.getRuntimeId());
        return "NutsWorkspaceConfig{"
                + "workspaceBootId=" + s1
                + ", workspaceRuntimeId=" + s2
                + ", workspace=" + ((currentConfig == null) ? "NULL" : ("'"
                + ((DefaultNutsWorkspaceLocationManager) (NutsWorkspaceUtils.defaultSession(ws))
                .locations()).getModel().getWorkspaceLocation() + '\''))
                + '}';
    }

    public void prepareBootRuntimeOrExtension(NutsId id, boolean force, NutsIdType idType, NutsSession session) {
        NutsWorkspaceUtils.checkSession(ws, session);
        NutsPath configFile = session.locations().getStoreLocation(NutsStoreLocation.CACHE)
                .resolve(NutsConstants.Folders.ID).resolve(session.locations().getDefaultIdBasedir(id)).resolve(
                        idType == NutsIdType.RUNTIME
                                ? NutsConstants.Files.WORKSPACE_RUNTIME_CACHE_FILE_NAME
                                : NutsConstants.Files.WORKSPACE_EXTENSION_CACHE_FILE_NAME
                );
        NutsPath jarFile = session.locations().getStoreLocation(NutsStoreLocation.LIB)
                .resolve(NutsConstants.Folders.ID).resolve(session.locations().getDefaultIdBasedir(id))
                .resolve(session.locations().getDefaultIdFilename(id.builder().setFaceContent().setPackaging("jar").build()));
        if (!force && (configFile.isRegularFile() && jarFile.isRegularFile())) {
            return;
        }
        List<NutsId> deps = new ArrayList<>();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id.getLongName());

        NutsDefinition def = session.fetch().setId(id).setDependencies(true)
                //
                .setOptional(false)
                .addScope(NutsDependencyScopePattern.RUN)
                .setDependencyFilter(CoreNutsDependencyUtils.createJavaRunDependencyFilter(session))
                //
                .setContent(true)
                .setFailFast(false)
                .setSession(session)
                .getResultDefinition();
        if (def == null) {
            _LOGOP(session).level(Level.CONFIG)
                    .verb(NutsLogVerb.WARNING)
                    .log(NutsMessage.jstyle("selected repositories ({0}) cannot reach runtime package. fallback to default.",
                            Arrays.stream(session.repos().setSession(session).getRepositories()).map(NutsRepository::getName).collect(Collectors.joining(", "))
                    ));
            if (isFirstBoot()) {
                MavenUtils.DepsAndRepos dd = MavenUtils.of(session).loadDependenciesAndRepositoriesFromPomPath(id,
                        resolveBootRepositoriesBootSelectionArray(session),
                        session);
                if (dd == null) {
                    throw new NutsNotFoundException(session, id);
                }
                m.put("dependencies", String.join(";", dd.deps));
                if (idType == NutsIdType.RUNTIME) {
                    m.put("bootRepositories", String.join(";", dd.repos));
                }
                for (String dep : dd.deps) {
                    deps.add(NutsId.of(dep, session));
                }
            } else {
                throw new NutsNotFoundException(session, id);
            }
        } else {
            for (NutsDependency dep : def.getDependencies()) {
                deps.add(dep.toId());
            }
            m.put("dependencies",
                    def.getDependencies().stream().map(x -> x.toId().getLongName()).collect(Collectors.joining(";"))
            );
            if (idType == NutsIdType.RUNTIME) {
                m.put("bootRepositories", def.getDescriptor().getPropertyValue("nuts-runtime-repositories"));
            }
        }

        if (force || !configFile.isRegularFile()) {
            NutsElements.of(session).json().setValue(m)
                    .setNtf(false).print(configFile);
        }
//        downloadId(id, force, (def != null && def.getContent().getPath() != null) ? def.getContent().getFilePath() : null, false, runtime,session);
//        for (NutsId dep : deps) {
//            downloadId(dep, force, null, true, NutsIdType.REGULAR, session);
//        }
    }

    private boolean isFirstBoot() {
        return ws.boot().isFirstBoot();
    }

    private void downloadId(NutsId id, NutsSession session) {
        NutsWorkspaceUtils.checkSession(ws, session);
        List<NutsId> nutsIds = session.search().setInstallStatus(NutsInstallStatusFilters.of(session).byDeployed(true))
                .setId(id).setLatest(true).getResultIds().toList();
        if (!nutsIds.isEmpty()) {
            return;
        }
        nutsIds = session.search().setInstallStatus(NutsInstallStatusFilters.of(session).byDeployed(false))
                .setId(id).setLatest(true).getResultIds().toList();
        if (!nutsIds.isEmpty()) {
            session.install().setId(nutsIds.get(0)).run();
            return;
        }
        // this happens when you are creating a workspace with archetype=minimal
        // and hence there is no repository to look from.
        // In that case we look in default repos on "first boot"
        if (isFirstBoot()) {
            boolean deployFromDefaultRepos = false;
            if (deployFromDefaultRepos) {
                String tmp = null;
                try {
                    String idFileName = session.locations().getDefaultIdFilename(id.builder().setFaceContent().setPackaging("jar").build());
                    for (NutsRepositorySelector.Selection pp0 : resolveBootRepositoriesBootSelectionArray(session)) {
                        NutsAddRepositoryOptions opt = NutsRepositorySelector.createRepositoryOptions(pp0, false, session);
                        NutsPath pp = NutsPath.of(opt.getConfig() == null ? opt.getLocation() : opt.getConfig().getLocation(), session);
                        boolean copiedLocally = false;
                        try {
                            if (tmp == null) {
                                tmp = NutsTmp.of(session).createTempFile(idFileName).toString();
                            }
                            NutsPath srcPath = pp
                                    .resolve(session.locations().getDefaultIdBasedir(id))
                                    .resolve(idFileName);
                            if (srcPath.exists()) {
                                NutsCp.of(session).from(srcPath).to(tmp).run();
                                copiedLocally = true;
                            }
                        } catch (Exception ex) {
                            //
                        }
                        if (copiedLocally) {
                            if (deployToInstalledRepository(Paths.get(tmp), session)) {
                                return;
                            }
                        }
                    }
                } finally {
                    if (tmp != null) {
                        NutsPath tp = NutsPath.of(tmp, session);
                        if (tp.exists()) {
                            tp.delete();
                        }
                    }
                }
            }
            NutsClassLoaderNode n = searchBootNode(id, session);
            if (n != null) {
                if (deployToInstalledRepository(
                        NutsPath.of(n.getURL(), session).toFile()
                        , session)) {
                    for (NutsClassLoaderNode d : n.getDependencies()) {
                        NutsId depId = NutsId.of(d.getId(), session);
                        downloadId(depId, session);
                    }
                    return;
                }
            }
        }
        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unable to load %s", id));
    }

    private boolean deployToInstalledRepository(Path tmp, NutsSession session) {
        NutsInstalledRepository ins = NutsWorkspaceExt.of(session.getWorkspace()).getInstalledRepository();
        NutsDescriptor descriptor = CoreIOUtils.resolveNutsDescriptorFromFileContent(tmp, null, session);
        if (descriptor != null) {
            DefaultNutsDefinition b = new DefaultNutsDefinition(
                    null, null,
                    descriptor.getId(),
                    descriptor, new NutsDefaultContent(NutsPath.of(tmp, session), true, true),
                    new DefaultNutsInstallInfo(descriptor.getId(), NutsInstallStatus.NONE, null, null, null, null, null, null, false, false),
                    null, session
            );
            ins.install(b, session);
            return true;
        }
        return false;
    }

    private NutsClassLoaderNode searchBootNode(NutsId id, NutsSession session) {
        NutsBootManager boot = session.boot();
        List<NutsClassLoaderNode> all = new ArrayList();
        all.add(boot.getBootRuntimeClassLoaderNode());
        all.addAll(Arrays.asList(boot.getBootExtensionClassLoaderNode()));
        return searchBootNode(id, all.toArray(new NutsClassLoaderNode[0]));
    }

    private NutsClassLoaderNode searchBootNode(NutsId id, NutsClassLoaderNode[] into) {
        for (NutsClassLoaderNode n : into) {
            if (n != null) {
                if (id.getLongName().equals(n.getId())) {
                    return n;
                }
            }
            NutsClassLoaderNode a = searchBootNode(id, n.getDependencies());
            if (a != null) {
                return a;
            }
        }
        return null;
    }

    public void onPreUpdateConfig(String confName, NutsSession session) {
//        options = CoreNutsUtils.validate(options, ws);
        preUpdateConfigStoreLocations = new NutsStoreLocationsMap(currentConfig.getStoreLocations());
    }

    public void onPostUpdateConfig(String confName, NutsSession session) {
//        options = CoreNutsUtils.validate(options, ws);
        preUpdateConfigStoreLocations = new NutsStoreLocationsMap(currentConfig.getStoreLocations());
        DefaultNutsWorkspaceCurrentConfig d = currentConfig;
        d.setUserStoreLocations(new NutsStoreLocationsMap(storeModelBoot.getStoreLocations()).toMapOrNull());
        d.setHomeLocations(new NutsHomeLocationsMap(storeModelBoot.getHomeLocations()).toMapOrNull());
        d.build(session.locations().getWorkspaceLocation(), session);
        NutsStoreLocationsMap newSL = new NutsStoreLocationsMap(currentConfig.getStoreLocations());
        for (NutsStoreLocation sl : NutsStoreLocation.values()) {
            String oldPath = preUpdateConfigStoreLocations.get(sl);
            String newPath = newSL.get(sl);
            if (!oldPath.equals(newPath)) {
                Path oldPathObj = Paths.get(oldPath);
                if (Files.exists(oldPathObj)) {
                    CoreIOUtils.copyFolder(oldPathObj, Paths.get(newPath),session);
                }
            }
        }
        fireConfigurationChanged(confName, session, ConfigEventType.API);
    }

    private void onLoadWorkspaceError(Throwable ex, NutsSession session) {
        DefaultNutsWorkspaceConfigModel wconfig = this;
        Path file = session.locations().getWorkspaceLocation().toFile().resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        if (wconfig.isReadOnly()) {
            throw new NutsIOException(session, NutsMessage.cstyle("unable to load config file %s", file), ex);
        }
        String fileSuffix = Instant.now().toString();
        fileSuffix = fileSuffix.replace(':', '-');
        String fileName = "nuts-workspace-" + fileSuffix;
        NutsPath logError = session.locations().getStoreLocation(ws.getApiId(), NutsStoreLocation.LOG).resolve("invalid-config");
        NutsPath logFile = logError.resolve(fileName + ".error");
        _LOGOP(session).level(Level.SEVERE).verb(NutsLogVerb.FAIL)
                .log(NutsMessage.jstyle("erroneous workspace config file. Unable to load file {0} : {1}", file, ex));

        try {
            logFile.mkParentDirs();
        } catch (Exception ex1) {
            throw new NutsIOException(session, NutsMessage.cstyle("unable to log workspace error while loading config file %s : %s", file, ex1), ex);
        }
        NutsPath newfile = logError.resolve(fileName + ".json");
        _LOGOP(session).level(Level.SEVERE).verb(NutsLogVerb.FAIL)
                .log(NutsMessage.jstyle("erroneous workspace config file will be replaced by a fresh one. Old config is copied to {0}\n error logged to  {1}", newfile.toString(), logFile));
        try {
            Files.move(file, newfile.toFile());
        } catch (IOException e) {
            throw new NutsIOException(session, NutsMessage.cstyle("unable to load and re-create config file %s : %s", file, e), ex);
        }

        try (PrintStream o = new PrintStream(logFile.getOutputStream())) {
            o.println("workspace.path:");
            o.println(session.locations().getWorkspaceLocation());
            o.println("workspace.options:");
            o.println(wconfig.getOptions(session).formatter().setCompact(false).setRuntime(true).setInit(true).setExported(true).getBootCommandLine());
            for (NutsStoreLocation location : NutsStoreLocation.values()) {
                o.println("location." + location.id() + ":");
                o.println(session.locations().getStoreLocation(location));
            }
            o.println("java.class.path:");
            o.println(System.getProperty("java.class.path"));
            o.println();
            ex.printStackTrace(o);
        } catch (Exception ex2) {
            //ignore
        }
    }

    public NutsUserConfig getSecurity(String id) {
        return configUsers.get(id);
    }

    private NutsWorkspaceConfigBoot parseBootConfig(NutsSession session) {
        return parseBootConfig(session.locations().getWorkspaceLocation(), session);
    }

    private NutsWorkspaceConfigBoot parseBootConfig(NutsPath path, NutsSession session) {
        Path file = path.toFile().resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(file, session);
        if (bytes == null) {
            return null;
        }
        try {
            Map<String, Object> a_config0 = NutsElements.of(session).json().parse(bytes, Map.class);
            String version = (String) a_config0.get("configVersion");
            if (version == null) {
                version = (String) a_config0.get("createApiVersion");
                if (version == null) {
                    version = Nuts.getVersion();
                }
            }
            return createNutsVersionCompat(version, session).parseConfig(bytes, session);
        } catch (Exception ex) {
            _LOGOP(session).level(Level.SEVERE).verb(NutsLogVerb.FAIL)
                    .log(NutsMessage.jstyle("erroneous workspace config file. Unable to load file {0} : {1}",
                            file, ex));
            throw new NutsIOException(session, NutsMessage.cstyle("unable to load config file %s", file), ex);
        }
    }

    private NutsVersionCompat createNutsVersionCompat(String apiVersion, NutsSession session) {
        int buildNumber = CoreNutsUtils.getApiVersionOrdinalNumber(apiVersion);
        if (buildNumber >= 803) {
            return new NutsVersionCompat803(session, apiVersion);
        } else if (buildNumber >= 507) {
            return new NutsVersionCompat507(session, apiVersion);
        } else if (buildNumber >= 506) {
            return new NutsVersionCompat506(session, apiVersion);
        } else {
            return new NutsVersionCompat502(session, apiVersion);
        }
    }

    public NutsRepositorySelector.Selection[] resolveBootRepositoriesBootSelectionArray(NutsSession session) {
        HashMap<String, String> defaults = new HashMap<>();
        DefaultNutsWorkspaceConfigManager rm = (DefaultNutsWorkspaceConfigManager) session.config();
        for (NutsAddRepositoryOptions d : rm.getDefaultRepositories()) {
            defaults.put(d.getName(), null);
        }
        return resolveBootRepositoriesList().resolveSelectors(defaults);
    }

    public NutsRepositorySelector.SelectorList resolveBootRepositoriesList() {
        if (parsedBootRepositoriesList != null) {
            return parsedBootRepositoriesList;
        }
        parsedBootRepositoriesList = NutsRepositorySelector.parse(options.getRepositories());
        return parsedBootRepositoriesList;
    }

    public NutsWorkspaceConfigBoot getStoreModelBoot() {
        return storeModelBoot;
    }

    public NutsWorkspaceConfigApi getStoreModelApi() {
        return storeModelApi;
    }

    public NutsWorkspaceConfigRuntime getStoreModelRuntime() {
        return storeModelRuntime;
    }

    public NutsWorkspaceConfigSecurity getStoreModelSecurity() {
        return storeModelSecurity;
    }

    public ExecutorService executorService(NutsSession session) {
        if (executorService == null) {
            synchronized (this) {
                if (executorService == null) {
                    executorService = session.boot().getBootOptions().getExecutorService();
                    if (executorService == null) {

                        int minPoolSize = getConfigProperty("nuts.threads.min").getInt(2);
                        if(minPoolSize<1) {
                            minPoolSize = 60;
                        }else if(minPoolSize>500){
                            minPoolSize=500;
                        }
                        int maxPoolSize = getConfigProperty("nuts.threads.max").getInt(60);
                        if(maxPoolSize<1) {
                            maxPoolSize = 60;
                        }else if(maxPoolSize>500){
                            maxPoolSize=500;
                        }
                        if(minPoolSize>maxPoolSize){
                            minPoolSize=maxPoolSize;
                        }
                        TimePeriod defaultPeriod = new TimePeriod(3, TimeUnit.SECONDS);
                        TimePeriod period=TimePeriod.parseLenient(
                                getConfigProperty("nuts.threads.keep-alive").getString(),
                                TimeUnit.SECONDS, defaultPeriod,
                                defaultPeriod
                        );
                        if(period.getCount()<0){
                            period=defaultPeriod;
                        }
                        ThreadPoolExecutor executorService2 = (ThreadPoolExecutor) Executors.newCachedThreadPool(CoreNutsUtils.nutsDefaultThreadFactory);
                        executorService2.setCorePoolSize(minPoolSize);
                        executorService2.setKeepAliveTime(period.getCount(), period.getUnit());
                        executorService2.setMaximumPoolSize(maxPoolSize);
                        executorService = executorService2;
                    }
                }
            }
        }
        return executorService;
    }

    public NutsSystemTerminal createSystemTerminal(NutsTerminalSpec spec, NutsSession session) {
        NutsSystemTerminalBase termb = session.extensions()
                .setSession(session)
                .createSupported(NutsSystemTerminalBase.class, true, spec);
        return NutsSystemTerminal_of_NutsSystemTerminalBase(termb, session);
    }

    public void enableRichTerm(NutsSession session) {
        NutsSystemTerminal st = getSystemTerminal();
        if (st.isAutoCompleteSupported()) {
            //that's ok
        } else {
            NutsId extId = NutsId.of("net.thevpc.nuts.ext:next-term#" + session.getWorkspace().getApiVersion(), session);
            if (!session.config().isExcludedExtension(extId.toString(), session.boot().getBootOptions())) {
                NutsWorkspaceExtensionManager extensions = session.extensions();
                extensions.setSession(session).loadExtension(extId);
                NutsSystemTerminal systemTerminal = createSystemTerminal(
                        new NutsDefaultTerminalSpec()
                                .setAutoComplete(true),
                        session
                );
                setSystemTerminal(systemTerminal, session);
                if (getSystemTerminal().isAutoCompleteSupported()) {
                    _LOGOP(session).level(Level.FINE).verb(NutsLogVerb.SUCCESS)
                            .log(NutsMessage.jstyle("enable rich terminal"));
                } else {
                    _LOGOP(session).level(Level.FINE).verb(NutsLogVerb.FAIL)
                            .log(NutsMessage.jstyle("unable to enable rich terminal"));
                }
            } else {
                _LOGOP(session).level(Level.FINE).verb(NutsLogVerb.WARNING)
                        .log(NutsMessage.jstyle("enableRichTerm discarded; next-term is excluded."));
            }
        }
    }

    public NutsSystemTerminal getSystemTerminal() {
        return systemTerminal;
    }

    public NutsSessionTerminal getTerminal() {
        return terminal;
    }

    public void setTerminal(NutsSessionTerminal terminal, NutsSession session) {
        NutsWorkspaceUtils.checkSession(ws, session);
        if (terminal == null) {
            terminal = createTerminal(session);
        }
        if (!(terminal instanceof UnmodifiableSessionTerminal)) {
            terminal = new UnmodifiableSessionTerminal(terminal, session);
        }
        this.terminal = terminal;
    }

    public NutsSessionTerminal createTerminal(InputStream in, NutsPrintStream out, NutsPrintStream err, NutsSession session) {
        NutsSessionTerminal t = createTerminal(session);
        if (in != null) {
            t.setIn(in);
        }
        if (out != null) {
            t.setOut(out);
        }
        if (err != null) {
            t.setErr(err);
        }
        return t;
    }

    public NutsSessionTerminal createTerminal(NutsSession session) {
        return new DefaultNutsSessionTerminalFromSystem(
                session, workspaceSystemTerminalAdapter
        );
//        return createTerminal(null, session);
    }

    private NutsSystemTerminal NutsSystemTerminal_of_NutsSystemTerminalBase(NutsSystemTerminalBase terminal, NutsSession session) {
        if (terminal == null) {
            throw new NutsExtensionNotFoundException(session, NutsSystemTerminalBase.class, "SystemTerminalBase");
        }
        NutsSystemTerminal syst;
        if ((terminal instanceof NutsSystemTerminal)) {
            syst = (NutsSystemTerminal) terminal;
        } else {
            try {
                syst = new DefaultSystemTerminal(terminal);
                NutsWorkspaceUtils.setSession(syst, session);
            } catch (Exception ex) {
                _LOGOP(session).level(Level.FINEST).verb(NutsLogVerb.WARNING)
                        .log(NutsMessage.jstyle("unable to create system terminal : {0}", ex));
                DefaultNutsSystemTerminalBase b = new DefaultNutsSystemTerminalBase();
                NutsWorkspaceUtils.setSession(b, session);
                syst = new DefaultSystemTerminal(b);
                NutsWorkspaceUtils.setSession(syst, session);
            }
        }
        return syst;
    }

    public void setSystemTerminal(NutsSystemTerminalBase terminal, NutsSession session) {
        if (terminal == null) {
            throw new NutsExtensionNotFoundException(session, NutsSystemTerminalBase.class, null);
        }
        NutsSystemTerminal syst = NutsSystemTerminal_of_NutsSystemTerminalBase(terminal, session);
        NutsSystemTerminal old = this.systemTerminal;
        this.systemTerminal = syst;

        if (old != this.systemTerminal) {
            NutsWorkspaceEvent event = null;
            if (session != null) {
                for (NutsWorkspaceListener workspaceListener : session.events().getWorkspaceListeners()) {
                    if (event == null) {
                        event = new DefaultNutsWorkspaceEvent(session, null, "systemTerminal", old, this.systemTerminal);
                    }
                    workspaceListener.onUpdateProperty(event);
                }
            }
        }
    }

    public InputStream stdin() {
        return stdin == null ? bootModel.stdin() : stdin;
    }

    public void setStdin(InputStream stdin) {
        this.stdin = stdin;
    }

    public NutsPrintStream stdout() {
        return stdout;
    }

    public void setStdout(NutsPrintStream stdout) {
        this.stdout = stdout == null ?

                bootModel.stdout() : stdout;
    }

    public NutsPrintStream stderr() {
        return stderr;
    }

    public void setStderr(NutsPrintStream stderr) {
        this.stderr = stderr == null ? bootModel.stderr() : stderr;
    }

    public void addPathFactory(NutsPathFactory f) {
        if (f != null && !pathFactories.contains(f)) {
            pathFactories.add(f);
        }
    }

    public void removePathFactory(NutsPathFactory f) {
        pathFactories.remove(f);
    }

    public NutsPath resolve(String path, NutsSession session, ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        //
        ClassLoader finalClassLoader = classLoader;
        NutsSupported<NutsPathSPI> z = Arrays.stream(getPathFactories())
                .map(x -> {
                    try {
                        return x.createPath(path, session, finalClassLoader);
                    } catch (Exception ex) {
                        //
                    }
                    return null;
                })
                .filter(x -> x != null && x.getSupportLevel() > 0)
                .max(Comparator.comparingInt(NutsSupported::getSupportLevel))
                .orElse(null);
        NutsPathSPI s = z == null ? null : z.getValue();
        if (s != null) {
            if (s instanceof NutsPath) {
                return (NutsPath) s;
            }
            return new NutsPathFromSPI(s);
        }
        return null;
    }

    public NutsPathFactory[] getPathFactories() {
        List<NutsPathFactory> all=new ArrayList<>(pathFactories.size()+1);
        all.addAll(pathFactories);
        all.add(invalidPathFactory);
        return all.toArray(new NutsPathFactory[0]);
    }

    public DefaultNutsBootModel getBootModel() {
        return bootModel;
    }

    public NutsPrintStream nullPrintStream() {
        return nullOut;
        //return createPrintStream(NullOutputStream.INSTANCE, NutsTerminalMode.FILTERED, session);
    }

    private static class WorkspaceSystemTerminalAdapter extends AbstractSystemTerminalAdapter {

        private final NutsWorkspace workspace;

        public WorkspaceSystemTerminalAdapter(NutsWorkspace workspace) {
            this.workspace = workspace;
        }

        public NutsSystemTerminalBase getParent() {
            return NutsWorkspaceUtils.defaultSession(workspace).config()
                    .getSystemTerminal();
        }
    }

    private class NutsWorkspaceStoredConfigImpl implements NutsWorkspaceStoredConfig {

        public NutsWorkspaceStoredConfigImpl() {
        }

        @Override
        public String getName() {
            return getStoredConfigBoot().getName();
        }

        @Override
        public NutsStoreLocationStrategy getStoreLocationStrategy() {
            return getStoredConfigBoot().getStoreLocationStrategy();
        }

        @Override
        public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy() {
            return getStoredConfigBoot().getStoreLocationStrategy();
        }

        @Override
        public NutsOsFamily getStoreLocationLayout() {
            return getStoredConfigBoot().getStoreLocationLayout();
        }

        @Override
        public Map<NutsStoreLocation, String> getStoreLocations() {
            return getStoredConfigBoot().getStoreLocations();
        }

        @Override
        public Map<NutsHomeLocation, String> getHomeLocations() {
            return getStoredConfigBoot().getHomeLocations();
        }

        @Override
        public String getStoreLocation(NutsStoreLocation folderType) {
            return new NutsStoreLocationsMap(getStoredConfigBoot().getStoreLocations()).get(folderType);
        }

        @Override
        public String getHomeLocation(NutsHomeLocation homeLocation) {
            return new NutsHomeLocationsMap(getStoredConfigBoot().getHomeLocations()).get(homeLocation);
        }

        @Override
        public NutsId getApiId() {
            String v = getStoredConfigApi().getApiVersion();
            NutsSession ws = NutsWorkspaceUtils.defaultSession(DefaultNutsWorkspaceConfigModel.this.ws);

            return v == null ? null
                    : NutsId.of(NutsConstants.Ids.NUTS_API + "#" + v, ws);
        }

        @Override
        public NutsId getRuntimeId() {
            String v = getStoredConfigApi().getRuntimeId();
            NutsSession ws = NutsWorkspaceUtils.defaultSession(DefaultNutsWorkspaceConfigModel.this.ws);
            return v == null ? null : v.contains("#")
                    ? NutsId.of(v, ws)
                    : NutsId.of(NutsConstants.Ids.NUTS_RUNTIME + "#" + v, ws);
        }

        @Override
        public String getRuntimeDependencies() {
            return getStoredConfigRuntime().getDependencies();
        }

        //        @Override
//        public String getExtensionDependencies() {
//            return getStoredConfigApi().getExtensionDependencies();
//        }
        @Override
        public String getBootRepositories() {
            return getStoredConfigBoot().getBootRepositories();
        }

        @Override
        public String getJavaCommand() {
            return getStoredConfigApi().getJavaCommand();
        }

        @Override
        public String getJavaOptions() {
            return getStoredConfigApi().getJavaOptions();
        }

        @Override
        public boolean isGlobal() {
            return getStoredConfigBoot().isGlobal();
        }

    }

    private class URLPathFactory implements NutsPathFactory {
        @Override
        public NutsSupported<NutsPathSPI> createPath(String path, NutsSession session, ClassLoader classLoader) {
            NutsWorkspaceUtils.checkSession(getWorkspace(), session);
            try {
                URL url = new URL(path);
                return NutsSupported.of(2,()->new URLPath(url, session));
            } catch (Exception ex) {
                //ignore
            }
            return null;
        }
    }

    private class NutsResourcePathFactory implements NutsPathFactory {
        @Override
        public NutsSupported<NutsPathSPI> createPath(String path, NutsSession session, ClassLoader classLoader) {
            NutsWorkspaceUtils.checkSession(getWorkspace(), session);
            try {
                if (path.startsWith("nuts-resource:")) {
                    return NutsSupported.of(2,()->new NutsResourcePath(path, session));
                }
            } catch (Exception ex) {
                //ignore
            }
            return null;
        }
    }

    private class ClasspathNutsPathFactory implements NutsPathFactory {
        @Override
        public NutsSupported<NutsPathSPI> createPath(String path, NutsSession session, ClassLoader classLoader) {
            NutsWorkspaceUtils.checkSession(getWorkspace(), session);
            try {
                if (path.startsWith("classpath:")) {
                    return NutsSupported.of(2,()->new ClassLoaderPath(path, classLoader, session));
                }
            } catch (Exception ex) {
                //ignore
            }
            return null;
        }
    }

    private class FilePathFactory implements NutsPathFactory {
        @Override
        public NutsSupported<NutsPathSPI> createPath(String path, NutsSession session, ClassLoader classLoader) {
            NutsWorkspaceUtils.checkSession(getWorkspace(), session);
            try {
                if (MOSTLY_URL_PATTERN.matcher(path).matches()) {
                    return null;
                }
                Path value = Paths.get(path);
                return NutsSupported.of(1,()->new FilePath(value, session));
            } catch (Exception ex) {
                //ignore
            }
            return null;
        }
    }

    private class InvalidFilePathFactory implements NutsPathFactory {
        @Override
        public NutsSupported<NutsPathSPI> createPath(String path, NutsSession session, ClassLoader classLoader) {
            NutsWorkspaceUtils.checkSession(getWorkspace(), session);
            try {
                return NutsSupported.of(1,()->new InvalidFilePath(path, session));
            } catch (Exception ex) {
                //ignore
            }
            return null;
        }
    }

    public Map<String, String> getConfigMap() {
        Map<String, String> p = new LinkedHashMap<>();
        if (getStoreModelMain().getEnv() != null) {
            p.putAll(getStoreModelMain().getEnv());
        }
//        p.putAll(options);
        return p;
    }

    public NutsVal getConfigProperty(String property) {
        Map<String, String> env = getStoreModelMain().getEnv();
        if (env != null) {
            return new DefaultNutsVal(env.get(property));
        }
        return new DefaultNutsVal(null);
    }

    public void setConfigProperty(String property, String value, NutsSession session) {
        Map<String, String> env = getStoreModelMain().getEnv();
//        session = CoreNutsUtils.validate(session, workspace);
        if (NutsBlankable.isBlank(value)) {
            if (env != null && env.containsKey(property)) {
                env.remove(property);
                NutsWorkspaceConfigManagerExt.of(session.config())
                        .getModel()
                        .fireConfigurationChanged("env", session, ConfigEventType.MAIN);
            }
        } else {
            if (env == null) {
                env = new LinkedHashMap<>();
                getStoreModelMain().setEnv(env);
            }
            String old = env.get(property);
            if (!value.equals(old)) {
                env.put(property, value);
                NutsWorkspaceConfigManagerExt.of(session.config())
                        .getModel()
                        .fireConfigurationChanged("env", session, ConfigEventType.MAIN);
            }
        }
    }

}
