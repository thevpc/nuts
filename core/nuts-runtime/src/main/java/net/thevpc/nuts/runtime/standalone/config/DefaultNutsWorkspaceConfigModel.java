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

import net.thevpc.nuts.runtime.core.repos.NutsRepositorySelector;
import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.CoreNutsConstants;
import net.thevpc.nuts.runtime.core.config.NutsRepositoryConfigManagerExt;
import net.thevpc.nuts.runtime.core.events.DefaultNutsWorkspaceEvent;
import net.thevpc.nuts.runtime.standalone.*;
import net.thevpc.nuts.runtime.standalone.bridges.maven.MavenUtils;
import net.thevpc.nuts.runtime.core.model.CoreNutsWorkspaceOptions;
import net.thevpc.nuts.NutsLogVerb;
import net.thevpc.nuts.runtime.standalone.DefaultNutsWorkspace;
import net.thevpc.nuts.runtime.standalone.config.compat.CompatUtils;
import net.thevpc.nuts.runtime.standalone.config.compat.NutsVersionCompat;
import net.thevpc.nuts.runtime.standalone.config.compat.v502.NutsVersionCompat502;
import net.thevpc.nuts.runtime.standalone.config.compat.v506.NutsVersionCompat506;
import net.thevpc.nuts.runtime.standalone.config.compat.v507.NutsVersionCompat507;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.spi.NutsIndexStoreFactory;
import net.thevpc.nuts.spi.NutsRepositoryFactoryComponent;
import net.thevpc.nuts.spi.NutsWorkspaceArchetypeComponent;

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
import net.thevpc.nuts.runtime.core.app.DefaultNutsWorkspaceLocationManager;
import net.thevpc.nuts.runtime.core.util.CoreBooleanUtils;
import net.thevpc.nuts.runtime.core.util.CoreNutsDependencyUtils;

/**
 * @author thevpc
 */
public class DefaultNutsWorkspaceConfigModel {

    public static final boolean NO_M2 = CoreBooleanUtils.getSysBoolNutsProperty("no-m2", false);
    private final DefaultNutsWorkspace ws;
    private final Map<String, NutsUserConfig> configUsers = new LinkedHashMap<>();
    private NutsLogger LOG;
    protected NutsWorkspaceConfigBoot storeModelBoot = new NutsWorkspaceConfigBoot();
    protected NutsWorkspaceConfigApi storeModelApi = new NutsWorkspaceConfigApi();
    protected NutsWorkspaceConfigRuntime storeModelRuntime = new NutsWorkspaceConfigRuntime();
    protected NutsWorkspaceConfigSecurity storeModelSecurity = new NutsWorkspaceConfigSecurity();
    protected NutsWorkspaceConfigMain storeModelMain = new NutsWorkspaceConfigMain();
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
//    private Set<String> excludedRepositoriesSet = new HashSet<>();
    private NutsStoreLocationsMap preUpdateConfigStoreLocations;
    private NutsRepositorySelector.SelectorList parsedBootRepositoriesList;
//    private NutsRepositorySelector[] parsedBootRepositoriesArr;

