package net.thevpc.nuts.runtime.standalone.log;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.log.NLogs;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;

@NComponentScope(NScopeType.WORKSPACE)
public class DefaultNLogs implements NLogs {

    private final DefaultNLogModel model;

    public DefaultNLogs() {
        this.model = NWorkspaceExt.of().getModel().logModel;
    }

    @Override
    public List<Handler> getHandlers() {
        return model.getHandlers();
    }

    @Override
    public NLogs removeHandler(Handler handler) {
        model.removeHandler(handler);
        return this;
    }

    @Override
    public NLogs addHandler(Handler handler) {
        model.addHandler(handler);
        return this;
    }

    @Override
    public Handler getTermHandler() {
        return model.getTermHandler();
    }

    @Override
    public Handler getFileHandler() {
        return model.getFileHandler();
    }

    @Override
    public NLog createLogger(String name) {
        return model.createLogger(name);
    }

    @Override
    public NLog createLogger(Class<?> clazz) {
        return model.createLogger(clazz);
    }

    @Override
    public Level getTermLevel() {
        return model.getTermLevel();
    }

    @Override
    public NLogs setTermLevel(Level level) {
        model.setTermLevel(level);
        return this;
    }

    @Override
    public Level getFileLevel() {
        return model.getFileLevel();
    }

    @Override
    public NLogs setFileLevel(Level level) {
        model.setFileLevel(level);
        return this;
    }

    public DefaultNLogModel getModel() {
        return model;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
