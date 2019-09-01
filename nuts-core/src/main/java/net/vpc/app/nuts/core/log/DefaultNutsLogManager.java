package net.vpc.app.nuts.core.log;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import sun.rmi.log.LogHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultNutsLogManager implements NutsLogManager {
    private NutsWorkspace ws;

    private Level logLevel;
    private Handler consoleHandler = new NutsLogConsoleHandler();
    private Handler fileHandler;
    private List<Handler> extraHandlers = new ArrayList<>();
    private static Handler[] EMPTY = new Handler[0];

    public DefaultNutsLogManager(NutsWorkspace ws) {
        this.ws = ws;
        try {
            fileHandler = NutsLogFileHandler.create(ws, ws.config().options().getLogConfig(), true);
        } catch (Exception ex) {
            Logger.getLogger(DefaultNutsLogManager.class.getName()).log(Level.FINE, "Unable to create file handler", ex);
        }
    }

    @Override
    public Handler[] getHandlers() {
        if (extraHandlers.isEmpty()) {
            return EMPTY;
        }
        return extraHandlers.toArray(EMPTY);
    }

    @Override
    public void removeHandler(Handler handler) {
        extraHandlers.remove(handler);
    }

    @Override
    public void addHandler(Handler handler) {
        if (handler != null) {
            extraHandlers.add(handler);
        }
    }

    @Override
    public Handler getConsoleHandler() {
        return consoleHandler;
    }

    @Override
    public Handler getFileHandler() {
        return fileHandler;
    }

    @Override
    public NutsLogger of(String name) {
        return new DefaultNutsLogger(ws, name);
    }

    @Override
    public NutsLogger of(Class clazz) {
        return new DefaultNutsLogger(ws, clazz);
    }

    @Override
    public Level getLogLevel() {
        if (logLevel != null) {
            return logLevel;
        }
        NutsLogConfig lc = ws.config().options().getLogConfig();
        if (lc != null) {
            Level ll = lc.getLogLevel();
            if (ll != null) {
                return ll;
            }
        }
        return Level.WARNING;
    }

    @Override
    public void setLogLevel(Level level, NutsUpdateOptions options) {
        this.logLevel = level;
        options = CoreNutsUtils.validate(options, ws);
        Logger rootLogger = Logger.getLogger("");
        if (level == null) {
            level = Level.WARNING;
        }
        rootLogger.setLevel(level);
        for (Handler handler : rootLogger.getHandlers()) {
            handler.setLevel(level);
        }
    }
}
