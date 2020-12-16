package net.thevpc.nuts.runtime.standalone.main.config;

import net.thevpc.nuts.NutsConfigItem;
import net.thevpc.nuts.NutsId;

public class NutsWorkspaceConfigApi extends NutsConfigItem {
    private static final long serialVersionUID = 3;

    private String apiVersion = null;
    /**
     * boot component Id in long format (as defined in
     * {@link NutsId#getLongName()})
     *
     * @see NutsId#getLongNameId()
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
