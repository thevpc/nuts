package net.thevpc.nuts.runtime.standalone.app;

import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.spi.NutsApplicationContexts;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.util.NutsClock;

import java.util.ArrayList;
import java.util.Arrays;

@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class DefaultDefaultNutsApplicationContexts implements NutsApplicationContexts {
    private final NutsSession session;

    public DefaultDefaultNutsApplicationContexts(NutsSession session) {
        this.session = session;
    }

    public NutsApplicationContext create(String[] args, NutsClock startTime, Class appClass, String storeId) {
//        if (session == null) {
//            checkSession(this.session);
//            session = this.session;
//        }
        return new DefaultNutsApplicationContext(session.getWorkspace(), session, new ArrayList<>(Arrays.asList(args)), appClass, storeId, startTime);
    }


    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
