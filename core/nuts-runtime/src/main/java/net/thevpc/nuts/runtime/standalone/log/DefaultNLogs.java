package net.thevpc.nuts.runtime.standalone.log;

import net.thevpc.nuts.spi.NLogs;
import net.thevpc.nuts.util.NLog;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NComponentScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;

@NComponentScope(NComponentScopeType.WORKSPACE)
public class DefaultNLogs implements NLogs {

    private final DefaultNLogModel model;
    private final NSession session;

    public DefaultNLogs(NSession session) {
        this.session = session;
        this.model = ((NWorkspaceExt) (session.getWorkspace())).getModel().logModel;
    }

    @Override
    public List<Handler> getHandlers(NSession session) {
        checkSession(session);
        return model.getHandlers();
    }

    @Override
    public NLogs removeHandler(Handler handler, NSession session) {
        checkSession(session);
        model.removeHandler(handler);
        return this;
    }

    @Override
    public NLogs addHandler(Handler handler, NSession session) {
        checkSession(session);
        model.addHandler(handler);
        return this;
    }

    @Override
    public Handler getTermHandler(NSession session) {
        checkSession(session);
        return model.getTermHandler();
    }

    @Override
    public Handler getFileHandler(NSession session) {
        checkSession(session);
        return model.getFileHandler();
    }

    @Override
    public NLog createLogger(String name, NSession session) {
        checkSession(session);
        return model.createLogger(name, session);
    }

    @Override
    public NLog createLogger(Class clazz, NSession session) {
        checkSession(session);
        return model.createLogger(clazz, session);
    }

    @Override
    public Level getTermLevel(NSession session) {
        checkSession(session);
        return model.getTermLevel();
    }

    @Override
    public NLogs setTermLevel(Level level, NSession session) {
        checkSession(session);
        model.setTermLevel(level, session);
        return this;
    }

    @Override
    public Level getFileLevel(NSession session) {
        checkSession(session);
        return model.getFileLevel();
    }

    @Override
    public NLogs setFileLevel(Level level, NSession session) {
        checkSession(session);
        model.setFileLevel(level, session);
        return this;
    }

    private void checkSession(NSession session) {
        NSessionUtils.checkSession(model.getWorkspace(), session);
    }

    public DefaultNLogModel getModel() {
        return model;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
