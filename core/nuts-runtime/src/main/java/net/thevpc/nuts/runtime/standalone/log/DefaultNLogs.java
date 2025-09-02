package net.thevpc.nuts.runtime.standalone.log;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.log.*;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NCallable;
import net.thevpc.nuts.util.NMsg;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Handler;
import java.util.logging.Level;

@NComponentScope(NScopeType.WORKSPACE)
public class DefaultNLogs implements NLogs {

    private final DefaultNLogModel model;
    private InheritableThreadLocal<NLogContext> logContext = new InheritableThreadLocal<>();

    public DefaultNLogs() {
        this.model = NWorkspaceExt.of().getModel().logModel;
    }

    @Override
    public NLogContext newContext() {
        return NLogContextImpl.BLANK;
    }

    @Override
    public NLogContext getContext() {
        return model.getContext();
    }

    @Override
    public void runWith(NLogContext context, Runnable runnable) {
        model.runWithContext(context, runnable);
    }

    @Override
    public <T> T callWithContext(NLogContext context, NCallable<T> callable) {
        return model.callWithContext(context, callable);
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
    public NLog getLogger(String name) {
        return model.getLogger(name);
    }

    @Override
    public NLog getNullLogger() {
        return model.getNullLogger();
    }

    @Override
    public NLog createCustomLogger(String name, NLogSPI spi) {
        return model.createCustomLogger(name, spi);
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
