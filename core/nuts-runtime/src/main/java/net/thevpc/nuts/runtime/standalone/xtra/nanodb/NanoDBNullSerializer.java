package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import net.thevpc.nuts.NSession;

public abstract class NanoDBNullSerializer<T> extends NanoDBAbstractSerializer<T> {

    public NanoDBNullSerializer(Class<T> supportedType) {
        super(supportedType);
    }

    public abstract void writeNonNull(T obj, NanoDBOutputStream out, NSession session);

    public abstract T readNonNull(NanoDBInputStream in, Class expectedType, NSession session);

    public final void write(T obj, NanoDBOutputStream out, NSession session) {
        T i = (T) obj;
        if (i == null) {
            out.writeByte(0);
        } else {
            out.writeByte(1);
            writeNonNull(i, out, session);
        }
    }

    @Override
    public final T read(NanoDBInputStream in, Class expectedType, NSession session) {
        byte b = in.readByte();
        if (b == 0) {
            return null;
        }
        return readNonNull(in, expectedType, session);
    }
}
