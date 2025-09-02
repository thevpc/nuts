package net.thevpc.nuts.runtime.standalone.log;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.log.NLogSPI;
import net.thevpc.nuts.log.NLogs;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NCallable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;

@NComponentScope(NScopeType.WORKSPACE)
public class DefaultNLogs implements NLogs {

    private final DefaultNLogModel model;

    public DefaultNLogs() {
        this.model = NWorkspaceExt.of().getModel().logModel;
    }

    public void runWithMdc(Map<String, Object> values, Runnable runnable) {
        if (runnable != null) {
            Map<String, Object> old = new HashMap<>();
            if (values != null) {
                for (String s : values.keySet()) {
                    old.put(s, model.getMdc(s));
                }
            }
            try {
                runnable.run();
            } finally {
                if (values != null) {
                    for (Map.Entry<String, Object> e : old.entrySet()) {
                        model.setMdc(e.getKey(), e.getValue());
                    }
                }
            }
        }
    }

    public <T> T callWithMdc(Map<String, Object> values, NCallable<T> callable) {
        if (callable != null) {
            Map<String, Object> old = new HashMap<>();
            if (values != null) {
                for (String s : values.keySet()) {
                    old.put(s, model.getMdc(s));
                }
            }
            try {
                return callable.call();
            } finally {
                if (values != null) {
                    for (Map.Entry<String, Object> e : old.entrySet()) {
                        model.setMdc(e.getKey(), e.getValue());
                    }
                }
            }
        }
        return null;
    }

    @Override
    public NLogs setMdc(Map<String, Object> values) {
        model.setMdc(values);
        return this;
    }

    @Override
    public NLogs setMdc(String key, Object value) {
        model.setMdc(key, value);
        return this;
    }

    @Override
    public Object getMdc(String key) {
        return model.getMdc(key);
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
