package net.thevpc.nuts.internal.artifact;

import net.thevpc.nuts.artifact.NVersionPart;
import net.thevpc.nuts.artifact.NVersionPartType;

import java.util.Objects;

/**
 * DefaultNVersionPart class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NVersionPartImpl implements NVersionPart {

    String string;
    NVersionPartType type;

    /**
     * Default n version part.
     *
     * @param string string
     * @param type type
     * @return default n version part result
     */
    public NVersionPartImpl(String string, NVersionPartType type) {
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
        NVersionPartImpl that = (NVersionPartImpl) o;
        return string.equalsIgnoreCase(that.string) && type == that.type;
    }

    @Override
    public String toString() {
        String name = type.name().toLowerCase();
        return name + "(" + string + ")";
    }


}
