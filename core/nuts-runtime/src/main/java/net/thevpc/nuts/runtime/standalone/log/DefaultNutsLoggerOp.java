package net.thevpc.nuts.runtime.standalone.log;

import net.thevpc.nuts.NutsLoggerOp;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsTextFormatStyle;

import java.util.function.Supplier;
import java.util.logging.Level;

public class DefaultNutsLoggerOp implements NutsLoggerOp {
    public static final Object[] OBJECTS0 = new Object[0];
    private NutsSession session;
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

    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsLoggerOp session(NutsSession session) {
        this.session = session;
        return this;
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
    public NutsLoggerOp error(Throwable error) {
        this.error = error;
        return this;
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
            NutsSession s=session;
            if(s==null){
                s=logger.getSession();
            }
            NutsLogRecord record = new NutsLogRecord(
                    logger.getWorkspace(),
                    s,
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
