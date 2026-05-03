package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.platform.NExecutionEngineLocation;

import java.lang.reflect.Type;

public class NElementMapperNPlatformLocation implements NElementMapper<NExecutionEngineLocation> {

    @Override
    public Object toSimple(NExecutionEngineLocation src, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultToSimple(src, null);
    }

    @Override
    public NElement createElement(NExecutionEngineLocation o, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultCreateElement(o, null);
    }

    @Override
    public NExecutionEngineLocation createObject(NElementDeserializerContext context) {
        NObjectElement obj = context.element().asObject().get();
        NId id = context.toObject(obj.get("id").orElse(NElement.ofString("")), NId.class);
        String product = context.toObject(obj.get("product").orElse(NElement.ofString("")), String.class);
        String vendor = context.toObject(obj.get("vendor").orElse(NElement.ofString("")), String.class);
        String variant = context.toObject(obj.get("variant").orElse(NElement.ofString("")), String.class);
        String name = context.toObject(obj.get("name").orElse(NElement.ofString("")), String.class);
        String path = context.toObject(obj.get("path").orElse(NElement.ofString("")), String.class);
        String version = context.toObject(obj.get("version").orElse(NElement.ofString("")), String.class);
        String packaging = context.toObject(obj.get("packaging").orElse(NElement.ofString("")), String.class);
        int priority = context.toObject(obj.get("priority").orElse(NElement.ofInt(0)), int.class);
        return new NExecutionEngineLocation(id, vendor, product, variant, name, path, version, packaging, priority);
    }

}
