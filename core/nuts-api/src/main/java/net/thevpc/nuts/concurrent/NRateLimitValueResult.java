package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NMsg;

import java.util.function.Consumer;
import java.util.function.Function;

public interface NRateLimitValueResult {
    boolean success();

    //throw error if not success
    NRateLimitValueResult orElseError();

    NRateLimitValueResult orElseThrow(Function<NMsg, RuntimeException> other);

    NRateLimitValueResult onSuccess(Runnable r);

    <T> NRateLimitValueResult onSuccessCall(NCallable<T> r);

    NRateLimitValueResult orElse(Consumer<NMsg> r);

    <T> NRateLimitValueResult orElseCall(Function<NMsg, T> r);

    <T> T get();
}
