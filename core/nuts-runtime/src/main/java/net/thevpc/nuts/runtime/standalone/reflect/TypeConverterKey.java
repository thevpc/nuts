package net.thevpc.nuts.runtime.standalone.reflect;

import net.thevpc.nuts.reflect.NReflectType;

import java.util.Objects;

public class TypeConverterKey {
    private NReflectType fromType;
    private NReflectType toType;

    public TypeConverterKey(NReflectType fromType, NReflectType toType) {
        this.fromType = fromType;
        this.toType = toType;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TypeConverterKey that = (TypeConverterKey) o;
        return Objects.equals(fromType, that.fromType) && Objects.equals(toType, that.toType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromType, toType);
    }
}
