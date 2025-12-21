package net.thevpc.nuts.runtime.standalone.extension;

import java.util.Arrays;
import java.util.Objects;

public class TypeAndArgTypes {
    private Class implType;
    private Class apiType;
    private Class[] argTypes;

    public TypeAndArgTypes(Class implType, Class[] argTypes,Class apiType) {
        this.implType = implType;
        this.argTypes = argTypes;
        this.apiType = apiType;
    }

    public Class getApiType() {
        return apiType;
    }

    public Class getImplType() {
        return implType;
    }

    public Class[] getArgTypes() {
        return argTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeAndArgTypes that = (TypeAndArgTypes) o;
        return
                Objects.equals(implType, that.implType)
                && Objects.equals(apiType, that.apiType)
                && Arrays.equals(argTypes, that.argTypes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(implType);
        result = 31 * result + Objects.hash(apiType);
        result = 31 * result + Arrays.hashCode(argTypes);
        return result;
    }
}
