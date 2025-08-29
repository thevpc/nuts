package net.thevpc.nuts.util;

/**
 * @author vpc
 */
public class NBooleanRef extends NObjectRef<Boolean> {

    public static NBooleanRef of(Boolean value) {
        return new NBooleanRef(value);
    }

    public static NBooleanRef of(boolean value) {
        return new NBooleanRef(value);
    }

    public static NBooleanRef ofFalse() {
        return of(false);
    }

    public static NBooleanRef ofTrue() {
        return of(true);
    }

    public static NBooleanRef ofNull() {
        return of();
    }

    public static NBooleanRef of() {
        return new NBooleanRef(null);
    }

    public NBooleanRef(Boolean value) {
        super(value);
    }

    public NBooleanRef flip() {
        Boolean v = get();
        if (v != null) {
            set(!v);
        }
        return this;
    }

    public NBooleanRef set() {
        set(true);
        return this;
    }

    public NBooleanRef setTrue() {
        set(true);
        return this;
    }

    public NBooleanRef setFalse() {
        set(false);
        return this;
    }

    public NBooleanRef flipOrTrue() {
        Boolean v = get();
        if (v != null) {
            set(!v);
        } else {
            set(true);
        }
        return this;
    }

    public NBooleanRef flipOrFalse() {
        Boolean v = get();
        if (v != null) {
            set(!v);
        } else {
            set(false);
        }
        return this;
    }

}
