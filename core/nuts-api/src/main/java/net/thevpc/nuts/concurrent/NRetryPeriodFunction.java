package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.internal.rpi.NConcurrentRPI;
import net.thevpc.nuts.time.NDuration;

import java.util.function.IntFunction;

@FunctionalInterface
public interface NRetryPeriodFunction extends IntFunction<NDuration> {

    static NRetryPeriodFunction ofFixedPeriod(NDuration period) {
        return ofFixedPeriods(period);
    }

    static NRetryPeriodFunction ofFixedPeriods(NDuration... periods) {
        return NConcurrentRPI.of().createRetryFixedPeriods(periods);
    }

    static NRetryPeriodFunction ofExponentialPeriod(NDuration base, double multiplier) {
        return NConcurrentRPI.of().createRetryExponentialPeriod(base, multiplier);
    }

    static NRetryPeriodFunction ofMultiplied(NDuration base, double multiplier) {
        return NConcurrentRPI.of().createRetryMultipliedPeriod(base, multiplier);
    }

}
