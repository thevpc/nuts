package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.*;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents the state model of an {@link NLock} instance.
 * <p>
 * This model holds lock identification, current owner, acquisition timestamp,
 * expiration timestamp for distributed lease management, and re-entrant hold count.
 *
 * @since 0.8.8
 */
public class NLockModel implements Cloneable, NCopiable {

    /**
     * Unique identifier for this lock.
     */
    private String id;

    /**
     * Current owner identifier holding this lock (e.g. process-id / thread-id / node-id).
     */
    private String owner;

    /**
     * Timestamp when the lock was acquired.
     */
    private Instant acquiredAt;

    /**
     * Timestamp when the lock lease expires.
     */
    private Instant expiresAt;

    /**
     * Re-entrant hold count for this lock.
     */
    private int holdCount;

    public NLockModel() {
    }

    public NLockModel(String id) {
        this.id = id;
    }

    public NLockModel(String id, String owner, Instant acquiredAt, Instant expiresAt) {
        this.id = id;
        this.owner = owner;
        this.acquiredAt = acquiredAt;
        this.expiresAt = expiresAt;
    }

    @NGetter
    public String id() {
        return id;
    }

    @NSetter
    public NLockModel id(String id) {
        this.id = id;
        return this;
    }

    @NGetter
    public String owner() {
        return owner;
    }

    @NSetter
    public NLockModel owner(String owner) {
        this.owner = owner;
        return this;
    }

    @NGetter
    public Instant acquiredAt() {
        return acquiredAt;
    }

    @NSetter
    public NLockModel acquiredAt(Instant acquiredAt) {
        this.acquiredAt = acquiredAt;
        return this;
    }

    @NGetter
    public Instant expiresAt() {
        return expiresAt;
    }

    @NSetter
    public NLockModel expiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    @NGetter
    public int holdCount() {
        return holdCount;
    }

    @NSetter
    public NLockModel holdCount(int holdCount) {
        this.holdCount = holdCount;
        return this;
    }

    /**
     * Checks if the lock lease has expired relative to the given instant.
     *
     * @param now current instant
     * @return true if expired
     */
    public boolean isExpired(Instant now) {
        return expiresAt != null && (now == null ? Instant.now() : now).isAfter(expiresAt);
    }

    /**
     * Checks if the lock lease has expired relative to current time.
     *
     * @return true if expired
     */
    public boolean isExpired() {
        return isExpired(Instant.now());
    }

    @Override
    public NLockModel copy() {
        return clone();
    }

    @Override
    protected NLockModel clone() {
        try {
            return (NLockModel) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new NUnexpectedException(NMsg.ofC("clone unsupported for %s", getClass()), e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NLockModel that = (NLockModel) o;
        return holdCount == that.holdCount &&
                Objects.equals(id, that.id) &&
                Objects.equals(owner, that.owner) &&
                Objects.equals(acquiredAt, that.acquiredAt) &&
                Objects.equals(expiresAt, that.expiresAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, owner, acquiredAt, expiresAt, holdCount);
    }

    @Override
    public String toString() {
        return NToStringBuilder.of(this).omitBlanks(true)
                .add("id", id)
                .add("owner", owner)
                .add("acquiredAt", acquiredAt)
                .add("expiresAt", expiresAt)
                .add("holdCount", holdCount)
                .build();
    }
}
