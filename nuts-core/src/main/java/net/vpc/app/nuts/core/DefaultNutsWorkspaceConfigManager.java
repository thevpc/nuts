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
package net.vpc.app.nuts.core;

import java.io.ByteArrayInputStream;
import net.vpc.app.nuts.core.spi.NutsWorkspaceConfigManagerExt;
import net.vpc.app.nuts.core.util.io.CoreSecurityUtils;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.common.CorePlatformUtils;
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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.vpc.app.nuts.core.compat.NutsWorkspaceConfig502;
import net.vpc.app.nuts.core.repos.NutsRepositoryRegistryHelper;
import net.vpc.app.nuts.core.spi.NutsAuthenticationAgentSpi;
import net.vpc.app.nuts.core.spi.NutsWorkspaceExt;

/**
 * @author vpc
 */
public class DefaultNutsWorkspaceConfigManager implements NutsWorkspaceConfigManagerExt {

    public static final Logger LOG = Logger.getLogger(DefaultNutsWorkspaceConfigManager.class.getName());

    private final DefaultNutsWorkspace ws;
    private NutsBootContext runningContext;
    private ClassLoader bootClassLoader;
    private URL[] bootClassWorldURLs;
    protected NutsWorkspaceConfig config = new NutsWorkspaceConfig();
    private final List<NutsWorkspaceCommandFactory> commandFactories = new ArrayList<>();
    private final ConfigNutsWorkspaceCommandFactory defaultCommandFactory = new ConfigNutsWorkspaceCommandFactory(this);
    private boolean configurationChanged = false;
    private Path workspaceLocation;
    private NutsWorkspaceOptions options;
    private NutsId platformOs;
    private NutsOsFamily platformOsFamily;
    private NutsId platformArch;
    private NutsId platformOsdist;
    private String[] platformOsPath = new String[NutsStoreLocation.values().length];
    private long startCreateTime;
    private long endCreateTime;
    private final Map<String, List<NutsSdkLocation>> configSdks = new LinkedHashMap<>();
    private final Map<String, NutsUserConfig> configUsers = new LinkedHashMap<>();
    private final NutsRepositoryRegistryHelper repositoryRegistryHelper;

    private NutsIndexStoreClientFactory indexStoreClientFactory;
    private boolean global;
    private Set<String> cachedImports;
    private Set<String> excludedRepositoriesSet = new HashSet<>();

