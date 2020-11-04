package net.thevpc.nuts;

import java.util.Objects;

/**
 * 
 * @author vpc
 * @category Format
 */
public class NutsString {
    private String value;

    public NutsString(String value) {
        this.value = value == null ? "" : value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsString that = (NutsString) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
