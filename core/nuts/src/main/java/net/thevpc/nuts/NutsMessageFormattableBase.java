package net.thevpc.nuts;

public abstract class NutsMessageFormattableBase implements NutsMessageFormattable {
    @Override
    public String toString() {
        return formatMessage().toString();
    }
}
