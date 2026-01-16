package net.thevpc.nuts.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface NAssignmentPolicy {
    NAssignmentPolicy ANY = NAssignmentPolicySimple.ANY;
    NAssignmentPolicy TARGET_NULL = NAssignmentPolicySimple.TARGET_NULL;
    NAssignmentPolicy TARGET_BLANK = NAssignmentPolicySimple.TARGET_BLANK;
    NAssignmentPolicy SOURCE_NON_NULL = NAssignmentPolicySimple.SOURCE_NON_NULL;
    NAssignmentPolicy SOURCE_NON_BLANK = NAssignmentPolicySimple.SOURCE_NON_BLANK;

    static NAssignmentPolicy of(NMapSideStrategy source, NMapSideStrategy target) {
        return NAssignmentPolicySimple.of(source, target);
    }

    default <T> boolean applyOptionalValue(Supplier<NOptional<T>> sourceGetter, Supplier<NOptional<T>> targetGetter, Consumer<T> targetSetter) {
        return applyValue(() -> sourceGetter.get().orNull(), () -> targetGetter.get().orNull(), targetSetter);
    }

    default <T> boolean applyOptionalMappingValue(Supplier<NOptional<T>> sourceGetter, Supplier<NOptional<T>> targetGetter, MappingAssigner<T> targetSetter) {
        return applyMappingValue(() -> sourceGetter.get().orNull(), () -> targetGetter.get().orNull(), targetSetter);
    }

    <T> boolean applyValue(Supplier<T> sourceGetter, Supplier<T> targetGetter, Consumer<T> targetSetter);

    <T> boolean applyMappingValue(Supplier<T> sourceGetter, Supplier<T> targetGetter, MappingAssigner<T> targetSetter);

    interface MappingValue<T> {
        T getSourceValue();

        T getTargetValue();
    }

    @FunctionalInterface
    interface MappingAssigner<T> {
        /**
         * Apply the value. Return true if a write actually happened, false otherwise.
         */
        boolean apply(MappingValue<T> value);
    }
}
