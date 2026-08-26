package net.thevpc.nuts.elem;

import net.thevpc.nuts.internal.rpi.NElementRPI;
import net.thevpc.nuts.text.NNewLineMode;

/**
 * NElementLine interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NElementLine {
    /**
     * Creates a new instance of of element line.
     *
     * @param prefix prefix
     * @param startMarker start marker
     * @param startPadding start padding
     * @param content content
     * @param endPadding end padding
     * @param endMarker end marker
     * @param newline newline
     * @return of element line result
     */
    static NElementLine ofElementLine(String prefix, String startMarker, String startPadding, String content, String endPadding, String endMarker, NNewLineMode newline) {
        return NElementRPI.of().createElementLine(prefix, startMarker, startPadding, content, endPadding, endMarker, newline);
    }

    /**
     * Creates a new instance of of element line.
     *
     * @param prefix prefix
     * @param startPadding start padding
     * @param content content
     * @param endPadding end padding
     * @param newline newline
     * @return of element line result
     */
    static NElementLine ofElementLine(String prefix, String startPadding, String content, String endPadding, NNewLineMode newline) {
        return NElementRPI.of().createElementLine(prefix, null, startPadding, content, endPadding, null, newline);
    }

    /**
     * Prefix.
     *
     * @return prefix result
     */
    String prefix();

    /**
     * Start marker.
     *
     * @return start marker result
     */
    String startMarker();

    /**
     * End marker.
     *
     * @return end marker result
     */
    String endMarker();

    /**
     * Start padding.
     *
     * @return start padding result
     */
    String startPadding();

    /**
     * End padding.
     *
     * @return end padding result
     */
    String endPadding();

    /**
     * Content.
     *
     * @return content result
     */
    String content();

    /**
     * Newline.
     *
     * @return newline result
     */
    NNewLineMode newline();

    /**
     * With newline.
     *
     * @param nl nl
     * @return with newline result
     */
    NElementLine withNewline(NNewLineMode nl);
}
