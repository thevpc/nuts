package net.thevpc.nuts.runtime.standalone.tson.impl.elements;

import net.thevpc.nuts.runtime.standalone.tson.Tson;
import net.thevpc.nuts.runtime.standalone.tson.TsonElementType;
import net.thevpc.nuts.runtime.standalone.tson.TsonString;

public abstract class AbstractTemporalTsonElement extends AbstractPrimitiveTsonElement{
    public AbstractTemporalTsonElement(TsonElementType type) {
        super(type);
    }
    @Override
    public TsonString toStr() {
        return (TsonString) Tson.of(String.valueOf(temporalValue()));
    }
}
