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
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.main.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.*;
import net.thevpc.nuts.runtime.bridges.maven.MavenUtils;
import net.thevpc.nuts.runtime.core.CoreNutsWorkspaceOptions;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.log.NutsLogVerb;
import net.thevpc.nuts.runtime.main.DefaultNutsWorkspace;
import net.thevpc.nuts.runtime.main.config.compat.CompatUtils;
import net.thevpc.nuts.runtime.main.config.compat.NutsVersionCompat;
import net.thevpc.nuts.runtime.main.config.compat.v502.NutsVersionCompat502;
import net.thevpc.nuts.runtime.main.config.compat.v506.NutsVersionCompat506;
import net.thevpc.nuts.runtime.main.config.compat.v507.NutsVersionCompat507;
import net.thevpc.nuts.runtime.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.util.common.CoreCommonUtils;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.util.io.CoreIOUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author vpc
 */
public class DefaultNutsWorkspaceConfigManager implements NutsWorkspaceConfigManagerExt {

    public static final boolean NO_M2 = CoreCommonUtils.getSysBoolNutsProperty("no-m2", false);
    private final DefaultNutsWorkspace ws;
    private final Map<String, NutsUserConfig> configUsers = new LinkedHashMap<>();
    public NutsLogger LOG;
    protected NutsWorkspaceConfigBoot storeModelBoot = new NutsWorkspaceConfigBoot();
    protected NutsWorkspaceConfigApi storeModelApi = new NutsWorkspaceConfigApi();
    protected NutsWorkspaceConfigRuntime storeModelRuntime = new NutsWorkspaceConfigRuntime();
    protected NutsWorkspaceConfigSecurity storeModelSecurity = new NutsWorkspaceConfigSecurity();
    protected NutsWorkspaceConfigMain storeModelMain = new NutsWorkspaceConfigMain();
    protected NutsSdkManager sdks;
    private DefaultNutsWorkspaceCurrentConfig currentConfig;
    private NutsWorkspaceStoredConfig storedConfig = new NutsWorkspaceStoredConfigImpl();
    private ClassLoader bootClassLoader;
    private URL[] bootClassWorldURLs;
    private boolean storeModelBootChanged = false;
    private boolean storeModelApiChanged = false;
    private boolean storeModelRuntimeChanged = false;
    private boolean storeModelSecurityChanged = false;
    private boolean storeModelMainChanged = false;
    private NutsWorkspaceOptions options;
    private NutsWorkspaceInitInformation initOptions;
    private long startCreateTime;
    private long endCreateTime;
    private NutsIndexStoreFactory indexStoreClientFactory;
    private Set<String> excludedRepositoriesSet = new HashSet<>();
    private NutsStoreLocationsMap preUpdateConfigStoreLocations;
    private Set<String> parsedBootRepositories;
    private NutsCommandAliasManager aliases;
    private NutsImportManager imports;
    private NutsWorkspaceEnvManager env;

    public DefaultNutsWorkspaceConfigManager(final DefaultNutsWorkspace ws, NutsWorkspaceInitInformation initOptions) {
        this.ws = ws;
        LOG = ws.log().of(DefaultNutsWorkspaceConfigManager.class);
        this.initOptions = initOptions;
        this.options = this.initOptions.getOptions();
        this.bootClassLoader = initOptions.getClassWorldLoader() == null ? Thread.currentThread().getContextClassLoader() : initOptions.getClassWorldLoader();
        this.bootClassWorldURLs = initOptions.getClassWorldURLs() == null ? null : Arrays.copyOf(initOptions.getClassWorldURLs(), initOptions.getClassWorldURLs().length);
        this.excludedRepositoriesSet = this.options.getExcludedRepositories() == null ? null : new HashSet<>(CoreStringUtils.split(Arrays.asList(this.options.getExcludedRepositories()), " ,;"));
        sdks = new DefaultNutsSdkManager(ws);
        env = new DefaultWorkspaceEnvManager(ws);
        imports = new DefaultImportManager(ws);
        aliases = new DefaultAliasManager(ws);
    }

    @Override
    public String getName() {
        return ws.locations().getWorkspaceLocation().getFileName().toString();
    }

    @Override
    public String getUuid() {
        return storeModelBoot.getUuid();
    }

    @Override
    public NutsWorkspaceStoredConfig stored() {
        return storedConfig;
    }

    @Override
    public ClassLoader getBootClassLoader() {
        return bootClassLoader;
    }

    @Override
    public URL[] getBootClassWorldURLs() {
        return bootClassWorldURLs == null ? null : Arrays.copyOf(bootClassWorldURLs, bootClassWorldURLs.length);
    }

    @Override
    public boolean isReadOnly() {
        return options.isReadOnly();
    }

