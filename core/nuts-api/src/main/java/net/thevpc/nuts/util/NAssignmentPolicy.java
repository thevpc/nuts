package net.thevpc.nuts.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * NAssignmentPolicy interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NAssignmentPolicy {
    NAssignmentPolicy ANY = NAssignmentPolicySimple.ANY;
    NAssignmentPolicy TARGET_NULL = NAssignmentPolicySimple.TARGET_NULL;
    NAssignmentPolicy TARGET_BLANK = NAssignmentPolicySimple.TARGET_BLANK;
    NAssignmentPolicy SOURCE_NON_NULL = NAssignmentPolicySimple.SOURCE_NON_NULL;
    NAssignmentPolicy SOURCE_NON_BLANK = NAssignmentPolicySimple.SOURCE_NON_BLANK;

    /**
     * Creates a new instance of of.
     *
     * @param source source
     * @param target target
     * @return of result
     */
    static NAssignmentPolicy of(NMapSideStrategy source, NMapSideStrategy target) {
        return NAssignmentPolicySimple.of(source, target);
    }

    /**
     * Apply optional value.
     *
     * @param sourceGetter source getter
     * @param targetGetter target getter
     * @param targetSetter target setter
     * @return apply optional value result
     */
    default <T> boolean applyOptionalValue(Supplier<NOptional<T>> sourceGetter, Supplier<NOptional<T>> targetGetter, Consumer<T> targetSetter) {
        /**
         * Apply value.
         *
         * @param targetSetter target setter
         * @return apply value result
         */
        return applyValue(() -> sourceGetter.get().orNull(), () -> targetGetter.get().orNull(), targetSetter);
    }

    /**
     * Apply optional mapping value.
     *
     * @param sourceGetter source getter
     * @param targetGetter target getter
     * @param targetSetter target setter
     * @return apply optional mapping value result
     */
    default <T> boolean applyOptionalMappingValue(Supplier<NOptional<T>> sourceGetter, Supplier<NOptional<T>> targetGetter, MappingAssigner<T> targetSetter) {
        /**
         * Apply mapping value.
         *
         * @param targetSetter target setter
         * @return apply mapping value result
         */
        return applyMappingValue(() -> sourceGetter.get().orNull(), () -> targetGetter.get().orNull(), targetSetter);
    }

    /**
     * Apply value.
     *
     * @param sourceGetter source getter
     * @param targetGetter target getter
     * @param targetSetter target setter
     * @return apply value result
     */
    <T> boolean applyValue(Supplier<T> sourceGetter, Supplier<T> targetGetter, Consumer<T> targetSetter);

    /**
     * Apply mapping value.
     *
     * @param sourceGetter source getter
     * @param targetGetter target getter
     * @param targetSetter target setter
     * @return apply mapping value result
     */
    <T> boolean applyMappingValue(Supplier<T> sourceGetter, Supplier<T> targetGetter, MappingAssigner<T> targetSetter);

    interface MappingValue<T> {
        /**
         * Source value.
         *
         * @return source value result
         */
        T sourceValue();

        /**
         * Target value.
         *
         * @return target value result
         */
        T targetValue();
    }

    @FunctionalInterface
    interface MappingAssigner<T> {
        /**
         * Apply the value. Return true if a write actually happened, false otherwise.
         */
        boolean apply(MappingValue<T> value);
    }
}
