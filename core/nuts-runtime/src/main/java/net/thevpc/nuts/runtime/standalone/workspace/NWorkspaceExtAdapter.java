package net.thevpc.nuts.runtime.standalone.workspace;

import net.thevpc.nuts.app.NApp;
import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.boot.NWorkspaceTerminalOptions;
import net.thevpc.nuts.command.*;
import net.thevpc.nuts.concurrent.NScopedValue;
import net.thevpc.nuts.core.*;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.platform.*;
import net.thevpc.nuts.runtime.standalone.repository.config.DefaultNRepositoryModel;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NInstalledRepository;
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
    private NWorkspaceExt baseExt;
    private NWorkspace base;

    public NWorkspaceExtAdapter(NWorkspaceExt baseExt) {
        this.baseExt = baseExt;
        this.base = (NWorkspace) baseExt;
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
        return baseExt.defaultSession();
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
    public NScopedValue<NSession> sessionScopes() {
        return baseExt.sessionScopes();
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
    public NDescriptor resolveEffectiveDescriptor(NDescriptor descriptor) {
        return base.resolveEffectiveDescriptor(descriptor);
    }


    @Override
    public String getUuid() {
        return base.getUuid();
    }

    @Override
    public String getName() {
        return base.getName();
    }

    @Override
    public String getDigestName() {
        return base.getDigestName();
    }

    @Override
    public NVersion getApiVersion() {
        return base.getApiVersion();
    }

    @Override
    public NVersion getBootVersion() {
        return base.getBootVersion();
    }

    @Override
    public NId getApiId() {
        return base.getApiId();
    }

    @Override
    public NId getAppId() {
        return base.getAppId();
    }

    @Override
    public NId getRuntimeId() {
        return base.getRuntimeId();
    }

    @Override
    public NPath getLocation() {
        return base.getLocation();
    }

    @Override
    public NSession createSession() {
        return null;
    }

    @Override
    public NSession currentSession() {
        return null;
    }

    @Override
    public NExtensions extensions() {
        return null;
    }

    @Override
    public void close() {
        //base.close();
    }

    @Override
    public NRepository addRepository(NAddRepositoryOptions options) {
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
    public NOptional<NRepository> findRepository(String repositoryIdOrName) {
        return base.findRepository(repositoryIdOrName);
    }

    @Override
    public NWorkspace removeRepository(String locationOrRepositoryId) {
        return base.removeRepository(locationOrRepositoryId);
    }

    @Override
    public List<NRepository> getRepositories() {
        return base.getRepositories();
    }

    @Override
    public NWorkspace removeAllRepositories() {
        return base.removeAllRepositories();
    }

    @Override
    public Map<String, Object> getProperties() {
        return base.getProperties();
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
    public String getPid() {
        return base.getPid();
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
    public NPath getStoreLocation(NStoreType folderType) {
        return base.getStoreLocation(folderType);
    }

    @Override
    public NPath getStoreLocation(NId id, NStoreType folderType) {
        return base.getStoreLocation(id, folderType);
    }

    @Override
    public NPath getStoreLocation(NStoreType folderType, String repositoryIdOrName) {
        return base.getStoreLocation(folderType, repositoryIdOrName);
    }

    @Override
    public NPath getStoreLocation(NId id, NStoreType folderType, String repositoryIdOrName) {
        return base.getStoreLocation(id, folderType, repositoryIdOrName);
    }

    @Override
    public NPath getStoreLocation(NLocationKey nLocationKey) {
        return base.getStoreLocation(nLocationKey);
    }

    @Override
    public NStoreStrategy getStoreStrategy() {
        return base.getStoreStrategy();
    }

    @Override
    public NWorkspace setStoreStrategy(NStoreStrategy strategy) {
        return base.setStoreStrategy(strategy);
    }

    @Override
    public NStoreStrategy getRepositoryStoreStrategy() {
        return base.getRepositoryStoreStrategy();
    }

    @Override
    public NOsFamily getStoreLayout() {
        return base.getStoreLayout();
    }

    @Override
    public NWorkspace setStoreLayout(NOsFamily storeLayout) {
        return base.setStoreLayout(storeLayout);
    }

    @Override
    public Map<NStoreType, String> getStoreLocations() {
        return base.getStoreLocations();
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
    public Map<NHomeLocation, String> getHomeLocations() {
        return base.getHomeLocations();
    }

    @Override
    public NPath getHomeLocation(NHomeLocation location) {
        return base.getHomeLocation(location);
    }

    @Override
    public NPath getWorkspaceLocation() {
        return base.getWorkspaceLocation();
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
    public Set<String> getAllImports() {
        return base.getAllImports();
    }

    @Override
    public boolean isImportedGroupId(String groupId) {
        return base.isImportedGroupId(groupId);
    }

    @Override
    public NWorkspaceStoredConfig getStoredConfig() {
        return base.getStoredConfig();
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
    public List<NAddRepositoryOptions> getDefaultRepositories() {
        return base.getDefaultRepositories();
    }

    @Override
    public Set<String> getAvailableArchetypes() {
        return base.getAvailableArchetypes();
    }

    @Override
    public NPath resolveRepositoryPath(String repositoryLocation) {
        return base.resolveRepositoryPath(repositoryLocation);
    }

    @Override
    public NIndexStoreFactory getIndexStoreClientFactory() {
        return base.getIndexStoreClientFactory();
    }

    @Override
    public String getJavaCommand() {
        return base.getJavaCommand();
    }

    @Override
    public String getJavaOptions() {
        return base.getJavaOptions();
    }

    @Override
    public boolean isSystemWorkspace() {
        return base.isSystemWorkspace();
    }

    @Override
    public Map<String, String> getConfigMap() {
        return base.getConfigMap();
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
    public List<NCommandFactoryConfig> getCommandFactories() {
        return base.getCommandFactories();
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
    public NBootOptions getBootOptions() {
        return base.getBootOptions();
    }

    @Override
    public ClassLoader getBootClassLoader() {
        return base.getBootClassLoader();
    }

    @Override
    public List<URL> getBootClassWorldURLs() {
        return base.getBootClassWorldURLs();
    }

    @Override
    public List<String> getBootRepositories() {
        return base.getBootRepositories();
    }

    @Override
    public Instant getCreationStartTime() {
        return base.getCreationStartTime();
    }

    @Override
    public Instant getCreationFinishTime() {
        return base.getCreationFinishTime();
    }

    @Override
    public NDuration getCreationDuration() {
        return base.getCreationDuration();
    }

    @Override
    public NClassLoaderNode getBootRuntimeClassLoaderNode() {
        return base.getBootRuntimeClassLoaderNode();
    }

    @Override
    public List<NClassLoaderNode> getBootExtensionClassLoaderNode() {
        return base.getBootExtensionClassLoaderNode();
    }

    @Override
    public NWorkspaceTerminalOptions getBootTerminal() {
        return base.getBootTerminal();
    }

    @Override
    public void runBootCommand() {
        base.runBootCommand();
    }
}
