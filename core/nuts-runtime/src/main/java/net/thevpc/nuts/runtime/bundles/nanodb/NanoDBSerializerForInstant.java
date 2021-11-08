package net.thevpc.nuts.runtime.bundles.nanodb;

import net.thevpc.nuts.NutsSession;

import java.time.Instant;

class NanoDBSerializerForInstant extends NanoDBNonNullSerializer<Instant> {
    public NanoDBSerializerForInstant() {
        super(Instant.class);
    }

    @Override
    public void write(Instant obj, NanoDBOutputStream out, NutsSession session) {
        out.writeLong(obj.getEpochSecond());
        out.writeLong(obj.getNano());
    }

    @Override
    public Instant read(NanoDBInputStream in, Class expectedType, NutsSession session) {
        long epochSecond = in.readLong();
        long nanoAdjustment = in.readLong();
        return Instant.ofEpochSecond(epochSecond, nanoAdjustment);
    }
}
