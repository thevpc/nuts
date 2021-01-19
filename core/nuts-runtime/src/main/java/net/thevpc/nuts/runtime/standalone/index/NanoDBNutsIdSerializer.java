package net.thevpc.nuts.runtime.standalone.index;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsIdParser;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.bundles.nanodb.NanoDBInputStream;
import net.thevpc.nuts.runtime.bundles.nanodb.NanoDBNonNullSerializer;
import net.thevpc.nuts.runtime.bundles.nanodb.NanoDBNullSerializer;
import net.thevpc.nuts.runtime.bundles.nanodb.NanoDBOutputStream;

public class NanoDBNutsIdSerializer {
    public static class NonNull extends NanoDBNonNullSerializer<NutsId> {
        private final NutsWorkspace ws;

        public NonNull(NutsWorkspace ws) {
            super(NutsId.class);
            this.ws = ws;
        }

        @Override
        public void write(NutsId obj, NanoDBOutputStream out) {
            out.writeUTF(obj.getLongName());
        }

        @Override
        public NutsId read(NanoDBInputStream in) {
            NutsIdParser parser = ws.id().parser();
            return parser.parse(in.readUTF());
        }
    }
    
    public static class Null extends NanoDBNullSerializer<NutsId> {
        private final NutsWorkspace ws;

        public Null(NutsWorkspace ws) {
            super(NutsId.class);
            this.ws = ws;
        }

        @Override
        public void writeNonNull(NutsId obj, NanoDBOutputStream out) {
            out.writeUTF(obj.getLongName());
        }

        @Override
        public NutsId readNonNull(NanoDBInputStream in) {
            NutsIdParser parser = ws.id().parser();
            return parser.parse(in.readUTF());
        }
    }
}
