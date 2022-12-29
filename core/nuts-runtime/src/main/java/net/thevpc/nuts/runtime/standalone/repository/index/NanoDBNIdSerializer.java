package net.thevpc.nuts.runtime.standalone.repository.index;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDBInputStream;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDBNonNullSerializer;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDBOutputStream;

public class NanoDBNIdSerializer extends NanoDBNonNullSerializer<NId>{
    private final NSession session;

    public NanoDBNIdSerializer(NSession session) {
        super(NId.class);
        this.session = session;
    }

    @Override
    public void write(NId obj, NanoDBOutputStream out, NSession session) {
        out.writeUTF(obj.getLongName());
    }

    @Override
    public NId read(NanoDBInputStream in, Class expectedType, NSession session) {
        return NId.of(in.readUTF()).get( this.session);
    }
}
