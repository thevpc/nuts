package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NCreator;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.util.NOptional;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves the {@link NCreator} members of a type once and applies them to an element.
 * <p>
 * The whole creator semantics lives here : candidate collection, element parameter matching,
 * ambiguity detection, property binding and the {@code NOptional} decline protocol.
 * {@link NElementMapperObjReflect} only asks this class to create an instance and falls back to
 * the default mapping when the type declares no creator.
 *
 * @author thevpc
 * @since 1.1.0
 */
public class NElementCreatorResolver {

    /**
     * Returned by {@link #create} when no creator applies : the caller is then free to use the
     * default mapping.
     */
    public static final Object NO_CREATOR = new Object();

    /**
     * The only accepted static factory method name.
     */
    public static final String FACTORY_METHOD_NAME = "of";

    private static final int CACHE_LIMIT = 512;
    private static final Map<Class<?>, Resolution> cache = new ConcurrentHashMap<>();

    private NElementCreatorResolver() {
    }

    /**
     * Create an instance of the given type from the given element using its declared creators.
     * <p>
     * Returns {@link #NO_CREATOR} when the type declares no creator, or when every declared creator
     * declined the element and the default mapping is still able to handle it.
     *
     * @param element source element
     * @param type    target type
     * @param context deserialization context
     * @return the created instance or {@link #NO_CREATOR}
     */
    public static Object create(NElement element, Class<?> type, NElementDeserializerContext context) {
        if (element == null) {
            return NO_CREATOR;
        }
        Resolution resolution = resolutionOf(type);
        if (resolution.isEmpty()) {
            return NO_CREATOR;
        }
        Object value = resolution.create(element, type, context);
        if (value != NO_CREATOR) {
            if (value != null && !type.isInstance(value)) {
                throw new NIllegalArgumentException(NMsg.ofC(
                        "cannot map %s element to %s : the creator returned an instance of %s",
                        element.type(), type.getName(), value.getClass().getName()));
            }
            return value;
        }
        // every creator declined the element : the default mapping is the next strategy
        if (canUseDefaultMapping(type, context)) {
            return NO_CREATOR;
        }
        throw new NIllegalArgumentException(NMsg.ofC(
                "no creator accepted the %s element for type %s", element.type(), type.getName()));
    }

    private static boolean canUseDefaultMapping(Class<?> type, NElementDeserializerContext context) {
        if (NElementMapperObjReflect.isRecord(type)) {
            return true;
        }
        return context.typesRepository().getType(type).hasNoArgsConstructor();
    }

    private static Resolution resolutionOf(Class<?> type) {
        Resolution resolution = cache.get(type);
        if (resolution == null) {
            try {
                resolution = build(type);
            } catch (NIllegalArgumentException ex) {
                resolution = new Resolution(null, null, ex);
            }
            if (cache.size() >= CACHE_LIMIT) {
                cache.clear();
            }
            cache.put(type, resolution);
        }
        return resolution;
    }

