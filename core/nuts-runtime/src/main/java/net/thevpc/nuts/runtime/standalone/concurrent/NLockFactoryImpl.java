package net.thevpc.nuts.runtime.standalone.concurrent;

import net.thevpc.nuts.concurrent.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NBlankable;

import java.util.UUID;

public class NLockFactoryImpl implements NLockFactory {
    private final NLockStore store;

    public NLockFactoryImpl(NLockStore store) {
        this.store = store;
    }

    @Override
    public NLockStore store() {
        return store;
    }

    @Override
    public NLockFactory withStore(NLockStore store) {
        return new NLockFactoryImpl(store);
    }

    @Override
    public NLock of(String id) {
        String nid = NBlankable.isBlank(id) ? UUID.randomUUID().toString() : id;
        if (store != null) {
            return new NStoreNLock(store, nid);
        }
        return ofBuilder(nid).build();
    }

    @Override
    public NLock of(Object target) {
        return ofBuilder().target(target).build();
    }

    @Override
    public NLock ofFile(NPath lockFile) {
        return ofBuilder().lockFile(lockFile).build();
    }

    @Override
    public NLock ofCompanion(NPath targetPath) {
        return ofBuilder().companion(targetPath).build();
    }

    @Override
    public NLock ofCompanion(NPath targetPath, String companionNameOrSuffix) {
        return ofBuilder().companion(targetPath, companionNameOrSuffix).build();
    }

    @Override
    public NLockBuilder ofBuilder(String id) {
        NLockBuilder b = NLockBuilder.of();
        if (id != null) {
            b.id(id);
        }
        if (store != null) {
            b.store(store);
        }
        return b;
    }

    @Override
    public NLockBuilder ofBuilder() {
        return ofBuilder(null);
    }
}
