package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NCopiable;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NSetter;

import java.util.function.Supplier;

/**
 * Represents a once value that can be cached or computed on demand.
 * <p>
 * This model holds the actual value, an optional error state, and a supplier
 * to lazily recompute the value if needed. It supports cloning and copying.
 *
 * @since 0.8.6
 */
public class NOnceValueModel implements Cloneable, NCopiable {

    /**
     * Unique identifier for this once value.
     */
    private String id;
    /**
     * Current value held by this once value.
     */
    private Object value;
    /**
     * Throwable associated with a failed computation of the value.
     */
    private Throwable error;

    /**
     * True if the value is in an error state, false if valid, null if unknown.
     */
    private Boolean errorState;

    /**
     * Supplier to lazily compute the value when needed.
     */
    private Supplier<?> supplier;

    public NOnceValueModel() {
    }

    /**
     * Creates a new once value model with the given id and supplier.
     *
     * @param id       unique identifier
     * @param supplier supplier to compute the value
     */
    public NOnceValueModel(String id, Supplier<?> supplier) {
        this.id = id;
        this.supplier = supplier;
    }

    public Supplier<?> supplier() {
        return supplier;
    }

    public NOnceValueModel supplier(Supplier<?> supplier) {
        this.supplier = supplier;
        return this;
    }

    @NGetter
    public String id() {
        return id;
    }

    public NOnceValueModel id(String id) {
        this.id = id;
        return this;
    }

    @NGetter
    public Object value() {
        return value;
    }

    @NSetter
    public NOnceValueModel value(Object value) {
        this.value = value;
        return this;
    }

    @NGetter
    public Throwable error() {
        return error;
    }

    @NSetter
    public NOnceValueModel error(Throwable throwable) {
        this.error = throwable;
        return this;
    }

    @NGetter
    public Boolean errorState() {
        return errorState;
    }

    @NSetter
    public NOnceValueModel errorState(Boolean errorState) {
        this.errorState = errorState;
        return this;
    }

    /**
     * Returns a copy of this model.
     *
     * @return a cloned instance
     */
    public NOnceValueModel copy(){
        return clone();
    }

    protected NOnceValueModel clone(){
        try {
            return (NOnceValueModel) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
