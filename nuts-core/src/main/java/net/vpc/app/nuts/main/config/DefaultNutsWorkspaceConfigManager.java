/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.main.config;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.config.NutsWorkspaceConfigManagerExt;
import net.vpc.app.nuts.main.DefaultNutsWorkspace;
import net.vpc.app.nuts.main.config.compat.CompatUtils;
import net.vpc.app.nuts.main.config.compat.NutsVersionCompat;
import net.vpc.app.nuts.main.config.compat.v502.NutsVersionCompat502;
import net.vpc.app.nuts.main.config.compat.v506.NutsVersionCompat506;
import net.vpc.app.nuts.main.config.compat.v507.NutsVersionCompat507;
import net.vpc.app.nuts.main.repos.NutsRepositoryRegistryHelper;
import net.vpc.app.nuts.main.repos.NutsSimpleRepositoryWrapper;
import net.vpc.app.nuts.runtime.*;
import net.vpc.app.nuts.runtime.bridges.maven.MavenUtils;
import net.vpc.app.nuts.runtime.log.NutsLogVerb;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;
import net.vpc.app.nuts.runtime.util.NutsJavaSdkUtils;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.runtime.util.common.CoreCommonUtils;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;
import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;
import net.vpc.app.nuts.runtime.wscommands.CommandNutsWorkspaceCommandFactory;
import net.vpc.app.nuts.runtime.wscommands.ConfigNutsWorkspaceCommandFactory;
import net.vpc.app.nuts.runtime.wscommands.DefaultNutsWorkspaceCommandAlias;

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
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author vpc
 */
public class DefaultNutsWorkspaceConfigManager implements NutsWorkspaceConfigManagerExt {
    public static final boolean NO_M2 = CoreCommonUtils.getSysBoolNutsProperty("no-m2", false);

    public static NutsLogger LOG;

    private final DefaultNutsWorkspace ws;
    private final List<NutsWorkspaceCommandFactory> commandFactories = new ArrayList<>();
    private final ConfigNutsWorkspaceCommandFactory defaultCommandFactory;
    private final Map<String, List<NutsSdkLocation>> configSdks = new LinkedHashMap<>();
    private final Map<String, NutsUserConfig> configUsers = new LinkedHashMap<>();
    private final NutsRepositoryRegistryHelper repositoryRegistryHelper;
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
    private Path workspaceLocation;
    private NutsWorkspaceOptions options;
    private NutsWorkspaceInitInformation initOptions;
    private long startCreateTime;
    private long endCreateTime;
    private NutsIndexStoreFactory indexStoreClientFactory;
    private Set<String> cachedImports;
    private Set<String> excludedRepositoriesSet = new HashSet<>();
    private NutsStoreLocationsMap preUpdateConfigStoreLocations;
    private Set<String> parsedBootRepositories;

    public DefaultNutsWorkspaceConfigManager(final DefaultNutsWorkspace ws, NutsWorkspaceInitInformation initOptions) {
        this.ws = ws;
        defaultCommandFactory = new ConfigNutsWorkspaceCommandFactory(this);
        LOG = ws.log().of(DefaultNutsWorkspaceConfigManager.class);
        repositoryRegistryHelper = new NutsRepositoryRegistryHelper(ws);
        this.workspaceLocation = Paths.get(initOptions.getWorkspaceLocation());
        this.initOptions = initOptions;
        this.options = this.initOptions.getOptions();
        this.bootClassLoader = initOptions.getClassWorldLoader() == null ? Thread.currentThread().getContextClassLoader() : initOptions.getClassWorldLoader();
        this.bootClassWorldURLs = initOptions.getClassWorldURLs() == null ? null : Arrays.copyOf(initOptions.getClassWorldURLs(), initOptions.getClassWorldURLs().length);
        this.excludedRepositoriesSet = this.options.getExcludedRepositories() == null ? null : new HashSet<>(CoreStringUtils.split(Arrays.asList(this.options.getExcludedRepositories()), " ,;"));
    }

    @Override
    public String name() {
        return getName();
    }

    @Override
    public String getName() {
        return getWorkspaceLocation().getFileName().toString();
    }

    @Override
    public String getUuid() {
        return storeModelBoot.getUuid();
    }

    @Override
    public Map<String, String> getEnv() {
        Map<String, String> p = new LinkedHashMap<>();
        if (storeModelMain.getEnv() != null) {
            p.putAll(storeModelMain.getEnv());
        }
        return p;
    }

