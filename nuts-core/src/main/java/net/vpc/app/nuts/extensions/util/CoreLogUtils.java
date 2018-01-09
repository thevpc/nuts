package net.vpc.app.nuts.extensions.util;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CoreLogUtils {
    public static void setLevel(Level level) {
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
