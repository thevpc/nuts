package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.NConfigItem;
import net.thevpc.nuts.NId;

public class NWorkspaceConfigRuntime extends NConfigItem implements Cloneable{
    private static final long serialVersionUID = 2;

    /**
     * boot package Id in long format (as defined in
     * {@link NId#getLongName()})
     *
     * @see NId#getLongId()
     */
    private NId id = null;

    /**
     * ';' separated list of package Ids in long format (as defined in
     * {@link NId#getLongName()}) that defines ALL dependencies needed (no
     * further dependency computation should be performed) to load and execute a
     * valid implementation of nuts-api. These packages should be accessible
     * from {@link NWorkspaceConfigBoot#getBootRepositories()}
     *
     * @see NId#getLongId()
     */
    private String dependencies = null;

    public NId getId() {
        return id;
    }

    public void setId(NId id) {
        this.id = id;
    }

    public String getDependencies() {
        return dependencies;
    }

    public void setDependencies(String dependencies) {
        this.dependencies = dependencies;
    }

    public NWorkspaceConfigRuntime copy() {
        try {
            return (NWorkspaceConfigRuntime) clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