    @Override
    public String getEnv(String property, String defaultValue) {
        Map<String, String> env = storeModelMain.getEnv();
        if (env == null) {
            return defaultValue;
        }
        String o = env.get(property);
        if (CoreStringUtils.isBlank(o)) {
            return defaultValue;
        }
        return o;
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
    public Path getWorkspaceLocation() {
        return workspaceLocation;
    }

    @Override
    public boolean isReadOnly() {
        return options.isReadOnly();
    }

    @Override
    public void setEnv(String property, String value, NutsUpdateOptions options) {
        Map<String, String> env = storeModelMain.getEnv();
        options = CoreNutsUtils.validate(options, ws);
        if (CoreStringUtils.isBlank(value)) {
            if (env != null && env.containsKey(property)) {
                env.remove(property);
                fireConfigurationChanged("env", options.getSession(), ConfigEventType.MAIN);
            }
        } else {
            if (env == null) {
                env = new LinkedHashMap<>();
                storeModelMain.setEnv(env);
            }
            String old = env.get(property);
            if (!value.equals(old)) {
                env.put(property, value);
                fireConfigurationChanged("env", options.getSession(), ConfigEventType.MAIN);
            }
        }
    }

    @Override
    public void addImports(String[] importExpressions, NutsAddOptions options) {
        options = CoreNutsUtils.validate(options, ws);
        Set<String> imports = new LinkedHashSet<>();
        if (storeModelMain.getImports() != null) {
            imports.addAll(storeModelMain.getImports());
        }
        if (importExpressions != null) {
            for (String importExpression : importExpressions) {
                if (importExpression != null) {
                    for (String s : importExpression.split("[,;: ]")) {
                        imports.add(s.trim());
                    }
                }
            }
        }
        String[] arr = imports.toArray(new String[0]);
//        Arrays.sort(arr);
        setImports(arr, CoreNutsUtils.toUpdateOptions(options));
    }

    @Override
    public void removeAllImports(NutsRemoveOptions options) {
        options = CoreNutsUtils.validate(options, ws);
        setImports(null, CoreNutsUtils.toUpdateOptions(options));
    }

    @Override
    public void removeImports(String[] importExpressions, NutsRemoveOptions options) {
        options = CoreNutsUtils.validate(options, ws);
        if (storeModelMain.getImports() != null) {
            Set<String> imports = new LinkedHashSet<>();
            for (String importExpression : storeModelMain.getImports()) {
                imports.addAll(parseImports(importExpression));
            }
            if (importExpressions != null) {
                for (String importExpression : importExpressions) {
                    imports.removeAll(parseImports(importExpression));
                }
            }
            String[] arr = imports.toArray(new String[0]);
//        Arrays.sort(arr);
            setImports(arr, CoreNutsUtils.toUpdateOptions(options));
        }
    }

    @Override
    public void setImports(String[] imports, NutsUpdateOptions options) {
        options = CoreNutsUtils.validate(options, ws);
        Set<String> simports = new LinkedHashSet<>();
        if (imports != null) {
            for (String s : imports) {
                simports.addAll(parseImports(s));
            }
        }
        storeModelMain.setImports(new ArrayList<>(simports));
        fireConfigurationChanged("import", options.getSession(), ConfigEventType.MAIN);
    }

    @Override
    public Set<String> getImports() {
        if (cachedImports == null) {
            Set<String> all = new LinkedHashSet<>();
            if (storeModelMain.getImports() != null) {
                all.addAll(storeModelMain.getImports());
            }
            return cachedImports = Collections.unmodifiableSet(all);
        }
        return cachedImports;
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
        Path apiVersionSpecificLocation = getStoreLocation(getApiId(), NutsStoreLocation.CONFIG);
        if (force || storeModelBootChanged) {

            Path file = getWorkspaceLocation().resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
            storeModelBoot.setConfigVersion(current().getApiVersion());
            if (storeModelBoot.getExtensions() != null) {
                for (NutsWorkspaceConfigBoot.ExtensionConfig extension : storeModelBoot.getExtensions()) {
                    //inherited
                    extension.setConfigVersion(null);
                }
            }
            ws.json().value(storeModelBoot).print(file);
            storeModelBootChanged = false;
            ok = true;
        }

        Path configVersionSpecificLocation = getStoreLocation(getApiId().builder().setVersion(NutsConstants.Versions.RELEASE).build(), NutsStoreLocation.CONFIG);
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
            ws.json().value(storeModelSecurity).print(file);
            storeModelSecurityChanged = false;
            ok = true;
        }

        if (force || storeModelMainChanged) {
            List<NutsSdkLocation> plainSdks = new ArrayList<>();
            for (List<NutsSdkLocation> value : configSdks.values()) {
                plainSdks.addAll(value);
            }
            storeModelMain.setSdk(plainSdks);
            storeModelMain.setRepositories(new ArrayList<>(Arrays.asList(repositoryRegistryHelper.getRepositoryRefs())));

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
            ws.json().value(storeModelMain).print(file);
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
            ws.json().value(storeModelApi).print(afile);
            storeModelApiChanged = false;
            ok = true;
        }
        if (force || storeModelRuntimeChanged) {
            Path runtimeVersionSpecificLocation = getStoreLocation(NutsStoreLocation.CACHE)
                    .resolve(NutsConstants.Folders.ID).resolve(getDefaultIdBasedir(getRuntimeId()));
            Path afile = runtimeVersionSpecificLocation.resolve(NutsConstants.Files.WORKSPACE_RUNTIME_CACHE_FILE_NAME);
            storeModelRuntime.setConfigVersion(current().getApiVersion());
            ws.json().value(storeModelRuntime).print(afile);
            storeModelRuntimeChanged = false;
            ok = true;
        }
        NutsException error = null;
        for (NutsRepository repo : getRepositories(session)) {
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
    public String[] getSdkTypes() {
        Set<String> s = getSdk().keySet();
        return s.toArray(new String[0]);
    }

    @Override
    public boolean addSdk(NutsSdkLocation location, NutsAddOptions options) {
        options = CoreNutsUtils.validate(options, ws);
        if (location != null) {
            if (CoreStringUtils.isBlank(location.getProduct())) {
                throw new NutsIllegalArgumentException(ws, "Sdk Type should not be null");
            }
            if (CoreStringUtils.isBlank(location.getName())) {
                throw new NutsIllegalArgumentException(ws, "Sdk Name should not be null");
            }
            if (CoreStringUtils.isBlank(location.getVersion())) {
                throw new NutsIllegalArgumentException(ws, "Sdk Version should not be null");
            }
            if (CoreStringUtils.isBlank(location.getPath())) {
                throw new NutsIllegalArgumentException(ws, "Sdk Path should not be null");
            }
            List<NutsSdkLocation> list = getSdk().get(location.getProduct());
            if (list == null) {
                list = new ArrayList<>();
                configSdks.put(location.getProduct(), list);
            }
            NutsSdkLocation old = null;
            for (NutsSdkLocation nutsSdkLocation : list) {
                if (
                        nutsSdkLocation.getName().equals(location.getName())
                                ||
                                nutsSdkLocation.getPath().equals(location.getPath())
                ) {
                    old = nutsSdkLocation;
                    break;
                }
            }
            if (old != null) {
                return false;
            }
            list.add(location);
            fireConfigurationChanged("sdk", options.getSession(), ConfigEventType.MAIN);
            return true;
        }
        return false;
    }

    @Override
    public NutsSdkLocation removeSdk(NutsSdkLocation location, NutsRemoveOptions options) {
        options = CoreNutsUtils.validate(options, ws);
        if (location != null) {
            List<NutsSdkLocation> list = getSdk().get(location.getProduct());
            if (list != null) {
                for (Iterator<NutsSdkLocation> iterator = list.iterator(); iterator.hasNext(); ) {
                    NutsSdkLocation location2 = iterator.next();
                    if (location2.equals(location)) {
                        iterator.remove();
                        fireConfigurationChanged("sdk", options.getSession(), ConfigEventType.MAIN);
                        return location2;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public NutsSdkLocation findSdkByName(String type, String locationName, NutsSession session) {
        type = toValidSdkName(type);
        if (locationName != null) {
            List<NutsSdkLocation> list = getSdk().get(type);
            if (list != null) {
                for (NutsSdkLocation location : list) {
                    if (location.getName().equals(locationName)) {
                        return location;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public NutsSdkLocation findSdkByPath(String type, Path path, NutsSession session) {
        type = toValidSdkName(type);
        if (path != null) {
            List<NutsSdkLocation> list = getSdk().get(type);
            if (list != null) {
                for (NutsSdkLocation location : list) {
                    if (location.getPath() != null && location.getPath().equals(path.toString())) {
                        return location;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public NutsSdkLocation findSdkByVersion(String type, String version, NutsSession session) {
        type = toValidSdkName(type);
        if (version != null) {
            List<NutsSdkLocation> list = getSdk().get(type);
            if (list != null) {
                for (NutsSdkLocation location : list) {
                    if (location.getVersion().equals(version)) {
                        return location;
                    }
                }
            }
        }
        return null;
    }

//    public void setRepositoryEnabled(String repoName, boolean enabled) {
//        NutsRepositoryRef e = repositoryRegistryHelper.findRepositoryRef(repoName);
//        if (e != null && e.isEnabled() != enabled) {
//            e.setEnabled(enabled);
//            fireConfigurationChanged();
//        }
//    }

    @Override
    public NutsSdkLocation findSdk(String type, NutsSdkLocation location, NutsSession session) {
        type = toValidSdkName(type);
        if (location != null) {
            List<NutsSdkLocation> list = getSdk().get(type);
            if (list != null) {
                for (NutsSdkLocation location2 : list) {
                    if (location2.equals(location)) {
                        return location2;
                    }
                }
            }
        }
        return null;
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
    public NutsSdkLocation getSdk(String type, String requestedVersion, NutsSession session) {
        type = toValidSdkName(type);
        NutsVersionFilter javaVersionFilter = ws.version().parse(requestedVersion).filter();
        NutsSdkLocation best = null;
        session = CoreNutsUtils.checkSession(session);
        for (NutsSdkLocation jdk : getSdks(type, session)) {
            String currVersion = jdk.getVersion();
            if (javaVersionFilter.accept(DefaultNutsVersion.valueOf(currVersion), session)) {
                if (best == null || DefaultNutsVersion.compareVersions(best.getVersion(), currVersion) < 0) {
                    best = jdk;
                }
            }
        }
        return best;
    }

    @Override
    public NutsSdkLocation[] getSdks(String type, NutsSession session) {
        type = toValidSdkName(type);
        List<NutsSdkLocation> list = getSdk().get(type);
        if (list == null) {
            return new NutsSdkLocation[0];
        }
        return list.toArray(new NutsSdkLocation[0]);
    }

    @Override
    public NutsSdkLocation[] searchSdkLocations(String sdkType, NutsSession session) {
        session = NutsWorkspaceUtils.of(ws).validateSession(session);
        if ("java".equals(sdkType)) {
            try {
                return NutsJavaSdkUtils.of(session.getWorkspace()).searchJdkLocationsFuture(session).get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return new NutsSdkLocation[0];
    }

    @Override
    public NutsSdkLocation[] searchSdkLocations(String sdkType, Path path, NutsSession session) {
        session = NutsWorkspaceUtils.of(ws).validateSession(session);
        if ("java".equals(sdkType)) {
            return NutsJavaSdkUtils.of(session.getWorkspace()).searchJdkLocations(path, session);
        }
        return new NutsSdkLocation[0];
    }

    @Override
    public NutsSdkLocation resolveSdkLocation(String sdkType, Path path, String preferredName, NutsSession session) {
        session = NutsWorkspaceUtils.of(ws).validateSession(session);
        if ("java".equals(sdkType)) {
            return NutsJavaSdkUtils.of(session.getWorkspace()).resolveJdkLocation(path, null, session);
        }
        return null;
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
    public void addCommandAliasFactory(NutsCommandAliasFactoryConfig commandFactoryConfig, NutsAddOptions options) {
        options = CoreNutsUtils.validate(options, ws);
        if (commandFactoryConfig == null || commandFactoryConfig.getFactoryId() == null || commandFactoryConfig.getFactoryId().isEmpty() || !commandFactoryConfig.getFactoryId().trim().equals(commandFactoryConfig.getFactoryId())) {
            throw new NutsIllegalArgumentException(ws, "Invalid WorkspaceCommandFactory " + commandFactoryConfig);
        }
        for (NutsWorkspaceCommandFactory factory : commandFactories) {
            if (commandFactoryConfig.getFactoryId().equals(factory.getFactoryId())) {
                throw new IllegalArgumentException();
            }
        }
        NutsWorkspaceCommandFactory f = null;
        if (CoreStringUtils.isBlank(commandFactoryConfig.getFactoryType()) || "command".equals(commandFactoryConfig.getFactoryType().trim())) {
            f = new CommandNutsWorkspaceCommandFactory(ws);
        }
        if (f != null) {
            f.configure(commandFactoryConfig);
            commandFactories.add(f);
        }
        Collections.sort(commandFactories, new Comparator<NutsWorkspaceCommandFactory>() {
            @Override
            public int compare(NutsWorkspaceCommandFactory o1, NutsWorkspaceCommandFactory o2) {
                return Integer.compare(o2.getPriority(), o1.getPriority());
            }
        });
        List<NutsCommandAliasFactoryConfig> commandFactories = storeModelMain.getCommandFactories();
        if (commandFactories == null) {
            commandFactories = new ArrayList<>();
            storeModelMain.setCommandFactories(commandFactories);
        }
        NutsCommandAliasFactoryConfig oldCommandFactory = null;
        for (NutsCommandAliasFactoryConfig commandFactory : commandFactories) {
            if (f == null || commandFactory.getFactoryId().equals(f.getFactoryId())) {
                oldCommandFactory = commandFactory;
            }
        }
        if (oldCommandFactory == null) {
            commandFactories.add(commandFactoryConfig);
        } else if (oldCommandFactory != commandFactoryConfig) {
            oldCommandFactory.setFactoryId(commandFactoryConfig.getFactoryId());
            oldCommandFactory.setFactoryType(commandFactoryConfig.getFactoryType());
            oldCommandFactory.setParameters(commandFactoryConfig.getParameters() == null ? null : new LinkedHashMap<>(commandFactoryConfig.getParameters()));
            oldCommandFactory.setPriority(commandFactoryConfig.getPriority());
        }
        fireConfigurationChanged("command", options.getSession(), ConfigEventType.MAIN);
    }

    @Override
    public boolean removeCommandAliasFactory(String factoryId, NutsRemoveOptions options) {
        options = CoreNutsUtils.validate(options, ws);
        if (factoryId == null || factoryId.isEmpty()) {
            throw new NutsIllegalArgumentException(ws, "Invalid WorkspaceCommandFactory " + factoryId);
        }
        NutsWorkspaceCommandFactory removeMe = null;
        NutsCommandAliasFactoryConfig removeMeConfig = null;
        for (Iterator<NutsWorkspaceCommandFactory> iterator = commandFactories.iterator(); iterator.hasNext(); ) {
            NutsWorkspaceCommandFactory factory = iterator.next();
            if (factoryId.equals(factory.getFactoryId())) {
                removeMe = factory;
                iterator.remove();
                fireConfigurationChanged("command", options.getSession(), ConfigEventType.MAIN);
                break;
            }
        }
        List<NutsCommandAliasFactoryConfig> _commandFactories = storeModelMain.getCommandFactories();
        if (_commandFactories != null) {
            for (Iterator<NutsCommandAliasFactoryConfig> iterator = _commandFactories.iterator(); iterator.hasNext(); ) {
                NutsCommandAliasFactoryConfig commandFactory = iterator.next();
                if (factoryId.equals(commandFactory.getFactoryId())) {
                    removeMeConfig = commandFactory;
                    iterator.remove();
                    fireConfigurationChanged("command", options.getSession(), ConfigEventType.MAIN);
                    break;
                }
            }
        }
        if (removeMe == null && removeMeConfig == null) {
            throw new NutsIllegalArgumentException(ws, "Command Factory does not exists " + factoryId);
        }
        return true;
    }

    @Override
    public boolean addCommandAlias(NutsCommandAliasConfig command, NutsAddOptions options) {
        options = CoreNutsUtils.validate(options, ws);
        if (command == null
                || CoreStringUtils.isBlank(command.getName())
                || command.getName().contains(" ") || command.getName().contains(".")
                || command.getName().contains("/") || command.getName().contains("\\")
                || command.getCommand() == null
                || command.getCommand().length == 0) {
            throw new NutsIllegalArgumentException(ws, "Invalid command alias " + (command == null ? "<NULL>" : command.getName()));
        }
        boolean forced = false;
        NutsSession session = options.getSession();
        if (defaultCommandFactory.findCommand(command.getName(), ws) != null) {
            if (session.isYes()) {
                forced = true;
                removeCommandAlias(command.getName(),
                        new NutsRemoveOptions().setSession(session)
                );
            } else {
                throw new NutsIllegalArgumentException(ws, "Command alias already exists " + command.getName());
            }
        }
        defaultCommandFactory.installCommand(command, options);
        if (session.isPlainTrace()) {
            PrintStream out = CoreIOUtils.resolveOut(session);
            out.printf("[[install]] command alias ==%s==%n", command.getName());
        }
        return forced;
    }

    @Override
    public boolean removeCommandAlias(String name, NutsRemoveOptions options) {
        if (CoreStringUtils.isBlank(name)) {
            throw new NutsIllegalArgumentException(ws, "Invalid command alias " + (name == null ? "<NULL>" : name));
        }
        options = CoreNutsUtils.validate(options, ws);
        NutsSession session = options.getSession();
        NutsCommandAliasConfig command = defaultCommandFactory.findCommand(name, ws);
        if (command == null) {
            throw new NutsIllegalArgumentException(ws, "Command alias does not exists " + name);
        }
        defaultCommandFactory.uninstallCommand(name, options);
        if (session.isPlainTrace()) {
            PrintStream out = CoreIOUtils.resolveOut(session);
            out.printf("[[uninstall]] command alias ==%s==%n", name);
        }
        return true;
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
    public NutsWorkspaceCommandAlias findCommandAlias(String name, NutsSession session) {
        NutsCommandAliasConfig c = defaultCommandFactory.findCommand(name, ws);
        if (c == null) {
            for (NutsWorkspaceCommandFactory commandFactory : commandFactories) {
                c = commandFactory.findCommand(name, ws);
                if (c != null) {
                    break;
                }
            }
        }
        if (c == null) {
            return null;
        }
        return toDefaultNutsWorkspaceCommand(c);
    }

    @Override
    public List<NutsWorkspaceCommandAlias> findCommandAliases(NutsSession session) {
        HashMap<String, NutsWorkspaceCommandAlias> all = new HashMap<>();
        for (NutsCommandAliasConfig command : defaultCommandFactory.findCommands(ws)) {
            all.put(command.getName(), toDefaultNutsWorkspaceCommand(command));
        }
        for (NutsWorkspaceCommandFactory commandFactory : commandFactories) {
            for (NutsCommandAliasConfig command : commandFactory.findCommands(ws)) {
                if (!all.containsKey(command.getName())) {
                    all.put(command.getName(), toDefaultNutsWorkspaceCommand(command));
                }
            }
        }
        return new ArrayList<>(all.values());
    }

    @Override
    public List<NutsWorkspaceCommandAlias> findCommandAliases(NutsId id, NutsSession session) {
        HashMap<String, NutsWorkspaceCommandAlias> all = new HashMap<>();
        for (NutsCommandAliasConfig command : defaultCommandFactory.findCommands(id, ws)) {
            all.put(command.getName(), toDefaultNutsWorkspaceCommand(command));
        }
        return new ArrayList<>(all.values());
    }

    @Override
    public Path getHomeLocation(NutsStoreLocation folderType) {
        return current().getHomeLocation(folderType);
    }

    @Override
    public Path getStoreLocation(NutsStoreLocation folderType) {
        return current().getStoreLocation(folderType);
    }

    @Override
    public void setStoreLocation(NutsStoreLocation folderType, String location, NutsUpdateOptions options) {
        if (folderType == null) {
            throw new NutsIllegalArgumentException(ws, "Invalid store root folder null");
        }
        options = CoreNutsUtils.validate(options, ws);
        onPreUpdateConfig("store-location", options);
        storeModelBoot.setStoreLocations(new NutsStoreLocationsMap(storeModelBoot.getStoreLocations()).set(folderType, location).toMapOrNull());
        onPostUpdateConfig("store-location", options);
    }

    @Override
    public void setStoreLocationStrategy(NutsStoreLocationStrategy strategy, NutsUpdateOptions options) {
        if (strategy == null) {
            strategy = NutsStoreLocationStrategy.EXPLODED;
        }
        options = CoreNutsUtils.validate(options, ws);
        onPreUpdateConfig("store-location-strategy", options);
        storeModelBoot.setStoreLocationStrategy(strategy);
        onPostUpdateConfig("store-location-strategy", options);
    }

    @Override
    public void setStoreLocationLayout(NutsOsFamily layout, NutsUpdateOptions options) {
        options = CoreNutsUtils.validate(options, ws);
        onPreUpdateConfig("store-location-layout", options);
        storeModelBoot.setStoreLocationLayout(layout);
        onPostUpdateConfig("store-location-layout", options);
    }

    @Override
    public Path getStoreLocation(String id, NutsStoreLocation folderType) {
        return getStoreLocation(ws.id().parse(id), folderType);
    }

    @Override
    public Path getStoreLocation(NutsId id, NutsStoreLocation folderType) {
        Path storeLocation = getStoreLocation(folderType);
        if (storeLocation == null) {
            return null;
        }
        return storeLocation.resolve(NutsConstants.Folders.ID).resolve(getDefaultIdBasedir(id));
//        switch (folderType) {
//            case CACHE:
//                return storeLocation.resolve(NutsConstants.Folders.ID).resolve(getDefaultIdBasedir(id));
//            case CONFIG:
//                return storeLocation.resolve(NutsConstants.Folders.ID).resolve(getDefaultIdBasedir(id));
//        }
//        return storeLocation.resolve(getDefaultIdBasedir(id));
    }

    @Override
    public String getDefaultIdFilename(NutsId id) {
        String classifier = "";
        String ext = getDefaultIdExtension(id);
        if (!ext.equals(NutsConstants.Files.DESCRIPTOR_FILE_EXTENSION) && !ext.equals(".pom")) {
            String c = id.getClassifier();
            if (!CoreStringUtils.isBlank(c)) {
                classifier = "-" + c;
            }
        }
        return id.getArtifactId() + "-" + id.getVersion().getValue() + classifier + ext;
    }

    @Override
    public String getDefaultIdBasedir(NutsId id) {
        NutsWorkspaceUtils.of(ws).checkSimpleNameNutsId(id);
        String groupId = id.getGroupId();
        String artifactId = id.getArtifactId();
        String plainIdPath = groupId.replace('.', '/') + "/" + artifactId;
        if (id.getVersion().isBlank()) {
            return plainIdPath;
        }
        String version = id.getVersion().getValue();
//        String a = CoreNutsUtils.trimToNullAlternative(id.getAlternative());
        String x = plainIdPath + "/" + version;
//        if (a != null) {
//            x += "/" + a;
//        }
        return x;
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
    public NutsCommandAliasFactoryConfig[] getCommandFactories(NutsSession session) {
        if (storeModelMain.getCommandFactories() != null) {
            return storeModelMain.getCommandFactories().toArray(new NutsCommandAliasFactoryConfig[0]);
        }
        return new NutsCommandAliasFactoryConfig[0];
    }

    @Override
    public NutsRepositoryRef[] getRepositoryRefs(NutsSession session) {
        return repositoryRegistryHelper.getRepositoryRefs();
    }

    @Override
    public NutsWorkspaceListManager createWorkspaceListManager(String name, NutsSession session) {
        return new DefaultNutsWorkspaceListManager(ws, name);
    }

    @Override
    public void setHomeLocation(NutsOsFamily layout, NutsStoreLocation folder, String location, NutsUpdateOptions options) {
        if (folder == null) {
            throw new NutsIllegalArgumentException(ws, "Invalid store root folder null");
        }
        options = CoreNutsUtils.validate(options, ws);
        onPreUpdateConfig("home-location", options);
        storeModelBoot.setHomeLocations(new NutsHomeLocationsMap(storeModelBoot.getHomeLocations()).set(layout, folder, location).toMapOrNull());
        onPostUpdateConfig("home-location", options);
    }

    @Override
    public boolean isSupportedRepositoryType(String repositoryType) {
        if (CoreStringUtils.isBlank(repositoryType)) {
            repositoryType = NutsConstants.RepoTypes.NUTS;
        }
        return ws.extensions().createAllSupported(NutsRepositoryFactoryComponent.class,
                new DefaultNutsSupportLevelContext<>(ws, new NutsRepositoryConfig().setType(repositoryType))
        ).size() > 0;
    }

    @Override
    public NutsRepository addRepository(NutsRepositoryModel repository, NutsSession session) {
        NutsRepositoryConfig config = new NutsRepositoryConfig();
        String name = repository.getName();
        String uuid = repository.getUuid();
        if (CoreStringUtils.isBlank(name)) {
            name = "custom";
        }
        if (CoreStringUtils.isBlank(uuid)) {
            uuid = UUID.randomUUID().toString();
        }
        config.setName(name);
        config.setType("custom");
        config.setUuid(uuid);
        config.setStoreLocationStrategy(repository.getStoreLocationStrategy());
        NutsAddRepositoryOptions options = new NutsAddRepositoryOptions();
        Path rootFolder = getRepositoriesRoot();
        options.setName(config.getName());
        options.setConfig(config);
        options.setDeployOrder(repository.getDeployOrder());
        options.setSession(session);
        options.setTemporary(true);
        options.setLocation(CoreIOUtils.resolveRepositoryPath(options, rootFolder, ws));
        NutsRepository r = new NutsSimpleRepositoryWrapper(options, ws, null, repository);
        addRepository(null, r, new NutsAddOptions().setSession(session));
        return r;
    }

    @Override
    public NutsRepository addRepository(String repositoryNamedUrl, NutsSession session) {
        return addRepository(CoreNutsUtils.defToOptions(CoreNutsUtils.repositoryStringToDefinition(repositoryNamedUrl).setSession(session)));
    }

    @Override
    public NutsRepository addRepository(NutsRepositoryDefinition definition) {
        return addRepository(CoreNutsUtils.defToOptions(definition));
    }

    @Override
    public NutsRepository addRepository(NutsAddRepositoryOptions options) {
        if (excludedRepositoriesSet != null && excludedRepositoriesSet.contains(options.getName())) {
            return null;
        }
        if (options.getSession() == null) {
            options.setSession(ws.createSession());
        }
        if (options.isProxy()) {
            if (options.getConfig() == null) {
                NutsRepository proxy = addRepository(
                        new NutsAddRepositoryOptions()
                                .setName(options.getName())
                                .setFailSafe(options.isFailSafe())
                                .setLocation(options.getName())
                                .setEnabled(options.isEnabled())
                                .setCreate(options.isCreate())
                                .setDeployOrder(options.getDeployOrder())
                                .setConfig(
                                        new NutsRepositoryConfig()
                                                .setType(NutsConstants.RepoTypes.NUTS)
                                                .setName(options.getName())
                                                .setLocation(null)
                                )
                );
                if (proxy == null) {
                    //mainly because path is not accessible
                    //or the repository is excluded
                    return null;
                }
                //Dont need to add mirror if repository is already loadable from config!
                final String m2 = options.getName() + "-ref";
                if (proxy.config().findMirror(m2, options.getSession().copy().setTransitive(false)) == null) {
                    proxy.config().addMirror(new NutsAddRepositoryOptions()
                            .setName(m2)
                            .setFailSafe(options.isFailSafe())
                            .setEnabled(options.isEnabled())
                            .setLocation(options.getLocation())
                            .setDeployOrder(options.getDeployOrder())
                            .setCreate(options.isCreate())
                    );
                }
                return proxy;
            } else {
                NutsRepository proxy = addRepository(
                        new NutsAddRepositoryOptions()
                                .setName(options.getName())
                                .setFailSafe(options.isFailSafe())
                                .setEnabled(options.isEnabled())
                                .setLocation(options.getLocation())
                                .setCreate(options.isCreate())
                                .setDeployOrder(options.getDeployOrder())
                                .setConfig(
                                        new NutsRepositoryConfig()
                                                .setType(NutsConstants.RepoTypes.NUTS)
                                                .setName(options.getConfig().getName())
                                                .setLocation(null)
                                )
                );
                if (proxy == null) {
                    return null;
                }
                //Dont need to add mirror if repository is already loadable from config!
                final String m2 = options.getName() + "-ref";
                if (proxy.config().findMirror(m2, options.getSession().copy().setTransitive(false)) == null) {
                    proxy.config().addMirror(new NutsAddRepositoryOptions()
                            .setName(m2)
                            .setFailSafe(options.isFailSafe())
                            .setEnabled(options.isEnabled())
                            .setLocation(m2)
                            .setCreate(options.isCreate())
                            .setDeployOrder(options.getDeployOrder())
                            .setConfig(
                                    new NutsRepositoryConfig()
                                            .setName(m2)
                                            .setType(CoreStringUtils.coalesce(options.getConfig().getType(), NutsConstants.RepoTypes.NUTS))
                                            .setLocation(options.getConfig().getLocation())
                            ));
                }
                return proxy;
            }
        } else {
            NutsRepositoryRef ref = options.isTemporary() ? null : CoreNutsUtils.optionsToRef(options);
            NutsRepository r = this.createRepository(options, getRepositoriesRoot(), null);
            addRepository(ref, r, new NutsAddOptions().setSession(options.getSession()));
            return r;
        }
    }

    @Override
    public NutsRepository findRepositoryById(String repositoryNameOrId, NutsSession session) {
        NutsRepository y = repositoryRegistryHelper.findRepositoryById(repositoryNameOrId);
        if (y != null) {
            return y;
        }
        if (session.isTransitive()) {
            for (NutsRepository child : repositoryRegistryHelper.getRepositories()) {
                final NutsRepository m = child.config().findMirrorById(repositoryNameOrId, session.copy().setTransitive(true));
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NutsIllegalArgumentException(ws, "Ambigous repository name " + repositoryNameOrId + " Found two Ids " + y.getUuid() + " and " + m.getUuid());
                    }
                    return m;
                }
            }
        }
        return y;
    }

    @Override
    public NutsRepository findRepositoryByName(String repositoryNameOrId, NutsSession session) {
        NutsRepository y = repositoryRegistryHelper.findRepositoryByName(repositoryNameOrId);
        if (y != null) {
            return y;
        }
        if (session.isTransitive()) {
            for (NutsRepository child : repositoryRegistryHelper.getRepositories()) {
                final NutsRepository m = child.config().findMirrorByName(repositoryNameOrId, session.copy().setTransitive(true));
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NutsIllegalArgumentException(ws, "Ambigous repository name " + repositoryNameOrId + " Found two Ids " + y.getUuid() + " and " + m.getUuid());
                    }
                    return m;
                }
            }
        }
        return y;
    }

    @Override
    public NutsRepository findRepository(String repositoryNameOrId, NutsSession session) {
        NutsRepository y = repositoryRegistryHelper.findRepository(repositoryNameOrId);
        if (y != null) {
            return y;
        }
        if (session.isTransitive()) {
            for (NutsRepository child : repositoryRegistryHelper.getRepositories()) {
                final NutsRepository m = child.config().findMirror(repositoryNameOrId, session.copy().setTransitive(true));
                if (m != null) {
                    if (y == null) {
                        y = m;
                    } else {
                        throw new NutsIllegalArgumentException(ws, "Ambigous repository name " + repositoryNameOrId + " Found two Ids " + y.getUuid() + " and " + m.getUuid());
                    }
                    return m;
                }
            }
        }
        return y;
    }

    @Override
    public NutsRepository getRepository(String repositoryIdOrName, NutsSession session) throws NutsRepositoryNotFoundException {
        NutsRepository r = findRepository(repositoryIdOrName, session);
        if (r != null) {
            return r;
        }
        throw new NutsRepositoryNotFoundException(ws, repositoryIdOrName);
    }

    @Override
    public NutsWorkspaceConfigManager removeRepository(String repositoryId, NutsRemoveOptions options) {
        ws.security().checkAllowed(NutsConstants.Permissions.REMOVE_REPOSITORY, "remove-repository");
        if (options == null) {
            options = new NutsRemoveOptions();
        }
        if (options.getSession() == null) {
            options.setSession(ws.createSession());
        }
        final NutsRepository repository = repositoryRegistryHelper.removeRepository(repositoryId);
        if (repository != null) {
            fireConfigurationChanged("config-main", options.getSession(), ConfigEventType.MAIN);
            NutsWorkspaceUtils.of(ws).events().fireOnRemoveRepository(new DefaultNutsWorkspaceEvent(options.getSession(), repository, "repository", repository, null));
        }
        return this;
    }

    @Override
    public NutsRepository[] getRepositories(NutsSession session) {
        return repositoryRegistryHelper.getRepositories();
    }

    @Override
    public NutsRepositoryDefinition[] getDefaultRepositories() {
        List<NutsRepositoryDefinition> all = new ArrayList<>();
        for (NutsRepositoryFactoryComponent provider : ws.extensions().createAll(NutsRepositoryFactoryComponent.class)) {
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
        for (NutsWorkspaceArchetypeComponent extension : ws.extensions().createAllSupported(NutsWorkspaceArchetypeComponent.class, new DefaultNutsSupportLevelContext<>(ws, null))) {
            set.add(extension.getName());
        }
        return set;
    }

    @Override
    public Path resolveRepositoryPath(String repositoryLocation) {
        Path root = this.getRepositoriesRoot();
        NutsWorkspaceConfigManager configManager = this.ws.config();
        return Paths.get(ws.io().expandPath(repositoryLocation,
                root != null ? root.toString() : configManager.getStoreLocation(NutsStoreLocation.CONFIG)
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
    public NutsRepository createRepository(NutsAddRepositoryOptions options, Path rootFolder, NutsRepository parentRepository) {
        options = options.copy();
        try {
            NutsRepositoryConfig conf = options.getConfig();
            if (conf == null) {
                options.setLocation(CoreIOUtils.resolveRepositoryPath(options, rootFolder, ws));
                conf = loadRepository(Paths.get(options.getLocation(), NutsConstants.Files.REPOSITORY_CONFIG_FILE_NAME), options.getName(), ws);
                if (conf == null) {
                    if (options.isFailSafe()) {
                        return null;
                    }
                    throw new NutsInvalidRepositoryException(ws, options.getLocation(), "Invalid location " + options.getLocation());
                }
                options.setConfig(conf);
            } else {
                options.setLocation(CoreIOUtils.resolveRepositoryPath(options, rootFolder, ws));
            }
            if (CoreStringUtils.isBlank(conf.getType())) {
                conf.setType(NutsConstants.RepoTypes.NUTS);
            }
            if (CoreStringUtils.isBlank(conf.getName())) {
                conf.setName(options.getName());
            }
            NutsRepositoryFactoryComponent factory_ = ws.extensions().createSupported(NutsRepositoryFactoryComponent.class,
                    new DefaultNutsSupportLevelContext<>(ws, conf));
            if (factory_ != null) {
                NutsRepository r = factory_.create(options, ws, parentRepository);
                if (r != null) {
                    return r;
                }
            }
            throw new NutsInvalidRepositoryException(ws, options.getName(), "Invalid type " + conf.getType());
        } catch (RuntimeException ex) {
            if (options.isFailSafe()) {
                return null;
            }
            throw ex;
        }
    }

    @Override
    public String getDefaultIdContentExtension(String packaging) {
        if (CoreStringUtils.isBlank(packaging)) {
            throw new NutsIllegalArgumentException(ws, "Unsupported empty Packaging");
        }
        switch (packaging) {
            case "jar":
            case "bundle":
            case "nuts-extension":
            case "maven-archetype":
            case "maven-plugin":
            case "ejb-client":
            case "test-jar":
            case "ejb":
            case "java-source":
            case "javadoc":
                return ".jar";
            case "dll":
            case "so":
            case "jnilib":
                return "-natives.jar";
            case "war":
                return ".war";
            case "ear":
                return ".ear";
            case "pom":
                return ".pom";
            case "nuts":
                return NutsConstants.Files.DESCRIPTOR_FILE_EXTENSION;
            case "rar":
                return ".rar";
            case "zip":
            case "nbm-application":
                return ".zip";
        }
        return "." + packaging;
    }

    @Override
    public String getDefaultIdExtension(NutsId id) {
        Map<String, String> q = id.getProperties();
        String f = CoreStringUtils.trim(q.get(NutsConstants.IdProperties.FACE));
        switch (f) {
            case NutsConstants.QueryFaces.DESCRIPTOR: {
                return NutsConstants.Files.DESCRIPTOR_FILE_EXTENSION;
            }
            case NutsConstants.QueryFaces.DESCRIPTOR_HASH: {
                return ".nuts.sha1";
            }
            case CoreNutsConstants.QueryFaces.CATALOG: {
                return ".catalog";
            }
            case NutsConstants.QueryFaces.CONTENT_HASH: {
                return getDefaultIdExtension(id.builder().setFaceContent().build()) + ".sha1";
            }
            case NutsConstants.QueryFaces.CONTENT: {
                return getDefaultIdContentExtension(q.get(NutsConstants.IdProperties.PACKAGING));
            }
            default: {
                if (f.equals("cache") || f.endsWith(".cache")) {
                    return "." + f;
                }
                if (CoreStringUtils.isBlank(f)) {
                    throw new NutsIllegalArgumentException(ws, "Missing face in " + id);
                }
                throw new NutsIllegalArgumentException(ws, "Unsupported face " + f + " in " + id);
            }
        }
    }

    @Override
    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        return current().getStoreLocationStrategy();
    }

    @Override
    public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy() {
        return current().getRepositoryStoreLocationStrategy();
    }

    @Override
    public NutsOsFamily getStoreLocationLayout() {
        return current().getStoreLocationLayout();
    }

    @Override
    public Map<String, String> getStoreLocations() {
        return current().getStoreLocations();
    }

    @Override
    public Map<String, String> getHomeLocations() {
        return current().getHomeLocations();
    }

    @Override
    public Path getHomeLocation(NutsOsFamily layout, NutsStoreLocation location) {
        return current().getHomeLocation(layout, location);
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
    public NutsOsFamily getOsFamily() {
        return current().getOsFamily();
    }

    @Override
    public NutsId getPlatform() {
        return current().getPlatform();
    }

    @Override
    public NutsId getOs() {
        return current().getOs();
    }

    @Override
    public NutsId getOsDist() {
        return current().getOsDist();
    }

    @Override
    public NutsId getArch() {
        return current().getArch();
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
        configSdks.clear();
        if (this.storeModelMain.getSdk() != null) {
            for (NutsSdkLocation sdk : this.storeModelMain.getSdk()) {
                List<NutsSdkLocation> list = configSdks.get(sdk.getProduct());
                if (list == null) {
                    list = new ArrayList<>();
                    configSdks.put(sdk.getProduct(), list);
                }
                list.add(sdk);
            }
        }
        NutsRemoveOptions o0 = CoreNutsUtils.toRemoveOptions(options);
        o0.setSession(options.getSession());
        removeAllRepositories(o0);
        if (this.storeModelMain.getRepositories() != null) {
            for (NutsRepositoryRef ref : this.storeModelMain.getRepositories()) {
                NutsAddRepositoryOptions o1 = CoreNutsUtils.refToOptions(ref);
                o1.setSession(options.getSession());
                NutsRepository r = this.createRepository(o1, getRepositoriesRoot(), null);
                NutsAddOptions o2 = CoreNutsUtils.toAddOptions(options);
                o2.setSession(options.getSession());
                addRepository(ref, r, o2);
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

    public DefaultNutsWorkspaceCurrentConfig current() {
        if (currentConfig == null) {
            throw new IllegalStateException("Unable to use workspace.current(). Still in initialize status");
        }
        return currentConfig;
    }

    protected Set<String> parseImports(String importExpression) {
        Set<String> imports = new LinkedHashSet<>();
        if (importExpression != null) {
            for (String s : importExpression.split("[,;: \t\n]")) {
                if (!s.isEmpty()) {
                    imports.add(s.trim());
                }
            }
        }
        return imports;
    }

    private String toValidSdkName(String type) {
        if (CoreStringUtils.isBlank(type)) {
            type = "java";
        } else {
            type = CoreStringUtils.trim(type);
        }
        return type;
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
//                    ws.config().getDefaultIdFilename(
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
                + ", workspace=" + ((currentConfig == null) ? "NULL" : ("'" + getWorkspaceLocation() + '\''))
                + '}';
    }

    private NutsWorkspaceCommandAlias toDefaultNutsWorkspaceCommand(NutsCommandAliasConfig c) {
        if (c.getCommand() == null || c.getCommand().length == 0) {

            LOG.with().level(Level.WARNING).verb(NutsLogVerb.FAIL).log("Invalid Command Definition ''{0}''. Missing Command. Ignored", c.getName());
            return null;
        }
//        if (c.getOwner() == null) {
//            LOG.log(Level.WARNING, "Invalid Command Definition ''{0}''. Missing Owner. Ignored", c.getName());
//            return null;
//        }
        return new DefaultNutsWorkspaceCommandAlias(ws)
                .setCommand(c.getCommand())
                .setFactoryId(c.getFactoryId())
                .setOwner(c.getOwner())
                .setExecutorOptions(c.getExecutorOptions())
                .setName(c.getName())
                .setHelpCommand(c.getHelpCommand())
                .setHelpText(c.getHelpText());
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
        Path apiConfigFile = getStoreLocation(apiId, NutsStoreLocation.CONFIG).resolve(NutsConstants.Files.WORKSPACE_API_CONFIG_FILE_NAME);
        if (force || !Files.isRegularFile(apiConfigFile)) {
            Map<String, Object> m = new LinkedHashMap<>();
            if (runtimeId == null) {
                runtimeId = ws.search().addId(NutsConstants.Ids.NUTS_RUNTIME)
                        .setRuntime(true)
                        .setTargetApiVersion(apiId.getVersion().getValue())
                        .setFailFast(false).setLatest(true).getResultIds().first();
            }
            if (runtimeId == null) {
                runtimeId = MavenUtils.of(ws).resolveLatestMavenId(ws.id().parse(NutsConstants.Ids.NUTS_RUNTIME),
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
            ws.json().value(m).print(apiConfigFile);
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
                cconfig.setApiId(ws.id().parse(NutsConstants.Ids.NUTS_API + "#" + initOptions.getApiVersion()));
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

            setCurrentConfig(cconfig.build(getWorkspaceLocation()));

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
                cconfig.setApiId(ws.id().parse(NutsConstants.Ids.NUTS_API + "#" + initOptions.getApiVersion()));
                cconfig.setRuntimeId(initOptions.getRuntimeId());
                cconfig.setRuntimeDependencies(initOptions.getRuntimeDependencies());
                cconfig.setExtensionDependencies(initOptions.getExtensionDependencies());
                cconfig.setBootRepositories(initOptions.getBootRepositories());
            }
            setCurrentConfig(cconfig
                    .build(getWorkspaceLocation())
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
    public void fireConfigurationChanged(String configName, NutsSession session, DefaultNutsWorkspaceConfigManager.ConfigEventType t) {
        cachedImports = null;
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
        for (NutsWorkspaceListener workspaceListener : ws.getWorkspaceListeners()) {
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
    public void removeAllRepositories(NutsRemoveOptions options) {
        if (options == null) {
            options = new NutsRemoveOptions();
        }
        if (options.getSession() == null) {
            options.setSession(ws.createSession());
        }
        for (NutsRepository repository : repositoryRegistryHelper.getRepositories()) {
            removeRepository(repository.getUuid(), options);
        }
    }

    @Override
    public Path getRepositoriesRoot() {
        return ws.config().getStoreLocation(NutsStoreLocation.CONFIG).resolve(NutsConstants.Folders.REPOSITORIES);
    }

    @Override
    public boolean isValidWorkspaceFolder() {
        Path file = getWorkspaceLocation().resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        return Files.isRegularFile(file);
    }

    @Override
    public NutsAuthenticationAgent createAuthenticationAgent(String authenticationAgent) {
        authenticationAgent = CoreStringUtils.trim(authenticationAgent);
        NutsAuthenticationAgent supported = null;
        if (authenticationAgent.isEmpty()) {
            supported = ws.extensions().createSupported(NutsAuthenticationAgent.class, new DefaultNutsSupportLevelContext<>(ws, ""));
        } else {
            List<NutsAuthenticationAgent> agents = ws.extensions().createAllSupported(NutsAuthenticationAgent.class, new DefaultNutsSupportLevelContext<>(ws, authenticationAgent));
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

    public void onExtensionsPrepared() {
        try {
            indexStoreClientFactory = ws.extensions().createSupported(NutsIndexStoreFactory.class, new DefaultNutsSupportLevelContext<>(ws, null));
        } catch (Exception ex) {
            //
        }
        if (indexStoreClientFactory == null) {
            indexStoreClientFactory = new DummyNutsIndexStoreFactory();
        }
    }

    public void prepareBootRuntimeOrExtension(NutsId id, boolean force, boolean runtime, NutsSession session) {
        Path configFile = getStoreLocation(NutsStoreLocation.CACHE)
                .resolve(NutsConstants.Folders.ID).resolve(getDefaultIdBasedir(id)).resolve(runtime ?
                        NutsConstants.Files.WORKSPACE_RUNTIME_CACHE_FILE_NAME
                        : NutsConstants.Files.WORKSPACE_EXTENSION_CACHE_FILE_NAME
                );
        Path jarFile = getStoreLocation(NutsStoreLocation.LIB)
                .resolve(NutsConstants.Folders.ID).resolve(getDefaultIdBasedir(id))
                .resolve(ws.config().getDefaultIdFilename(id.builder().setFaceContent().setPackaging("jar").build()));
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
                deps.add(ws.id().parse(dep));
            }
        } else {
            for (NutsDependency dep : def.getDependencies()) {
                deps.add(dep.getId());
            }
            m.put("dependencies",
                    Arrays.stream(
                            def.getDependencies()
                    ).map(x -> x.getId().getLongName()).collect(Collectors.joining(";"))
            );
            if (runtime) {
                m.put("bootRepositories", def.getDescriptor().getProperties().get("nuts-runtime-repositories"));
            }
        }

        if (force || !Files.isRegularFile(configFile)) {
            ws.json().value(m).print(configFile);
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
        String idFileName = ws.config().getDefaultIdFilename(id.builder().setFaceContent().setPackaging("jar").build());
        Path jarFile = getStoreLocation(NutsStoreLocation.LIB)
                .resolve(NutsConstants.Folders.ID).resolve(getDefaultIdBasedir(id))
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
                            ws.io().copy().from(pp + ws.config().getDefaultIdBasedir(id) + "/" + idFileName).to(jarFile).run();
                            return;
                        } catch (Exception ex) {
                            //ignore
                        }
                    } else {
                        try {
                            ws.io().copy().from(Paths.get(pp)
                                    .resolve(ws.config().getDefaultIdBasedir(id))
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

    private void onPreUpdateConfig(String confName, NutsUpdateOptions options) {
        options = CoreNutsUtils.validate(options, ws);
        preUpdateConfigStoreLocations = new NutsStoreLocationsMap(currentConfig.getStoreLocations());
    }

    private void onPostUpdateConfig(String confName, NutsUpdateOptions options) {
        options = CoreNutsUtils.validate(options, ws);
        preUpdateConfigStoreLocations = new NutsStoreLocationsMap(currentConfig.getStoreLocations());
        DefaultNutsWorkspaceCurrentConfig d = (DefaultNutsWorkspaceCurrentConfig) currentConfig;
        d.setUserStoreLocations(new NutsHomeLocationsMap(storeModelBoot.getStoreLocations()).toMapOrNull());
        d.setHomeLocations(new NutsHomeLocationsMap(storeModelBoot.getHomeLocations()).toMapOrNull());
        d.build(getWorkspaceLocation());
        NutsStoreLocationsMap newSL = new NutsStoreLocationsMap(currentConfig.getStoreLocations());
        for (NutsStoreLocation sl : NutsStoreLocation.values()) {
            String oldPath = preUpdateConfigStoreLocations.get(sl);
            String newPath = newSL.get(sl);
            if (!oldPath.equals(newPath)) {
                Path oldPathObj = Paths.get(oldPath);
                if (Files.exists(oldPathObj)) {
                    try {
                        CoreIOUtils.copyFolder(oldPathObj, Paths.get(newPath));
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                }
            }
        }
        fireConfigurationChanged(confName, options.getSession(), ConfigEventType.API);
    }

    private void onLoadWorkspaceError(Throwable ex) {
        NutsWorkspaceConfigManager wconfig = this;
        Path file = getWorkspaceLocation().resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        if (wconfig.isReadOnly()) {
            throw new UncheckedIOException("Unable to load config file " + file.toString(), new IOException(ex));
        }
        String fileName = "nuts-workspace-" + Instant.now().toString();
        LOG.with().level(Level.SEVERE).verb(NutsLogVerb.FAIL).log("Erroneous config file. Unable to load file {0} : {1}", new Object[]{file, ex.toString()});
        Path logError = wconfig.getStoreLocation(wconfig.getApiId(), NutsStoreLocation.LOG).resolve("invalid-config");
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
            o.println(wconfig.getWorkspaceLocation());
            o.println("workspace.options:");
            o.println(wconfig.getOptions().format().setCompact(false).setRuntime(true).setInit(true).setExported(true).getBootCommandLine());
            for (NutsStoreLocation location : NutsStoreLocation.values()) {
                o.println("location." + location.id() + ":");
                o.println(wconfig.getStoreLocation(location));
            }
            o.println("java.class.path:");
            o.println(System.getProperty("java.class.path"));
            o.println();
            ex.printStackTrace(o);
        } catch (Exception ex2) {
            //ignore
        }
    }

    private void onLoadRepositoryError(Path file, String name, String uuid, Throwable ex) {
        NutsWorkspaceConfigManager wconfig = this;
        if (wconfig.isReadOnly()) {
            throw new UncheckedIOException("Error loading repository " + file.toString(), new IOException(ex));
        }
        String fileName = "nuts-repository" + (name == null ? "" : ("-") + name) + (uuid == null ? "" : ("-") + uuid) + "-" + Instant.now().toString();
        LOG.with().level(Level.SEVERE).verb(NutsLogVerb.FAIL).log("Erroneous config file. Unable to load file {0} : {1}", new Object[]{file, ex.toString()});
        Path logError = wconfig.getStoreLocation(wconfig.getApiId(), NutsStoreLocation.LOG).resolve("invalid-config");
        try {
            Files.createDirectories(logError);
        } catch (IOException ex1) {
            throw new UncheckedIOException("Unable to log repository error while loading config file " + file.toString() + " : " + ex1.toString(), new IOException(ex));
        }
        Path newfile = logError.resolve(fileName + ".json");
        LOG.with().level(Level.SEVERE).verb(NutsLogVerb.FAIL).log("Erroneous repository config file will be replaced by a fresh one. Old config is copied to {0}", newfile.toString());
        try {
            Files.move(file, newfile);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to load and re-create repository config file " + file.toString() + " : " + e.toString(), new IOException(ex));
        }

        try (PrintStream o = new PrintStream(logError.resolve(fileName + ".error").toFile())) {
            o.println("workspace.path:" + wconfig.getWorkspaceLocation());
            o.println("repository.path:" + file);
            o.println("workspace.options:" + wconfig.getOptions().format().setCompact(false).setRuntime(true).setInit(true).setExported(true).getBootCommandLine());
            for (NutsStoreLocation location : NutsStoreLocation.values()) {
                o.println("location." + location.id() + ":" + wconfig.getStoreLocation(location));
            }
            o.println("java.class.path:" + System.getProperty("java.class.path"));
            o.println();
            ex.printStackTrace(o);
        } catch (Exception ex2) {
            //ignore
        }
    }

    public Map<String, List<NutsSdkLocation>> getSdk() {
        return configSdks;
    }

    public NutsUserConfig getSecurity(String id) {
        return configUsers.get(id);
    }

    protected void addRepository(NutsRepositoryRef ref, NutsRepository repo, NutsAddOptions options) {
        options = CoreNutsUtils.validate(options, ws);
        repositoryRegistryHelper.addRepository(ref, repo);
        if (repo != null) {
            fireConfigurationChanged("config-main", options.getSession(), ConfigEventType.MAIN);
            NutsWorkspaceUtils.of(ws).events().fireOnAddRepository(
                    new DefaultNutsWorkspaceEvent(options.getSession(), repo, "repository", null, repo)
            );
        }
    }

    public NutsRepositoryConfig loadRepository(Path file, String name, NutsWorkspace ws) {
        NutsRepositoryConfig conf = null;
        if (Files.isRegularFile(file) && Files.isReadable(file)) {
            byte[] bytes;
            try {
                bytes = Files.readAllBytes(file);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            try {
                Map<String, Object> a_config0 = ws.json().parse(bytes, Map.class);
                String version = (String) a_config0.get("configVersion");
                if (version == null) {
                    version = ws.config().getApiVersion();
                }
                int buildNumber = CoreNutsUtils.getApiVersionOrdinalNumber(version);
                if (buildNumber < 506) {

                }
                conf = ws.json().parse(file, NutsRepositoryConfig.class);
            } catch (Exception ex) {
                onLoadRepositoryError(file, name, null, ex);
            }
        }
        return conf;
    }

    private NutsWorkspaceConfigBoot parseBootConfig() {
        Path file = ws.config().getWorkspaceLocation().resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(file);
        if (bytes == null) {
            return null;
        }
        try {
            Map<String, Object> a_config0 = ws.json().parse(bytes, Map.class);
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
            if(s.trim().length()>0){
                repos.add(s);
            }
        }
        if (repos.isEmpty()) {
            if (!NO_M2) {
                repos.add("maven-local");
            }
            repos.add("vpc-public-nuts");
            repos.add("vpc-public-maven");
            repos.add("maven-central");
        }
        return parsedBootRepositories = repos;
    }

    public enum ConfigEventType {
        API, RUNTIME, BOOT, MAIN, SECURITY,
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
