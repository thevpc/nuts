package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

public abstract class NanoDBNullSerializer<T> extends NanoDBAbstractSerializer<T> {

    public NanoDBNullSerializer(Class<T> supportedType) {
        super(supportedType);
    }

    public abstract void writeNonNull(T obj, NanoDBOutputStream out);

    public abstract T readNonNull(NanoDBInputStream in, Class expectedType);

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
    public final T read(NanoDBInputStream in, Class expectedType) {
        byte b = in.readByte();
        if (b == 0) {
            return null;
        }
        return readNonNull(in, expectedType);
    }
}
