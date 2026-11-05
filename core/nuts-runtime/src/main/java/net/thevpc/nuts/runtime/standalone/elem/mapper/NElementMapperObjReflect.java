package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.reflect.NReflectProperty;
import net.thevpc.nuts.reflect.NReflectType;
import net.thevpc.nuts.reflect.NReflectUtils;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.text.NMsg;

import java.lang.reflect.Modifier;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
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
    public Object toSimple(NElementSerializerContext<Object> context) {
        Class<?> c = NReflectUtils.getRawClass(context.instanceType()).get();
        if (isRecord(c)) {
            Map<String, Object> obj = new LinkedHashMap<>();
            for (RecordComponentData component : recordComponents(c)) {
                Object value = readRecordComponent(component, context.instance());
                obj.put(component.elementName, context.toSimple(value, component.genericType));
            }
            return obj;
        }
        NReflectType m = context.typesRepository().getType(context.instanceType());
        Map<String, Object> obj = new LinkedHashMap<>();
        for (NReflectProperty property : m.properties()) {
            final Object v = property.read(context.instance());
            if (!property.isDefaultValue(v)) {
                obj.put(property.name(), context.toSimple(v, null));
            }
        }
        return obj;
    }

    @Override
    public NElement toElement(NElementSerializerContext<Object> context) {
        Class<?> c = NReflectUtils.getRawClass(context.instanceType()).get();
        if (isRecord(c)) {
            NObjectElementBuilder obj = NElement.ofObjectBuilder();
            for (RecordComponentData component : recordComponents(c)) {
                Object value = readRecordComponent(component, context.instance());
                obj.set(component.elementName, context.toElement(value, component.genericType));
            }
            return obj.build();
        }
        NReflectType m = context.typesRepository().getType(context.instanceType());
        NObjectElementBuilder obj = NElement.ofObjectBuilder();
        for (NReflectProperty property : m.properties()) {
            final Object v = property.read(context.instance());
            if (!property.isDefaultValue(v)) {
                obj.set(property.name(), context.toElement(v));
            }
        }
        return obj.build();
    }

    @Override
    public Object toObject(NElementDeserializerContext context) {
        Type typeOfResult = context.instanceType();
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
        Object created = NElementCreatorResolver.create(element, c, context);
        if (created != NElementCreatorResolver.NO_CREATOR) {
            return created;
        }
        if (isRecord(c)) {
            return createRecord(c, element, context);
        }
        NReflectType m = context.typesRepository().getType(typeOfResult);
        Object instance = m.newInstance();
        NObjectElement eobj = element.asObject().get();
//        NElements prv = NElements;
        for (NReflectProperty property : m.properties()) {
            if (property.isWrite()) {
                NElement v = eobj.get(property.name()).orNull();
                if (v != null) {
                    property.write(instance, context.toObject(v, property.propertyType().javaType()));
                }
            }
        }
        return instance;
    }

    static boolean isRecord(Class<?> type) {
        try {
            return Boolean.TRUE.equals(Class.class.getMethod("isRecord").invoke(type));
        } catch (NoSuchMethodException ex) {
            return false;
        } catch (ReflectiveOperationException ex) {
            throw new NIllegalArgumentException(NMsg.ofC("unable to inspect record type %s", type.getName()), ex);
        }
    }

    private static RecordComponentData[] recordComponents(Class<?> type) {
        try {
            Object[] components = (Object[]) Class.class.getMethod("getRecordComponents").invoke(type);
            Class<?>[] parameterTypes = new Class<?>[components.length];
            Method getName = components.length == 0 ? null : components[0].getClass().getMethod("getName");
            Method getType = components.length == 0 ? null : components[0].getClass().getMethod("getType");
            Method getAccessor = components.length == 0 ? null : components[0].getClass().getMethod("getAccessor");
            for (int i = 0; i < components.length; i++) {
                parameterTypes[i] = (Class<?>) getType.invoke(components[i]);
            }
            Constructor<?> constructor = type.getDeclaredConstructor(parameterTypes);
            Parameter[] parameters = constructor.getParameters();
            RecordComponentData[] result = new RecordComponentData[components.length];
            for (int i = 0; i < components.length; i++) {
                String name = (String) getName.invoke(components[i]);
                String declared = declaredElementName(parameters[i], type, name);
                String elementName = declared == null ? name : declared;
                Method accessor = (Method) getAccessor.invoke(components[i]);
                accessor.setAccessible(true);
                result[i] = new RecordComponentData(
                        name,
                        elementName,
                        parameterTypes[i],
                        parameters[i].getParameterizedType(),
                        accessor
                );
            }
            return result;
        } catch (ReflectiveOperationException ex) {
            throw new NIllegalArgumentException(NMsg.ofC("unable to inspect record type %s", type.getName()), ex);
        }
    }

    /**
     * The element property name explicitly declared for a creator parameter, or null when the member
     * keeps its java name.
     * <p>
     * A record component declaration writes its annotations on the backing field, on the accessor and,
     * only when the canonical constructor is the implicit one, on the matching constructor parameter.
     * Reading the parameter alone would lose the declared name of a record that writes its canonical
     * constructor by hand, so the component field is used as a fallback.
     *
     * @param parameter     the creator parameter, may be null
     * @param owner         the declaring class of the creator
     * @param componentName the java name of the record component, null for a non record parameter
     * @return the declared name or null
     */
    static String declaredElementName(AnnotatedElement parameter, Class<?> owner, String componentName) {
        String name = declaredNameOn(parameter);
        if (name == null && componentName != null) {
            try {
                name = declaredNameOn(owner.getDeclaredField(componentName));
            } catch (NoSuchFieldException ex) {
                name = null;
            }
        }
        return name;
    }

    private static String declaredNameOn(AnnotatedElement element) {
        if (element == null) {
            return null;
        }
        NProperty property = element.getAnnotation(NProperty.class);
        return property != null && !property.value().trim().isEmpty() ? property.value() : null;
    }

    /**
     * The component names of the record whose canonical constructor matches the given signature, or null
     * when the declaring class is not a record or the signature is not the canonical one.
     *
     * @param owner          the declaring class of the creator
     * @param parameterTypes the creator parameter types
     * @return the component names or null
     */
    static String[] canonicalComponentNames(Class<?> owner, Class<?>[] parameterTypes) {
        if (!isRecord(owner)) {
            return null;
        }
        try {
            Object[] components = (Object[]) Class.class.getMethod("getRecordComponents").invoke(owner);
            if (components.length != parameterTypes.length) {
                return null;
            }
            String[] names = new String[components.length];
            if (components.length == 0) {
                return names;
            }
            Method getName = components[0].getClass().getMethod("getName");
            Method getType = components[0].getClass().getMethod("getType");
            for (int i = 0; i < components.length; i++) {
                if (!getType.invoke(components[i]).equals(parameterTypes[i])) {
                    return null;
                }
                names[i] = (String) getName.invoke(components[i]);
            }
            return names;
        } catch (ReflectiveOperationException ex) {
            return null;
        }
    }

    private static Object readRecordComponent(RecordComponentData component, Object instance) {
        try {
            return component.accessor.invoke(instance);
        } catch (ReflectiveOperationException ex) {
            throw new NIllegalArgumentException(
                    NMsg.ofC("unable to read record component %s", component.name),
                    unwrapInvocationException(ex)
            );
        }
    }

    private static Object createRecord(Class<?> type, NElement element, NElementDeserializerContext context) {
        if (!(element instanceof NObjectElement)) {
            throw new NIllegalArgumentException(NMsg.ofC(
                    "expected an object to create record %s, got %s",
                    type.getName(),
                    element.type()
            ));
        }
        RecordComponentData[] components = recordComponents(type);
        Class<?>[] parameterTypes = new Class<?>[components.length];
        Object[] arguments = new Object[components.length];
        NObjectElement object = (NObjectElement) element;
        for (int i = 0; i < components.length; i++) {
            RecordComponentData component = components[i];
            parameterTypes[i] = component.type;
            NElement value = object.get(component.elementName).orNull();
            if (value == null) {
                arguments[i] = primitiveDefault(component.type);
            } else if (value.isNull() && component.type.isPrimitive()) {
                throw new NIllegalArgumentException(NMsg.ofC(
                        "null is not valid for record component %s of %s",
                        component.elementName,
                        type.getName()
                ));
            } else {
                arguments[i] = context.toObject(value, component.genericType);
            }
        }
        try {
            Constructor<?> constructor = type.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor.newInstance(arguments);
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause() == null ? ex : ex.getCause();
            throw new NIllegalArgumentException(NMsg.ofC("unable to create record %s", type.getName()), cause);
        } catch (ReflectiveOperationException ex) {
            throw new NIllegalArgumentException(NMsg.ofC("unable to create record %s", type.getName()), ex);
        }
    }

    static Object primitiveDefault(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == char.class) {
            return '\0';
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0F;
        }
        return 0D;
    }

    private static Throwable unwrapInvocationException(ReflectiveOperationException ex) {
        if (ex instanceof InvocationTargetException && ((InvocationTargetException) ex).getCause() != null) {
            return ((InvocationTargetException) ex).getCause();
        }
        return ex;
    }

    private static final class RecordComponentData {
        private final String name;
        private final String elementName;
        private final Class<?> type;
        private final Type genericType;
        private final Method accessor;

        private RecordComponentData(String name, String elementName, Class<?> type, Type genericType, Method accessor) {
            this.name = name;
            this.elementName = elementName;
            this.type = type;
            this.genericType = genericType;
            this.accessor = accessor;
        }
    }
}
