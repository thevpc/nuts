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
package net.vpc.app.nuts.core.config;

import java.io.ByteArrayInputStream;
import net.vpc.app.nuts.core.spi.NutsWorkspaceConfigManagerExt;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.core.CommandNutsWorkspaceCommandFactory;
import net.vpc.app.nuts.core.ConfigNutsWorkspaceCommandFactory;
import net.vpc.app.nuts.core.DefaultNutsSupportLevelContext;
import net.vpc.app.nuts.core.DefaultNutsVersion;
import net.vpc.app.nuts.core.DefaultNutsWorkspace;
import net.vpc.app.nuts.core.DefaultNutsWorkspaceCommandAlias;
import net.vpc.app.nuts.core.DefaultNutsWorkspaceEvent;
import net.vpc.app.nuts.core.DefaultNutsWorkspaceListManager;
import net.vpc.app.nuts.core.NutsHomeLocationsMap;
import net.vpc.app.nuts.core.NutsStoreLocationsMap;
import net.vpc.app.nuts.core.config.compat.NutsWorkspaceConfig502;
import net.vpc.app.nuts.core.repos.NutsRepositoryRegistryHelper;
import net.vpc.app.nuts.core.spi.NutsAuthenticationAgentSpi;
import net.vpc.app.nuts.core.spi.NutsWorkspaceExt;

/**
 * @author vpc
 */
public class DefaultNutsWorkspaceConfigManager implements NutsWorkspaceConfigManagerExt {

    public static final Logger LOG = Logger.getLogger(DefaultNutsWorkspaceConfigManager.class.getName());

    private final DefaultNutsWorkspace ws;
    private DefaultNutsWorkspaceCurrentConfig currentConfig;
    private NutsWorkspaceStoredConfig storedConfig = new NutsWorkspaceStoredConfigImpl();
    private ClassLoader bootClassLoader;
    private URL[] bootClassWorldURLs;
    protected NutsWorkspaceConfig storeModel = new NutsWorkspaceConfig();
    private final List<NutsWorkspaceCommandFactory> commandFactories = new ArrayList<>();
    private final ConfigNutsWorkspaceCommandFactory defaultCommandFactory = new ConfigNutsWorkspaceCommandFactory(this);
    private boolean configurationChanged = false;
    private Path workspaceLocation;
    private NutsWorkspaceOptions options;
    private long startCreateTime;
    private long endCreateTime;
    private final Map<String, List<NutsSdkLocation>> configSdks = new LinkedHashMap<>();
    private final Map<String, NutsUserConfig> configUsers = new LinkedHashMap<>();
    private final NutsRepositoryRegistryHelper repositoryRegistryHelper;

    private NutsIndexStoreClientFactory indexStoreClientFactory;
    private Set<String> cachedImports;
    private Set<String> excludedRepositoriesSet = new HashSet<>();
    private NutsStoreLocationsMap preUpdateConfigStoreLocations;

