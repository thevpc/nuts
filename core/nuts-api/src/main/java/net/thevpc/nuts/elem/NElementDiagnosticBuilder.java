package net.thevpc.nuts.elem;

import net.thevpc.nuts.text.NMsg;

public interface NElementDiagnosticBuilder {
    static NElementDiagnosticBuilder of() {
        return NElement.ofDiagnosticBuilder();
    }

    NMsg message();

    NElementDiagnosticBuilder message(NMsg message);

    NElementToken token();

    NElementDiagnosticBuilder token(NElementToken message);
    NElementDiagnostic build();
}
