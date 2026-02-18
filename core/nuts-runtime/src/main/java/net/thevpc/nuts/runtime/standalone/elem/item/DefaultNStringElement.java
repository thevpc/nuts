package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.elem.NElementUtils;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.List;

public class DefaultNStringElement extends DefaultNPrimitiveElement implements NStringElement {
    private String rawValue;

    public DefaultNStringElement(NElementType type, String value) {
        this(type, value, value, null,null,null);
    }

    public DefaultNStringElement(NElementType type, String value, String rawValue) {
        this(type, value, rawValue, null,null,null);
    }

    public DefaultNStringElement(NElementType type, String value,
                                 String rawValue,
                                 List<NBoundAffix> affixes, List<NElementDiagnostic> diagnostics,NElementMetadata metadata) {
        super(type, value, affixes,diagnostics,metadata);
        this.rawValue = rawValue;
        if (type == NElementType.NAME) {
            NAssert.requireNamedTrue(NElementUtils.isValidElementName((String) value), "valid name : " + value);
        }
    }

    public DefaultNStringElement(NElementType type, Character value) {
        this(type, value, null,null,null);
    }
    public DefaultNStringElement(NElementType type, Character value,
                                 List<NBoundAffix> affixes, List<NElementDiagnostic> diagnostics,NElementMetadata metadata) {
        super(type, value, affixes,diagnostics,metadata);
        if (type != NElementType.CHAR) {
            throw new NIllegalArgumentException(NMsg.ofC("expected character"));
        }
    }

    @Override
    public String stringValue() {
        return String.valueOf(value());
    }

    @Override
    public NOptional<NStringElement> asString() {
        return NOptional.of(this);
    }

    @Override
    public String literalString() {
        return asLiteral().toStringLiteral();
    }
}