    @Override
    public boolean save(boolean force, NutsSession session) {
        if (!force && !isConfigurationChanged()) {
            return false;
        }
        NutsWorkspaceUtils.of(ws).checkReadOnly();
        session = NutsWorkspaceUtils.of(ws).validateSession(session);
        boolean ok = false;
        ws.security().checkAllowed(NutsConstants.Permissions.SAVE, "save");
        Path apiVersionSpecificLocation = ws.locations().getStoreLocation(getApiId(), NutsStoreLocation.CONFIG);
        if (force || storeModelBootChanged) {

            Path file = ws.locations().getWorkspaceLocation().resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
            storeModelBoot.setConfigVersion(current().getApiVersion());
            if (storeModelBoot.getExtensions() != null) {
                for (NutsWorkspaceConfigBoot.ExtensionConfig extension : storeModelBoot.getExtensions()) {
                    //inherited
                    extension.setConfigVersion(null);
                }
            }
            ws.formats().json().value(storeModelBoot).print(file);
            storeModelBootChanged = false;
            ok = true;
        }

        Path configVersionSpecificLocation = ws.locations().getStoreLocation(getApiId().builder().setVersion(NutsConstants.Versions.RELEASE).build(), NutsStoreLocation.CONFIG);
        if (force || storeModelSecurityChanged) {
            storeModelSecurity.setUsers(configUsers.isEmpty() ? null : configUsers.values().toArray(new NutsUserConfig[0]));

            Path file = configVersionSpecificLocation.resolve(CoreNutsConstants.Files.WORKSPACE_SECURITY_CONFIG_FILE_NAME);
            storeModelSecurity.setConfigVersion(current().getApiVersion());
            if (storeModelSecurity.getUsers() != null) {
                for (NutsUserConfig extension : storeModelSecurity.getUsers()) {
                    //inherited
                    extension.setConfigVersion(null);
                }
            }
            ws.formats().json().value(storeModelSecurity).print(file);
            storeModelSecurityChanged = false;
            ok = true;
        }

        if (force || storeModelMainChanged) {
            List<NutsSdkLocation> plainSdks = new ArrayList<>();
            plainSdks.addAll(Arrays.asList(sdks().find(null, null, null)));
            storeModelMain.setSdk(plainSdks);
            storeModelMain.setRepositories(new ArrayList<>(Arrays.asList(ws.repos().getRepositoryRefs(session))));

            Path file = configVersionSpecificLocation.resolve(CoreNutsConstants.Files.WORKSPACE_MAIN_CONFIG_FILE_NAME);
            storeModelMain.setConfigVersion(current().getApiVersion());
            if (storeModelMain.getCommandFactories() != null) {
                for (NutsCommandAliasFactoryConfig item : storeModelMain.getCommandFactories()) {
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
            if (storeModelMain.getSdk() != null) {
                for (NutsSdkLocation item : storeModelMain.getSdk()) {
                    //inherited
                    item.setConfigVersion(null);
                }
            }
            ws.formats().json().value(storeModelMain).print(file);
            storeModelMainChanged = false;
            ok = true;
        }

        if (force || storeModelApiChanged) {
            Path afile = apiVersionSpecificLocation.resolve(NutsConstants.Files.WORKSPACE_API_CONFIG_FILE_NAME);
            storeModelApi.setConfigVersion(current().getApiVersion());
            if (storeModelSecurity.getUsers() != null) {
                for (NutsUserConfig item : storeModelSecurity.getUsers()) {
                    //inherited
                    item.setConfigVersion(null);
                }
            }
            ws.formats().json().value(storeModelApi).print(afile);
            storeModelApiChanged = false;
            ok = true;
        }
        if (force || storeModelRuntimeChanged) {
            Path runtimeVersionSpecificLocation = ws.locations().getStoreLocation(NutsStoreLocation.CACHE)
                    .resolve(NutsConstants.Folders.ID).resolve(ws.locations().getDefaultIdBasedir(getRuntimeId()));
            Path afile = runtimeVersionSpecificLocation.resolve(NutsConstants.Files.WORKSPACE_RUNTIME_CACHE_FILE_NAME);
            storeModelRuntime.setConfigVersion(current().getApiVersion());
            ws.formats().json().value(storeModelRuntime).print(afile);
            storeModelRuntimeChanged = false;
            ok = true;
        }
        NutsException error = null;
        for (NutsRepository repo : ws.repos().getRepositories(session)) {
            try {
                ok |= repo.config().save(force, session);
            } catch (NutsException ex) {
                error = ex;
            }
        }
        if (error != null) {
            throw error;
        }

        return ok;
    }

    @Override
    public void save(NutsSession session) {
        save(true, session);
    }

    @Override
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
            lastConfigPath = Nuts.getPlatformHomeFolder(null, null, null,
                    global,
                    CoreNutsUtils.resolveValidWorkspaceName(effWorkspaceName));
            lastConfigLoaded = parseBootConfig(Paths.get(lastConfigPath));
            defaultLocation = true;
            return new DefaultNutsWorkspaceBootConfig(ws, _ws0, lastConfigPath, effWorkspaceName, defaultLocation, lastConfigLoaded);
        } else if (followLinks) {
            defaultLocation = CoreNutsUtils.isValidWorkspaceName(_ws);
            int maxDepth = 36;
            for (int i = 0; i < maxDepth; i++) {
                lastConfigPath
                        = CoreNutsUtils.isValidWorkspaceName(_ws)
                        ? Nuts.getPlatformHomeFolder(
                        null, null, null,
                        global,
                        CoreNutsUtils.resolveValidWorkspaceName(_ws)
                ) : CoreIOUtils.getAbsolutePath(_ws);

                NutsWorkspaceConfigBoot configLoaded = parseBootConfig(Paths.get(lastConfigPath));
                if (configLoaded == null) {
                    //not loaded
                    break;
                }
                if (CoreStringUtils.isBlank(configLoaded.getWorkspace())) {
                    lastConfigLoaded = configLoaded;
                    break;
                }
                _ws = configLoaded.getWorkspace();
                if (i >= maxDepth - 1) {
                    throw new NutsIllegalArgumentException(null, "Cyclic Workspace resolution");
                }
            }
            if(lastConfigLoaded==null){
                return null;
            }
            effWorkspaceName = CoreNutsUtils.resolveValidWorkspaceName(_ws);
            return new DefaultNutsWorkspaceBootConfig(ws, _ws0, lastConfigPath, effWorkspaceName, defaultLocation, lastConfigLoaded);
        } else {
            defaultLocation = CoreNutsUtils.isValidWorkspaceName(_ws);
            lastConfigPath
                    = CoreNutsUtils.isValidWorkspaceName(_ws)
                    ? Nuts.getPlatformHomeFolder(
                    null, null, null,
                    global,
                    CoreNutsUtils.resolveValidWorkspaceName(_ws)
            ) : CoreIOUtils.getAbsolutePath(_ws);

            lastConfigLoaded = parseBootConfig(Paths.get(lastConfigPath));
            if (lastConfigLoaded == null) {
                return null;
            }
            effWorkspaceName = CoreNutsUtils.resolveValidWorkspaceName(_ws);
            return new DefaultNutsWorkspaceBootConfig(ws, _ws0, lastConfigPath, effWorkspaceName, defaultLocation, lastConfigLoaded);
        }
    }

    @Override
    public NutsWorkspaceOptionsBuilder optionsBuilder() {
        return new CoreNutsWorkspaceOptions(ws);
    }

