package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NCachedValue;
import net.thevpc.nuts.concurrent.NCachedValueModel;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDescribables;
import net.thevpc.nuts.elem.NUpletElementBuilder;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NExceptions;
import net.thevpc.nuts.util.NIllegalStateException;

import java.util.function.Supplier;

public final class NCachedValueImpl<T> implements NCachedValue<T> {

    private final NCachedValueFactoryImpl factory;
    private final Supplier<T> supplier;
    private NCachedValueModel model;

    NCachedValueImpl(String id, Supplier<T> supplier, NCachedValueFactoryImpl factory) {
        this.supplier = NAssert.requireNonNull(supplier);
        this.factory = NAssert.requireNonNull(factory);
        this.model = new NCachedValueModel(NAssert.requireNonNull(id, "id"));
        reload();
    }

    public void reload() {
        synchronized (this) {
            NCachedValueModel m = factory.load(model.getId());
            if (m == null) {
                m = new NCachedValueModel(model.getId());
                m.setExpiry(NDuration.ofMillis(Long.MAX_VALUE));
                m.setRetryPeriod(NDuration.ZERO);
                factory.save(m);
            }
            model = m;
        }
    }

    @Override
    public NCachedValue<T> setExpiryMillis(long expiry) {
        return setExpiry(NDuration.ofMillis(expiry));
    }

    public NCachedValueImpl<T> setExpiry(NDuration expiry) {
        model.setExpiry(NAssert.requireNonNull(expiry));
        factory.save(model);
        return this;
    }

    public NCachedValueImpl<T> setMaxRetries(int maxRetries) {
        if (maxRetries < 0) throw new IllegalArgumentException("maxRetries >= 0");
        model.setMaxRetries(maxRetries);
        factory.save(model);
        return this;
    }

    public NCachedValueImpl<T> setRetryPeriod(NDuration retryPeriod) {
        model.setRetryPeriod(retryPeriod);
        factory.save(model);
        return this;
    }

    public NCachedValueImpl<T> setRetry(int maxRetries, NDuration retryPeriod) {
        return setMaxRetries(maxRetries).setRetryPeriod(retryPeriod);
    }

    public NCachedValueImpl<T> retainLastOnFailure(boolean retain) {
        model.setRetainLastOnFailure(retain);
        factory.save(model);
        return this;
    }

    public NCachedValue<T> invalidate() {
        model.setErrorState(false);
        model.setValue(null);
        model.setInvalidated(true);
        factory.save(model);
        return this;
    }

    public boolean isValid() {
        if(model.isInvalidated()) {
            return false;
        }
        Boolean lastAttemptError = model.getErrorState();
        if (lastAttemptError != null && lastAttemptError) {
            return false;
        }
        if (model.getExpiry() == null) {
            return true;
        }
        long now = System.currentTimeMillis();
        return now - model.getLastEvalTimestamp() <= model.getExpiry().toMillis(); // not expired
    }

    public boolean isError() {
        Boolean lastAttemptError = model.getErrorState();
        if (lastAttemptError != null && lastAttemptError) {
            return true;
        }
        return false;
    }

    public boolean isEvaluated() {
        Boolean lastAttemptError = model.getErrorState();
        return lastAttemptError != null;
    }

    public boolean isExpired() {
        if (model.isInvalidated()) {
            return true;
        }
        if (model.getExpiry() == null) {
            return false;
        }
        return System.currentTimeMillis() - model.getLastEvalTimestamp() >= model.getExpiry().toMillis();
    }

    public T get() {
        long now = System.currentTimeMillis();
        Boolean evb = model.getErrorState();

        boolean expired = evb == null || model.isInvalidated() || (model.getExpiry() != null && (now - model.getLastEvalTimestamp() >= model.getExpiry().toMillis()));
        long effectiveRetryPeriod = (model.getRetryPeriod() != null ? model.getRetryPeriod().toMillis() : 0);

        boolean canRetry = evb == null
                || !evb
                || (model.getFailedAttempts() < model.getMaxRetries() && (now - model.getLastEvalTimestamp() >= effectiveRetryPeriod));

        if (!expired && (evb == null || !evb)) {
            return (T) model.getValue();
        }

        if (!canRetry) {
            if (model.isRetainLastOnFailure() && model.getLastValidValue() != null)
                return (T) model.getLastValidValue();
            throwAsRuntime(model.getValue());
        }

        synchronized (this) {
            evb = model.getErrorState();
            now = System.currentTimeMillis();
            expired = evb == null || model.isInvalidated() || (model.getExpiry() != null && (now - model.getLastEvalTimestamp() >= model.getExpiry().toMillis()));
            canRetry = evb == null
                    || !evb
                    || (model.getFailedAttempts() < model.getMaxRetries() && (now - model.getLastEvalTimestamp() >= effectiveRetryPeriod));

            if (!expired && evb != null && !evb) {
                return (T) model.getValue();
            }
            if (!canRetry) {
                if (model.isRetainLastOnFailure() && model.getLastValidValue() != null)
                    return (T) model.getLastValidValue();
                throwAsRuntime(model.getThrowable());
            }
            return _computeAndSet0(now, supplier);
        }
    }

