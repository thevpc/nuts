package net.thevpc.nuts.util;

/**
 * @author vpc
 */
public class NBooleanRef extends NObjectRef<Boolean> {

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