    public DefaultNutsWorkspaceConfigModel(final DefaultNutsWorkspace ws, NutsWorkspaceInitInformation initOptions) {
        this.ws = ws;
        this.initOptions = initOptions;
        this.options = this.initOptions.getOptions();
        this.bootClassLoader = initOptions.getClassWorldLoader() == null ? Thread.currentThread().getContextClassLoader() : initOptions.getClassWorldLoader();
        this.bootClassWorldURLs = initOptions.getClassWorldURLs() == null ? null : Arrays.copyOf(initOptions.getClassWorldURLs(), initOptions.getClassWorldURLs().length);
//        this.excludedRepositoriesSet = this.options.getExcludedRepositories() == null ? null : new HashSet<>(CoreStringUtils.split(Arrays.asList(this.options.getExcludedRepositories()), " ,;"));
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = session.getWorkspace().log().of(DefaultNutsWorkspaceConfigModel.class);
        }
        return LOG;
    }

    public DefaultNutsWorkspaceCurrentConfig getCurrentConfig() {
        return currentConfig;
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
        NutsWorkspace ws = session.getWorkspace();
        NutsWorkspaceUtils.of(session).checkReadOnly();
        NutsWorkspaceUtils.checkSession(ws, session);
        boolean ok = false;
        ws.security().checkAllowed(NutsConstants.Permissions.SAVE, "save");
        String apiVersionSpecificLocation = ws.locations().getStoreLocation(ws.getApiId(), NutsStoreLocation.CONFIG);
        if (force || storeModelBootChanged) {

            Path file = Paths.get(ws.locations().getWorkspaceLocation()).resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
            storeModelBoot.setConfigVersion(DefaultNutsWorkspace.VERSION_WS_CONFIG_BOOT);
            if (storeModelBoot.getExtensions() != null) {
                for (NutsWorkspaceConfigBoot.ExtensionConfig extension : storeModelBoot.getExtensions()) {
                    //inherited
                    extension.setConfigVersion(null);
                }
            }
            ws.elem().setContentType(NutsContentType.JSON).setValue(storeModelBoot).print(file);
            storeModelBootChanged = false;
            ok = true;
        }

        String configVersionSpecificLocation = ws.locations().getStoreLocation(ws.getApiId().builder().setVersion(NutsConstants.Versions.RELEASE).build(), NutsStoreLocation.CONFIG);
        if (force || storeModelSecurityChanged) {
            storeModelSecurity.setUsers(configUsers.isEmpty() ? null : configUsers.values().toArray(new NutsUserConfig[0]));

            Path file = Paths.get(configVersionSpecificLocation).resolve(CoreNutsConstants.Files.WORKSPACE_SECURITY_CONFIG_FILE_NAME);
            storeModelSecurity.setConfigVersion(current().getApiVersion());
            if (storeModelSecurity.getUsers() != null) {
                for (NutsUserConfig extension : storeModelSecurity.getUsers()) {
                    //inherited
                    extension.setConfigVersion(null);
                }
            }
            ws.elem().setSession(session).setContentType(NutsContentType.JSON).setValue(storeModelSecurity).print(file);
            storeModelSecurityChanged = false;
            ok = true;
        }

        if (force || storeModelMainChanged) {
            List<NutsSdkLocation> plainSdks = new ArrayList<>();
            plainSdks.addAll(Arrays.asList(ws.sdks().find(null, null)));
            storeModelMain.setSdk(plainSdks);
            storeModelMain.setRepositories(new ArrayList<>(
                    Arrays.stream(ws.repos().getRepositories()).filter(x -> !x.config().isTemporary())
                            .map(x -> x.config().getRepositoryRef()).collect(Collectors.toList())
            ));

            Path file = Paths.get(configVersionSpecificLocation).resolve(CoreNutsConstants.Files.WORKSPACE_MAIN_CONFIG_FILE_NAME);
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
            if (storeModelMain.getSdk() != null) {
                for (NutsSdkLocation item : storeModelMain.getSdk()) {
                    //inherited
                    item.setConfigVersion(null);
                }
            }
            ws.elem().setSession(session).setContentType(NutsContentType.JSON).setValue(storeModelMain).print(file);
            storeModelMainChanged = false;
            ok = true;
        }

        if (force || storeModelApiChanged) {
            Path afile = Paths.get(apiVersionSpecificLocation).resolve(NutsConstants.Files.WORKSPACE_API_CONFIG_FILE_NAME);
            storeModelApi.setConfigVersion(current().getApiVersion());
            if (storeModelSecurity.getUsers() != null) {
                for (NutsUserConfig item : storeModelSecurity.getUsers()) {
                    //inherited
                    item.setConfigVersion(null);
                }
            }
            ws.elem().setSession(session).setContentType(NutsContentType.JSON).setValue(storeModelApi).print(afile);
            storeModelApiChanged = false;
            ok = true;
        }
        if (force || storeModelRuntimeChanged) {
            Path runtimeVersionSpecificLocation = Paths.get(ws.locations().getStoreLocation(NutsStoreLocation.CONFIG))
                    .resolve(NutsConstants.Folders.ID).resolve(ws.locations().getDefaultIdBasedir(ws.getRuntimeId()));
            Path afile = runtimeVersionSpecificLocation.resolve(NutsConstants.Files.WORKSPACE_RUNTIME_CONFIG_FILE_NAME);
            storeModelRuntime.setConfigVersion(current().getApiVersion());
            ws.elem().setSession(session).setContentType(NutsContentType.JSON).setValue(storeModelRuntime).print(afile);
            storeModelRuntimeChanged = false;
            ok = true;
        }
        NutsException error = null;
        for (NutsRepository repo : ws.repos().getRepositories()) {
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
            lastConfigPath = Nuts.getPlatformHomeFolder(null, null, null,
                    global,
                    CoreNutsUtils.resolveValidWorkspaceName(effWorkspaceName));
            lastConfigLoaded = parseBootConfig(lastConfigPath, session);
            defaultLocation = true;
            return new DefaultNutsWorkspaceBootConfig(session, _ws0, lastConfigPath, effWorkspaceName, defaultLocation, lastConfigLoaded);
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

                NutsWorkspaceConfigBoot configLoaded = parseBootConfig(lastConfigPath, session);
                if (configLoaded == null) {
                    //not loaded
                    break;
                }
                if (NutsUtilStrings.isBlank(configLoaded.getWorkspace())) {
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
                    ? Nuts.getPlatformHomeFolder(
                            null, null, null,
                            global,
                            CoreNutsUtils.resolveValidWorkspaceName(_ws)
                    ) : CoreIOUtils.getAbsolutePath(_ws);

            lastConfigLoaded = parseBootConfig(lastConfigPath, session);
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

    public NutsWorkspaceOptions options() {
        return getOptions();
    }

    public boolean isExcludedExtension(String extensionId, NutsWorkspaceOptions options, NutsSession session) {
        if (extensionId != null && options != null) {
            NutsId pnid = session.getWorkspace().id().parser().parse(extensionId);
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

    public NutsWorkspaceOptions getOptions() {
        return options.copy();
    }

    public NutsId createContentFaceId(NutsId id, NutsDescriptor desc) {
        Map<String, String> q = id.getProperties();
        q.put(NutsConstants.IdProperties.PACKAGING, NutsUtilStrings.trim(desc.getPackaging()));
//        q.put(NutsConstants.QUERY_EXT,NutsUtilStrings.trim(descriptor.getExt()));
        q.put(NutsConstants.IdProperties.FACE, NutsConstants.QueryFaces.CONTENT);
        return id.builder().setProperties(q).build();
    }

    public NutsWorkspaceListManager createWorkspaceListManager(String name, NutsSession session) {
        return new DefaultNutsWorkspaceListManager(ws, session, name);
    }

    //    
//    public void setBootConfig(NutsBootConfig other) {
//        if (other == null) {
//            other = new NutsBootConfig();
//        }
//        if (!NutsUtilStrings.isBlank(other.getRuntimeId())) {
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
        if (NutsUtilStrings.isBlank(repositoryType)) {
            repositoryType = NutsConstants.RepoTypes.NUTS;
        }
        return session.getWorkspace().extensions().createAllSupported(NutsRepositoryFactoryComponent.class,
                new NutsRepositoryConfig().setType(repositoryType)).size() > 0;
    }

    public NutsAddRepositoryOptions[] getDefaultRepositories(NutsSession session) {
//        session = NutsWorkspaceUtils.of(getWorkspace()).validateSession(session);
        List<NutsAddRepositoryOptions> all = new ArrayList<>();
        for (NutsRepositoryFactoryComponent provider : session.getWorkspace().extensions()
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
        for (NutsWorkspaceArchetypeComponent extension : session.getWorkspace().extensions()
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
    public String resolveRepositoryPath(String repositoryLocation, NutsSession session) {
        String root = this.getRepositoriesRoot(session);
        return Paths.get(session.getWorkspace().io().expandPath(repositoryLocation,
                root != null ? root : Paths.get(session.getWorkspace().locations().getStoreLocation(NutsStoreLocation.CONFIG))
                                .resolve(NutsConstants.Folders.REPOSITORIES).toString())).toString();
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
//            return session.getWorkspace().id().parser().parseList(NutsConstants.Ids.NUTS_API + "#" + Nuts.getVersion());
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

    public void setCurrentConfig(DefaultNutsWorkspaceCurrentConfig currentConfig) {
        this.currentConfig = currentConfig;
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
        Path apiConfigFile = Paths.get(session.getWorkspace().locations().getStoreLocation(apiId, NutsStoreLocation.CONFIG))
                .resolve(NutsConstants.Files.WORKSPACE_API_CONFIG_FILE_NAME);
        if (force || !Files.isRegularFile(apiConfigFile)) {
            Map<String, Object> m = new LinkedHashMap<>();
            if (runtimeId == null) {
                runtimeId = session.getWorkspace().search().addId(NutsConstants.Ids.NUTS_RUNTIME)
                        .setRuntime(true)
                        .setTargetApiVersion(apiId.getVersion())
                        .setFailFast(false).setLatest(true).getResultIds().first();
            }
            if (runtimeId == null) {
                runtimeId = MavenUtils.of(session).resolveLatestMavenId(session.getWorkspace().id().parser().parse(NutsConstants.Ids.NUTS_RUNTIME),
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
            session.getWorkspace().elem().setContentType(NutsContentType.JSON).setValue(m).print(apiConfigFile);
        }
        downloadId(apiId, force, null, true, session);
    }

    public void prepareBootRuntime(NutsId id, boolean force, NutsSession session) {
        prepareBootRuntimeOrExtension(id, force, true, session);
    }

    public void prepareBootExtension(NutsId id, boolean force, NutsSession session) {
        prepareBootRuntimeOrExtension(id, force, false, session);
    }

    public void prepareBoot(boolean force, NutsSession session) {
        prepareBootApi(ws.getApiId(), ws.getRuntimeId(), force, session);
        prepareBootRuntime(ws.getRuntimeId(), force, session);
        List<NutsWorkspaceConfigBoot.ExtensionConfig> extensions = getStoredConfigBoot().getExtensions();
        if (extensions != null) {
            for (NutsWorkspaceConfigBoot.ExtensionConfig extension : extensions) {
                if (extension.isEnabled()) {
                    prepareBootExtension(extension.getId(), force, session);
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
                cconfig.setApiId(session.getWorkspace().id().parser().parse(NutsConstants.Ids.NUTS_API + "#" + initOptions.getApiVersion()));
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
            cconfig.merge(options(), session);

            setCurrentConfig(cconfig.build(session.getWorkspace().locations().getWorkspaceLocation(), session));

            NutsVersionCompat compat = createNutsVersionCompat(Nuts.getVersion());
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
                cconfig.setApiId(session.getWorkspace().id().parser().parse(NutsConstants.Ids.NUTS_API + "#" + initOptions.getApiVersion()));
                cconfig.setRuntimeId(initOptions.getRuntimeId() == null ? null : initOptions.getRuntimeId().toString(), session);
                cconfig.setRuntimeBootDescriptor(initOptions.getRuntimeBootDescriptor());
                cconfig.setExtensionBootDescriptors(initOptions.getExtensionBootDescriptors());
                cconfig.setBootRepositories(initOptions.getBootRepositories());
            }
            setCurrentConfig(cconfig
                    .build(session.getWorkspace().locations().getWorkspaceLocation(), session)
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
            if (session.getWorkspace().env().getBootOptions().isRecover()) {
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
        ((DefaultImportManager) session.getWorkspace().imports()).getModel().invalidateCache();
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
        for (NutsWorkspaceListener workspaceListener : session.getWorkspace().events().getWorkspaceListeners()) {
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

    public String getRepositoriesRoot(NutsSession session) {
        return Paths.get(session.getWorkspace().locations().getStoreLocation(NutsStoreLocation.CONFIG)).resolve(NutsConstants.Folders.REPOSITORIES).toString();
    }

    public String getTempRepositoriesRoot(NutsSession session) {
        return Paths.get(session.getWorkspace().locations().getStoreLocation(NutsStoreLocation.TEMP)).resolve(NutsConstants.Folders.REPOSITORIES).toString();
    }

    public boolean isValidWorkspaceFolder(NutsSession session) {
        Path file = Paths.get(session.getWorkspace().locations().getWorkspaceLocation()).resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        return Files.isRegularFile(file);
    }

    public NutsAuthenticationAgent createAuthenticationAgent(String authenticationAgent, NutsSession session) {
        NutsWorkspace ws = session.getWorkspace();
        authenticationAgent = NutsUtilStrings.trim(authenticationAgent);
        NutsAuthenticationAgent supported = null;
        if (authenticationAgent.isEmpty()) {
            supported = ws.extensions().createSupported(NutsAuthenticationAgent.class, "");
        } else {
            List<NutsAuthenticationAgent> agents = ws.extensions().createAllSupported(NutsAuthenticationAgent.class, authenticationAgent);
            for (NutsAuthenticationAgent agent : agents) {
                if (agent.getId().equals(authenticationAgent)) {
                    supported = agent;
                }
            }
        }
        if (supported == null) {
            throw new NutsExtensionNotFoundException(session, NutsAuthenticationAgent.class, "AuthenticationAgent");
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
            indexStoreClientFactory = session.getWorkspace().extensions().createSupported(NutsIndexStoreFactory.class, null);
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
        DefaultNutsSdkManager d = (DefaultNutsSdkManager) session.getWorkspace().sdks();
        d.getModel().setSdks(this.storeModelMain.getSdk().toArray(new NutsSdkLocation[0]), session);
        NutsRepositoryManager repos = session.getWorkspace().repos();
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
        if (NutsUtilStrings.isBlank(config.getUuid())) {
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
                        + ((DefaultNutsWorkspaceLocationManager) ws.locations()).getModel().getWorkspaceLocation() + '\''))
                + '}';
    }

    public void prepareBootRuntimeOrExtension(NutsId id, boolean force, boolean runtime, NutsSession session) {
        NutsWorkspaceUtils.checkSession(ws, session);
        NutsWorkspace ws = session.getWorkspace();
        Path configFile = Paths.get(ws.locations().getStoreLocation(NutsStoreLocation.CACHE))
                .resolve(NutsConstants.Folders.ID).resolve(ws.locations().getDefaultIdBasedir(id)).resolve(runtime
                ? NutsConstants.Files.WORKSPACE_RUNTIME_CACHE_FILE_NAME
                : NutsConstants.Files.WORKSPACE_EXTENSION_CACHE_FILE_NAME
        );
        Path jarFile = Paths.get(ws.locations().getStoreLocation(NutsStoreLocation.LIB))
                .resolve(NutsConstants.Folders.ID).resolve(ws.locations().getDefaultIdBasedir(id))
                .resolve(ws.locations().getDefaultIdFilename(id.builder().setFaceContent().setPackaging("jar").build()));
        if (!force && (Files.isRegularFile(configFile) && Files.isRegularFile(jarFile))) {
            return;
        }
        List<NutsId> deps = new ArrayList<>();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id.getLongName());

        NutsDefinition def = ws.fetch().setId(id).setDependencies(true)
                //
                .setOptional(false)
                .addScope(NutsDependencyScopePattern.RUN)
                .setDependencyFilter(CoreNutsDependencyUtils.createJavaRunDependencyFilter(session))
                //
                .setContent(true)
                .setFailFast(false)
                .setSession(CoreNutsUtils.silent(session))
                .getResultDefinition();
        if (def == null) {
            _LOGOP(session).level(Level.CONFIG)
                    .verb(NutsLogVerb.WARNING).log("selected repositories ({0}) cannot reach runtime package. fallback to default.",
                    Arrays.stream(ws.repos().setSession(session).getRepositories()).map(NutsRepository::getName).collect(Collectors.joining(", "))
            );
            HashMap<String, String> defaults = new HashMap<>();
            MavenUtils.DepsAndRepos dd = MavenUtils.of(session).loadDependenciesAndRepositoriesFromPomPath(id,
                    resolveBootRepositoriesList().resolveSelectors(defaults),
                    session);
            if (dd == null) {
                throw new NutsNotFoundException(session, id);
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
                    def.getDependencies().stream().map(x -> x.toId().getLongName()).collect(Collectors.joining(";"))
            );
            if (runtime) {
                m.put("bootRepositories", def.getDescriptor().getProperties().get("nuts-runtime-repositories"));
            }
        }

        if (force || !Files.isRegularFile(configFile)) {
            ws.elem().setContentType(NutsContentType.JSON).setValue(m).print(configFile);
        }
        downloadId(id, force, (def != null && def.getContent().getPath() != null) ? def.getContent().getFilePath() : null, false, session);
        for (NutsId dep : deps) {
            downloadId(dep, force, null, true, session);
        }
    }

    //    
//    public String getRuntimeDependencies() {
//        return current().getRuntimeDependencies();
//    }
//
//    
//    public String getExtensionDependencies() {
//        return current().getExtensionDependencies();
//    }
    private void downloadId(NutsId id, boolean force, Path path, boolean fetch, NutsSession session) {
        NutsWorkspaceUtils.checkSession(ws, session);
        NutsWorkspace ws = session.getWorkspace();
        String idFileName = ws.locations().getDefaultIdFilename(id.builder().setFaceContent().setPackaging("jar").build());
        Path jarFile = Paths.get(ws.locations().getStoreLocation(NutsStoreLocation.LIB))
                .resolve(NutsConstants.Folders.ID).resolve(ws.locations().getDefaultIdBasedir(id))
                .resolve(idFileName);
        if (force || !Files.isRegularFile(jarFile)) {
            if (path != null) {
                ws.io().copy().from(path).to(jarFile).setSession(session).run();
            } else {
                if (fetch) {
                    NutsDefinition def = ws.fetch().setId(id).setDependencies(true)
                            //
                            .setOptional(false)
                            .addScope(NutsDependencyScopePattern.RUN)
                            .setDependencyFilter(CoreNutsDependencyUtils.createJavaRunDependencyFilter(session))
                            //
                            .setSession(session.copy().setTrace(false))
                            .setContent(true)
                            .setFailFast(false)
                            .getResultDefinition();
                    if (def != null && def.getPath() != null) {
                        ws.io().copy().from(def.getPath()).to(jarFile).setSession(session).run();
                        return;
                    }
                }
                for (NutsRepositorySelector.Selection pp0 : resolveBootRepositoriesList().resolveSelectors(null)) {
                    NutsAddRepositoryOptions opt = NutsRepositorySelector.createRepositoryOptions(pp0, false, session);
                    String pp = opt.getConfig() == null ? opt.getLocation() : opt.getConfig().getLocation();
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
                throw new NutsIllegalArgumentException(session,
                        NutsMessage.cstyle("unable to load %s", id)
                );
            }
        }
    }

    public void onPreUpdateConfig(String confName, NutsSession session) {
//        options = CoreNutsUtils.validate(options, ws);
        preUpdateConfigStoreLocations = new NutsStoreLocationsMap(currentConfig.getStoreLocations());
    }

    public void onPostUpdateConfig(String confName, NutsSession session) {
//        options = CoreNutsUtils.validate(options, ws);
        preUpdateConfigStoreLocations = new NutsStoreLocationsMap(currentConfig.getStoreLocations());
        DefaultNutsWorkspaceCurrentConfig d = (DefaultNutsWorkspaceCurrentConfig) currentConfig;
        d.setUserStoreLocations(new NutsHomeLocationsMap(storeModelBoot.getStoreLocations()).toMapOrNull());
        d.setHomeLocations(new NutsHomeLocationsMap(storeModelBoot.getHomeLocations()).toMapOrNull());
        d.build(ws.locations().getWorkspaceLocation(), session);
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
        fireConfigurationChanged(confName, session, ConfigEventType.API);
    }

    private void onLoadWorkspaceError(Throwable ex, NutsSession session) {
        DefaultNutsWorkspaceConfigModel wconfig = this;
        Path file = Paths.get(ws.locations().getWorkspaceLocation()).resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        if (wconfig.isReadOnly()) {
            throw new UncheckedIOException("unable to load config file " + file.toString(), new IOException(ex));
        }
        String fileSuffix = Instant.now().toString();
        fileSuffix = fileSuffix.replace(':', '-');
        String fileName = "nuts-workspace-" + fileSuffix;
        Path logError = Paths.get(ws.locations().getStoreLocation(ws.getApiId(), NutsStoreLocation.LOG)).resolve("invalid-config");
        Path logFile = logError.resolve(fileName + ".error");
        _LOGOP(session).level(Level.SEVERE).verb(NutsLogVerb.FAIL)
                .log("erroneous workspace config file. Unable to load file {0} : {1}", new Object[]{file, ex});
        try {
            CoreIOUtils.mkdirs(logError);
        } catch (Exception ex1) {
            throw new UncheckedIOException("unable to log workspace error while loading config file " + file.toString() + " : " + ex1.toString(), new IOException(ex));
        }
        Path newfile = logError.resolve(fileName + ".json");
        _LOGOP(session).level(Level.SEVERE).verb(NutsLogVerb.FAIL).log("erroneous workspace config file will be replaced by a fresh one. Old config is copied to {0}\n error logged to  {1}", newfile.toString(), logFile);
        try {
            Files.move(file, newfile);
        } catch (IOException e) {
            throw new UncheckedIOException("unable to load and re-create config file " + file.toString() + " : " + e.toString(), new IOException(ex));
        }

        try (PrintStream o = new PrintStream(logFile.toFile())) {
            o.println("workspace.path:");
            o.println(ws.locations().getWorkspaceLocation());
            o.println("workspace.options:");
            o.println(wconfig.getOptions().formatter().setCompact(false).setRuntime(true).setInit(true).setExported(true).getBootCommandLine());
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

    private NutsWorkspaceConfigBoot parseBootConfig(NutsSession session) {
        return parseBootConfig(ws.locations().getWorkspaceLocation(), session);
    }

    private NutsWorkspaceConfigBoot parseBootConfig(String path, NutsSession session) {
        Path file = Paths.get(path).resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(file);
        if (bytes == null) {
            return null;
        }
        try {
            Map<String, Object> a_config0 = ws.elem().setSession(session).setContentType(NutsContentType.JSON).parse(bytes, Map.class);
            String version = (String) a_config0.get("configVersion");
            if (version == null) {
                version = (String) a_config0.get("createApiVersion");
                if (version == null) {
                    version = Nuts.getVersion();
                }
            }
            return createNutsVersionCompat(version).parseConfig(bytes, session);
        } catch (Exception ex) {
            _LOGOP(session).level(Level.SEVERE).verb(NutsLogVerb.FAIL)
                    .log("erroneous workspace config file. Unable to load file {0} : {1}",
                            new Object[]{file, ex});
            throw new UncheckedIOException("unable to load config file " + file.toString(), new IOException(ex));
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
            NutsWorkspace ws = NutsWorkspaceUtils.defaultSession(DefaultNutsWorkspaceConfigModel.this.ws).getWorkspace();

            return v == null ? null
                    : ws
                            .id().parser().parse(NutsConstants.Ids.NUTS_API + "#" + v);
        }

        @Override
        public NutsId getRuntimeId() {
            String v = getStoredConfigApi().getRuntimeId();
            NutsWorkspace ws = NutsWorkspaceUtils.defaultSession(DefaultNutsWorkspaceConfigModel.this.ws).getWorkspace();
            return v == null ? null : v.contains("#")
                    ? ws.id().parser().parse(v)
                    : ws.id().parser().parse(NutsConstants.Ids.NUTS_RUNTIME + "#" + v);
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
