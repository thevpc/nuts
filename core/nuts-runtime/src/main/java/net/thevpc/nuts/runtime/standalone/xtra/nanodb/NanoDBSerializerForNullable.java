package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import net.thevpc.nuts.NutsSession;

public class NanoDBSerializerForNullable<T> extends NanoDBNullSerializer<T>{
    private NanoDBSerializer<T> nonNullSer;

    public NanoDBSerializerForNullable(NanoDBSerializer<T> nonNullSer) {
        super(nonNullSer.getSupportedType());
        this.nonNullSer = nonNullSer;
    }

    @Override
    public void writeNonNull(T obj, NanoDBOutputStream out, NutsSession session) {
        nonNullSer.write(obj,out, session);
    }

    @Override
    public T readNonNull(NanoDBInputStream in, Class expectedType, NutsSession session) {
        return nonNullSer.read(in, expectedType, session);
    }
}
