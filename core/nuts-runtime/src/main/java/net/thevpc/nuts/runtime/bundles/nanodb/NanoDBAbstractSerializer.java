package net.thevpc.nuts.runtime.bundles.nanodb;

public abstract class NanoDBAbstractSerializer<T> implements NanoDBSerializer<T> {
    private Class<T> supportedType;

    public NanoDBAbstractSerializer(Class<T> supportedType) {
        this.supportedType = supportedType;
    }

    @Override
    public Class<T> getSupportedType() {
        return supportedType;
    }
}
