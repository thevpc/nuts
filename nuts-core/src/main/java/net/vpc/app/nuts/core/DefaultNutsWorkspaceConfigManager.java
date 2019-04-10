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

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.*;

import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.core.util.bundledlibs.mvn.PomId;
import net.vpc.app.nuts.core.util.bundledlibs.mvn.PomIdResolver;

/**
 * @author vpc
 */
public class DefaultNutsWorkspaceConfigManager implements NutsWorkspaceConfigManagerExt {

    public static final Logger log = Logger.getLogger(DefaultNutsWorkspaceConfigManager.class.getName());

    private final DefaultNutsWorkspace ws;
    private NutsBootContext runningBootConfig;
    private NutsBootContext wsBootConfig;
    private ClassLoader bootClassLoader;
    private URL[] bootClassWorldURLs;
    protected NutsWorkspaceConfig config = new NutsWorkspaceConfig();
    private final List<NutsWorkspaceCommandFactory> commandFactories = new ArrayList<>();
    private final ConfigNutsWorkspaceCommandFactory defaultCommandFactory = new ConfigNutsWorkspaceCommandFactory(this);
    private boolean configurationChanged = false;
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
    private final Map<String, NutsRepositoryRef> configReposByName = new LinkedHashMap<>();

    private Map<String, NutsRepository> repositories = new LinkedHashMap<>();
    private NutsIndexStoreClientFactory indexStoreClientFactory;
    private boolean global;

    protected DefaultNutsWorkspaceConfigManager(final DefaultNutsWorkspace outer) {
        this.ws = outer;
        try {
            indexStoreClientFactory = ws.extensions().createSupported(NutsIndexStoreClientFactory.class, ws);
        } catch (Exception ex) {
            //
        }
        if (indexStoreClientFactory == null) {
            indexStoreClientFactory = new DummyNutsIndexStoreClientFactory();
        }
    }

    @Override
    public ClassLoader getBootClassLoader() {
        return bootClassLoader;
    }

    @Override
    public void setConfig(NutsWorkspaceConfig config) {
        setConfig(config, true);
    }

    private void setConfig(NutsWorkspaceConfig config, boolean fire) {
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
        configReposByName.clear();
        if (config.getRepositories() != null) {
            for (NutsRepositoryRef repository : config.getRepositories()) {
                configReposByName.put(repository.getName(), repository);
            }
        }
        if (fire) {
            fireConfigurationChanged();
        }
    }

    @Override
    public NutsBootContext getRunningContext() {
        return runningBootConfig;
    }

    @Override
    public NutsBootContext getBootContext() {
        return wsBootConfig;
    }

