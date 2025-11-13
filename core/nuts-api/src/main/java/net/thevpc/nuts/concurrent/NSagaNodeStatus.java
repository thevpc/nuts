package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * Represents the execution status of a saga node.
 * <p>
 * Each node in a saga workflow has a status that indicates its current state
 * in the execution lifecycle. This includes normal progression, failure, and
 * compensation handling.
 * <p>
 * Status values:
 * <ul>
 *     <li>{@link #PENDING} – node has not yet started execution</li>
 *     <li>{@link #RUNNING} – node is currently executing</li>
 *     <li>{@link #FINISHED} – node has successfully finished execution</li>
 *     <li>{@link #FAILED} – node execution failed</li>
 *     <li>{@link #IGNORED} – node failed but its compensation strategy is IGNORE</li>
 *     <li>{@link #COMPENSATING} – node is undergoing compensation (rollback)</li>
 *     <li>{@link #COMPENSATED} – node has been successfully compensated</li>
 *     <li>{@link #COMPENSATION_FAILED} – node compensation failed</li>
 *     <li>{@link #COMPENSATION_IGNORED} – node compensation was ignored due to strategy</li>
 * </ul>
 * <p>
 * Implements {@link NEnum} to provide a lower-cased {@link #id()} identifier
 * suitable for serialization, logging, or display purposes.
 * <p>
 * Parsing from string is supported via {@link #parse(String)}.
 *
 * @since 0.8.7
 */
public enum NSagaNodeStatus implements NEnum {
    /**
     * ode has not yet started execution
     */
    PENDING,
    /**
     * node is currently executing
     */
    RUNNING,
    /**
     * node has successfully finished execution
     */
    FINISHED,
    /**
     * node execution failed
     */
    FAILED,
    /**
     * node failed but its compensation strategy is IGNORE
     */
    IGNORED,
    /**
     * node is undergoing compensation (rollback)
     */
    COMPENSATING,
    /**
     * node has been successfully compensated
     */
    COMPENSATED,
    /**
     * node compensation failed
     */
    COMPENSATION_FAILED,
    /**
     * node compensation was ignored due to strategy
     */
    COMPENSATION_IGNORED;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NSagaNodeStatus() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NSagaNodeStatus> parse(String value) {
        return NEnumUtils.parseEnum(value, NSagaNodeStatus.class);
    }

    @Override
    public String id() {
        return id;
    }
}
