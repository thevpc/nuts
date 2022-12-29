package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import net.thevpc.nuts.NSession;

class NanoDBSerializerForLong extends NanoDBNonNullSerializer<Long> {
    public NanoDBSerializerForLong() {
        super(Long.class);
    }

    @Override
    public void write(Long obj, NanoDBOutputStream out, NSession session) {
        out.writeLong((long) obj);
    }

    @Override
    public Long read(NanoDBInputStream in, Class expectedType, NSession session) {
        return in.readLong();
    }
}
