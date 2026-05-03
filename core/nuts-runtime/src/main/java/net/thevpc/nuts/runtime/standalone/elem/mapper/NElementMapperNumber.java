package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementSerializerContext;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;

public class NElementMapperNumber implements NElementMapper<Number> {

    @Override
    public Object toSimple(NElementSerializerContext<Number> context) {
        return context.instance();
    }

    @Override
    public NElement toElement(NElementSerializerContext<Number> context) {
        return NElement.ofNumber(context.instance());
    }

    @Override
    public Number toObject(NElementDeserializerContext context) {
        Type to = context.instanceType();
        NElement element = context.element();
        if(to==null){
            to=Number.class;
        }
        switch (((Class) to).getName()) {
            case "byte":
            case "java.lang.Byte":
                return element.asLiteral().asByte().get();
            case "short":
            case "java.lang.Short":
                return element.asLiteral().asShort().get();
            case "int":
            case "java.lang.Integer":
                return element.asLiteral().asInt().get();
            case "long":
            case "java.lang.Long":
                return element.asLiteral().asLong().get();
            case "float":
            case "java.lang.Float":
                return element.asLiteral().asFloat().get();
            case "double":
            case "java.lang.Double":
                return element.asLiteral().asDouble().get();
            case "java.lang.BigDecimal":
                return new BigDecimal(element.asStringValue().get());
            case "java.lang.BigInteger":
                return new BigInteger(element.asStringValue().get());
        }
        if(Number.class.isAssignableFrom((Class) to)){
            return element.asLiteral().asNumber().get();
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
