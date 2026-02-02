package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.core.NConfirmationMode;
import net.thevpc.nuts.io.NIn;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.text.NMsg;

import java.util.function.Supplier;

public class DefaultWriteTypeProcessor {
    private NMsg askMessage;
    private NMsg logMessage;
    private Supplier<RuntimeException> error;
    private NLog log;
    private NConfirmationMode writeType;

    public DefaultWriteTypeProcessor(NConfirmationMode writeType) {
        this.writeType = writeType;
    }

    public static DefaultWriteTypeProcessor of(NConfirmationMode writeType) {
        return new DefaultWriteTypeProcessor(writeType);
    }

    public DefaultWriteTypeProcessor ask(NMsg m) {
        NAssert.requireNamedNonNull(m, "message");
        this.askMessage = m;
        return this;
    }

    public DefaultWriteTypeProcessor withLog(NLog log, NMsg m) {
        NAssert.requireNamedNonNull(log, "log");
        NAssert.requireNamedNonNull(m, "message");
        this.log = log;
        this.logMessage = m;
        return this;
    }

    public DefaultWriteTypeProcessor onError(Supplier<RuntimeException> error) {
        NAssert.requireNamedNonNull(error, "error handler");
        this.error = error;
        return this;
    }

    private NMsg getValidAskMessage() {
        NAssert.requireNamedNonNull(askMessage, "message");
        return askMessage;
    }

    private NMsg getValidLogMessage() {
        NAssert.requireNamedNonNull(logMessage, "log message");
        return logMessage;
    }

    private Supplier<RuntimeException> getValidError() {
        NAssert.requireNamedNonNull(error, "error handler");
        return error;
    }

    private NLog getValidLog() {
        NAssert.requireNamedNonNull(log, "log");
        return log;
    }


    public boolean process() {
        switch (writeType) {
            case ERROR: {
                throw getValidError().get();
            }
            case ASK: {
                if (!NIn.ask()
                        .forBoolean(getValidAskMessage())
                        .setDefaultValue(false).getBooleanValue()) {
                    return false;
                }
                break;
            }
            case NO: {
                getValidLog()
                        .log(getValidLogMessage().asFineAlert());
                return false;
            }
        }
        getValidLog()
                .log(getValidLogMessage().asFineAlert());
        return true;
    }
}
