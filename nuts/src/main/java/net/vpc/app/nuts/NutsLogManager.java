package net.vpc.app.nuts;

import java.util.logging.Handler;
import java.util.logging.Level;

public interface NutsLogManager {
    Handler[] getHandlers();

    void removeHandler(Handler handler);

    void addHandler(Handler handler);

    Handler getConsoleHandler();

    Handler getFileHandler();

    NutsLogger of(String name);

    NutsLogger of(Class clazz);

    Level getLogLevel();

    void setLogLevel(Level level, NutsUpdateOptions options);
}
