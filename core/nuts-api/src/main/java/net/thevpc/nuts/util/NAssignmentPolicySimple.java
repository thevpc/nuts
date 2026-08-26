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

    /**
     * Creates a new instance of of.
     *
     * @param source source
     * @param target target
     * @return of result
     */
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

    /**
     * N assignment policy simple.
     *
     * @param source source
     * @param target target
     * @return n assignment policy simple result
     */
    public NAssignmentPolicySimple(NMapSideStrategy source, NMapSideStrategy target) {
        NAssert.requireNamedNonNull(source, "source");
        NAssert.requireNamedNonNull(target, "target");
        this.source = source;
        this.target = target;
    }

    /**
     * Source.
     *
     * @return source result
     */
    public NMapSideStrategy source() {
        return source;
    }

    /**
     * Target.
     *
     * @return target result
     */
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

    /**
     * Apply optional value.
     *
     * @param sourceGetter source getter
     * @param targetGetter target getter
     * @param targetSetter target setter
     * @return apply optional value result
     */
    public <T> boolean applyOptionalValue(Supplier<NOptional<T>> sourceGetter, Supplier<NOptional<T>> targetGetter, Consumer<T> targetSetter) {
        /**
         * Apply value.
         *
         * @param targetSetter target setter
         * @return apply value result
         */
        return applyValue(() -> sourceGetter.get().orNull(), () -> targetGetter.get().orNull(), targetSetter);
    }

    /**
     * Apply value.
     *
     * @param sourceGetter source getter
     * @param targetGetter target getter
     * @param targetSetter target setter
     * @return apply value result
     */
    public <T> boolean applyValue(Supplier<T> sourceGetter, Supplier<T> targetGetter, Consumer<T> targetSetter) {
        return applyMappingValue(sourceGetter, targetGetter, a -> {
            targetSetter.accept(a.sourceValue());
            return true;
        });
    }

    /**
     * Apply mapping value.
     *
     * @param sourceGetter source getter
     * @param targetGetter target getter
     * @param targetSetter target setter
     * @return apply mapping value result
     */
    public <T> boolean applyMappingValue(Supplier<T> sourceGetter, Supplier<T> targetGetter, MappingAssigner<T> targetSetter) {
        MappingValueImpl<T> assignableValue = new MappingValueImpl<>(sourceGetter, targetGetter);
        if (doRejectSideStrategy(source, assignableValue::sourceValue)) {
            return false;
        }
        if (doRejectSideStrategy(target, assignableValue::targetValue)) {
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

        /**
         * Mapping value impl.
         *
         * @param sourceSupplier source supplier
         * @param targetSupplier target supplier
         * @return mapping value impl result
         */
        public MappingValueImpl(Supplier<T> sourceSupplier, Supplier<T> targetSupplier) {
            this.sourceSupplier = sourceSupplier;
            this.targetSupplier = targetSupplier;
        }

        /**
         * Source value.
         *
         * @return source value result
         */
        public T sourceValue() {
            if (!sourceSet) {
                sourceSet = true;
                source = sourceSupplier.get();
            }
            return source;
        }

        /**
         * Target value.
         *
         * @return target value result
         */
        public T targetValue() {
            if (!targetSet) {
                targetSet = true;
                target = targetSupplier.get();
            }
            return target;
        }
    }

    /**
     * Do reject side strategy.
     *
     * @param source source
     * @param any any
     * @return do reject side strategy result
     */
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
