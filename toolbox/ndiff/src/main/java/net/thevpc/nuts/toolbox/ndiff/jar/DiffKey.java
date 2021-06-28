package net.thevpc.nuts.toolbox.ndiff.jar;

import java.util.Objects;

public final class DiffKey implements Comparable<DiffKey> {
    public static final String KIND_FILE = "file";
    public static final String KIND_VAR = "var";
    private String name;
    private String kind;
    private int order;

    public DiffKey(String name, String kind, int order) {
        this.name = name;
        this.kind = kind;
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public String getKind() {
        return kind;
    }

    public int getOrder() {
        return order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiffKey diffKey = (DiffKey) o;
        return Objects.equals(name, diffKey.name) &&
                Objects.equals(kind, diffKey.kind);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, kind);
    }

    @Override
    public int compareTo(DiffKey o) {
        int c = Integer.compare(this.order, o.order);
        if (c != 0) {
            return c;
        }
        c = this.kind.compareTo(o.kind);
        if (c != 0) {
            return c;
        }
        return this.name.compareTo(o.name);
    }
}
