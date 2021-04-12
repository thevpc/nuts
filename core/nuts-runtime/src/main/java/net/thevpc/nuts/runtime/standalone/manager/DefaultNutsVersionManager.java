package net.thevpc.nuts.runtime.standalone.manager;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.DefaultVersionFormat;
import net.thevpc.nuts.runtime.core.parser.DefaultNutsVersionParser;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultNutsVersionManager implements NutsVersionManager {

    private NutsWorkspace workspace;

    private NutsSession session;

    public DefaultNutsVersionManager(NutsWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsVersionManager setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    protected void checkSession() {
        NutsWorkspaceUtils.checkSession(workspace, session);
    }

    @Override
    public NutsVersionParser parser() {
        checkSession();
        return new DefaultNutsVersionParser(getSession());
    }

    @Override
    public NutsVersionFormat formatter() {
        return new DefaultVersionFormat(getWorkspace()).setSession(getSession());
    }

    @Override
    public NutsVersionFormat formatter(NutsVersion version) {
        return formatter().setVersion(version);
    }

    @Override
    public NutsVersionFilterManager filter() {
        return getWorkspace().filters().version().setSession(getSession());
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

}
