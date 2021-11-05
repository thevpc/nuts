package net.thevpc.nuts;

public class NutsDefaultSupported<T> implements NutsSupported<T>{
    public static final NutsSupported INVALID = new NutsDefaultSupported(null,-1);
    private T value;
    private int supportLevel;

    public NutsDefaultSupported(T value, int supportLevel) {
        this.value = value;
        this.supportLevel = supportLevel;
    }

    public T getValue() {
        return value;
    }

    public int getSupportLevel() {
        return supportLevel;
    }
}
