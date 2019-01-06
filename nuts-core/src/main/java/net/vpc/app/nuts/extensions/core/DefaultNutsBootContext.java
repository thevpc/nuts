package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.NutsBootConfig;
import net.vpc.app.nuts.NutsBootContext;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;

public final class DefaultNutsBootContext implements NutsBootContext {
    private final NutsId bootAPI;
    private final NutsId bootRuntime;
    private final String bootRuntimeDependencies;
    private final String bootRepositories;
    private final String bootJavaCommand;
    private final String bootJavaOptions;

    public DefaultNutsBootContext(NutsId bootAPI, NutsId bootRuntime, String bootRuntimeDependencies, String bootRepositories, String bootJavaCommand, String bootJavaOptions) {
        this.bootAPI = bootAPI;
        this.bootRuntime = bootRuntime;
        this.bootRuntimeDependencies = bootRuntimeDependencies;
        this.bootRepositories = bootRepositories;
        this.bootJavaCommand = bootJavaCommand;
        this.bootJavaOptions = bootJavaOptions;
    }

    public DefaultNutsBootContext(NutsBootConfig c) {
        this.bootAPI = c.getApiVersion()==null ? null: CoreNutsUtils.parseNutsId(NutsConstants.NUTS_ID_BOOT_API+"#"+c.getApiVersion());
        this.bootRuntime = c.getRuntimeId()==null ? null: c.getRuntimeId().contains("#")?
                CoreNutsUtils.parseNutsId(c.getRuntimeId()):
                CoreNutsUtils.parseNutsId(NutsConstants.NUTS_ID_BOOT_RUNTIME+"#"+c.getRuntimeId());
        this.bootRuntimeDependencies = c.getRuntimeDependencies();
        this.bootRepositories = c.getRepositories();
        this.bootJavaCommand = c.getJavaCommand();
        this.bootJavaOptions = c.getJavaOptions();
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
}
