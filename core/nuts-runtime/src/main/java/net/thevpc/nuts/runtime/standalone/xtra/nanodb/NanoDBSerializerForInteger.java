package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import net.thevpc.nuts.NSession;

class NanoDBSerializerForInteger extends NanoDBNonNullSerializer<Integer> {
    public NanoDBSerializerForInteger() {
        super(Integer.class);
    }

    @Override
    public void write(Integer obj, NanoDBOutputStream out, NSession session) {
        out.writeInt((int) obj);
    }

    @Override
    public Integer read(NanoDBInputStream in, Class expectedType, NSession session) {
        return in.readInt();
    }
}
