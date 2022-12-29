package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import net.thevpc.nuts.NSession;

class NanoDBSerializerForBoolean extends NanoDBNonNullSerializer<Boolean> {
    public NanoDBSerializerForBoolean() {
        super(Boolean.class);
    }

    @Override
    public void write(Boolean obj, NanoDBOutputStream out, NSession session) {
        out.writeBoolean((Boolean) obj);
    }

    @Override
    public Boolean read(NanoDBInputStream in, Class expectedType, NSession session) {
        return in.readBoolean();
    }
}
