package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.NSession;

import java.util.HashMap;
import java.util.Map;

class NanoDBSerializerForEnumByName extends NanoDBNonNullSerializer<Enum> {
    Map<Class, Object[]> constants = new HashMap<>();

    public NanoDBSerializerForEnumByName() {
        super(Enum.class);
    }

    @Override
    public void write(Enum obj, NanoDBOutputStream out, NSession session) {
        out.writeUTF(obj.name());
    }

    @Override
    public Enum read(NanoDBInputStream in, Class expectedType, NSession session) {
        String o = in.readUTF();
        if (NEnum.class.isAssignableFrom(expectedType)) {
            return (Enum) NEnum.parse(expectedType, o).get(session);
        } else {
            return Enum.valueOf(expectedType, o);
        }
    }
}
