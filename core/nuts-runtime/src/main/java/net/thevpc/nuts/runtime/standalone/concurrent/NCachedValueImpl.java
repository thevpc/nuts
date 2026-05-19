package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NCachedValue;
import net.thevpc.nuts.concurrent.NCachedValueModel;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NDescribables;
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
        this.supplier = NAssert.requireNamedNonNull(supplier);
        this.factory = NAssert.requireNamedNonNull(factory);
        this.model = new NCachedValueModel(NAssert.requireNamedNonNull(id, "id"));
        reload();
    }

    public void reload() {
        synchronized (this) {
            NCachedValueModel m = factory.load(model.id());
            if (m == null) {
                m = new NCachedValueModel(model.id());
                m.expiry(NDuration.ofMillis(Long.MAX_VALUE));
                m.retryPeriod(NDuration.ZERO);
                factory.save(m);
            }
            model = m;
        }
    }

    @Override
    public NCachedValue<T> expiryMillis(long expiry) {
        return expiry(NDuration.ofMillis(expiry));
    }

    public NCachedValueImpl<T> expiry(NDuration expiry) {
        model.expiry(NAssert.requireNamedNonNull(expiry));
        factory.save(model);
        return this;
    }

    public NCachedValueImpl<T> maxRetries(int maxRetries) {
        if (maxRetries < 0) throw new IllegalArgumentException("maxRetries >= 0");
        model.maxRetries(maxRetries);
        factory.save(model);
        return this;
    }

    public NCachedValueImpl<T> retryPeriod(NDuration retryPeriod) {
        model.retryPeriod(retryPeriod);
        factory.save(model);
        return this;
    }

    public NCachedValueImpl<T> retry(int maxRetries, NDuration retryPeriod) {
        return maxRetries(maxRetries).retryPeriod(retryPeriod);
    }

    public NCachedValueImpl<T> retainLastOnFailure(boolean retain) {
        model.retainLastOnFailure(retain);
        factory.save(model);
        return this;
    }

    public NCachedValue<T> invalidate() {
        model.errorState(false);
        model.value(null);
        model.invalidated(true);
        factory.save(model);
        return this;
    }

    public boolean isValid() {
        if(model.isInvalidated()) {
            return false;
        }
        Boolean lastAttemptError = model.errorState();
        if (lastAttemptError != null && lastAttemptError) {
            return false;
        }
        if (model.expiry() == null) {
            return true;
        }
        long now = System.currentTimeMillis();
        return now - model.lastEvalTimestamp() <= model.expiry().toMillis(); // not expired
    }

    public boolean isError() {
        Boolean lastAttemptError = model.errorState();
        if (lastAttemptError != null && lastAttemptError) {
            return true;
        }
        return false;
    }

    public boolean isEvaluated() {
        Boolean lastAttemptError = model.errorState();
        return lastAttemptError != null;
    }

    public boolean isExpired() {
        if (model.isInvalidated()) {
            return true;
        }
        if (model.expiry() == null) {
            return false;
        }
        return System.currentTimeMillis() - model.lastEvalTimestamp() >= model.expiry().toMillis();
    }

    public T get() {
        long now = System.currentTimeMillis();
        Boolean evb = model.errorState();

        boolean expired = evb == null || model.isInvalidated() || (model.expiry() != null && (now - model.lastEvalTimestamp() >= model.expiry().toMillis()));
        long effectiveRetryPeriod = (model.retryPeriod() != null ? model.retryPeriod().toMillis() : 0);

        boolean canRetry = evb == null
                || !evb
                || (model.failedAttempts() < model.maxRetries() && (now - model.lastEvalTimestamp() >= effectiveRetryPeriod));

        if (!expired && (evb == null || !evb)) {
            return (T) model.value();
        }

        if (!canRetry) {
            if (model.isRetainLastOnFailure() && model.lastValidValue() != null)
                return (T) model.lastValidValue();
            throwAsRuntime(model.value());
        }

        synchronized (this) {
            evb = model.errorState();
            now = System.currentTimeMillis();
            expired = evb == null || model.isInvalidated() || (model.expiry() != null && (now - model.lastEvalTimestamp() >= model.expiry().toMillis()));
            canRetry = evb == null
                    || !evb
                    || (model.failedAttempts() < model.maxRetries() && (now - model.lastEvalTimestamp() >= effectiveRetryPeriod));

            if (!expired && evb != null && !evb) {
                return (T) model.value();
            }
            if (!canRetry) {
                if (model.isRetainLastOnFailure() && model.lastValidValue() != null)
                    return (T) model.lastValidValue();
                throwAsRuntime(model.error());
            }
            return _computeAndSet0(now, supplier);
        }
    }

    private T _computeAndSet0(long now, Supplier<T> supplier) {
        model.invalidated(false);
        try {
            T value = supplier.get();
            model.lastValidValue(value);
            model.lastEvalTimestamp(now);
            model.failedAttempts(0);
            model.value(value);
            model.error(null);
            model.errorState(false);
            factory.save(model);
            return value;
        } catch (Throwable ex) {
            model.failedAttempts(Math.max(0, model.failedAttempts() + 1));
            model.lastEvalTimestamp(now);
            model.value(null);
            model.error(ex);
            model.errorState(true);
            factory.save(model);
            if (model.isRetainLastOnFailure() && model.lastValidValue() != null)
                return (T) model.lastValidValue();
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
    public NCachedValue<T> value(T value) {
        long now = System.currentTimeMillis();
        synchronized (this) {
            model.lastValidValue(value);
            model.lastEvalTimestamp(now);
            model.failedAttempts(0);
            model.value(value);
            model.error(null);
            model.errorState(false);
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
                model.lastValidValue(value);
                model.lastEvalTimestamp(now);
                model.failedAttempts(0);
                model.value(value);
                model.error(null);
                model.errorState(false);
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
                .add("supplier", NDescribables.describeResolveOrSimplify(supplier));

        if (model.expiry() != null) {
            b.add("expiry", NDescribables.describeResolveOrSimplify(model.expiry().toMillis()));
        }
        if (model.retryPeriod() != null) {
            b.add("retryPeriod", NDescribables.describeResolveOrSimplify(model.retryPeriod().toMillis()));
        }
        b.add("maxRetries", NDescribables.describeResolveOrSimplify(model.maxRetries()));
        Boolean ev = model.errorState();
        b.add("evaluated", ev != null);
        if (ev != null) {
            b.add("success", !ev);
            if (!ev) {
                b.add("value", NDescribables.describeResolveOrSimplify(model.value()));
            } else {
                b.add("error", NDescribables.describeResolveOrSimplify(model.error()));
            }
        }
        return b
                .build();
    }
}
