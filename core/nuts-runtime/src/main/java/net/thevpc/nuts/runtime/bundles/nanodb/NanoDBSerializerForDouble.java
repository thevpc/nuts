package net.thevpc.nuts.runtime.bundles.nanodb;

import net.thevpc.nuts.NutsSession;

class NanoDBSerializerForDouble extends NanoDBNonNullSerializer<Double> {
    public NanoDBSerializerForDouble() {
        super(Double.class);
    }

    @Override
    public void write(Double obj, NanoDBOutputStream out, NutsSession session) {
        out.writeDouble((double) obj);
    }

    @Override
    public Double read(NanoDBInputStream in, Class expectedType, NutsSession session) {
        return in.readDouble();
    }
}
