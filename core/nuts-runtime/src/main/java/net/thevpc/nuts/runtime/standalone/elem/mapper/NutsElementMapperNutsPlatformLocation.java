package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.*;

import java.lang.reflect.Type;

public class NutsElementMapperNutsPlatformLocation implements NutsElementMapper<NutsPlatformLocation> {

    @Override
    public Object destruct(NutsPlatformLocation src, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.defaultDestruct(src, null);
    }

    @Override
    public NutsElement createElement(NutsPlatformLocation o, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.defaultObjectToElement(o, null);
    }

    @Override
    public NutsPlatformLocation createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        NutsObjectElement obj = o.asObject().get(session);
        NutsElements _prm = context.elem();
        NutsId id = context.elementToObject(obj.get("id").orElse(_prm.ofString("")), NutsId.class);
        String product = context.elementToObject(obj.get("product").orElse(_prm.ofString("")), String.class);
        String name = context.elementToObject(obj.get("name").orElse(_prm.ofString("")), String.class);
        String path = context.elementToObject(obj.get("path").orElse(_prm.ofString("")), String.class);
        String version = context.elementToObject(obj.get("version").orElse(_prm.ofString("")), String.class);
        String packaging = context.elementToObject(obj.get("packaging").orElse(_prm.ofString("")), String.class);
        int priority = context.elementToObject(obj.get("priority").orElse(_prm.ofInt(0)), int.class);
        return new NutsPlatformLocation(id, product, name, path, version, packaging, priority);
    }

}
