package net.thevpc.nuts.elem;

import net.thevpc.nuts.text.NNewLineMode;

/**
 * NElementFormatOptions interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NElementFormatOptions {
    /**
     * Complexity threshold.
     *
     * @return complexity threshold result
     */
    int complexityThreshold() ;
    /**
     * Indent.
     *
     * @return indent result
     */
    int indent() ;
    /**
     * Column limit.
     *
     * @return column limit result
     */
    int columnLimit();

    /**
     * New line mode.
     *
     * @return new line mode result
     */
    NNewLineMode newLineMode();
}
