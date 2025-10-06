package net.thevpc.nuts.artifact;

import java.util.Objects;

public class DefaultNVersionPart implements NVersionPart {

    String string;
    NVersionPartType type;

    public DefaultNVersionPart(String string, NVersionPartType type) {
        this.string = string;
        this.type = type;
    }

    @Override
    public NVersionPartType type() {
        return type;
    }

    @Override
    public String value() {
        return string;
    }

    @Override
    public int hashCode() {
        return Objects.hash(string, type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNVersionPart that = (DefaultNVersionPart) o;
        return string.equalsIgnoreCase(that.string) && type == that.type;
    }

    @Override
    public String toString() {
        String name = type.name().toLowerCase();
        return name + "(" + string + ")";
    }


}
