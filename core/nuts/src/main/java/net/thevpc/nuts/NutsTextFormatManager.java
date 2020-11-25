package net.thevpc.nuts;

import java.io.PrintStream;
import java.io.Reader;
import java.util.Formatter;
import java.util.Locale;

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


    /**
     * text length after filtering all special characters
     * @param value value for length evaluation
     * @return effective length after filtering the text
     */

    int textLength(String value);

    /**
     * this method removes all special "nuts print format" sequences support
     * and returns the raw string to be printed on an
     * ordinary {@link PrintStream}
     *
     * @param value input string
     * @return string without any escape sequences so that the text printed
     * correctly on any non formatted {@link PrintStream}
     */
    String filterText(String value);

    /**
     * This method escapes all special characters that are interpreted by
     * "nuts print format" o that this exact string is printed on
     * such print streams When str is null, an empty string is return
     *
     * @param value input string
     * @return string with escaped characters so that the text printed correctly
     * on a "nuts print format" aware print stream
     */
    String escapeText(String value);

    /**
     * format string. supports {@link Formatter#format(java.util.Locale, java.lang.String, java.lang.Object...)
     * }
     * pattern format and adds NutsString special format to print unfiltered strings.
     *
     *
     * @param style style
     * @param locale locale
     * @param format format
     * @param args arguments
     * @return formatted string
     */
    String formatText(NutsTextFormatStyle style, Locale locale, String format, Object... args);

    /**
     * format string. supports {@link Formatter#format(java.lang.String, java.lang.Object...)
     * }
     * pattern format and adds NutsString special format to print unfiltered strings.
     *
     *
     * @param style format style
     * @param format format
     * @param args arguments
     * @return formatted string
     */
    String formatText(NutsTextFormatStyle style, String format, Object... args);

}
