package net.thevpc.nuts.runtime.standalone.workspace.factorycache;

import java.util.Arrays;
import java.util.Objects;

public class TypeAndArgTypes {
    private Class type;
    private Class[] argTypes;

    public TypeAndArgTypes(Class type, Class... argTypes) {
        this.type = type;
        this.argTypes = argTypes;
    }

    public Class getType() {
        return type;
    }

    public Class[] getArgTypes() {
        return argTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeAndArgTypes that = (TypeAndArgTypes) o;
        return Objects.equals(type, that.type) && Arrays.equals(argTypes, that.argTypes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(type);
        result = 31 * result + Arrays.hashCode(argTypes);
        return result;
    }
}
