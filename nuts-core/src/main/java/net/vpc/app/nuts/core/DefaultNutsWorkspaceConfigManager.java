/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.*;
import net.vpc.common.io.URLUtils;
import net.vpc.common.mvn.PomId;
import net.vpc.common.mvn.PomIdResolver;
import net.vpc.common.strings.StringUtils;

import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;

/**
 * @author vpc
 */
class DefaultNutsWorkspaceConfigManager implements NutsWorkspaceConfigManagerExt {

    private final DefaultNutsWorkspace ws;

//    private NutsId workspaceBootId;
//    private NutsId workspaceRuntimeId;

    private NutsBootContext runningBootConfig;
    private NutsBootContext wsBootConfig;
//    private NutsId actualBootId;
//    private NutsId actualRuntimeId;

    private ClassLoader bootClassLoader;
    private URL[] bootClassWorldURLs;
    private NutsWorkspaceConfig config = new NutsWorkspaceConfig();
    private String cwd = System.getProperty("user.dir");
    private List<NutsWorkspaceCommandFactory> commandFactories = new ArrayList<>();
    private ConfigNutsWorkspaceCommandFactory defaultCommandFactory = new ConfigNutsWorkspaceCommandFactory(this);
    private boolean configurationChanged = false;
    private NutsWorkspaceOptions options;
    private NutsId platformOs;
    private NutsId platformArch;
    private NutsId platformOsdist;
    private String platformOsLibPath;
    private long startCreateTime;
    private long endCreateTime;
    private Map<String, List<NutsSdkLocation>> configSdks = new HashMap<>();
    private Map<String, NutsUserConfig> configUsers = new HashMap<>();
    private Map<String, NutsRepositoryLocation> configReposByName = new HashMap<>();

    protected DefaultNutsWorkspaceConfigManager(final DefaultNutsWorkspace outer) {
        this.ws = outer;
    }

    public ClassLoader getBootClassLoader() {
        return bootClassLoader;
    }

    public void setConfig(NutsWorkspaceConfig config) {
        this.config = config;
        if (StringUtils.isEmpty(config.getUuid())) {
            config.setUuid(UUID.randomUUID().toString());
        }
        configSdks.clear();
        for (NutsSdkLocation sdk : config.getSdk()) {
            List<NutsSdkLocation> list = configSdks.get(sdk.getType());
            if (list == null) {
                list = new ArrayList<>();
                configSdks.put(sdk.getType(), list);
            }
            list.add(sdk);
        }
        configUsers.clear();
        for (NutsUserConfig s : config.getUsers()) {
            configUsers.put(s.getUser(), s);
        }
        configReposByName.clear();
        for (NutsRepositoryLocation repository : config.getRepositories()) {
            configReposByName.put(repository.getName(),repository);
        }
        fireConfigurationChanged();
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
        cc.setWorkspace(getWorkspaceLocation());
        cc.setApiVersion(config.getBootApiVersion());
        cc.setRuntimeId(config.getBootRuntime());
        cc.setRuntimeDependencies(config.getBootRuntimeDependencies());
        cc.setRepositories(config.getBootRepositories());
        cc.setJavaCommand(config.getBootJavaCommand());
        cc.setJavaOptions(config.getBootJavaOptions());
        cc.setProgramsStoreLocation(config.getProgramsStoreLocation());
        cc.setConfigStoreLocation(config.getConfigStoreLocation());
        cc.setLogsStoreLocation(config.getLogsStoreLocation());
        cc.setTempStoreLocation(config.getTempStoreLocation());
        cc.setCacheStoreLocation(config.getCacheStoreLocation());
        cc.setVarStoreLocation(config.getVarStoreLocation());
        cc.setStoreLocationStrategy(config.getStoreLocationStrategy());
        return new DefaultNutsBootContext(cc);
    }

    @Override
    public NutsBootConfig getBootConfig() {
        return new NutsBootConfig()
                .setApiVersion(config.getBootApiVersion())
                .setRuntimeId(config.getBootRuntime())
                .setRepositories(config.getBootRepositories())
                .setJavaCommand(config.getBootJavaCommand())
                .setJavaOptions(config.getBootJavaOptions())
                ;
    }

