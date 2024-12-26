package net.thevpc.nuts.runtime.standalone.util.reflect.mapper;

import java.lang.reflect.*;
import java.util.*;

public class TypeHelper {
    public interface ObjFactory<T>{
        T newInstance();
    }

    public static <T> ObjFactory<T> constructorOf(Class<T> t){
        Constructor<T> c;
        try {
            c = t.getConstructor();
            c.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return new ObjFactory<T>() {
            @Override
            public T newInstance() {
                try {
                    return c.newInstance();
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
    public static boolean isLoadableClass(String className) {
        return isLoadableClass(className, null);
    }

    public static boolean isLoadableClass(String className, ClassLoader cl) {
        try {
            if (cl == null) {
                Class.forName(className);
                return true;
            } else {
                Class.forName(className, true, cl);
                return true;
            }
        } catch (Throwable ex) {
            return false;
        }
    }

    public static Object getFieldValue(Object obj, String name) {
        Field declaredField = null;
        try {
            declaredField = obj.getClass().getDeclaredField(name);
            declaredField.setAccessible(true);
            return declaredField.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setFieldValue(Object obj, String name,Object value) {
        Field declaredField = null;
        try {
            declaredField = obj.getClass().getDeclaredField(name);
            declaredField.setAccessible(true);
            declaredField.set(obj,value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    public static GenericField[] getDeclaredFields(Type type) {
        if (type instanceof Class) {
            List<GenericField> fields = new ArrayList<>();
            for (Field declaredField : ((Class<?>) type).getDeclaredFields()) {
                fields.add(new GenericField(declaredField, declaredField.getGenericType()));
            }
            return fields.toArray(new GenericField[0]);
        } else if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType instanceof Class) {
                TypeVariable<? extends Class<?>>[] typeParameters = ((Class<?>) rawType).getTypeParameters();
                Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
                List<GenericField> fields = new ArrayList<>();
                for (Field declaredField : ((Class<?>) rawType).getDeclaredFields()) {
                    if (!Modifier.isStatic(declaredField.getModifiers())) {
                        Type genericType = declaredField.getGenericType();
                        for (int i = 0; i < typeParameters.length; i++) {
                            if (typeParameters[i] == genericType) {
                                genericType = actualTypeArguments[i];
                                break;
                            }
                        }
                        fields.add(new GenericField(declaredField, genericType));
                    } else {
                        fields.add(new GenericField(declaredField, declaredField.getGenericType()));
                    }
                }
                return fields.toArray(new GenericField[0]);
            }
        }
        throw new IllegalArgumentException("Not supported");
    }

    public static Type getGenericParent(ParameterizedType childRawType, Type parent) {
        if (parent == null) {
            return null;
        }
        if (parent instanceof Class) {
            return parent;
        } else if (parent instanceof ParameterizedType) {
            ParameterizedType pParent=(ParameterizedType) parent;
            Type pParentRaw = pParent.getRawType();
            TypeVariable[] parentTypeParameters = null;
            TypeVariable[] childTypeParameters = null;
            Type[] parentActualTypeArguments = pParent.getActualTypeArguments();
            Type[] parentActualTypeArguments2 = new Type[parentActualTypeArguments.length];
            Type[] childActualTypeArguments = childRawType.getActualTypeArguments();
            Type childRaw = childRawType.getRawType();
            if (childRaw instanceof Class && pParentRaw instanceof Class) {
                Class cc=(Class) childRaw;
                childTypeParameters = cc.getTypeParameters();
                parentTypeParameters = ((Class) pParentRaw).getTypeParameters();
                for (int i = 0; i < parentActualTypeArguments2.length; i++) {
                    Type e = parentActualTypeArguments[i];
                    if (e instanceof TypeVariable<?>) {
                        for (int j = 0; j < childTypeParameters.length; j++) {
                            TypeVariable t = childTypeParameters[j];
                            Type f = childActualTypeArguments[j];
                            if (t == e) {
                                e = f;
                            }
                        }
                    }
                    parentActualTypeArguments2[i] = e;
                }
                return new ParameterizedTypeImpl(parentActualTypeArguments2, (Class<?>) pParentRaw, pParent);
            } else {
                throw new IllegalArgumentException("Not supported yet");
            }
        } else {
            throw new IllegalArgumentException("Not supported yet");
        }
    }

    public static Type getGenericSuperclass(Type type) {
        if (type instanceof Class) {
            return ((Class<?>) type).getGenericSuperclass();
        }
        if (type instanceof ParameterizedType) {
            return getGenericParent((ParameterizedType) type, getGenericSuperclass(((ParameterizedType) type).getRawType()));
        } else {
            throw new IllegalArgumentException("Not Supported yet");
        }
    }

    public static TypeVariable[] getTypeArguments(Type type) {
        if (type instanceof Class) {
            return ((Class<?>) type).getTypeParameters();
        }
        return new TypeVariable[0];
    }

    public static Optional<Class> rawClass(Type type) {
        Type c = type;
        while (c != null) {
            if (c instanceof Class) {
                return Optional.of((Class) c);
            } else if (c instanceof ParameterizedType) {
                c = ((ParameterizedType) c).getRawType();
            } else {
                break;
            }
        }
        return Optional.empty();
    }

    public static boolean isArray(Type a) {
        return a instanceof Class<?> && ((Class<?>) a).isArray();
    }

    public static boolean isEnum(Type a) {
        Class ac = rawClass(a).orElse(null);
        return ac != null && ac.isEnum();
    }

    public static boolean isAssignableFrom(Type a, Type b) {
        Class ac = rawClass(a).orElse(null);
        Class bc = rawClass(b).orElse(null);
        return ac != null && bc != null && ac.isAssignableFrom(bc);
    }

    public static Type[] getGenericInterfaces(Type type) {
        if (type instanceof Class) {
            return ((Class<?>) type).getGenericInterfaces();
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType pt=(ParameterizedType) type;
            Type rawType = pt.getRawType();
            Type[] g = getGenericInterfaces(rawType);
            Type[] a = new Type[g.length];
            for (int i = 0; i < g.length; i++) {
                a[i] = getGenericParent(pt, g[i]);
            }
            return a;
        } else {
            throw new IllegalArgumentException("Not Supported yet");
        }
    }

    public static Optional<Type[]> asTypeArgs(Type toCheck, Class against) {
        return asType(toCheck, against)
                .map(x -> {
                    if (x instanceof Class) {
                        TypeVariable<? extends Class<?>>[] typeParameters = ((Class<?>) x).getTypeParameters();
                        Type[] a = new Type[typeParameters.length];
                        for (int i = 0; i < a.length; i++) {
                            if (typeParameters[i].getBounds().length == 0) {
                                a[i] = Object.class;
                            } else if (typeParameters[i].getBounds().length == 1) {
                                a[i] = typeParameters[i].getBounds()[0];
                            } else {
                                throw new IllegalArgumentException("Unsupported");
                            }
                        }
                    } else if (x instanceof ParameterizedType) {
                        return ((ParameterizedType) x).getActualTypeArguments();
                    }
                    throw new IllegalArgumentException("unsupported");
                });
    }

    public static Optional<Type> asType(Type toCheck, Class against) {
        Stack<Type> stack = new Stack<>();
        Set<Type> visited = new HashSet<>();
        stack.push(toCheck);
        while (!stack.isEmpty()) {
            Type a = stack.pop();
            if (!visited.contains(a)) {
                visited.add(a);
                /// do something
                if (a.equals(against)) {
                    return Optional.of(a);
                }
                if (a instanceof ParameterizedType) {
                    Type rawType = ((ParameterizedType) a).getRawType();
                    if (rawType.equals(against)) {
                        return Optional.of(a);
                    }
                }
                Type s = getGenericSuperclass(a);
                if (s != null && !visited.contains(s)) {
                    stack.push(s);
                }
                for (Type i : getGenericInterfaces(a)) {
                    if (i != null && !visited.contains(i)) {
                        stack.push(i);
                    }
                }
            }
        }
        return Optional.empty();
    }

    public static class GenericField {
        private final Field field;
        private final Type type;

        public GenericField(Field field, Type type) {
            this.field = field;
            this.type = type;
        }

        public Field getField() {
            return field;
        }

        public Type getType() {
            return type;
        }

        public int getModifiers() {
            return field.getModifiers();
        }

        public String getName() {
            return field.getName();
        }

        @Override
        public String toString() {
            return String.valueOf(field);
        }
    }

    private static class ParameterizedTypeImpl implements ParameterizedType {
        private final Type[] actualTypeArguments;
        private final Class<?> rawType;
        private final Type ownerType;

        public ParameterizedTypeImpl(Type[] actualTypeArguments, Class<?> rawType, Type ownerType) {
            this.actualTypeArguments = actualTypeArguments;
            this.rawType = rawType;
            this.ownerType = ownerType;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return actualTypeArguments;
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        @Override
        public Type getOwnerType() {
            return ownerType;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof ParameterizedType) {
                // Check that information is equivalent

                ParameterizedType that=(ParameterizedType) o;
                if (this == that)
                    return true;
                Type thatOwner = that.getOwnerType();
                Type thatRawType = that.getRawType();

                if (false) { // Debugging
                    boolean ownerEquality = (Objects.equals(ownerType, thatOwner));
                    boolean rawEquality = (Objects.equals(rawType, thatRawType));

                    boolean typeArgEquality = Arrays.equals(actualTypeArguments, // avoid clone
                            that.getActualTypeArguments());
//                    for (Type t : actualTypeArguments) {
//                        System.out.printf("\t\t%s%s%n", t, t.getClass());
//                    }
//
//                    System.out.printf("\towner %s\traw %s\ttypeArg %s%n",
//                            ownerEquality, rawEquality, typeArgEquality);
                    return ownerEquality && rawEquality && typeArgEquality;
                }

                return
                        Objects.equals(ownerType, thatOwner) &&
                                Objects.equals(rawType, thatRawType) &&
                                Arrays.equals(actualTypeArguments, // avoid clone
                                        that.getActualTypeArguments());
            } else
                return false;
        }

        @Override
        public int hashCode() {
            return
                    Arrays.hashCode(actualTypeArguments) ^
                            Objects.hashCode(ownerType) ^
                            Objects.hashCode(rawType);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();

            if (ownerType != null) {
                if (ownerType instanceof Class) {
                    sb.append(((Class) ownerType).getName());
                } else {
                    sb.append(ownerType);
                }
                sb.append("$");
                sb.append(rawType.getSimpleName());
            } else
                sb.append(rawType.getName());

            if (actualTypeArguments != null &&
                    actualTypeArguments.length > 0) {
                sb.append("<");
                boolean first = true;
                for (Type t : actualTypeArguments) {
                    if (!first)
                        sb.append(", ");
                    sb.append(t.getTypeName());
                    first = false;
                }
                sb.append(">");
            }

            return sb.toString();
        }
    }

    public static String toPrimitiveName(Type from) {
        if (!(from instanceof Class)) {
            return null;
        }
        Class cc=(Class) from;
        if (cc.isPrimitive()) {
            return cc.getName();
        }
        switch (cc.getName()) {
            case "java.lang.Boolean":
                return "boolean";
            case "java.lang.Byte":
                return "byte";
            case "java.lang.Short":
                return "short";
            case "java.lang.Integer":
                return "int";
            case "java.lang.Long":
                return "long";
            case "java.lang.Float":
                return "float";
            case "java.lang.Double":
                return "double";
            case "java.lang.Character":
                return "char";
        }
        return null;
    }

    public static boolean isBoxedOrPrimitive(Type from) {
        return toPrimitiveName(from) != null;
    }

}
