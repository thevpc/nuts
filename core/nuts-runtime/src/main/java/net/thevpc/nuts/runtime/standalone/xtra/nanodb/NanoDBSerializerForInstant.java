package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import java.time.Instant;

class NanoDBSerializerForInstant extends NanoDBNonNullSerializer<Instant> {
    public NanoDBSerializerForInstant() {
        super(Instant.class);
    }

    @Override
    public void write(Instant obj, NanoDBOutputStream out) {
        out.writeLong(obj.getEpochSecond());
        out.writeLong(obj.getNano());
    }

    @Override
    public Instant read(NanoDBInputStream in, Class expectedType) {
        long epochSecond = in.readLong();
        long nanoAdjustment = in.readLong();
        return Instant.ofEpochSecond(epochSecond, nanoAdjustment);
    }
}
