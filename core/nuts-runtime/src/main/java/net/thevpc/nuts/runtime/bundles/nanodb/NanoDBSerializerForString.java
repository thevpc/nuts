package net.thevpc.nuts.runtime.bundles.nanodb;

import net.thevpc.nuts.NutsSession;

class NanoDBSerializerForString extends NanoDBNonNullSerializer<String>{
    public NanoDBSerializerForString() {
        super(String.class);
    }

    @Override
    public void write(String obj, NanoDBOutputStream out, NutsSession session) {
        out.writeUTF(obj);
    }

    @Override
    public String read(NanoDBInputStream in, Class expectedType, NutsSession session) {
        return in.readUTF();
    }
}