    @Override
    public NutsWorkspaceOptions options() {
        return getOptions();
    }

    @Override
    public NutsWorkspaceOptions getOptions() {
        return options.copy();
    }

    @Override
    public NutsId createContentFaceId(NutsId id, NutsDescriptor desc) {
        Map<String, String> q = id.getProperties();
        q.put(NutsConstants.IdProperties.PACKAGING, CoreStringUtils.trim(desc.getPackaging()));
//        q.put(NutsConstants.QUERY_EXT,CoreStringUtils.trim(descriptor.getExt()));
        q.put(NutsConstants.IdProperties.FACE, NutsConstants.QueryFaces.CONTENT);
        return id.builder().setProperties(q).build();
    }

    @Override
    public NutsWorkspaceListManager createWorkspaceListManager(String name, NutsSession session) {
        return new DefaultNutsWorkspaceListManager(ws, name);
    }

    //    @Override
//    public void setBootConfig(NutsBootConfig other) {
//        if (other == null) {
//            other = new NutsBootConfig();
//        }
//        if (!CoreStringUtils.isBlank(other.getRuntimeId())) {
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

    @Override
    public boolean isSupportedRepositoryType(String repositoryType, NutsSession session) {
        if (CoreStringUtils.isBlank(repositoryType)) {
            repositoryType = NutsConstants.RepoTypes.NUTS;
        }
        return ws.extensions().createAllSupported(NutsRepositoryFactoryComponent.class,
                new NutsRepositoryConfig().setType(repositoryType),
                session).size() > 0;
    }

    @Override
    public NutsRepositoryDefinition[] getDefaultRepositories(NutsSession session) {
        List<NutsRepositoryDefinition> all = new ArrayList<>();
        for (NutsRepositoryFactoryComponent provider : ws.extensions().createAll(NutsRepositoryFactoryComponent.class, session)) {
            all.addAll(Arrays.asList(provider.getDefaultRepositories(ws)));
        }
        Collections.sort(all, new Comparator<NutsRepositoryDefinition>() {
            @Override
            public int compare(NutsRepositoryDefinition o1, NutsRepositoryDefinition o2) {
                return Integer.compare(o1.getOrder(), o2.getOrder());
            }
        });
        return all.toArray(new NutsRepositoryDefinition[0]);
    }

    @Override
    public Set<String> getAvailableArchetypes(NutsSession session) {
        Set<String> set = new HashSet<>();
        set.add("default");
        for (NutsWorkspaceArchetypeComponent extension : ws.extensions().createAllSupported(NutsWorkspaceArchetypeComponent.class, null, session)) {
            set.add(extension.getName());
        }
        return set;
    }


    //    @Override
//    public char[] decryptString(char[] input) {
//        if (input == null || input.length == 0) {
//            return new char[0];
//        }
//        return CoreIOUtils.bytesToChars(decryptString(CoreIOUtils.charsToBytes(input)));
//    }
//
//    @Override
//    public char[] encryptString(char[] input) {
//        if (input == null || input.length == 0) {
//            return new char[0];
//        }
//        return CoreIOUtils.bytesToChars(encryptString(CoreIOUtils.charsToBytes(input)));
//    }
//
//    @Override
//    public byte[] decryptString(byte[] input) {
//        if (input == null || input.length == 0) {
//            return new byte[0];
//        }
//        String passphrase = getEnv(CoreSecurityUtils.ENV_KEY_PASSPHRASE, CoreSecurityUtils.DEFAULT_PASSPHRASE);
//        return CoreSecurityUtils.httpDecrypt(input, passphrase);
//    }
//
//    @Override
//    public byte[] encryptString(byte[] input) {
//        if (input == null || input.length == 0) {
//            return new byte[0];
//        }
//        String passphrase = getEnv(CoreSecurityUtils.ENV_KEY_PASSPHRASE, CoreSecurityUtils.DEFAULT_PASSPHRASE);
//        return CoreSecurityUtils.httpEncrypt(input, passphrase);
//    }

    @Override
    public Path resolveRepositoryPath(String repositoryLocation, NutsSession session) {
        Path root = this.getRepositoriesRoot();
        return Paths.get(ws.io().expandPath(repositoryLocation,
                root != null ? root.toString() : ws.locations().getStoreLocation(NutsStoreLocation.CONFIG)
                        .resolve(NutsConstants.Folders.REPOSITORIES).toString()));
    }

    //    @Override
//    public boolean isGlobal() {
//        return config.isGlobal();
//    }
    @Override
    public NutsIndexStoreFactory getIndexStoreClientFactory() {
        return indexStoreClientFactory;
    }

    @Override
    public String getBootRepositories() {
        return current().getBootRepositories();
    }

    @Override
    public String getJavaCommand() {
        return current().getJavaCommand();
    }

    @Override
    public String getJavaOptions() {
        return current().getJavaOptions();
    }

    @Override
    public boolean isGlobal() {
        return current().isGlobal();
    }

    @Override
    public long getCreationStartTimeMillis() {
        return startCreateTime;
    }

    @Override
    public long getCreationFinishTimeMillis() {
        return endCreateTime;
    }

    @Override
    public long getCreationTimeMillis() {
        return endCreateTime - startCreateTime;
    }

    public NutsWorkspaceConfigMain getStoreModelMain() {
        return storeModelMain;
    }

    @Override
    public String getApiVersion() {
        if (currentConfig == null) {
            return Nuts.getVersion();
        }
        return current().getApiVersion();
    }

    @Override
    public NutsId getApiId() {
        if (currentConfig == null) {
            return CoreNutsUtils.parseNutsId(NutsConstants.Ids.NUTS_API + "#" + Nuts.getVersion());
        }
        return current().getApiId();
    }

    @Override
    public NutsId getRuntimeId() {
        return current().getRuntimeId();
    }

    @Override
    public DefaultNutsWorkspaceCurrentConfig current() {
        if (currentConfig == null) {
            throw new IllegalStateException("Unable to use workspace.current(). Still in initialize status");
        }
        return currentConfig;
    }

