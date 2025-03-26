package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NStringUtils;

public class DefaultNStringElement extends DefaultNPrimitiveElement implements NStringElement {
    private NStringLayout stringLayout;

    public DefaultNStringElement(NElementType type, String value) {
        this(type, value, null, null, null);
    }

    public DefaultNStringElement(NElementType type, String value, NStringLayout layout) {
        this(type, value, layout, null, null);
    }

    public DefaultNStringElement(NElementType type, String value,
                                 NStringLayout layout,
                                 NElementAnnotation[] annotations, NElementComments comments) {
        super(type, value, annotations, comments);
        this.stringLayout = layout == null ? NStringLayout.DOUBLE_QUOTE : layout;
    }

    @Override
    public String stringValue() {
        return (String) value();
    }

    @Override
    public NStringLayout stringLayout() {
        return stringLayout;
    }

}
