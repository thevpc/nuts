package net.thevpc.nuts.reflect;

import net.thevpc.nuts.util.NAssert;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

/**
 * NTypeName class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public final class NTypeName<T> implements Serializable {
    private static final long serialVersionUID = 1;
    private final String typeName;
    private final NTypeName[] parameters;
    private final int arr;

    /**
     * N type name.
     *
     * @param name name
     * @param parameters parameters
     * @return n type name result
     */
    public NTypeName(String name, NTypeName... parameters) {
      /**
       * This.
       *
       * @param name name
       * @param parameters parameters
       * @param 0 0
       */
        this(name, parameters, 0);
    }

    /**
     * N type name.
     *
     * @param name name
     * @param parameters parameters
     * @param arr arr
     * @return n type name result
     */
    public NTypeName(String name, NTypeName[] parameters, int arr) {
        if (name.contains("<")) {
            if (parameters.length != 0) {
                /**
                 * Illegal argument exception.
                 *
                 * @param parameters" parameters"
                 * @return illegal argument exception result
                 */
                throw new IllegalArgumentException("Could not use <> names with effective parameters");
            }
            /**
             * Illegal argument exception.
             *
             * @param yet" yet"
             * @return illegal argument exception result
             */
            throw new IllegalArgumentException("Not Supported yet");
        } else {
            this.typeName = name;
            this.parameters = parameters;
            this.arr = arr;
            NAssert.requireNamedTrue(arr >= 0, "array");
        }
    }

    /**
     * Converts to array.
     *
     * @return to array result
     */
    public NTypeName<T> toArray() {
        return new NTypeName<>(typeName, parameters, arr + 1);
    }


    /**
     * Component type.
     *
     * @return component type result
     */
    public NTypeName<T> componentType() {
        if (arr == 0) {
            return this;
        }
        return new NTypeName<>(typeName, parameters, arr - 1);
    }

    /**
     * Checks if is array.
     *
     * @return is array result
     */
    public boolean isArray() {
        return arr != 0;
    }

    /**
     * Name.
     *
     * @return name result
     */
    public String name() {
        return typeName;
    }

    /**
     * Parameters count.
     *
     * @return parameters count result
     */
    public int parametersCount() {
        return parameters.length;
    }

    /**
     * Parameters.
     *
     * @return parameters result
     */
    public NTypeName[] parameters() {
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

    /**
     * Creates a new instance of of.
     *
     * @param type type
     * @param args args
     * @return of result
     */
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
