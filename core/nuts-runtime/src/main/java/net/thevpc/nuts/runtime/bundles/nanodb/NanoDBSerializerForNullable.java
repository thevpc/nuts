package net.thevpc.nuts.runtime.bundles.nanodb;

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
    public T readNonNull(NanoDBInputStream in) {
        return nonNullSer.read(in);
    }
}
