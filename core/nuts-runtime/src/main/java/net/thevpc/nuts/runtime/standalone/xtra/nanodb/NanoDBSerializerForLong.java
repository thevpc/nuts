package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import net.thevpc.nuts.NutsSession;

class NanoDBSerializerForLong extends NanoDBNonNullSerializer<Long> {
    public NanoDBSerializerForLong() {
        super(Long.class);
    }

    @Override
    public void write(Long obj, NanoDBOutputStream out, NutsSession session) {
        out.writeLong((long) obj);
    }

    @Override
    public Long read(NanoDBInputStream in, Class expectedType, NutsSession session) {
        return in.readLong();
    }
}
