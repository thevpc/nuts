package net.thevpc.nuts.runtime.standalone.repository.index;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDBInputStream;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDBNonNullSerializer;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDBOutputStream;

public class NanoDBNutsIdSerializer extends NanoDBNonNullSerializer<NutsId>{
    private final NutsSession session;

    public NanoDBNutsIdSerializer(NutsSession session) {
        super(NutsId.class);
        this.session = session;
    }

    @Override
    public void write(NutsId obj, NanoDBOutputStream out, NutsSession session) {
        out.writeUTF(obj.getLongName());
    }

    @Override
    public NutsId read(NanoDBInputStream in, Class expectedType, NutsSession session) {
        return NutsId.of(in.readUTF()).get( this.session);
    }
}
