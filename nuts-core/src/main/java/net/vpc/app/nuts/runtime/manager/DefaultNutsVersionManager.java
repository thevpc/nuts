package net.vpc.app.nuts.runtime.manager;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.format.DefaultVersionFormat;
import net.vpc.app.nuts.runtime.parser.DefaultNutsVersionParser;

public class DefaultNutsVersionManager implements NutsVersionManager {
    private NutsWorkspace workspace;

    public DefaultNutsVersionManager(NutsWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public NutsVersionParser parser() {
        return new DefaultNutsVersionParser(workspace);
    }

    @Override
    public NutsVersionFormat formatter() {
        return new DefaultVersionFormat(getWorkspace());
    }

    @Override
    public NutsVersionFormat formatter(NutsVersion version) {
        return formatter().setVersion(version);
    }

    @Override
    public NutsVersionFilterManager filter() {
        return getWorkspace().filters().version();
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

}
