package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import net.thevpc.nuts.NSession;

class NanoDBSerializerForString extends NanoDBNonNullSerializer<String>{
    public NanoDBSerializerForString() {
        super(String.class);
    }

    @Override
    public void write(String obj, NanoDBOutputStream out, NSession session) {
        out.writeUTF(obj);
    }

    @Override
    public String read(NanoDBInputStream in, Class expectedType, NSession session) {
        return in.readUTF();
    }
}
