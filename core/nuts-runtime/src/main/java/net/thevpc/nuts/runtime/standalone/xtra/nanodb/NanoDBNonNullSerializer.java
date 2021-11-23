package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

public abstract class NanoDBNonNullSerializer<T> extends NanoDBAbstractSerializer<T> {
    public NanoDBNonNullSerializer(Class<T> supportedType) {
        super(supportedType);
    }
}
