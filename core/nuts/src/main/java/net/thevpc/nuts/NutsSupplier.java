package net.thevpc.nuts;

public interface NutsSupplier<T> {
    int getLevel();

    T create();
}
