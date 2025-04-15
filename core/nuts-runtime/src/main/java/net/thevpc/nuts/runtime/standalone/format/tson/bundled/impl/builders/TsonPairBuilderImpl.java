package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.builders;

import net.thevpc.nuts.runtime.standalone.format.tson.bundled.*;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.elements.TsonNullImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.elements.TsonPairImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.util.TsonUtils;

public class TsonPairBuilderImpl extends AbstractTsonElementBuilder<TsonPairBuilder> implements TsonPairBuilder {
    private TsonElement key;
    private TsonElement value;

    @Override
    public TsonElementType type() {
        return TsonElementType.PAIR;
    }

    @Override
    public TsonPairBuilder merge(TsonPair other) {
        key = other.key();
        value = other.value();
        return this;
    }

    @Override
    public TsonPairBuilder reset() {
        key = null;
        value = null;
        return this;
    }

    @Override
    public TsonElement key() {
        return key;
    }

    @Override
    public TsonPairBuilder key(TsonElementBase key) {
        this.key = Tson.of(key);
        return this;
    }


    @Override
    public TsonElement value() {
        return value;
    }

    @Override
    public TsonPairBuilder value(TsonElementBase value) {
        this.value = Tson.of(value);
        return this;
    }


    @Override
    public TsonPair build() {
        return (TsonPair) TsonUtils.decorate(
                new TsonPairImpl(
                        key == null ? TsonNullImpl.INSTANCE : key,
                        value == null ? TsonNullImpl.INSTANCE : value
                ), comments(), annotations());
    }


}
