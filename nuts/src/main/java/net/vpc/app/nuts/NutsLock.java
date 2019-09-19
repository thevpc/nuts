package net.vpc.app.nuts;

public interface NutsLock {
    void checkFree();

    boolean isFree();

    void acquire();

    void release();
}
