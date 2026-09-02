package net.thevpc.nuts.util;

import java.util.Objects;

/**
 *
 * @author vpc
 */
public class NObjectRef<T> implements NRef<T> {
    
    private T value;
    private boolean set;

    /**
     * N object ref.
     *
     * @param value value
     * @return n object ref result
     */
    public NObjectRef(T value) {
        this.value = value;
    }

    
    /**
     * Returns the get.
     *
     * @return get result
     */
    public T get() {
        return value;
    }

    /**
     * Or else.
     *
     * @param other other
     * @return or else result
     */
    public T orElse(T other) {
        if (value == null) {
            return other;
        }
        return value;
    }

    /**
     * Sets the non null.
     *
     * @param value value
     */
    public void setNonNull(T value) {
        if (value != null) {
            set(value);
        }
    }
    /**
     * Sets the if null.
     *
     * @param value value
     */
    public void setIfNull(T value) {
        if (this.value==null) {
            set(value);
        }
    }

    /**
     * Sets the set.
     *
     * @param value value
     */
    public void set(T value) {
        this.value = value;
        this.set = true;
    }

    /**
     * Unset.
     */
    public void unset() {
        this.value = null;
        this.set = false;
    }

    /**
     * Checks if is not null.
     *
     * @return is not null result
     */
    public boolean isNotNull() {
        return value != null;
    }

    /**
     * Checks if is blank.
     *
     * @return is blank result
     */
    public boolean isBlank() {
        return NBlankable.isBlank(value);
    }

    /**
     * Checks if is empty.
     *
     * @return is empty result
     */
    public boolean isEmpty() {
        return value == null || String.valueOf(value).isEmpty();
    }

    /**
     * Checks if is null.
     *
     * @return is null result
     */
    public boolean isNull() {
        return value == null;
    }

    /**
     * Checks if is set.
     *
     * @return is set result
     */
    public boolean isSet() {
        return set;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Checks if is value.
     *
     * @param o o
     * @return is value result
     */
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
