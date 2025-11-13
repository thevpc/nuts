package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NCopiable;

import java.util.function.Supplier;

/**
 * Represents a stable value that can be cached or computed on demand.
 * <p>
 * This model holds the actual value, an optional error state, and a supplier
 * to lazily recompute the value if needed. It supports cloning and copying.
 *
 * @since 0.8.6
 */
public class NStableValueModel implements Cloneable, NCopiable {

    /**
     * Unique identifier for this stable value.
     */
    private String id;
    /**
     * Current value held by this stable value.
     */
    private Object value;
    /**
     * Throwable associated with a failed computation of the value.
     */
    private Throwable throwable;

    /**
     * True if the value is in an error state, false if valid, null if unknown.
     */
    private Boolean errorState;

    /**
     * Supplier to lazily compute the value when needed.
     */
    private Supplier<?> supplier;

    public NStableValueModel() {
    }

    /**
     * Creates a new stable value model with the given id and supplier.
     *
     * @param id       unique identifier
     * @param supplier supplier to compute the value
     */
    public NStableValueModel(String id,Supplier<?> supplier) {
        this.id = id;
        this.supplier = supplier;
    }

    public Supplier<?> getSupplier() {
        return supplier;
    }

    public NStableValueModel setSupplier(Supplier<?> supplier) {
        this.supplier = supplier;
        return this;
    }

    public String getId() {
        return id;
    }

    public NStableValueModel setId(String id) {
        this.id = id;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public NStableValueModel setValue(Object value) {
        this.value = value;
        return this;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public NStableValueModel setThrowable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    public Boolean getErrorState() {
        return errorState;
    }

    public NStableValueModel setErrorState(Boolean errorState) {
        this.errorState = errorState;
        return this;
    }

    /**
     * Returns a copy of this model.
     *
     * @return a cloned instance
     */
    public NStableValueModel copy(){
        return clone();
    }

    protected NStableValueModel clone(){
        try {
            return (NStableValueModel) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
