package net.vpc.app.nuts;

import java.util.logging.Handler;
import java.util.logging.Level;

public interface NutsLogManager {
    Handler[] getHandlers();

    void removeHandler(Handler handler);

    void addHandler(Handler handler);

    Handler getTermHandler();

    Handler getFileHandler();

    NutsLogger of(String name);

    NutsLogger of(Class clazz);

    Level getTermLevel();

    void setTermLevel(Level level, NutsUpdateOptions options);

    Level getFileLevel();

    void setFileLevel(Level level, NutsUpdateOptions options);
}
