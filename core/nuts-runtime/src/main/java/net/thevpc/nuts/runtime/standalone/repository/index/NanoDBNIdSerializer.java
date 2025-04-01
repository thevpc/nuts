package net.thevpc.nuts.runtime.standalone.repository.index;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDBInputStream;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDBNonNullSerializer;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDBOutputStream;

public class NanoDBNIdSerializer extends NanoDBNonNullSerializer<NId>{

    public NanoDBNIdSerializer() {
        super(NId.class);
    }

    @Override
    public void write(NId obj, NanoDBOutputStream out) {
        out.writeUTF(obj.getLongName());
    }

    @Override
    public NId read(NanoDBInputStream in, Class expectedType) {
        return NId.get(in.readUTF()).get();
    }
}
