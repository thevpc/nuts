package net.vpc.app.nuts.core.impl.def.config;

import java.io.Serializable;

public class NutsWorkspaceConfigApi implements Serializable {
    private static final long serialVersionUID = 2;

    private String configVersion = null;

    private String apiVersion = null;
    /**
     * boot component Id in long format (as defined in
     * {@link net.vpc.app.nuts.NutsId#getLongName()})
     *
     * @see net.vpc.app.nuts.NutsId#getLongNameId()
     */
    private String runtimeId = null;

    private String javaCommand = null;
    private String javaOptions = null;

    public String getApiVersion() {
        return apiVersion;
    }

    public NutsWorkspaceConfigApi setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    public String getRuntimeId() {
        return runtimeId;
    }

    public NutsWorkspaceConfigApi setRuntimeId(String runtimeId) {
        this.runtimeId = runtimeId;
        return this;
    }

    public String getConfigVersion() {
        return configVersion;
    }

    public NutsWorkspaceConfigApi setConfigVersion(String configVersion) {
        this.configVersion = configVersion;
        return this;
    }

    public String getJavaCommand() {
        return javaCommand;
    }

    public NutsWorkspaceConfigApi setJavaCommand(String javaCommand) {
        this.javaCommand = javaCommand;
        return this;
    }

    public String getJavaOptions() {
        return javaOptions;
    }

    public NutsWorkspaceConfigApi setJavaOptions(String javaOptions) {
        this.javaOptions = javaOptions;
        return this;
    }
}
