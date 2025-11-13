package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * Represents the type of a saga node, determining its execution behavior.
 * <p>
 * Implements {@link NEnum} for lower-cased IDs and parsing support via {@link #parse(String)}.
 *
 * @since 0.8.7
 */
public enum NSagaNodeType implements NEnum {
    /**
     * An executable step node that performs a single operation.
     */
    STEP,
    /**
     * A conditional node that executes its children only if a specified {@link NSagaCondition} evaluates to true.
     */
    IF,
    /**
     * A looping node that repeatedly executes its children while a {@link NSagaCondition} evaluates to true.
     */
    WHILE,
    /**
     * A container node that groups multiple child nodes sequentially.
     * Used internally to organize a sequence of steps.
     */
    SUITE,
    /**
     * A node used for compensating or rolling back previous steps.
     */
    UNDO;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NSagaNodeType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    /**
     * Parses a string value into the corresponding {@link NSagaNodeType}.
     *
     * @param value the string representation of the node type
     * @return an {@link NOptional} containing the node type if found, or empty if not
     */
    public static NOptional<NSagaNodeType> parse(String value) {
        return NEnumUtils.parseEnum(value, NSagaNodeType.class);
    }

    @Override
    public String id() {
        return id;
    }
}
