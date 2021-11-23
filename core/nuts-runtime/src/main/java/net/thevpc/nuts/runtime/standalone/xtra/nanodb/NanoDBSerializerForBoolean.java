package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import net.thevpc.nuts.NutsSession;

class NanoDBSerializerForBoolean extends NanoDBNonNullSerializer<Boolean> {
    public NanoDBSerializerForBoolean() {
        super(Boolean.class);
    }

    @Override
    public void write(Boolean obj, NanoDBOutputStream out, NutsSession session) {
        out.writeBoolean((Boolean) obj);
    }

    @Override
    public Boolean read(NanoDBInputStream in, Class expectedType, NutsSession session) {
        return in.readBoolean();
    }
}
