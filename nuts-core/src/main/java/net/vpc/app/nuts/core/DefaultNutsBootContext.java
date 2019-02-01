package net.vpc.app.nuts.core;

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

    public DefaultNutsBootContext(String workspace, NutsId bootAPI, NutsId bootRuntime,
                                  String bootRuntimeDependencies, String bootRepositories, String bootJavaCommand, String bootJavaOptions,
                                  String[] locations, NutsStoreLocationStrategy storeLocationStrategy, NutsStoreLocationLayout storeLocationLayout, NutsStoreLocationStrategy repositoryStoreLocationStrategy
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
        storeLocations = new String[NutsStoreFolder.values().length];
        for (int i = 0; i < storeLocations.length; i++) {
            storeLocations[i] = locations[i];
        }
    }

    public DefaultNutsBootContext(NutsBootConfig c) {
        this.workspace = c.getWorkspace();
        this.bootAPI = c.getApiVersion() == null ? null : CoreNutsUtils.parseNutsId(NutsConstants.NUTS_ID_BOOT_API + "#" + c.getApiVersion());
        this.bootRuntime = c.getRuntimeId() == null ? null : c.getRuntimeId().contains("#") ?
                CoreNutsUtils.parseNutsId(c.getRuntimeId()) :
                CoreNutsUtils.parseNutsId(NutsConstants.NUTS_ID_BOOT_RUNTIME + "#" + c.getRuntimeId());
        this.bootRuntimeDependencies = c.getRuntimeDependencies();
        this.bootRepositories = c.getRepositories();
        this.bootJavaCommand = c.getJavaCommand();
        this.bootJavaOptions = c.getJavaOptions();
        this.storeLocationStrategy = c.getStoreLocationStrategy();
        this.repositoryStoreLocationStrategy = c.getRepositoryStoreLocationStrategy();
        this.storeLocationLayout = c.getStoreLocationLayout();
        storeLocations = new String[NutsStoreFolder.values().length];
        for (int i = 0; i < storeLocations.length; i++) {
            switch (NutsStoreFolder.values()[i]) {
                case PROGRAMS: {
                    storeLocations[i] = c.getProgramsStoreLocation();
                    break;
                }
                case LIB: {
                    storeLocations[i] = c.getLibStoreLocation();
                    break;
                }
                case CACHE: {
                    storeLocations[i] = c.getCacheStoreLocation();
                    break;
                }
                case CONFIG: {
                    storeLocations[i] = c.getConfigStoreLocation();
                    break;
                }
                case LOGS: {
                    storeLocations[i] = c.getLogsStoreLocation();
                    break;
                }
                case TEMP: {
                    storeLocations[i] = c.getTempStoreLocation();
                    break;
                }
                case VAR: {
                    storeLocations[i] = c.getVarStoreLocation();
                    break;
                }
                default: {
                    throw new NutsIllegalArgumentException("Unsupported " + NutsStoreFolder.values()[i]);
                }
            }
        }
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
    public String getStoreLocation(NutsStoreFolder folderType) {
        return storeLocations[folderType.ordinal()];
    }

    @Override
    public NutsStoreLocationLayout getStoreLocationLayout() {
        return storeLocationLayout;
    }
}
