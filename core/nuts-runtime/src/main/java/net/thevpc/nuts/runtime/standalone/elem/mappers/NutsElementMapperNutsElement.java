package net.thevpc.nuts.runtime.standalone.elem.mappers;

import net.thevpc.nuts.*;

import java.lang.reflect.Type;

public class NutsElementMapperNutsElement implements NutsElementMapper<NutsElement> {

    public NutsElementMapperNutsElement() {
    }

    @Override
    public Object destruct(NutsElement src, Type typeOfSrc, NutsElementFactoryContext context) {
        switch (src.type()) {
            case ARRAY:
                return context.objectToElement(src, NutsArrayElement.class);
            case OBJECT:
                return context.objectToElement(src, NutsObjectElement.class);
            default: {
                return context.objectToElement(src, NutsPrimitiveElement.class);
            }
        }
    }

    @Override
    public NutsElement createElement(NutsElement src, Type typeOfSrc, NutsElementFactoryContext context) {
        return src;
    }

    @Override
    public NutsElement createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        return o;
    }
}
