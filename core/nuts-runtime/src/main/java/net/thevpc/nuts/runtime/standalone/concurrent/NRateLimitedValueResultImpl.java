package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.concurrent.NRateLimitExceededException;
import net.thevpc.nuts.concurrent.NRateLimitedValueResult;
import net.thevpc.nuts.util.NCallable;
import net.thevpc.nuts.util.NMsg;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

class NRateLimitedValueResultImpl implements NRateLimitedValueResult {
    private boolean success;
    private boolean processed;
    private NMsg errorMessage;

    public NRateLimitedValueResultImpl(boolean success, NMsg errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean success() {
        return success;
    }

    @Override
    public NRateLimitedValueResult orElseError() {
        if(processed){
            return this;
        }
        if (!success) {
            processed=true;
            throw new NRateLimitExceededException(errorMessage);
        }
        return this;
    }

    @Override
    public NRateLimitedValueResult orElseThrow(Function<NMsg,RuntimeException> other) {
        if(processed){
            return this;
        }
        if (!success) {
            processed=true;
            if (other != null) {
                RuntimeException e = other.apply(errorMessage);
                if (e != null) {
                    throw e;
                }
            }
        }
        return orElseError();
    }

    @Override
    public NRateLimitedValueResult onSuccess(Runnable r) {
        if(processed){
            return this;
        }
        if (success) {
            processed=true;
            r.run();
        }
        return this;
    }

    @Override
    public <T> T onSuccessCall(NCallable<T> r) {
        if(processed){
            return null;
        }
        if (success) {
            processed=true;
            return r.call();
        }
        return null;
    }

    @Override
    public NRateLimitedValueResult orElse(Consumer<NMsg> r) {
        if(processed){
            return this;
        }
        if (!success) {
            processed=true;
            r.accept(errorMessage);
        }
        return this;
    }

    @Override
    public <T> T orElseCall(Function<NMsg,T> r) {
        if(processed){
            return null;
        }
        if (!success) {
            processed=true;
            return r.apply(errorMessage);
        }
        return null;
    }
}
