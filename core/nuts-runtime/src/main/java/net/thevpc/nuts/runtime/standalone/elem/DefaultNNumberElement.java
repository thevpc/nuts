package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NStringUtils;

public class DefaultNNumberElement extends DefaultNPrimitiveElement implements NNumberElement {
    private NNumberLayout layout;
    private String suffix;

    public DefaultNNumberElement(NElementType type, Number value) {
        this(type, value, null,null,null,null);
    }
    public DefaultNNumberElement(NElementType type, Number value, NNumberLayout layout, String suffix) {
        this(type, value, layout, suffix,null,null);
    }

    public DefaultNNumberElement(NElementType type, Object value,
                                 NNumberLayout layout,
                                 String suffix,
                                 NElementAnnotation[] annotations, NElementComments comments) {
        super(type, value, annotations, comments);
        this.layout = layout == null ? NNumberLayout.DECIMAL : layout;
        this.suffix = NStringUtils.trimToNull(suffix);
    }

    @Override
    public Number numberValue() {
        return (Number) value();
    }

    @Override
    public NNumberLayout numberLayout() {
        return layout;
    }

    @Override
    public String numberSuffix() {
        return suffix;
    }
}