    @Override
    public void setStartCreateTimeMillis(long startCreateTime) {
        this.startCreateTime = startCreateTime;
    }

    @Override
    public void setCurrentConfig(DefaultNutsWorkspaceCurrentConfig currentConfig) {
        this.currentConfig = currentConfig;
    }

    @Override
    public void setConfigBoot(NutsWorkspaceConfigBoot config, NutsUpdateOptions options) {
        setConfigBoot(config, options, true);
    }

    public void setConfigApi(NutsWorkspaceConfigApi config, NutsUpdateOptions options) {
        setConfigApi(config, options, true);
    }

    public void setConfigRuntime(NutsWorkspaceConfigRuntime config, NutsUpdateOptions options) {
        setConfigRuntime(config, options, true);
    }

    public void setConfigSecurity(NutsWorkspaceConfigSecurity config, NutsUpdateOptions options) {
        setConfigSecurity(config, options, true);
    }

    public void setConfigMain(NutsWorkspaceConfigMain config, NutsUpdateOptions options) {
        setConfigMain(config, options, true);
    }

    @Override
    public void setEndCreateTimeMillis(long endCreateTime) {
        this.endCreateTime = endCreateTime;
    }

    @Override
    public void prepareBootApi(NutsId apiId, NutsId runtimeId, boolean force, NutsSession session) {
        if (apiId == null) {
            throw new NutsNotFoundException(ws, apiId);
        }
        Path apiConfigFile = ws.locations().getStoreLocation(apiId, NutsStoreLocation.CONFIG).resolve(NutsConstants.Files.WORKSPACE_API_CONFIG_FILE_NAME);
        if (force || !Files.isRegularFile(apiConfigFile)) {
            Map<String, Object> m = new LinkedHashMap<>();
            if (runtimeId == null) {
                runtimeId = ws.search().addId(NutsConstants.Ids.NUTS_RUNTIME)
                        .setRuntime(true)
                        .setTargetApiVersion(apiId.getVersion().getValue())
                        .setFailFast(false).setLatest(true).getResultIds().first();
            }
            if (runtimeId == null) {
                runtimeId = MavenUtils.of(ws).resolveLatestMavenId(ws.id().parser().parse(NutsConstants.Ids.NUTS_RUNTIME),
                        (rtVersion) -> rtVersion.startsWith(apiId.getVersion().getValue() + ".")
                );
            }
            if (runtimeId == null) {
                throw new NutsNotFoundException(ws, runtimeId);
            }
            m.put("configVersion", apiId.getVersion().getValue());
            m.put("apiVersion", apiId.getVersion().getValue());
            m.put("runtimeId", runtimeId.getLongName());
            String javaCommand = getStoredConfigApi().getJavaCommand();
            String javaOptions = getStoredConfigApi().getJavaOptions();
            m.put("javaCommand", javaCommand);
            m.put("javaOptions", javaOptions);
            ws.formats().json().value(m).print(apiConfigFile);
        }
        downloadId(apiId, force, null, true);
    }

    @Override
    public void prepareBootRuntime(NutsId id, boolean force, NutsSession session) {
        prepareBootRuntimeOrExtension(id, force, true, session);
    }

    @Override
    public void prepareBootExtension(NutsId id, boolean force, NutsSession session) {
        prepareBootRuntimeOrExtension(id, force, false, session);
    }

    @Override
    public void prepareBoot(boolean force, NutsSession session) {
        prepareBootApi(getApiId(), current().getRuntimeId(), force, session);
        prepareBootRuntime(current().getRuntimeId(), force, session);
        List<NutsWorkspaceConfigBoot.ExtensionConfig> extensions = getStoredConfigBoot().getExtensions();
        if (extensions != null) {
            for (NutsWorkspaceConfigBoot.ExtensionConfig extension : extensions) {
                if (extension.isEnabled()) {
                    prepareBootExtension(extension.getId(), force, session);
                }
            }
        }
    }

    @Override
    public boolean isConfigurationChanged() {
        return storeModelBootChanged || storeModelApiChanged || storeModelRuntimeChanged || storeModelSecurityChanged || storeModelMainChanged;
    }

    @Override
    public boolean loadWorkspace(NutsSession session) {
        try {
            session = NutsWorkspaceUtils.of(ws).validateSession(session);
            NutsWorkspaceConfigBoot _config = parseBootConfig();
            if (_config == null) {
                return false;
            }
            DefaultNutsWorkspaceCurrentConfig cconfig = new DefaultNutsWorkspaceCurrentConfig(ws).merge(_config);
            if (cconfig.getApiId() == null) {
                cconfig.setApiId(ws.id().parser().parse(NutsConstants.Ids.NUTS_API + "#" + initOptions.getApiVersion()));
            }
            if (cconfig.getRuntimeId() == null) {
                cconfig.setRuntimeId(initOptions.getRuntimeId());
            }
            if (cconfig.getRuntimeDependencies() == null) {
                cconfig.setRuntimeDependencies(initOptions.getRuntimeDependencies());
            }
            if (cconfig.getExtensionDependencies() == null) {
                cconfig.setExtensionDependencies(initOptions.getExtensionDependencies());
            }
            if (cconfig.getBootRepositories() == null) {
                cconfig.setBootRepositories(initOptions.getBootRepositories());
            }
            cconfig.merge(options());

            setCurrentConfig(cconfig.build(ws.locations().getWorkspaceLocation()));

            NutsVersionCompat compat = createNutsVersionCompat(Nuts.getVersion());
            NutsWorkspaceConfigApi aconfig = compat.parseApiConfig();
            if (aconfig != null) {
                cconfig.merge(aconfig);
            }
            NutsWorkspaceConfigRuntime rconfig = compat.parseRuntimeConfig();
            if (rconfig != null) {
                cconfig.merge(rconfig);
            }
            NutsWorkspaceConfigSecurity sconfig = compat.parseSecurityConfig();
            NutsWorkspaceConfigMain mconfig = compat.parseMainConfig();
            if (options.isRecover() || options.isReset()) {
                //always reload boot resolved versions!
                cconfig.setApiId(ws.id().parser().parse(NutsConstants.Ids.NUTS_API + "#" + initOptions.getApiVersion()));
                cconfig.setRuntimeId(initOptions.getRuntimeId());
                cconfig.setRuntimeDependencies(initOptions.getRuntimeDependencies());
                cconfig.setExtensionDependencies(initOptions.getExtensionDependencies());
                cconfig.setBootRepositories(initOptions.getBootRepositories());
            }
            setCurrentConfig(cconfig
                    .build(ws.locations().getWorkspaceLocation())
            );
            setConfigBoot(_config, new NutsUpdateOptions().setSession(session), false);
            setConfigApi(aconfig, new NutsUpdateOptions().setSession(session), false);
            setConfigRuntime(rconfig, new NutsUpdateOptions().setSession(session), false);
            setConfigSecurity(sconfig, new NutsUpdateOptions().setSession(session), false);
            setConfigMain(mconfig, new NutsUpdateOptions().setSession(session), false);
            storeModelBootChanged = false;
            storeModelApiChanged = false;
            storeModelRuntimeChanged = false;
            storeModelSecurityChanged = false;
            storeModelMainChanged = false;
            return true;
        } catch (Exception ex) {
            onLoadWorkspaceError(ex);
        }
        return false;
    }

