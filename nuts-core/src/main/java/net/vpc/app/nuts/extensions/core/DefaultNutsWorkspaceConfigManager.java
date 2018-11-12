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
    private NutsId workspaceBootId;
    private NutsBootWorkspace workspaceBoot;
    private NutsId workspaceRuntimeId;
    private String nutsHome;
    private ClassLoader bootClassLoader;
    private URL[] bootClassWorldURLs;
    private NutsWorkspaceConfig config = new NutsWorkspaceConfig();
    private String workspace;
    private String cwd = System.getProperty("user.dir");
    private ObservableMap<String, Object> sharedObjects = new ObservableMap<>();

    protected DefaultNutsWorkspaceConfigManager(final DefaultNutsWorkspace outer) {
        this.ws = outer;
    }

    public NutsBootWorkspace getWorkspaceBoot() {
        return workspaceBoot;
    }

    public ClassLoader getBootClassLoader() {
        return bootClassLoader;
    }

    public void setConfig(NutsWorkspaceConfig config) {
        this.config = config;
    }

    @Override
    public NutsId getWorkspaceRuntimeId() {
        return workspaceRuntimeId;
    }

    @Override
    public NutsId getWorkspaceBootId() {
        return workspaceBootId;
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
        String[] arr = imports.toArray(new String[imports.size()]);
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
        String[] arr = imports.toArray(new String[imports.size()]);
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
        String[] arr = simports.toArray(new String[simports.size()]);
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
            for (String s : extraImports.split("[,;: ]")) {
                all.add(s);
            }
        }
        return all.toArray(new String[all.size()]);
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
        return s.toArray(new String[s.size()]);
    }

    @Override
    public NutsSdkLocation getSdk(String type, String requestedVersion) {
        NutsVersionFilter javaVersionFilter = CoreVersionUtils.createNutsVersionFilter(requestedVersion);
        NutsSdkLocation best = null;
        for (NutsSdkLocation jdk : getSdks("java")) {
            String currVersion = jdk.getVersion();
            if (javaVersionFilter.accept(new NutsVersionImpl(currVersion))) {
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
        return list.toArray(new NutsSdkLocation[list.size()]);
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
            if (extension.isSameFullName(extensionId)) {
                getConfig().removeExtension(extension);
                return true;
            }
        }
        return false;
    }

    @Override
    public String getWorkspaceLocation() {
        return workspace;
    }

    @Override
    public void save() {
        if (!ws.getSecurityManager().isAllowed(NutsConstants.RIGHT_SAVE_WORKSPACE)) {
            throw new NutsSecurityException("Not Allowed " + NutsConstants.RIGHT_SAVE_WORKSPACE);
        }
        File file = CoreIOUtils.createFile(workspace, NutsConstants.NUTS_WORKSPACE_CONFIG_FILE_NAME);
        CoreJsonUtils.storeJson(config, file, true);
        NutsRepositoryFilter repositoryFilter = null;
        for (NutsRepository repo : ws.getEnabledRepositories(repositoryFilter)) {
            repo.save();
        }
    }

    @Override
    public String getNutsHomeLocation() {
        return nutsHome;
    }

    @Override
    public Map<String, String> getRuntimeProperties() {
        Map<String, String> map = new HashMap<>();
        map.put("nuts.workspace-boot.version", workspaceBootId.getVersion().toString());
        map.put("nuts.workspace-boot.id", workspaceBootId.toString());
        map.put("nuts.workspace-runtime.id", getWorkspaceRuntimeId().toString());
        map.put("nuts.workspace-runtime.version", getWorkspaceRuntimeId().getVersion().toString());
        map.put("nuts.workspace-location", NutsWorkspaceHelper.resolveImmediateWorkspacePath(workspace, NutsConstants.DEFAULT_WORKSPACE_NAME, getNutsHomeLocation()));
        return map;
    }

    @Override
    public NutsBootWorkspace getBoot() {
        return workspaceBoot;
    }

    public void onInitializeWorkspace(NutsBootWorkspace workspaceBoot,
                                      String workspaceRoot,
                                      NutsWorkspaceFactory factory,
                                      NutsId workspaceBootId, NutsId workspaceRuntimeId, String workspace,
                                      URL[] bootClassWorldURLs,
                                      ClassLoader bootClassLoader) {
        this.workspaceBoot = workspaceBoot;
        this.nutsHome = workspaceRoot;
        this.workspaceBootId = workspaceBootId;
        this.workspaceRuntimeId = workspaceRuntimeId;
        this.bootClassLoader = bootClassLoader;
        this.bootClassWorldURLs = bootClassWorldURLs == null ? null : Arrays.copyOf(bootClassWorldURLs, bootClassWorldURLs.length);
        this.workspace = workspace;
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
            NutsId baseId = CoreNutsUtils.parseOrErrorNutsId(NutsConstants.NUTS_ID_BOOT);
            String urlPath = "/META-INF/maven/" + baseId.getGroup() + "/" + baseId.getName() + "/pom.properties";
            URL resource = Nuts.class.getResource(urlPath);
            if (resource != null) {
                URL runtimeURL = CorePlatformUtils.resolveURLFromResource(Nuts.class, urlPath);
                File file = CorePlatformUtils.resolveLocalFileFromURL(runtimeURL);
                return file == null ? null : file.getPath();
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return null;
    }

    public void addSharedObjectsListener(MapListener<String, Object> listener) {
        sharedObjects.addListener(listener);
    }

    @Override
    public void removeSharedObjectsListener(MapListener<String, Object> listener) {
        sharedObjects.removeListener(listener);
    }

    @Override
    public MapListener<String, Object>[] getSharedObjectsListeners() {
        return sharedObjects.getListeners();
    }

    @Override
    public Map<String, Object> getSharedObjects() {
        return sharedObjects;
    }

    @Override
    public String toString() {
        return "NutsWorkspaceConfig{"
                + "workspaceBootId=" + workspaceBootId
                + ", workspaceBoot=" + workspaceBoot
                + ", workspaceRuntimeId=" + workspaceRuntimeId
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
            if (extension.isSameFullName(extension)) {
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
        return JavaHelper.resolveJdkLocation(path,ws);
    }


    @Override
    public byte[] decryptString(byte[] input) {
        if(input==null || input.length==0){
            return new byte[0];
        }
        String passphrase = getEnv(NutsConstants.ENV_KEY_PASSPHRASE, CoreNutsUtils.DEFAULT_PASSPHRASE);
        return CoreSecurityUtils.httpDecrypt(input, passphrase);
    }

    @Override
    public byte[] encryptString(byte[] input) {
        if(input==null || input.length==0){
            return new byte[0];
        }
        String passphrase = getEnv(NutsConstants.ENV_KEY_PASSPHRASE, CoreNutsUtils.DEFAULT_PASSPHRASE);
        return CoreSecurityUtils.httpEncrypt(input, passphrase);
    }

}