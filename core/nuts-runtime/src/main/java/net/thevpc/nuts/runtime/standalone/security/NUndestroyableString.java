package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.security.NSecureString;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NIntRef;
import net.thevpc.nuts.util.NRef;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class NUndestroyableString extends NAbstractSecureString {
    public static final NUndestroyableString EMPTY = new NUndestroyableString(new char[0]);

    public NUndestroyableString(char[] value) {
        super(value);
    }

    @Override
    public NSecureString destroy() {
        return this;
    }

    @Override
    public boolean isDestroyed() {
        return false;
    }

    @Override
    public String toString() {
        return Arrays.toString(value);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NUndestroyableString that = (NUndestroyableString) o;
        return Objects.deepEquals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(value));
    }

    @Override
    public NSecureString copy() {
        return this;
    }
}
