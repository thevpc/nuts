package net.thevpc.nuts.util;

import java.util.Objects;

/**
 *
 * @author vpc
 */
public class NObjectRef<T> implements NRef<T> {
    
    private T value;
    private boolean set;

    public NObjectRef(T value) {
        this.value = value;
    }

    
    public T get() {
        return value;
    }

    public T orElse(T other) {
        if (value == null) {
            return other;
        }
        return value;
    }

    public void setNonNull(T value) {
        if (value != null) {
            set(value);
        }
    }

    public void set(T value) {
        this.value = value;
        this.set = true;
    }

    public void unset() {
        this.value = null;
        this.set = false;
    }

    public boolean isNotNull() {
        return value != null;
    }

    public boolean isBlank() {
        return NBlankable.isBlank(value);
    }

    public boolean isEmpty() {
        return value == null || String.valueOf(value).isEmpty();
    }

    public boolean isNull() {
        return value == null;
    }

    public boolean isSet() {
        return set;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public boolean isValue(Object o) {
        return Objects.equals(value, o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NObjectRef<?> nRef = (NObjectRef<?>) o;
        return set == nRef.set && Objects.equals(value, nRef.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, set);
    }
    
}