    private static Resolution build(Class<?> type) {
        boolean record = NElementMapperObjReflect.isRecord(type);
        List<Creator> elementCreators = new ArrayList<>();
        List<Creator> propertyCreators = new ArrayList<>();
        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            Creator creator = creatorOf(constructor, record, true);
            if (creator != null) {
                (creator.elementType == null ? propertyCreators : elementCreators).add(creator);
            }
        }
        for (Method method : type.getDeclaredMethods()) {
            if (!Modifier.isStatic(method.getModifiers()) || !FACTORY_METHOD_NAME.equals(method.getName())) {
                continue;
            }
            Creator creator = creatorOf(method, record, false);
            if (creator != null) {
                (creator.elementType == null ? propertyCreators : elementCreators).add(creator);
            }
        }
        if (propertyCreators.size() > 1) {
            // several property bound creators share the same parameter shape
            selectFirstOrThrow(type, propertyCreators);
        }
        return new Resolution(elementCreators, propertyCreators, null);
    }

    private static Creator creatorOf(Executable member, boolean record, boolean constructor) {
        if (member.isSynthetic()) {
            return null;
        }
        boolean declared = member.getAnnotation(NCreator.class) != null;
        if (!declared && record && constructor) {
            // a record always keeps its canonical constructor mapping unless it is explicitly declared
            return null;
        }
        Parameter[] parameters = member.getParameters();
        boolean elementParameter = parameters.length == 1
                && NElement.class.isAssignableFrom(member.getParameterTypes()[0]);
        if (!declared && !elementParameter) {
            // an unannotated property bound creator is never a candidate
            return null;
        }
        Class<?> owner = member.getDeclaringClass();
        Type declaredReturn;
        boolean optionalResult = false;
        if (constructor) {
            declaredReturn = owner;
        } else {
            declaredReturn = ((Method) member).getGenericReturnType();
            optionalResult = isNOptional(declaredReturn);
            Class<?> resultType = rawResultTypeOf(declaredReturn);
            if (resultType == null || !resultType.isAssignableFrom(owner)) {
                if (declared) {
                    throw new NIllegalArgumentException(NMsg.ofC(
                            "%s of %s is declared as a creator but returns %s instead of %s",
                            describe(member), owner.getName(), declaredReturn, owner.getName()));
                }
                return null;
            }
        }
        Creator creator = new Creator();
        creator.member = member;
        creator.constructor = constructor;
        creator.optionalResult = optionalResult;
        creator.parameterTypes = member.getGenericParameterTypes();
        if (elementParameter) {
            creator.elementType = member.getParameterTypes()[0];
        } else {
            String[] componentNames = constructor
                    ? NElementMapperObjReflect.canonicalComponentNames(owner, member.getParameterTypes())
                    : null;
            creator.names = new String[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                creator.names[i] = nameOf(member, parameters[i], i,
                        componentNames == null ? null : componentNames[i]);
            }
        }
        return creator;
    }

    /**
     * Resolve the property name bound to a creator parameter.
     *
     * @param member        the creator
     * @param parameter     the creator parameter
     * @param index         the parameter index, used in error messages
     * @param componentName the record component name when the creator is a canonical constructor
     */
    private static String nameOf(Executable member, Parameter parameter, int index, String componentName) {
        String declared = NElementMapperObjReflect.declaredElementName(
                parameter, member.getDeclaringClass(), componentName);
        if (declared != null) {
            return declared;
        }
        if (parameter.isNamePresent()) {
            return parameter.getName();
        }
        throw new NIllegalArgumentException(NMsg.ofC(
                "unable to map the parameter %d of the creator %s : no @NProperty name is declared"
                        + " and the java parameter name is not available", index, describe(member)));
    }

    private static boolean isNOptional(Type type) {
        if (type instanceof ParameterizedType) {
            Type raw = ((ParameterizedType) type).getRawType();
            return raw instanceof Class && NOptional.class.equals(raw);
        }
        return false;
    }

    private static Class<?> rawResultTypeOf(Type type) {
        if (isNOptional(type)) {
            Type[] args = ((ParameterizedType) type).getActualTypeArguments();
            if (args.length != 1) {
                return null;
            }
            type = args[0];
        }
        if (type instanceof ParameterizedType) {
            type = ((ParameterizedType) type).getRawType();
        }
        if (type instanceof Class) {
            return (Class<?>) type;
        }
        return null;
    }

    private static String describe(Executable member) {
        StringBuilder sb = new StringBuilder();
        sb.append(member.getDeclaringClass().getName());
        if (member instanceof Constructor) {
            sb.append("<init>");
        } else {
            sb.append('.').append(member.getName());
        }
        sb.append('(');
        Class<?>[] parameters = member.getParameterTypes();
        for (int i = 0; i < parameters.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(parameters[i].getSimpleName());
        }
        return sb.append(')').toString();
    }

    private static NIllegalArgumentException ambiguity(Class<?> type, List<Creator> candidates) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < candidates.size(); i++) {
            if (i > 0) {
                sb.append(" ; ");
            }
            sb.append(describe(candidates.get(i).member));
        }
        return new NIllegalArgumentException(NMsg.ofC(
                "unable to map %s : %d creators share the same parameter shape [%s]",
                type.getName(), candidates.size(), sb));
    }

    /**
     * This is the single place where the first candidate of an ambiguous group could be taken
     * instead of failing. It always fails, as required by the creator resolution rules.
     *
     * @param type       target type
     * @param candidates the ambiguous candidates
     * @return nothing, this method always throws
     */
    private static Creator selectFirstOrThrow(Class<?> type, List<Creator> candidates) {
        throw ambiguity(type, candidates);
    }

    private static class Creator {
        Executable member;
        boolean constructor;
        boolean optionalResult;
        Class<?> elementType;
        String[] names;
        Type[] parameterTypes;

        boolean accepts(NElement element) {
            return elementType == null || elementType.isInstance(element);
        }

        boolean moreSpecificThan(Creator other) {
            return other.elementType != null && elementType != other.elementType
                    && other.elementType.isAssignableFrom(elementType);
        }

        Object invoke(NElement element, NElementDeserializerContext context) {
            Object result;
            try {
                result = call(elementType == null ? bind(element, context) : new Object[]{element});
            } catch (InvocationTargetException ex) {
                Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                throw creatorFailure(cause);
            } catch (ReflectiveOperationException ex) {
                throw creatorFailure(ex);
            }
            if (optionalResult) {
                NOptional<?> optional = (NOptional<?>) result;
                if (optional == null || !optional.isPresent()) {
                    // the creator declines this element
                    return NO_CREATOR;
                }
                return optional.orNull();
            }
            return result;
        }

        private NIllegalArgumentException creatorFailure(Throwable ex) {
            // the element public APIs may drop the exception cause, so keep it in the message
            return new NIllegalArgumentException(NMsg.ofC(
                    "creator %s failed to create an instance of %s : %s",
                    describe(member), member.getDeclaringClass().getName(), ex), ex);
        }

        private Object call(Object[] args) throws ReflectiveOperationException {
            if (constructor) {
                Constructor<?> c = (Constructor<?>) member;
                c.setAccessible(true);
                return c.newInstance(args);
            }
            Method m = (Method) member;
            m.setAccessible(true);
            return m.invoke(null, args);
        }

        private Object[] bind(NElement element, NElementDeserializerContext context) {
            Class<?>[] parameterTypes = member.getParameterTypes();
            Object[] args = new Object[names.length];
            NObjectElement object = element.asObject().orNull();
            if (object == null) {
                throw new NIllegalArgumentException(NMsg.ofC(
                        "cannot map %s element to %s : the creator %s expects an object element",
                        element.type(), member.getDeclaringClass().getName(), describe(member)));
            }
            for (int i = 0; i < args.length; i++) {
                NElement v = object.get(names[i]).orNull();
                if (v == null) {
                    args[i] = NElementMapperObjReflect.primitiveDefault(parameterTypes[i]);
                } else if (v.isNull() && parameterTypes[i].isPrimitive()) {
                    throw new NIllegalArgumentException(NMsg.ofC(
                            "null is not valid for the parameter %d (%s) of the creator %s",
                            i, names[i], describe(member)));
                } else {
                    args[i] = context.toObject(v, parameterTypes[i]);
                }
            }
            return args;
        }
    }

    private static class Resolution {
        private final List<Creator> elementCreators;
        private final List<Creator> propertyCreators;
        private final NIllegalArgumentException error;

        Resolution(List<Creator> elementCreators, List<Creator> propertyCreators,
                   NIllegalArgumentException error) {
            this.elementCreators = elementCreators == null
                    ? Collections.<Creator>emptyList() : elementCreators;
            this.propertyCreators = propertyCreators == null
                    ? Collections.<Creator>emptyList() : propertyCreators;
            this.error = error;
        }

        boolean isEmpty() {
            return error == null && elementCreators.isEmpty() && propertyCreators.isEmpty();
        }

        Object create(NElement element, Class<?> type, NElementDeserializerContext context) {
            if (error != null) {
                throw error;
            }
            if (!elementCreators.isEmpty()) {
                // an element parameter creator wins over the property bound ones
                List<Creator> accepted = new ArrayList<>();
                for (Creator c : elementCreators) {
                    if (c.accepts(element)) {
                        accepted.add(c);
                    }
                }
                if (accepted.isEmpty()) {
                    throw new NIllegalArgumentException(NMsg.ofC(
                            "cannot map %s element to %s : creator expects %s",
                            element.type(), type.getName(), expectedKinds()));
                }
                List<Creator> mostSpecific = new ArrayList<>();
                for (Creator c : accepted) {
                    boolean dominated = false;
                    for (Creator other : accepted) {
                        if (other != c && other.moreSpecificThan(c)) {
                            dominated = true;
                            break;
                        }
                    }
                    if (!dominated) {
                        mostSpecific.add(c);
                    }
                }
                if (mostSpecific.size() > 1) {
                    // this is where the first candidate could be taken instead of failing
                    selectFirstOrThrow(type, mostSpecific);
                }
                Object value = mostSpecific.get(0).invoke(element, context);
                if (value != NO_CREATOR) {
                    return value;
                }
                // the element creator declined this element, the property bound ones are the next strategy
            }
            if (!propertyCreators.isEmpty()) {
                return propertyCreators.get(0).invoke(element, context);
            }
            return NO_CREATOR;
        }

        private String expectedKinds() {
            List<String> kinds = new ArrayList<>();
            for (Creator c : elementCreators) {
                String kind = c.elementType.getSimpleName();
                if (!kinds.contains(kind)) {
                    kinds.add(kind);
                }
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < kinds.size(); i++) {
                if (i > 0) {
                    sb.append(" or ");
                }
                sb.append(kinds.get(i));
            }
            return sb.toString();
        }
    }
}
