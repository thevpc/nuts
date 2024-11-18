package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import net.thevpc.nuts.util.NEnum;

import java.util.HashMap;
import java.util.Map;

class NanoDBSerializerForEnumByName extends NanoDBNonNullSerializer<Enum> {
    Map<Class, Object[]> constants = new HashMap<>();

    public NanoDBSerializerForEnumByName() {
        super(Enum.class);
    }

    @Override
    public void write(Enum obj, NanoDBOutputStream out) {
        out.writeUTF(obj.name());
    }

    @Override
    public Enum read(NanoDBInputStream in, Class expectedType) {
        String o = in.readUTF();
        if (NEnum.class.isAssignableFrom(expectedType)) {
            return (Enum) NEnum.parse(expectedType, o).get();
        } else {
            return Enum.valueOf(expectedType, o);
        }
    }
}
