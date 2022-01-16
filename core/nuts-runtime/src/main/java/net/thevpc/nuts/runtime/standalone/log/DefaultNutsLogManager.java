package net.thevpc.nuts.runtime.standalone.log;

import net.thevpc.nuts.NutsLogger;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.spi.NutsLogManager;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.util.logging.Handler;
import java.util.logging.Level;

@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class DefaultNutsLogManager implements NutsLogManager {

    private final DefaultNutsLogModel model;
    private final NutsWorkspace ws;

    public DefaultNutsLogManager(NutsWorkspace ws) {
        this.ws = ws;
        this.model = ((NutsWorkspaceExt) ws).getModel().logModel;
    }

    @Override
    public Handler[] getHandlers(NutsSession session) {
        checkSession(session);
        return model.getHandlers();
    }

    @Override
    public NutsLogManager removeHandler(Handler handler, NutsSession session) {
        checkSession(session);
        model.removeHandler(handler);
        return this;
    }

    @Override
    public NutsLogManager addHandler(Handler handler, NutsSession session) {
        checkSession(session);
        model.addHandler(handler);
        return this;
    }

    @Override
    public Handler getTermHandler(NutsSession session) {
        checkSession(session);
        return model.getTermHandler();
    }

    @Override
    public Handler getFileHandler(NutsSession session) {
        checkSession(session);
        return model.getFileHandler();
    }

    @Override
    public NutsLogger createLogger(String name, NutsSession session) {
        checkSession(session);
        return model.createLogger(name, session);
    }

    @Override
    public NutsLogger createLogger(Class clazz, NutsSession session) {
        checkSession(session);
        return model.createLogger(clazz, session);
    }

    @Override
    public Level getTermLevel(NutsSession session) {
        checkSession(session);
        return model.getTermLevel();
    }

    @Override
    public NutsLogManager setTermLevel(Level level, NutsSession session) {
        checkSession(session);
        model.setTermLevel(level, session);
        return this;
    }

    @Override
    public Level getFileLevel(NutsSession session) {
        checkSession(session);
        return model.getFileLevel();
    }

    @Override
    public NutsLogManager setFileLevel(Level level, NutsSession session) {
        checkSession(session);
        model.setFileLevel(level, session);
        return this;
    }

    private void checkSession(NutsSession session) {
        NutsSessionUtils.checkSession(model.getWorkspace(), session);
    }

    public DefaultNutsLogModel getModel() {
        return model;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
