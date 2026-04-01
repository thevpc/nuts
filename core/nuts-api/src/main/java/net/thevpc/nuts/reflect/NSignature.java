package net.thevpc.nuts.reflect;

import net.thevpc.nuts.util.NOptional;

public interface NSignature<T,A extends NSignature<T,?>> {
    NOptional<String> name();
    A toUnnamed();
    A toNamed(String newName);
    boolean isNamed();
    T getType(int index);
    A setVararg(boolean vararg);
    A set(T any, int pos);
    int size();
    T[] types();
    boolean isVarArgs();
    boolean matches(A other);
    NSignatureScore calculateScore(A other);
    NSignatureDomain<T> domain();
}
