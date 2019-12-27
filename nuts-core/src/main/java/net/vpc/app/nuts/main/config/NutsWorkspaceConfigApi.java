package net.vpc.app.nuts.main.config;

import net.vpc.app.nuts.NutsConfigItem;

import java.io.Serializable;

public class NutsWorkspaceConfigApi extends NutsConfigItem {
    private static final long serialVersionUID = 3;

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
