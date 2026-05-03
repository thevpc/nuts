package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.reflect.NReflectProperty;
import net.thevpc.nuts.reflect.NReflectType;
import net.thevpc.nuts.reflect.NReflectUtils;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.text.NMsg;

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
    public Object toSimple(Object src, Type typeOfSrc, NElementFactoryContext context) {
        NReflectType m = context.getTypesRepository().getType(typeOfSrc);
        Map<String, Object> obj = new LinkedHashMap<>();
        for (NReflectProperty property : m.getProperties()) {
            final Object v = property.read(src);
            if (!property.isDefaultValue(v)) {
                obj.put(property.getName(), context.toSimple(v, null));
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
                obj.set(property.getName(), context.toElement(v));
            }
        }
        return obj.build();
    }

    @Override
    public Object createObject(NElementDeserializerContext context) {
        Type typeOfResult = context.to();
        NElement element = context.element();
//        NSession session = context.getSession();
        Class c = NReflectUtils.getRawClass(typeOfResult).get();
        switch (element.type()) {
            case NULL: {
                return null;
            }
            case DOUBLE_QUOTED_STRING:
            case SINGLE_QUOTED_STRING:
            case BACKTICK_STRING:
            case TRIPLE_DOUBLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING:
            case TRIPLE_BACKTICK_STRING:
            case LINE_STRING:
            case BLOCK_STRING:
            {
                if (c.isAssignableFrom(String.class)) {
                    return element.asStringValue().orNull();
                }
                break;
            }
            case BOOLEAN: {
                if (c.isAssignableFrom(Boolean.class)) {
                    return element.asLiteral().asBoolean();
                }
                break;
            }
            case DOUBLE: {
                if (c.isAssignableFrom(Double.class)) {
                    return element.asLiteral().asDouble();
                }
                break;
            }
            case FLOAT: {
                if (c.isAssignableFrom(Float.class)) {
                    return element.asLiteral().asFloat();
                }
                break;
            }
            case BYTE: {
                if (c.isAssignableFrom(Byte.class)) {
                    return element.asLiteral().asByte();
                }
                break;
            }
            case BIG_DECIMAL: {
                if (c.isAssignableFrom(BigDecimal.class)) {
                    return element.asLiteral().asNumber();
                }
                break;
            }
            case BIG_INT: {
                if (c.isAssignableFrom(BigInteger.class)) {
                    return element.asLiteral().asNumber();
                }
                break;
            }
            case LONG: {
                if (c.isAssignableFrom(Long.class)) {
                    return element.asLiteral().asLong();
                }
                break;
            }
            case SHORT: {
                if (c.isAssignableFrom(Short.class)) {
                    return element.asLiteral().asShort();
                }
                break;
            }
            case INT: {
                if (c.isAssignableFrom(Integer.class)) {
                    return element.asLiteral().asInt();
                }
                break;
            }
            case INSTANT: {
                if (c.isAssignableFrom(Instant.class)) {
                    return element.asLiteral().asInstant();
                }
                break;
            }
            case ARRAY: {
                if (c.isAssignableFrom(List.class)) {
                    return context.toObject(element,List.class);
                }
                break;
            }
            case OBJECT: {
                if (c.equals(Object.class)) {
                    return context.toObject(element,Map.class);
                }
                break;
            }
            case CUSTOM:{
                return c.cast(element.asCustom().get().value());
            }
        }
        int mod = c.getModifiers();
        if (Modifier.isAbstract(mod)) {
            throw new NIllegalArgumentException(NMsg.ofC("cannot instantiate abstract class %s", typeOfResult));
        }
        NReflectType m = context.getTypesRepository().getType(typeOfResult);
        Object instance = m.newInstance();
        NObjectElement eobj = element.asObject().get();
//        NElements prv = NElements;
        for (NReflectProperty property : m.getProperties()) {
            if (property.isWrite()) {
                NElement v = eobj.get(property.getName()).orNull();
                if (v != null) {
                    property.write(instance, context.toObject(v, property.getPropertyType().getJavaType()));
                }
            }
        }
        return instance;
    }

}
