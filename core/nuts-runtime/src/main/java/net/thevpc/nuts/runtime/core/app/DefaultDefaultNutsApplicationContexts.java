package net.thevpc.nuts.runtime.core.app;

import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.spi.NutsApplicationContexts;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class DefaultDefaultNutsApplicationContexts implements NutsApplicationContexts {
    private final NutsSession session;

    public DefaultDefaultNutsApplicationContexts(NutsSession session) {
        this.session = session;
    }

    public NutsApplicationContext create(String[] args, long startTimeMillis, Class appClass, String storeId) {
//        if (session == null) {
//            checkSession(this.session);
//            session = this.session;
//        }
        return new DefaultNutsApplicationContext(session.getWorkspace(), session, args, appClass, storeId, startTimeMillis);
    }


    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
