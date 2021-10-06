package net.thevpc.nuts.runtime.bundles.nanodb;

class NanoDBSerializerForLong extends NanoDBNonNullSerializer<Long> {
    public NanoDBSerializerForLong() {
        super(Long.class);
    }

    @Override
    public void write(Long obj, NanoDBOutputStream out) {
        out.writeLong((long) obj);
    }

    @Override
    public Long read(NanoDBInputStream in) {
        return in.readLong();
    }
}
