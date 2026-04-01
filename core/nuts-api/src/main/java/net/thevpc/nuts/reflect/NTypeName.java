package net.thevpc.nuts.reflect;

import net.thevpc.nuts.util.NAssert;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

public final class NTypeName<T> implements Serializable {
    private static final long serialVersionUID = 1;
    private final String typeName;
    private final NTypeName[] parameters;
    private final int arr;

    public NTypeName(String name, NTypeName... parameters) {
        this(name, parameters, 0);
    }

    public NTypeName(String name, NTypeName[] parameters, int arr) {
        if (name.contains("<")) {
            if (parameters.length != 0) {
                throw new IllegalArgumentException("Could not use <> names with effective parameters");
            }
            throw new IllegalArgumentException("Not Supported yet");
        } else {
            this.typeName = name;
            this.parameters = parameters;
            this.arr = arr;
            NAssert.requireNamedTrue(arr >= 0, "array");
        }
    }

    public NTypeName<T> toArray() {
        return new NTypeName<>(typeName, parameters, arr + 1);
    }


    public NTypeName<T> getComponentType() {
        if (arr == 0) {
            return this;
        }
        return new NTypeName<>(typeName, parameters, arr - 1);
    }

    public boolean isArray() {
        return arr != 0;
    }

    public String name() {
        return typeName;
    }

    public int getParametersCount() {
        return parameters.length;
    }

    public NTypeName[] getParameters() {
        return Arrays.copyOf(parameters, parameters.length);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NTypeName that = (NTypeName) o;
        return Objects.equals(typeName, that.typeName)
                && Arrays.equals(parameters, that.parameters)
                && arr==that.arr
                ;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(typeName);
        result = 31 * result + Arrays.hashCode(parameters);
        result = 31 * result + arr;
        return result;
    }

    public static <T> NTypeName<T> of(Type type, NTypeName<?>... args) {
        if (type instanceof ParameterizedType) {
            ParameterizedType ptype = (ParameterizedType) type;
            return new NTypeName(ptype.getRawType().toString(), args);
        }
        return new NTypeName(type.toString());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(typeName);
        if (parameters.length > 0) {
            sb.append("<");
            for (NTypeName parameter : parameters) {
                if (sb.charAt(sb.length() - 1) != '<') {
                    sb.append(",");
                }
                sb.append(parameter.name());
            }
            sb.append(">");
        }
        for (int i = 0; i < arr; i++) {
            sb.append("[]");
        }
        return sb.toString();
    }
}
