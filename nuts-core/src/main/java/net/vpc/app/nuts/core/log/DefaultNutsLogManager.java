package net.vpc.app.nuts.core.log;

import net.vpc.app.nuts.NutsLogManager;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsLogger;

public class DefaultNutsLogManager implements NutsLogManager {
    private NutsWorkspace ws;

    public DefaultNutsLogManager(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsLogger of(String name) {
        return new DefaultNutsLogger(ws,name);
    }

    @Override
    public NutsLogger of(Class clazz) {
        return new DefaultNutsLogger(ws,clazz);
    }
}
