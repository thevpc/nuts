package net.thevpc.nuts.lib.slf4j;

import net.thevpc.nuts.log.NLogSPI;
import net.thevpc.nuts.util.NMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

import java.util.function.Supplier;
import java.util.logging.Level;

public class Slf4JNLogSPI implements NLogSPI {
    private static final String FQCN; // Fully Qualified Class Name
    private final Logger logger;
    private final LocationAwareLogger locationAwareLogger;

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

    public String getName(){
        return logger.getName();
    }



//    @Override
    public void log(Level level, Supplier<NMsg> msg) {
        if (level == null) {
            level = Level.INFO;
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
                throwable=msg1.getThrowable();
            }
        }
        log0(slf4jLevel, mm, throwable);
    }


    @Override
    public void log(NMsg message) {
        Level level = message == null ? Level.INFO : message.getLevel();
        if (level == null) {
            level = Level.INFO;
        }
        org.slf4j.event.Level slf4jLevel = toSlf4jLevel(level);
        if (!logger.isEnabledForLevel(slf4jLevel)) {
            return;
        }
        String msgText = (message != null) ? message.toString() : "";
        log0(slf4jLevel, msgText,message==null?null:message.getThrowable());
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
                    if (throwable != null) {
                        logger.error(message, throwable);
                    } else {
                        logger.error(message);
                    }
                    break;
                }
                case WARN: {
                    if (throwable != null) {
                        logger.warn(message, throwable);
                    } else {
                        logger.warn(message);
                    }
                    break;
                }
                case INFO: {
                    if (throwable != null) {
                        logger.info(message, throwable);
                    } else {
                        logger.info(message);
                    }
                    break;
                }
                case DEBUG: {
                    if (throwable != null) {
                        logger.debug(message, throwable);
                    } else {
                        logger.debug(message);
                    }
                    break;
                }
            }
        }
    }

    private StackTraceElement findCaller() {
        StackTraceElement[] stack = new Throwable().getStackTrace();
        for (StackTraceElement frame : stack) {
            String className = frame.getClassName();
            if (!className.startsWith("com.cts.halbrisk.core.infra.log")) {
                // This is the real caller
                String methodName = frame.getMethodName();
                return frame;
            }
        }
        return null;
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
            case 400:
            case 300:
                return org.slf4j.event.Level.TRACE;
            default:
                // fallback for user-defined or unknown levels
                int val = julLevel.intValue();
                if (val >= Level.SEVERE.intValue()) return org.slf4j.event.Level.ERROR;
                if (val >= Level.WARNING.intValue()) return org.slf4j.event.Level.WARN;
                if (val >= Level.INFO.intValue()) return org.slf4j.event.Level.INFO;
                if (val >= Level.CONFIG.intValue()) return org.slf4j.event.Level.DEBUG;
                return org.slf4j.event.Level.TRACE;
        }
    }

}
