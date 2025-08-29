package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NAsk;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.util.NMsg;

import java.util.function.Supplier;
import java.util.logging.Level;

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
        NAssert.requireNonNull(m, "message");
        this.askMessage = m;
        return this;
    }

    public DefaultWriteTypeProcessor withLog(NLog log, NMsg m) {
        NAssert.requireNonNull(log, "log");
        NAssert.requireNonNull(m, "message");
        this.log = log;
        this.logMessage = m;
        return this;
    }

    public DefaultWriteTypeProcessor onError(Supplier<RuntimeException> error) {
        NAssert.requireNonNull(error, "error handler");
        this.error = error;
        return this;
    }

    private NMsg getValidAskMessage() {
        NAssert.requireNonNull(askMessage, "message");
        return askMessage;
    }

    private NMsg getValidLogMessage() {
        NAssert.requireNonNull(logMessage, "log message");
        return logMessage;
    }

    private Supplier<RuntimeException> getValidError() {
        NAssert.requireNonNull(error, "error handler");
        return error;
    }

    private NLog getValidLog() {
        NAssert.requireNonNull(log, "log");
        return log;
    }


    public boolean process() {
        switch (writeType) {
            case ERROR: {
                throw getValidError().get();
            }
            case ASK: {
                if (!NAsk.of()
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
