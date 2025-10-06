package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.core.NConfigItem;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NVersion;

public class NWorkspaceConfigApi extends NConfigItem implements Cloneable{
    private static final long serialVersionUID = 3;

    private NVersion apiVersion = null;
    /**
     * boot package Id in long format (as defined in
     * {@link NId#getLongName()})
     *
     * @see NId#getLongId()
     */
    private NId runtimeId = null;

    private String javaCommand = null;
    private String javaOptions = null;

    public NVersion getApiVersion() {
        return apiVersion;
    }

    public NWorkspaceConfigApi setApiVersion(NVersion apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    public NId getRuntimeId() {
        return runtimeId;
    }

    public NWorkspaceConfigApi setRuntimeId(NId runtimeId) {
        this.runtimeId = runtimeId;
        return this;
    }

    public String getJavaCommand() {
        return javaCommand;
    }

    public NWorkspaceConfigApi setJavaCommand(String javaCommand) {
        this.javaCommand = javaCommand;
        return this;
    }

    public String getJavaOptions() {
        return javaOptions;
    }

    public NWorkspaceConfigApi setJavaOptions(String javaOptions) {
        this.javaOptions = javaOptions;
        return this;
    }

    public NWorkspaceConfigApi copy() {
        try {
            return (NWorkspaceConfigApi) clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
