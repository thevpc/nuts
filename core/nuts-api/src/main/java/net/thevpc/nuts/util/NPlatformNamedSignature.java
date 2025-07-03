package net.thevpc.nuts.util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Objects;

public class NPlatformNamedSignature {
    private NPlatformArgsSignature args;
    private String name;

    public static NPlatformNamedSignature of(String name, Type... types) {
        return new NPlatformNamedSignature(name, types, false);
    }

    public static NPlatformNamedSignature ofVarArgs(String name, Type... types) {
        checkVararg(types);
        return new NPlatformNamedSignature(name, types, true);
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

    private NPlatformNamedSignature(String name, Type[] types, boolean vararg) {
        this.name = name;
        this.args = vararg ? NPlatformArgsSignature.ofVarArgs(types) : NPlatformArgsSignature.of(types);
    }

    public Type getType(int index) {
        return args.getType(index);
    }

    public NPlatformNamedSignature setVararg(boolean vararg) {
        if (vararg == isVararg()) {
            return this;
        }
        return args.isVararg()
                ? ofVarArgs(name, args.types())
                : of(name, args.types());
    }

    public NPlatformNamedSignature set(Type any, int pos) {
        return args.isVararg()
                ? ofVarArgs(name, args.set(any, pos).types())
                : of(name, args.set(any, pos).types());
    }

    public int size() {
        return args.size();
    }

    public Type[] types() {
        return args.types();
    }

    public boolean isVararg() {
        return args.isVararg();
    }

    @Override
    public String toString() {
        if (NBlankable.isBlank(name)) {
            return args.toString();
        }
        return name.trim() + args.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NPlatformNamedSignature nSig = (NPlatformNamedSignature) o;
        return Objects.equals(name, nSig.name) && Objects.deepEquals(args, nSig.args);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, args);
    }

    public String name() {
        return name;
    }

    public NPlatformArgsSignature args() {
        return args;
    }

    private boolean nameMatches(String name, String namePattern) {
        if (NBlankable.isBlank(namePattern) || NBlankable.isBlank(name)) {
            return true;
        }
        if (NStringUtils.trim(namePattern).equals("*")) {
            return true;
        }
        return name.matches(namePattern);
    }

    public boolean matches(NPlatformNamedSignature other) {
        return nameMatches(name, other.name) && args.matches(other.args);
    }
}
