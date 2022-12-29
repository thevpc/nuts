package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import net.thevpc.nuts.NSession;

public class NanoDBSerializerForNullable<T> extends NanoDBNullSerializer<T>{
    private NanoDBSerializer<T> nonNullSer;

    public NanoDBSerializerForNullable(NanoDBSerializer<T> nonNullSer) {
        super(nonNullSer.getSupportedType());
        this.nonNullSer = nonNullSer;
    }

    @Override
    public void writeNonNull(T obj, NanoDBOutputStream out, NSession session) {
        nonNullSer.write(obj,out, session);
    }

    @Override
    public T readNonNull(NanoDBInputStream in, Class expectedType, NSession session) {
        return nonNullSer.read(in, expectedType, session);
    }
}
