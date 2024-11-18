package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

class NanoDBSerializerForLong extends NanoDBNonNullSerializer<Long> {
    public NanoDBSerializerForLong() {
        super(Long.class);
    }

    @Override
    public void write(Long obj, NanoDBOutputStream out) {
        out.writeLong((long) obj);
    }

    @Override
    public Long read(NanoDBInputStream in, Class expectedType) {
        return in.readLong();
    }
}
