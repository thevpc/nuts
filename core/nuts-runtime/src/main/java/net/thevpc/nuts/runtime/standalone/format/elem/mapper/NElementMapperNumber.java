package net.thevpc.nuts.runtime.standalone.format.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;

public class NElementMapperNumber implements NElementMapper<Number> {

    @Override
    public Object destruct(Number src, Type typeOfSrc, NElementFactoryContext context) {
        return src;
    }

    @Override
    public NElement createElement(Number o, Type typeOfSrc, NElementFactoryContext context) {
        return NElement.ofNumber((Number) o);
    }

    @Override
    public Number createObject(NElement o, Type to, NElementFactoryContext context) {
        switch (((Class) to).getName()) {
            case "byte":
            case "java.lang.Byte":
                return o.asLiteral().asByte().get();
            case "short":
            case "java.lang.Short":
                return o.asLiteral().asShort().get();
            case "int":
            case "java.lang.Integer":
                return o.asLiteral().asInt().get();
            case "long":
            case "java.lang.Long":
                return o.asLiteral().asLong().get();
            case "float":
            case "java.lang.Float":
                return o.asLiteral().asFloat().get();
            case "double":
            case "java.lang.Double":
                return o.asLiteral().asDouble().get();
            case "java.lang.BigDecimal":
                return new BigDecimal(o.asStringValue().get());
            case "java.lang.BigInteger":
                return new BigInteger(o.asStringValue().get());
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
