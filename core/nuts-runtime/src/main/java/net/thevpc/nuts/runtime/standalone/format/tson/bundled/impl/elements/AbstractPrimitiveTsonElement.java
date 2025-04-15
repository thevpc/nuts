package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.elements;

import net.thevpc.nuts.runtime.standalone.format.tson.bundled.TsonElementType;
import net.thevpc.nuts.runtime.standalone.format.tson.bundled.TsonParserVisitor;

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
