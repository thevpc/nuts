package net.thevpc.nuts.runtime.standalone.elem.mappers;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.reflect.ReflectProperty;
import net.thevpc.nuts.runtime.bundles.reflect.ReflectType;
import net.thevpc.nuts.runtime.bundles.reflect.ReflectUtils;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNutsElementFactoryService;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

public class NutsElementMapperObjReflect implements NutsElementMapper<Object> {

    private final DefaultNutsElementFactoryService defaultNutsElementFactoryService;

    public NutsElementMapperObjReflect(DefaultNutsElementFactoryService defaultNutsElementFactoryService) {
        this.defaultNutsElementFactoryService = defaultNutsElementFactoryService;
    }

    @Override
    public Object destruct(Object src, Type typeOfSrc, NutsElementFactoryContext context) {
        ReflectType m = defaultNutsElementFactoryService.getTypesRepository().getType(typeOfSrc);
        Map<String, Object> obj = new LinkedHashMap<>();
        for (ReflectProperty property : m.getProperties()) {
            final Object v = property.read(src);
            if (!property.isDefaultValue(v)) {
                obj.put(property.getName(), context.destruct(v, null));
            }
        }
        return obj;
    }

    @Override
    public NutsElement createElement(Object src, Type typeOfSrc, NutsElementFactoryContext context) {
        ReflectType m = defaultNutsElementFactoryService.getTypesRepository().getType(typeOfSrc);
        NutsObjectElementBuilder obj = context.elem().ofObject();
        for (ReflectProperty property : m.getProperties()) {
            final Object v = property.read(src);
            if (!property.isDefaultValue(v)) {
                obj.set(property.getName(), context.objectToElement(v, null));
            }
        }
        return obj.build();
    }

    @Override
    public Object createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        Class c = ReflectUtils.getRawClass(typeOfResult);
        int mod = c.getModifiers();
        if (Modifier.isAbstract(mod)) {
            throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.cstyle("cannot instantiate abstract class %s", typeOfResult));
        }
        ReflectType m = defaultNutsElementFactoryService.getTypesRepository().getType(typeOfResult);
        Object instance;
        if (m.hasSessionConstructor()) {
            instance = m.newInstance(context.getSession());
        } else {
            instance = m.newInstance();
        }
        NutsObjectElement eobj = o.asObject();
        NutsElements prv = context.elem();
        for (ReflectProperty property : m.getProperties()) {
            if (property.isWrite()) {
                NutsElement v = eobj.get(prv.ofString(property.getName()));
                if (v != null) {
                    property.write(instance, context.elementToObject(v, property.getPropertyType()));
                }
            }
        }
        return instance;
    }

}
