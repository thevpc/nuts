package net.thevpc.nuts.runtime.bundles.nanodb;

public abstract class NanoDBNonNullSerializer<T> extends NanoDBAbstractSerializer<T> {
    public NanoDBNonNullSerializer(Class<T> supportedType) {
        super(supportedType);
    }
}
