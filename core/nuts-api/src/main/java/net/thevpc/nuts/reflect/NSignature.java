package net.thevpc.nuts.reflect;

import java.util.Arrays;
import java.util.Objects;

public class NSignature {
    private NReflectType[] types;
    private boolean vararg;

    public static NSignature of(NReflectType... types) {
        return new NSignature(types, false);
    }

    public static NSignature ofVarArgs(NReflectType... types) {
        checkVararg(types);
        return new NSignature(types, true);
    }

    private static void checkVararg(NReflectType... types) {
        if (types.length == 0) {
            throw new IllegalArgumentException("invalid vararg with zero length arguments");
        }
        NReflectType lastType = types[types.length - 1];
        if (!lastType.isArrayType()) {
            throw new IllegalArgumentException("last argument must be a valid array for vararg methods");
        }
    }

    private NSignature(NReflectType[] types, boolean vararg) {
        this.types = types;
        this.vararg = vararg;
    }

    public NReflectType getType(int index) {
        return types[index];
    }

    public NSignature setVararg(boolean vararg) {
        if(this.vararg==vararg){
            return this;
        }
        if (vararg) {
            checkVararg(types);
        }
        NReflectType[] types2 = Arrays.copyOfRange(types, 0, types.length);
        return new NSignature(types2, vararg);
    }

    public NSignature set(NReflectType any, int pos) {
        NReflectType[] types2 = Arrays.copyOfRange(types, 0, types.length);
        types2[pos] = any;
        if (vararg) {
            checkVararg(types2);
        }
        return new NSignature(types2, vararg);
    }

    public int size() {
        return types.length;
    }

    public NReflectType[] types() {
        return types;
    }

    public boolean isVarArgs() {
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
    private String strOfType(NReflectType t) {
        return t.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NSignature nSig = (NSignature) o;
        return vararg == nSig.vararg && Objects.deepEquals(types, nSig.types);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(types), vararg);
    }
}
