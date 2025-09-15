package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NCachedValue;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public final class NCachedValueImpl<T> implements NCachedValue<T> {

    private final Supplier<T> supplier;
    private final AtomicReference<Evaluated> lastAttempt = new AtomicReference<>(null);

    private volatile T lastValidValue = null;
    private volatile long lastEvalTimestamp = 0;
    private volatile int failedAttempts = 0;

    private volatile Duration expiry = Duration.ofMillis(Long.MAX_VALUE);
    private volatile Duration retryPeriod = Duration.ZERO;
    private volatile int maxRetries = 0;
    private volatile boolean retainLastOnFailure = false;

    private static class Evaluated {
        final Object valueOrException;
        final boolean isError;

        Evaluated(Object valueOrException, boolean isError) {
            this.valueOrException = valueOrException;
            this.isError = isError;
        }
    }

    private NCachedValueImpl(Supplier<T> supplier) {
        this.supplier = Objects.requireNonNull(supplier);
    }

    public static <T> NCachedValueImpl<T> of(Supplier<T> supplier) {
        return new NCachedValueImpl<>(supplier);
    }

    public NCachedValueImpl<T> setExpiry(Duration expiry) {
        this.expiry = Objects.requireNonNull(expiry);
        return this;
    }

    public NCachedValueImpl<T> setMaxRetries(int maxRetries) {
        if (maxRetries < 0) throw new IllegalArgumentException("maxRetries >= 0");
        this.maxRetries = maxRetries;
        return this;
    }

    public NCachedValueImpl<T> setRetryPeriod(Duration retryPeriod) {
        this.retryPeriod = retryPeriod; // allow null
        return this;
    }

    public NCachedValueImpl<T> setRetry(int maxRetries, Duration retryPeriod) {
        return setMaxRetries(maxRetries).setRetryPeriod(retryPeriod);
    }

    public NCachedValueImpl<T> retainLastOnFailure(boolean retain) {
        this.retainLastOnFailure = retain;
        return this;
    }

    public void invalidate() {
        lastAttempt.set(null);
    }

    public boolean isValid() {
        Evaluated e = lastAttempt.get();
        if (e == null || e.isError) return false;
        long now = System.currentTimeMillis();
        return now - lastEvalTimestamp <= expiry.toMillis(); // not expired
    }

    public boolean isError() {
        Evaluated e = lastAttempt.get();
        return e != null && e.isError;
    }

    public boolean isEvaluated() {
        return lastAttempt.get() != null;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - lastEvalTimestamp >= expiry.toMillis();
    }

    public T get() {
        long now = System.currentTimeMillis();
        Evaluated ev = lastAttempt.get();

        boolean expired = ev == null || (now - lastEvalTimestamp >= expiry.toMillis());
        long effectiveRetryPeriod = (retryPeriod != null ? retryPeriod.toMillis() : expiry.toMillis());

        boolean canRetry = ev == null
                || !ev.isError
                || (failedAttempts < maxRetries && (now - lastEvalTimestamp >= effectiveRetryPeriod));

        if (!expired && !ev.isError) {
            return (T) ev.valueOrException;
        }

        if (!canRetry) {
            if (retainLastOnFailure && lastValidValue != null) return lastValidValue;
            throwAsRuntime(ev.valueOrException);
        }

        synchronized (this) {
            ev = lastAttempt.get();
            now = System.currentTimeMillis();
            expired = ev == null || (now - lastEvalTimestamp >= expiry.toMillis());
            canRetry = ev == null
                    || !ev.isError
                    || (failedAttempts < maxRetries && (now - lastEvalTimestamp >= effectiveRetryPeriod));

            if (!expired && ev != null && !ev.isError) {
                return (T) ev.valueOrException;
            }
            if (!canRetry) {
                if (retainLastOnFailure && lastValidValue != null) return lastValidValue;
                throwAsRuntime(ev.valueOrException);
            }

            try {
                T value = supplier.get();
                lastValidValue = value;
                lastEvalTimestamp = now;
                failedAttempts = 0;
                lastAttempt.set(new Evaluated(value, false));
                return value;
            } catch (Throwable ex) {
                failedAttempts++;
                lastEvalTimestamp = now;
                lastAttempt.set(new Evaluated(ex, true));
                if (retainLastOnFailure && lastValidValue != null) return lastValidValue;
                throwAsRuntime(ex);
                return null; // unreachable
            }
        }
    }

    @Override
    public NCachedValue<T> computeAndSet(Supplier<T> supplier) {
        long now = System.currentTimeMillis();
        synchronized(this) {
            try {
                T value = supplier.get();
                lastValidValue = value;
                lastEvalTimestamp = now;
                failedAttempts = 0;
                lastAttempt.set(new Evaluated(value, false));
            } catch (Throwable ex) {
                failedAttempts++;
                lastEvalTimestamp = now;
                lastAttempt.set(new Evaluated(ex, true));
//                    if (retainLastOnFailure && lastValidValue != null) return lastValidValue;
                throwAsRuntime(ex);
            }
        }
        return this;
    }

    @Override
    public NCachedValue<T> setValue(T value) {
        long now = System.currentTimeMillis();
        synchronized(this) {
            lastValidValue = value;
            lastEvalTimestamp = now;
            failedAttempts = 0;
            lastAttempt.set(new Evaluated(value, false));
        }
        return this;
    }

    @Override
    public boolean computeAndSetIfInvalid(Supplier<T> supplier) {
        long now = System.currentTimeMillis();
        synchronized(this) {
            if (!isValid()) {
                try {
                    T value = supplier.get();
                    lastValidValue = value;
                    lastEvalTimestamp = now;
                    failedAttempts = 0;
                    lastAttempt.set(new Evaluated(value, false));
                } catch (Throwable ex) {
                    failedAttempts++;
                    lastEvalTimestamp = now;
                    lastAttempt.set(new Evaluated(ex, true));
//                    if (retainLastOnFailure && lastValidValue != null) return lastValidValue;
                    throwAsRuntime(ex);
                }
                return true;
            }
            return false;
        }
    }

    public boolean setValueIfInvalid(T value) {
        synchronized(this) {
            if (!isValid()) { // no valid value currently
                lastAttempt.set(new Evaluated( value,false));
                lastValidValue = value;
                lastEvalTimestamp = System.currentTimeMillis();
                failedAttempts = 0; // reset retry
                return true;
            }
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void throwAsRuntime(Object ex) throws E {
        throw (E) ex;
    }
}
