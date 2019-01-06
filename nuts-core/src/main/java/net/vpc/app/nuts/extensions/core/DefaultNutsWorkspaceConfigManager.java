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
package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.*;
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

    private NutsBootContext actualBootConfig;
    private NutsBootContext wsBootConfig;
//    private NutsId actualBootId;
//    private NutsId actualRuntimeId;

    private String nutsHome;
    private ClassLoader bootClassLoader;
    private URL[] bootClassWorldURLs;
    private NutsWorkspaceConfig config = new NutsWorkspaceConfig();
    private String workspace;
    private String cwd = System.getProperty("user.dir");
    private List<NutsWorkspaceCommandFactory> commandFactories = new ArrayList<>();
    private ConfigNutsWorkspaceCommandFactory defaultCommandFactory = new ConfigNutsWorkspaceCommandFactory();
    private boolean configurationChanged = false;
    private NutsWorkspaceOptions options;
    private NutsId platformOs;
    private NutsId platformArch;
    private NutsId platformOsdist;
    private String platformOsLibPath;
    private long startCreateTime;
    private long endCreateTime;

    protected DefaultNutsWorkspaceConfigManager(final DefaultNutsWorkspace outer) {
        this.ws = outer;
    }

    public ClassLoader getBootClassLoader() {
        return bootClassLoader;
    }

    public void setConfig(NutsWorkspaceConfig config) {
        this.config = config;
    }

    @Override
    public NutsBootContext getRunningContext() {
        return actualBootConfig;
    }

    @Override
    public NutsBootContext getBootContext() {
        return wsBootConfig;
    }

    @Override
    public NutsBootContext getConfigContext() {
        return new DefaultNutsBootContext(
                CoreNutsUtils.parseNutsId(NutsConstants.NUTS_ID_BOOT_API + "#" + config.getBootApiVersion()),
                CoreNutsUtils.parseNutsId(config.getBootRuntime()),
                config.getBootRuntimeDependencies(),
                config.getBootRepositories(),
                config.getBootJavaCommand(),
                config.getBootJavaOptions()
        )
                ;
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
        Set<String> imports = new LinkedHashSet<>(Arrays.asList(getConfig().getImports()));
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
        Set<String> imports = new LinkedHashSet<>(Arrays.asList(getConfig().getImports()));
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
    }

    @Override
    public String[] getImports() {
        String[] envImports = getConfig().getImports();
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
        return getConfig().getExtensions();
    }

    @Override
    public boolean isRepositoryEnabled(String repoId) {
        NutsRepositoryLocation r = getConfig().getRepository(repoId);
        return r != null && r.isEnabled();
    }

    @Override
    public void setRepositoryEnabled(String repoId, boolean enabled) {
        getConfig().getRepository(repoId).setEnabled(enabled);
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
            List<NutsSdkLocation> list = config.getSdk().get(name);
            if (list == null) {
                list = new ArrayList<>();
                config.getSdk().put(name, list);
            }
            if (list.contains(location)) {
                return false;
            }
            list.add(location);
            return true;
        }
        return false;
    }

    @Override
    public NutsSdkLocation findSdkByName(String name, String locationName) {
        if (locationName != null) {
            List<NutsSdkLocation> list = config.getSdk().get(name);
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
            List<NutsSdkLocation> list = config.getSdk().get(name);
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
            List<NutsSdkLocation> list = config.getSdk().get(name);
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
            List<NutsSdkLocation> list = config.getSdk().get(name);
            if (list != null) {
                for (Iterator<NutsSdkLocation> iterator = list.iterator(); iterator.hasNext(); ) {
                    NutsSdkLocation location2 = iterator.next();
                    if (location2.equals(location)) {
                        iterator.remove();
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
            List<NutsSdkLocation> list = config.getSdk().get(name);
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
        Set<String> s = config.getSdk().keySet();
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
        List<NutsSdkLocation> list = config.getSdk().get(type);
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
            getConfig().addExtension(extensionId);
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
                getConfig().removeExtension(extension);
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
                config.updateExtensionAt(i, extensionId);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isValidWorkspaceFolder(){
        String workspace = resolveWorkspacePath(this.workspace);
        File file = CoreIOUtils.createFile(workspace, NutsConstants.NUTS_WORKSPACE_CONFIG_FILE_NAME);
        if (file.isFile() && file.exists()) {
            return true;
        }
        return false;
    }

    @Override
    public String getWorkspaceLocation() {
        return workspace;
    }

    @Override
    public boolean isReadOnly() {
        return options.isReadOnly();
    }

    @Override
    public void save() {
        CoreNutsUtils.checkReadOnly(ws);
        ws.getSecurityManager().checkAllowed(NutsConstants.RIGHT_SAVE_WORKSPACE, "save");
        File file = CoreIOUtils.createFile(workspace, NutsConstants.NUTS_WORKSPACE_CONFIG_FILE_NAME);
        CoreJsonUtils.storeJson(config, file, true);
        NutsRepositoryFilter repositoryFilter = null;
        for (NutsRepository repo : ws.getEnabledRepositories(repositoryFilter)) {
            repo.save();
        }
        configurationChanged = false;
    }

    @Override
    public String getHomeLocation() {
        return nutsHome;
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
        map.put("nuts.workspace-location", NutsWorkspaceHelper.resolveImmediateWorkspacePath(workspace, NutsConstants.DEFAULT_WORKSPACE_NAME, getHomeLocation()));
        return map;
    }

    public void onInitializeWorkspace(
            NutsWorkspaceOptions options,
            String workspaceRoot,
            NutsWorkspaceFactory factory,
            DefaultNutsBootContext actualBootConfig,
            DefaultNutsBootContext wsBootConfig,
            String workspace,
            URL[] bootClassWorldURLs,
            ClassLoader bootClassLoader) {
        this.nutsHome = StringUtils.isEmpty(workspaceRoot) ? Nuts.getDefaultNutsHome() : workspaceRoot;
        this.options = options;

        this.actualBootConfig = actualBootConfig;
        this.wsBootConfig = wsBootConfig;

        this.bootClassLoader = bootClassLoader;
        this.bootClassWorldURLs = bootClassWorldURLs == null ? null : Arrays.copyOf(bootClassWorldURLs, bootClassWorldURLs.length);
        this.workspace =  resolveWorkspacePath(workspace);
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
                    + ws.getFileName(ws.createIdBuilder().setGroup(m.getGroupId()).setName(m.getArtifactId()).setVersion(m.getVersion()).build()
                    , "jar"));
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
                + ", nutsHome='" + nutsHome + '\''
                + ", workspace='" + workspace + '\''
                + ", cwd=" + cwd
                + '}';
    }

    public NutsUserConfig[] getUsers() {
        return getConfig().getSecurity();
    }

    public NutsUserConfig getUser(String userId) {
        NutsUserConfig config = getConfig().getSecurity(userId);
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
            getConfig().setSecurity(config);
        }
    }

    @Override
    public boolean isSecure() {
        return getConfig().isSecure();
    }

    @Override
    public void setSecure(boolean secure) {
        getConfig().setSecure(secure);
    }

    public void addRepository(NutsRepositoryLocation repository) {
        if (repository == null) {
            throw new NutsIllegalArgumentException("Invalid Repository");
        }
        if (StringUtils.isEmpty(repository.getId())) {
            throw new NutsIllegalArgumentException("Invalid Repository Id");
        }
        if (StringUtils.isEmpty(repository.getType())) {
            repository.setType(NutsConstants.REPOSITORY_TYPE_NUTS);
        }
        if (getConfig().containsRepository(repository.getId())) {
            throw new NutsIllegalArgumentException("Duplicate Repository Id " + repository.getId());
        }
        getConfig().addRepository(repository);
    }


    public void removeRepository(String repositoryId) {
        if (repositoryId == null) {
            throw new NutsIllegalArgumentException("Invalid Null Repository");
        }
        getConfig().removeRepository(repositoryId);
    }

    public NutsRepositoryLocation getRepository(String repositoryId) {
        return getConfig().getRepository(repositoryId);
    }

    public NutsRepositoryLocation[] getRepositories() {
        return getConfig().getRepositories();
    }

    public void setRepositories(NutsRepositoryLocation[] repositories) {
        for (NutsRepositoryLocation repositoryLocation : getRepositories()) {
            removeRepository(repositoryLocation.getId());
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
        if (StringUtils.isEmpty(value)) {
            getConfig().getEnv().remove(property);
        } else {
            getConfig().getEnv().setProperty(property, value);
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
        getConfig().removeSecurity(userId);
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
    public String getComponentsLocation() {
        return getConfig().getComponentsLocation();
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
    public boolean installCommand(NutsWorkspaceCommandConfig command,NutsConfirmAction confirmAction) {
        if (command == null
                || StringUtils.isEmpty(command.getName())
                || command.getName().contains(" ") || command.getName().contains("/") || command.getName().contains("\\")
                || command.getId() == null
                || command.getId().getName().isEmpty()
                || command.getId().getGroup().isEmpty()
                || command.getCommand() == null
        ) {
            throw new IllegalArgumentException("Invalid command " + (command == null ? "<NULL>" : command.getName()));
        }
        if (defaultCommandFactory.findCommand(command.getName(), ws) != null) {
            if(confirmAction==null){
                confirmAction=NutsConfirmAction.ERROR;
            }
            switch (confirmAction){
                case ERROR: throw new IllegalArgumentException("Command already exists " + command.getName());
                case IGNORE:return false;
            }
        }
        defaultCommandFactory.installCommand(command);
        return true;
    }

    @Override
    public void uninstallCommand(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Invalid command " + (name == null ? "<NULL>" : name));
        }
        defaultCommandFactory.uninstallCommand(name);
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
        f.configure(commandFactoryConfig);
        commandFactories.add(f);
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
            if (commandFactory.getFactoryId().equals(f.getFactoryId())) {
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
    }

    @Override
    public void uninstallCommandFactory(String factoryId) {
        if (factoryId == null || factoryId.isEmpty()) {
            throw new IllegalArgumentException("Invalid WorkspaceCommandFactory " + factoryId);
        }
        NutsWorkspaceCommandFactory removeMe = null;
        for (Iterator<NutsWorkspaceCommandFactory> iterator = commandFactories.iterator(); iterator.hasNext(); ) {
            NutsWorkspaceCommandFactory factory = iterator.next();
            if (factoryId.equals(factory.getFactoryId())) {
                iterator.remove();
                break;
            }
        }
        List<NutsWorkspaceCommandFactoryConfig> commandFactories = config.getCommandFactories();
        if (commandFactories != null) {
            for (Iterator<NutsWorkspaceCommandFactoryConfig> iterator = commandFactories.iterator(); iterator.hasNext(); ) {
                NutsWorkspaceCommandFactoryConfig commandFactory = iterator.next();
                if (factoryId.equals(commandFactory.getFactoryId())) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    @Override
    public List<NutsWorkspaceCommand> findCommands(NutsId id) {
        HashMap<String, NutsWorkspaceCommand> all = new HashMap<>();
        for (NutsWorkspaceCommandConfig command : defaultCommandFactory.findCommands(id, ws)) {
            all.put(command.getName(), toDefaultNutsWorkspaceCommand(command));
        }
        return new ArrayList<>(all.values());
    }

    private class ConfigNutsWorkspaceCommandFactory implements NutsWorkspaceCommandFactory {
        @Override
        public void configure(NutsWorkspaceCommandFactoryConfig config) {

        }

        @Override
        public String getFactoryId() {
            return "default";
        }

        public File getRootFolder() {
            String storeRoot = getStoreRoot(RootFolderType.PROGRAMS);
            if (storeRoot == null) {
                return null;
            }
            return new File(storeRoot);
        }

        @Override
        public int getPriority() {
            return Integer.MAX_VALUE;
        }

        public void uninstallCommand(String name) {
            File file = new File(getRootFolder(), name + NutsConstants.NUTS_COMMAND_FILE_EXTENSION);
            if (file.isFile()) {
                if (!file.delete()) {
                    throw new IllegalArgumentException("Unable to delete file " + file.getPath());
                }
            }
        }

        public void installCommand(NutsWorkspaceCommandConfig command) {
            File file = new File(getRootFolder(), command.getName() + NutsConstants.NUTS_COMMAND_FILE_EXTENSION);
            ws.getIOManager().writeJson(command, file, true);
        }

        @Override
        public NutsWorkspaceCommandConfig findCommand(String name, NutsWorkspace workspace) {
            File file = new File(getRootFolder(), name + NutsConstants.NUTS_COMMAND_FILE_EXTENSION);
            if (file.exists()) {
                NutsWorkspaceCommandConfig c = ws.getIOManager().readJson(file, NutsWorkspaceCommandConfig.class);
                if (c != null) {
                    c.setName(name);
                    return c;
                }
            }
            return null;
        }

        @Override
        public List<NutsWorkspaceCommandConfig> findCommands(NutsWorkspace workspace) {
            return findCommands((ObjectFilter<NutsWorkspaceCommandConfig>) null);
        }

        public List<NutsWorkspaceCommandConfig> findCommands(NutsId id, NutsWorkspace workspace) {
            return findCommands(new ObjectFilter<NutsWorkspaceCommandConfig>() {
                @Override
                public boolean accept(NutsWorkspaceCommandConfig value) {
                    if (id.getVersion().isEmpty()) {
                        return value.getId().getSimpleName().equals(id.getSimpleName());
                    } else {
                        return value.getId().getLongName().equals(id.getLongName());
                    }
                }
            });
        }

        public List<NutsWorkspaceCommandConfig> findCommands(ObjectFilter<NutsWorkspaceCommandConfig> filter) {
            List<NutsWorkspaceCommandConfig> all = new ArrayList<>();
            File[] files = getRootFolder().listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(NutsConstants.NUTS_COMMAND_FILE_EXTENSION)) {
                        NutsWorkspaceCommandConfig c = null;
                        try {
                            c = ws.getIOManager().readJson(file, NutsWorkspaceCommandConfig.class);
                        } catch (Exception ex) {
                            //
                        }
                        if (c != null) {
                            c.setName(file.getName().substring(0, file.getName().length() - 4));
                            if (filter == null || filter.accept(c)) {
                                all.add(c);
                            }
                        }
                    }
                }
            }
            return all;
        }
    }

    @Override
    public String getStoreRoot(RootFolderType folderType) {
        if (folderType == null) {
            folderType = RootFolderType.PROGRAMS;
        }
        String k = null;
        String v = null;
        switch (folderType) {
            case PROGRAMS: {
                k = NutsConstants.ENV_STORE_PROGRAMS;
                break;
            }
            case VAR: {
                k = NutsConstants.ENV_STORE_VAR;
                break;
            }
            case LOGS: {
                k = NutsConstants.ENV_STORE_LOGS;
                break;
            }
            case CONFIG: {
                k = NutsConstants.ENV_STORE_CONFIG;
                break;
            }
            case TEMP: {
                k = NutsConstants.ENV_STORE_TEMP;
                break;
            }
            case CACHE: {
                k = NutsConstants.ENV_STORE_CACHE;
                break;
            }
            default: {
                k = NutsConstants.ENV_STORE_TEMP;
                break;
            }
        }
        v = Nuts.getDefaultWorkspaceFolder(getWorkspaceLocation(),getHomeLocation(),folderType);
        return CoreIOUtils.resolvePath(getEnv(k, v),
                new File(ws.getIOManager().resolvePath(getWorkspaceLocation())),
                getHomeLocation()).getPath();
    }

    @Override
    public String getStoreRoot(String id, RootFolderType folderType) {
        return getStoreRoot(ws.getParseManager().parseId(id), folderType);
    }

    @Override
    public String getStoreRoot(NutsId id, RootFolderType folderType) {
        if (StringUtils.isEmpty(id.getGroup())) {
            throw new NutsElementNotFoundException("Missing group for " + id);
        }
        String storeRoot = getStoreRoot(folderType);
        if (storeRoot == null) {
            return null;
        }
        File groupFolder = new File(storeRoot, id.getGroup().replaceAll("\\.", File.separator));
        if (StringUtils.isEmpty(id.getName())) {
            throw new NutsElementNotFoundException("Missing name for " + id.toString());
        }
        File artifactFolder = new File(groupFolder, id.getName());
        if (id.getVersion().isEmpty()) {
            throw new NutsElementNotFoundException("Missing version for " + id.toString());
        }
        return new File(artifactFolder, id.getVersion().getValue()).getPath();
    }

    private String resolveWorkspacePath(String workspace) {
        if (StringUtils.isEmpty(workspace)) {
            File file = CoreIOUtils.resolvePath(getHomeLocation() + "/" + NutsConstants.DEFAULT_WORKSPACE_NAME, null, getHomeLocation());
            workspace = file == null ? null : file.getPath();
        } else {
            File file = CoreIOUtils.resolvePath(workspace, null, getHomeLocation());
            workspace = file == null ? null : file.getPath();
        }

        Set<String> visited = new HashSet<String>();
        while (true) {
            File file = CoreIOUtils.createFile(workspace, NutsConstants.NUTS_WORKSPACE_CONFIG_FILE_NAME);
            NutsWorkspaceConfig nutsWorkspaceConfig = CoreJsonUtils.loadJson(file, NutsWorkspaceConfig.class);
            if (nutsWorkspaceConfig != null) {
                String nextWorkspace = nutsWorkspaceConfig.getWorkspace();
                if (nextWorkspace != null && nextWorkspace.trim().length() > 0) {
                    if (visited.contains(nextWorkspace)) {
                        throw new NutsException("Circular Workspace Dependency : " + nextWorkspace);
                    }
                    visited.add(nextWorkspace);
                    workspace = nextWorkspace;
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        return workspace;
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

    public DefaultNutsWorkspaceConfigManager setStartCreateTimeMillis(long startCreateTime) {
        this.startCreateTime = startCreateTime;
        return this;
    }

    @Override
    public long getCreationFinishTimeMillis() {
        return endCreateTime;
    }

    public DefaultNutsWorkspaceConfigManager setEndCreateTimeMillis(long endCreateTime) {
        this.endCreateTime = endCreateTime;
        return this;
    }

    @Override
    public long getCreationTimeMillis() {
        return endCreateTime-startCreateTime;
    }

    @Override
    public NutsAuthenticationAgent createAuthenticationAgent(String authenticationAgent){
        NutsAuthenticationAgent supported = ws.getExtensionManager().createSupported(NutsAuthenticationAgent.class, authenticationAgent);
        if(supported==null){
            throw new NutsExtensionMissingException(NutsAuthenticationAgent.class, "AuthenticationAgent");
        }
        return supported;
    }
}