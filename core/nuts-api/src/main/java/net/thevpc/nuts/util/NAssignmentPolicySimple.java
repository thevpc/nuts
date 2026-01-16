package net.thevpc.nuts.util;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

class NAssignmentPolicySimple implements NAssignmentPolicy {
    private static final NAssignmentPolicy[][] _CACHE = new NAssignmentPolicy[NMapSideStrategy.values().length][NMapSideStrategy.values().length];
    public static final NAssignmentPolicy ANY = of(NMapSideStrategy.ANY, NMapSideStrategy.ANY);
    public static final NAssignmentPolicy TARGET_NULL = of(NMapSideStrategy.ANY, NMapSideStrategy.NULL);
    public static final NAssignmentPolicy TARGET_BLANK = of(NMapSideStrategy.ANY, NMapSideStrategy.BLANK);
    public static final NAssignmentPolicy SOURCE_NON_NULL = of(NMapSideStrategy.NON_NULL, NMapSideStrategy.ANY);
    public static final NAssignmentPolicy SOURCE_NON_BLANK = of(NMapSideStrategy.NON_BLANK, NMapSideStrategy.ANY);

    private NMapSideStrategy source;
    private NMapSideStrategy target;

    public static NAssignmentPolicy of(NMapSideStrategy source, NMapSideStrategy target) {
        if (source == null) {
            source = NMapSideStrategy.ANY;
        }
        if (target == null) {
            target = NMapSideStrategy.ANY;
        }
        int so = source.ordinal();
        int to = target.ordinal();
        NAssignmentPolicy o = _CACHE[so][to];
        if (o != null) {
            return o;
        }
        o = new NAssignmentPolicySimple(source, target);
        _CACHE[so][to] = o;
        return o;
    }

    public NAssignmentPolicySimple(NMapSideStrategy source, NMapSideStrategy target) {
        NAssert.requireNonNull(source, "source");
        NAssert.requireNonNull(target, "target");
        this.source = source;
        this.target = target;
    }

    public NMapSideStrategy source() {
        return source;
    }

    public NMapSideStrategy target() {
        return target;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NAssignmentPolicySimple that = (NAssignmentPolicySimple) o;
        return source == that.source && target == that.target;
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target);
    }

    @Override
    public String toString() {
        return "NMapStrategy{" +
                "source=" + source +
                ", target=" + target +
                '}';
    }

    public <T> boolean applyOptionalValue(Supplier<NOptional<T>> sourceGetter, Supplier<NOptional<T>> targetGetter, Consumer<T> targetSetter) {
        return applyValue(() -> sourceGetter.get().orNull(), () -> targetGetter.get().orNull(), targetSetter);
    }

    public <T> boolean applyValue(Supplier<T> sourceGetter, Supplier<T> targetGetter, Consumer<T> targetSetter) {
        return applyMappingValue(sourceGetter, targetGetter, a -> {
            targetSetter.accept(a.getSourceValue());
            return true;
        });
    }

    public <T> boolean applyMappingValue(Supplier<T> sourceGetter, Supplier<T> targetGetter, MappingAssigner<T> targetSetter) {
        MappingValueImpl<T> assignableValue = new MappingValueImpl<>(sourceGetter, targetGetter);
        if (doRejectSideStrategy(source, assignableValue::getSourceValue)) {
            return false;
        }
        if (doRejectSideStrategy(target, assignableValue::getTargetValue)) {
            return false;
        }
        return targetSetter.apply(assignableValue);
    }

    private static class MappingValueImpl<T> implements MappingValue<T> {
        Supplier<T> sourceSupplier;
        T source;
        boolean sourceSet;

        Supplier<T> targetSupplier;
        T target;
        boolean targetSet;

        public MappingValueImpl(Supplier<T> sourceSupplier, Supplier<T> targetSupplier) {
            this.sourceSupplier = sourceSupplier;
            this.targetSupplier = targetSupplier;
        }

        public T getSourceValue() {
            if (!sourceSet) {
                sourceSet = true;
                source = sourceSupplier.get();
            }
            return source;
        }

        public T getTargetValue() {
            if (!targetSet) {
                targetSet = true;
                target = targetSupplier.get();
            }
            return target;
        }
    }

    private <V> boolean doRejectSideStrategy(NMapSideStrategy source, Supplier<V> any) {
        switch (source) {
            case ANY: {
                return false;
            }
            case NULL: {
                return any.get() != null;
            }
            case NON_NULL: {
                return any.get() == null;
            }
            case BLANK: {
                return !NBlankable.isBlank(any.get());
            }
            case NON_BLANK: {
                return NBlankable.isBlank(any.get());
            }
        }
        return true;
    }
}
