package net.thevpc.nuts.elem;

import net.thevpc.nuts.text.NMsg;

public interface NElementDiagnosticBuilder {
    static NElementDiagnosticBuilder of() {
        return NElement.ofDiagnosticBuilder();
    }

    NMsg getMessage();

    NElementDiagnosticBuilder setMessage(NMsg message);

    NElementToken getToken();

    NElementDiagnosticBuilder setToken(NElementToken message);
    NElementDiagnostic build();
}
