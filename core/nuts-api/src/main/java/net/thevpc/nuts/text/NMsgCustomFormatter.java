package net.thevpc.nuts.text;

import net.thevpc.nuts.spi.NComponent;

import java.util.List;

/**
 * NMsgCustomFormatter interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NMsgCustomFormatter extends NComponent {
    /**
     * Id.
     *
     * @return id result
     */
    String id();

    /**
     * Format.
     *
     * @param msg msg
     * @return format result
     */
    NText format(NMsg msg);

    /**
     * Extract params.
     *
     * @param message message
     * @return extract params result
     */
    List<String> extractParams(String message);
}
