package net.thevpc.nuts.runtime.core;

import net.thevpc.nuts.NutsLogger;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.log.NutsLogVerb;

import java.util.function.Supplier;
import java.util.logging.Level;

public class DefaultWriteTypeProcessor {
    private String askMessage;
    private Object[] askMessageParams;
    private String logMessage;
    private Object[] logMessageParams;
    private Supplier<RuntimeException> error;
    private NutsLogger log;
    private WriteType writeType;
    private NutsSession session;

    public DefaultWriteTypeProcessor(WriteType writeType, NutsSession session) {
        this.writeType = writeType;
        this.session = session;
    }

    public static DefaultWriteTypeProcessor of(WriteType writeType, NutsSession session) {
        return new DefaultWriteTypeProcessor(writeType, session);
    }

    public DefaultWriteTypeProcessor ask(String m, Object... p) {
        if (m == null) {
            throw new IllegalArgumentException("Missing Ask Message");
        }
        this.askMessage = m;
        this.askMessageParams = p;
        return this;
    }

    public DefaultWriteTypeProcessor withLog(NutsLogger log, String m, Object... p) {
        if (log == null) {
            throw new IllegalArgumentException("Missing Ask Log");
        }
        this.log = log;
        if (m == null) {
            throw new IllegalArgumentException("Missing Log Message");
        }
        this.logMessage = m;
        this.logMessageParams = p;
        return this;
    }

    public DefaultWriteTypeProcessor onError(Supplier<RuntimeException> error) {
        if (error == null) {
            throw new IllegalArgumentException("Missing Error Handler");
        }
        this.error = error;
        return this;
    }

    private String getAskMessage() {
        if (askMessage == null) {
            throw new IllegalArgumentException("Missing Ask message");
        }
        return askMessage;
    }

    private Object[] getAskMessageParams() {
        return askMessageParams == null ? new Object[0] : askMessageParams;
    }

    private String getLogMessage() {
        if (logMessage == null) {
            throw new IllegalArgumentException("Missing Log Message");
        }
        return logMessage;
    }

    private Object[] getLogMessageParams() {
        return logMessageParams == null ? new Object[0] : logMessageParams;
    }

    private Supplier<RuntimeException> getError() {
        if (error == null) {
            throw new IllegalArgumentException("Missing Error Handler");
        }
        return error;
    }

    private NutsLogger getLog() {
        if (log == null) {
            throw new IllegalArgumentException("Missing Log");
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
            case IGNORE: {
                getLog().with().level(Level.FINE).verb(NutsLogVerb.WARNING).log( getLogMessage(), getLogMessageParams());
                return false;
            }
        }
        getLog().with().level(Level.FINE).verb(NutsLogVerb.WARNING).log( getLogMessage(), getLogMessageParams());
        return true;
    }
}