    public DefaultNutsWorkspaceConfigManager(final DefaultNutsWorkspace ws) {
        this.ws = ws;
        repositoryRegistryHelper = new NutsRepositoryRegistryHelper(ws);
        try {
            indexStoreClientFactory = ws.extensions().createSupported(NutsIndexStoreClientFactory.class, new DefaultNutsSupportLevelContext<>(ws, null));
        } catch (Exception ex) {
            //
        }
        if (indexStoreClientFactory == null) {
            indexStoreClientFactory = new DummyNutsIndexStoreClientFactory();
        }
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
    public void setExcludedRepositories(String[] excludedRepositories) {
        excludedRepositoriesSet = excludedRepositories == null ? null : new HashSet<>(CoreStringUtils.split(Arrays.asList(excludedRepositories), " ,;"));
    }

    @Override
    public ClassLoader getBootClassLoader() {
        return bootClassLoader;
    }

    @Override
    public void setConfig(NutsWorkspaceConfig config, NutsSession session) {
        setConfig(config, session, true);
    }

    private void setConfig(NutsWorkspaceConfig config, NutsSession session, boolean fire) {
        this.storeModel = config;
        if (CoreStringUtils.isBlank(config.getUuid())) {
            config.setUuid(UUID.randomUUID().toString());
            fire = true;
        }
        configSdks.clear();
        if (config.getSdk() != null) {
            for (NutsSdkLocation sdk : config.getSdk()) {
                List<NutsSdkLocation> list = configSdks.get(sdk.getType());
                if (list == null) {
                    list = new ArrayList<>();
                    configSdks.put(sdk.getType(), list);
                }
                list.add(sdk);
            }
        }
        configUsers.clear();
        if (config.getUsers() != null) {
            for (NutsUserConfig s : config.getUsers()) {
                configUsers.put(s.getUser(), s);
            }
        }
        removeAllRepositories(new NutsRemoveOptions().session(session));
        if (config.getRepositories() != null) {
            for (NutsRepositoryRef ref : config.getRepositories()) {
                NutsRepository r = this.createRepository(CoreNutsUtils.refToOptions(ref), getRepositoriesRoot(), null);
                addRepository(ref, r, session);
            }
        }
        if (fire) {
            fireConfigurationChanged();
        }
    }

    public DefaultNutsWorkspaceCurrentConfig current() {
        return currentConfig;
    }

    @Override
    public NutsWorkspaceStoredConfig stored() {
        return storedConfig;
    }

    @Override
    public void addImports(String... importExpressions) {
        Set<String> imports = new LinkedHashSet<>();
        if (storeModel.getImports() != null) {
            imports.addAll(storeModel.getImports());
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
        setImports(arr);
    }

    @Override
    public void removeAllImports() {
        setImports(null);
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

    @Override
    public void removeImports(String... importExpressions) {
        if (storeModel.getImports() != null) {
            Set<String> imports = new LinkedHashSet<>();
            for (String importExpression : storeModel.getImports()) {
                imports.addAll(parseImports(importExpression));
            }
            if (importExpressions != null) {
                for (String importExpression : importExpressions) {
                    imports.removeAll(parseImports(importExpression));
                }
            }
            String[] arr = imports.toArray(new String[0]);
//        Arrays.sort(arr);
            setImports(arr);
        }
    }

    @Override
    public void setImports(String[] imports) {
        Set<String> simports = new LinkedHashSet<>();
        if (imports != null) {
            for (String s : imports) {
                simports.addAll(parseImports(s));
            }
        }
        storeModel.setImports(new ArrayList<>(simports));
        fireConfigurationChanged();
    }

    @Override
    public Set<String> getImports() {
        if (cachedImports == null) {
            Set<String> all = new LinkedHashSet<>();
            if (storeModel.getImports() != null) {
                all.addAll(storeModel.getImports());
            }
            return cachedImports = Collections.unmodifiableSet(all);
        }
        return cachedImports;
    }

    @Override
    public Properties getEnv() {
        Properties p = new Properties();
        if (storeModel.getEnv() != null) {
            p.putAll(storeModel.getEnv());
        }
        return p;
    }

    public void setRepositoryEnabled(String repoName, boolean enabled) {
        NutsRepositoryRef e = repositoryRegistryHelper.findRepositoryRef(repoName);
        if (e != null && e.isEnabled() != enabled) {
            e.setEnabled(enabled);
            fireConfigurationChanged();
        }
    }

    @Override
    public String getUuid() {
        return storeModel.getUuid();
    }

    @Override
    public boolean addSdk(NutsSdkLocation location, NutsAddOptions options) {
        if (location != null) {
            if (CoreStringUtils.isBlank(location.getType())) {
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
            List<NutsSdkLocation> list = getSdk().get(location.getType());
            if (list == null) {
                list = new ArrayList<>();
                configSdks.put(location.getType(), list);
            }
            if (list.contains(location)) {
                return false;
            }
            list.add(location);
            fireConfigurationChanged();
            return true;
        }
        return false;
    }

    @Override
    public NutsSdkLocation findSdkByName(String type, String locationName) {
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
    public NutsSdkLocation findSdkByPath(String type, Path path) {
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
    public NutsSdkLocation findSdkByVersion(String type, String version) {
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

    @Override
    public NutsSdkLocation removeSdk(NutsSdkLocation location, NutsRemoveOptions options) {
        if (location != null) {
            List<NutsSdkLocation> list = getSdk().get(location.getType());
            if (list != null) {
                for (Iterator<NutsSdkLocation> iterator = list.iterator(); iterator.hasNext();) {
                    NutsSdkLocation location2 = iterator.next();
                    if (location2.equals(location)) {
                        iterator.remove();
                        fireConfigurationChanged();
                        return location2;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public NutsSdkLocation findSdk(String type, NutsSdkLocation location) {
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

    @Override
    public String[] getSdkTypes() {
        Set<String> s = getSdk().keySet();
        return s.toArray(new String[0]);
    }

//    @Override
//    public void setBootConfig(NutsBootConfig other) {
//        if (other == null) {
//            other = new NutsBootConfig();
//        }
//        if (!CoreStringUtils.isBlank(other.getRuntimeId())) {
//            NutsSession searchSession = ws.createSession().trace(false);
//            other.setRuntimeDependencies(ws.search().session(searchSession).addId(other.getRuntimeId())
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
    public NutsSdkLocation getSdk(String type, String requestedVersion) {
        type = toValidSdkName(type);
        NutsVersionFilter javaVersionFilter = ws.version().parse(requestedVersion).toFilter();
        NutsSdkLocation best = null;
        final NutsSession session = ws.createSession();
        for (NutsSdkLocation jdk : getSdks(type)) {
            String currVersion = jdk.getVersion();
            if (javaVersionFilter.accept(DefaultNutsVersion.valueOf(currVersion), session)) {
                if (best == null || DefaultNutsVersion.compareVersions(best.getVersion(), currVersion) < 0) {
                    best = jdk;
                }
            }
        }
        return best;
    }

    private String toValidSdkName(String type) {
        if (CoreStringUtils.isBlank(type)) {
            type = "java";
        } else {
            type = CoreStringUtils.trim(type);
        }
        return type;
    }

    @Override
    public NutsSdkLocation[] getSdks(String type) {
        type = toValidSdkName(type);
        List<NutsSdkLocation> list = getSdk().get(type);
        if (list == null) {
            return new NutsSdkLocation[0];
        }
        return list.toArray(new NutsSdkLocation[0]);
    }

    @Override
    public boolean isValidWorkspaceFolder() {
        Path file = getWorkspaceLocation().resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        return Files.isRegularFile(file);
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
    public boolean save(boolean force) {
        boolean ok = false;
        if (force || (!isReadOnly() && isConfigurationChanged())) {
            NutsWorkspaceUtils.checkReadOnly(ws);
            ws.security().checkAllowed(NutsConstants.Rights.SAVE_WORKSPACE, "save");
            storeModel.setConfigVersion(Nuts.getVersion());
            storeModel.setUsers(configUsers.isEmpty() ? null : new ArrayList<>(configUsers.values()));
            storeModel.setRepositories(new ArrayList<>(Arrays.asList(repositoryRegistryHelper.getRepositoryRefs())));
            List<NutsSdkLocation> plainSdks = new ArrayList<>();
            for (List<NutsSdkLocation> value : configSdks.values()) {
                plainSdks.addAll(value);
            }
            storeModel.setSdk(plainSdks);
            Path file = getWorkspaceLocation().resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
            if (CoreStringUtils.isBlank(storeModel.getConfigVersion())) {
                storeModel.setConfigVersion(current().getApiVersion());
            }
            ws.json().value(storeModel).print(file);
            configurationChanged = false;
            ok = true;
        }
        NutsException error = null;
        for (NutsRepository repo : getRepositories()) {
            try {
                ok |= repo.config().save(force);
            } catch (NutsException ex) {
                error = ex;
            }
        }
        if (error != null) {
            throw error;
        }

        return false;
    }

    @Override
    public void save() {
        save(true);
    }

    @Override
    public void onInitializeWorkspace(
            Path workspaceLocation,
            NutsWorkspaceOptions options,
            URL[] bootClassWorldURLs, ClassLoader bootClassLoader) {
        this.workspaceLocation = workspaceLocation;
        this.options = options;
        this.bootClassLoader = bootClassLoader;
        this.bootClassWorldURLs = bootClassWorldURLs == null ? null : Arrays.copyOf(bootClassWorldURLs, bootClassWorldURLs.length);
    }

    @Override
    public void setCurrentConfig(DefaultNutsWorkspaceCurrentConfig currentConfig) {
        this.currentConfig = currentConfig;
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

    @Override
    public URL[] getBootClassWorldURLs() {
        return bootClassWorldURLs == null ? null : Arrays.copyOf(bootClassWorldURLs, bootClassWorldURLs.length);
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

    @Override
    public NutsUserConfig[] getUsers() {
        return configUsers.values().toArray(new NutsUserConfig[0]);
    }

    @Override
    public NutsUserConfig getUser(String userId) {
        NutsUserConfig _config = getSecurity(userId);
        if (_config == null) {
            if (NutsConstants.Users.ADMIN.equals(userId) || NutsConstants.Users.ANONYMOUS.equals(userId)) {
                _config = new NutsUserConfig(userId, null, null, null);
                setUser(_config);
            }
        }
        return _config;
    }

    @Override
    public void setUser(NutsUserConfig config) {
        if (config != null) {
            configUsers.put(config.getUser(), config);
            fireConfigurationChanged();
        }
    }

    @Override
    public void setSecure(boolean secure) {
        if (secure != storeModel.isSecure()) {
            storeModel.setSecure(secure);
            fireConfigurationChanged();
        }
    }

    @Override
    public NutsCommandAliasFactoryConfig[] getCommandFactories() {
        if (storeModel.getCommandFactories() != null) {
            return storeModel.getCommandFactories().toArray(new NutsCommandAliasFactoryConfig[0]);
        }
        return new NutsCommandAliasFactoryConfig[0];
    }

    @Override
    public NutsRepositoryRef[] getRepositoryRefs() {
        return repositoryRegistryHelper.getRepositoryRefs();
    }

    @Override
    public void setEnv(String property, String value) {
        Properties env = storeModel.getEnv();
        if (CoreStringUtils.isBlank(value)) {
            if (env != null && env.contains(property)) {
                env.remove(property);
                fireConfigurationChanged();
            }
        } else {
            if (env == null) {
                env = new Properties();
                storeModel.setEnv(env);
            }
            String old = env.getProperty(property);
            if (!value.equals(old)) {
                env.setProperty(property, value);
                fireConfigurationChanged();
            }
        }
    }

    @Override
    public String getEnv(String property, String defaultValue) {
        Properties env = storeModel.getEnv();
        if (env == null) {
            return defaultValue;
        }
        String o = env.getProperty(property);
        if (CoreStringUtils.isBlank(o)) {
            return defaultValue;
        }
        return o;
    }

    @Override
    public void removeUser(String userId) {
        NutsUserConfig old = getSecurity(userId);
        if (old != null) {
            configUsers.remove(userId);
            fireConfigurationChanged();
        }
    }

    @Override
    public void fireConfigurationChanged() {
        cachedImports = null;
        setConfigurationChanged(true);
    }

    @Override
    public void setUsers(NutsUserConfig[] users) {
        for (NutsUserConfig u : getUsers()) {
            removeUser(u.getUser());
        }
        for (NutsUserConfig conf : users) {
            setUser(conf);
        }
    }

    @Override
    public void setLogLevel(Level level) {
        Logger rootLogger = Logger.getLogger("");
        if (level == null) {
            level = Level.WARNING;
        }
        rootLogger.setLevel(level);
        for (Handler handler : rootLogger.getHandlers()) {
            handler.setLevel(level);
        }
    }

    @Override
    public NutsSdkLocation[] searchSdkLocations(String sdkType, PrintStream out) {
        if ("java".equals(sdkType)) {
            return NutsWorkspaceUtils.searchJdkLocations(ws, out);
        }
        return new NutsSdkLocation[0];
    }

    @Override
    public NutsSdkLocation[] searchSdkLocations(String sdkType, Path path, PrintStream out) {
        if ("java".equals(sdkType)) {
            return NutsWorkspaceUtils.searchJdkLocations(ws, path, out);
        }
        return new NutsSdkLocation[0];
    }

    @Override
    public NutsSdkLocation resolveSdkLocation(String sdkType, Path path) {
        if ("java".equals(sdkType)) {
            return NutsWorkspaceUtils.resolveJdkLocation(ws, path);
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
    public NutsWorkspaceCommandAlias findCommandAlias(String name) {
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

    private NutsWorkspaceCommandAlias toDefaultNutsWorkspaceCommand(NutsCommandAliasConfig c) {
        if (c.getCommand() == null || c.getCommand().length == 0) {
            LOG.log(Level.WARNING, "Invalid Command Definition ''{0}''. Missing Command. Ignored", c.getName());
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
    public boolean addCommandAlias(NutsCommandAliasConfig command, NutsAddOptions options) {
        if (command == null
                || CoreStringUtils.isBlank(command.getName())
                || command.getName().contains(" ") || command.getName().contains(".")
                || command.getName().contains("/") || command.getName().contains("\\")
                || command.getCommand() == null
                || command.getCommand().length == 0) {
            throw new NutsIllegalArgumentException(ws, "Invalid command alias " + (command == null ? "<NULL>" : command.getName()));
        }
        boolean forced = false;
        if (options == null) {
            options = new NutsAddOptions();
        }
        NutsSession session = NutsWorkspaceUtils.validateSession(ws, options.getSession());
        if (defaultCommandFactory.findCommand(command.getName(), ws) != null) {
            if (session.isForce()) {
                forced = true;
                removeCommandAlias(command.getName(),
                        new NutsRemoveOptions().session(session)
                );
            } else {
                throw new NutsIllegalArgumentException(ws, "Command alias already exists " + command.getName());
            }
        }
        defaultCommandFactory.installCommand(command);
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
        if (options == null) {
            options = new NutsRemoveOptions();
        }
        NutsSession session = NutsWorkspaceUtils.validateSession(ws, options.getSession());
        NutsCommandAliasConfig command = defaultCommandFactory.findCommand(name, ws);
        if (command == null) {
            throw new NutsIllegalArgumentException(ws, "Command alias does not exists " + name);
        }
        defaultCommandFactory.uninstallCommand(name);
        if (session.isPlainTrace()) {
            PrintStream out = CoreIOUtils.resolveOut(session);
            out.printf("[[uninstall]] command alias ==%s==%n", name);
        }
        return true;
    }

    @Override
    public List<NutsWorkspaceCommandAlias> findCommandAliases() {
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
    public void addCommandAliasFactory(NutsCommandAliasFactoryConfig commandFactoryConfig, NutsAddOptions options) {
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
        List<NutsCommandAliasFactoryConfig> commandFactories = storeModel.getCommandFactories();
        if (commandFactories == null) {
            commandFactories = new ArrayList<>();
            storeModel.setCommandFactories(commandFactories);
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
        fireConfigurationChanged();
    }

    @Override
    public boolean removeCommandAliasFactory(String factoryId, NutsRemoveOptions options) {
        if (factoryId == null || factoryId.isEmpty()) {
            throw new NutsIllegalArgumentException(ws, "Invalid WorkspaceCommandFactory " + factoryId);
        }
        NutsWorkspaceCommandFactory removeMe = null;
        NutsCommandAliasFactoryConfig removeMeConfig = null;
        for (Iterator<NutsWorkspaceCommandFactory> iterator = commandFactories.iterator(); iterator.hasNext();) {
            NutsWorkspaceCommandFactory factory = iterator.next();
            if (factoryId.equals(factory.getFactoryId())) {
                removeMe = factory;
                iterator.remove();
                fireConfigurationChanged();
                break;
            }
        }
        List<NutsCommandAliasFactoryConfig> _commandFactories = storeModel.getCommandFactories();
        if (_commandFactories != null) {
            for (Iterator<NutsCommandAliasFactoryConfig> iterator = _commandFactories.iterator(); iterator.hasNext();) {
                NutsCommandAliasFactoryConfig commandFactory = iterator.next();
                if (factoryId.equals(commandFactory.getFactoryId())) {
                    removeMeConfig = commandFactory;
                    iterator.remove();
                    fireConfigurationChanged();
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
    public List<NutsWorkspaceCommandAlias> findCommandAliases(NutsId id) {
        HashMap<String, NutsWorkspaceCommandAlias> all = new HashMap<>();
        for (NutsCommandAliasConfig command : defaultCommandFactory.findCommands(id, ws)) {
            all.put(command.getName(), toDefaultNutsWorkspaceCommand(command));
        }
        return new ArrayList<>(all.values());
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
        switch (folderType) {
            case CACHE:
                return storeLocation.resolve(NutsConstants.Folders.ID).resolve(getDefaultIdBasedir(id));
            case CONFIG:
                return storeLocation.resolve(NutsConstants.Folders.ID).resolve(getDefaultIdBasedir(id));
        }
        return storeLocation.resolve(getDefaultIdBasedir(id));
    }

    @Override
    public Path getHomeLocation(NutsStoreLocation folderType) {
        return current().getHomeLocation(folderType);
    }

    @Override
    public long getCreationStartTimeMillis() {
        return startCreateTime;
    }

    @Override
    public void setStartCreateTimeMillis(long startCreateTime) {
        this.startCreateTime = startCreateTime;
    }

    @Override
    public long getCreationFinishTimeMillis() {
        return endCreateTime;
    }

    @Override
    public void setEndCreateTimeMillis(long endCreateTime) {
        this.endCreateTime = endCreateTime;
    }

    @Override
    public long getCreationTimeMillis() {
        return endCreateTime - startCreateTime;
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
        ((NutsAuthenticationAgentSpi) supported).setWorkspace(ws);
        return supported;
    }

    @Override
    public void setStoreLocation(NutsStoreLocation folderType, String location) {
        if (folderType == null) {
            throw new NutsIllegalArgumentException(ws, "Invalid store root folder null");
        }
        onPreUpdateConfig();
        storeModel.setStoreLocations(new NutsStoreLocationsMap(storeModel.getStoreLocations()).set(folderType, location).toMapOrNull());
        onPostUpdateConfig();
    }

    private void onPreUpdateConfig() {
        preUpdateConfigStoreLocations = new NutsStoreLocationsMap(currentConfig.getStoreLocations());
    }

    private void onPostUpdateConfig() {
        preUpdateConfigStoreLocations = new NutsStoreLocationsMap(currentConfig.getStoreLocations());
        DefaultNutsWorkspaceCurrentConfig d = (DefaultNutsWorkspaceCurrentConfig) currentConfig;
        d.setUserStoreLocations(new NutsHomeLocationsMap(storeModel.getStoreLocations()).toMapOrNull());
        d.setHomeLocations(new NutsHomeLocationsMap(storeModel.getHomeLocations()).toMapOrNull());
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
        fireConfigurationChanged();
    }

    @Override
    public void setHomeLocation(NutsOsFamily layout, NutsStoreLocation folder, String location) {
        if (folder == null) {
            throw new NutsIllegalArgumentException(ws, "Invalid store root folder null");
        }
        onPreUpdateConfig();
        storeModel.setHomeLocations(new NutsHomeLocationsMap(storeModel.getHomeLocations()).set(layout, folder, location).toMapOrNull());
        onPostUpdateConfig();
    }

    @Override
    public void setStoreLocationStrategy(NutsStoreLocationStrategy strategy) {
        if (strategy == null) {
            strategy = NutsStoreLocationStrategy.EXPLODED;
        }
        onPreUpdateConfig();
        storeModel.setStoreLocationStrategy(strategy);
        onPostUpdateConfig();
    }

    @Override
    public void setStoreLocationLayout(NutsOsFamily layout) {
        onPreUpdateConfig();
        storeModel.setStoreLocationLayout(layout);
        onPostUpdateConfig();
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return ws;
    }

    @Override
    public String getDefaultIdBasedir(NutsId id) {
        NutsWorkspaceUtils.checkSimpleNameNutsId(getWorkspace(), id);
        String groupId = id.getGroup();
        String artifactId = id.getName();
        String plainIdPath = groupId.replace('.', '/') + "/" + artifactId;
        if (id.getVersion().isBlank()) {
            return plainIdPath;
        }
        String version = id.getVersion().getValue();
        String a = CoreNutsUtils.trimToNullAlternative(id.getAlternative());
        String x = plainIdPath + "/" + version;
        if (a != null) {
            x += "/" + a;
        }
        return x;
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
        return id.getName() + "-" + id.getVersion().getValue() + classifier + ext;
    }

    @Override
    public String getDefaultIdComponentExtension(String packaging) {
        if (CoreStringUtils.isBlank(packaging)) {
            throw new NutsIllegalArgumentException(ws, "Unsupported empty Packaging");
        }
        switch (packaging) {
            case "bundle":
            case "nuts-extension":
            case "maven-archetype":
            case "maven-plugin":
            case "ejb":
                return ".jar";
            case "dll":
            case "so":
            case "jnilib":
                return "-natives.jar";
            case "war":
                return ".war";
            case "jar":
                return ".jar";
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
        Map<String, String> q = id.getQueryMap();
        String f = CoreStringUtils.trim(q.get(NutsConstants.QueryKeys.FACE));
        switch (f) {
            case NutsConstants.QueryFaces.DESCRIPTOR: {
                return NutsConstants.Files.DESCRIPTOR_FILE_EXTENSION;
            }
            case NutsConstants.QueryFaces.DESC_HASH: {
                return ".nuts.sha1";
            }
            case NutsConstants.QueryFaces.CATALOG: {
                return ".catalog";
            }
            case NutsConstants.QueryFaces.COMPONENT_HASH: {
                return getDefaultIdExtension(id.setFaceComponent()) + ".sha1";
            }
            case NutsConstants.QueryFaces.COMPONENT: {
                return getDefaultIdComponentExtension(q.get(NutsConstants.QueryKeys.PACKAGING));
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
    public NutsId createComponentFaceId(NutsId id, NutsDescriptor desc) {
        Map<String, String> q = id.getQueryMap();
        q.put(NutsConstants.QueryKeys.PACKAGING, CoreStringUtils.trim(desc.getPackaging()));
//        q.put(NutsConstants.QUERY_EXT,CoreStringUtils.trim(descriptor.getExt()));
        q.put(NutsConstants.QueryKeys.FACE, NutsConstants.QueryFaces.COMPONENT);
        return id.setQuery(q);
    }

    @Override
    public boolean isConfigurationChanged() {
        return configurationChanged;
    }

    public NutsWorkspaceConfigManager setConfigurationChanged(boolean configurationChanged) {
        this.configurationChanged = configurationChanged;
        return this;
    }

    @Override
    public Path getConfigFile() {
        return getWorkspaceLocation().resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
    }

    @Override
    public boolean loadWorkspace(NutsSession session) {
        try {
            Path file = getConfigFile();
            NutsWorkspaceConfig _config = Files.isRegularFile(file) ? parseConfigForAnyVersion(file) : null;
            if (_config != null) {
                setCurrentConfig(new DefaultNutsWorkspaceCurrentConfig(ws)
                        .merge(_config)
                        .mergeRuntime(options())
                        .build(getWorkspaceLocation())
                );
                setConfig(_config, session, false);
                configurationChanged = false;
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            onLoadWorkspaceError(ex);
        }
        return false;
    }

    private void onLoadWorkspaceError(Throwable ex) {
        NutsWorkspaceConfigManager wconfig = this;
        Path file = this.getConfigFile();
        if (wconfig.isReadOnly()) {
            throw new UncheckedIOException("Unable to load config file " + file.toString(), new IOException(ex));
        }
        if (file == null) {
            file = wconfig.getWorkspaceLocation().resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
        }
        String fileName = "nuts-workspace-" + Instant.now().toString();
        LOG.log(Level.SEVERE, "Erroneous config file. Unable to load file {0} : {1}", new Object[]{file, ex.toString()});
        Path logError = wconfig.getStoreLocation(wconfig.getApiId(), NutsStoreLocation.LOG).resolve("invalid-config");
        try {
            Files.createDirectories(logError);
        } catch (IOException ex1) {
            throw new UncheckedIOException("Unable to log workspace error while loading config file " + file.toString() + " : " + ex1.toString(), new IOException(ex));
        }
        Path newfile = logError.resolve(fileName + ".json");
        LOG.log(Level.SEVERE, "Erroneous config file will be replaced by a fresh one. Old config is copied to {0}", newfile.toString());
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
        LOG.log(Level.SEVERE, "Erroneous config file. Unable to load file {0} : {1}", new Object[]{file, ex.toString()});
        Path logError = wconfig.getStoreLocation(wconfig.getApiId(), NutsStoreLocation.LOG).resolve("invalid-config");
        try {
            Files.createDirectories(logError);
        } catch (IOException ex1) {
            throw new UncheckedIOException("Unable to log repository error while loading config file " + file.toString() + " : " + ex1.toString(), new IOException(ex));
        }
        Path newfile = logError.resolve(fileName + ".json");
        LOG.log(Level.SEVERE, "Erroneous repository config file will be replaced by a fresh one. Old config is copied to {0}", newfile.toString());
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

    private NutsWorkspaceConfig parseConfigForAnyVersion(Path file) {
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(file);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        try {
            Map<String, Object> a_config0 = ws.json().parse(new InputStreamReader(new ByteArrayInputStream(bytes)), Map.class);
            String version = (String) a_config0.get("configVersion");
            if (version == null) {
                version = (String) a_config0.get("createApiVersion");
                if (version == null) {
                    version = Nuts.getVersion();
                }
            }
            int buildNumber = CoreNutsUtils.getApiVersionOrdinalNumber(version);
            if (buildNumber < 506) {
                //deprecated, will ignore
                return ws.json().parse(new InputStreamReader(new ByteArrayInputStream(bytes)), NutsWorkspaceConfig502.class).toWorkspaceConfig();
            }
            return ws.json().parse(new InputStreamReader(new ByteArrayInputStream(bytes)), NutsWorkspaceConfig.class);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Erroneous config file. Unable to load file {0} : {1}", new Object[]{file, ex.toString()});
            throw new UncheckedIOException("Unable to load config file " + file.toString(), new IOException(ex));
        }
    }

    public Map<String, List<NutsSdkLocation>> getSdk() {
        return configSdks;
    }

    public NutsUserConfig getSecurity(String id) {
        return configUsers.get(id);
    }

    @Override
    public void setBootApiVersion(String value) {
        if (!Objects.equals(value, storeModel.getApiVersion())) {
            storeModel.setApiVersion(value);
            fireConfigurationChanged();
        }
    }

    @Override
    public void setBootRuntime(String value) {
        if (!Objects.equals(value, storeModel.getRuntimeId())) {
            storeModel.setRuntimeId(value);
            fireConfigurationChanged();
        }
    }

    @Override
    public void setBootRuntimeDependencies(String value) {
        if (!Objects.equals(value, storeModel.getRuntimeDependencies())) {
            storeModel.setRuntimeDependencies(value);
            fireConfigurationChanged();
        }
    }

    @Override
    public void setBootRepositories(String value) {
        if (!Objects.equals(value, storeModel.getBootRepositories())) {
            storeModel.setBootRepositories(value);
            fireConfigurationChanged();
        }
    }

    @Override
    public NutsWorkspaceListManager createWorkspaceListManager(String name) {
        return new DefaultNutsWorkspaceListManager(ws, name);
    }

//    @Override
//    public boolean isGlobal() {
//        return config.isGlobal();
//    }
    @Override
    public NutsIndexStoreClientFactory getIndexStoreClientFactory() {
        return indexStoreClientFactory;
    }

    @Override
    public NutsWorkspaceConfigManager removeRepository(String repositoryId, NutsRemoveOptions options) {
        ws.security().checkAllowed(NutsConstants.Rights.REMOVE_REPOSITORY, "remove-repository");
        if (options == null) {
            options = new NutsRemoveOptions();
        }
        if (options.getSession() == null) {
            options.setSession(ws.createSession());
        }
        final NutsRepository repository = repositoryRegistryHelper.removeRepository(repositoryId);
        if (repository != null) {
            NutsWorkspaceExt.of(ws).fireOnRemoveRepository(new DefaultNutsWorkspaceEvent(options.getSession(), repository, "repository", repository, null));
        }
        return this;
    }

    @Override
    public Path getRepositoriesRoot() {
        return ws.config().getStoreLocation(NutsStoreLocation.CONFIG).resolve(NutsConstants.Folders.REPOSITORIES);
    }

    @Override
    public NutsRepository findRepository(String repositoryNameOrId, boolean transitive) {
        NutsRepository y = repositoryRegistryHelper.findRepository(repositoryNameOrId);
        if (y != null) {
            return y;
        }
        if (transitive) {
            for (NutsRepository child : repositoryRegistryHelper.getRepositories()) {
                final NutsRepository m = child.config().findMirror(repositoryNameOrId, true);
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
    public NutsRepository getRepository(String repositoryIdOrName) throws NutsRepositoryNotFoundException {
        return getRepository(repositoryIdOrName, false);
    }

    @Override
    public NutsRepository getRepository(String repositoryName, boolean transitive) {
        NutsRepository r = findRepository(repositoryName, transitive);
        if (r != null) {
            return r;
        }
        throw new NutsRepositoryNotFoundException(ws, repositoryName);
    }

    @Override
    public NutsRepository[] getRepositories() {
        return repositoryRegistryHelper.getRepositories();
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
    public NutsRepository addRepository(NutsRepositoryDefinition definition) {
        return addRepository(CoreNutsUtils.defToOptions(definition));
    }

    @Override
    public NutsRepository addRepository(NutsCreateRepositoryOptions options) {
        if (excludedRepositoriesSet != null && excludedRepositoriesSet.contains(options.getName())) {
            return null;
        }
        if (options.getSession() == null) {
            options.setSession(ws.createSession());
        }
        if (options.isProxy()) {
            if (options.getConfig() == null) {
                NutsRepository proxy = addRepository(
                        new NutsCreateRepositoryOptions()
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
                    //mainly becausse path is not accessible
                    //or the repository is excluded
                    return null;
                }
                //Dont need to add mirror if repository is already loadable from config!
                final String m2 = options.getName() + "-ref";
                if (proxy.config().findMirror(m2, false) == null) {
                    proxy.config().addMirror(new NutsCreateRepositoryOptions()
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
                        new NutsCreateRepositoryOptions()
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
                if (proxy.config().findMirror(m2, false) == null) {
                    proxy.config().addMirror(new NutsCreateRepositoryOptions()
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
            addRepository(ref, r, options.getSession());
            return r;
        }
    }

    protected void addRepository(NutsRepositoryRef ref, NutsRepository repo, NutsSession session) {
        repositoryRegistryHelper.addRepository(ref, repo);
        if (repo != null) {
            NutsWorkspaceExt.of(ws).fireOnAddRepository(
                    new DefaultNutsWorkspaceEvent(session, repo, "repository", null, repo)
            );
        }
    }

    @Override
    public Set<String> getAvailableArchetypes() {
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
        return ws.io().path(ws.io().expandPath(repositoryLocation,
                root != null ? root.toString() : configManager.getStoreLocation(NutsStoreLocation.CONFIG)
                        .resolve(NutsConstants.Folders.REPOSITORIES).toString()));
    }

    private static class DummyNutsIndexStoreClient implements NutsIndexStoreClient {

        @Override
        public List<NutsId> searchVersions(NutsId id, NutsRepositorySession session) {
            return null;
        }

        @Override
        public Iterator<NutsId> search(NutsIdFilter filter, NutsRepositorySession session) {
            return null;
        }

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public void setEnabled(boolean enabled) {
        }

        @Override
        public void invalidate(NutsId id) {

        }

        @Override
        public void revalidate(NutsId id) {

        }

        @Override
        public boolean subscribe() {
            return false;
        }

        @Override
        public void unsubscribe() {

        }

        @Override
        public boolean isSubscribed(NutsRepository repository) {
            return false;
        }
    }

    private static class DummyNutsIndexStoreClientFactory implements NutsIndexStoreClientFactory {

        @Override
        public int getSupportLevel(NutsSupportLevelContext<Object> criteria) {
            return DEFAULT_SUPPORT;
        }

        @Override
        public NutsIndexStoreClient createIndexStoreClient(NutsRepository repository) {
            return new DummyNutsIndexStoreClient();
        }
    }

    @Override
    public NutsRepository createRepository(NutsCreateRepositoryOptions options, Path rootFolder, NutsRepository parentRepository) {
        options = options.copy();
        try {
            NutsRepositoryConfig conf = options.getConfig();
            if (conf == null) {
                options.setLocation(CoreIOUtils.resolveRepositoryPath(options, rootFolder, ws));
                conf = loadRepository(ws.io().path(options.getLocation(), NutsConstants.Files.REPOSITORY_CONFIG_FILE_NAME), options.getName(), ws);
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
    public NutsWorkspaceConfig getStoredConfig() {
        return storeModel;
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
                Map<String, Object> a_config0 = ws.json().parse(new InputStreamReader(new ByteArrayInputStream(bytes)), Map.class);
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

    private class NutsWorkspaceStoredConfigImpl implements NutsWorkspaceStoredConfig {

        public NutsWorkspaceStoredConfigImpl() {
        }

        @Override
        public String getName() {
            return getStoredConfig().getName();
        }

        @Override
        public NutsStoreLocationStrategy getStoreLocationStrategy() {
            return getStoredConfig().getStoreLocationStrategy();
        }

        @Override
        public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy() {
            return getStoredConfig().getStoreLocationStrategy();
        }

        @Override
        public NutsOsFamily getStoreLocationLayout() {
            return getStoredConfig().getStoreLocationLayout();
        }

        @Override
        public Map<String, String> getStoreLocations() {
            return getStoredConfig().getStoreLocations();
        }

        @Override
        public String getStoreLocation(NutsStoreLocation folderType) {
            return new NutsStoreLocationsMap(getStoredConfig().getStoreLocations()).get(folderType);
        }

        @Override
        public String getHomeLocation(NutsOsFamily layout, NutsStoreLocation folderType) {
            return new NutsHomeLocationsMap(getStoredConfig().getHomeLocations()).get(layout, folderType);
        }

        @Override
        public Map<String, String> getHomeLocations() {
            return getStoredConfig().getHomeLocations();
        }

        @Override
        public NutsId getApiId() {
            String v = getStoredConfig().getApiVersion();
            return v == null ? null : CoreNutsUtils.parseNutsId(NutsConstants.Ids.NUTS_API + "#" + v);
        }

        @Override
        public NutsId getRuntimeId() {
            String v = getStoredConfig().getRuntimeId();
            return v == null ? null : v.contains("#")
                    ? CoreNutsUtils.parseNutsId(v)
                    : CoreNutsUtils.parseNutsId(NutsConstants.Ids.NUTS_RUNTIME + "#" + v);
        }

        @Override
        public String getRuntimeDependencies() {
            return getStoredConfig().getRuntimeDependencies();
        }

        @Override
        public String getExtensionDependencies() {
            return getStoredConfig().getExtensionDependencies();
        }

        @Override
        public String getBootRepositories() {
            return getStoredConfig().getBootRepositories();
        }

        @Override
        public String getJavaCommand() {
            return getStoredConfig().getJavaCommand();
        }

        @Override
        public String getJavaOptions() {
            return getStoredConfig().getJavaOptions();
        }

        @Override
        public boolean isGlobal() {
            return getStoredConfig().isGlobal();
        }

    }

    @Override
    public Path getStoreLocation(NutsStoreLocation folderType) {
        return current().getStoreLocation(folderType);
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
        return current().getApiVersion();
    }

    @Override
    public NutsId getApiId() {
        return current().getApiId();
    }

    @Override
    public NutsId getRuntimeId() {
        return current().getRuntimeId();
    }

    @Override
    public String getRuntimeDependencies() {
        return current().getRuntimeDependencies();
    }

    @Override
    public String getExtensionDependencies() {
        return current().getExtensionDependencies();
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
    public NutsOsFamily getPlatformOsFamily() {
        return current().getPlatformOsFamily();
    }

    @Override
    public NutsId getPlatformOs() {
        return current().getPlatformOs();
    }

    @Override
    public NutsId getPlatformOsDist() {
        return current().getPlatformOsDist();
    }

    @Override
    public NutsId getPlatformArch() {
        return current().getPlatformArch();
    }

}
