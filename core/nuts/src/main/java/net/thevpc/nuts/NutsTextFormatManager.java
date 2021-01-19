package net.thevpc.nuts;

import java.io.PrintStream;
import java.io.Reader;

/**
 * @category Format
 */
public interface NutsTextFormatManager {
    /**
     * load resource as a formatted string to be used mostly as a help string.
     *
     * @param reader      resource reader
     * @param classLoader class loader
     * @return formatted string (in Nuts Stream Format)
     */
    String loadFormattedString(Reader reader, ClassLoader classLoader);

    /**
     * load resource as a formatted string to be used mostly as a help string.
     *
     * @param resourcePath resource path
     * @param classLoader  class loader
     * @param defaultValue default value if the loading fails
     * @return formatted string (in Nuts Stream Format)
     */
    String loadFormattedString(String resourcePath, ClassLoader classLoader, String defaultValue);

    NutsTextNodeFactory factory();

    NutsTextNodeBuilder builder();

    NutsTextNode parse(String t);

    /**
     * converts any object to a NutsString instance.
     * special processing is supporting the following types:
     * <ul>
     *     <li>null</li>
     *     <li>NutsTextNode</li>
     *     <li>NutsFormattable</li>
     *     <li>NutsMessage</li>
     * </ul>
     * @param instance instance
     * @param session session
     * @return new instance of NutsString
     */
    NutsString of(Object instance, NutsSession session);

    NutsString of(Object instance);

    NutsTextNodeParser parser();

    NutsTitleNumberSequence createTitleNumberSequence();

    NutsTitleNumberSequence createTitleNumberSequence(String pattern);


}
