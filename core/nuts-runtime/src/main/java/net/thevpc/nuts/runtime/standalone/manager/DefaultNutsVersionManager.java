package net.thevpc.nuts.runtime.standalone.manager;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.DefaultVersionFormat;
import net.thevpc.nuts.runtime.core.parser.DefaultNutsVersionParser;

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
