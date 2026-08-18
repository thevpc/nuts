package net.thevpc.nuts.runtime.standalone.log;

import net.thevpc.nuts.internal.rpi.NLogRPI;
import net.thevpc.nuts.log.*;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NLogSPI;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.concurrent.NCallable;

import java.util.logging.Level;
import java.util.logging.Logger;

@NComponentScope(NScopeType.WORKSPACE)
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNLogRPI implements NLogRPI {

    private final DefaultNLogModel model;

    public DefaultNLogRPI() {
        this.model = NWorkspaceExt.of().getModel().logModel;
    }

    @Override
    public NLogScope createScope() {
        return NLogScopeImpl.BLANK;
    }

    @Override
    public NLogScope currentScope() {
        return model.getContext();
    }

    @Override
    public void runInScope(NLogScope context, Runnable runnable) {
        model.runWithContext(context, runnable);
    }

    @Override
    public <T> T callInScope(NLogScope context, NCallable<T> callable) {
        return model.callWithContext(context, callable);
    }

    @Override
    public NLog getLogger(String name) {
        return model.getLogger(name);
    }

    @Override
    public NLog getLogger(Logger logger) {
        return model.getLogger(logger);
    }

    @Override
    public NLog nullLogger() {
        return model.getNullLogger();
    }

    @Override
    public NLog createCustomLogger(String name, NLogSPI spi) {
        return model.createCustomLogger(name, spi);
    }

    @Override
    public Level termLevel() {
        return model.getTermLevel();
    }

    @Override
    public NLogRPI termLevel(Level level) {
        model.setTermLevel(level);
        return this;
    }

    @Override
    public Level fileLevel() {
        return model.getFileLevel();
    }

    @Override
    public NLogRPI fileLevel(Level level) {
        model.setFileLevel(level);
        return this;
    }

    public DefaultNLogModel getModel() {
        return model;
    }

}
