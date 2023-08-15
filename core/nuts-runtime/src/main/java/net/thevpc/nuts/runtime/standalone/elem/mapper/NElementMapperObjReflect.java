package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.reflect.NReflectProperty;
import net.thevpc.nuts.reflect.NReflectType;
import net.thevpc.nuts.runtime.standalone.util.reflect.ReflectUtils;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElementFactoryService;
import net.thevpc.nuts.util.NMsg;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NElementMapperObjReflect implements NElementMapper<Object> {

    private final DefaultNElementFactoryService defaultNutsElementFactoryService;

    public NElementMapperObjReflect(DefaultNElementFactoryService defaultNutsElementFactoryService) {
        this.defaultNutsElementFactoryService = defaultNutsElementFactoryService;
    }

    @Override
    public Object destruct(Object src, Type typeOfSrc, NElementFactoryContext context) {
        NReflectType m = defaultNutsElementFactoryService.getTypesRepository().getType(typeOfSrc);
        Map<String, Object> obj = new LinkedHashMap<>();
        for (NReflectProperty property : m.getProperties()) {
            final Object v = property.read(src);
            if (!property.isDefaultValue(v)) {
                obj.put(property.getName(), context.destruct(v, null));
            }
        }
        return obj;
    }

    @Override
    public NElement createElement(Object src, Type typeOfSrc, NElementFactoryContext context) {
        NReflectType m = defaultNutsElementFactoryService.getTypesRepository().getType(typeOfSrc);
        NObjectElementBuilder obj = context.elem().ofObject();
        for (NReflectProperty property : m.getProperties()) {
            final Object v = property.read(src);
            if (!property.isDefaultValue(v)) {
                obj.set(property.getName(), context.objectToElement(v, null));
            }
        }
        return obj.build();
    }

    @Override
    public Object createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        NSession session = context.getSession();
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
            throw new NIllegalArgumentException(session, NMsg.ofC("cannot instantiate abstract class %s", typeOfResult));
        }
        NReflectType m = defaultNutsElementFactoryService.getTypesRepository().getType(typeOfResult);
        Object instance;
        if (m.hasSessionConstructor()) {
            instance = m.newInstance(session);
        } else {
            instance = m.newInstance();
        }
        NObjectElement eobj = o.asObject().get(session);
        NElements prv = context.elem();
        for (NReflectProperty property : m.getProperties()) {
            if (property.isWrite()) {
                NElement v = eobj.get(property.getName()).orNull();
                if (v != null) {
                    property.write(instance, context.elementToObject(v, property.getPropertyType()));
                }
            }
        }
        return instance;
    }

}
