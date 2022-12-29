package net.thevpc.nuts.toolbox.nbackup;

import net.thevpc.nuts.NBlankable;

import java.util.Objects;

public class DecoratedPath {
    private String path;
    private String name;

    public DecoratedPath() {

    }

    public DecoratedPath(String path, String name) {
        if (NBlankable.isBlank(path)) {
            throw new IllegalArgumentException("invalid path");
        }
        this.path = path.trim();
        if (NBlankable.isBlank(name)) {
            name = path;
        }
        this.name = name.trim();

    }

    public String getPath() {
        return path;
    }

    public DecoratedPath setPath(String path) {
        this.path = path;
        return this;
    }

    public String getName() {
        return name;
    }

    public DecoratedPath setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DecoratedPath that = (DecoratedPath) o;
        return Objects.equals(path, that.path) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, name);
    }
}
