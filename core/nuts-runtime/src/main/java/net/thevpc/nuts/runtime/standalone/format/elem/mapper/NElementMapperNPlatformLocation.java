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
        return context.defaultCreateElement(o, null);
    }

    @Override
    public NPlatformLocation createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        NObjectElement obj = o.asObject().get();
        NId id = context.createObject(obj.get("id").orElse(NElement.ofString("")), NId.class);
        String product = context.createObject(obj.get("product").orElse(NElement.ofString("")), String.class);
        String name = context.createObject(obj.get("name").orElse(NElement.ofString("")), String.class);
        String path = context.createObject(obj.get("path").orElse(NElement.ofString("")), String.class);
        String version = context.createObject(obj.get("version").orElse(NElement.ofString("")), String.class);
        String packaging = context.createObject(obj.get("packaging").orElse(NElement.ofString("")), String.class);
        int priority = context.createObject(obj.get("priority").orElse(NElement.ofInt(0)), int.class);
        return new NPlatformLocation(id, product, name, path, version, packaging, priority);
    }

}
