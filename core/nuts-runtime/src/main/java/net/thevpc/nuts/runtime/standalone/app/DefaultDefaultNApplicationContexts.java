package net.thevpc.nuts.runtime.standalone.app;

import net.thevpc.nuts.NApplicationContext;
import net.thevpc.nuts.spi.NApplicationContexts;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NComponentScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NClock;

import java.util.ArrayList;
import java.util.Arrays;

@NComponentScope(NComponentScopeType.WORKSPACE)
public class DefaultDefaultNApplicationContexts implements NApplicationContexts {
    private final NSession session;

    public DefaultDefaultNApplicationContexts(NSession session) {
        this.session = session;
    }

    public NApplicationContext create(String[] args, NClock startTime, Class appClass, String storeId) {
//        if (session == null) {
//            checkSession(this.session);
//            session = this.session;
//        }
        return new DefaultNApplicationContext(session.getWorkspace(), session, new ArrayList<>(Arrays.asList(args)), appClass, storeId, startTime);
    }


    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
