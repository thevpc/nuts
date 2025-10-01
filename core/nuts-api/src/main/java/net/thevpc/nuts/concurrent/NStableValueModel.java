package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.util.NCopiable;

import java.util.function.Supplier;

/**
 * @since 0.8.6
 */
public class NStableValueModel implements Cloneable, NCopiable {
    private String id;
    private Object value;
    private Throwable throwable;
    private Boolean errorState;
    private Supplier<?> supplier;

    public NStableValueModel() {
    }

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
