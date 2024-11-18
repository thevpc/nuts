package net.thevpc.nuts.runtime.standalone.elem.mapper;

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
        NSession session = context.getSession();
        NObjectElement obj = o.asObject().get();
        NElements _prm = context.elem();
        NId id = context.elementToObject(obj.get("id").orElse(_prm.ofString("")), NId.class);
        String product = context.elementToObject(obj.get("product").orElse(_prm.ofString("")), String.class);
        String name = context.elementToObject(obj.get("name").orElse(_prm.ofString("")), String.class);
        String path = context.elementToObject(obj.get("path").orElse(_prm.ofString("")), String.class);
        String version = context.elementToObject(obj.get("version").orElse(_prm.ofString("")), String.class);
        String packaging = context.elementToObject(obj.get("packaging").orElse(_prm.ofString("")), String.class);
        int priority = context.elementToObject(obj.get("priority").orElse(_prm.ofInt(0)), int.class);
        return new NPlatformLocation(id, product, name, path, version, packaging, priority);
    }

}
