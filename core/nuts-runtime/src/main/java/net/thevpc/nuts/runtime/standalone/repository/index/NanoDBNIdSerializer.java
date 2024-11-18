package net.thevpc.nuts.runtime.standalone.repository.index;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDBInputStream;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDBNonNullSerializer;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDBOutputStream;

public class NanoDBNIdSerializer extends NanoDBNonNullSerializer<NId>{
    private final NWorkspace workspace;

    public NanoDBNIdSerializer(NWorkspace workspace) {
        super(NId.class);
        this.workspace = workspace;
    }

    @Override
    public void write(NId obj, NanoDBOutputStream out) {
        out.writeUTF(obj.getLongName());
    }

    @Override
    public NId read(NanoDBInputStream in, Class expectedType) {
        return NId.of(in.readUTF()).get();
    }
}
