package net.thevpc.nuts;

import java.io.PrintStream;
import java.io.Reader;

/**
 * @category Format
 */
public interface NutsTextFormatManager {
    NutsTextFormatTheme getTheme();
    NutsTextNodeFactory factory();

    NutsTextNodeBuilder builder();

    NutsTextNode parse(String t);

    NutsTextNodeParser parser();


}
