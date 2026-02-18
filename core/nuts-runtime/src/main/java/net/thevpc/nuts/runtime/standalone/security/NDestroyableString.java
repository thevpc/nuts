package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.security.NSecureString;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NIllegalStateException;

import java.util.Arrays;
import java.util.Objects;

public class NDestroyableString extends NAbstractSecureString {
    private boolean destroyed;

    public NDestroyableString(char[] value) {
        super(value);
    }

    @Override
    public NSecureString destroy() {
        if (!destroyed) {
            Arrays.fill(value, '\0');
            destroyed = true;
        }
        return this;
    }

    @Override
    public NSecureString copy() {
        synchronized (this) {
            if (isDestroyed()) {
                throw new NIllegalStateException(NMsg.ofC("cannot copy a destroyed secure string"));
            }
            // We use the raw value directly while inside the lock
            // to create a new, fresh instance.
            return NSecureString.ofSecure(Arrays.copyOf(value, value.length));
        }
    }
    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NDestroyableString that = (NDestroyableString) o;
        return destroyed == that.destroyed && Objects.deepEquals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(value), destroyed);
    }

    @Override
    public String toString() {
        if(destroyed){
            return "SecureDestroyedString";
        }
        return "SecureString";
    }
}
