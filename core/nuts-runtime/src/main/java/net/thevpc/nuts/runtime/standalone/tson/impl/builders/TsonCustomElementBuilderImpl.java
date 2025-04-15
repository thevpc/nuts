package net.thevpc.nuts.runtime.standalone.tson.impl.builders;

import net.thevpc.nuts.runtime.standalone.tson.*;
import net.thevpc.nuts.runtime.standalone.tson.impl.elements.TsonCustomImpl;
import net.thevpc.nuts.runtime.standalone.tson.impl.elements.TsonNullImpl;
import net.thevpc.nuts.runtime.standalone.tson.impl.util.TsonUtils;

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
