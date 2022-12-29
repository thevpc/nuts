package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import net.thevpc.nuts.NSession;

import java.time.Instant;

class NanoDBSerializerForInstant extends NanoDBNonNullSerializer<Instant> {
    public NanoDBSerializerForInstant() {
        super(Instant.class);
    }

    @Override
    public void write(Instant obj, NanoDBOutputStream out, NSession session) {
        out.writeLong(obj.getEpochSecond());
        out.writeLong(obj.getNano());
    }

    @Override
    public Instant read(NanoDBInputStream in, Class expectedType, NSession session) {
        long epochSecond = in.readLong();
        long nanoAdjustment = in.readLong();
        return Instant.ofEpochSecond(epochSecond, nanoAdjustment);
    }
}
