package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NCopiable;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NSetter;
import net.thevpc.nuts.util.NUnexpectedException;

/**
 * Internal data model representing the persisted state of a cached value.
 * <p>
 * This class is not intended for direct use by application developers.
 * It is used internally by {@link NCachedValueFactory} and {@link NCachedValueStore}
 * implementations to store and exchange metadata associated with cached values.
 * </p>
 *
 * <p>
 * The model holds all the information required to manage the lifecycle
 * of a cached entry, including:
 * </p>
 * <ul>
 *     <li>the current and last valid values</li>
 *     <li>timestamps and retry counters</li>
 *     <li>expiry and retry configuration</li>
 *     <li>failure tracking and invalidation flags</li>
 * </ul>
 *
 * <p>
 * Implementations of {@link NCachedValueStore} are responsible for
 * persisting or reconstructing this model when cache entries are loaded or saved.
 * </p>
 *
 * @implNote
 * This class performs only shallow cloning. It is designed to be a simple,
 * serializable-like data carrier between caching components.
 *
 * @see NCachedValue
 * @see NCachedValueFactory
 * @see NCachedValueStore
 * @since 0.8.6
 */
public class NCachedValueModel implements Cloneable, NCopiable {

    /** Unique identifier for the cached entry. */
    private String id;

    /** The most recently computed value (may be {@code null}). */
    private Object value;

    /** The last thrown exception during value computation, if any. */
    private Throwable error;

    /** Indicates whether the cache entry has been explicitly invalidated. */
    private boolean invalidated;

    /** Whether the cache is in an error state (e.g., repeated failures). */
    private Boolean errorState;

    /** The last known valid value, used if {@code retainLastOnFailure} is enabled. */
    private Object lastValidValue = null;

    /** Timestamp of the last successful or attempted evaluation, in milliseconds. */
    private long lastEvalTimestamp = 0;

    /** Number of consecutive failed attempts since last success. */
    private int failedAttempts = 0;

    /** Maximum duration before the cached value expires. */
    private NDuration expiry = NDuration.ofMillis(Long.MAX_VALUE);

    /** Minimum wait period before retrying after a failure. */
    private NDuration retryPeriod = NDuration.ZERO;

    /** Maximum number of retries allowed after failure before marking error state. */
    private int maxRetries = 0;

    /** Whether to retain the last valid value if computation fails. */
    private boolean retainLastOnFailure = false;

    /** Creates an empty model with default settings. */
    public NCachedValueModel() {
    }

    /** Creates a model associated with a specific identifier. */
    public NCachedValueModel(String id) {
        this.id = id;
    }

    // ---- Getters / Setters ----
    /**
     * Checks if is invalidated.
     *
     * @return is invalidated result
     */
    @NGetter
    public boolean isInvalidated() {
        return invalidated;
    }

    /**
     * Invalidated.
     *
     * @param invalidated invalidated
     * @return invalidated result
     */
    @NSetter
    public NCachedValueModel invalidated(boolean invalidated) {
        this.invalidated = invalidated;
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
    public NCachedValueModel error(Throwable throwable) {
        this.error = throwable;
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
    @NSetter
    public NCachedValueModel id(String id) {
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
    public NCachedValueModel value(Object value) {
        this.value = value;
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
    public NCachedValueModel errorState(Boolean errorState) {
        this.errorState = errorState;
        return this;
    }

    /**
     * Last valid value.
     *
     * @return last valid value result
     */
    @NGetter
    public Object lastValidValue() {
        return lastValidValue;
    }

    /**
     * Last valid value.
     *
     * @param lastValidValue last valid value
     * @return last valid value result
     */
    @NSetter
    public NCachedValueModel lastValidValue(Object lastValidValue) {
        this.lastValidValue = lastValidValue;
        return this;
    }

    /**
     * Last eval timestamp.
     *
     * @return last eval timestamp result
     */
    @NGetter
    public long lastEvalTimestamp() {
        return lastEvalTimestamp;
    }

    /**
     * Last eval timestamp.
     *
     * @param lastEvalTimestamp last eval timestamp
     * @return last eval timestamp result
     */
    @NSetter
    public NCachedValueModel lastEvalTimestamp(long lastEvalTimestamp) {
        this.lastEvalTimestamp = lastEvalTimestamp;
        return this;
    }

    /**
     * Failed attempts.
     *
     * @return failed attempts result
     */
    @NGetter
    public int failedAttempts() {
        return failedAttempts;
    }

    /**
     * Failed attempts.
     *
     * @param failedAttempts failed attempts
     * @return failed attempts result
     */
    @NSetter
    public NCachedValueModel failedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
        return this;
    }

    /**
     * Expiry.
     *
     * @return expiry result
     */
    @NGetter
    public NDuration expiry() {
        return expiry;
    }

    /**
     * Expiry.
     *
     * @param expiry expiry
     * @return expiry result
     */
    @NSetter
    public NCachedValueModel expiry(NDuration expiry) {
        this.expiry = expiry;
        return this;
    }

    /**
     * Retry period.
     *
     * @return retry period result
     */
    @NGetter
    public NDuration retryPeriod() {
        return retryPeriod;
    }

    /**
     * Retry period.
     *
     * @param retryPeriod retry period
     * @return retry period result
     */
    @NSetter
    public NCachedValueModel retryPeriod(NDuration retryPeriod) {
        this.retryPeriod = retryPeriod;
        return this;
    }

    /**
     * Max retries.
     *
     * @return max retries result
     */
    @NGetter
    public int maxRetries() {
        return maxRetries;
    }

    /**
     * Max retries.
     *
     * @param maxRetries max retries
     * @return max retries result
     */
    @NSetter
    public NCachedValueModel maxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    /**
     * Checks if is retain last on failure.
     *
     * @return is retain last on failure result
     */
    @NGetter
    public boolean isRetainLastOnFailure() {
        return retainLastOnFailure;
    }

    /**
     * Retain last on failure.
     *
     * @param retainLastOnFailure retain last on failure
     * @return retain last on failure result
     */
    @NSetter
    public NCachedValueModel retainLastOnFailure(boolean retainLastOnFailure) {
        this.retainLastOnFailure = retainLastOnFailure;
        return this;
    }

    /**
     * Copy.
     *
     * @return copy result
     */
    public NCachedValueModel copy(){
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
    protected NCachedValueModel clone(){
        try {
          /**
           * Return.
           *
           * @param super.clone( super.clone(
           */
            return (NCachedValueModel) super.clone();
        } catch (CloneNotSupportedException e) {
            /**
             * Runtime exception.
             *
             * @param e e
             * @return runtime exception result
             */
            throw new NUnexpectedException(NMsg.ofC("clone unsupported for %s",getClass()),e);
        }
    }
}
