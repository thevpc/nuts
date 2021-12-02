package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementFactoryContext;
import net.thevpc.nuts.NutsElementMapper;
import net.thevpc.nuts.NutsPrimitiveElement;

import java.lang.reflect.Type;

public class NutsElementMapperNutsPrimitiveElement implements NutsElementMapper<NutsPrimitiveElement> {

    public NutsElementMapperNutsPrimitiveElement() {
    }

    @Override
    public Object destruct(NutsPrimitiveElement src, Type typeOfSrc, NutsElementFactoryContext context) {
        return src.getValue();
    }

    @Override
    public NutsElement createElement(NutsPrimitiveElement src, Type typeOfSrc, NutsElementFactoryContext context) {
        return src;
    }

    @Override
    public NutsPrimitiveElement createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        if (o.type().isPrimitive()) {
            return o.asPrimitive();
        }
        return context.elem().ofString(o.toString());
    }
}
