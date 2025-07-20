package net.thevpc.nuts.runtime.standalone.format.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.reflect.NReflectProperty;
import net.thevpc.nuts.reflect.NReflectType;
import net.thevpc.nuts.runtime.standalone.util.reflect.ReflectUtils;
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


    public NElementMapperObjReflect() {
    }

    @Override
    public Object destruct(Object src, Type typeOfSrc, NElementFactoryContext context) {
        NReflectType m = context.getTypesRepository().getType(typeOfSrc);
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
        NReflectType m = context.getTypesRepository().getType(typeOfSrc);
        NObjectElementBuilder obj = NElement.ofObjectBuilder();
        for (NReflectProperty property : m.getProperties()) {
            final Object v = property.read(src);
            if (!property.isDefaultValue(v)) {
                obj.set(property.getName(), context.createElement(v));
            }
        }
        return obj.build();
    }

    @Override
    public Object createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
//        NSession session = context.getSession();
        Class c = ReflectUtils.getRawClass(typeOfResult);
        switch (o.type()) {
            case NULL: {
                return null;
            }
            case DOUBLE_QUOTED_STRING:
            case SINGLE_QUOTED_STRING:
            case ANTI_QUOTED_STRING:
            case TRIPLE_DOUBLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING:
            case TRIPLE_ANTI_QUOTED_STRING:
            case LINE_STRING:
            {
                if (c.isAssignableFrom(String.class)) {
                    return o.asStringValue().orNull();
                }
                break;
            }
            case BOOLEAN: {
                if (c.isAssignableFrom(Boolean.class)) {
                    return o.asLiteral().asBoolean();
                }
                break;
            }
            case DOUBLE: {
                if (c.isAssignableFrom(Double.class)) {
                    return o.asLiteral().asDouble();
                }
                break;
            }
            case FLOAT: {
                if (c.isAssignableFrom(Float.class)) {
                    return o.asLiteral().asFloat();
                }
                break;
            }
            case BYTE: {
                if (c.isAssignableFrom(Byte.class)) {
                    return o.asLiteral().asByte();
                }
                break;
            }
            case BIG_DECIMAL: {
                if (c.isAssignableFrom(BigDecimal.class)) {
                    return o.asLiteral().asNumber();
                }
                break;
            }
            case BIG_INT: {
                if (c.isAssignableFrom(BigInteger.class)) {
                    return o.asLiteral().asNumber();
                }
                break;
            }
            case LONG: {
                if (c.isAssignableFrom(Long.class)) {
                    return o.asLiteral().asLong();
                }
                break;
            }
            case SHORT: {
                if (c.isAssignableFrom(Short.class)) {
                    return o.asLiteral().asShort();
                }
                break;
            }
            case INT: {
                if (c.isAssignableFrom(Integer.class)) {
                    return o.asLiteral().asInt();
                }
                break;
            }
            case INSTANT: {
                if (c.isAssignableFrom(Instant.class)) {
                    return o.asLiteral().asInstant();
                }
                break;
            }
            case ARRAY: {
                if (c.isAssignableFrom(List.class)) {
                    return context.createObject(o,List.class);
                }
                break;
            }
            case OBJECT: {
                if (c.equals(Object.class)) {
                    return context.createObject(o,Map.class);
                }
                break;
            }
            case CUSTOM:{
                return c.cast(o.asCustom().get().value());
            }
        }
        int mod = c.getModifiers();
        if (Modifier.isAbstract(mod)) {
            throw new NIllegalArgumentException(NMsg.ofC("cannot instantiate abstract class %s", typeOfResult));
        }
        NReflectType m = context.getTypesRepository().getType(typeOfResult);
        Object instance = m.newInstance();
        NObjectElement eobj = o.asObject().get();
//        NElements prv = NElements;
        for (NReflectProperty property : m.getProperties()) {
            if (property.isWrite()) {
                NElement v = eobj.get(property.getName()).orNull();
                if (v != null) {
                    property.write(instance, context.createObject(v, property.getPropertyType().getJavaType()));
                }
            }
        }
        return instance;
    }

}
