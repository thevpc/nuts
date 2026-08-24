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

    /**
     * N once value model.
     *
     * @return n once value model result
     */
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

    /**
     * Supplier.
     *
     * @return supplier result
     */
    public Supplier<?> supplier() {
        return supplier;
    }

    /**
     * Supplier.
     *
     * @param supplier supplier
     * @return supplier result
     */
    public NOnceValueModel supplier(Supplier<?> supplier) {
        this.supplier = supplier;
        return this;
    }

    /**
     * Id.
     *
     * @return id result
     */
    @NGetter
    public String id() {
        return id;
    }

    /**
     * Id.
     *
     * @param id id
     * @return id result
     */
    public NOnceValueModel id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Value.
     *
     * @return value result
     */
    @NGetter
    public Object value() {
        return value;
    }

    /**
     * Value.
     *
     * @param value value
     * @return value result
     */
    @NSetter
    public NOnceValueModel value(Object value) {
        this.value = value;
        return this;
    }

    /**
     * Error.
     *
     * @return error result
     */
    @NGetter
    public Throwable error() {
        return error;
    }

    /**
     * Error.
     *
     * @param throwable throwable
     * @return error result
     */
    @NSetter
    public NOnceValueModel error(Throwable throwable) {
        this.error = throwable;
        return this;
    }

    /**
     * Error state.
     *
     * @return error state result
     */
    @NGetter
    public Boolean errorState() {
        return errorState;
    }

    /**
     * Error state.
     *
     * @param errorState error state
     * @return error state result
     */
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
        /**
         * Clone.
         *
         * @return clone result
         */
        return clone();
    }

    /**
     * Clone.
     *
     * @return clone result
     */
    protected NOnceValueModel clone(){
        try {
          /**
           * Return.
           *
           * @param super.clone( super.clone(
           */
            return (NOnceValueModel) super.clone();
        } catch (CloneNotSupportedException e) {
            /**
             * Runtime exception.
             *
             * @param e e
             * @return runtime exception result
             */
            throw new RuntimeException(e);
        }
    }
}
