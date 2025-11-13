package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * Represents the overall execution status of a saga.
 * <p>
 * Implements {@link NEnum} for lower-cased IDs and parsing support via {@link #parse(String)}.
 *
 * @since 0.8.7
 */
public enum NSagaStatus implements NEnum {

    /**
     * The saga has not yet started execution.
     */
    PENDING,
    /**
     * The saga is currently executing its steps.
     */
    RUNNING,
    /**
     * The saga finished all steps normally without errors.
     */
    SUCCESS,
    /**
     * Some steps failed, but all required compensations succeeded.
     */
    ROLLED_BACK,
    /**
     * Some steps failed, and some compensations were ignored or skipped (strategy = IGNORE).
     */
    PARTIAL_ROLLBACK,
    /**
     * Some step could not be compensated (strategy = ABORT); the saga failed.
     */
    FAILED;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NSagaStatus() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    /**
     * Parses a string value into the corresponding {@link NSagaStatus}.
     *
     * @param value the string representation of the saga status
     * @return an {@link NOptional} containing the status if found, or empty if not
     */
    public static NOptional<NSagaStatus> parse(String value) {
        return NEnumUtils.parseEnum(value, NSagaStatus.class);
    }

    @Override
    public String id() {
        return id;
    }
}