    @Override
    public void setBootApiVersion(String value, NutsUpdateOptions options) {
        if (!Objects.equals(value, storeModelApi.getApiVersion())) {
            options = CoreNutsUtils.validate(options, ws);
            storeModelApi.setApiVersion(value);
            fireConfigurationChanged("api-version", options.getSession(), ConfigEventType.API);
        }
    }

    @Override
    public void setBootRuntimeId(String value, NutsUpdateOptions options) {
        if (!Objects.equals(value, storeModelApi.getRuntimeId())) {
            options = CoreNutsUtils.validate(options, ws);
            storeModelApi.setRuntimeId(value);
            fireConfigurationChanged("runtime-id", options.getSession(), ConfigEventType.API);
        }
    }

    @Override
    public void setBootRuntimeDependencies(String value, NutsUpdateOptions options) {
        if (!Objects.equals(value, storeModelRuntime.getDependencies())) {
            options = CoreNutsUtils.validate(options, ws);
            storeModelRuntime.setDependencies(value);
            setConfigRuntime(storeModelRuntime, options, true);
        }
    }

    @Override
    public void setBootRepositories(String value, NutsUpdateOptions options) {
        if (!Objects.equals(value, storeModelBoot.getBootRepositories())) {
            options = CoreNutsUtils.validate(options, ws);
            storeModelBoot.setBootRepositories(value);
            fireConfigurationChanged("boot-repositories", options.getSession(), ConfigEventType.API);
        }
    }

    @Override
    public NutsUserConfig getUser(String userId) {
        NutsUserConfig _config = getSecurity(userId);
        if (_config == null) {
            if (NutsConstants.Users.ADMIN.equals(userId) || NutsConstants.Users.ANONYMOUS.equals(userId)) {
                _config = new NutsUserConfig(userId, null, null, null);
                setUser(_config, new NutsUpdateOptions().setSession(ws.createSession()));
            }
        }
        return _config;
    }

    @Override
    public NutsUserConfig[] getUsers() {
        return configUsers.values().toArray(new NutsUserConfig[0]);
    }

    @Override
    public void setUser(NutsUserConfig config, NutsUpdateOptions options) {
        if (config != null) {
            options = CoreNutsUtils.validate(options, ws);
            configUsers.put(config.getUser(), config);
            fireConfigurationChanged("user", options.getSession(), ConfigEventType.SECURITY);
        }
    }

    @Override
    public void removeUser(String userId, NutsRemoveOptions options) {
        NutsUserConfig old = getSecurity(userId);
        if (old != null) {
            configUsers.remove(userId);
            fireConfigurationChanged("users", options == null ? null : options.getSession(), ConfigEventType.SECURITY);
        }
    }

    @Override
    public void setSecure(boolean secure, NutsUpdateOptions options) {
        if (secure != storeModelSecurity.isSecure()) {
            options = CoreNutsUtils.validate(options, ws);
            storeModelSecurity.setSecure(secure);
            fireConfigurationChanged("secure", options.getSession(), ConfigEventType.SECURITY);
        }
    }

    @Override
    public void fireConfigurationChanged(String configName, NutsSession session, ConfigEventType t) {
        ((DefaultImportManager) imports()).invalidateCache();
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
        for (NutsWorkspaceListener workspaceListener : ws.events().getWorkspaceListeners()) {
            workspaceListener.onConfigurationChanged(evt);
        }
    }

    //    @Override
    public NutsWorkspaceConfigApi getStoredConfigApi() {
        if (storeModelApi.getApiVersion() == null) {
            storeModelApi.setApiVersion(Nuts.getVersion());
        }
        return storeModelApi;
    }

    @Override
    public NutsWorkspaceConfigBoot getStoredConfigBoot() {
        return storeModelBoot;
    }

    @Override
    public NutsWorkspaceConfigSecurity getStoredConfigSecurity() {
        return storeModelSecurity;
    }

    @Override
    public NutsWorkspaceConfigMain getStoredConfigMain() {
        return storeModelMain;
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return ws;
    }

    @Override
    public Path getRepositoriesRoot() {
        return ws.locations().getStoreLocation(NutsStoreLocation.CONFIG).resolve(NutsConstants.Folders.REPOSITORIES);
    }

    @Override
    public boolean isValidWorkspaceFolder() {
        Path file = ws.locations().getWorkspaceLocation().resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        return Files.isRegularFile(file);
    }

