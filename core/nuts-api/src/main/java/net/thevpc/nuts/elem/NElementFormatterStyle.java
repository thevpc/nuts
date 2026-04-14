package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * Defines the formatting strategies for {@link NElement} serialization in TSON.
 * <p>
 * These styles represent a layered hierarchy of rules, moving from raw data
 * preservation to structural reconstruction.
 */
public enum NElementFormatterStyle implements NEnum {
    /**
     * Preserves the original layout of the element as much as possible.
     * <p>
     * <b>Intervention:</b> Low. It writes exactly what is stored in the
     * element's affixes (whether parsed from source or set via Java builders).
     * It only injects whitespace for "Fatal" collisions where tokens would
     * merge and change the semantic meaning of the AST.
     * <p>
     * <i>Use case:</i> Maintaining Git-history or manual formatting in
     * configuration files.
     */
    VERBATIM,

    /**
     * Builds upon {@link #VERBATIM} by ensuring the output is visually clear
     * for human readers.
     * <p>
     * <b>Intervention:</b> Medium-Low. In addition to fixing fatal collisions,
     * it injects spacing for "Unpretty" collisions (e.g., between quotes and
     * identifiers). This is the recommended style for programmatically built
     * elements where the developer may have omitted manual spacing.
     * <p>
     * <i>Use case:</i> Standard programmatic serialization and logging.
     */
    STABLE,

    /**
     * Produces a clean, standalone representation of the element.
     * <p>
     * <b>Intervention:</b> Medium. Designed as the primary logic for {@code toString()}.
     * It builds on {@link #STABLE}, may inject missing structural separators (commas)
     * between elements to ensure a valid sequence, and strips "Root Garbage"
     * (prefix/suffix separators belonging to a parent).
     * <p>
     * <i>Use case:</i> CLI output, displaying individual property values, or
     * generating UI labels.
     */
    SIMPLE,

    /**
     * Produces the most dense valid TSON string.
     * <p>
     * <b>Intervention:</b> High. Strips all optional whitespace and all
     * non-essential separators (commas/semicolons). It reapplies only the
     * absolute minimum "Fatal" disambiguation required to ensure the string
     * remains parsable.
     * <p>
     * <i>Use case:</i> Network transmission or high-density data storage where
     * every byte counts.
     */
    COMPACT,

    /**
     * Performs a total structural reconstruction for maximum readability.
     * <p>
     * <b>Intervention:</b> Total. It ignores all existing affixes and
     * performs a structural reconstruction with consistent indentation,
     * column alignment, and complexity-based line wrapping.
     * <p>
     * <i>Use case:</i> Generating documentation, example files, or
     * auto-formatting a developer's configuration.
     */
    PRETTY,

    /**
     * Reserved for user-defined formatting logic.
     */
    CUSTOM,
    ;
    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NElementFormatterStyle() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NElementFormatterStyle> parse(String value) {
        return NEnumUtils.parseEnum(value, NElementFormatterStyle.class, s->{
            return null;
        });
    }

    /**
     * lower cased identifier.
     *
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }
}
