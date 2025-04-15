package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.builders;

import net.thevpc.nuts.runtime.standalone.format.tson.bundled.TsonCustomBuilder;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.TsonElement;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.TsonElementType;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.elements.TsonCustomImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.elements.TsonNullImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.util.TsonUtils;

public class TsonCustomElementBuilderImpl extends AbstractTsonElementBuilder<TsonCustomBuilder> implements TsonCustomBuilder {
    private Object value = null;

    @Override
    public TsonElementType type() {
        return TsonElementType.CUSTOM;
    }

    @Override
    public TsonCustomBuilder setCustom(Object value) {
        this.value = value;
        return this;
    }

    @Override
    public Object getCustom() {
        return value;
    }

    @Override
    public TsonElement build() {
        return TsonUtils.decorate(
                (value == null ? TsonNullImpl.INSTANCE : new TsonCustomImpl(value))
                , comments(), annotations())
                ;
    }

}
