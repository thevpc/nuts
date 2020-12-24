package net.thevpc.nuts.runtime.core;

import net.thevpc.nuts.NutsConfirmationMode;
import net.thevpc.nuts.NutsLogger;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.log.NutsLogVerb;

import java.util.function.Supplier;
import java.util.logging.Level;

public class DefaultWriteTypeProcessor {
    private String askMessage;
    private Object[] askMessageParams;
    private String logMessage;
    private Object[] logMessageParams;
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

    public DefaultWriteTypeProcessor ask(String m, Object... p) {
        if (m == null) {
            throw new IllegalArgumentException("missing ask message");
        }
        this.askMessage = m;
        this.askMessageParams = p;
        return this;
    }

    public DefaultWriteTypeProcessor withLog(NutsLogger log, String m, Object... p) {
        if (log == null) {
            throw new IllegalArgumentException("missing ask log");
        }
        this.log = log;
        if (m == null) {
            throw new IllegalArgumentException("missing log message");
        }
        this.logMessage = m;
        this.logMessageParams = p;
        return this;
    }

    public DefaultWriteTypeProcessor onError(Supplier<RuntimeException> error) {
        if (error == null) {
            throw new IllegalArgumentException("missing error handler");
        }
        this.error = error;
        return this;
    }

    private String getAskMessage() {
        if (askMessage == null) {
            throw new IllegalArgumentException("missing ask message");
        }
        return askMessage;
    }

    private Object[] getAskMessageParams() {
        return askMessageParams == null ? new Object[0] : askMessageParams;
    }

    private String getLogMessage() {
        if (logMessage == null) {
            throw new IllegalArgumentException("missing log message");
        }
        return logMessage;
    }

    private Object[] getLogMessageParams() {
        return logMessageParams == null ? new Object[0] : logMessageParams;
    }

    private Supplier<RuntimeException> getError() {
        if (error == null) {
            throw new IllegalArgumentException("missing error handler");
        }
        return error;
    }

    private NutsLogger getLog() {
        if (log == null) {
            throw new IllegalArgumentException("missing log");
        }
        return log;
    }


    public boolean process() {
        switch (writeType) {
            case ERROR: {
                throw getError().get();
            }
            case ASK: {
                if (!session.getTerminal().ask().forBoolean(getAskMessage(), getAskMessageParams())
                        .defaultValue(false).getBooleanValue()) {
                    return false;
                }
                break;
            }
            case NO: {
                getLog().with().level(Level.FINE).verb(NutsLogVerb.WARNING).log( getLogMessage(), getLogMessageParams());
                return false;
            }
        }
        getLog().with().level(Level.FINE).verb(NutsLogVerb.WARNING).log( getLogMessage(), getLogMessageParams());
        return true;
    }
}
