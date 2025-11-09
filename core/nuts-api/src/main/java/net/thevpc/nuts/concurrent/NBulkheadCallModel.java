package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NCopiable;

import java.util.function.IntFunction;

/**
 * @since 0.8.8
 */
public class NBulkheadCallModel implements Cloneable, NCopiable {
    private String id;
    private int maxConcurrent;
    private NDuration permitExpiry = NDuration.ofMinutes(5); // default for persistent backends


    /** Original caller task associated with this model. */
    private NCallable<?> caller;

    public NBulkheadCallModel() {
    }

    public NBulkheadCallModel(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public NBulkheadCallModel setId(String id) {
        this.id = id;
        return this;
    }

    public NDuration getPermitExpiry() {
        return permitExpiry;
    }

    public NBulkheadCallModel setPermitExpiry(NDuration permitExpiry) {
        this.permitExpiry = permitExpiry;
        return this;
    }

    public int getMaxConcurrent() {
        return maxConcurrent;
    }

    public NBulkheadCallModel setMaxConcurrent(int maxConcurrent) {
        this.maxConcurrent = maxConcurrent;
        return this;
    }

    public NCallable<?> getCaller() {
        return caller;
    }

    public NBulkheadCallModel setCaller(NCallable<?> caller) {
        this.caller = caller;
        return this;
    }

    public NBulkheadCallModel copy(){
        return clone();
    }

    protected NBulkheadCallModel clone(){
        try {
            return (NBulkheadCallModel) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
