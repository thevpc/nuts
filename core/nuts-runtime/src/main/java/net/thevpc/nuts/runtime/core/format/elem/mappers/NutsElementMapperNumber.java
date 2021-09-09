package net.thevpc.nuts.runtime.core.format.elem.mappers;

import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementFactoryContext;
import net.thevpc.nuts.NutsElementMapper;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;

public class NutsElementMapperNumber implements NutsElementMapper<Number> {

    @Override
    public Object destruct(Number src, Type typeOfSrc, NutsElementFactoryContext context) {
        return src;
    }

    @Override
    public NutsElement createElement(Number o, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.element().forNumber((Number) o);
    }

    @Override
    public Number createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
        switch (((Class) to).getName()) {
            case "byte":
            case "java.lang.Byte":
                return o.asPrimitive().getByte();
            case "short":
            case "java.lang.Short":
                return o.asPrimitive().getShort();
            case "int":
            case "java.lang.Integer":
                return o.asPrimitive().getInt();
            case "long":
            case "java.lang.Long":
                return o.asPrimitive().getShort();
            case "float":
            case "java.lang.Float":
                return o.asPrimitive().getShort();
            case "double":
            case "java.lang.Double":
                return o.asPrimitive().getShort();
            case "java.lang.BigDecimal":
                return new BigDecimal(o.asPrimitive().getString());
            case "java.lang.BigInteger":
                return new BigInteger(o.asPrimitive().getString());
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
