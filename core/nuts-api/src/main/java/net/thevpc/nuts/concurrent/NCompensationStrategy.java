package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * Defines how a saga should behave when a compensation step fails.
 * <p>
 * In a saga, compensation steps are used to undo completed actions when
 * part of the workflow fails. The {@code NCompensationStrategy} determines
 * whether the saga should stop or continue execution when one of these
 * compensations itself fails.
 *
 * @since 0.8.7
 */
public enum NCompensationStrategy implements NEnum {
    /**
     * Abort the entire saga when a compensation fails.
     * <p>
     * This strategy ensures strict consistency by halting execution immediately
     * if a rollback step cannot be completed successfully. The saga will be
     * marked as failed or partially rolled back depending on other conditions.
     */
    ABORT,


    /**
     * Ignore failed compensations and continue execution.
     * <p>
     * This strategy allows the saga to proceed even if some compensation steps
     * fail, accepting partial rollback or inconsistent state as a trade-off
     * for higher resilience or tolerance of non-critical operations.
     */
    IGNORE;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NCompensationStrategy() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    /**
     * Parses the given string into an {@link NCompensationStrategy} instance.
     * Matching is case-insensitive and supports common enum name variants.
     *
     * @param value string value to parse
     * @return an {@link NOptional} containing the parsed enum value if recognized
     */
    public static NOptional<NCompensationStrategy> parse(String value) {
        return NEnumUtils.parseEnum(value, NCompensationStrategy.class);
    }


    /**
     * Returns the lower-cased identifier for this strategy.
     *
     * @return the identifier string
     */
    @Override
    public String id() {
        return id;
    }
}
