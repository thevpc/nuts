package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.NSession;

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
        return context.elem().ofNumber((Number) o);
    }

    @Override
    public Number createObject(NElement o, Type to, NElementFactoryContext context) {
        NSession session = context.getSession();
        switch (((Class) to).getName()) {
            case "byte":
            case "java.lang.Byte":
                return o.asByteValue().get();
            case "short":
            case "java.lang.Short":
                return o.asShortValue().get();
            case "int":
            case "java.lang.Integer":
                return o.asIntValue().get();
            case "long":
            case "java.lang.Long":
                return o.asLongValue().get();
            case "float":
            case "java.lang.Float":
                return o.asFloatValue().get();
            case "double":
            case "java.lang.Double":
                return o.asDoubleValue().get();
            case "java.lang.BigDecimal":
                return new BigDecimal(o.asStringValue().get());
            case "java.lang.BigInteger":
                return new BigInteger(o.asStringValue().get());
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
