package net.vpc.app.nuts.core;

import java.util.Arrays;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

public final class DefaultNutsBootContext implements NutsBootContext {

    private final String workspace;
    private final NutsId bootAPI;
    private final NutsId bootRuntime;
    private final String bootRuntimeDependencies;
    private final String bootRepositories;
    private final String bootJavaCommand;
    private final String bootJavaOptions;
    private final NutsStoreLocationStrategy storeLocationStrategy;
    private final NutsStoreLocationStrategy repositoryStoreLocationStrategy;
    private final NutsStoreLocationLayout storeLocationLayout;
    private final String[] storeLocations;
    private final String[] homeLocations;
    private final boolean global;

    public DefaultNutsBootContext(String workspace, NutsId bootAPI, NutsId bootRuntime,
            String bootRuntimeDependencies, String bootRepositories, String bootJavaCommand, String bootJavaOptions,
            String[] locations, String[] homeLocations, NutsStoreLocationStrategy storeLocationStrategy,
            NutsStoreLocationLayout storeLocationLayout, NutsStoreLocationStrategy repositoryStoreLocationStrategy,
            boolean global
    ) {
        this.workspace = workspace;
        this.bootAPI = bootAPI;
        this.bootRuntime = bootRuntime;
        this.bootRuntimeDependencies = bootRuntimeDependencies;
        this.bootRepositories = bootRepositories;
        this.bootJavaCommand = bootJavaCommand;
        this.bootJavaOptions = bootJavaOptions;
        this.storeLocationStrategy = storeLocationStrategy;
        this.storeLocationLayout = storeLocationLayout;
        this.repositoryStoreLocationStrategy = repositoryStoreLocationStrategy;
        if (locations.length != NutsStoreLocation.values().length) {
            throw new IllegalArgumentException("Invalid locations count");
        }
        storeLocations = new String[NutsStoreLocation.values().length];
        for (int i = 0; i < storeLocations.length; i++) {
            this.storeLocations[i] = locations[i];
        }
        if (homeLocations.length != NutsStoreLocation.values().length * 2) {
            throw new IllegalArgumentException("Invalid home locations count");
        }
        this.homeLocations = new String[NutsStoreLocation.values().length * 2];
        for (int i = 0; i < homeLocations.length; i++) {
            this.homeLocations[i] = homeLocations[i];
        }
        this.global = global;
    }

    public DefaultNutsBootContext(NutsBootConfig c) {
        this.workspace = c.getWorkspace();
        this.bootAPI = c.getApiVersion() == null ? null : CoreNutsUtils.parseNutsId(NutsConstants.Ids.NUTS_API + "#" + c.getApiVersion());
        this.bootRuntime = c.getRuntimeId() == null ? null : c.getRuntimeId().contains("#")
                ? CoreNutsUtils.parseNutsId(c.getRuntimeId())
                : CoreNutsUtils.parseNutsId(NutsConstants.Ids.NUTS_RUNTIME + "#" + c.getRuntimeId());
        this.bootRuntimeDependencies = c.getRuntimeDependencies();
        this.bootRepositories = c.getRepositories();
        this.bootJavaCommand = c.getJavaCommand();
        this.bootJavaOptions = c.getJavaOptions();
        this.storeLocationStrategy = c.getStoreLocationStrategy();
        this.repositoryStoreLocationStrategy = c.getRepositoryStoreLocationStrategy();
        this.storeLocationLayout = c.getStoreLocationLayout();
        this.storeLocations = c.getStoreLocations();
        this.homeLocations = c.getHomeLocations();
        this.global = c.isGlobal();
    }

    @Override
    public boolean isGlobal() {
        return this.global;
    }

    @Override
    public NutsId getApiId() {
        return bootAPI;
    }

    @Override
    public NutsId getRuntimeId() {
        return bootRuntime;
    }

    @Override
    public String getRuntimeDependencies() {
        return bootRuntimeDependencies;
    }

    @Override
    public String getRepositories() {
        return bootRepositories;
    }

    @Override
    public String getJavaCommand() {
        return bootJavaCommand;
    }

    @Override
    public String getJavaOptions() {
        return bootJavaOptions;
    }

    @Override
    public String getWorkspace() {
        return workspace;
    }

    @Override
    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        return storeLocationStrategy;
    }

    @Override
    public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy() {
        return repositoryStoreLocationStrategy;
    }

    @Override
    public String getStoreLocation(NutsStoreLocation folderType) {
        return storeLocations[folderType.ordinal()];
    }

    @Override
    public String[] getStoreLocations() {
        return Arrays.copyOf(storeLocations, storeLocations.length);
    }

    @Override
    public String[] getHomeLocations() {
        return Arrays.copyOf(homeLocations, homeLocations.length);
    }

    @Override
    public String getHomeLocation(NutsStoreLocationLayout layout, NutsStoreLocation folderType) {
        return this.homeLocations[layout.ordinal() * NutsStoreLocation.values().length + folderType.ordinal()];
    }

    @Override
    public NutsStoreLocationLayout getStoreLocationLayout() {
        return storeLocationLayout;
    }
}