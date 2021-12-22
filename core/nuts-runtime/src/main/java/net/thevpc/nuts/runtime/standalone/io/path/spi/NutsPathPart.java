package net.thevpc.nuts.runtime.standalone.io.path.spi;

import java.util.Objects;

public class NutsPathPart {
    private final String separator;
    private final String name;

    public NutsPathPart(String prefixSep, String name) {
        if (name == null || prefixSep == null) {
            throw new IllegalArgumentException("null name or prefix");
        }
        if (name.isEmpty() && prefixSep.isEmpty()) {
            throw new IllegalArgumentException("empty part");
        }
        this.separator = prefixSep;
        this.name = name;
    }

    public String getSeparator() {
        return separator;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return separator + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsPathPart part = (NutsPathPart) o;
        return Objects.equals(separator, part.separator) && Objects.equals(name, part.name);
    }

    public boolean isSeparated() {
        return !separator.isEmpty();
    }

    public boolean isSeparatedName() {
        return !name.isEmpty() && !separator.isEmpty();
    }

    public boolean isEmpty() {
        return name.isEmpty() && separator.isEmpty();
    }

    public boolean isName() {
        return !name.isEmpty() && separator.isEmpty();
    }

    public boolean isTrailingSeparator() {
        return name.isEmpty() && !separator.isEmpty();
    }

    @Override
    public int hashCode() {
        return Objects.hash(separator, name);
    }
}
