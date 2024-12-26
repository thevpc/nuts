package net.thevpc.nuts.runtime.standalone.util.reflect;

import net.thevpc.nuts.reflect.NReflectMapper;
import net.thevpc.nuts.reflect.NReflectMapperContext;
import net.thevpc.nuts.reflect.NReflectType;
import net.thevpc.nuts.util.NEnum;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;

public class DefaultConvertersByType implements NReflectMapper.Converter {
    @Override
    public Object convert(Object value, String path, NReflectType fromType, NReflectType toType, NReflectMapperContext context) {
        if (value == null) {
            return toType.getDefaultValue();
        }
        fromType = fromType.getBoxedType().get();
        toType = toType.getBoxedType().get();


        Type tojType = toType.getJavaType();
        if (
                toType.getName().equals(fromType.getName())
                || toType.isAssignableFrom(fromType)
        ) {
            // immutable / value objects
            switch (fromType.getName()) {
                case "java.lang.Boolean":
                case "java.lang.Byte":
                case "java.lang.Character":
                case "java.lang.Short":
                case "java.lang.Integer":
                case "java.lang.Long":
                case "java.lang.Float":
                case "java.lang.Double":
                case "java.lang.String":
                    return value;
            }
        }

        //fall through

        if (toType.getName().equals("java.lang.String")) {
            return String.valueOf(value);
        }else if (fromType.getName().equals("java.lang.String")) {
            if (tojType instanceof NEnum) {
                return NEnum.parse((Class<? extends NEnum>) tojType, (String) value).get();
            }
            if (tojType instanceof Enum) {
                return Enum.valueOf((Class<? extends Enum>) tojType, (String) value);
            }
            switch (toType.getName()) {
                case "java.lang.Boolean":return Boolean.parseBoolean((String) value);
                case "java.lang.Character":return ((String) value).charAt(0);
                case "java.lang.Byte":return Byte.parseByte((String) value);
                case "java.lang.Short":return Short.parseShort((String) value);
                case "java.lang.Integer":return Integer.parseInt((String) value);
                case "java.lang.Long":return Long.parseLong((String) value);
                case "java.lang.Float":return Float.parseFloat((String) value);
                case "java.lang.Double":return Double.parseDouble((String) value);
            }
        } else if (context.getReflectRepository().getType(Number.class).isAssignableFrom(fromType)) {
            if (tojType instanceof Enum) {
                return ((Class<? extends Enum>) tojType).getEnumConstants()[((Number) value).intValue()];
            }else if (tojType instanceof Class && Number.class.isAssignableFrom((Class<?>) tojType)) {
                switch (toType.getName()) {
                    case "java.lang.Byte":return ((Number) value).byteValue();
                    case "java.lang.Short":return ((Number) value).shortValue();
                    case "java.lang.Integer":return ((Number) value).intValue();
                    case "java.lang.Long":return ((Number) value).longValue();
                    case "java.lang.Float":return ((Number) value).floatValue();
                    case "java.lang.Double":return ((Number) value).doubleValue();
                }
            }
        } else if (fromType.isArrayType()) {
            if (toType.isArrayType()) {
                int len = Array.getLength(value);
                Object newArr = Array.newInstance((Class<?>) toType.getJavaType(), len);
                for (int i = 0; i < len; i++) {
                    Array.set(newArr, i, Array.get(value, i));
                }
                return newArr;
            } else if (tojType instanceof Class) {
                Class<?> cTojType = (Class<?>) tojType;
                if (java.util.Collection.class.isAssignableFrom(cTojType)) {
                    if (cTojType.equals(java.util.Collection.class)) {
                        cTojType = java.util.ArrayList.class;
                    } else if (cTojType.equals(java.util.List.class)) {
                        cTojType = java.util.ArrayList.class;
                    } else if (cTojType.equals(java.util.Set.class)) {
                        cTojType = java.util.HashSet.class;
                    }
                    Collection li = (Collection) context.getReflectRepository().getType(cTojType).newInstance();
                    int len = Array.getLength(value);
                    for (int i = 0; i < len; i++) {
                        li.add(Array.get(value, i));
                    }
                    return li;
                }
            }
        } else if (fromType.getJavaType() instanceof Class && java.util.Collection.class.isAssignableFrom((Class<?>) fromType.getJavaType())) {
            if (toType.isArrayType()) {
                Collection coll = (Collection) value;
                int len = coll.size();
                Object newArr = Array.newInstance((Class<?>) toType.getJavaType(), len);
                int i=0;
                for (Object o : coll) {
                    Array.set(newArr, i++, o);
                }
                return newArr;
            } else if (tojType instanceof Class) {
                Class<?> cTojType = (Class<?>) tojType;
                if (java.util.Collection.class.isAssignableFrom(cTojType)) {
                    if (cTojType.equals(java.util.Collection.class)) {
                        cTojType = java.util.ArrayList.class;
                    } else if (cTojType.equals(java.util.List.class)) {
                        cTojType = java.util.ArrayList.class;
                    } else if (cTojType.equals(java.util.Set.class)) {
                        cTojType = java.util.HashSet.class;
                    }
                    Collection li = (Collection) context.getReflectRepository().getType(cTojType).newInstance();
                    Collection coll = (Collection) value;
                    li.addAll(coll);
                    return li;
                }
            }
        }
        return null;
    }
}
