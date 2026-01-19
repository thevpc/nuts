package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.NElementDiagnostic;
import net.thevpc.nuts.elem.NElementDiagnosticBuilder;
import net.thevpc.nuts.elem.NElementToken;
import net.thevpc.nuts.runtime.standalone.elem.builder.DefaultNElementDiagnosticBuilder;
import net.thevpc.nuts.text.NMsg;

public class DefaultNElementDiagnostic implements NElementDiagnostic {
    private NElementToken token;
    private NMsg message;

    public DefaultNElementDiagnostic(NElementToken token, NMsg message) {
        this.token = token;
        this.message = message;
    }

    @Override
    public NMsg message() {
        return message;
    }

    @Override
    public NElementToken token() {
        return token;
    }

    @Override
    public NElementDiagnosticBuilder builder() {
        return new DefaultNElementDiagnosticBuilder(token, message);
    }
}
