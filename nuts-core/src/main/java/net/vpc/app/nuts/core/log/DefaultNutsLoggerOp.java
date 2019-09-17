package net.vpc.app.nuts.core.log;

import net.vpc.app.nuts.NutsLoggerOp;
import net.vpc.app.nuts.NutsTextFormatStyle;

import java.util.function.Supplier;
import java.util.logging.Level;

public class DefaultNutsLoggerOp implements NutsLoggerOp {
    public static final Object[] OBJECTS0 = new Object[0];
    private DefaultNutsLogger logger;
    private Level level = Level.FINE;
    private String verb;
    private String msg;
    private long time;
    private boolean formatted;
    private Supplier<String> msgSupplier;
    private Throwable error;
    private NutsTextFormatStyle style = NutsTextFormatStyle.POSITIONAL;
    private Object[] params = OBJECTS0;

    public DefaultNutsLoggerOp(DefaultNutsLogger logger) {
        this.logger = logger;
    }

    @Override
    public NutsLoggerOp formatted(boolean value) {
        this.formatted = value;
        return this;
    }

    @Override
    public NutsLoggerOp formatted() {
        this.formatted = true;
        return this;
    }

    @Override
    public NutsLoggerOp verb(String verb) {
        this.verb = verb;
        return this;
    }

    @Override
    public NutsLoggerOp level(Level level) {
        this.level = level == null ? Level.FINE : level;
        return this;
    }

    @Override
    public NutsLoggerOp error(Throwable throwable) {
        this.error = throwable;
        return null;
    }

    @Override
    public void log(String msg, Object... params) {
        this.msg = msg;
        this.params = params;
        run();
    }

    @Override
    public void log(Supplier<String> msgSupplier) {
        this.msgSupplier = msgSupplier;
        run();
    }

    @Override
    public NutsLoggerOp time(long time) {
        this.time = time;
        return this;
    }

    @Override
    public NutsLoggerOp style(NutsTextFormatStyle style) {
        this.style = style == null ? NutsTextFormatStyle.POSITIONAL : style;
        return this;
    }

    private void run() {
        if (logger.isLoggable(level)) {
            String m = msg;
            if (msgSupplier != null) {
                m = msgSupplier.get();
            }
            NutsLogRecord record = new NutsLogRecord(
                    logger.getWorkspace(),
                    logger.getSession(),
                    level,
                    verb,
                    m,
                    params, formatted, time, style
            );
            if (error != null) {
                record.setThrown(error);
            }
            logger.log(record);
        }
    }
}
