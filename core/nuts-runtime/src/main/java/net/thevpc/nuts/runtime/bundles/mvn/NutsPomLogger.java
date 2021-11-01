package net.thevpc.nuts.runtime.bundles.mvn;

import net.thevpc.nuts.*;

import java.util.logging.Level;

public class NutsPomLogger implements PomLogger {

    private final NutsSession session;
    NutsLogger LOG;

    public NutsPomLogger(NutsSession session) {
        this.session = session;
        LOG = NutsLogger.of(PomIdResolver.class,session);
    }

    @Override
    public void log(Level level, String msg, Object... params) {
        LOG.with().session(session)
                .level(Level.FINE)
                .verb(NutsLogVerb.FAIL)
                .log(NutsMessage.jstyle(msg, params));
    }

    @Override
    public void log(Level level, String msg, Throwable throwable) {
        LOG.with().session(session)
                .level(Level.FINE)
                .verb(NutsLogVerb.FAIL)
                .log(NutsMessage.jstyle("{0}", msg));
    }
}
