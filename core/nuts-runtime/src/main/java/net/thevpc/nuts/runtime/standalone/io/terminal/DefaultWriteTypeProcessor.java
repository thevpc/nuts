package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NLogger;
import net.thevpc.nuts.util.NLoggerVerb;
import net.thevpc.nuts.util.NUtils;

import java.util.function.Supplier;
import java.util.logging.Level;

public class DefaultWriteTypeProcessor {
    private NMsg askMessage;
    private NMsg logMessage;
    private Supplier<RuntimeException> error;
    private NLogger log;
    private NConfirmationMode writeType;
    private NSession session;

    public DefaultWriteTypeProcessor(NConfirmationMode writeType, NSession session) {
        this.writeType = writeType;
        this.session = session;
    }

    public static DefaultWriteTypeProcessor of(NConfirmationMode writeType, NSession session) {
        return new DefaultWriteTypeProcessor(writeType, session);
    }

    public DefaultWriteTypeProcessor ask(NMsg m) {
        NUtils.requireNonNull(m, "message", session);
        this.askMessage = m;
        return this;
    }

    public DefaultWriteTypeProcessor withLog(NLogger log, NMsg m) {
        NUtils.requireNonNull(log, "log", session);
        NUtils.requireNonNull(m, "message", session);
        this.log = log;
        this.logMessage = m;
        return this;
    }

    public DefaultWriteTypeProcessor onError(Supplier<RuntimeException> error) {
        NUtils.requireNonNull(error, "error handler", session);
        this.error = error;
        return this;
    }

    private NMsg getValidAskMessage() {
        NUtils.requireNonNull(askMessage, "message", session);
        return askMessage;
    }

    private NMsg getValidLogMessage() {
        NUtils.requireNonNull(logMessage, "log message", session);
        return logMessage;
    }

    private Supplier<RuntimeException> getValidError() {
        NUtils.requireNonNull(error, "error handler", session);
        return error;
    }

    private NLogger getValidLog() {
        NUtils.requireNonNull(log, "log", session);
        return log;
    }


    public boolean process() {
        switch (writeType) {
            case ERROR: {
                throw getValidError().get();
            }
            case ASK: {
                if (!session.getTerminal().ask()
                        .setSession(session)
                        .forBoolean(getValidAskMessage())
                        .setDefaultValue(false).getBooleanValue()) {
                    return false;
                }
                break;
            }
            case NO: {
                getValidLog().with().session(session).level(Level.FINE).verb(NLoggerVerb.WARNING)
                        .log(getValidLogMessage());
                return false;
            }
        }
        getValidLog().with().session(session).level(Level.FINE).verb(NLoggerVerb.WARNING)
                .log(getValidLogMessage());
        return true;
    }
}
