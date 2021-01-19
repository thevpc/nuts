package net.thevpc.nuts.runtime.bundles.mvn;

import java.util.logging.Level;
import java.util.logging.Logger;

public interface PomLogger {
    PomLogger DEFAULT = new PomLogger() {
        @Override
        public void log(Level level, String msg, Object... params) {
            Logger.getLogger(PomLogger.class.getName()).log(level, msg, params);
        }

        @Override
        public void log(Level level, String msg, Throwable throwable) {
            Logger.getLogger(PomLogger.class.getName()).log(level, msg, throwable);
        }
    };

    void log(Level level, String msg, Object... params);

    void log(Level level, String msg, Throwable throwable);
}
