package net.thevpc.nuts.runtime.standalone.tson.impl.builders;

import net.thevpc.nuts.runtime.standalone.tson.*;
import net.thevpc.nuts.runtime.standalone.tson.impl.elements.TsonOpImpl;
import net.thevpc.nuts.runtime.standalone.tson.impl.elements.TsonNullImpl;
import net.thevpc.nuts.runtime.standalone.tson.impl.util.TsonUtils;

public class TsonOpBuilderImpl extends AbstractTsonElementBuilder<TsonOpBuilder> implements TsonOpBuilder {
    private String opName;
    private TsonOpType opType;
    private TsonElement first;
    private TsonElement second;

    public String opName() {
        return opName;
    }

    public TsonOpType opType() {
        return opType;
    }

    public TsonOpBuilder opName(String opName) {
        this.opName = opName;
        return this;
    }

    public TsonOpBuilder opType(TsonOpType opType) {
        this.opType = opType;
        return this;
    }

    @Override
    public TsonElementType type() {
        return TsonElementType.OP;
    }

    @Override
    public TsonOpBuilderImpl merge(TsonOp other) {
        first = other.first();
        second = other.second();
        opName = other.opName();
        opType = other.opType();
        return this;
    }

    @Override
    public TsonOpBuilder reset() {
        first = null;
        second = null;
        opType = null;
        opName = null;
        return this;
    }

    @Override
    public TsonElement first() {
        return first;
    }

    @Override
    public TsonOpBuilder first(TsonElementBase key) {
        this.first = Tson.of(key);
        return this;
    }


    @Override
    public TsonElement second() {
        return second;
    }

    @Override
    public TsonOpBuilder second(TsonElementBase value) {
        this.second = Tson.of(value);
        return this;
    }


    @Override
    public TsonOp build() {
        return (TsonOp) TsonUtils.decorate(
                new TsonOpImpl(
                        opName,
                        opType == null ? TsonOpType.BINARY : opType,
                        first == null ? TsonNullImpl.INSTANCE : first,
                        second == null ? TsonNullImpl.INSTANCE : second
                ), comments(), annotations());
    }


}