    @Override
    public void addImports(String... importExpressions) {
        Set<String> imports = new LinkedHashSet<>(getConfig().getImports());
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

    @Override
    public void removeImports(String... importExpressions) {
        Set<String> imports = new LinkedHashSet<>(getConfig().getImports());
        if (importExpressions != null) {
            for (String importExpression : importExpressions) {
                if (importExpression != null) {
                    for (String s : importExpression.split("[,;: ]")) {
                        imports.remove(s.trim());
                    }
                }
            }
        }
        String[] arr = imports.toArray(new String[0]);
//        Arrays.sort(arr);
        setImports(arr);
    }

    @Override
    public void setImports(String[] imports) {
        Set<String> simports = new LinkedHashSet<>();
        if (imports != null) {
            for (String s : imports) {
                if (s == null) {
                    s = "";
                }
                s = s.trim();
                if (!StringUtils.isEmpty(s)) {
                    simports.add(s);
                }
            }
        }
        String[] arr = simports.toArray(new String[0]);
//        Arrays.sort(arr);
        getConfig().setImports(arr);
        fireConfigurationChanged();
    }

    @Override
    public String[] getImports() {
        String[] envImports = getConfig().getImports().toArray(new String[0]);
        HashSet<String> all = new HashSet<>(Arrays.asList(envImports));
        //        public static final String ENV_KEY_IMPORTS = "imports";
        //workaround
        String extraImports = getEnv("imports", null);
        if (extraImports != null) {
            all.addAll(Arrays.asList(extraImports.split("[,;: ]")));
        }
        return all.toArray(new String[0]);
    }

    @Override
    public Properties getEnv() {
        Properties p = new Properties();
        p.putAll(getConfig().getEnv());
        return p;
    }

    @Override
    public NutsId[] getExtensions() {
        return getConfig().getExtensions().toArray(new NutsId[0]);
    }

    public NutsRepositoryLocation getRepository(String repositoryName) {
        return configReposByName.get(repositoryName);
    }

    @Override
    public boolean isRepositoryEnabled(String repoName) {
        NutsRepositoryLocation r = getRepository(repoName);
        return r != null && r.isEnabled();
    }

    @Override
    public void setRepositoryEnabled(String repoName, boolean enabled) {
        NutsRepositoryLocation e = getRepository(repoName);
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
    public NutsWorkspaceConfig getConfig() {
        return config;
    }

    @Override
    public boolean addSdk(String name, NutsSdkLocation location) {
        if (location != null) {
            if (StringUtils.isEmpty(location.getName())) {
                throw new IllegalArgumentException("Sdk Name should not be null");
            }
            if (StringUtils.isEmpty(location.getVersion())) {
                throw new IllegalArgumentException("Sdk Version should not be null");
            }
            if (StringUtils.isEmpty(location.getPath())) {
                throw new IllegalArgumentException("Sdk Path should not be null");
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
            updateConfigSdk();
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
    public NutsSdkLocation findSdkByPath(String name, String path) {
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
                for (Iterator<NutsSdkLocation> iterator = list.iterator(); iterator.hasNext(); ) {
                    NutsSdkLocation location2 = iterator.next();
                    if (location2.equals(location)) {
                        iterator.remove();
                        updateConfigSdk();
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
        if (!StringUtils.isEmpty(other.getRuntimeId())) {
            List<NutsDefinition> nutsDefinitions = ws.createQuery().addId(other.getRuntimeId()).includeDependencies().fetch();
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
        NutsVersionFilter javaVersionFilter = CoreVersionUtils.createNutsVersionFilter(requestedVersion);
        NutsSdkLocation best = null;
        for (NutsSdkLocation jdk : getSdks("java")) {
            String currVersion = jdk.getVersion();
            if (javaVersionFilter == null || javaVersionFilter.accept(DefaultNutsVersion.valueOf(currVersion))) {
                if (best == null || CoreVersionUtils.compareVersions(best.getVersion(), currVersion) < 0) {
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

    public boolean addExtension(NutsId extensionId) {
        if (extensionId == null) {
            throw new NutsIllegalArgumentException("Invalid Extension");
        }
        if (!containsExtension(extensionId)) {
            getConfig().getExtensions().add(extensionId);
            fireConfigurationChanged();
            return true;
        }
        return false;
    }

    public boolean removeExtension(NutsId extensionId) {
        if (extensionId == null) {
            throw new NutsIllegalArgumentException("Invalid Extension");
        }
        for (NutsId extension : getExtensions()) {
            if (extension.equalsSimpleName(extensionId)) {
                getConfig().getExtensions().remove(extension);
                fireConfigurationChanged();
                return true;
            }
        }
        return false;
    }

    public boolean updateExtension(NutsId extensionId) {
        if (extensionId == null) {
            throw new NutsIllegalArgumentException("Invalid Extension");
        }
        NutsId[] extensions = getExtensions();
        for (int i = 0; i < extensions.length; i++) {
            NutsId extension = extensions[i];
            if (extension.equalsSimpleName(extensionId)) {
                config.getExtensions().set(i, extensionId);
                fireConfigurationChanged();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isValidWorkspaceFolder() {
        File file = CoreIOUtils.createFile(runningBootConfig.getWorkspace(), NutsConstants.NUTS_WORKSPACE_CONFIG_FILE_NAME);
        if (file.isFile() && file.exists()) {
            return true;
        }
        return false;
    }

    @Override
    public String getWorkspaceLocation() {
        return runningBootConfig.getWorkspace();
    }

    @Override
    public boolean isReadOnly() {
        return options.isReadOnly();
    }

    @Override
    public void save() {
        CoreNutsUtils.checkReadOnly(ws);
        ws.getSecurityManager().checkAllowed(NutsConstants.RIGHT_SAVE_WORKSPACE, "save");
        File file = CoreIOUtils.createFile(runningBootConfig.getWorkspace(), NutsConstants.NUTS_WORKSPACE_CONFIG_FILE_NAME);
        ws.getIOManager().writeJson(config, file, true);
        NutsRepositoryFilter repositoryFilter = null;
        for (NutsRepository repo : ws.getEnabledRepositories(repositoryFilter)) {
            repo.save();
        }
        configurationChanged = false;
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
            if (URLUtils.isFileURL(bootClassWorldURL)) {
                File f = URLUtils.toFile(bootClassWorldURL);
                sb.append(f.getPath());
            } else {
                sb.append(bootClassWorldURL.toString().replace(":", "\\:"));
            }
        }
        return sb.toString();
    }

    public URL[] getBootClassWorldURLs() {
        return bootClassWorldURLs == null ? null : Arrays.copyOf(bootClassWorldURLs, bootClassWorldURLs.length);
    }

    @Override
    public String getCwd() {
        return cwd;
    }

    @Override
    public void setCwd(String cwd) {
        if (cwd == null) {
            throw new NutsIllegalArgumentException("Invalid cwd");
        }
        if (!new File(cwd).isDirectory()) {
            throw new NutsIllegalArgumentException("Invalid cwd " + cwd);
        }
        if (!new File(cwd).isAbsolute()) {
            throw new NutsIllegalArgumentException("Invalid cwd " + cwd);
        }
        this.cwd = cwd;
    }

    @Override
    public String resolveNutsJarFile() {
        try {
            NutsId baseId = ws.getParseManager().parseRequiredId(NutsConstants.NUTS_ID_BOOT_API);
            String urlPath = "/META-INF/maven/" + baseId.getGroup() + "/" + baseId.getName() + "/pom.properties";
            URL resource = Nuts.class.getResource(urlPath);
            if (resource != null) {
                URL runtimeURL = CorePlatformUtils.resolveURLFromResource(Nuts.class, urlPath);
                return CorePlatformUtils.resolveLocalFileFromURL(runtimeURL).getPath();
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        // This will happen when running app from  nuts dev project so that classes folder is considered as
        // binary class path instead of a single jar file.
        // In that case we will gather nuts from maven .m2 repository
        PomId m = PomIdResolver.resolvePomId(Nuts.class, null);
        if (m != null) {
            File f = new File(System.getProperty("user.home") + "/.m2/repository/" + m.getGroupId().replace('.', '/') + "/" + m.getArtifactId() + "/" + m.getVersion() + "/"
                    + ws.getConfigManager().getDefaultIdFilename(
                    ws.createIdBuilder().setGroup(m.getGroupId()).setName(m.getArtifactId()).setVersion(m.getVersion())
                            .setFaceComponent()
                            .setPackaging("jar")
                            .build()
            ));
            if (f.exists()) {
                return f.getPath();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "NutsWorkspaceConfig{"
                + "workspaceBootId=" + getBootContext().getApiId().toString()
                + ", workspaceRuntimeId=" + getBootContext().getRuntimeId().toString()
                + ", workspace='" + runningBootConfig.getWorkspace() + '\''
                + ", cwd=" + cwd
                + '}';
    }

    public NutsUserConfig[] getUsers() {
        return configUsers.values().toArray(new NutsUserConfig[0]);
    }

    public NutsUserConfig getUser(String userId) {
        NutsUserConfig config = getSecurity(userId);
        if (config == null) {
            if (NutsConstants.USER_ADMIN.equals(userId) || NutsConstants.USER_ANONYMOUS.equals(userId)) {
                config = new NutsUserConfig(userId, null, null, null, null);
                setUser(config);
            }
        }
        return config;
    }

    @Override
    public void setUser(NutsUserConfig config) {
        if (config != null) {
            configUsers.put(config.getUser(), config);
            updateConfigUsers();
            fireConfigurationChanged();
        }
    }

    @Override
    public boolean isSecure() {
        return getConfig().isSecure();
    }

    @Override
    public void setSecure(boolean secure) {
        if (secure != getConfig().isSecure()) {
            getConfig().setSecure(secure);
            fireConfigurationChanged();
        }
    }

    public void addRepository(NutsRepositoryLocation repository) {
        if (repository == null) {
            throw new NutsIllegalArgumentException("Invalid Repository");
        }
        if (StringUtils.isEmpty(repository.getName())) {
            throw new NutsIllegalArgumentException("Invalid Repository Name");
        }
        if (StringUtils.isEmpty(repository.getType())) {
            repository.setType(NutsConstants.REPOSITORY_TYPE_NUTS);
        }
        if (getRepository(repository.getName()) != null) {
            throw new NutsIllegalArgumentException("Duplicate Repository Id " + repository.getName());
        }
        configReposByName.put(repository.getName(),repository);
        updateConfigRepos();
        fireConfigurationChanged();
    }


    public void removeRepository(String repositoryName) {
        if (repositoryName == null) {
            throw new NutsIllegalArgumentException("Invalid Null Repository");
        }
        NutsRepositoryLocation old = getRepository(repositoryName);
        if (old != null) {
            configReposByName.remove(old.getName());
            updateConfigRepos();
            fireConfigurationChanged();
        }
    }

    public NutsWorkspaceCommandFactoryConfig[] getCommandFactories() {
        return getConfig().getCommandFactories().toArray(new NutsWorkspaceCommandFactoryConfig[0]);
    }

    public NutsRepositoryLocation[] getRepositories() {
        return getConfig().getRepositories().toArray(new NutsRepositoryLocation[0]);
    }

    public void setRepositories(NutsRepositoryLocation[] repositories) {
        for (NutsRepositoryLocation repositoryLocation : getRepositories()) {
            removeRepository(repositoryLocation.getName());
        }
        for (NutsRepositoryLocation repository : repositories) {
            addRepository(repository);
        }
    }

    public boolean containsExtension(NutsId extensionId) {
        if (extensionId == null) {
            throw new NutsIllegalArgumentException("Invalid Extension");
        }
        for (NutsId extension : getExtensions()) {
            if (extension.equalsSimpleName(extension)) {
                return true;
            }
        }
        return false;
    }


    public void setEnv(String property, String value) {
        Properties env = getConfig().getEnv();
        if (StringUtils.isEmpty(value)) {
            if (env.contains(property)) {
                env.remove(property);
                fireConfigurationChanged();
            }
        } else {
            String old = env.getProperty(property);
            if (!value.equals(old)) {
                env.setProperty(property, value);
                fireConfigurationChanged();
            }
        }
    }


    public String getEnv(String property, String defaultValue) {
        String o = getConfig().getEnv().getProperty(property);
        if (StringUtils.isEmpty(o)) {
            return defaultValue;
        }
        return o;
    }

    public void removeUser(String userId) {
        NutsUserConfig old = getSecurity(userId);
        if (old != null) {
            configUsers.remove(userId);
            updateConfigUsers();
            fireConfigurationChanged();
        }
    }

    private void fireConfigurationChanged() {
        setConfigurationChanged(true);
    }

    public void setUsers(NutsUserConfig[] users) {
        for (NutsUserConfig u : getUsers()) {
            removeUser(u.getUser());
        }
        for (NutsUserConfig conf : users) {
            setUser(conf);
        }
    }

    @Override
    public void setLogLevel(Level levek) {
        CoreLogUtils.setLevel(Level.FINEST);
    }

    @Override
    public NutsSdkLocation[] searchJdkLocations(PrintStream out) {
        return JavaHelper.searchJdkLocations(ws, out);
    }

    @Override
    public NutsSdkLocation[] searchJdkLocations(String path, PrintStream out) {
        return JavaHelper.searchJdkLocations(ws, path, out);
    }

    @Override
    public NutsSdkLocation resolveJdkLocation(String path) {
        return JavaHelper.resolveJdkLocation(path, ws);
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
        String passphrase = getEnv(NutsConstants.ENV_KEY_PASSPHRASE, CoreNutsUtils.DEFAULT_PASSPHRASE);
        return CoreSecurityUtils.httpDecrypt(input, passphrase);
    }

    @Override
    public byte[] encryptString(byte[] input) {
        if (input == null || input.length == 0) {
            return new byte[0];
        }
        String passphrase = getEnv(NutsConstants.ENV_KEY_PASSPHRASE, CoreNutsUtils.DEFAULT_PASSPHRASE);
        return CoreSecurityUtils.httpEncrypt(input, passphrase);
    }

    @Override
    public NutsWorkspaceCommand findCommand(String name) {
        NutsWorkspaceCommandConfig c = defaultCommandFactory.findCommand(name, ws);
        if (c == null) {
            for (NutsWorkspaceCommandFactory commandFactory : commandFactories) {
                c = commandFactory.findCommand(name, ws);
                if (c != null && c.getId() != null) {
                    break;
                }
            }
        }
        if (c == null) {
            return null;
        }
        return toDefaultNutsWorkspaceCommand(c);
    }

    private DefaultNutsWorkspaceCommand toDefaultNutsWorkspaceCommand(NutsWorkspaceCommandConfig c) {
        return new DefaultNutsWorkspaceCommand()
                .setCommand(c.getCommand())
                .setFactoryId(c.getFactoryId())
                .setId(c.getId())
                .setName(c.getName());
    }

    @Override
    public boolean installCommand(NutsWorkspaceCommandConfig command, NutsConfirmAction confirmAction) {
        if (command == null
                || StringUtils.isEmpty(command.getName())
                || command.getName().contains(" ") || command.getName().contains("/") || command.getName().contains("\\")
                || command.getId() == null
                || command.getId().getName().isEmpty()
                || command.getId().getGroup().isEmpty()
                || command.getCommand() == null
        ) {
            throw new NutsIllegalArgumentException("Invalid command " + (command == null ? "<NULL>" : command.getName()));
        }
        if (defaultCommandFactory.findCommand(command.getName(), ws) != null) {
            if (confirmAction == null) {
                confirmAction = NutsConfirmAction.ERROR;
            }
            switch (confirmAction) {
                case ERROR:
                    throw new NutsIllegalArgumentException("Command already exists " + command.getName());
                case IGNORE:
                    return false;
            }
        }
        defaultCommandFactory.installCommand(command);
        return true;
    }

    @Override
    public boolean uninstallCommand(String name, NutsConfirmAction confirmAction) {
        if (StringUtils.isEmpty(name)) {
            throw new NutsIllegalArgumentException("Invalid command " + (name == null ? "<NULL>" : name));
        }
        NutsWorkspaceCommandConfig command = defaultCommandFactory.findCommand(name, ws);
        if (command == null) {
            switch (confirmAction) {
                case ERROR:
                    throw new NutsIllegalArgumentException("Command does not exists " + name);
                case IGNORE:
                    return false;
            }
        }
        defaultCommandFactory.uninstallCommand(name);
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
    public void installCommandFactory(NutsWorkspaceCommandFactoryConfig commandFactoryConfig) {
        if (commandFactoryConfig == null || commandFactoryConfig.getFactoryId() == null || commandFactoryConfig.getFactoryId().isEmpty() || !commandFactoryConfig.getFactoryId().trim().equals(commandFactoryConfig.getFactoryId())) {
            throw new IllegalArgumentException("Invalid WorkspaceCommandFactory " + commandFactoryConfig);
        }
        for (NutsWorkspaceCommandFactory factory : commandFactories) {
            if (commandFactoryConfig.getFactoryId().equals(factory.getFactoryId())) {
                throw new IllegalArgumentException();
            }
        }
        NutsWorkspaceCommandFactory f = null;
        if (StringUtils.isEmpty(commandFactoryConfig.getFactoryType()) || "command".equals(commandFactoryConfig.getFactoryType().trim())) {
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
    public boolean uninstallCommandFactory(String factoryId, NutsConfirmAction confirmAction) {
        if (factoryId == null || factoryId.isEmpty()) {
            throw new IllegalArgumentException("Invalid WorkspaceCommandFactory " + factoryId);
        }
        NutsWorkspaceCommandFactory removeMe = null;
        NutsWorkspaceCommandFactoryConfig removeMeConfig = null;
        for (Iterator<NutsWorkspaceCommandFactory> iterator = commandFactories.iterator(); iterator.hasNext(); ) {
            NutsWorkspaceCommandFactory factory = iterator.next();
            if (factoryId.equals(factory.getFactoryId())) {
                removeMe = factory;
                iterator.remove();
                fireConfigurationChanged();
                break;
            }
        }
        List<NutsWorkspaceCommandFactoryConfig> commandFactories = config.getCommandFactories();
        if (commandFactories != null) {
            for (Iterator<NutsWorkspaceCommandFactoryConfig> iterator = commandFactories.iterator(); iterator.hasNext(); ) {
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
            switch (confirmAction) {
                case ERROR:
                    throw new NutsIllegalArgumentException("Command Factory does not exists " + factoryId);
                case IGNORE:
                    return false;
            }
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
    public String getHome(NutsStoreFolder folderType) {
        return Nuts.resolveHomeFolder(folderType, runningBootConfig.getStoreLocationLayout());
    }

    @Override
    public String getStoreLocation(NutsStoreFolder folderType) {
        if (folderType == null) {
            folderType = NutsStoreFolder.PROGRAMS;
        }
        return runningBootConfig.getStoreLocation(folderType);
    }

    @Override
    public String getStoreLocation(String id, NutsStoreFolder folderType) {
        return getStoreLocation(ws.getParseManager().parseId(id), folderType);
    }

    @Override
    public String getStoreLocation(NutsId id, NutsStoreFolder folderType) {
        if (StringUtils.isEmpty(id.getGroup())) {
            throw new NutsElementNotFoundException("Missing group for " + id);
        }
        String storeLocation = getStoreLocation(folderType);
        if (storeLocation == null) {
            return null;
        }
        return CoreNutsUtils.resolveNutsDefaultPath(id, new File(storeLocation)).getPath();
    }

    @Override
    public NutsId getPlatformOs() {
        if (platformOs == null) {
            platformOs = ws.getParseManager().parseId(CorePlatformUtils.getPlatformOs());
        }
        return platformOs;
    }

    @Override
    public NutsId getPlatformOsDist() {
        if (platformOsdist == null) {
            platformOsdist = ws.getParseManager().parseId(CorePlatformUtils.getPlatformOsDist());
        }
        return platformOsdist;
    }

    @Override
    public String getPlatformOsLibPath() {
        if (platformOsLibPath == null) {
            platformOsLibPath = CorePlatformUtils.getPlatformOsLib();
        }
        return platformOsLibPath;
    }

    @Override
    public NutsId getPlatformArch() {
        if (platformArch == null) {
            platformArch = ws.getParseManager().parseId(CorePlatformUtils.getPlatformArch());
        }
        return platformArch;
    }

    @Override
    public long getCreationStartTimeMillis() {
        return startCreateTime;
    }

    public void setStartCreateTimeMillis(long startCreateTime) {
        this.startCreateTime = startCreateTime;
    }

    @Override
    public long getCreationFinishTimeMillis() {
        return endCreateTime;
    }

    public void setEndCreateTimeMillis(long endCreateTime) {
        this.endCreateTime = endCreateTime;
    }

    @Override
    public long getCreationTimeMillis() {
        return endCreateTime - startCreateTime;
    }

    @Override
    public NutsAuthenticationAgent createAuthenticationAgent(String authenticationAgent) {
        NutsAuthenticationAgent supported = ws.getExtensionManager().createSupported(NutsAuthenticationAgent.class, authenticationAgent);
        if (supported == null) {
            throw new NutsExtensionMissingException(NutsAuthenticationAgent.class, "AuthenticationAgent");
        }
        return supported;
    }

    @Override
    public void setStoreLocation(NutsStoreFolder folderType, String location) {
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
        if (!StringUtils.isEmpty(ext)) {
            if (!ext.startsWith(".")) {
                ext = "." + ext;
            }
        }
        if (!ext.equals(".nuts") && !ext.equals(".pom")) {
            String c = id.getClassifier();
            if (!StringUtils.isEmpty(c)) {
                classifier = "-" + c;
            }
        }
        return id.getName() + "-" + id.getVersion().getValue() + classifier + ext;
    }


    @Override
    public String getDefaultIdExtension(NutsId id) {
        Map<String, String> q = id.getQueryMap();
        String f = StringUtils.trim(q.get(NutsConstants.QUERY_FACE));
        switch (f) {
            case NutsConstants.FACE_DESCRIPTOR: {
                return "nuts";
            }
            case NutsConstants.FACE_DESC_HASH: {
                return "nuts.sha1";
            }
            case NutsConstants.FACE_CATALOG: {
                return "catalog";
            }
            case NutsConstants.FACE_COMPONENT_HASH: {
                return getDefaultIdExtension(id.setFaceComponent()) + ".sha1";
            }
            case NutsConstants.FACE_COMPONENT: {
                String packaging = q.get(NutsConstants.QUERY_PACKAGING);
                if (StringUtils.isEmpty(packaging)) {
                    throw new NutsIllegalArgumentException("Unsupported empty Packaging");
                }
                if (!StringUtils.isEmpty(packaging)) {
                    switch (packaging) {
                        case "bundle":
                        case "nuts-extension":
                        case "maven-archetype":
                            return "jar";
                    }
                    return packaging;
                }
                return packaging;
            }
            default: {
                throw new IllegalArgumentException("Unsupported fact " + f);
            }
        }
    }

    @Override
    public NutsId createComponentFaceId(NutsId id, NutsDescriptor desc) {
        Map<String, String> q = id.getQueryMap();
        q.put(NutsConstants.QUERY_PACKAGING, StringUtils.trim(desc.getPackaging()));
//        q.put(NutsConstants.QUERY_EXT,StringUtils.trim(desc.getExt()));
        q.put(NutsConstants.QUERY_FACE, NutsConstants.FACE_COMPONENT);
        return id.setQuery(q);
    }

    public boolean isConfigurationChanged() {
        return configurationChanged;
    }

    public DefaultNutsWorkspaceConfigManager setConfigurationChanged(boolean configurationChanged) {
        this.configurationChanged = configurationChanged;
        return this;
    }

    @Override
    public File getConfigFile() {
        return CoreIOUtils.createFile(getWorkspaceLocation(), NutsConstants.NUTS_WORKSPACE_CONFIG_FILE_NAME);
    }

    public boolean load() {
        File file = getConfigFile();
        NutsWorkspaceConfig config = file.isFile() ? ws.getIOManager().readJson(file, NutsWorkspaceConfig.class) : null;
        if (config != null) {
            this.config = config;
            if (StringUtils.isEmpty(config.getUuid())) {
                config.setUuid(UUID.randomUUID().toString());
                fireConfigurationChanged();
            } else {
                configurationChanged = false;
            }
            return true;
        } else {
            return false;
        }
    }

    private void updateConfigSdk() {
        List<NutsSdkLocation> all = new ArrayList<>();
        for (List<NutsSdkLocation> value : configSdks.values()) {
            all.addAll(value);
        }
        config.setSdk(all.toArray(new NutsSdkLocation[0]));
    }

    private void updateConfigUsers() {
        List<NutsUserConfig> all = new ArrayList<>(configUsers.values());
        config.setUsers(all.toArray(new NutsUserConfig[0]));
    }

    private void updateConfigRepos() {
        List<NutsRepositoryLocation> all = new ArrayList<>(configReposByName.values());
        config.setRepositories(all.toArray(new NutsRepositoryLocation[0]));
    }

    public Map<String, List<NutsSdkLocation>> getSdk() {
        return configSdks;
    }

    public NutsUserConfig getSecurity(String id) {
        return configUsers.get(id);
    }

}