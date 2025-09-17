package net.thevpc.nuts.concurrent;

import java.io.Serializable;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

public class NRateLimitedValueModel implements Serializable {
    private String id;
    private Instant lastAccess;
    private NRateLimitStrategyModel[] constraints;

    public NRateLimitedValueModel(String id, Instant lastAccess, NRateLimitStrategyModel[] constraints) {
        this.id = id == null ? "" : id;
        this.lastAccess = lastAccess;
        this.constraints = constraints;
    }

    public String getId() {
        return id;
    }

    public Instant getLastAccess() {
        return lastAccess;
    }

    public NRateLimitStrategyModel[] getConstraints() {
        return constraints;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NRateLimitedValueModel that = (NRateLimitedValueModel) o;
        return Objects.equals(id, that.id) && Objects.equals(lastAccess, that.lastAccess) && Objects.deepEquals(constraints, that.constraints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, lastAccess, Arrays.hashCode(constraints));
    }

    @Override
    public String toString() {
        return "NLimitedValueData{" +
                "uuid='" + id + '\'' +
                ", lastAccess=" + lastAccess +
                ", constraints=" + Arrays.toString(constraints) +
                '}';
    }
}
