package net.thevpc.nuts.runtime.standalone.elem.builder;

import net.thevpc.nuts.elem.NElementDiagnostic;
import net.thevpc.nuts.elem.NElementDiagnosticBuilder;
import net.thevpc.nuts.elem.NElementToken;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNElementDiagnostic;
import net.thevpc.nuts.text.NMsg;

public class DefaultNElementDiagnosticBuilder implements NElementDiagnosticBuilder {
    private NElementToken token;
    private NMsg message;

    public DefaultNElementDiagnosticBuilder() {
    }
    public DefaultNElementDiagnosticBuilder(NElementToken token, NMsg message) {
        this.token = token;
        this.message = message;
    }

    @Override
    public NMsg getMessage() {
        return message;
    }

    @Override
    public NElementDiagnosticBuilder setMessage(NMsg message) {
        this.message = message;
        return this;
    }

    @Override
    public NElementToken getToken() {
        return token;
    }

    @Override
    public NElementDiagnosticBuilder setToken(NElementToken message) {
        this.token = message;
        return this;
    }

    @Override
    public NElementDiagnostic build() {
        return new DefaultNElementDiagnostic(token, message);
    }
}
