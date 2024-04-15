package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NAsk;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.util.NMsg;

import java.util.function.Supplier;
import java.util.logging.Level;

public class DefaultWriteTypeProcessor {
    private NMsg askMessage;
    private NMsg logMessage;
    private Supplier<RuntimeException> error;
    private NLog log;
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
        NAssert.requireNonNull(m, "message", session);
        this.askMessage = m;
        return this;
    }

    public DefaultWriteTypeProcessor withLog(NLog log, NMsg m) {
        NAssert.requireNonNull(log, "log", session);
        NAssert.requireNonNull(m, "message", session);
        this.log = log;
        this.logMessage = m;
        return this;
    }

    public DefaultWriteTypeProcessor onError(Supplier<RuntimeException> error) {
        NAssert.requireNonNull(error, "error handler", session);
        this.error = error;
        return this;
    }

    private NMsg getValidAskMessage() {
        NAssert.requireNonNull(askMessage, "message", session);
        return askMessage;
    }

    private NMsg getValidLogMessage() {
        NAssert.requireNonNull(logMessage, "log message", session);
        return logMessage;
    }

    private Supplier<RuntimeException> getValidError() {
        NAssert.requireNonNull(error, "error handler", session);
        return error;
    }

    private NLog getValidLog() {
        NAssert.requireNonNull(log, "log", session);
        return log;
    }


    public boolean process() {
        switch (writeType) {
            case ERROR: {
                throw getValidError().get();
            }
            case ASK: {
                if (!NAsk.of(session)
                        .setSession(session)
                        .forBoolean(getValidAskMessage())
                        .setDefaultValue(false).getBooleanValue()) {
                    return false;
                }
                break;
            }
            case NO: {
                getValidLog().with().session(session).level(Level.FINE).verb(NLogVerb.WARNING)
                        .log(getValidLogMessage());
                return false;
            }
        }
        getValidLog().with().session(session).level(Level.FINE).verb(NLogVerb.WARNING)
                .log(getValidLogMessage());
        return true;
    }
}
