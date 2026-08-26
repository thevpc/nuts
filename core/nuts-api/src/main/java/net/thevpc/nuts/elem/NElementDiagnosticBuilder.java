package net.thevpc.nuts.elem;

import net.thevpc.nuts.text.NMsg;

/**
 * NElementDiagnosticBuilder interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NElementDiagnosticBuilder {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NElementDiagnosticBuilder of() {
        return NElement.ofDiagnosticBuilder();
    }

    /**
     * Message.
     *
     * @return message result
     */
    NMsg message();

    /**
     * Message.
     *
     * @param message message
     * @return message result
     */
    NElementDiagnosticBuilder message(NMsg message);

    /**
     * Converts to en.
     *
     * @return token result
     */
    NElementToken token();

    /**
     * Converts to en.
     *
     * @param message message
     * @return token result
     */
    NElementDiagnosticBuilder token(NElementToken message);
    /**
     * Build.
     *
     * @return build result
     */
    NElementDiagnostic build();
}