    @Override
    public NutsAuthenticationAgent createAuthenticationAgent(String authenticationAgent, NutsSession session) {
        authenticationAgent = CoreStringUtils.trim(authenticationAgent);
        NutsAuthenticationAgent supported = null;
        if (authenticationAgent.isEmpty()) {
            supported = ws.extensions().createSupported(NutsAuthenticationAgent.class, "", session);
        } else {
            List<NutsAuthenticationAgent> agents = ws.extensions().createAllSupported(NutsAuthenticationAgent.class, authenticationAgent, session);
            for (NutsAuthenticationAgent agent : agents) {
                if (agent.getId().equals(authenticationAgent)) {
                    supported = agent;
                }
            }
        }
        if (supported == null) {
            throw new NutsExtensionNotFoundException(ws, NutsAuthenticationAgent.class, "AuthenticationAgent");
        }
        NutsWorkspaceUtils.of(ws).setWorkspace(supported);
        return supported;
    }

    @Override
    public void setExcludedRepositories(String[] excludedRepositories, NutsUpdateOptions options) {
        excludedRepositoriesSet = excludedRepositories == null ? null : new HashSet<>(CoreStringUtils.split(Arrays.asList(excludedRepositories), " ,;"));
    }

    @Override
    public void setUsers(NutsUserConfig[] users, NutsUpdateOptions options) {
        options = CoreNutsUtils.validate(options, ws);
        for (NutsUserConfig u : getUsers()) {
            removeUser(u.getUser(), CoreNutsUtils.toRemoveOptions(options));
        }
        for (NutsUserConfig conf : users) {
            setUser(conf, options);
        }
    }

    @Override
    public NutsWorkspaceConfigRuntime getStoredConfigRuntime() {
        return storeModelRuntime;
    }

    @Override
    public NutsId createSdkId(String type, String version) {
        return NutsWorkspaceUtils.of(ws).createSdkId(type, version);
    }

    public void onExtensionsPrepared(NutsSession session) {
        try {
            indexStoreClientFactory = ws.extensions().createSupported(NutsIndexStoreFactory.class, null, session);
        } catch (Exception ex) {
            //
        }
        if (indexStoreClientFactory == null) {
            indexStoreClientFactory = new DummyNutsIndexStoreFactory();
        }
    }

    @Override
    public NutsSdkManager sdks() {
        return sdks;
    }

    @Override
    public NutsImportManager imports() {
        return imports;
    }

    @Override
    public NutsCommandAliasManager aliases() {
        return aliases;
    }

    @Override
    public NutsWorkspaceEnvManager env() {
        return env;
    }

    public void setConfigApi(NutsWorkspaceConfigApi config, NutsUpdateOptions options, boolean fire) {
        this.storeModelApi = config == null ? new NutsWorkspaceConfigApi() : config;
        if (fire) {
            fireConfigurationChanged("boot-api-config", options.getSession(), ConfigEventType.API);
        }
    }

    public void setConfigRuntime(NutsWorkspaceConfigRuntime config, NutsUpdateOptions options, boolean fire) {
        this.storeModelRuntime = config == null ? new NutsWorkspaceConfigRuntime() : config;
        if (fire) {
            fireConfigurationChanged("boot-runtime-config", options.getSession(), ConfigEventType.RUNTIME);
        }
    }

    private void setConfigSecurity(NutsWorkspaceConfigSecurity config, NutsUpdateOptions options, boolean fire) {
        this.storeModelSecurity = config == null ? new NutsWorkspaceConfigSecurity() : config;
        configUsers.clear();
        if (this.storeModelSecurity.getUsers() != null) {
            for (NutsUserConfig s : this.storeModelSecurity.getUsers()) {
                configUsers.put(s.getUser(), s);
            }
        }
        storeModelSecurityChanged = true;
        if (fire) {
            fireConfigurationChanged("config-security", options.getSession(), ConfigEventType.SECURITY);
        }
    }

    private void setConfigMain(NutsWorkspaceConfigMain config, NutsUpdateOptions options, boolean fire) {
        this.storeModelMain = config == null ? new NutsWorkspaceConfigMain() : config;
        ((DefaultNutsSdkManager) sdks()).setSdks(this.storeModelMain.getSdk().toArray(new NutsSdkLocation[0]), options);
        NutsRemoveOptions o0 = CoreNutsUtils.toRemoveOptions(options);
        o0.setSession(options.getSession());
        ws.repos().removeAllRepositories(o0);
        if (this.storeModelMain.getRepositories() != null) {
            NutsAddOptions addOption = CoreNutsUtils.toAddOptions(options);
            for (NutsRepositoryRef ref : this.storeModelMain.getRepositories()) {
                ws.repos().addRepository(ref, addOption);
            }
        }

        storeModelMainChanged = true;
        if (fire) {
            fireConfigurationChanged("config-main", options.getSession(), ConfigEventType.MAIN);
        }
    }

    private void setConfigBoot(NutsWorkspaceConfigBoot config, NutsUpdateOptions options, boolean fire) {
        options = CoreNutsUtils.validate(options, ws);
        this.storeModelBoot = config;
        if (CoreStringUtils.isBlank(config.getUuid())) {
            config.setUuid(UUID.randomUUID().toString());
            fire = true;
        }
        if (fire) {
            fireConfigurationChanged("config-master", options.getSession(), ConfigEventType.BOOT);
        }
    }

    public String getBootClassWorldString() {
        StringBuilder sb = new StringBuilder();
        for (URL bootClassWorldURL : getBootClassWorldURLs()) {
            if (sb.length() > 0) {
                sb.append(File.pathSeparator);
            }
            if (CoreIOUtils.isPathFile(bootClassWorldURL.toString())) {
                File f = CoreIOUtils.toPathFile(bootClassWorldURL.toString()).toFile();
                sb.append(f.getPath());
            } else {
                sb.append(bootClassWorldURL.toString().replace(":", "\\:"));
            }
        }
        return sb.toString();
    }

    //    @Override
//    public Path getBootNutsJar() {
//        try {
//            NutsId baseId = ws.id().parseRequired(NutsConstants.Ids.NUTS_API);
//            String urlPath = "/META-INF/maven/" + baseId.getGroup() + "/" + baseId.getName() + "/pom.properties";
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
//                            ws.builder().setGroup(m.getGroupId()).setName(m.getArtifactId()).setVersion(m.getVersion())
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
    @Override
    public String toString() {
        String s1 = "NULL";
        String s2 = "NULL";
        s1 = String.valueOf(getApiId());
        s2 = String.valueOf(getRuntimeId());
        return "NutsWorkspaceConfig{"
                + "workspaceBootId=" + s1
                + ", workspaceRuntimeId=" + s2
                + ", workspace=" + ((currentConfig == null) ? "NULL" : ("'" + ws.locations().getWorkspaceLocation() + '\''))
                + '}';
    }