    private T _computeAndSet0(long now, Supplier<T> supplier) {
        model.setInvalidated(false);
        try {
            T value = supplier.get();
            model.setLastValidValue(value);
            model.setLastEvalTimestamp(now);
            model.setFailedAttempts(0);
            model.setValue(value);
            model.setThrowable(null);
            model.setErrorState(false);
            factory.save(model);
            return value;
        } catch (Throwable ex) {
            model.setFailedAttempts(Math.max(0, model.getFailedAttempts() + 1));
            model.setLastEvalTimestamp(now);
            model.setValue(null);
            model.setThrowable(ex);
            model.setErrorState(true);
            factory.save(model);
            if (model.isRetainLastOnFailure() && model.getLastValidValue() != null)
                return (T) model.getLastValidValue();
            throwAsRuntime(ex);
            return null; // unreachable
        }
    }

    @Override
    public NCachedValue<T> update(Supplier<T> supplier) {
        long now = System.currentTimeMillis();
        synchronized (this) {
            _computeAndSet0(now, supplier==null?this.supplier:supplier);
        }
        return this;
    }

    @Override
    public NCachedValue<T> update() {
        long now = System.currentTimeMillis();
        synchronized (this) {
            _computeAndSet0(now, supplier);
        }
        return this;
    }

    @Override
    public NCachedValue<T> setValue(T value) {
        long now = System.currentTimeMillis();
        synchronized (this) {
            model.setLastValidValue(value);
            model.setLastEvalTimestamp(now);
            model.setFailedAttempts(0);
            model.setValue(value);
            model.setThrowable(null);
            model.setErrorState(false);
            factory.save(model);
        }
        return this;
    }

    @Override
    public boolean computeAndSetIfInvalid(Supplier<T> supplier) {
        long now = System.currentTimeMillis();
        synchronized (this) {
            if (!isValid()) {
                _computeAndSet0(now, supplier==null?this.supplier:supplier);
                return true;
            }
            return false;
        }
    }

    public boolean setValueIfInvalid(T value) {
        long now = System.currentTimeMillis();
        synchronized (this) {
            if (!isValid()) { // no valid value currently
                model.setLastValidValue(value);
                model.setLastEvalTimestamp(now);
                model.setFailedAttempts(0);
                model.setValue(value);
                model.setThrowable(null);
                model.setErrorState(false);
                factory.save(model);
                return true;
            }
            return false;
        }
    }

    private static void throwAsRuntime(Object ex) {
        if (ex instanceof Throwable) {
            throw NExceptions.ofUncheckedException((Throwable) ex);
        }
        throw new NIllegalStateException(NMsg.ofC("unexpected exception : %s", ex));
    }

    @Override
    public NElement describe() {
        NUpletElementBuilder b = NElement.ofUpletBuilder("CachedValue")
                .add("supplier", NElementDescribables.describeResolveOrDestruct(supplier));

        if (model.getExpiry() != null) {
            b.add("expiry", NElementDescribables.describeResolveOrDestruct(model.getExpiry().toMillis()));
        }
        if (model.getRetryPeriod() != null) {
            b.add("retryPeriod", NElementDescribables.describeResolveOrDestruct(model.getRetryPeriod().toMillis()));
        }
        b.add("maxRetries", NElementDescribables.describeResolveOrDestruct(model.getMaxRetries()));
        Boolean ev = model.getErrorState();
        b.add("evaluated", ev != null);
        if (ev != null) {
            b.add("success", !ev);
            if (!ev) {
                b.add("value", NElementDescribables.describeResolveOrDestruct(model.getValue()));
            } else {
                b.add("error", NElementDescribables.describeResolveOrDestruct(model.getThrowable()));
            }
        }
        return b
                .build();
    }
}
