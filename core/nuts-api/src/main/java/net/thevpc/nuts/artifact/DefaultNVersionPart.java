package net.thevpc.nuts.artifact;

import java.util.Objects;

/**
 * DefaultNVersionPart class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class DefaultNVersionPart implements NVersionPart {

    String string;
    NVersionPartType type;

    /**
     * Default n version part.
     *
     * @param string string
     * @param type type
     * @return default n version part result
     */
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
