package net.thevpc.nuts.runtime.standalone.format.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.*;

import java.lang.reflect.Type;

public class NElementMapperNPlatformLocation implements NElementMapper<NPlatformLocation> {

    @Override
    public Object destruct(NPlatformLocation src, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultDestruct(src, null);
    }

    @Override
    public NElement createElement(NPlatformLocation o, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultObjectToElement(o, null);
    }

    @Override
    public NPlatformLocation createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        NObjectElement obj = o.asObject().get();
        NId id = context.elementToObject(obj.get("id").orElse(NElements.ofString("")), NId.class);
        String product = context.elementToObject(obj.get("product").orElse(NElements.ofString("")), String.class);
        String name = context.elementToObject(obj.get("name").orElse(NElements.ofString("")), String.class);
        String path = context.elementToObject(obj.get("path").orElse(NElements.ofString("")), String.class);
        String version = context.elementToObject(obj.get("version").orElse(NElements.ofString("")), String.class);
        String packaging = context.elementToObject(obj.get("packaging").orElse(NElements.ofString("")), String.class);
        int priority = context.elementToObject(obj.get("priority").orElse(NElements.ofInt(0)), int.class);
        return new NPlatformLocation(id, product, name, path, version, packaging, priority);
    }

}
