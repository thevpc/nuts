package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

public class NanoDBSerializerForNullable<T> extends NanoDBNullSerializer<T>{
    private NanoDBSerializer<T> nonNullSer;

    public NanoDBSerializerForNullable(NanoDBSerializer<T> nonNullSer) {
        super(nonNullSer.getSupportedType());
        this.nonNullSer = nonNullSer;
    }

    @Override
    public void writeNonNull(T obj, NanoDBOutputStream out) {
        nonNullSer.write(obj,out);
    }

    @Override
    public T readNonNull(NanoDBInputStream in, Class expectedType) {
        return nonNullSer.read(in, expectedType);
    }
}
