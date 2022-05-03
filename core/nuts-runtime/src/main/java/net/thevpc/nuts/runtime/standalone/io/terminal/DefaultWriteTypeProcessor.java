package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NutsLogger;
import net.thevpc.nuts.util.NutsLoggerVerb;
import net.thevpc.nuts.util.NutsUtils;

import java.util.function.Supplier;
import java.util.logging.Level;

public class DefaultWriteTypeProcessor {
    private NutsMessage askMessage;
    private NutsMessage logMessage;
    private Supplier<RuntimeException> error;
    private NutsLogger log;
    private NutsConfirmationMode writeType;
    private NutsSession session;

    public DefaultWriteTypeProcessor(NutsConfirmationMode writeType, NutsSession session) {
        this.writeType = writeType;
        this.session = session;
    }

    public static DefaultWriteTypeProcessor of(NutsConfirmationMode writeType, NutsSession session) {
        return new DefaultWriteTypeProcessor(writeType, session);
    }

    public DefaultWriteTypeProcessor ask(NutsMessage m) {
        NutsUtils.requireNonNull(m, session, "message");
        this.askMessage = m;
        return this;
    }

    public DefaultWriteTypeProcessor withLog(NutsLogger log, NutsMessage m) {
        NutsUtils.requireNonNull(log, session, "log");
        NutsUtils.requireNonNull(m, session, "message");
        this.log = log;
        this.logMessage = m;
        return this;
    }

    public DefaultWriteTypeProcessor onError(Supplier<RuntimeException> error) {
        NutsUtils.requireNonNull(error, session, "error handler");
        this.error = error;
        return this;
    }

    private NutsMessage getValidAskMessage() {
        NutsUtils.requireNonNull(askMessage, session, "message");
        return askMessage;
    }

    private NutsMessage getValidLogMessage() {
        NutsUtils.requireNonNull(logMessage, session, "log message");
        return logMessage;
    }

    private Supplier<RuntimeException> getValidError() {
        NutsUtils.requireNonNull(error, session, "error handler");
        return error;
    }

    private NutsLogger getValidLog() {
        NutsUtils.requireNonNull(log, session, "log");
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
                getValidLog().with().session(session).level(Level.FINE).verb(NutsLoggerVerb.WARNING)
                        .log(getValidLogMessage());
                return false;
            }
        }
        getValidLog().with().session(session).level(Level.FINE).verb(NutsLoggerVerb.WARNING)
                .log(getValidLogMessage());
        return true;
    }
}
