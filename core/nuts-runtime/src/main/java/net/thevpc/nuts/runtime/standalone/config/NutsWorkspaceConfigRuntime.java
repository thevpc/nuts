package net.thevpc.nuts.runtime.standalone.config;

import net.thevpc.nuts.NutsConfigItem;
import net.thevpc.nuts.NutsId;

public class NutsWorkspaceConfigRuntime extends NutsConfigItem {
    private static final long serialVersionUID = 2;

    /**
     * boot package Id in long format (as defined in
     * {@link NutsId#getLongName()})
     *
     * @see NutsId#getLongId()
     */
    private String id = null;

    /**
     * ';' separated list of package Ids in long format (as defined in
     * {@link NutsId#getLongName()}) that defines ALL dependencies needed (no
     * further dependency computation should be performed) to load and execute a
     * valid implementation of nuts-api. These packages should be accessible
     * from {@link NutsWorkspaceConfigBoot#getBootRepositories()}
     *
     * @see NutsId#getLongId()
     */
    private String dependencies = null;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDependencies() {
        return dependencies;
    }

    public void setDependencies(String dependencies) {
        this.dependencies = dependencies;
    }
}
