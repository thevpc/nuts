package net.thevpc.nuts.runtime.core.format.elem.mappers;

import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementFactoryContext;
import net.thevpc.nuts.NutsElementMapper;

import java.lang.reflect.Type;

public class NutsElementMapperNull implements NutsElementMapper<Object> {

    @Override
    public Object destruct(Object src, Type typeOfSrc, NutsElementFactoryContext context) {
        return null;
    }

    @Override
    public NutsElement createElement(Object o, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.element().forNull();
    }

    @Override
    public Object createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
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
