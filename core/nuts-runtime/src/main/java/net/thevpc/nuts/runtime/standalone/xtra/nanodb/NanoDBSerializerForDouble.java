package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import net.thevpc.nuts.NSession;

class NanoDBSerializerForDouble extends NanoDBNonNullSerializer<Double> {
    public NanoDBSerializerForDouble() {
        super(Double.class);
    }

    @Override
    public void write(Double obj, NanoDBOutputStream out, NSession session) {
        out.writeDouble((double) obj);
    }

    @Override
    public Double read(NanoDBInputStream in, Class expectedType, NSession session) {
        return in.readDouble();
    }
}
