package net.thevpc.nuts.runtime.standalone.format.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.elem.NElements;

import java.lang.reflect.Type;

public class NElementMapperNull implements NElementMapper<Object> {

    @Override
    public Object destruct(Object src, Type typeOfSrc, NElementFactoryContext context) {
        return null;
    }

    @Override
    public NElement createElement(Object o, Type typeOfSrc, NElementFactoryContext context) {
        return NElements.ofNull();
    }

    @Override
    public Object createObject(NElement o, Type to, NElementFactoryContext context) {
        if (to instanceof Class) {
            Class c = (Class) to;
            if (c.isPrimitive()) {
                switch (c.getName()) {
                    case "boolean":
                        return false;
                    case "byte":
                        return (byte) 0;
                    case "short":
                        return (short) 0;
                    case "int":
                        return 0;
                    case "char":
                        return (char) 0;
                    case "long":
                        return (long) 0;
                    case "float":
                        return (float) 0;
                    case "double":
                        return (double) 0;
                }
            }
        }
        return null;
    }

}
