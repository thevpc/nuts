package net.thevpc.nuts;

public interface NutsSupported<T> {
    static <T> NutsSupported<T> of(T value, int supportLevel) {
        return supportLevel <= 0 ? invalid() : new NutsDefaultSupported<>(value, supportLevel);
    }

    static <T> NutsSupported<T> invalid() {
        return NutsDefaultSupported.INVALID;
    }

    T getValue();

    default boolean isValid() {
        return getSupportLevel() > 0;
    }

    int getSupportLevel();
}
