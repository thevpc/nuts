package net.thevpc.nuts.lib.slf4j;

import net.thevpc.nuts.log.NLogSPI;
import net.thevpc.nuts.util.NMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;

public class Slf4JNLogSPI implements NLogSPI {
    private static final String FQCN;
    private final Logger logger;
    private final LocationAwareLogger locationAwareLogger;
    private static final Set<String> LOGGER_PACKAGES = new HashSet<>(Arrays.asList(
            Slf4JNLogSPI.class.getPackage().getName(),
            "org.slf4j"
    ));

    static {
        FQCN = Slf4JNLogSPI.class.getName();
    }

    public Slf4JNLogSPI(String loggerName) {
        this.logger = LoggerFactory.getLogger(loggerName);
        this.locationAwareLogger = (logger instanceof LocationAwareLogger)
                ? (LocationAwareLogger) logger : null;
    }

    public Slf4JNLogSPI(Class<?> loggerClass) {
        this.logger = LoggerFactory.getLogger(loggerClass);
        this.locationAwareLogger = (logger instanceof LocationAwareLogger)
                ? (LocationAwareLogger) logger : null;
    }

    @Override
    public boolean isLoggable(Level level) {
        org.slf4j.event.Level slf4jLevel = toSlf4jLevel(level);
        return logger.isEnabledForLevel(slf4jLevel);
    }


    public void log(Level level, Supplier<NMsg> msg) {
        if (level == null) {
            level = Level.INFO;
        }
        if (level.intValue() == Level.OFF.intValue()) {
            return;
        }
        org.slf4j.event.Level slf4jLevel = toSlf4jLevel(level);
        if (!logger.isEnabledForLevel(slf4jLevel)) {
            return;
        }
        Throwable throwable = null;
        String mm = "";
        if (msg != null) {
            NMsg msg1 = msg.get();
            if (msg1 != null) {
                mm = msg1.toString();
                throwable = msg1.getThrowable();
            }
        }
        log0(slf4jLevel, mm, throwable);
    }


    @Override
    public void log(NMsg message) {
        Level level = message == null ? Level.INFO : message.getNormalizedLevel();
        if (level == null) {
            level = Level.INFO;
        }
        if (level.intValue() == Level.OFF.intValue()) {
            return;
        }
        org.slf4j.event.Level slf4jLevel = toSlf4jLevel(level);
        if (!logger.isEnabledForLevel(slf4jLevel)) {
            return;
        }
        String msgText = (message != null) ? message.toString() : "";
        log0(slf4jLevel, msgText, message == null ? null : message.getThrowable());
    }

    private void log0(org.slf4j.event.Level slf4jLevel, String message, Throwable throwable) {
        if (locationAwareLogger != null) {
            locationAwareLogger.log(null, FQCN, slf4jLevel.toInt(), message, null, throwable);
        } else {
            StackTraceElement caller = findCaller(); // or findCallerCached(FQCN, "log0")
            String callerClass = caller != null ? caller.getClassName() : "unknown";
            String callerMethod = caller != null ? caller.getMethodName() : "unknown";
            message = NMsg.ofC("[%s][%s] %s", callerClass, callerMethod, message).toString();
            switch (slf4jLevel) {
                case ERROR: {
                    logger.error(message, throwable);
                    break;
                }
                case WARN: {
                    logger.warn(message, throwable);
                    break;
                }
                case INFO: {
                    logger.info(message, throwable);
                    break;
                }
                case DEBUG: {
                    logger.debug(message, throwable);
                    break;
                }
                case TRACE: {
                    logger.trace(message, throwable);
                    break;
                }
            }
        }
    }

    private StackTraceElement findCaller() {
        StackTraceElement[] stack = new Throwable().getStackTrace();
        for (StackTraceElement frame : stack) {
            String className = frame.getClassName();
            boolean skip = false;
            for (String pkg : LOGGER_PACKAGES) {
                if (className.startsWith(pkg)) {
                    skip = true;
                    break;
                }
            }
            if (!skip) {
                return frame;
            }
        }
        return null; // fallback
    }

    private static org.slf4j.event.Level toSlf4jLevel(Level julLevel) {
        if (julLevel == null) return org.slf4j.event.Level.TRACE;
        switch (julLevel.intValue()) {
            case 1000:
                return org.slf4j.event.Level.ERROR;
            case 900:
                return org.slf4j.event.Level.WARN;
            case 800:
                return org.slf4j.event.Level.INFO;
            case 700:
            case 500:
                return org.slf4j.event.Level.DEBUG;
            default:
                return org.slf4j.event.Level.TRACE;
        }
    }

}
