package net.thevpc.nuts.util;

import net.thevpc.nuts.reflect.NReflectUtils;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

public class NPlatformSignature {
    private Type[] types;
    private boolean vararg;

    public static NPlatformSignature of(Type... types) {
        return new NPlatformSignature(types, false);
    }

    public static NPlatformSignature ofVarArgs(Type... types) {
        checkVararg(types);
        return new NPlatformSignature(types, true);
    }

    private static void checkVararg(Type... types) {
        if (types.length == 0) {
            throw new IllegalArgumentException("invalid vararg with zero length arguments");
        }
        Type lastType = types[types.length - 1];
        if (!(lastType instanceof Class)) {
            throw new IllegalArgumentException("last argument must be a valid array for vararg methods");
        }
        if (!(lastType instanceof GenericArrayType) && (!(lastType instanceof Class && ((Class<?>) lastType).isArray()))) {
            throw new IllegalArgumentException("last argument must be a valid array for vararg methods");
        }
    }

    private NPlatformSignature(Type[] types, boolean vararg) {
        this.types = types;
        this.vararg = vararg;
    }

    public Type getType(int index) {
        return types[index];
    }

    public NPlatformSignature setVararg(boolean vararg) {
        if (vararg) {
            checkVararg(types);
        }
        Type[] types2 = Arrays.copyOfRange(types, 0, types.length);
        return new NPlatformSignature(types2, vararg);
    }

    public NPlatformSignature set(Type any, int pos) {
        Type[] types2 = Arrays.copyOfRange(types, 0, types.length);
        types2[pos] = any;
        if (vararg) {
            checkVararg(types2);
        }
        return new NPlatformSignature(types2, vararg);
    }

    public int size() {
        return types.length;
    }

    public Type[] types() {
        return types;
    }

    public boolean isVararg() {
        return vararg;
    }

    @Override
    public String toString() {
        if (types.length == 0) {
            return "()";
        } else {
            StringBuilder b = new StringBuilder();
            b.append('(');
            if (vararg) {
                if (types.length == 1) {
                    b.append(strOfType(types[0]));
                    b.append("...");
                } else {
                    b.append(strOfType(types[0]));
                    for (int i = 1; i < types.length - 1; i++) {
                        b.append(",");
                        b.append(strOfType(types[i]));
                    }
                    b.append(strOfType(types[types.length - 1]));
                    b.append("...");
                }
            } else {
                b.append(types[0]);
                for (int i = 1; i < types.length; i++) {
                    b.append(",");
                    b.append(strOfType(types[i]));
                }
            }
            return b.append(')').toString();
        }
    }

    private String strOfType(Type t) {
        if (t instanceof Class) {
            return ((Class) t).getName();
        }
        return t == null ? "null" : t.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NPlatformSignature nSig = (NPlatformSignature) o;
        return vararg == nSig.vararg && Objects.deepEquals(types, nSig.types);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(types), vararg);
    }

    public boolean matches(NPlatformSignature other) {
        int mySize = this.size();
        boolean vararg1 = isVararg();
        if (vararg1 && other.size() == mySize - 1) {
            for (int i = 0; i < mySize - 1; i++) {
                if (!typeMatches(types[i], other.types[i], false)) {
                    return false;
                }
            }
            return true;
        }
        if (mySize != other.size()) {
            return false;
        }
        for (int i = 0; i < mySize; i++) {
            if (!typeMatches(types[i], other.types[i], vararg1 && i == mySize - 1)) {
                return false;
            }
        }
        return true;
    }

    private boolean typeMatches(Type a, Type b, boolean varArg) {
        if (a == null) {
            return true;
        }
        if (b == null) {
            return true;
        }
        if (a instanceof Class
                && b instanceof Class
        ) {
            Class<?> ca = (Class<?>) a;
            Class<?> cb = (Class<?>) b;
            if (ca.isPrimitive() || cb.isPrimitive()) {
                return typeMatches(NReflectUtils.toBoxedType(ca).orElse(ca), NReflectUtils.toBoxedType(cb).orElse(cb), false);
            }
            if (ca.isAssignableFrom(cb)) {
                return true;
            }
            if (varArg && ca.isArray() && typeMatches(ca.getComponentType(), cb, false)) {
                return true;
            }
        }
        return false;
    }
}
