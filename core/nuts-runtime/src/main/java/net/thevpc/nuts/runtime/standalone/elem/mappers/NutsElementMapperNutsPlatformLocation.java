package net.thevpc.nuts.runtime.standalone.elem.mappers;

import net.thevpc.nuts.*;

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
        NutsObjectElement obj = o.asObject();
        NutsElements _prm = context.elem();
        NutsId id = context.elementToObject(obj.get(_prm.ofString("id")), NutsId.class);
        String product = context.elementToObject(obj.get(_prm.ofString("product")), String.class);
        String name = context.elementToObject(obj.get(_prm.ofString("name")), String.class);
        String path = context.elementToObject(obj.get(_prm.ofString("path")), String.class);
        String version = context.elementToObject(obj.get(_prm.ofString("version")), String.class);
        String packaging = context.elementToObject(obj.get(_prm.ofString("packaging")), String.class);
        int priority = context.elementToObject(obj.get(_prm.ofString("priority")), int.class);
        return new NutsPlatformLocation(id, product, name, path, version, packaging, priority);
    }

}
