package net.thevpc.nuts.runtime.bundles.nanodb;

public abstract class NanoDBNullSerializer<T> extends NanoDBAbstractSerializer<T> {

    public NanoDBNullSerializer(Class<T> supportedType) {
        super(supportedType);
    }

    public abstract void writeNonNull(T obj, NanoDBOutputStream out);

    public abstract T readNonNull(NanoDBInputStream in);

    public final void write(T obj, NanoDBOutputStream out) {
        T i = (T) obj;
        if (i == null) {
            out.writeByte(0);
        } else {
            out.writeByte(1);
            writeNonNull(i, out);
        }
    }

    @Override
    public final T read(NanoDBInputStream in) {
        byte b = in.readByte();
        if (b == 0) {
            return null;
        }
        return readNonNull(in);
    }
}
