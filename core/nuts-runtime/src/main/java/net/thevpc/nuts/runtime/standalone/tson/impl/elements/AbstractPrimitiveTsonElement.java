package net.thevpc.nuts.runtime.standalone.tson.impl.elements;

import net.thevpc.nuts.runtime.standalone.tson.TsonElementType;
import net.thevpc.nuts.runtime.standalone.tson.TsonParserVisitor;

public abstract class AbstractPrimitiveTsonElement extends AbstractTsonElement {
    public AbstractPrimitiveTsonElement(TsonElementType type) {
        super(type);
    }

    protected <T> T throwPrimitive(TsonElementType type){
        throw new ClassCastException(type()+" Cannot cast to "+type);
    }

    protected <T> T throwNonPrimitive(TsonElementType type){
        throw new ClassCastException(type()+" cannot be cast to "+type);
    }

    @Override
    public void visit(TsonParserVisitor visitor) {
        visitor.visitElementStart();
        visitor.visitPrimitiveEnd(this);
    }
}