    public void prepareBootRuntimeOrExtension(NutsId id, boolean force, boolean runtime, NutsSession session) {
        Path configFile = ws.locations().getStoreLocation(NutsStoreLocation.CACHE)
                .resolve(NutsConstants.Folders.ID).resolve(ws.locations().getDefaultIdBasedir(id)).resolve(runtime
                        ? NutsConstants.Files.WORKSPACE_RUNTIME_CACHE_FILE_NAME
                        : NutsConstants.Files.WORKSPACE_EXTENSION_CACHE_FILE_NAME
                );
        Path jarFile = ws.locations().getStoreLocation(NutsStoreLocation.LIB)
                .resolve(NutsConstants.Folders.ID).resolve(ws.locations().getDefaultIdBasedir(id))
                .resolve(ws.locations().getDefaultIdFilename(id.builder().setFaceContent().setPackaging("jar").build()));
        if (!force && (Files.isRegularFile(configFile) && Files.isRegularFile(jarFile))) {
            return;
        }
        List<NutsId> deps = new ArrayList<>();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id.getLongName());

        NutsDefinition def = ws.fetch().setId(id).setDependencies(true)
                .setOptional(false)
                .addScope(NutsDependencyScopePattern.RUN)
                .setContent(true)
                .setFailFast(false)
                .setSession(CoreNutsUtils.silent(session))
                .getResultDefinition();
        if (def == null) {
            //selected repositories cannot reach runtime component
            //fallback to default
            MavenUtils.DepsAndRepos dd = MavenUtils.of(ws).loadDependenciesAndRepositoriesFromPomPath(id, resolveBootRepositories());
            if (dd == null) {
                throw new NutsNotFoundException(ws, id);
            }
            m.put("dependencies", String.join(";", dd.deps));
            if (runtime) {
                m.put("bootRepositories", String.join(";", dd.repos));
            }
            for (String dep : dd.deps) {
                deps.add(ws.id().parser().parse(dep));
            }
        } else {
            for (NutsDependency dep : def.getDependencies()) {
                deps.add(dep.toId());
            }
            m.put("dependencies",
                    Arrays.stream(
                            def.getDependencies()
                    ).map(x -> x.toId().getLongName()).collect(Collectors.joining(";"))
            );
            if (runtime) {
                m.put("bootRepositories", def.getDescriptor().getProperties().get("nuts-runtime-repositories"));
            }
        }

        if (force || !Files.isRegularFile(configFile)) {
            ws.formats().json().value(m).print(configFile);
        }
        downloadId(id, force, (def != null && def.getContent().getPath() != null) ? def.getContent().getPath() : null, false);
        for (NutsId dep : deps) {
            downloadId(dep, force, null, true);
        }
    }

    //    @Override