    @Override
    public NutsBootContext getConfigContext() {
        NutsBootConfig cc = new NutsBootConfig();
        cc.setWorkspace(getWorkspaceLocation().toString());
        cc.setApiVersion(config.getBootApiVersion());
        cc.setRuntimeId(config.getBootRuntime());
        cc.setRuntimeDependencies(config.getBootRuntimeDependencies());
        cc.setRepositories(config.getBootRepositories());
        cc.setJavaCommand(config.getBootJavaCommand());
        cc.setJavaOptions(config.getBootJavaOptions());
        CoreNutsUtils.wconfigToBconfig(config, cc);
        cc.setStoreLocationStrategy(config.getStoreLocationStrategy());
        cc.setRepositoryStoreLocationStrategy(config.getRepositoryStoreLocationStrategy());
        cc.setStoreLocationLayout(config.getStoreLocationLayout());
        return new DefaultNutsBootContext(cc);
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
    public String[] getImports() {
        HashSet<String> all = new HashSet<>();
        if (config.getImports() != null) {
            all.addAll(config.getImports());
        }
        return all.toArray(new String[0]);
    }

    @Override
    public Properties getEnv() {
        Properties p = new Properties();
        if (config.getEnv() != null) {
            p.putAll(config.getEnv());
        }
        return p;
    }

//    @Override
    public NutsRepositoryRef getRepositoryRef(String repositoryName) {
        return configReposByName.get(repositoryName);
    }

//    @Override
    public void setRepositoryEnabled(String repoName, boolean enabled) {
        NutsRepositoryRef e = getRepositoryRef(repoName);
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
    public boolean addSdk(String name, NutsSdkLocation location) {
        if (location != null) {
            if (CoreStringUtils.isBlank(location.getName())) {
                throw new NutsIllegalArgumentException("Sdk Name should not be null");
            }
            if (CoreStringUtils.isBlank(location.getVersion())) {
                throw new NutsIllegalArgumentException("Sdk Version should not be null");
            }
            if (CoreStringUtils.isBlank(location.getPath())) {
                throw new NutsIllegalArgumentException("Sdk Path should not be null");
            }
            List<NutsSdkLocation> list = getSdk().get(name);
            if (list == null) {
                list = new ArrayList<>();
                configSdks.put(name, list);
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
    public NutsSdkLocation findSdkByName(String name, String locationName) {
        if (locationName != null) {
            List<NutsSdkLocation> list = getSdk().get(name);
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
    public NutsSdkLocation findSdkByPath(String name, Path path) {
        if (path != null) {
            List<NutsSdkLocation> list = getSdk().get(name);
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
    public NutsSdkLocation findSdkByVersion(String name, String version) {
        if (version != null) {
            List<NutsSdkLocation> list = getSdk().get(name);
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
    public NutsSdkLocation removeSdk(String name, NutsSdkLocation location) {
        if (location != null) {
            List<NutsSdkLocation> list = getSdk().get(name);
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
    public NutsSdkLocation findSdk(String name, NutsSdkLocation location) {
        if (location != null) {
            List<NutsSdkLocation> list = getSdk().get(name);
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
            List<NutsDefinition> nutsDefinitions = ws.find().addId(other.getRuntimeId()).mainAndDependencies().getResultDefinitions().list();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < nutsDefinitions.size(); i++) {
                if (i > 0) {
                    sb.append(":");
                }
                sb.append(nutsDefinitions.get(i).getId().toString());
            }
            other.setRuntimeDependencies(sb.toString());
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
        NutsVersionFilter javaVersionFilter = ws.parser().parseVersionFilter(requestedVersion);
        NutsSdkLocation best = null;
        for (NutsSdkLocation jdk : getSdks("java")) {
            String currVersion = jdk.getVersion();
            if (javaVersionFilter.accept(DefaultNutsVersion.valueOf(currVersion))) {
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
        return ws.io().path(runningBootConfig.getWorkspace());
    }

    @Override
    public boolean isReadOnly() {
        return options.isReadOnly();
    }

    @Override
    public boolean save(boolean force) {
        boolean ok = false;
        if (force || (!isReadOnly() && isConfigurationChanged())) {
            CoreNutsUtils.checkReadOnly(ws);
            ws.security().checkAllowed(NutsConstants.Rights.SAVE_WORKSPACE, "save");
            config.setUsers(configUsers.isEmpty() ? null : new ArrayList<>(configUsers.values()));
            config.setRepositories(configReposByName.isEmpty() ? null : new ArrayList<>(configReposByName.values()));
            List<NutsSdkLocation> plainSdks = new ArrayList<>();
            for (List<NutsSdkLocation> value : configSdks.values()) {
                plainSdks.addAll(value);
            }
            config.setSdk(plainSdks);
            Path file = getWorkspaceLocation().resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
            ws.io().writeJson(config, file, true);
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
    public Map<String, String> getRuntimeProperties() {
        Map<String, String> map = new HashMap<>();
        NutsBootContext rc = getRunningContext();
        NutsBootContext bc = getBootContext();
        map.put("nuts.boot.version", rc.getApiId().getVersion().toString());
        map.put("nuts.boot.id", rc.getApiId().toString());
        map.put("nuts.workspace-boot.version", bc.getApiId().getVersion().toString());
        map.put("nuts.workspace-boot.id", bc.getApiId().toString());
        map.put("nuts.workspace-runtime.id", bc.getRuntimeId().toString());
        map.put("nuts.workspace-runtime.version", bc.getRuntimeId().getVersion().toString());
        map.put("nuts.workspace-location", runningBootConfig.getWorkspace());
        return map;
    }

    @Override
    public void onInitializeWorkspace(
            NutsWorkspaceOptions options,
            DefaultNutsBootContext runningBootConfig,
            DefaultNutsBootContext wsBootConfig,
            URL[] bootClassWorldURLs,
            ClassLoader bootClassLoader) {
        this.options = options;
        this.runningBootConfig = runningBootConfig;
        this.wsBootConfig = wsBootConfig;

        this.bootClassLoader = bootClassLoader;
        this.bootClassWorldURLs = bootClassWorldURLs == null ? null : Arrays.copyOf(bootClassWorldURLs, bootClassWorldURLs.length);
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

    @Override
    public Path resolveNutsJarFile() {
        try {
            NutsId baseId = ws.parser().parseRequiredId(NutsConstants.Ids.NUTS_API);
            String urlPath = "/META-INF/maven/" + baseId.getGroup() + "/" + baseId.getName() + "/pom.properties";
            URL resource = Nuts.class.getResource(urlPath);
            if (resource != null) {
                URL runtimeURL = CoreIOUtils.resolveURLFromResource(Nuts.class, urlPath);
                return CoreIOUtils.resolveLocalPathFromURL(runtimeURL);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        // This will happen when running app from  nuts dev project so that classes folder is considered as
        // binary class path instead of a single jar file.
        // In that case we will gather nuts from maven .m2 repository
        PomId m = PomIdResolver.resolvePomId(Nuts.class, null);
        if (m != null) {
            Path f = ws.io().path(System.getProperty("user.home"), ".m2", "repository", m.getGroupId().replace('.', '/'), m.getArtifactId(), m.getVersion(),
                    ws.config().getDefaultIdFilename(
                            ws.createIdBuilder().setGroup(m.getGroupId()).setName(m.getArtifactId()).setVersion(m.getVersion())
                                    .setFaceComponent()
                                    .setPackaging("jar")
                                    .build()
                    ));
            if (Files.exists(f)) {
                return f;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        String s1 = "NULL";
        String s2 = "NULL";
        if (getBootContext() != null) {
            s1 = String.valueOf(getBootContext().getApiId());
            s2 = String.valueOf(getBootContext().getRuntimeId());
        }
        return "NutsWorkspaceConfig{"
                + "workspaceBootId=" + s1
                + ", workspaceRuntimeId=" + s2
                + ", workspace=" + ((runningBootConfig == null) ? "NULL" : ("'" + runningBootConfig.getWorkspace() + '\''))
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
            if (NutsConstants.Names.USER_ADMIN.equals(userId) || NutsConstants.Names.USER_ANONYMOUS.equals(userId)) {
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
    public boolean isSecure() {
        return config.isSecure();
    }

    @Override
    public void setSecure(boolean secure) {
        if (secure != config.isSecure()) {
            config.setSecure(secure);
            fireConfigurationChanged();
        }
    }

//    @Override
    public void addRepositoryRef(NutsRepositoryRef repository) {
        if (repository == null) {
            throw new NutsIllegalArgumentException("Invalid Repository");
        }
        if (!CoreNutsUtils.isValidIdentifier(repository.getName())) {
            throw new NutsIllegalArgumentException("Invalid Repository Name : " + repository.getName());
        }
        if (getRepositoryRef(repository.getName()) != null) {
            throw new NutsIllegalArgumentException("Duplicate Repository Id " + repository.getName());
        }
        configReposByName.put(repository.getName(), repository);
        fireConfigurationChanged();
    }

//    @Override
    public void removeRepositoryRef(String repositoryName) {
        if (repositoryName == null) {
            throw new NutsIllegalArgumentException("Invalid Null Repository");
        }
        NutsRepositoryRef old = getRepositoryRef(repositoryName);
        if (old != null) {
            configReposByName.remove(old.getName());
            fireConfigurationChanged();
        }
    }

    @Override
    public NutsWorkspaceCommandFactoryConfig[] getCommandFactories() {
        if (config.getCommandFactories() != null) {
            return config.getCommandFactories().toArray(new NutsWorkspaceCommandFactoryConfig[0]);
        }
        return new NutsWorkspaceCommandFactoryConfig[0];
    }

    @Override
    public NutsRepositoryRef[] getRepositoryRefs() {
        return configReposByName.values().toArray(new NutsRepositoryRef[0]);
    }

    public void setRepositories(NutsRepositoryRef[] repositories) {
        for (NutsRepositoryRef repositoryLocation : getRepositoryRefs()) {
            removeRepositoryRef(repositoryLocation.getName());
        }
        for (NutsRepositoryRef repository : repositories) {
            addRepositoryRef(repository);
        }
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

    protected void fireConfigurationChanged() {
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
    public NutsSdkLocation[] searchJdkLocations(PrintStream out) {
        return CorePlatformUtils.searchJdkLocations(ws, out);
    }

    @Override
    public NutsSdkLocation[] searchJdkLocations(Path path, PrintStream out) {
        return CorePlatformUtils.searchJdkLocations(ws, path, out);
    }

    @Override
    public NutsSdkLocation resolveJdkLocation(Path path) {
        return CorePlatformUtils.resolveJdkLocation(path, ws);
    }

    @Override
    public NutsWorkspaceOptions getOptions() {
        return options.copy();
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

    public NutsWorkspaceCommand findEmbeddedCommand(String name) {
        return null;
    }

    public NutsWorkspaceCommand findCommand(String name) {
        NutsWorkspaceCommandConfig c = defaultCommandFactory.findCommand(name, ws);
        if (c == null) {
            for (NutsWorkspaceCommandFactory commandFactory : commandFactories) {
                c = commandFactory.findCommand(name, ws);
                if (c != null && c.getOwner() != null) {
                    break;
                }
            }
        }
        if (c == null) {
            return null;
        }
        return toDefaultNutsWorkspaceCommand(c);
    }

    private NutsWorkspaceCommand toDefaultNutsWorkspaceCommand(NutsWorkspaceCommandConfig c) {
        if (c.getCommand() == null || c.getCommand().length == 0) {
            log.log(Level.WARNING, "Invalid Command Definition ''{0}''. Missing Command. Ignored", c.getName());
            return null;
        }
        if (c.getOwner() == null) {
            log.log(Level.WARNING, "Invalid Command Definition ''{0}''. Missing Owner. Ignored", c.getName());
            return null;
        }
        return new DefaultNutsWorkspaceCommand(ws)
                .setCommand(c.getCommand())
                .setFactoryId(c.getFactoryId())
                .setOwner(c.getOwner())
                .setExecutorOptions(c.getExecutorOptions())
                .setName(c.getName());
    }

    @Override
    public boolean installCommand(NutsWorkspaceCommandConfig command, NutsInstallCommandOptions options, NutsSession session) {
        if (command == null
                || CoreStringUtils.isBlank(command.getName())
                || command.getName().contains(" ") || command.getName().contains("/") || command.getName().contains("\\")
                || command.getOwner() == null
                || command.getOwner().getName().isEmpty()
                || command.getOwner().getGroup().isEmpty()
                || command.getCommand() == null) {
            throw new NutsIllegalArgumentException("Invalid command " + (command == null ? "<NULL>" : command.getName()));
        }
        boolean forced = false;
        if (options == null) {
            options = new NutsInstallCommandOptions();
        }
        if (defaultCommandFactory.findCommand(command.getName(), ws) != null) {
            if (options.isForce()) {
                forced = true;
                uninstallCommand(command.getName(), new NutsUninstallOptions().setTrace(options.isTrace()), session);
            } else {
                throw new NutsIllegalArgumentException("Command already exists " + command.getName());
            }
        }
        defaultCommandFactory.installCommand(command);
        if (options.isTrace()) {
            PrintStream out = CoreIOUtils.resolveOut(ws, session);
            out.printf("[[install]] command ==%s==\n", command.getName());
        }
        return forced;
    }

    @Override
    public boolean uninstallCommand(String name, NutsUninstallOptions options, NutsSession session) {
        if (CoreStringUtils.isBlank(name)) {
            throw new NutsIllegalArgumentException("Invalid command " + (name == null ? "<NULL>" : name));
        }
        if (options == null) {
            options = new NutsUninstallOptions();
        }
        NutsWorkspaceCommandConfig command = defaultCommandFactory.findCommand(name, ws);
        if (command == null) {
            throw new NutsIllegalArgumentException("Command does not exists " + name);
        }
        defaultCommandFactory.uninstallCommand(name);
        if (options.isTrace()) {
            PrintStream out = CoreIOUtils.resolveOut(ws, session);
            out.printf("[[uninstall]] command ==%s==\n", name);
        }
        return true;
    }

    @Override
    public List<NutsWorkspaceCommand> findCommands() {
        HashMap<String, NutsWorkspaceCommand> all = new HashMap<>();
        for (NutsWorkspaceCommandConfig command : defaultCommandFactory.findCommands(ws)) {
            all.put(command.getName(), toDefaultNutsWorkspaceCommand(command));
        }
        for (NutsWorkspaceCommandFactory commandFactory : commandFactories) {
            for (NutsWorkspaceCommandConfig command : commandFactory.findCommands(ws)) {
                if (!all.containsKey(command.getName())) {
                    all.put(command.getName(), toDefaultNutsWorkspaceCommand(command));
                }
            }
        }
        return new ArrayList<>(all.values());
    }

    @Override
    public void installCommandFactory(NutsWorkspaceCommandFactoryConfig commandFactoryConfig, NutsSession session) {
        if (commandFactoryConfig == null || commandFactoryConfig.getFactoryId() == null || commandFactoryConfig.getFactoryId().isEmpty() || !commandFactoryConfig.getFactoryId().trim().equals(commandFactoryConfig.getFactoryId())) {
            throw new NutsIllegalArgumentException("Invalid WorkspaceCommandFactory " + commandFactoryConfig);
        }
        for (NutsWorkspaceCommandFactory factory : commandFactories) {
            if (commandFactoryConfig.getFactoryId().equals(factory.getFactoryId())) {
                throw new IllegalArgumentException();
            }
        }
        NutsWorkspaceCommandFactory f = null;
        if (CoreStringUtils.isBlank(commandFactoryConfig.getFactoryType()) || "command".equals(commandFactoryConfig.getFactoryType().trim())) {
            f = new CommandNutsWorkspaceCommandFactory();
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
        List<NutsWorkspaceCommandFactoryConfig> commandFactories = config.getCommandFactories();
        if (commandFactories == null) {
            commandFactories = new ArrayList<>();
            config.setCommandFactories(commandFactories);
        }
        NutsWorkspaceCommandFactoryConfig oldCommandFactory = null;
        for (NutsWorkspaceCommandFactoryConfig commandFactory : commandFactories) {
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
    public boolean uninstallCommandFactory(String factoryId, NutsSession session) {
        if (factoryId == null || factoryId.isEmpty()) {
            throw new NutsIllegalArgumentException("Invalid WorkspaceCommandFactory " + factoryId);
        }
        NutsWorkspaceCommandFactory removeMe = null;
        NutsWorkspaceCommandFactoryConfig removeMeConfig = null;
        for (Iterator<NutsWorkspaceCommandFactory> iterator = commandFactories.iterator(); iterator.hasNext();) {
            NutsWorkspaceCommandFactory factory = iterator.next();
            if (factoryId.equals(factory.getFactoryId())) {
                removeMe = factory;
                iterator.remove();
                fireConfigurationChanged();
                break;
            }
        }
        List<NutsWorkspaceCommandFactoryConfig> _commandFactories = config.getCommandFactories();
        if (_commandFactories != null) {
            for (Iterator<NutsWorkspaceCommandFactoryConfig> iterator = _commandFactories.iterator(); iterator.hasNext();) {
                NutsWorkspaceCommandFactoryConfig commandFactory = iterator.next();
                if (factoryId.equals(commandFactory.getFactoryId())) {
                    removeMeConfig = commandFactory;
                    iterator.remove();
                    fireConfigurationChanged();
                    break;
                }
            }
        }
        if (removeMe == null && removeMeConfig == null) {
            throw new NutsIllegalArgumentException("Command Factory does not exists " + factoryId);
        }
        return true;
    }

    @Override
    public List<NutsWorkspaceCommand> findCommands(NutsId id) {
        HashMap<String, NutsWorkspaceCommand> all = new HashMap<>();
        for (NutsWorkspaceCommandConfig command : defaultCommandFactory.findCommands(id, ws)) {
            all.put(command.getName(), toDefaultNutsWorkspaceCommand(command));
        }
        return new ArrayList<>(all.values());
    }

    @Override
    public Path getHomeLocation(NutsStoreLocation folderType) {
        return ws.io().path(NutsPlatformUtils.resolveHomeFolder(runningBootConfig.getStoreLocationLayout(), folderType, runningBootConfig.getHomeLocations(), runningBootConfig.isGlobal()));
    }

    @Override
    public Path getStoreLocation(NutsStoreLocation folderType) {
        if (folderType == null) {
            folderType = NutsStoreLocation.PROGRAMS;
        }
        return ws.io().path(runningBootConfig.getStoreLocation(folderType));
    }

    @Override
    public Path getStoreLocation(String id, NutsStoreLocation folderType) {
        return getStoreLocation(ws.parser().parseId(id), folderType);
    }

    @Override
    public Path getStoreLocation(NutsId id, NutsStoreLocation folderType) {
        if (CoreStringUtils.isBlank(id.getGroup())) {
            throw new NutsElementNotFoundException("Missing group for " + id);
        }
        Path storeLocation = getStoreLocation(folderType);
        if (storeLocation == null) {
            return null;
        }
        return CoreIOUtils.resolveNutsDefaultPath(id, storeLocation);
    }

    @Override
    public Path getStoreLocation(NutsId id, Path path) {
        if (CoreStringUtils.isBlank(id.getGroup())) {
            throw new NutsElementNotFoundException("Missing group for " + id);
        }
        return CoreIOUtils.resolveNutsDefaultPath(id, path);
    }

    @Override
    public NutsId getPlatformOs() {
        if (platformOs == null) {
            platformOs = ws.parser().parseId(CorePlatformUtils.getPlatformOs());
        }
        return platformOs;
    }

    @Override
    public NutsId getPlatformOsDist() {
        if (platformOsdist == null) {
            platformOsdist = ws.parser().parseId(CorePlatformUtils.getPlatformOsDist());
        }
        return platformOsdist;
    }

    @Override
    public String getPlatformOsHome(NutsStoreLocation location) {
        int ordinal = location.ordinal();
        String s = platformOsPath[ordinal];
        if (s == null) {
            platformOsPath[ordinal] = s = NutsPlatformUtils.getPlatformOsHome(location);
        }
        return s;
    }

    @Override
    public NutsId getPlatformArch() {
        if (platformArch == null) {
            platformArch = ws.parser().parseId(CorePlatformUtils.getPlatformArch());
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
        NutsAuthenticationAgent supported = ws.extensions().createSupported(NutsAuthenticationAgent.class, authenticationAgent);
        if (supported == null) {
            throw new NutsExtensionMissingException(NutsAuthenticationAgent.class, "AuthenticationAgent");
        }
        return supported;
    }

    @Override
    public void setStoreLocation(NutsStoreLocation folderType, String location) {
        if (folderType == null) {
            throw new NutsIllegalArgumentException("Invalid store root folder null");
        }
        switch (folderType) {
            case PROGRAMS: {
                config.setProgramsStoreLocation(location);
                break;
            }
            case CACHE: {
                config.setCacheStoreLocation(location);
                break;
            }
            case CONFIG: {
                config.setConfigStoreLocation(location);
                break;
            }
            case LOGS: {
                config.setLogsStoreLocation(location);
                break;
            }
            case TEMP: {
                config.setTempStoreLocation(location);
                break;
            }
            case VAR: {
                config.setVarStoreLocation(location);
                break;
            }
            case LIB: {
                config.setLibStoreLocation(location);
                break;
            }
            default: {
                throw new NutsIllegalArgumentException("Invalid folder type " + folderType);
            }
        }
        fireConfigurationChanged();
    }

    @Override
    public void setHomeLocation(NutsStoreLocationLayout layout, NutsStoreLocation folderType, String location) {
        if (layout == null) {
            throw new NutsIllegalArgumentException("Invalid layout home null");
        }
        if (folderType == null) {
            throw new NutsIllegalArgumentException("Invalid store folder null");
        }
        switch (layout) {
            case SYSTEM: {
                switch (folderType) {
                    case PROGRAMS: {
                        config.setProgramsSystemHome(location);
                        break;
                    }
                    case CACHE: {
                        config.setCacheSystemHome(location);
                        break;
                    }
                    case CONFIG: {
                        config.setConfigSystemHome(location);
                        break;
                    }
                    case LOGS: {
                        config.setLogsSystemHome(location);
                        break;
                    }
                    case TEMP: {
                        config.setTempSystemHome(location);
                        break;
                    }
                    case VAR: {
                        config.setVarSystemHome(location);
                        break;
                    }
                    case LIB: {
                        config.setLibSystemHome(location);
                        break;
                    }
                    default: {
                        throw new NutsIllegalArgumentException("Invalid folder type " + folderType);
                    }
                }
                break;
            }
            case WINDOWS: {
                switch (folderType) {
                    case PROGRAMS: {
                        config.setProgramsWindowsHome(location);
                        break;
                    }
                    case CACHE: {
                        config.setCacheWindowsHome(location);
                        break;
                    }
                    case CONFIG: {
                        config.setConfigWindowsHome(location);
                        break;
                    }
                    case LOGS: {
                        config.setLogsWindowsHome(location);
                        break;
                    }
                    case TEMP: {
                        config.setTempWindowsHome(location);
                        break;
                    }
                    case VAR: {
                        config.setVarWindowsHome(location);
                        break;
                    }
                    case LIB: {
                        config.setLibWindowsHome(location);
                        break;
                    }
                    default: {
                        throw new NutsIllegalArgumentException("Invalid folder type " + folderType);
                    }
                }
                break;
            }
            case LINUX: {
                switch (folderType) {
                    case PROGRAMS: {
                        config.setProgramsLinuxHome(location);
                        break;
                    }
                    case CACHE: {
                        config.setCacheLinuxHome(location);
                        break;
                    }
                    case CONFIG: {
                        config.setConfigLinuxHome(location);
                        break;
                    }
                    case LOGS: {
                        config.setLogsLinuxHome(location);
                        break;
                    }
                    case TEMP: {
                        config.setTempLinuxHome(location);
                        break;
                    }
                    case VAR: {
                        config.setVarLinuxHome(location);
                        break;
                    }
                    case LIB: {
                        config.setLibLinuxHome(location);
                        break;
                    }
                    default: {
                        throw new NutsIllegalArgumentException("Invalid folder type " + folderType);
                    }
                }
                break;
            }
            default: {
                throw new NutsIllegalArgumentException("Invalid layout " + layout);
            }
        }

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
    public void setStoreLocationLayout(NutsStoreLocationLayout layout) {
        if (layout == null) {
            layout = NutsStoreLocationLayout.values()[0];
        }
        config.setStoreLocationLayout(layout);
        fireConfigurationChanged();
    }

    @Override
    public NutsStoreLocationLayout getStoreLocationLayout() {
        NutsStoreLocationLayout s = config.getStoreLocationLayout();
        return s == null ? NutsStoreLocationLayout.values()[0] : s;
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }

    @Override
    public String getDefaultIdFilename(NutsId id) {
        String classifier = "";
        String ext = getDefaultIdExtension(id);
        if (!ext.equals(".nuts") && !ext.equals(".pom")) {
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
            throw new NutsIllegalArgumentException("Unsupported empty Packaging");
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
                return ".nuts";
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
            case "cache-eff-nuts": {
                return ".cache-eff-nuts";
            }
            case "cache-info": {
                return ".cache-info";
            }
            case NutsConstants.QueryFaces.DESCRIPTOR: {
                return ".nuts";
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
                if (CoreStringUtils.isBlank(f)) {
                    throw new NutsIllegalArgumentException("Missing face in " + id);
                }
                throw new NutsIllegalArgumentException("Unsupported face " + f + " in " + id);
            }
        }
    }

    @Override
    public NutsId createComponentFaceId(NutsId id, NutsDescriptor desc) {
        Map<String, String> q = id.getQueryMap();
        q.put(NutsConstants.QueryKeys.PACKAGING, CoreStringUtils.trim(desc.getPackaging()));
//        q.put(NutsConstants.QUERY_EXT,CoreStringUtils.trim(desc.getExt()));
        q.put(NutsConstants.QueryKeys.FACE, NutsConstants.QueryFaces.COMPONENT);
        return id.setQuery(q);
    }

    @Override
    public boolean isConfigurationChanged() {
        return configurationChanged;
    }

    public DefaultNutsWorkspaceConfigManager setConfigurationChanged(boolean configurationChanged) {
        this.configurationChanged = configurationChanged;
        return this;
    }

    @Override
    public Path getConfigFile() {
        return getWorkspaceLocation().resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME);
    }

    @Override
    public boolean load() {
        Path file = getConfigFile();
        NutsWorkspaceConfig _config = Files.isRegularFile(file) ? ws.io().readJson(file, NutsWorkspaceConfig.class) : null;
        if (_config != null) {
            setConfig(_config, false);
            configurationChanged = false;
            return true;
        } else {
            return false;
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
        return ws.parser().parseId(getBootConfig().getApiId());
    }

    @Override
    public NutsId getRuntimeId() {
        return ws.parser().parseId(getBootConfig().getRuntimeId());
    }

    @Override
    public NutsIndexStoreClientFactory getIndexStoreClientFactory() {
        return indexStoreClientFactory;
    }

    @Override
    public void removeRepository(String repositoryId) {
        ws.security().checkAllowed(NutsConstants.Rights.REMOVE_REPOSITORY, "remove-repository");
        NutsRepository removed = repositories.remove(repositoryId);
        removeRepositoryRef(repositoryId);
        if (removed != null) {
            for (NutsWorkspaceListener nutsWorkspaceListener : ws.getWorkspaceListeners()) {
                nutsWorkspaceListener.onRemoveRepository(ws, removed);
            }
        }
    }

//    @Override
    public Path getRepositoriesRoot() {
        return ws.config().getWorkspaceLocation().resolve(NutsConstants.Folders.REPOSITORIES);
    }

    @Override
    public NutsRepository findRepository(String repositoryName) {
        if (!CoreStringUtils.isBlank(repositoryName)) {
            repositoryName = CoreIOUtils.trimSlashes(repositoryName);
            if (repositoryName.contains("/")) {
                int s = repositoryName.indexOf("/");
                NutsRepository r = repositories.get(repositoryName.substring(0, s));
                if (r != null) {
                    return r.config().findMirror(repositoryName.substring(s + 1));
                }
            } else {
                NutsRepository r = repositories.get(repositoryName);
                if (r != null) {
                    return r;
                }
            }
            for (NutsRepository r : repositories.values()) {
                if (repositoryName.equals(r.config().getLocation(true))) {
                    return r;
                }
            }
        }
        return null;
    }

    @Override
    public NutsRepository getRepository(String repositoryName) {
        NutsRepository r = findRepository(repositoryName);
        if (r != null) {
            return r;
        }
        throw new NutsRepositoryNotFoundException(repositoryName);
    }

    @Override
    public NutsRepository[] getRepositories() {
        return repositories.values().toArray(new NutsRepository[0]);
    }

    @Override
    public boolean isSupportedRepositoryType(String repositoryType) {
        if (CoreStringUtils.isBlank(repositoryType)) {
            repositoryType = NutsConstants.RepoTypes.NUTS;
        }
        return ws.extensions().createAllSupported(NutsRepositoryFactoryComponent.class, new NutsRepositoryLocation().setType(repositoryType)).size() > 0;
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

//    @Override
    public NutsRepository wireRepository(NutsRepository repository) {
        if (repository == null) {
            //mainly if the lcoation is inaccessible!
            return null;
        }
        CoreNutsUtils.validateRepositoryName(repository.config().getName(), repositories.keySet());
        repositories.put(repository.config().getName(), repository);
        for (NutsWorkspaceListener nutsWorkspaceListener : ws.getWorkspaceListeners()) {
            nutsWorkspaceListener.onAddRepository(ws, repository);
        }
        return repository;
    }

//    @Override
    public void removeAllRepositories() {
        repositories.clear();
    }

    @Override
    public NutsRepository addRepository(NutsRepositoryDefinition definition) {
        return addRepository(CoreNutsUtils.defToOptions(definition));
    }

    @Override
    public NutsRepository addRepository(NutsCreateRepositoryOptions options) {
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
                    return null;
                }
                //Dont need to add mirror if repository is already loadable from config!
                final String m2 = options.getName() + "-ref";
                if (!proxy.config().containsMirror(m2)) {
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
                if (!proxy.config().containsMirror(m2)) {
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
            if (!options.isTemporay()) {
                addRepositoryRef(CoreNutsUtils.optionsToRef(options));
            }
            return wireRepository(this.createRepository(options, getRepositoriesRoot(), null));
        }
    }

    @Override
    public Set<String> getAvailableArchetypes() {
        Set<String> set = new HashSet<>();
        set.add("default");
        for (NutsWorkspaceArchetypeComponent extension : ws.extensions().createAllSupported(NutsWorkspaceArchetypeComponent.class, ws)) {
            set.add(extension.getName());
        }
        return set;
    }

    @Override
    public Path resolveRepositoryPath(String repositoryLocation) {
        Path root = this.getRepositoriesRoot();
        NutsWorkspaceConfigManager configManager = this.ws.config();
        return ws.io().path(ws.io().expandPath(repositoryLocation,
                root != null ? root.toString() : configManager.getWorkspaceLocation().resolve(NutsConstants.Folders.REPOSITORIES).toString()));
    }

    private static class DummyNutsIndexStoreClient implements NutsIndexStoreClient {

        @Override
        public List<NutsId> findVersions(NutsId id, NutsRepositorySession session) {
            return null;
        }

        @Override
        public Iterator<NutsId> find(NutsIdFilter filter, NutsRepositorySession session) {
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
        public int getSupportLevel(NutsWorkspace criteria) {
            return 0;
        }

        @Override
        public NutsIndexStoreClient createNutsIndexStoreClient(NutsRepository repository) {
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
                    throw new NutsInvalidRepositoryException(options.getLocation(), "Invalid location " + options.getLocation());
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
            NutsRepositoryFactoryComponent factory_ = ws.extensions().createSupported(NutsRepositoryFactoryComponent.class, conf);
            if (factory_ != null) {
                NutsRepository r = factory_.create(options, ws, parentRepository);
                if (r != null) {
                    return r;
                }
            }
            throw new NutsInvalidRepositoryException(options.getName(), "Invalid type " + conf.getType());
        } catch (RuntimeException ex) {
            if (options.isFailSafe()) {
                return null;
            }
            throw ex;
        }
    }

    public NutsWorkspaceConfig getStoredConfig() {
        return config;
    }
    
}
