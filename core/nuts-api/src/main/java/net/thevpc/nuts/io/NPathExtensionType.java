package net.thevpc.nuts.io;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;


/**
 * Represents the strategy used to extract the extension part of a file name in an {@code NPath}.
 *
 * <p>Each type defines a different way to interpret the extension:
 * <ul>
 *   <li>{@code SHORT} – Uses the last dot in the file name to determine the extension (e.g., {@code file.tar.gz} → {@code gz}).</li>
 *   <li>{@code LONG} – Uses the first dot in the file name to determine the extension (e.g., {@code file.tar.gz} → {@code tar.gz}).</li>
 *   <li>{@code SMART} – Uses a heuristic or version-aware logic to determine the extension, useful for versioned files (e.g., {@code lib-1.2.3.jar} → {@code jar}).</li>
 * </ul>
 * </p>
 */

public enum NPathExtensionType implements NEnum {

    /**
     * Extracts the extension using the last dot in the file name.
     */
    SHORT,

    /**
     * Extracts the extension using the first dot in the file name.
     */
    LONG,

    /**
     * Uses a smart, version-aware strategy to determine the extension.
     */
    SMART;
    private final String id;

    NPathExtensionType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }


    /**
     * Returns the formatted identifier for this extension type.
     *
     * @return the formatted ID string
     */
    @Override
    public String id() {
        return id;
    }


    /**
     * Parses a string value into an {@code NPathExtensionType}, if valid.
     *
     * @param value the string representation of the extension type
     * @return an optional containing the parsed enum value, or empty if invalid
     */
    public static NOptional<NPathExtensionType> parse(String value) {
        return NEnumUtils.parseEnum(value, NPathExtensionType.class);
    }
}