    protected DefaultNutsWorkspaceConfigManager(final DefaultNutsWorkspace ws) {
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
        this.config = config;
        if (CoreStringUtils.isBlank(config.getUuid())) {
            config.setUuid(UUID.randomUUID().toString());
            fire = true;
        }
        this.global = config.isGlobal();
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

//    @Override
//    public NutsBootContext getRunningContext() {
//        return runningBootConfig;
//    }
    @Override
    public NutsBootContext getContext(NutsBootContextType contextType) {
        if (contextType == null) {
            contextType = NutsBootContextType.RUNTIME;
        }
        switch (contextType) {
            case RUNTIME: {
                return runningContext;
            }
            case CONFIG: {
                return new DefaultNutsBootContext(ws).merge(config).build(getWorkspaceLocation());
            }

        }
        throw new NutsUnsupportedArgumentException(ws, "" + contextType);
    }

    @Override
    public NutsBootConfig getBootConfig() {
        return new NutsBootConfig()
                .setApiVersion(config.getBootApiVersion())
                .setRuntimeId(config.getBootRuntime())
                .setRepositories(config.getBootRepositories())
                .setJavaCommand(config.getBootJavaCommand())
                .setJavaOptions(config.getBootJavaOptions());
    }

    @Override
    public void addImports(String... importExpressions) {
        Set<String> imports = new LinkedHashSet<>();
        if (config.getImports() != null) {
            imports.addAll(config.getImports());
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
        if (config.getImports() != null) {
            Set<String> imports = new LinkedHashSet<>();
            for (String importExpression : config.getImports()) {
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
        config.setImports(new ArrayList<>(simports));
        fireConfigurationChanged();
    }

    @Override
    public Set<String> getImports() {
        if (cachedImports == null) {
            Set<String> all = new LinkedHashSet<>();
            if (config.getImports() != null) {
                all.addAll(config.getImports());
            }
            return cachedImports = Collections.unmodifiableSet(all);
        }
        return cachedImports;
    }

    @Override
    public Properties getEnv() {
        Properties p = new Properties();
        if (config.getEnv() != null) {
            p.putAll(config.getEnv());
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
        return config.getUuid();
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
    public NutsSdkLocation findSdkByName(String sdkType, String locationName) {
        if (locationName != null) {
            List<NutsSdkLocation> list = getSdk().get(sdkType);
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
    public NutsSdkLocation findSdkByPath(String sdkType, Path path) {
        if (path != null) {
            List<NutsSdkLocation> list = getSdk().get(sdkType);
            if (list != null) {
                for (NutsSdkLocation location : list) {
                    if (location.getPath().equals(path)) {
                        return location;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public NutsSdkLocation findSdkByVersion(String sdkType, String version) {
        if (version != null) {
            List<NutsSdkLocation> list = getSdk().get(sdkType);
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
    public NutsSdkLocation findSdk(String sdkType, NutsSdkLocation location) {
        if (location != null) {
            List<NutsSdkLocation> list = getSdk().get(sdkType);
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

    @Override
    public void setBootConfig(NutsBootConfig other) {
        if (other == null) {
            other = new NutsBootConfig();
        }
        if (!CoreStringUtils.isBlank(other.getRuntimeId())) {
            NutsSession searchSession = ws.createSession().trace(false);
            other.setRuntimeDependencies(ws.search().session(searchSession).addId(other.getRuntimeId())
                    .scope(NutsDependencyScopePattern.RUN)
                    .inlineDependencies()
                    .duplicates(false)
                    .getResultDefinitions().stream()
                    .map(x -> x.getId().getLongName())
                    .collect(Collectors.joining(";"))
            );
        } else {
            other.setRuntimeDependencies("");
        }
        config.setBootApiVersion(other.getApiVersion());
        config.setBootRuntime(other.getRuntimeId());
        config.setBootRuntimeDependencies(other.getRuntimeDependencies());
        config.setBootRepositories(other.getRepositories());
        fireConfigurationChanged();
    }

    @Override
    public NutsSdkLocation getSdk(String type, String requestedVersion) {
        NutsVersionFilter javaVersionFilter = ws.version().parse(requestedVersion).toFilter();
        NutsSdkLocation best = null;
        final NutsSession session = ws.createSession();
        for (NutsSdkLocation jdk : getSdks("java")) {
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
    public NutsSdkLocation[] getSdks(String type) {
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
            config.setUsers(configUsers.isEmpty() ? null : new ArrayList<>(configUsers.values()));
            config.setRepositories(new ArrayList<>(Arrays.asList(repositoryRegistryHelper.getRepositoryRefs())));
            List<NutsSdkLocation> plainSdks = new ArrayList<>();
            for (List<NutsSdkLocation> value : configSdks.values()) {
                plainSdks.addAll(value);
            }
            config.setSdk(plainSdks);
            Path file = getWorkspaceLocation().resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
            if (CoreStringUtils.isBlank(config.getCreateApiVersion())) {
                config.setCreateApiVersion(getApiId().getVersion().getValue());
            }
            ws.json().value(config).print(file);
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
    public void setRunningContext(NutsBootContext runningContext) {
        this.runningContext = runningContext;
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
        NutsBootContext c = getContext(NutsBootContextType.RUNTIME);
        if (c != null) {
            s1 = String.valueOf(c.getApiId());
            s2 = String.valueOf(c.getRuntimeId());
        }
        return "NutsWorkspaceConfig{"
                + "workspaceBootId=" + s1
                + ", workspaceRuntimeId=" + s2
                + ", workspace=" + ((runningContext == null) ? "NULL" : ("'" + getWorkspaceLocation()+ '\''))
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
        if (secure != config.isSecure()) {
            config.setSecure(secure);
            fireConfigurationChanged();
        }
    }

    @Override
    public NutsCommandAliasFactoryConfig[] getCommandFactories() {
        if (config.getCommandFactories() != null) {
            return config.getCommandFactories().toArray(new NutsCommandAliasFactoryConfig[0]);
        }
        return new NutsCommandAliasFactoryConfig[0];
    }

    @Override
    public NutsRepositoryRef[] getRepositoryRefs() {
        return repositoryRegistryHelper.getRepositoryRefs();
    }

    @Override
    public void setEnv(String property, String value) {
        Properties env = config.getEnv();
        if (CoreStringUtils.isBlank(value)) {
            if (env != null && env.contains(property)) {
                env.remove(property);
                fireConfigurationChanged();
            }
        } else {
            if (env == null) {
                env = new Properties();
                config.setEnv(env);
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
        Properties env = config.getEnv();
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

    @Override
    public char[] decryptString(char[] input) {
        if (input == null || input.length == 0) {
            return new char[0];
        }
        return CoreIOUtils.bytesToChars(decryptString(CoreIOUtils.charsToBytes(input)));
    }

    @Override
    public char[] encryptString(char[] input) {
        if (input == null || input.length == 0) {
            return new char[0];
        }
        return CoreIOUtils.bytesToChars(encryptString(CoreIOUtils.charsToBytes(input)));
    }

    @Override
    public byte[] decryptString(byte[] input) {
        if (input == null || input.length == 0) {
            return new byte[0];
        }
        String passphrase = getEnv(CoreSecurityUtils.ENV_KEY_PASSPHRASE, CoreSecurityUtils.DEFAULT_PASSPHRASE);
        return CoreSecurityUtils.httpDecrypt(input, passphrase);
    }

    @Override
    public byte[] encryptString(byte[] input) {
        if (input == null || input.length == 0) {
            return new byte[0];
        }
        String passphrase = getEnv(CoreSecurityUtils.ENV_KEY_PASSPHRASE, CoreSecurityUtils.DEFAULT_PASSPHRASE);
        return CoreSecurityUtils.httpEncrypt(input, passphrase);
    }

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
        List<NutsCommandAliasFactoryConfig> commandFactories = config.getCommandFactories();
        if (commandFactories == null) {
            commandFactories = new ArrayList<>();
            config.setCommandFactories(commandFactories);
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
        List<NutsCommandAliasFactoryConfig> _commandFactories = config.getCommandFactories();
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
    public Path getHomeLocation(NutsStoreLocation folderType) {
        return ws.io().path(NutsPlatformUtils.resolveHomeFolder(
                runningContext.getStoreLocationLayout(),
                folderType, runningContext.getHomeLocations(),
                runningContext.isGlobal(),
                runningContext.getName()
        ));
    }

    @Override
    public Path getStoreLocation(NutsStoreLocation folderType) {
        if (folderType == null) {
            folderType = NutsStoreLocation.APPS;
        }
        return ws.io().path(runningContext.getStoreLocation(folderType));
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
    public Path getStoreLocation(NutsId id, Path path) {
        return path.resolve(getDefaultIdBasedir(id));
    }

    @Override
    public NutsId getPlatformOs() {
        if (platformOs == null) {
            platformOs = ws.id().parse(CorePlatformUtils.getPlatformOs());
        }
        return platformOs;
    }

    @Override
    public NutsId getPlatformOsDist() {
        if (platformOsdist == null) {
            platformOsdist = ws.id().parse(CorePlatformUtils.getPlatformOsDist());
        }
        return platformOsdist;
    }

    @Override
    public String getPlatformOsHome(NutsStoreLocation location) {
        int ordinal = location.ordinal();
        String s = platformOsPath[ordinal];
        if (s == null) {
            platformOsPath[ordinal] = s = NutsPlatformUtils.getPlatformOsGlobalHome(location, runningContext.getName());
        }
        return s;
    }

    @Override
    public NutsId getPlatformArch() {
        if (platformArch == null) {
            platformArch = ws.id().parse(CorePlatformUtils.getPlatformArch());
        }
        return platformArch;
    }

    @Override
    public NutsOsFamily getPlatformOsFamily() {
        if (platformOsFamily == null) {
            platformOsFamily = NutsPlatformUtils.getPlatformOsFamily();
        }
        return platformOsFamily;
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
        NutsStoreLocationsMap m = new NutsStoreLocationsMap(config.getStoreLocations());
        m.set(folderType, location);
        config.setStoreLocations(m.toMapOrNull());
        fireConfigurationChanged();
    }

    @Override
    public void setHomeLocation(NutsOsFamily layout, NutsStoreLocation folder, String location) {
        if (folder == null) {
            throw new NutsIllegalArgumentException(ws, "Invalid store root folder null");
        }
        NutsHomeLocationsMap m = new NutsHomeLocationsMap(config.getHomeLocations());
        m.set(layout, folder, location);
        config.setHomeLocations(m.toMapOrNull());
        fireConfigurationChanged();
    }

    @Override
    public void setStoreLocationStrategy(NutsStoreLocationStrategy strategy) {
        if (strategy == null) {
            strategy = NutsStoreLocationStrategy.values()[0];
        }
        config.setStoreLocationStrategy(strategy);
        fireConfigurationChanged();
    }

    @Override
    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        NutsStoreLocationStrategy s = config.getStoreLocationStrategy();
        return s == null ? NutsStoreLocationStrategy.values()[0] : s;
    }

    @Override
    public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy() {
        NutsStoreLocationStrategy s = config.getRepositoryStoreLocationStrategy();
        return s == null ? NutsStoreLocationStrategy.values()[0] : s;
    }

    @Override
    public void setStoreLocationLayout(NutsOsFamily layout) {
        config.setStoreLocationLayout(layout);
        fireConfigurationChanged();
    }

    @Override
    public NutsOsFamily getStoreLocationLayout() {
        NutsOsFamily s = config.getStoreLocationLayout();
        return s == null ? NutsOsFamily.values()[0] : s;
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
    public boolean load(NutsSession session) {
        Path file = getConfigFile();
        NutsWorkspaceConfig _config = Files.isRegularFile(file) ? parseConfigForAnyVersion(file) : null;
        if (_config != null) {
            setRunningContext(new DefaultNutsBootContext(ws)
                    .merge(getStoredConfig())
                    .mergeRuntime(options())
                    .build(getWorkspaceLocation())
            );
            setConfig(_config, session, false);
            configurationChanged = false;
            return true;
        } else {
            return false;
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
            String version = (String) a_config0.get("createApiVersion");
            if (version == null) {
                version = "0.5.6";
            }
            int buildNumber = CoreNutsUtils.getNutsApiVersionOrdinalNumber(version);
            if (buildNumber < 506) {
                //deprecated, will ignore
                return ws.json().parse(new InputStreamReader(new ByteArrayInputStream(bytes)), NutsWorkspaceConfig502.class).toWorkspaceConfig();
            }
            return ws.json().parse(new InputStreamReader(new ByteArrayInputStream(bytes)), NutsWorkspaceConfig.class);
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "Erroneous config file. Unable to load file {0} : {1}", new Object[]{file, ex.toString()});
            if (!ws.config().isReadOnly()) {
                Path newfile = file.getParent().resolve("nuts-workspace-" + new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date()) + ".json");
                LOG.log(Level.SEVERE, "Erroneous config file will replace by fresh one. Old config is copied to {0}", newfile.toString());
                try {
                    Files.move(file, newfile);
                } catch (IOException e) {
                    throw new UncheckedIOException("Unable to load and re-create config file " + file.toString() + " : " + e.toString(), new IOException(ex));
                }
            } else {
                throw new UncheckedIOException("Unable to load config file " + file.toString(), new IOException(ex));
            }
            return null;
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
        if (!Objects.equals(value, config.getBootApiVersion())) {
            config.setBootApiVersion(value);
            fireConfigurationChanged();
        }
    }

    @Override
    public void setBootRuntime(String value) {
        if (!Objects.equals(value, config.getBootRuntime())) {
            config.setBootRuntime(value);
            fireConfigurationChanged();
        }
    }

    @Override
    public void setBootRuntimeDependencies(String value) {
        if (!Objects.equals(value, config.getBootRuntimeDependencies())) {
            config.setBootRuntimeDependencies(value);
            fireConfigurationChanged();
        }
    }

    @Override
    public void setBootRepositories(String value) {
        if (!Objects.equals(value, config.getBootRepositories())) {
            config.setBootRepositories(value);
            fireConfigurationChanged();
        }
    }

    @Override
    public NutsWorkspaceListManager createWorkspaceListManager(String name) {
        return new DefaultNutsWorkspaceListManager(ws, name);
    }

    @Override
    public boolean isGlobal() {
        return config.isGlobal();
    }

    @Override
    public NutsId getApiId() {
        return ws.id().parse(getBootConfig().getApiId());
    }

    @Override
    public NutsId getRuntimeId() {
        return ws.id().parse(getBootConfig().getRuntimeId());
    }

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
//        conf = CoreNutsUtils.loadNutsRepositoryConfig(new File(folder, NutsConstants.NUTS_REPOSITORY_CONFIG_FILE_NAME), ws);
        options = options.copy();
        try {
            NutsRepositoryConfig conf = options.getConfig();
            if (conf == null) {
                options.setLocation(CoreIOUtils.resolveRepositoryPath(options, rootFolder, ws));
                conf = CoreIOUtils.loadNutsRepositoryConfig(ws.io().path(options.getLocation(), NutsConstants.Files.REPOSITORY_CONFIG_FILE_NAME), ws);
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
        return config;
    }

}
