package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NRateLimitExceededException;
import net.thevpc.nuts.concurrent.NRateLimitValueResult;
import net.thevpc.nuts.concurrent.NCallable;
import net.thevpc.nuts.text.NMsg;

import java.util.function.Consumer;
import java.util.function.Function;

class NRateLimitValueResultImpl implements NRateLimitValueResult {
    private boolean success;
    private boolean processed;
    private NMsg errorMessage;
    private Object result;

    public NRateLimitValueResultImpl(boolean success, Object result,NMsg errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.result = result;
    }

    @Override
    public boolean success() {
        return success;
    }

    @Override
    public NRateLimitValueResult orElseError() {
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
    public NRateLimitValueResult orElseThrow(Function<NMsg,RuntimeException> other) {
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
    public NRateLimitValueResult onSuccess(Runnable r) {
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
    public <T> NRateLimitValueResult onSuccessCall(NCallable<T> r) {
        if(processed){
            return this;
        }
        if (success) {
            processed=true;
            result= r.call();
        }
        return this;
    }

    @Override
    public NRateLimitValueResult orElse(Consumer<NMsg> r) {
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
    public <T> NRateLimitValueResult orElseCall(Function<NMsg,T> r) {
        if(processed){
            return this;
        }
        if (!success) {
            processed=true;
            result= r.apply(errorMessage);
        }
        return this;
    }

    @Override
    public <T> T get() {
        return (T) result;
    }
}
