package net.thevpc.nuts.runtime.bundles.nanodb;

class NanoDBSerializerForInteger extends NanoDBNonNullSerializer<Integer> {
    public NanoDBSerializerForInteger() {
        super(Integer.class);
    }

    @Override
    public void write(Integer obj, NanoDBOutputStream out) {
        out.writeInt((int) obj);
    }

    @Override
    public Integer read(NanoDBInputStream in) {
        return in.readInt();
    }
}
