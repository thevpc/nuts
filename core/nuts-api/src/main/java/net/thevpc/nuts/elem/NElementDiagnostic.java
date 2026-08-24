package net.thevpc.nuts.elem;

import net.thevpc.nuts.text.NMsg;

/**
 * NElementDiagnostic interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NElementDiagnostic {
    /**
     * Message.
     *
     * @return message result
     */
    NMsg message();
    /**
     * Converts to en.
     *
     * @return token result
     */
    NElementToken token();
    /**
     * Builder.
     *
     * @return builder result
     */
    NElementDiagnosticBuilder builder();
}