//    public String getRuntimeDependencies() {
//        return current().getRuntimeDependencies();
//    }
//
//    @Override
//    public String getExtensionDependencies() {
//        return current().getExtensionDependencies();
//    }
    private void downloadId(NutsId id, boolean force, Path path, boolean fetch) {
        String idFileName = ws.locations().getDefaultIdFilename(id.builder().setFaceContent().setPackaging("jar").build());
        Path jarFile = ws.locations().getStoreLocation(NutsStoreLocation.LIB)
                .resolve(NutsConstants.Folders.ID).resolve(ws.locations().getDefaultIdBasedir(id))
                .resolve(idFileName);
        if (force || !Files.isRegularFile(jarFile)) {
            if (path != null) {
                ws.io().copy().from(path).to(jarFile).run();
            } else {
                if (fetch) {
                    NutsDefinition def = ws.fetch().setId(id).setDependencies(true)
                            .setOptional(false)
                            .addScope(NutsDependencyScopePattern.RUN)
                            .setContent(true)
                            .setSession(ws.createSession().setSilent())
                            .setFailFast(false)
                            .getResultDefinition();
                    if (def != null) {
                        ws.io().copy().from(def.getPath()).to(jarFile).run();
                        return;
                    }
                }
                for (String pp0 : resolveBootRepositories()) {
                    String pp = CoreNutsUtils.repositoryStringToDefinition(pp0).getLocation();
                    if (CoreIOUtils.isPathHttp(pp)) {
                        try {
                            if (!pp.endsWith("/")) {
                                pp += "/";
                            }
                            ws.io().copy().from(pp + ws.locations().getDefaultIdBasedir(id) + "/" + idFileName).to(jarFile).run();
                            return;
                        } catch (Exception ex) {
                            //ignore
                        }
                    } else {
                        try {
                            ws.io().copy().from(Paths.get(pp)
                                    .resolve(ws.locations().getDefaultIdBasedir(id))
                                    .resolve(idFileName)
                            ).to(jarFile).run();
                            return;
                        } catch (Exception ex) {
                            //ignore
                        }
                    }
                }
                throw new NutsIllegalArgumentException(ws, "Unable to load " + id);
            }
        }
    }

    public void onPreUpdateConfig(String confName, NutsUpdateOptions options) {
        options = CoreNutsUtils.validate(options, ws);
        preUpdateConfigStoreLocations = new NutsStoreLocationsMap(currentConfig.getStoreLocations());
    }

    public void onPostUpdateConfig(String confName, NutsUpdateOptions options) {
        options = CoreNutsUtils.validate(options, ws);
        preUpdateConfigStoreLocations = new NutsStoreLocationsMap(currentConfig.getStoreLocations());
        DefaultNutsWorkspaceCurrentConfig d = (DefaultNutsWorkspaceCurrentConfig) currentConfig;
        d.setUserStoreLocations(new NutsHomeLocationsMap(storeModelBoot.getStoreLocations()).toMapOrNull());
        d.setHomeLocations(new NutsHomeLocationsMap(storeModelBoot.getHomeLocations()).toMapOrNull());
        d.build(ws.locations().getWorkspaceLocation());
        NutsStoreLocationsMap newSL = new NutsStoreLocationsMap(currentConfig.getStoreLocations());
        for (NutsStoreLocation sl : NutsStoreLocation.values()) {
            String oldPath = preUpdateConfigStoreLocations.get(sl);
            String newPath = newSL.get(sl);
            if (!oldPath.equals(newPath)) {
                Path oldPathObj = Paths.get(oldPath);
                if (Files.exists(oldPathObj)) {
                    CoreIOUtils.copyFolder(oldPathObj, Paths.get(newPath));
                }
            }
        }
        fireConfigurationChanged(confName, options.getSession(), ConfigEventType.API);
    }

    private void onLoadWorkspaceError(Throwable ex) {
        NutsWorkspaceConfigManager wconfig = this;
        Path file = ws.locations().getWorkspaceLocation().resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        if (wconfig.isReadOnly()) {
            throw new UncheckedIOException("Unable to load config file " + file.toString(), new IOException(ex));
        }
        String fileName = "nuts-workspace-" + Instant.now().toString();
        LOG.with().level(Level.SEVERE).verb(NutsLogVerb.FAIL).log("Erroneous config file. Unable to load file {0} : {1}", new Object[]{file, ex.toString()});
        Path logError = ws.locations().getStoreLocation(ws.getApiId(), NutsStoreLocation.LOG).resolve("invalid-config");
        try {
            Files.createDirectories(logError);
        } catch (IOException ex1) {
            throw new UncheckedIOException("Unable to log workspace error while loading config file " + file.toString() + " : " + ex1.toString(), new IOException(ex));
        }
        Path newfile = logError.resolve(fileName + ".json");
        LOG.with().level(Level.SEVERE).verb(NutsLogVerb.FAIL).log("Erroneous config file will be replaced by a fresh one. Old config is copied to {0}", newfile.toString());
        try {
            Files.move(file, newfile);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to load and re-create config file " + file.toString() + " : " + e.toString(), new IOException(ex));
        }

        try (PrintStream o = new PrintStream(logError.resolve(fileName + ".error").toFile())) {
            o.println("workspace.path:");
            o.println(ws.locations().getWorkspaceLocation());
            o.println("workspace.options:");
            o.println(wconfig.getOptions().format().setCompact(false).setRuntime(true).setInit(true).setExported(true).getBootCommandLine());
            for (NutsStoreLocation location : NutsStoreLocation.values()) {
                o.println("location." + location.id() + ":");
                o.println(ws.locations().getStoreLocation(location));
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

    private NutsWorkspaceConfigBoot parseBootConfig() {
        return parseBootConfig(ws.locations().getWorkspaceLocation());
    }

    private NutsWorkspaceConfigBoot parseBootConfig(Path path) {
        Path file = path.resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(file);
        if (bytes == null) {
            return null;
        }
        try {
            Map<String, Object> a_config0 = ws.formats().json().parse(bytes, Map.class);
            String version = (String) a_config0.get("configVersion");
            if (version == null) {
                version = (String) a_config0.get("createApiVersion");
                if (version == null) {
                    version = Nuts.getVersion();
                }
            }
            return createNutsVersionCompat(version).parseConfig(bytes);
        } catch (Exception ex) {
            LOG.with().level(Level.SEVERE).verb(NutsLogVerb.FAIL).log("Erroneous config file. Unable to load file {0} : {1}", new Object[]{file, ex.toString()});
            throw new UncheckedIOException("Unable to load config file " + file.toString(), new IOException(ex));
        }
    }

    private NutsVersionCompat createNutsVersionCompat(String apiVersion) {
        int buildNumber = CoreNutsUtils.getApiVersionOrdinalNumber(apiVersion);
        if (buildNumber < 506) {
            return new NutsVersionCompat502(ws, apiVersion);
        } else if (buildNumber <= 506) {
            return new NutsVersionCompat506(ws, apiVersion);
        } else {
            return new NutsVersionCompat507(ws, apiVersion);
        }
    }

    public Collection<String> resolveBootRepositories() {
        if (parsedBootRepositories != null) {
            return parsedBootRepositories;
        }
        String bootRepositories = options.getBootRepositories();
        LinkedHashSet<String> repos = new LinkedHashSet<>();
        for (String s : CoreStringUtils.split(bootRepositories, ",;", true)) {
            if (s.trim().length() > 0) {
                repos.add(s);
            }
        }
        if (repos.isEmpty()) {
            if (!NO_M2) {
                repos.add("maven-local");
            }
//            repos.add("vpc-public-nuts");
//            repos.add("vpc-public-maven");
            repos.add("maven-central");
        }
        return parsedBootRepositories = repos;
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
        public Map<String, String> getStoreLocations() {
            return getStoredConfigBoot().getStoreLocations();
        }

        @Override
        public Map<String, String> getHomeLocations() {
            return getStoredConfigBoot().getHomeLocations();
        }

        @Override
        public String getStoreLocation(NutsStoreLocation folderType) {
            return new NutsStoreLocationsMap(getStoredConfigBoot().getStoreLocations()).get(folderType);
        }

        @Override
        public String getHomeLocation(NutsOsFamily layout, NutsStoreLocation folderType) {
            return new NutsHomeLocationsMap(getStoredConfigBoot().getHomeLocations()).get(layout, folderType);
        }

        @Override
        public NutsId getApiId() {
            String v = getStoredConfigApi().getApiVersion();
            return v == null ? null : CoreNutsUtils.parseNutsId(NutsConstants.Ids.NUTS_API + "#" + v);
        }

        @Override
        public NutsId getRuntimeId() {
            String v = getStoredConfigApi().getRuntimeId();
            return v == null ? null : v.contains("#")
                    ? CoreNutsUtils.parseNutsId(v)
                    : CoreNutsUtils.parseNutsId(NutsConstants.Ids.NUTS_RUNTIME + "#" + v);
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

}
