package net.thevpc.nuts;

import java.util.concurrent.ExecutorService;

public interface NutsConcurrentManager {

    /**
     * create new {@link NutsIOLockAction} instance
     *
     * @return create new {@link NutsIOLockAction} instance
     */
    NutsIOLockAction lock();

    /**
     * return non null executor service
     *
     * @return non null executor service
     */
    ExecutorService executorService();
}
