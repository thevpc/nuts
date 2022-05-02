package net.thevpc.nuts.reserved;

import net.thevpc.nuts.*;

import java.util.function.Function;
import java.util.function.Supplier;

public class NutsReservedOptionalError<T> extends NutsReservedOptionalThrowable<T> {
    private Function<NutsSession, NutsMessage> message;
    private Throwable error;

    public NutsReservedOptionalError(Function<NutsSession, NutsMessage> message, Throwable error) {
        if (message == null) {
            message = (s) -> {
                Throwable error1 = NutsReservedOptionalError.this.error;
                if (error1 == null) {
                    return NutsMessage.cstyle("erroneous value");
                } else {
                    return NutsMessage.cstyle("erroneous value : %s", NutsReservedLangUtils.getErrorMessage(error));
                }
            };
        }
        this.message = message;
        this.error = error;
    }

    @Override
    public Throwable getError() {
        return error;
    }

    @Override
    public T get(NutsSession session) {
        return get(this.message, session);
    }

    @Override
    public T get(Supplier<NutsMessage> message) {
        return get(s -> message.get(), null);
    }


    @Override
    public T get(Function<NutsSession, NutsMessage> message, NutsSession session) {
        if (message == null) {
            message = this.message;
        }
        NutsMessage m = prepareMessage(message.apply(session));
        if (session == null) {
            throw new RuntimeException(m.toString());
        } else {
            throw new NutsOptionalErrorException(session, m);
        }
    }

    @Override
    public NutsOptional<T> nonBlank() {
        return this;
    }

    @Override
    public boolean isError() {
        return true;
    }

    @Override
    public boolean isPresent() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isNotPresent() {
        return true;
    }

    @Override
    public Function<NutsSession, NutsMessage> getMessage() {
        return message;
    }

    @Override
    public boolean isBlank() {
        return false;
    }
    @Override
    public String toString() {
        return "ErrorOptional@" + System.identityHashCode(this);
    }
}
