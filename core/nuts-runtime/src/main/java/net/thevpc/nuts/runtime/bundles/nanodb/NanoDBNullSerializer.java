package net.thevpc.nuts.runtime.bundles.nanodb;

import net.thevpc.nuts.NutsSession;

public abstract class NanoDBNullSerializer<T> extends NanoDBAbstractSerializer<T> {

    public NanoDBNullSerializer(Class<T> supportedType) {
        super(supportedType);
    }

    public abstract void writeNonNull(T obj, NanoDBOutputStream out, NutsSession session);

    public abstract T readNonNull(NanoDBInputStream in, Class expectedType, NutsSession session);

    public final void write(T obj, NanoDBOutputStream out, NutsSession session) {
        T i = (T) obj;
        if (i == null) {
            out.writeByte(0);
        } else {
            out.writeByte(1);
            writeNonNull(i, out, session);
        }
    }

    @Override
    public final T read(NanoDBInputStream in, Class expectedType, NutsSession session) {
        byte b = in.readByte();
        if (b == 0) {
            return null;
        }
        return readNonNull(in, expectedType, session);
    }
}
