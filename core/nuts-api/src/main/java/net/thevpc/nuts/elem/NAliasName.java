package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NAssert;

import java.util.Objects;

public class NAliasName implements Comparable<NAliasName> {
    private String name;

    public NAliasName(String name) {
        this.name = NAssert.requireNonBlank(name, "name");
    }

    @Override
    public String toString() {
        return "&" + String.valueOf(name);
    }

    @Override
    public int compareTo(NAliasName o) {
        return name.compareTo(o.name);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NAliasName nName = (NAliasName) o;
        return Objects.equals(name, nName.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

}
