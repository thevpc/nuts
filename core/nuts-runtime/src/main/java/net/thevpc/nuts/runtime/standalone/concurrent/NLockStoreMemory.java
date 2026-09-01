package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.NLockModel;
import net.thevpc.nuts.concurrent.NLockStore;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NAssert;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class NLockStoreMemory implements NLockStore {
    private final Map<String, NLockModel> locks = new HashMap<>();

    @Override
    public NLockModel load(String id) {
        if (id == null) {
            return null;
        }
        synchronized (locks) {
            NLockModel model = locks.get(id);
            if (model != null) {
                if (model.isExpired()) {
                    locks.remove(id);
                    return null;
                }
                return model.copy();
            }
            return null;
        }
    }

    @Override
    public boolean tryAcquire(String id, String owner, NDuration leaseDuration) {
        NAssert.requireNamedNonBlank(id, "id");
        NAssert.requireNamedNonBlank(owner, "owner");
        synchronized (locks) {
            Instant now = Instant.now();
            NLockModel existing = locks.get(id);
            if (existing != null) {
                if (existing.isExpired(now)) {
                    locks.remove(id);
                    existing = null;
                } else if (!owner.equals(existing.owner())) {
                    return false;
                }
            }

            Instant expiresAt = null;
            if (leaseDuration != null && leaseDuration.toMillis() > 0) {
                expiresAt = now.plusMillis(leaseDuration.toMillis());
            }

            if (existing == null) {
                NLockModel model = new NLockModel(id, owner, now, expiresAt);
                model.holdCount(1);
                locks.put(id, model);
            } else {
                existing.expiresAt(expiresAt);
                existing.acquiredAt(now);
            }
            return true;
        }
    }

    @Override
    public boolean renew(String id, String owner, NDuration leaseDuration) {
        if (id == null || owner == null) {
            return false;
        }
        synchronized (locks) {
            NLockModel existing = locks.get(id);
            if (existing != null && owner.equals(existing.owner()) && !existing.isExpired()) {
                Instant expiresAt = null;
                if (leaseDuration != null && leaseDuration.toMillis() > 0) {
                    expiresAt = Instant.now().plusMillis(leaseDuration.toMillis());
                }
                existing.expiresAt(expiresAt);
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean release(String id, String owner) {
        if (id == null || owner == null) {
            return false;
        }
        synchronized (locks) {
            NLockModel existing = locks.get(id);
            if (existing != null && owner.equals(existing.owner())) {
                locks.remove(id);
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean delete(String id) {
        if (id == null) {
            return false;
        }
        synchronized (locks) {
            return locks.remove(id) != null;
        }
    }
}
