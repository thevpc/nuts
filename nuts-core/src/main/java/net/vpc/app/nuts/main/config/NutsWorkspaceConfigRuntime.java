package net.vpc.app.nuts.main.config;

import net.vpc.app.nuts.NutsConfigItem;

public class NutsWorkspaceConfigRuntime extends NutsConfigItem {
    private static final long serialVersionUID = 2;

    /**
     * boot component Id in long format (as defined in
     * {@link net.vpc.app.nuts.NutsId#getLongName()})
     *
     * @see net.vpc.app.nuts.NutsId#getLongNameId()
     */
    private String id = null;

    /**
     * ';' separated list of component Ids in long format (as defined in
     * {@link net.vpc.app.nuts.NutsId#getLongName()}) that defines ALL dependencies needed (no
     * further dependency computation should be performed) to load and execute a
     * valid implementation of nuts-api. These components should be accessible
     * from {@link NutsWorkspaceConfigBoot#getBootRepositories()}
     *
     * @see net.vpc.app.nuts.NutsId#getLongNameId()
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
