package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NCallable;
import net.thevpc.nuts.util.NMsg;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface NRateLimitedValueResult {
    boolean success();

    //throw error if not success
    NRateLimitedValueResult orElseError();

    NRateLimitedValueResult orElseThrow(Function<NMsg, RuntimeException> other);

    NRateLimitedValueResult onSuccess(Runnable r);

    <T> T onSuccessCall(NCallable<T> r);

    NRateLimitedValueResult orElse(Consumer<NMsg> r);

    <T> T orElseCall(Function<NMsg, T> r);
}
