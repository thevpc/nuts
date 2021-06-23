package net.thevpc.nuts;

public interface NutsSupplier<T> {
    int level();

    T create();
}
