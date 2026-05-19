package net.thevpc.nuts.runtime.standalone.workspace;

import net.thevpc.nuts.app.NApp;
import net.thevpc.nuts.app.NApplicationHandleMode;
import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.boot.NBootOptionsInfo;
import net.thevpc.nuts.boot.NWorkspaceTerminalOptions;
import net.thevpc.nuts.command.*;
import net.thevpc.nuts.concurrent.NScopedValue;
import net.thevpc.nuts.core.*;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.platform.*;
import net.thevpc.nuts.runtime.standalone.repository.config.DefaultNRepositoryModel;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NInstalledRepository;
import net.thevpc.nuts.runtime.standalone.session.DefaultNSession;
import net.thevpc.nuts.runtime.standalone.store.NWorkspaceStore;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NExecutionContextBuilder;
import net.thevpc.nuts.runtime.standalone.workspace.config.*;
import net.thevpc.nuts.spi.NDependencySolver;
import net.thevpc.nuts.spi.NIndexStoreFactory;
import net.thevpc.nuts.spi.NInstallerComponent;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NOptional;

import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class NWorkspaceExtAdapter extends AbstractNWorkspace implements NWorkspaceExt {
    private final NWorkspaceExt baseExt;
    private final NWorkspace base;
    private final NScopedValue<NSession> sessionScopes = new NScopedValue<>();
    private NSession bootSession;

    public NWorkspaceExtAdapter(NWorkspaceExt baseExt) {
        this.baseExt = baseExt;
        this.base = (NWorkspace) baseExt;
    }

    @Override
    public NBootOptionsInfo getCallerBootOptionsInfo() {
        return baseExt.getCallerBootOptionsInfo();
    }

    public NScopedValue<NSession> sessionScopes() {
        return sessionScopes;
    }

    @Override
    public Map<String, String> getSysEnv() {
        return baseExt.getSysEnv();
    }

    @Override
    public NApp getApp() {
        return baseExt.getApp();
    }

    @Override
    public NWorkspaceStore store() {
        return baseExt.store();
    }

    @Override
    public NText getWelcomeText() {
        return baseExt.getWelcomeText();
    }

    @Override
    public NText getHelpText() {
        return baseExt.getHelpText();
    }

    @Override
    public NText getLicenseText() {
        return baseExt.getLicenseText();
    }

    @Override
    public NText resolveDefaultHelp(Class<?> clazz) {
        return baseExt.resolveDefaultHelp(clazz);
    }

    @Override
    public NId resolveEffectiveId(NDescriptor descriptor) {
        return baseExt.resolveEffectiveId(descriptor);
    }

    @Override
    public NIdType resolveNutsIdType(NId id) {
        return baseExt.resolveNutsIdType(id);
    }

    @Override
    public NInstallerComponent getInstaller(NDefinition nutToInstall) {
        return baseExt.getInstaller(nutToInstall);
    }

    @Override
    public boolean requiresRuntimeExtension() {
        return baseExt.requiresRuntimeExtension();
    }

    @Override
    public NInstalledRepository getInstalledRepository() {
        return baseExt.getInstalledRepository();
    }

    @Override
    public NInstallStatus getInstallStatus(NId id, boolean checkDependencies) {
        return baseExt.getInstallStatus(id, checkDependencies);
    }

    @Override
    public NExecutionContextBuilder createExecutionContext() {
        return baseExt.createExecutionContext();
    }

    @Override
    public void deployBoot(NId def, boolean withDependencies) {
        baseExt.deployBoot(def, withDependencies);
    }

    @Override
    public NSession defaultSession() {
        if (bootSession == null) {
            this.bootSession = new DefaultNSession(this, null);
            this.bootSession.copyFrom(baseExt.defaultSession());
        }
        return bootSession;
    }

    @Override
    public NWorkspaceModel getModel() {
        return baseExt.getModel();
    }

    @Override
    public String getInstallationDigest() {
        return baseExt.getInstallationDigest();
    }

    @Override
    public void setInstallationDigest(String value) {
        baseExt.setInstallationDigest(value);
    }

    @Override
    public DefaultNRepositoryModel getRepositoryModel() {
        return baseExt.getRepositoryModel();
    }

    @Override
    public DefaultCustomCommandsModel getCommandModel() {
        return baseExt.getCommandModel();
    }

    @Override
    public DefaultNWorkspaceConfigModel getConfigModel() {
        return baseExt.getConfigModel();
    }

    @Override
    public DefaultImportModel getImportModel() {
        return baseExt.getImportModel();
    }

    @Override
    public NDependencySolver createDependencySolver(String solverName) {
        return baseExt.createDependencySolver(solverName);
    }

    @Override
    public List<String> getDependencySolverNames() {
        return baseExt.getDependencySolverNames();
    }

    @Override
    public DefaultNWorkspaceLocationModel getLocationModel() {
        return baseExt.getLocationModel();
    }

    @Override
    public NDescriptor resolveEffectiveDescriptor(NDescriptor descriptor, NDescriptorEffectiveConfig effectiveNDescriptorConfig) {
        return base.resolveEffectiveDescriptor(descriptor, effectiveNDescriptorConfig);
    }

    @Override
    public void runApplication(NApplicationHandleMode handleMode) {
        NWorkspaceHelper.runApplication(this, handleMode);
    }

    @Override
    public NDescriptor resolveEffectiveDescriptor(NDescriptor descriptor) {
        return base.resolveEffectiveDescriptor(descriptor);
    }


    @Override
    public String uuid() {
        return base.uuid();
    }

    @Override
    public String name() {
        return base.name();
    }

    @Override
    public String digestName() {
        return base.digestName();
    }

    @Override
    public NVersion apiVersion() {
        return base.apiVersion();
    }

    @Override
    public NVersion bootVersion() {
        return base.bootVersion();
    }

    @Override
    public NId apiId() {
        return base.apiId();
    }

    @Override
    public NId appId() {
        return base.appId();
    }

    @Override
    public NId runtimeId() {
        return base.runtimeId();
    }

    @Override
    public NPath location() {
        return base.location();
    }

    @Override
    public NSession createSession() {
        return null;
    }

    @Override
    public NSession currentSession() {
        NSession old = sessionScopes().get();
        if (old == null) {
            return defaultSession();
        }
        return old;
    }

    @Override
    public NExtensions extensions() {
        return base.extensions();
    }

    @Override
    public void close() {
        //base.close();
    }

    @Override
    public NRepository addRepository(NRepositorySpec options) {
        return base.addRepository(options);
    }

    @Override
    public NRepository addRepository(String repositoryNamedUrl) {
        return base.addRepository(repositoryNamedUrl);
    }

    @Override
    public NOptional<NRepository> findRepositoryById(String repositoryIdOrName) {
        return base.findRepositoryById(repositoryIdOrName);
    }

    @Override
    public NOptional<NRepository> findRepositoryByName(String repositoryIdOrName) {
        return base.findRepositoryByName(repositoryIdOrName);
    }

    @Override
    public NOptional<NRepository> getRepository(String repositoryIdOrName) {
        return base.getRepository(repositoryIdOrName);
    }

    @Override
    public NWorkspace removeRepository(String locationOrRepositoryId) {
        return base.removeRepository(locationOrRepositoryId);
    }

    @Override
    public List<NRepository> repositories() {
        return base.repositories();
    }

    @Override
    public NWorkspace removeAllRepositories() {
        return base.removeAllRepositories();
    }

    @Override
    public Map<String, Object> properties() {
        return base.properties();
    }

    @Override
    public NOptional<Object> getProperty(String property) {
        return base.getProperty(property);
    }

    @Override
    public <T> NOptional<T> getProperty(Class<T> propertyTypeAndName) {
        return base.getProperty(propertyTypeAndName);
    }

    @Override
    public NWorkspace setProperty(String property, Object value) {
        return base.setProperty(property, value);
    }

    @Override
    public <T> T getOrComputeProperty(Class<T> property, Supplier<T> supplier) {
        return base.getOrComputeProperty(property, supplier);
    }

    @Override
    public <T> T getOrComputeProperty(String property, Supplier<T> supplier) {
        return base.getOrComputeProperty(property, supplier);
    }

    @Override
    public String pid() {
        return base.pid();
    }

    @Override
    public void addLauncher(NLauncherOptions launcher) {
        base.addLauncher(launcher);
    }

    @Override
    public List<String> buildEffectiveCommand(String[] cmd, NRunAs runAsMode, Set<NDesktopEnvironmentFamily> de, Function<String, String> sysWhich, Boolean gui, String rootName, String userName, String[] executorOptions) {
        return base.buildEffectiveCommand(cmd, runAsMode, de, sysWhich, gui, rootName, userName, executorOptions);
    }

    @Override
    public NPath getHomeLocation(NStoreType folderType) {
        return base.getHomeLocation(folderType);
    }

    @Override
    public NStoreStrategy storeStrategy() {
        return base.storeStrategy();
    }

    @Override
    public NWorkspace storeStrategy(NStoreStrategy strategy) {
        return base.storeStrategy(strategy);
    }

    @Override
    public NStoreStrategy repositoryStoreStrategy() {
        return base.repositoryStoreStrategy();
    }

    @Override
    public NOsFamily storeLayout() {
        return base.storeLayout();
    }

    @Override
    public NWorkspace storeLayout(NOsFamily storeLayout) {
        return base.storeLayout(storeLayout);
    }

    @Override
    public Map<NStoreType, String> storeLocations() {
        return base.storeLocations();
    }

    @Override
    public String getDefaultIdFilename(NId id) {
        return base.getDefaultIdFilename(id);
    }

    @Override
    public NPath getDefaultIdBasedir(NId id) {
        return base.getDefaultIdBasedir(id);
    }

    @Override
    public String getDefaultIdContentExtension(String packaging) {
        return base.getDefaultIdContentExtension(packaging);
    }

    @Override
    public String getDefaultIdExtension(NId id) {
        return base.getDefaultIdExtension(id);
    }

    @Override
    public Map<NHomeLocation, String> homeLocations() {
        return base.homeLocations();
    }

    @Override
    public NPath getHomeLocation(NHomeLocation location) {
        return base.getHomeLocation(location);
    }

    @Override
    public NPath workspaceLocation() {
        return base.workspaceLocation();
    }

    @Override
    public NWorkspace setStoreLocation(NStoreType folderType, String location) {
        return base.setStoreLocation(folderType, location);
    }

    @Override
    public NWorkspace setHomeLocation(NHomeLocation homeType, String location) {
        return base.setHomeLocation(homeType, location);
    }

    @Override
    public NOptional<String> findSysCommand(String name) {
        return base.findSysCommand(name);
    }

    @Override
    public NWorkspace addImports(String... importExpression) {
        return base.addImports(importExpression);
    }

    @Override
    public NWorkspace clearImports() {
        return base.clearImports();
    }

    @Override
    public NWorkspace removeImports(String... importExpression) {
        return base.removeImports(importExpression);
    }

    @Override
    public NWorkspace updateImports(String[] imports) {
        return base.updateImports(imports);
    }

    @Override
    public Set<String> allImports() {
        return base.allImports();
    }

    @Override
    public boolean isImportedGroupId(String groupId) {
        return base.isImportedGroupId(groupId);
    }

    @Override
    public NWorkspaceStoredConfig storedConfig() {
        return base.storedConfig();
    }

    @Override
    public boolean isReadOnly() {
        return base.isReadOnly();
    }

    @Override
    public boolean saveConfig(boolean force) {
        return base.saveConfig(force);
    }

    @Override
    public boolean saveConfig() {
        return base.saveConfig();
    }

    @Override
    public NWorkspaceBootConfig loadBootConfig(String path, boolean global, boolean followLinks) {
        return base.loadBootConfig(path, global, followLinks);
    }

    @Override
    public boolean isSupportedRepositoryType(String repositoryType) {
        return base.isSupportedRepositoryType(repositoryType);
    }

    @Override
    public List<NRepositorySpec> defaultRepositories() {
        return base.defaultRepositories();
    }

    @Override
    public Set<String> availableArchetypes() {
        return base.availableArchetypes();
    }

    @Override
    public NPath resolveRepositoryPath(String repositoryLocation) {
        return base.resolveRepositoryPath(repositoryLocation);
    }

    @Override
    public NIndexStoreFactory indexStoreClientFactory() {
        return base.indexStoreClientFactory();
    }

    @Override
    public String javaCommand() {
        return base.javaCommand();
    }

    @Override
    public String javaOptions() {
        return base.javaOptions();
    }

    @Override
    public boolean isSystemWorkspace() {
        return base.isSystemWorkspace();
    }

    @Override
    public Map<String, String> configMap() {
        return base.configMap();
    }

    @Override
    public NOptional<NLiteral> getConfigProperty(String property) {
        return base.getConfigProperty(property);
    }

    @Override
    public NWorkspace setConfigProperty(String property, String value) {
        return base.setConfigProperty(property, value);
    }

    @Override
    public List<NCommandFactoryConfig> commandFactories() {
        return base.commandFactories();
    }

    @Override
    public void addCommandFactory(NCommandFactoryConfig commandFactory) {
        base.addCommandFactory(commandFactory);
    }

    @Override
    public void removeCommandFactory(String commandFactoryId) {
        base.removeCommandFactory(commandFactoryId);
    }

    @Override
    public boolean removeCommandFactoryIfExists(String commandFactoryId) {
        return base.removeCommandFactoryIfExists(commandFactoryId);
    }

    @Override
    public boolean commandExists(String command) {
        return base.commandExists(command);
    }

    @Override
    public boolean commandFactoryExists(String command) {
        return base.commandFactoryExists(command);
    }

    @Override
    public boolean addCommand(NCommandConfig command) {
        return base.addCommand(command);
    }

    @Override
    public boolean updateCommand(NCommandConfig command) {
        return base.updateCommand(command);
    }

    @Override
    public void removeCommand(String command) {
        base.removeCommand(command);
    }

    @Override
    public boolean removeCommandIfExists(String name) {
        return base.removeCommandIfExists(name);
    }

    @Override
    public NCustomCmd findCommand(String name, NId forId, NId forOwner) {
        return base.findCommand(name, forId, forOwner);
    }

    @Override
    public NCustomCmd findCommand(String name) {
        return base.findCommand(name);
    }

    @Override
    public List<NCustomCmd> findAllCommands() {
        return base.findAllCommands();
    }

    @Override
    public List<NCustomCmd> findCommandsByOwner(NId id) {
        return base.findCommandsByOwner(id);
    }

    @Override
    public boolean isFirstBoot() {
        return base.isFirstBoot();
    }

    @Override
    public NOptional<NLiteral> getCustomBootOption(String... names) {
        return base.getCustomBootOption(names);
    }

    @Override
    public NBootOptions bootOptions() {
        return base.bootOptions();
    }

    @Override
    public ClassLoader bootClassLoader() {
        return base.bootClassLoader();
    }

    @Override
    public List<URL> bootClassWorldURLs() {
        return base.bootClassWorldURLs();
    }

    @Override
    public List<String> bootRepositories() {
        return base.bootRepositories();
    }

    @Override
    public Instant creationStartTime() {
        return base.creationStartTime();
    }

    @Override
    public Instant creationFinishTime() {
        return base.creationFinishTime();
    }

    @Override
    public NDuration creationDuration() {
        return base.creationDuration();
    }

    @Override
    public NClassLoaderNode bootRuntimeClassLoaderNode() {
        return base.bootRuntimeClassLoaderNode();
    }

    @Override
    public List<NClassLoaderNode> bootExtensionClassLoaderNodes() {
        return base.bootExtensionClassLoaderNodes();
    }

    @Override
    public NWorkspaceTerminalOptions bootTerminal() {
        return base.bootTerminal();
    }

    @Override
    public void runBootCommand() {
        NWorkspaceHelper.runBootCommand(this);
    }
}
