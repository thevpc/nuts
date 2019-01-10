package net.vpc.app.nuts;

public final class NutsBootConfig implements Cloneable {
    private String home = null;
    private String apiVersion = null;
    private String runtimeId = null;
    private String runtimeDependencies = null;
    private String repositories = null;
    private String javaCommand = null;
    private String javaOptions = null;
    private String workspace = null;
    private String programsStoreLocation = null;
    private String libStoreLocation = null;
    private String configStoreLocation = null;
    private String varStoreLocation = null;
    private String logsStoreLocation = null;
    private String tempStoreLocation = null;
    private String cacheStoreLocation = null;
    /**
     * valid values are "", "bundle"
     */
    private StoreLocationStrategy storeLocationStrategy = null;
    private StoreLocationLayout storeLocationLayout = null;

    public NutsBootConfig() {
    }

    public NutsBootConfig(NutsWorkspaceOptions options) {
        if (options != null) {
            this.setHome(options.getHome());
            this.setWorkspace(options.getWorkspace());
            this.setStoreLocationStrategy(options.getStoreLocationStrategy());
            this.setStoreLocationLayout(options.getStoreLocationLayout());
            this.setProgramsStoreLocation(options.getProgramsStoreLocation());
            this.setLibStoreLocation(options.getLibStoreLocation());
            this.setLogsStoreLocation(options.getLogsStoreLocation());
            this.setVarStoreLocation(options.getVarStoreLocation());
            this.setCacheStoreLocation(options.getCacheStoreLocation());
            this.setTempStoreLocation(options.getTempStoreLocation());
            this.setConfigStoreLocation(options.getConfigStoreLocation());
        }
    }

    public NutsBootConfig(NutsBootContext context) {
        if (context != null) {
            this.apiVersion = context.getApiId().getVersion().getValue();
            this.runtimeId = context.getRuntimeId().toString();
            this.runtimeDependencies = context.getRuntimeDependencies();
            this.repositories = context.getRepositories();
            this.javaCommand = context.getJavaCommand();
            this.javaOptions = context.getJavaOptions();
            this.cacheStoreLocation = context.getStoreLocation(StoreFolder.CACHE);
            this.programsStoreLocation = context.getStoreLocation(StoreFolder.PROGRAMS);
            this.libStoreLocation = context.getStoreLocation(StoreFolder.LIB);
            this.configStoreLocation = context.getStoreLocation(StoreFolder.CONFIG);
            this.varStoreLocation = context.getStoreLocation(StoreFolder.VAR);
            this.logsStoreLocation = context.getStoreLocation(StoreFolder.LOGS);
            this.tempStoreLocation = context.getStoreLocation(StoreFolder.TEMP);
            this.storeLocationStrategy = context.getStoreLocationStrategy();
            this.storeLocationLayout = context.getStoreLocationLayout();
        }
    }

    public NutsBootConfig(NutsBootConfig other) {
        if (other != null) {
            this.apiVersion = other.getApiVersion();
            this.runtimeId = other.getRuntimeId();
            this.runtimeDependencies = other.getRuntimeDependencies();
            this.repositories = other.getRepositories();
            this.javaCommand = other.getJavaCommand();
            this.javaOptions = other.getJavaOptions();
            this.cacheStoreLocation = other.getCacheStoreLocation();
            this.programsStoreLocation = other.getProgramsStoreLocation();
            this.libStoreLocation = other.getLibStoreLocation();
            this.configStoreLocation = other.getConfigStoreLocation();
            this.varStoreLocation = other.getVarStoreLocation();
            this.logsStoreLocation = other.getLogsStoreLocation();
            this.tempStoreLocation = other.getTempStoreLocation();
            this.storeLocationStrategy = other.getStoreLocationStrategy();
            this.storeLocationLayout = other.getStoreLocationLayout();
        }
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public NutsBootConfig setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    public String getRuntimeId() {
        return runtimeId;
    }

    public NutsBootConfig setRuntimeId(String runtimeId) {
        this.runtimeId = runtimeId;
        return this;
    }

    public String getRuntimeDependencies() {
        return runtimeDependencies;
    }

    public NutsBootConfig setRuntimeDependencies(String runtimeDependencies) {
        this.runtimeDependencies = runtimeDependencies;
        return this;
    }

    public String getJavaCommand() {
        return javaCommand;
    }

    public NutsBootConfig setJavaCommand(String javaCommand) {
        this.javaCommand = javaCommand;
        return this;
    }

    public String getJavaOptions() {
        return javaOptions;
    }

    public NutsBootConfig setJavaOptions(String javaOptions) {
        this.javaOptions = javaOptions;
        return this;
    }

    public String getRepositories() {
        return repositories;
    }

    public NutsBootConfig setRepositories(String repositories) {
        this.repositories = repositories;
        return this;
    }

    public NutsBootConfig copy() {
        try {
            return (NutsBootConfig) clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException("Unexpected Behaviour");
        }
    }

    public String getProgramsStoreLocation() {
        return programsStoreLocation;
    }

    public NutsBootConfig setProgramsStoreLocation(String programsStoreLocation) {
        this.programsStoreLocation = programsStoreLocation;
        return this;
    }

    public String getConfigStoreLocation() {
        return configStoreLocation;
    }

    public NutsBootConfig setConfigStoreLocation(String configStoreLocation) {
        this.configStoreLocation = configStoreLocation;
        return this;
    }

    public String getVarStoreLocation() {
        return varStoreLocation;
    }

    public NutsBootConfig setVarStoreLocation(String varStoreLocation) {
        this.varStoreLocation = varStoreLocation;
        return this;
    }

    public String getLogsStoreLocation() {
        return logsStoreLocation;
    }

    public NutsBootConfig setLogsStoreLocation(String logsStoreLocation) {
        this.logsStoreLocation = logsStoreLocation;
        return this;
    }

    public String getTempStoreLocation() {
        return tempStoreLocation;
    }

    public NutsBootConfig setTempStoreLocation(String tempStoreLocation) {
        this.tempStoreLocation = tempStoreLocation;
        return this;
    }

    public String getCacheStoreLocation() {
        return cacheStoreLocation;
    }

    public NutsBootConfig setCacheStoreLocation(String cacheStoreLocation) {
        this.cacheStoreLocation = cacheStoreLocation;
        return this;
    }

    public StoreLocationStrategy getStoreLocationStrategy() {
        return storeLocationStrategy;
    }

    public NutsBootConfig setStoreLocationStrategy(StoreLocationStrategy storeLocationStrategy) {
        this.storeLocationStrategy = storeLocationStrategy;
        return this;
    }

    public String getWorkspace() {
        return workspace;
    }

    public NutsBootConfig setWorkspace(String workspace) {
        this.workspace = workspace;
        return this;
    }

    public String getHome() {
        return home;
    }

    public NutsBootConfig setHome(String home) {
        this.home = home;
        return this;
    }

    public StoreLocationLayout getStoreLocationLayout() {
        return storeLocationLayout;
    }

    public NutsBootConfig setStoreLocationLayout(StoreLocationLayout storeLocationLayout) {
        this.storeLocationLayout = storeLocationLayout;
        return this;
    }

    public String getLibStoreLocation() {
        return libStoreLocation;
    }

    public NutsBootConfig setLibStoreLocation(String libStoreLocation) {
        this.libStoreLocation = libStoreLocation;
        return this;
    }
}
