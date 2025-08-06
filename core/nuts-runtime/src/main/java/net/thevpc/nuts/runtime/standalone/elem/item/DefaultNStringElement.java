package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NElementUtils;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

public class DefaultNStringElement extends DefaultNPrimitiveElement implements NStringElement {

    public DefaultNStringElement(NElementType type, String value) {
        this(type, value, null, null);
    }

    public DefaultNStringElement(NElementType type, String value,
                                 NElementAnnotation[] annotations, NElementComments comments) {
        super(type, value, annotations, comments);
        if (type == NElementType.NAME) {
            NAssert.requireTrue(NElementUtils.isValidElementName((String) value), "valid name : "+value);
        }
    }
    public DefaultNStringElement(NElementType type, Character value,
                                 NElementAnnotation[] annotations, NElementComments comments) {
        super(type, value, annotations, comments);
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

}
