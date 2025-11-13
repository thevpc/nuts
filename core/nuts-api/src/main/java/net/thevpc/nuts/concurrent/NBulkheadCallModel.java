package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NCopiable;

import java.util.function.IntFunction;

/**
 * Represents the configuration and runtime metadata of a bulkhead-protected call.
 * A bulkhead call model defines concurrency limits and permit expiry rules,
 * allowing the system to restrict the number of concurrent invocations of a given task.
 *
 * @since 0.8.8
 */
public class NBulkheadCallModel implements Cloneable, NCopiable {
    /**
     * Identifier of the bulkhead call.
     */
    private String id;

    /**
     * Maximum number of concurrent executions allowed within this bulkhead.
     */
    private int maxConcurrent;

    /**
     * Duration after which a permit is considered expired.
     * Default value is 5 minutes for persistent backends.
     */
    private NDuration permitExpiry = NDuration.ofMinutes(5); // default for persistent backends


    /**
     * Original caller task associated with this model.
     */
    private NCallable<?> caller;

    /**
     * Creates an empty bulkhead call model.
     */
    public NBulkheadCallModel() {
    }

    /**
     * Creates a bulkhead call model with the given identifier.
     *
     * @param id bulkhead identifier
     */
    public NBulkheadCallModel(String id) {
        this.id = id;
    }

    /**
     * Returns the identifier of this bulkhead call.
     *
     * @return bulkhead identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the identifier for this bulkhead call.
     *
     * @param id bulkhead identifier
     * @return this instance for chaining
     */
    public NBulkheadCallModel setId(String id) {
        this.id = id;
        return this;
    }


    /**
     * Returns the permit expiry duration.
     *
     * @return expiry duration
     */
    public NDuration getPermitExpiry() {
        return permitExpiry;
    }

    /**
     * Sets the permit expiry duration.
     *
     * @param permitExpiry expiry duration
     * @return this instance for chaining
     */
    public NBulkheadCallModel setPermitExpiry(NDuration permitExpiry) {
        this.permitExpiry = permitExpiry;
        return this;
    }

    /**
     * Returns the maximum number of concurrent executions allowed.
     *
     * @return maximum concurrent calls
     */
    public int getMaxConcurrent() {
        return maxConcurrent;
    }

    /**
     * Sets the maximum number of concurrent executions allowed.
     *
     * @param maxConcurrent concurrency limit
     * @return this instance for chaining
     */
    public NBulkheadCallModel setMaxConcurrent(int maxConcurrent) {
        this.maxConcurrent = maxConcurrent;
        return this;
    }

    /**
     * Returns the original caller task associated with this model.
     *
     * @return associated callable
     */
    public NCallable<?> getCaller() {
        return caller;
    }

    /**
     * Sets the caller task associated with this model.
     *
     * @param caller callable task
     * @return this instance for chaining
     */
    public NBulkheadCallModel setCaller(NCallable<?> caller) {
        this.caller = caller;
        return this;
    }

    /**
     * Creates a deep copy of this model.
     *
     * @return copied model
     */
    public NBulkheadCallModel copy(){
        return clone();
    }

    /**
     * Clones this model.
     *
     * @return cloned model
     */
    protected NBulkheadCallModel clone(){
        try {
            return (NBulkheadCallModel) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
