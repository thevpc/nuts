package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.NutsConfigItem;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsVersion;

public class NutsWorkspaceConfigApi extends NutsConfigItem {
    private static final long serialVersionUID = 3;

    private NutsVersion apiVersion = null;
    /**
     * boot package Id in long format (as defined in
     * {@link NutsId#getLongName()})
     *
     * @see NutsId#getLongId()
     */
    private NutsId runtimeId = null;

    private String javaCommand = null;
    private String javaOptions = null;

    public NutsVersion getApiVersion() {
        return apiVersion;
    }

    public NutsWorkspaceConfigApi setApiVersion(NutsVersion apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    public NutsId getRuntimeId() {
        return runtimeId;
    }

    public NutsWorkspaceConfigApi setRuntimeId(NutsId runtimeId) {
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
