package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NCopiable;
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
    private Throwable throwable;

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
    public boolean isInvalidated() {
        return invalidated;
    }

    public NCachedValueModel setInvalidated(boolean invalidated) {
        this.invalidated = invalidated;
        return this;
    }

    public Object getThrowable() {
        return throwable;
    }

    public NCachedValueModel setThrowable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    public String getId() {
        return id;
    }

    public NCachedValueModel setId(String id) {
        this.id = id;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public NCachedValueModel setValue(Object value) {
        this.value = value;
        return this;
    }

    public Boolean getErrorState() {
        return errorState;
    }

    public NCachedValueModel setErrorState(Boolean errorState) {
        this.errorState = errorState;
        return this;
    }

    public Object getLastValidValue() {
        return lastValidValue;
    }

    public NCachedValueModel setLastValidValue(Object lastValidValue) {
        this.lastValidValue = lastValidValue;
        return this;
    }

    public long getLastEvalTimestamp() {
        return lastEvalTimestamp;
    }

    public NCachedValueModel setLastEvalTimestamp(long lastEvalTimestamp) {
        this.lastEvalTimestamp = lastEvalTimestamp;
        return this;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public NCachedValueModel setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
        return this;
    }

    public NDuration getExpiry() {
        return expiry;
    }

    public NCachedValueModel setExpiry(NDuration expiry) {
        this.expiry = expiry;
        return this;
    }

    public NDuration getRetryPeriod() {
        return retryPeriod;
    }

    public NCachedValueModel setRetryPeriod(NDuration retryPeriod) {
        this.retryPeriod = retryPeriod;
        return this;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public NCachedValueModel setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    public boolean isRetainLastOnFailure() {
        return retainLastOnFailure;
    }

    public NCachedValueModel setRetainLastOnFailure(boolean retainLastOnFailure) {
        this.retainLastOnFailure = retainLastOnFailure;
        return this;
    }

    public NCachedValueModel copy(){
        return clone();
    }

    protected NCachedValueModel clone(){
        try {
            return (NCachedValueModel) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
