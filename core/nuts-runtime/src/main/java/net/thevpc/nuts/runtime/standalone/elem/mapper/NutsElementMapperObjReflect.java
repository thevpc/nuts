package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.reflect.ReflectProperty;
import net.thevpc.nuts.runtime.standalone.util.reflect.ReflectType;
import net.thevpc.nuts.runtime.standalone.util.reflect.ReflectUtils;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNutsElementFactoryService;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
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
        NutsSession session = context.getSession();
        Class c = ReflectUtils.getRawClass(typeOfResult);
        switch (o.type()) {
            case NULL: {
                return null;
            }
            case STRING: {
                if (c.isAssignableFrom(String.class)) {
                    return o.asString().orNull();
                }
                break;
            }
            case BOOLEAN: {
                if (c.isAssignableFrom(Boolean.class)) {
                    return o.asBoolean();
                }
                break;
            }
            case DOUBLE: {
                if (c.isAssignableFrom(Double.class)) {
                    return o.asDouble();
                }
                break;
            }
            case FLOAT: {
                if (c.isAssignableFrom(Float.class)) {
                    return o.asFloat();
                }
                break;
            }
            case BYTE: {
                if (c.isAssignableFrom(Byte.class)) {
                    return o.asByte();
                }
                break;
            }
            case BIG_DECIMAL: {
                if (c.isAssignableFrom(BigDecimal.class)) {
                    return o.asNumber();
                }
                break;
            }
            case BIG_INTEGER: {
                if (c.isAssignableFrom(BigInteger.class)) {
                    return o.asNumber();
                }
                break;
            }
            case LONG: {
                if (c.isAssignableFrom(Long.class)) {
                    return o.asLong();
                }
                break;
            }
            case SHORT: {
                if (c.isAssignableFrom(Short.class)) {
                    return o.asShort();
                }
                break;
            }
            case INTEGER: {
                if (c.isAssignableFrom(Integer.class)) {
                    return o.asInt();
                }
                break;
            }
            case INSTANT: {
                if (c.isAssignableFrom(Instant.class)) {
                    return o.asInstant();
                }
                break;
            }
            case ARRAY: {
                if (c.isAssignableFrom(List.class)) {
                    return context.elementToObject(o,List.class);
                }
                break;
            }
            case OBJECT: {
                if (c.equals(Object.class)) {
                    return context.elementToObject(o,Map.class);
                }
                break;
            }
            case CUSTOM:{
                return c.cast(o.asCustom().get(session).getValue());
            }
        }
        int mod = c.getModifiers();
        if (Modifier.isAbstract(mod)) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("cannot instantiate abstract class %s", typeOfResult));
        }
        ReflectType m = defaultNutsElementFactoryService.getTypesRepository().getType(typeOfResult);
        Object instance;
        if (m.hasSessionConstructor()) {
            instance = m.newInstance(session);
        } else {
            instance = m.newInstance();
        }
        NutsObjectElement eobj = o.asObject().get(session);
        NutsElements prv = context.elem();
        for (ReflectProperty property : m.getProperties()) {
            if (property.isWrite()) {
                NutsElement v = eobj.get(property.getName()).orNull();
                if (v != null) {
                    property.write(instance, context.elementToObject(v, property.getPropertyType()));
                }
            }
        }
        return instance;
    }

}
