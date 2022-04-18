package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import net.thevpc.nuts.NutsEnum;
import net.thevpc.nuts.NutsSession;

import java.util.HashMap;
import java.util.Map;

class NanoDBSerializerForEnumByName extends NanoDBNonNullSerializer<Enum> {
    Map<Class, Object[]> constants = new HashMap<>();

    public NanoDBSerializerForEnumByName() {
        super(Enum.class);
    }

    @Override
    public void write(Enum obj, NanoDBOutputStream out, NutsSession session) {
        out.writeUTF(obj.name());
    }

    @Override
    public Enum read(NanoDBInputStream in, Class expectedType, NutsSession session) {
        String o = in.readUTF();
        if (NutsEnum.class.isAssignableFrom(expectedType)) {
            return (Enum) NutsEnum.parse(expectedType, o).get(session);
        } else {
            return Enum.valueOf(expectedType, o);
        }
    }
}
