package net.thevpc.nuts.elem;

import net.thevpc.nuts.text.NMsg;

public interface NElementDiagnostic {
    NMsg message();
    NElementToken token();
    NElementDiagnosticBuilder builder();
}
