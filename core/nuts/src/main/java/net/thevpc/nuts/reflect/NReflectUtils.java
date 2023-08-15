package net.thevpc.nuts.reflect;

import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NAssert;

public class NReflectUtils {
    private NReflectUtils() {
    }

    public static Object getDefaultValue(Class<?> anyType){
        NAssert.requireNonNull(anyType,"type");
        switch (anyType.getName()){
            case "boolean":return false;
            case "byte":return (byte)0;
            case "short":return (short)0;
            case "int":return (int)0;
            case "long":return 0L;
            case "char":return '\0';
            case "float":return 0.0f;
            case "double":return 0.0;
            case "void":return null;
        }
        return null;
    }

    public static NOptional<Class<?>> toBoxedType(Class<?> anyType){
        if(anyType==null){
            return NOptional.ofNamedError("no boxed type for null");
        }
        switch (anyType.getName()){
            case "boolean":return NOptional.of(Boolean.class);
            case "byte":return NOptional.of(Byte.class);
            case "short":return NOptional.of(Short.class);
            case "int":return NOptional.of(Integer.class);
            case "long":return NOptional.of(Long.class);
            case "char":return NOptional.of(Character.class);
            case "float":return NOptional.of(Float.class);
            case "double":return NOptional.of(Double.class);
            case "void":return NOptional.of(Void.class);
        }
        return NOptional.ofNamedError(NMsg.ofC("not a primitive type %s",anyType));
    }

    public static NOptional<Class<?>> toPrimitiveType(Class<?> anyType){
        if(anyType==null){
            return NOptional.ofNamedError("no boxed type for null");
        }
        switch (anyType.getName()){
            case "java.lang.Boolean":return NOptional.of(Boolean.TYPE);
            case "java.lang.Byte":return NOptional.of(Byte.TYPE);
            case "java.lang.Short":return NOptional.of(Short.TYPE);
            case "java.lang.Integer":return NOptional.of(Integer.TYPE);
            case "java.lang.Long":return NOptional.of(Long.TYPE);
            case "java.lang.Character":return NOptional.of(Character.TYPE);
            case "java.lang.Float":return NOptional.of(Float.TYPE);
            case "java.lang.Double":return NOptional.of(Double.TYPE);
            case "java.lang.Void":return NOptional.of(Void.TYPE);
        }
        return NOptional.ofNamedError(NMsg.ofC("not a primitive type %s",anyType));
    }
}
