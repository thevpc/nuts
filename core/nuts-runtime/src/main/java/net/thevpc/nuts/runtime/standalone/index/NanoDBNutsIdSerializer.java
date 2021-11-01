package net.thevpc.nuts.runtime.standalone.index;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsIdParser;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.bundles.nanodb.NanoDBInputStream;
import net.thevpc.nuts.runtime.bundles.nanodb.NanoDBNonNullSerializer;
import net.thevpc.nuts.runtime.bundles.nanodb.NanoDBNullSerializer;
import net.thevpc.nuts.runtime.bundles.nanodb.NanoDBOutputStream;

public class NanoDBNutsIdSerializer extends NanoDBNonNullSerializer<NutsId>{
    private final NutsSession session;

    public NanoDBNutsIdSerializer(NutsSession session) {
        super(NutsId.class);
        this.session = session;
    }

    @Override
    public void write(NutsId obj, NanoDBOutputStream out) {
        out.writeUTF(obj.getLongName());
    }

    @Override
    public NutsId read(NanoDBInputStream in) {
        return NutsId.of(in.readUTF(),session);
    }
}
