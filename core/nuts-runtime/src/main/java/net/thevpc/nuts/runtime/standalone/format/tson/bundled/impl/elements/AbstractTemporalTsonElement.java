package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.elements;

import net.thevpc.nuts.runtime.standalone.format.tson.bundled.Tson;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.TsonElementType;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.TsonString;

public abstract class AbstractTemporalTsonElement extends AbstractPrimitiveTsonElement{
    public AbstractTemporalTsonElement(TsonElementType type) {
        super(type);
    }
    @Override
    public TsonString toStr() {
        return (TsonString) Tson.of(String.valueOf(temporalValue()));
    }
}
