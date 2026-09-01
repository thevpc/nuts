package net.thevpc.nuts.concurrent;

import net.thevpc.nuts.internal.rpi.NConcurrentRPI;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NSetter;

import java.io.File;
import java.nio.file.Path;

/**
 * Factory interface for creating {@link NLock} instances.
 * <p>
 * An {@code NLockFactory} produces and configures locks backed by a pluggable {@link NLockStore}
 * (e.g. in-memory, file-based, JDBC database, or distributed cache).
 *
 * @see NLock
 * @see NLockStore
 * @see NLockBuilder
 * @since 0.8.8
 */
public interface NLockFactory {

    /**
     * Returns the currently configured {@link NLockFactory}.
     *
     * @return the lock factory instance
     */
    static NLockFactory of() {
        return NConcurrentRPI.of().lockFactory();
    }

    /**
     * Creates a new {@link NLockFactory} backed by the given {@link NLockStore}.
     *
     * @param store the lock store backend to use
     * @return a new lock factory instance configured with the store
     */
    static NLockFactory of(NLockStore store) {
        return NConcurrentRPI.of().lockFactory().withStore(store);
    }

    /**
     * Returns a memory-only {@link NLockFactory}.
     *
     * @return memory lock factory
     */
    static NLockFactory ofMem() {
        return NConcurrentRPI.of().memoryLockFactory();
    }

    /**
     * Returns the default {@link NLockFactory}.
     *
     * @return default lock factory
     */
    static NLockFactory ofDefault() {
        return NConcurrentRPI.of().defaultLockFactory();
    }

    /**
     * Configures the global {@link NLockFactory}.
     *
     * @param factory the factory to set as global default
     */
    @NSetter
    static void configure(NLockFactory factory) {
        NConcurrentRPI.of().lockFactory(factory);
    }

    /**
     * Returns the backing store used by this factory.
     *
     * @return the lock store, or {@code null} if using direct file/memory locks
     */
    NLockStore store();

    /**
     * Returns a new factory instance using the provided store.
     *
     * @param store the store to use
     * @return a new factory instance configured with the given store
     */
    NLockFactory withStore(NLockStore store);

    /**
     * Creates a named lock for the given identifier.
     *
     * @param id the unique lock identifier
     * @return a new {@link NLock} instance
     */
    NLock of(String id);

    /**
     * Creates a lock for the given target object (entity, key, or path).
     *
     * @param target the target object to lock
     * @return a new {@link NLock} instance
     */
    NLock of(Object target);

    /**
     * Creates a direct file lock where the specified file IS the lock file.
     *
     * @param lockFile the lock file path
     * @return a new {@link NLock} instance
     */
    NLock ofFile(NPath lockFile);

    /**
     * Creates a direct file lock where the specified file IS the lock file.
     *
     * @param lockFile the lock file path
     * @return a new {@link NLock} instance
     */
    default NLock ofFile(Path lockFile) {
        return ofFile(NPath.of(lockFile));
    }

    /**
     * Creates a direct file lock where the specified file IS the lock file.
     *
     * @param lockFile the lock file
     * @return a new {@link NLock} instance
     */
    default NLock ofFile(File lockFile) {
        return ofFile(NPath.of(lockFile));
    }

    /**
     * Creates a companion lock that protects the given file or directory.
     *
     * @param targetPath the file or directory to protect
     * @return a new {@link NLock} instance
     */
    NLock ofCompanion(NPath targetPath);

    /**
     * Creates a companion lock that protects the given file or directory.
     *
     * @param targetPath the file or directory to protect
     * @return a new {@link NLock} instance
     */
    default NLock ofCompanion(Path targetPath) {
        return ofCompanion(NPath.of(targetPath));
    }

    /**
     * Creates a companion lock that protects the given file or directory.
     *
     * @param targetPath the file or directory to protect
     * @return a new {@link NLock} instance
     */
    default NLock ofCompanion(File targetPath) {
        return ofCompanion(NPath.of(targetPath));
    }

    /**
     * Creates a companion lock that protects the given file or directory using a custom suffix or companion name.
     *
     * @param targetPath            the file or directory to protect
     * @param companionNameOrSuffix custom lock suffix (e.g. {@code ".lock"}) or lock filename
     * @return a new {@link NLock} instance
     */
    NLock ofCompanion(NPath targetPath, String companionNameOrSuffix);

    /**
     * Creates a companion lock that protects the given file or directory using a custom suffix or companion name.
     *
     * @param targetPath            the file or directory to protect
     * @param companionNameOrSuffix custom lock suffix or filename
     * @return a new {@link NLock} instance
     */
    default NLock ofCompanion(Path targetPath, String companionNameOrSuffix) {
        return ofCompanion(NPath.of(targetPath), companionNameOrSuffix);
    }

    /**
     * Creates a companion lock that protects the given file or directory using a custom suffix or companion name.
     *
     * @param targetPath            the file or directory to protect
     * @param companionNameOrSuffix custom lock suffix or filename
     * @return a new {@link NLock} instance
     */
    default NLock ofCompanion(File targetPath, String companionNameOrSuffix) {
        return ofCompanion(NPath.of(targetPath), companionNameOrSuffix);
    }

    /**
     * Creates a builder initialized with the given lock ID.
     *
     * @param id unique lock identifier
     * @return a new lock builder
     */
    NLockBuilder ofBuilder(String id);

    /**
     * Creates a new empty lock builder.
     *
     * @return a new lock builder
     */
    NLockBuilder ofBuilder();
}
